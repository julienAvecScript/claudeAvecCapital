package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class PositionCard {

    public PositionCard(Dashboard dash, Position position) {
        this.btcFormat.setRoundingMode(RoundingMode.CEILING);
        this.usdcFormat.setRoundingMode(RoundingMode.CEILING);
        this.dash = dash;
        this.client = dash.getClient();
        this.position = position;
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
        updateStory(0, 0);
    }

    private void killPosition() {
        this.position.setEndTS();
        this.position.setStatus(8);
        this.playButton.setVisible(false);
        this.killButton.setVisible(false);
        System.out.print("KILL");
        updateStory(0, 0);
    }

    public void chokePosition() {
        this.position.setChoke(true);
        System.out.print("CHOKE");
        updateStory(0, 0);
    }

    private void viewOnDashboard() {
        this.dash.viewPosition(this.position);
    }

    public Position checkPosition(double spotPrice, double open) {
        this.presentation = "I AM " + this.position.getName() + " " + this.turns[this.position.getTurn()];
        System.out.println(this.presentation);
        if (this.position.getStatus() > 5) return null;
        double goal = this.position.check(spotPrice, open, this.client);
        System.out.println("(OPEN " + open + "): STATUS " + this.position.getStatus() + ", GOAL " + goal);
        updateStory(goal, open);
        if (this.status != 6) return null;
        this.playButton.setVisible(false);
        this.killButton.setVisible(false);
        if (!this.position.isRecursiveDrive()) return null;
        if (this.position.getRecursions() >= this.position.getRecursionLimit()) return null;
        Position newPos = new Position(this.position);
        return newPos;
    }

    public String getName() {
        return this.position.getName();
    }

    public void startPresentation() {
        this.presentation = "I AM " + this.position.getName() + " " + this.turns[this.position.getTurn()];
    }

    public void startStory() {
        startPresentation();
        this.story = this.presentation + "\nReady to roll";
    }

    private void updateStory(double goal, double open) {
        int prevStatus = this.status;
        double prevGoal = this.statusGoals[prevStatus];
        double prevOpen = this.open;
        this.status = this.position.getStatus();
        this.statusGoals[this.status] = goal;
        this.open = open;
        if (this.status == prevStatus && goal == prevGoal && open == prevOpen) return;
        startPresentation();
        DecimalFormat format = new DecimalFormat("###.##");
        this.story = this.presentation +"\n" + this.statuses[this.status];
        System.out.print(this.statusGoals[this.status]);
        if (this.statusGoals[this.status] > 0) {
            if (this.status == 2 && goal < 1) {
                double goalDisplay = open+(open*this.position.getMomReversalLimit());
                this.story += "NEXT OPEN | | " + format.format(goalDisplay);
            } else this.story += format.format(this.statusGoals[this.status]);
        }
        this.story += "\n";
        System.out.println(this.story);
        drawStory();
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

    private void drawStory() {
        GraphicsContext graph = this.canvas.getGraphicsContext2D();
        graph.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        Paint color = FXFactory.newDashColor(this.position.isSims());
        graph.setStroke(color);
        graph.setFill(color);
        graph.strokeText(this.story, this.canvas.getWidth()/4, Dashboard.CELL_HEIGHT, this.canvas.getWidth()/2);
    }

    public Pane getCard() {
        return this.card;
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
            "Mom reversal good @ ",
            "Filling buy @ ",
            "Holding for peak @ ",
            "Exiting @ ",
            "Closed @ ",
            "Choked",
            "Cancelled",
            "Pending"
    };
    private double open;
    private double[] statusGoals = new double[statuses.length];
    private DecimalFormat btcFormat = new DecimalFormat("XX.XXXXXXXX");
    private DecimalFormat usdcFormat = new DecimalFormat("XXX.XX");
}
