package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class PositionCard {

    public PositionCard(Dashboard dash, Position position) {
        this.dash = dash;
        this.client = dash.getClient();
        this.position = position;
        this.change = false;
        this.canvas = new Canvas(Dashboard.POSITION_WIDTH, Dashboard.POSITION_HEIGHT);
        this.card = new Pane();
        this.card.getChildren().add(canvas);
        checkStory();
        Button selector = new Button();
        selector.setOpacity(0);
        selector.setTranslateX(0);
        selector.setTranslateY(0);
        selector.setPrefSize(Dashboard.POSITION_WIDTH, Dashboard.POSITION_HEIGHT);
        selector.setOnAction(e -> viewOnDashboard());
        this.card.getChildren().add(selector);
        if (this.position.getStatus() == 8) return;
        System.out.println(this.position.getStatus());
        this.playButton = new Button("RUN");
        this.playButton.setTranslateX(Dashboard.POSITION_WIDTH-(Dashboard.CELL_WIDTH*10));
        this.playButton.setTranslateY((Dashboard.POSITION_HEIGHT/2)-(Dashboard.CELL_HEIGHT/2));
        this.playButton.setPrefSize(Dashboard.CELL_WIDTH*4, Dashboard.CELL_HEIGHT*4);
        this.playButton.setOnAction(e -> toggleChoke());
        this.killButton = new Button("KILL");
        this.killButton.setTranslateX(Dashboard.POSITION_WIDTH-(Dashboard.CELL_WIDTH*15));
        this.killButton.setTranslateY((Dashboard.POSITION_HEIGHT/2)-(Dashboard.CELL_HEIGHT/2));
        this.killButton.setPrefSize(Dashboard.CELL_WIDTH*4, Dashboard.CELL_HEIGHT*4);
        this.killButton.setOnAction(e -> killPosition());
        this.card.getChildren().addAll(this.playButton, this.killButton);
    }

    private void toggleChoke() {
        int prevStatus = this.position.getStatus();
        this.position.setChoke(!this.position.isChoke());
        if (this.position.isChoke()) this.playButton.setText("RUN");
        else this.playButton.setText("CHOKE");
        updateStory(prevStatus, 0);
    }

    private void killPosition() {
        int prevStatus = this.position.getStatus();
        this.position.setEndTS();
        this.position.setStatus(8);
        this.playButton.setVisible(false);
        this.killButton.setVisible(false);
        System.out.print("KILL");
        updateStory(prevStatus, 0);
    }

    public void chokePosition() {
        int prevStatus = this.position.getStatus();
        this.position.setChoke(true);
        System.out.print("CHOKE");
        updateStory(prevStatus, 0);
    }

    private void viewOnDashboard() {
        this.dash.viewPosition(this.position);
    }

    public Position checkPosition(double spotPrice, double open, double prevOpen, HashMap<String, String> orders) {
        System.out.print("CHECK. ");
        this.presentation = "I AM " + this.position.getName() + " " + this.turns[this.position.getTurn()];
        if (this.position.isChoke()) return null;
        if (this.position.getStatus() > 5) return null;
        this.change = false;
        boolean sims = this.position.isSims();
        int prevStatus = this.status;
        double prevExit = this.position.getExitPoint();
        String chapter = this.presentation;
        this.status = this.position.getStatus();
        double enter = this.position.getEntryPoiht();
        System.out.print(enter);
        double dip = enter-(enter*this.position.getDipLimit());
        double buy, peak, sell, nextSell;
        Position nextPosition = null;
        switch (this.status) {
            case 0:
                this.statusGoals[this.status] = enter;
                if (spotPrice > enter) {
                    if (this.position.isExplorationDrive()) this.position.setEntryPoiht(spotPrice);
                    break;
                } else this.status = 1;
            case 1:
                this.position.setTurn(1);
                this.statusGoals[this.status] = dip;
                if (spotPrice > dip) break;
                this.status = 2;
                this.dip = spotPrice;
            case 2:
                if (spotPrice < this.dip) {
                    this.dip = spotPrice;
                }
                buy = this.dip + (this.dip * this.position.getMomReversalLimit());
                this.statusGoals[this.status] = buy;
                if (spotPrice < buy) break;
                if (spotPrice < open) break;
                double qty = this.position.getSize()/buy;
                if (!sims) {
                    JsonNode buyOrder = this.client.postStopBuy(String.valueOf(buy), String.valueOf(qty));
                    if (buyOrder == null) break;
                    String buyOrderID = buyOrder.get("id").asText();
                    System.out.println("ID: " + buyOrderID + ": " + qty + " FOR " + buy);
                    this.position.setBuyOrderID(UUID.fromString(buyOrderID));
                }
                this.position.setBuyPrice(buy);
                this.position.setSizeBTC(qty);
                this.status = 3;
            case 3:
                buy = this.position.getBuyPrice();
                this.statusGoals[this.status] = buy;
                boolean buyFilled;
                if (sims) {
                    buyFilled = true;
                } else {
                    JsonNode fillBuyOrder = this.client.getOrder(this.position.getBuyOrderID().toString());
                    double fillBuy = Double.parseDouble(fillBuyOrder.get("filled_size").asText());
                    buyFilled = (fillBuy == this.position.getSizeBTC());
                }
                if (buyFilled) this.status = 4;
                else break;
            case 4:
                this.position.setTurn(2);
                buy = this.position.getBuyPrice();
                peak = Math.max(spotPrice, buy+(buy*this.position.getPeakLimit()));
                sell = peak-(peak*this.position.getExitLimit());
                this.statusGoals[this.status] = peak;
                if (spotPrice < peak) break;
                if (!sims) {
                    JsonNode sellOrder = this.client.postStopSell(String.valueOf(sell), String.valueOf(this.position.getSizeBTC()));
                    if (sellOrder == null) break;
                    String sellOrderID = sellOrder.get("id").asText();
                    System.out.println(sellOrderID);
                    this.position.setSellOrderID(UUID.fromString(sellOrderID));
                    System.out.println("ID: " + this.position.getBuyOrderID() + ": " + this.position.getSizeBTC() + " FOR " + sell);
                }
                this.status = 5;
            case 5:
                this.position.setTurn(3);
                sell = this.position.getExitPoint();
                this.statusGoals[this.status] = sell;
                nextSell = sell+(sell*this.position.getPeakLimit());
                JsonNode fillSellOrder = this.client.getOrder(this.position.getBuyOrderID().toString());
                double fillSell = Double.parseDouble(fillSellOrder.get("filled_size").asText());
                boolean sellFilled = (fillSell == this.position.getSizeBTC());
                if (sellFilled) {
                    this.position.setExitPoint(sell);
                    this.status = 6;
                } else {
                    if (spotPrice > sell) {
                        if (spotPrice < nextSell) break;
                        //JsonNode deleteOrder = this.client.deleteOrder(this.position.getBuyOrderID().toString());
                        //validate success
                        //SEND STOP SELL TO COINBASE
                        //if SUCCESSFUL this.position.setSellOrderID(orderUUID);
                        this.position.setExitPoint(sell + (sell * this.position.getExitLimit()));
                        this.statusGoals[this.status] = this.position.getExitLimit();
                        break;
                    } else {
                        double size = this.position.getSizeBTC();
                        this.position.setSize(size * sell);
                        this.status = 6;
                        this.position.setClosePoint(sell);
                        if (!this.position.isRecursiveDrive()) break;
                        if (this.position.getRecursions() == this.position.getRecursionLimit()) break;
                        nextPosition = new Position(this.position);
                    }
                }
            case 6:
                this.statusGoals[this.status] = this.position.getClosePoint();
                this.position.setEndTS();
            default:
                break;
        }
        updateStory(prevStatus, prevExit);
        this.position.setStatus(this.status);
        return nextPosition;
    }

    public void updateLimit(HashMap<String, String> map) {

    }

    public void startPresentation() {
        this.presentation = "I AM " + this.position.getName() + " " + this.turns[this.position.getTurn()];
    }

    public void startStory() {
        startPresentation();
        this.story = this.presentation + "\nReady to roll";
    }

    private void updateStory(int prevStatus, double prevExit) {
        String prevStory = this.story;
        System.out.print(this.position.getTurn() + ", " + this.position.getStatus() + ". ");
        if (this.status != prevStatus || this.position.getExitPoint() != prevExit);
        this.change = true;
        startPresentation();
        DecimalFormat format = new DecimalFormat("###.##");
        this.story = this.presentation +"\n" + this.statuses[this.status];
        System.out.print(this.statusGoals[this.status]);
        if (this.statusGoals[this.status] > 0) {
            this.story += format.format(this.statusGoals[this.status]);
        }
        this.story += "\n";
        drawStory();
    }

    private void drawStory() {
        GraphicsContext graph = this.canvas.getGraphicsContext2D();
        graph.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        Paint color = FXFactory.newDashColor(this.position.isSims());
        graph.setStroke(color);
        graph.setFill(color);
        graph.strokeText(this.story, this.canvas.getWidth()/4, Dashboard.CELL_HEIGHT, this.canvas.getWidth()/2);
    }

    private void checkStory() {
        startPresentation();
        if (this.position.getStatus() == 8) {
            this.story = this.presentation + "\nCancelled";
        } else {
            this.story = this.presentation + "\nReady to roll";
        }
        drawStory();
    }

    public Pane getCard() {
        return this.card;
    }

    public boolean getChange() {
        return this.change;
    }

    private Dashboard dash;
    private CoinbaseClient client;
    private TextArea storyArea;
    private Position position;
    private Canvas canvas;
    private Pane card;
    private Button playButton;
    private Button killButton;
    private String presentation;
    private String story;
    private double dip;
    private String[] turns = {
            "",
            "LO",
            "HYST",
            "HI"
    };
    private int status;
    private String[] statuses = {
            "Entering @ ",
            "Waiting for dip @ ",
            "Momentum reversal good @ ",
            "Filling buy @ ",
            "Holding for peak @ ",
            "Exiting @ ",
            "Closed @ ",
            "Choked",
            "Cancelled",
            "Pending"
    };
    private double[] statusGoals = new double[statuses.length];
    private boolean change;
}
