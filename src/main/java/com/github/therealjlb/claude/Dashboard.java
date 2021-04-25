/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.therealjlb.claude;

import java.io.*;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

/**
 *
 * @author Jonathan
 */
public class Dashboard extends Application {


    public static void main(String args[]) {
        launch(args);
    }
    
    public void start(Stage stage) {
        this.stage = stage;
        seekProfile();
    }
        
    private void seekProfile() {
        Pane pane = FXFactory.newPane(this.sims);
        Button newProfileButton = new Button("NEW PROFILE");
        newProfileButton.setOnAction(e -> createNewProfile());
        newProfileButton.setPrefSize(PREFERRED_WIDTH/4, CELL_HEIGHT*5);
        newProfileButton.setLayoutX((PREFERRED_WIDTH/2)-(newProfileButton.getPrefWidth()/2));
        newProfileButton.setLayoutY((PREFERRED_HEIGHT/2)-(CELL_HEIGHT*3));
        Button loadProfileButton = new Button("LOAD PROFILE");
        loadProfileButton.setPrefSize(PREFERRED_WIDTH/4, CELL_HEIGHT*5);
        loadProfileButton.setLayoutX((PREFERRED_WIDTH/2)-(newProfileButton.getPrefWidth()/2));
        loadProfileButton.setLayoutY((PREFERRED_HEIGHT/2)+(CELL_HEIGHT*3));
        loadProfileButton.setOnAction(e -> selectProfile());
        pane.getChildren().addAll(newProfileButton, loadProfileButton);
        Scene profileScene = new Scene(pane, 500, 800);
        this.stage.setTitle("CLAUDE AVEC CAPITAL");
        this.stage.setScene(profileScene);
        this.stage.show();
    }

    private File getProfileDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("src"));
        return directoryChooser.showDialog(this.stage);
    }

    private void createNewProfile() {
        this.profileDir = getProfileDirectory();
        if (this.profileDir == null) return;
        String loco = this.profileDir.getAbsolutePath();
        System.out.println("PROFILE GOT: " + loco);
        String infoPath = loco + "\\info.claude";
        this.infoFile = new File(infoPath);
        System.out.print("INFO: " + infoPath + "? ");
        try {
            if (this.infoFile.createNewFile()) {
                initProfile();
            } else {
                System.out.println("ALREADY THERE");
                //SET UI ERROR MESSAGE
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String posPath = loco + "\\positions.claude";
        this.positionFile = new File(posPath);
        System.out.print("POSITIONS: " + posPath + "? ");
        try {
            if (this.positionFile.createNewFile()) {
                initPositions();
            } else {
                System.out.println("ALREADY THERE");
                //SET UI ERROR MESSAGE
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        seekAuthentication();
    }

    private void initProfile() {
        this.profile = new Profile();
    }

    private void initPositions() {
        this.positions = new ArrayList<Position>();
        this.positionCards = new ArrayList<PositionCard>();
        this.positionBox = new VBox();
    }

    private void selectProfile() {
        this.profileDir = getProfileDirectory();
        if (this.profileDir == null) return;
        String loco = this.profileDir.getAbsolutePath();
        String infoPath = loco + "\\info.claude";
        System.out.print("INFO: " + infoPath + "? ");
        this.infoFile = new File(infoPath);
        try {
            byte[] rawJSON = Files.readAllBytes(this.infoFile.toPath());
            initProfile(rawJSON);
        } catch (FileNotFoundException e) {
            System.out.println("NOT FOUND");
            //SET UI ERROR MESSAGE
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String posPath = loco + "\\positions.claude";
        System.out.print("POSITIONS: " + posPath + "? ");
        this.positionFile = new File(posPath);
        try {
            byte[] rawJSON = Files.readAllBytes(this.positionFile.toPath());
            initPositions(rawJSON);
        } catch (FileNotFoundException e) {
            System.out.println("NOT FOUND");
            //SET UI ERROR MESSAGE
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        seekAuthentication();
    }

    private void initProfile(byte[] rawJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HashMap<String, String> proMap = new HashMap<String, String>();
            proMap = objectMapper.readValue(rawJSON, new TypeReference<HashMap<String, String>>() {});
            if (proMap == null) initProfile();
            String key = proMap.get("key");
            String passphrase = proMap.get("passphrase");
            String secret = proMap.get("secret");
            this.client = new CoinbaseClient(key, passphrase, secret);
            this.walletProducer = new WalletProducer(this);
        } catch (MismatchedInputException e) {
            initProfile();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void initPositions(byte[] rawJSON) {
        ObjectMapper objectMapper = new ObjectMapper();
        initPositions();
        try {
            HashMap<String, HashMap<String, String>> posMap = new HashMap<String, HashMap<String, String>>();
            posMap = objectMapper.readValue(rawJSON, new TypeReference<HashMap<String, HashMap<String, String>>>() {});
            if (posMap == null) initPositions();
            Iterator it = posMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, HashMap<String, String>> pair = (Map.Entry<String, HashMap<String, String>>) it.next();
                Position position = new Position(pair.getValue());
                PositionCard positionCard = new PositionCard(this, position);
                this.positions.add(position);
                this.positionCards.add(positionCard);
                this.positionBox.getChildren().add(positionCard.getCard());
            }
        } catch (MismatchedInputException e) {
            initPositions();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void seekAuthentication() {
        Pane pane = FXFactory.newPane(this.sims);
        Button enterKeyButton = new Button("USE PROFILE KEY");
        enterKeyButton.setPrefSize(PREFERRED_WIDTH/4, CELL_HEIGHT*5);
        enterKeyButton.setLayoutX((PREFERRED_WIDTH/2)-(enterKeyButton.getPrefWidth()/2));
        enterKeyButton.setLayoutY((PREFERRED_HEIGHT/2)-(CELL_HEIGHT*3));
        enterKeyButton.setOnAction(e -> useProfileKey());
        Button beginSimsButton = new Button("BEGIN SIMULATION");
        beginSimsButton.setPrefSize(PREFERRED_WIDTH/4, CELL_HEIGHT*5);
        beginSimsButton.setLayoutX((PREFERRED_WIDTH/2)-(beginSimsButton.getPrefWidth()/2));
        beginSimsButton.setLayoutY((PREFERRED_HEIGHT/2)+(CELL_HEIGHT*3));
        beginSimsButton.setOnAction(e -> startClaude(true));
        pane.getChildren().addAll(enterKeyButton, beginSimsButton);
        Scene authenticateScene = new Scene(pane, 500, 800);
        this.stage.setScene(authenticateScene);
    }

    private void useProfileKey() {
        if (this.client == null) return;
        if (this.walletProducer == null) return;
        startClaude(false);
    }

    private void startClaude(boolean simsLock) {
        this.sims = simsLock;
        this.simsLock = simsLock;
        this.dashboardPane = FXFactory.newPane(this.sims);
        if (!this.sims) initWallet();
        initUI();
        initCandles();
        initTicker();
    }

    private void initWallet() {
        String cur1, cur2;
        cur1 = "BTC";
        cur2 = "USDC";
        this.walletNodes = new JsonNode[2];
        this.currencyPair = new String[2];
        this.currencyPair[0] = cur1;
        this.currencyPair[1] = cur2;
        this.pairBalanceFormat = new DecimalFormat[2];
        this.pairBalanceFormat[0] = new DecimalFormat("###.########");
        this.pairBalanceFormat[1] = new DecimalFormat("###.##");
        Text walletLabel = FXFactory.newDisplay(this.dashboardPane);
        walletLabel.setTranslateX(SCROLLER_LEFT);
        walletLabel.setTranslateY(GRAPH_TOP+(GRAPH_HALF_HEIGHT*2)+CELL_HEIGHT*5);
        walletLabel.setText("WALLET");
        this.pairBalanceDisplay = new Text[2];
        this.pairBalanceDisplay[0] = FXFactory.newDisplay(this.dashboardPane);
        this.pairBalanceDisplay[0].setX(CELL_WIDTH*2);
        this.pairBalanceDisplay[0].setY(GRAPH_TOP+(GRAPH_HALF_HEIGHT*2)+CELL_HEIGHT*10);
        Text pairBalanceLabel1 = FXFactory.newLabel(this.currencyPair[0], this.dashboardPane);
        pairBalanceLabel1.setX(CELL_WIDTH*2);
        pairBalanceLabel1.setY(GRAPH_TOP+(GRAPH_HALF_HEIGHT*2)+CELL_HEIGHT*8);
        this.pairBalanceDisplay[1] = FXFactory.newDisplay(this.dashboardPane);
        this.pairBalanceDisplay[1].setX(CELL_WIDTH*14);
        this.pairBalanceDisplay[1].setY(GRAPH_TOP+(GRAPH_HALF_HEIGHT*2)+CELL_HEIGHT*10);
        Text pairBalanceLabel2 = FXFactory.newLabel(this.currencyPair[1], this.dashboardPane);
        pairBalanceLabel2.setX(CELL_WIDTH*14);
        pairBalanceLabel2.setY(GRAPH_TOP+(GRAPH_HALF_HEIGHT*2)+CELL_HEIGHT*8);
        requestWallet();
    }

    public void requestWallet() {
        Thread walletThread = new Thread(this.walletProducer);
        walletThread.start();
    }

    public void updateWallet(JsonNode[] accounts) {
        System.out.println("ACCOUNTS (" + accounts.length + ") vs. " + this.currencyPair[0] + "-" + this.currencyPair[1] + ": ");
        int finds = 0;
        for (JsonNode node : accounts) {
            String currency = node.get("currency").asText();
            System.out.println(node.toPrettyString());
            if (this.currencyPair[0].equals(currency)) {
                this.walletNodes[0] = node;
                double available = Double.parseDouble(node.get("available").asText());
                this.pairBalanceDisplay[0].setText(this.pairBalanceFormat[0].format(available));
                finds++;
            }
            if (this.currencyPair[1].equals(currency)) {
                this.walletNodes[1] = node;
                double available = Double.parseDouble(node.get("available").asText());
                this.pairBalanceDisplay[1].setText(this.pairBalanceFormat[1].format(available));
                finds++;
            }
            if (finds > 1) break;
        }
        System.out.println(" ***");
    }

    private void initUI() {
        this.spotPriceDisplay = FXFactory.newDisplay(this.dashboardPane);
        this.spotPriceDisplay.setX(CELL_WIDTH*2);
        this.spotPriceDisplay.setY(CELL_HEIGHT*12);
        Text spotPriceLabel = FXFactory.newLabel("SPOT PRICE", this.dashboardPane);
        spotPriceLabel.setX(CELL_WIDTH*2);
        spotPriceLabel.setY(CELL_HEIGHT*10);

        this.momentumDisplay = FXFactory.newDisplay(this.dashboardPane);
        this.momentumDisplay.setX(CELL_WIDTH*14);
        this.momentumDisplay.setY(CELL_HEIGHT*12);
        Text momentumLabel = FXFactory.newLabel("15M MOMENTUM", this.dashboardPane);
        momentumLabel.setX(CELL_WIDTH*14);
        momentumLabel.setY(CELL_HEIGHT*10);

        this.dayPriceDeltaDisplay = FXFactory.newDisplay(this.dashboardPane);
        this.dayPriceDeltaDisplay.setX(CELL_WIDTH*28);
        this.dayPriceDeltaDisplay.setY(CELL_HEIGHT*12);
        Text dayPriceDeltaLabel = FXFactory.newLabel("24H DELTA", this.dashboardPane);
        dayPriceDeltaLabel.setX(CELL_WIDTH*28);
        dayPriceDeltaLabel.setY(CELL_HEIGHT*10);

        this.graphCanvas = new Canvas((GRAPH_CANDLE_WIDTH*CANDLE_15M_MAX)+(CELL_WIDTH), GRAPH_HALF_HEIGHT*2);
        this.graphCanvas.setTranslateX(GRAPH_LEFT);
        this.graphCanvas.setTranslateY(GRAPH_TOP);
        this.dashboardPane.getChildren().add(this.graphCanvas);

        GraphicsContext graph = this.graphCanvas.getGraphicsContext2D();
        graph.setStroke(Color.WHITE);
        graph.setFill(Color.rgb(255, 255, 255, 1));
        graph.setLineWidth(2);
        graph.strokeLine(0, 0, 0, GRAPH_HALF_HEIGHT*2);
        graph.strokeLine(0, GRAPH_HALF_HEIGHT*2, GRAPH_CANDLE_WIDTH*CANDLE_15M_MAX, GRAPH_HALF_HEIGHT*2);

        this.graphHiDisplay = FXFactory.newLabel("", this.dashboardPane);
        this.graphHiDisplay.setX(CELL_WIDTH*2);
        this.graphHiDisplay.setY(GRAPH_TOP);
        this.graphHiDisplay.setTextOrigin(VPos.TOP);

        this.graphMidDisplay = FXFactory.newLabel("", this.dashboardPane);
        this.graphMidDisplay.setX(CELL_WIDTH*2);
        this.graphMidDisplay.setY(GRAPH_TOP+(GRAPH_HALF_HEIGHT));
        this.graphMidDisplay.setTextOrigin(VPos.CENTER);

        this.graphLoDisplay = FXFactory.newLabel("", this.dashboardPane);
        this.graphLoDisplay.setX(CELL_WIDTH*2);
        this.graphLoDisplay.setY(GRAPH_TOP+(GRAPH_HALF_HEIGHT*2));

        Button addPositionButton = new Button("+");
        addPositionButton.setTranslateX(SCROLLER_LEFT+SCROLLER_WIDTH-(CELL_WIDTH*3));
        addPositionButton.setTranslateY(SCROLLER_TOP-(CELL_HEIGHT*3));
        addPositionButton.setPrefWidth(CELL_WIDTH*3);
        addPositionButton.setPrefHeight(CELL_HEIGHT*3);
        addPositionButton.setOnAction(e -> viewPosition(null));
        this.dashboardPane.getChildren().add(addPositionButton);

        this.positionScroller = new ScrollPane(this.positionBox);
        this.positionScroller.setPrefSize(SCROLLER_WIDTH, SCROLLER_HEIGHT);
        this.positionScroller.setTranslateX(SCROLLER_LEFT);
        this.positionScroller.setTranslateY(SCROLLER_TOP);
        VBox.setVgrow(this.positionScroller, Priority.ALWAYS);
        this.positionScroller.setContent(this.positionBox);
        this.dashboardPane.getChildren().add(this.positionScroller);
        this.positionLabel = FXFactory.newDisplay(this.dashboardPane);
        this.positionLabel.setTranslateX(SCROLLER_LEFT);
        this.positionLabel.setTranslateY(SCROLLER_TOP-(CELL_HEIGHT*2));
        this.positionLabel.setText("POSITIONS");

        this.positionEditor = new Pane();
        this.positionEditor.setVisible(false);
        this.positionEditor.setPrefSize(SCROLLER_WIDTH, SCROLLER_HEIGHT);
        this.positionEditor.setTranslateX(SCROLLER_LEFT);
        this.positionEditor.setTranslateY(SCROLLER_TOP);

        this.nameField = FXFactory.newTextField("Booboo", this.positionEditor);
        this.nameField.setTranslateX(CELL_WIDTH);
        this.nameField.setTranslateY(CELL_HEIGHT*3);
        this.nameField.setPrefSize(CELL_WIDTH*16, CELL_HEIGHT*2);
        Text nameLabel = FXFactory.newLabel("NAME", this.positionEditor);
        nameLabel.setTranslateX(CELL_WIDTH);
        nameLabel.setTranslateY(CELL_HEIGHT*2);

        this.sizeField = FXFactory.newTextField("0", this.positionEditor);
        this.sizeField.setTranslateX(CELL_WIDTH*18);
        this.sizeField.setTranslateY(CELL_HEIGHT*3);
        this.sizeField.setPrefSize(CELL_WIDTH*6, CELL_HEIGHT*2);
        Text sizeLabel = FXFactory.newLabel("SIZE", this.positionEditor);
        sizeLabel.setTranslateX(CELL_WIDTH*18);
        sizeLabel.setTranslateY(CELL_HEIGHT*2);

        this.explorationDriveCheckBox = FXFactory.newCheckBox(this.positionEditor);
        this.explorationDriveCheckBox.setTranslateX(CELL_WIDTH*28);
        this.explorationDriveCheckBox.setTranslateY(CELL_HEIGHT*3);
        this.explorationDriveCheckBox.setPrefSize(CELL_WIDTH*2, CELL_HEIGHT*2);
        Text explorationLabel = FXFactory.newLabel("EXPLO DRIVE", this.positionEditor);
        explorationLabel.setTranslateX(CELL_WIDTH*28);
        explorationLabel.setTranslateY(CELL_HEIGHT*2);

        this.entryPointField = FXFactory.newTextField("0", this.positionEditor);
        this.entryPointField.setTranslateX(CELL_WIDTH*36);
        this.entryPointField.setTranslateY(CELL_HEIGHT*3);
        this.entryPointField.setPrefSize(CELL_WIDTH*6, CELL_HEIGHT*2);
        Text entryPointabel = FXFactory.newLabel("ENTRY POINT", this.positionEditor);
        entryPointabel.setTranslateX(CELL_WIDTH*36);
        entryPointabel.setTranslateY(CELL_HEIGHT*2);

        this.dipLimitField = FXFactory.newTextField("0", this.positionEditor);
        this.dipLimitField.setTranslateX(CELL_WIDTH);
        this.dipLimitField.setTranslateY(CELL_HEIGHT*9);
        this.dipLimitField.setPrefSize(CELL_WIDTH*4, CELL_HEIGHT*2);
        Text dipLimitLabel = FXFactory.newLabel("DIP LIMIT", this.positionEditor);
        dipLimitLabel.setTranslateX(CELL_WIDTH);
        dipLimitLabel.setTranslateY(CELL_HEIGHT*8);

        this.improbabilityDriveCheckBox = FXFactory.newCheckBox(this.positionEditor);
        this.improbabilityDriveCheckBox.setTranslateX(CELL_WIDTH*8);
        this.improbabilityDriveCheckBox.setTranslateY(CELL_HEIGHT*9);
        this.improbabilityDriveCheckBox.setPrefSize(CELL_WIDTH*2, CELL_HEIGHT*2);
        Text improbabilityDriveLabel = FXFactory.newLabel("IMPRO DRIVE", this.positionEditor);
        improbabilityDriveLabel.setTranslateX(CELL_WIDTH*8);
        improbabilityDriveLabel.setTranslateY(CELL_HEIGHT*8);

        this.improbabilityLimitField = FXFactory.newTextField("0", this.positionEditor);
        this.improbabilityLimitField.setTranslateX(CELL_WIDTH*18);
        this.improbabilityLimitField.setTranslateY(CELL_HEIGHT*9);
        this.improbabilityLimitField.setPrefSize(CELL_WIDTH*4, CELL_HEIGHT*2);
        Text improbabilityLimitLabel = FXFactory.newLabel("IMPROBABILITY LIMIT", this.positionEditor);
        improbabilityLimitLabel.setTranslateX(CELL_WIDTH*18);
        improbabilityLimitLabel.setTranslateY(CELL_HEIGHT*8);

        this.momReversalLimitField = FXFactory.newTextField("0", this.positionEditor);
        this.momReversalLimitField.setTranslateX(CELL_WIDTH);
        this.momReversalLimitField.setTranslateY(CELL_HEIGHT*14);
        this.momReversalLimitField.setPrefSize(CELL_WIDTH*4, CELL_HEIGHT*2);
        Text momReversalLimitLabel = FXFactory.newLabel("MOMEN LIMIT", this.positionEditor);
        momReversalLimitLabel.setTranslateX(CELL_WIDTH);
        momReversalLimitLabel.setTranslateY(CELL_HEIGHT*13);

        this.peakLimitField = FXFactory.newTextField("0", this.positionEditor);
        this.peakLimitField.setTranslateX(CELL_WIDTH*10);
        this.peakLimitField.setTranslateY(CELL_HEIGHT*14);
        this.peakLimitField.setPrefSize(CELL_WIDTH*4, CELL_HEIGHT*2);
        Text peakLimitLabel = FXFactory.newLabel("PEAK LIMIT", this.positionEditor);
        peakLimitLabel.setTranslateX(CELL_WIDTH*10);
        peakLimitLabel.setTranslateY(CELL_HEIGHT*13);

        this.exitLimitField = FXFactory.newTextField("0", this.positionEditor);
        this.exitLimitField.setTranslateX(CELL_WIDTH);
        this.exitLimitField.setTranslateY(CELL_HEIGHT*20);
        this.exitLimitField.setPrefSize(CELL_WIDTH*4, CELL_HEIGHT*2);
        Text exitLimitLabel = FXFactory.newLabel("EXIT LIMIT", this.positionEditor);
        exitLimitLabel.setTranslateX(CELL_WIDTH);
        exitLimitLabel.setTranslateY(CELL_HEIGHT*19);

        this.recursiveDriveCheckBox = FXFactory.newCheckBox(this.positionEditor);
        this.recursiveDriveCheckBox.setTranslateX(CELL_WIDTH*18);
        this.recursiveDriveCheckBox.setTranslateY(CELL_HEIGHT*20);
        this.recursiveDriveCheckBox.setPrefSize(CELL_WIDTH*2, CELL_HEIGHT*2);
        Text recursiveDriveLabel = FXFactory.newLabel("RECUR DRIVE", this.positionEditor);
        recursiveDriveLabel.setTranslateX(CELL_WIDTH*18);
        recursiveDriveLabel.setTranslateY(CELL_HEIGHT*19);

        this.recursiveDriveLimitField = FXFactory.newTextField("0", this.positionEditor);
        this.recursiveDriveLimitField.setTranslateX(CELL_WIDTH*28);
        this.recursiveDriveLimitField.setTranslateY(CELL_HEIGHT*20);
        this.recursiveDriveLimitField.setPrefSize(CELL_WIDTH*4, CELL_HEIGHT*2);
        Text recursiveLimitLabel = FXFactory.newLabel("RECURSION LIMIT", this.positionEditor);
        recursiveLimitLabel.setTranslateX(CELL_WIDTH*28);
        recursiveLimitLabel.setTranslateY(CELL_HEIGHT*19);

        Button saveButton = new Button("SAVE");
        saveButton.setTranslateX(SCROLLER_WIDTH/2);
        saveButton.setTranslateY(SCROLLER_HEIGHT-(CELL_HEIGHT*4));
        saveButton.setPrefWidth(CELL_WIDTH*6);
        saveButton.setPrefHeight(CELL_HEIGHT*3);
        saveButton.setOnAction(e -> savePosition());
        this.positionEditor.getChildren().add(saveButton);
        Button cancelButton = new Button("CANCEL");
        cancelButton.setTranslateX((SCROLLER_WIDTH/2)+(CELL_WIDTH*7));
        cancelButton.setTranslateY(SCROLLER_HEIGHT-(CELL_HEIGHT*4));
        cancelButton.setPrefWidth(CELL_WIDTH*6);
        cancelButton.setPrefHeight(CELL_HEIGHT*3);
        cancelButton.setOnAction(e -> viewDashboard());
        this.positionEditor.getChildren().add(cancelButton);

        this.dashboardPane.getChildren().add(this.positionEditor);
        Scene dashboardScene = new Scene(this.dashboardPane, PREFERRED_WIDTH, PREFERRED_HEIGHT);
        this.stage.setScene(dashboardScene);
        this.dashboardPane.scaleXProperty().bind(new SimpleDoubleProperty(1.0).divide(dashboardScene.getWindow().outputScaleXProperty()));
        this.dashboardPane.scaleYProperty().bind(new SimpleDoubleProperty(1.0).divide(dashboardScene.getWindow().outputScaleYProperty()));
        this.stage.show();
    }

    private void testOrder() {
        JsonNode result = this.client.postStopBuy(this.test1.getText(), this.test2.getText());
        this.test3.setText(result.get("id").asText());
    }

    public void viewPosition(Position position) {
        this.positionScroller.setVisible(false);
        this.positionEditor.setVisible(true);
        if (position == null) {
            this.positionLabel.setText("WHAT'S YOUR POSITION?");
            String defaultName = "Booboo";
            int nb = countPositionNames(defaultName);
            if (nb > 0) defaultName += " " + nb;
            this.nameField.setText(defaultName);
            this.sizeField.setText("0");
            this.explorationDriveCheckBox.setSelected(false);
            this.entryPointField.setText("0");
            this.improbabilityDriveCheckBox.setSelected(false);
            this.improbabilityLimitField.setText("0");
            this.dipLimitField.setText("0");
            this.momReversalLimitField.setText("0");
            this.peakLimitField.setText("0");
            this.exitLimitField.setText("0");
            this.recursiveDriveCheckBox.setSelected(false);
            this.recursiveDriveLimitField.setText("0");
        } else {
            this.selectedPosition = position;
            this.positionLabel.setText("THAT'S YOUR POSITION");
            this.nameField.setText(position.getName());
            this.sizeField.setText(Double.toString(position.getSize()));
            this.explorationDriveCheckBox.setSelected(position.isExplorationDrive());
            this.entryPointField.setText(Double.toString(position.getEntryPoiht()));
            this.improbabilityDriveCheckBox.setSelected(position.isImprobabilityDrive());
            this.improbabilityLimitField.setText(Double.toString(position.getImprobabilityLimit()));
            this.dipLimitField.setText(Double.toString(position.getDipLimit()));
            this.momReversalLimitField.setText(Double.toString(position.getMomReversalLimit()));
            this.peakLimitField.setText(Double.toString(position.getPeakLimit()));
            this.exitLimitField.setText(Double.toString(position.getExitLimit()));
            this.recursiveDriveCheckBox.setSelected(position.isRecursiveDrive());
            this.recursiveDriveLimitField.setText(Integer.toString(position.getRecursionLimit()));
        }
    }

    private int countPositionNames(String name) {
        String nakedName = name.replaceAll("[^0-9]", "");
        int nb = 0;
        for (Position position : this.positions) {
            if (position.getName().contains(nakedName)) nb++;
        }
        return nb;
    }

    private void viewDashboard() {
        this.positionLabel.setText("POSITIONS");
        this.selectedPosition = null;
        for (PositionCard posCard : this.positionCards){
            posCard.startStory();
        }
        this.positionEditor.setVisible(false);
        this.positionScroller.setVisible(true);
    }

    private void savePosition() {
        if (this.selectedPosition == null) {
            this.selectedPosition = new Position(this.sims);
            this.selectedPosition.setName(this.nameField.getText());
            this.selectedPosition.setTurn(1);
            PositionCard positionCard = new PositionCard(this, this.selectedPosition);
            positionCard.startStory();
            this.positions.add(this.selectedPosition);
            this.positionCards.add(positionCard);
            this.positionBox.getChildren().add(positionCard.getCard());
        }
        this.selectedPosition.setName(this.nameField.getText());
        this.selectedPosition.setSize(Double.parseDouble(this.sizeField.getText()));
        this.selectedPosition.setExplorationDrive(this.explorationDriveCheckBox.isSelected());
        this.selectedPosition.setEntryPoiht(Double.parseDouble(this.entryPointField.getText()));
        this.selectedPosition.setImprobabilityDrive(this.improbabilityDriveCheckBox.isSelected());
        this.selectedPosition.setImprobabilityLimit(Double.parseDouble(this.improbabilityLimitField.getText()));
        this.selectedPosition.setDipLimit(Double.parseDouble(this.dipLimitField.getText()));
        this.selectedPosition.setMomReversalLimit(Double.parseDouble(this.momReversalLimitField.getText()));
        this.selectedPosition.setPeakLimit(Double.parseDouble(this.peakLimitField.getText()));
        this.selectedPosition.setExitLimit(Double.parseDouble(this.exitLimitField.getText()));
        this.selectedPosition.setRecursiveDrive(this.recursiveDriveCheckBox.isSelected());
        this.selectedPosition.setRecursionLimit(Integer.parseInt(this.recursiveDriveLimitField.getText()));
        viewDashboard();
    }

    private void initCandles() {
        this.candles15m = new ArrayList<TickCandle>();
        this.candleLive = new TickCandle(CANDLE_15M_SIZE);
        this.candles15m.add(this.candleLive);
        this.candleStep = 0;
        this.candleGo = true;
    }
    
    private void initTicker() {
        //WEBSOCKET IS IN DEVELOPMENT
        //FOR NOW USE POLLER (NOT RECOMMENDED)
        if (true) {
            TickPoller poller = new TickPoller(this, "BTC-USDC");
            this.ticker = new Ticker(poller);
        } else {
            TickSession session = new TickSession(this, "BTC-USDC");
            this.ticker = new Ticker(session);
        }
        Thread tickThread = new Thread(this.ticker);
        tickThread.run();
    }

    public void updateDashboard(JsonNode node) {
        this.spotPrice = Double.parseDouble(node.get("price").asText());
        this.spotPriceDisplay.setText(String.valueOf(this.spotPrice));
        updateHiLoWithSpotPrice(false);
        double spotPrice24h = updateCandles();
        updatePositions();
        if (spotPrice24h == 0) return;
        double delta = this.spotPrice-spotPrice24h;
        DecimalFormat format = new DecimalFormat("###.##");
        this.dayPriceDelta = (delta/spotPrice24h)*100;
        this.dayPriceDeltaDisplay.setText(format.format(this.dayPriceDelta) + "%");
        if (this.candleLive == null) return;
        double momPer = ((this.spotPrice-this.candleLive.getOpen())/this.candleLive.getOpen())*100;
        this.momentumDisplay.setText(format.format(momPer) + "%");
    }

    private void updateHiLoWithSpotPrice(boolean redraw) {
        if (this.lo == 0) this.lo = this.spotPrice;
        if (this.hi == 0) this.hi = this.spotPrice;
        double prevLo, prevHi;
        prevLo = this.lo;
        prevHi = this.hi;
        this.lo = Math.min(this.spotPrice, this.lo);
        this.hi = Math.max(this.spotPrice, this.hi);
        if (prevLo > this.lo || prevHi < this.hi) redraw = true;
        if (redraw) drawCandles();
    }

    private void updatePositions() {
        if (this.candles15m.size() < 1) return;
        double open;
        open = this.candleLive.getOpen();
        for (PositionCard posCard : this.positionCards) {
            System.out.println("CHECK " + posCard.getName() + ". ");
            posCard.checkPosition(this.spotPrice, open);
        }
    }

    private void drawCandles() {
        GraphicsContext graph = this.graphCanvas.getGraphicsContext2D();
        graph.clearRect(0, 0, PREFERRED_WIDTH, GRAPH_HALF_HEIGHT*2);
        graph.setStroke(Color.WHITE);
        graph.setLineWidth(1);
        graph.setFill(Color.rgb(255, 255, 255, 0.3));
        graph.strokeLine(0, 0, 0, GRAPH_HALF_HEIGHT*2);
        graph.strokeLine(0, GRAPH_HALF_HEIGHT*2, GRAPH_CANDLE_WIDTH*CANDLE_15M_MAX, GRAPH_HALF_HEIGHT*2);
        int i = 1;
        for (TickCandle candle : this.candles15m) {
            double x = i*this.GRAPH_CANDLE_WIDTH;
            double rangeY = this.graphHi-this.graphLo;
            double deltaY1 = this.graphHi-candle.getHi();
            double deltaPer1 = deltaY1/rangeY;
            double y = (this.GRAPH_HALF_HEIGHT*2)*deltaPer1;
            double w = this.GRAPH_CANDLE_WIDTH;
            double deltaY2 = this.graphHi-candle.getLo();
            double deltaPer2 = deltaY2/rangeY;
            double y2 = (this.GRAPH_HALF_HEIGHT*2)*deltaPer2;
            double h = y2-y;//Math.max(y-y2, 1);
            graph.strokeRect(x, y, w, h);
            i++;
        }
    }

    private double updateCandles() {
        //System.out.print("CANDLE GO: " + this.candleGo + " ");
        if (!this.candleGo) return 0;
        //System.out.print("CANDLE LIST SIZE: " + this.candles15m.size() + " ");
        if (this.candles15m.size() < 1) return 0;
        //System.out.print("CANDLE LIVE. ");
        if (this.candleLive == null) return 0;
        //System.out.print("UPDATE CANDLES GO: " + this.candles15m.size() + "/" + this.CANDLE_15M_MAX + ": " + this.candleStep + "/" + this.CANDLE_15M_SIZE + ". ");
        updateGraphLimits();
        TickPoint tick = new TickPoint(this.spotPrice, this.candleStep);
        if (this.candleStep == CANDLE_15M_SIZE) {
            this.candleStep = 0;
            this.candleLive = new TickCandle(CANDLE_15M_SIZE);
            this.candles15m.add(candleLive);
            if (this.candles15m.size() > CANDLE_15M_MAX) {
                this.candles15m.remove(0);
                updateHiLoWithCandles();
                drawCandles();
            }
        }
        this.candleLive.addTick(tick);
        if (this.candleLive.countTicks() == 1) drawLiveCandle();
        drawTickCandle(tick);
        double spotPrice24h = this.candles15m.get(0).getTickPrice(candleStep);
        this.candleStep++;
        if (this.candles15m.size() < 2) return spotPrice24h;
        int prevMom = 0;
        for (int i = this.candleStep; i < CANDLE_15M_SIZE; i++) {
            prevMom += this.candles15m.get(this.candles15m.size()-2).getTickJuju(i);
        }
        this.momentum15m = prevMom + this.candleLive.getMomentum();
        //System.out.print(", " + this.momentum15m);
        return spotPrice24h;
    }

    private void updateGraphLimits() {
        this.graphHi = Math.ceil((this.hi+500)/500)*500;
        this.graphLo = Math.floor((this.lo-500)/500)*500;
        int roundedHi, roundedLo, roundedMid;
        roundedHi = (int) this.graphHi;
        roundedLo = (int) this.graphLo;
        roundedMid = (int) (this.graphHi+this.graphLo)/2;
        this.graphHiDisplay.setText(Integer.toString(roundedHi));
        this.graphLoDisplay.setText(Integer.toString(roundedLo));
        this.graphMidDisplay.setText(Integer.toString(roundedMid));
    }

    private void drawTickCandle(TickPoint tick) {
        //System.out.print("{ HI-" + this.candleLive.getHi() + " LO-" + this.candleLive.getLo());
        if (tick == null) return;
        if (this.candleLive == null) return;
        drawLiveCandle();
    }

    private void drawLiveCandle() {
        double x = this.candles15m.size()*this.GRAPH_CANDLE_WIDTH;
        double rangeY = this.graphHi-this.graphLo;
        double deltaY1 = this.graphHi-this.candleLive.getHi();
        double deltaPer1 = deltaY1/rangeY;
        double y = (this.GRAPH_HALF_HEIGHT*2)*deltaPer1;
        double w = this.GRAPH_CANDLE_WIDTH;
        double deltaY2 = this.graphHi-this.candleLive.getLo();
        double deltaPer2 = deltaY2/rangeY;
        double y2 = (this.GRAPH_HALF_HEIGHT*2)*deltaPer2;
        double h = y2-y;//Math.max(y-y2, 1);
        //System.out.print(" deltaY2-" + deltaY2 + "deltaPer2-" + deltaPer2 + " X-" + x + " Y-" + y + " W-" + w + " H-" + h + "}");
        GraphicsContext graph = this.graphCanvas.getGraphicsContext2D();
        graph.clearRect(x, y-1, GRAPH_CANDLE_WIDTH, h+1);
        graph.setStroke(Color.WHITE);
        graph.setLineWidth(1);
        graph.strokeRect(x, y, w, h);
    }

    private void updateHiLoWithCandles() {
        if (this.candles15m.size() < 1) return;
        boolean redraw = false;
        for (TickCandle candle : this.candles15m) {
            double prevLo, prevHi;
            prevLo = this.lo;
            prevHi = this.hi;
            this.lo = Math.min(candle.getLo(), this.lo);
            this.hi = Math.max(candle.getHi(), this.hi);
            if (prevLo > this.lo || prevHi < this.hi) redraw = true;
        }
        updateHiLoWithSpotPrice(redraw);
    }

    @Override
    public void stop() {
        if (this.ticker != null) this.ticker.stop();
        for (PositionCard posCard : this.positionCards) {
            posCard.chokePosition();
        }
        writePositions();
    }

    private void writePositions() {
        System.out.print("WRITE. ");
        ObjectMapper mapper = new ObjectMapper();
        if (this.positions == null) return;
        try {
            HashMap<String, HashMap<String, String>> posMap = new HashMap<String, HashMap<String, String>>();
            for (Position pos : positions) {
                posMap.put(pos.getId().toString(), pos.getMap());
            }
            //System.out.println(posList.toString());
            mapper.writeValue(new FileOutputStream(this.positionFile.getAbsolutePath()), posMap);
        } catch (Exception e) {
            return;
        }
    }

    public CoinbaseClient getClient() {
        return this.client;
    }

    private Stage stage;
    private Scene dashboardScene;
    private Scene editorScene;
    private Pane dashboardPane;
    private Canvas graphCanvas;
    private VBox positionBox;
    private ScrollPane positionScroller;
    private Pane positionEditor;
    private Text[] pairBalanceDisplay;
    private Text positionLabel;
    private Text spotPriceDisplay;
    private Text dayPriceDeltaDisplay;
    private Text momentumDisplay;
    private Text graphHiDisplay;
    private Text graphMidDisplay;
    private Text graphLoDisplay;
    private File profileDir;
    private File infoFile;
    private File positionFile;
    private TextField nameField;
    private TextField sizeField;
    private CheckBox explorationDriveCheckBox;
    private TextField entryPointField;
    private CheckBox improbabilityDriveCheckBox;
    private TextField improbabilityLimitField;
    private TextField dipLimitField;
    private TextField momReversalLimitField;
    private TextField peakLimitField;
    private TextField exitLimitField;
    private CheckBox recursiveDriveCheckBox;
    private TextField recursiveDriveLimitField;
    private TextField test1;
    private TextField test2;
    private Text test3;
    private Profile profile;
    private CoinbaseClient client;
    private WalletProducer walletProducer;
    private Ticker ticker;
    private TickCandle candleLive;
    private PositionLogger positionLogger;
    private Position selectedPosition;
    private JsonNode[] walletNodes;
    private String[] currencyPair;
    private DecimalFormat[] pairBalanceFormat;
    private ArrayList<Position> positions;
    private ArrayList<PositionCard> positionCards;
    private ArrayList<TickCandle> candles1m;
    private ArrayList<TickCandle> candles5m;
    private ArrayList<TickCandle> candles15m;
    private boolean choke = false;
    private boolean candleGo = false;
    private boolean sims;
    private boolean simsLock;
    private int momentum15m;
    private int candleStep;
    private double spotPrice;
    private double dayPriceDelta;
    private double lo;
    private double hi;
    private double graphLo;
    private double graphHi;

    private String secret;
    private String passphrase;
    private String key;

    public static final Paint PROVINCIAL_BLUE = Color.web("001F97");
    public static final Paint FEDERAL_RED = Color.web("FF0000");
    public static final Font ARIALB_TITLE = new Font("Arial Black", 20);
    public static final Font ARIALB_VALUE = new Font("Arial Black", 14);
    public static final Font ARIALB_LABEL = new Font("Arial Black", 10);
    public static final int PREFERRED_WIDTH = 500;
    public static final int PREFERRED_HEIGHT = 800;
    public static final int CELL_WIDTH = PREFERRED_WIDTH/50;
    public static final int CELL_HEIGHT = PREFERRED_HEIGHT/80;
    public static final int TICK_TIME = 1000; //1 second
    public static final int CANDLE_1M_TIME = 60000;
    public static final int CANDLE_5M_TIME = 300000;
    public static final int CANDLE_15M_TIME = 900000;
    public static final int CANDLE_15M_SIZE = 900;
    public static final int CANDLE_15M_MAX = 96;
    public static final double GRAPH_TOP = CELL_HEIGHT*13;
    public static final double GRAPH_LEFT = CELL_WIDTH*7;
    public static final double GRAPH_CANDLE_WIDTH = (PREFERRED_WIDTH/500)*4;
    public static final double GRAPH_HALF_HEIGHT = CELL_HEIGHT*10;
    public static final double SCROLLER_TOP = CELL_HEIGHT*50;
    public static final double SCROLLER_LEFT = CELL_WIDTH*2;
    public static final double SCROLLER_WIDTH = PREFERRED_WIDTH-(SCROLLER_LEFT*2);
    public static final double SCROLLER_HEIGHT = CELL_HEIGHT*28;
    public static final double POSITION_WIDTH = SCROLLER_WIDTH-CELL_WIDTH;
    public static final double POSITION_HEIGHT = SCROLLER_HEIGHT/4;
}
