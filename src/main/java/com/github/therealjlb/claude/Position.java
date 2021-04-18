package com.github.therealjlb.claude;

import javafx.scene.canvas.Canvas;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Position {

    public Position(HashMap<String, String> map) {
        this.sims = Boolean.parseBoolean(map.get("sims"));
        this.id = UUID.fromString(map.get("id"));
        this.startTS = Timestamp.valueOf(map.get("startTS"));
        this.endTS = Timestamp.valueOf(map.get("endTS"));
        this.status = Integer.parseInt(map.get("status"));
        this.name = map.get("name");
        this.size = Double.parseDouble(map.get("size"));
        this.sizeBTC = Double.parseDouble(map.get("sizeBTC"));
        this.explorationDrive = Boolean.parseBoolean(map.get("explorationDrive"));
        this.entryPoiht = Double.parseDouble(map.get("entryPoint"));
        this.improbabilityDrive = Boolean.parseBoolean(map.get("improbabilityDrive"));
        this.improbabilityLimit = Double.parseDouble(map.get("improbabilityLimit"));
        this.dipLimit = Double.parseDouble(map.get("dipLimit"));
        this.momReversalLimit = Double.parseDouble(map.get("momReversalLimit"));
        this.peakLimit = Double.parseDouble(map.get("peakLimit"));
        this.exitLimit = Double.parseDouble(map.get("exitLimit"));
        this.exitPoint = Double.parseDouble(map.get("exitPoint"));
        this.recursiveDrive = Boolean.parseBoolean(map.get("recursiveDrive"));
        this.recursionLimit = Integer.parseInt(map.get("recursionLimit"));
        this.recursions = Integer.parseInt(map.get("recursions"));
        this.choke = Boolean.parseBoolean(map.get("choke"));
        this.turn = Integer.parseInt(map.get("turn"));
    }

    public Position(boolean sims) {
        this.sims = sims;
        this.id = UUID.randomUUID();
        this.startTS = new Timestamp(System.currentTimeMillis());
        this.endTS = new Timestamp(System.currentTimeMillis());
        this.status = 9;
        this.name = "";
        this.size = 0;
        this.sizeBTC = 0;
        this.explorationDrive = false;
        this.entryPoiht = 0;
        this.improbabilityDrive = false;
        this.improbabilityLimit = 0;
        this.dipLimit = 0;
        this.momReversalLimit = 0;
        this.peakLimit = 0;
        this.exitLimit = 0;
        this.exitPoint = 0;
        this.recursiveDrive = false;
        this.recursionLimit = 0;
        this.recursions = 0;
        this.choke = true;
        this.turn = 0;
    }

    public Position(Position position) {
        this.sims = position.isSims();
        this.startTS = new Timestamp(System.currentTimeMillis());
        this.status = 0;
        this.name = position.getName();
        this.size = position.getSize();
        this.sizeBTC = 0;
        this.explorationDrive = position.isExplorationDrive();
        this.entryPoiht = position.getClosePoint();
        this.improbabilityDrive = position.isImprobabilityDrive();
        this.improbabilityLimit = position.getImprobabilityLimit();
        this.dipLimit = position.getDipLimit();
        this.momReversalLimit = position.getMomReversalLimit();
        this.peakLimit = position.getPeakLimit();
        this.exitLimit = position.getExitLimit();
        this.recursiveDrive = position.isRecursiveDrive();
        this.recursionLimit = position.getRecursionLimit();
        if (position.parentID == null) {
            this.parentID = position.getId();
        } else {
            this.parentID = position.parentID;
            this.bigSiblingID = position.getId();
            position.setLittleSibling(this.id);
        }
        this.choke = false;
    }

    public boolean isSims() {
        return sims;
    }

    public void setSims(boolean sims) {
        this.sims = sims;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuyOrderID() {
        return buyOrderID;
    }

    public void setBuyOrderID(UUID buyOrderID) {
        this.buyOrderID = buyOrderID;
    }

    public UUID getSellOrderID() {
        return sellOrderID;
    }

    public void setSellOrderID(UUID sellOrderID) {
        this.sellOrderID = sellOrderID;
    }

    public UUID getParentID() {
        return parentID;
    }

    public void setParentID(UUID parentID) {
        this.parentID = parentID;
    }

    public UUID getBigSibling() {
        return bigSiblingID;
    }

    public void setLittleSibling(UUID littleSiblingID) {
        this.littleSiblingID = littleSiblingID;
    }

    public Timestamp getStartTS() {
        return startTS;
    }

    public void setStartTS(Timestamp startTS) {
        this.startTS = startTS;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getSizeBTC() {
        return sizeBTC;
    }

    public void setSizeBTC(double sizeBTC) {
        this.sizeBTC = sizeBTC;
    }

    public boolean isExplorationDrive() {
        return explorationDrive;
    }

    public void setExplorationDrive(boolean explorationDrive) {
        this.explorationDrive = explorationDrive;
    }

    public double getEntryPoiht() {
        return entryPoiht;
    }

    public void setEntryPoiht(double entryPoiht) {
        this.entryPoiht = entryPoiht;
    }

    public boolean isImprobabilityDrive() {
        return improbabilityDrive;
    }

    public void setImprobabilityDrive(boolean improbabilityDrive) {
        this.improbabilityDrive = improbabilityDrive;
    }

    public double getImprobabilityLimit() {
        return improbabilityLimit;
    }

    public void setImprobabilityLimit(double improbabilityLimit) {
        this.improbabilityLimit = improbabilityLimit;
    }

    public double getDipLimit() {
        return dipLimit;
    }

    public void setDipLimit(double dipLimit) {
        this.dipLimit = dipLimit;
    }

    public double getMomReversalLimit() {
        return momReversalLimit;
    }

    public void setMomReversalLimit(double momReversalLimit) {
        this.momReversalLimit = momReversalLimit;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getPeakLimit() {
        return peakLimit;
    }

    public void setPeakLimit(double peakLimit) {
        this.peakLimit = peakLimit;
    }

    public double getExitLimit() {
        return exitLimit;
    }

    public void setExitLimit(double exitLimit) {
        this.exitLimit = exitLimit;
    }

    public double getExitPoint() {
        return exitPoint;
    }

    public void setExitPoint(double exitPoint) {
        this.exitPoint = exitPoint;
    }

    public double getClosePoint() {
        return closePoint;
    }

    public void setClosePoint(double closePoint) {
        this.closePoint = closePoint;
    }

    public boolean isRecursiveDrive() {
        return recursiveDrive;
    }

    public void setRecursiveDrive(boolean recursiveDrive) {
        this.recursiveDrive = recursiveDrive;
    }

    public int getRecursionLimit() {
        return recursionLimit;
    }

    public void setRecursionLimit(int recursionLimit) {
        this.recursionLimit = recursionLimit;
    }

    public int getRecursions() {
        return recursions;
    }

    public void setRecursions(int recursions) {
        this.recursions = recursions;
    }

    public boolean isChoke() {
        return this.status == 7;
    }

    public void setChoke(boolean choke) {
        if (this.status == 8) return;
        if (choke) {
            this.status = 7;
        } else {
            this.status = this.turnStatuses[this.turn];
        }
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void setEndTS() {
        this.endTS = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getEndTS() {
        return endTS;
    }

    public HashMap<String, String> getMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("sims", String.valueOf(this.sims));
        map.put("id", String.valueOf(this.id));
        map.put("buyOrderID", String.valueOf(this.buyOrderID));
        map.put("sellOrderID", String.valueOf(this.sellOrderID));
        map.put("parentID", String.valueOf(this.parentID));
        map.put("bigSiblingID", String.valueOf(this.bigSiblingID));
        map.put("littleSiblingID", String.valueOf(this.littleSiblingID));
        map.put("startTS", String.valueOf(this.startTS));
        map.put("endTS", String.valueOf(this.endTS));
        map.put("status", String.valueOf(this.status));
        map.put("name", this.name);
        map.put("size", String.valueOf(this.size));
        map.put("sizeBTC", String.valueOf(this.sizeBTC));
        map.put("explorationDrive", String.valueOf(this.explorationDrive));
        map.put("entryPoint", String.valueOf(this.entryPoiht));
        map.put("improbabilityDrive", String.valueOf(this.improbabilityDrive));
        map.put("improbabilityLimit", String.valueOf(this.improbabilityLimit));
        map.put("dipLimit", String.valueOf(this.dipLimit));
        map.put("momReversalLimit", String.valueOf(this.momReversalLimit));
        map.put("buyPrice", String.valueOf(this.buyPrice));
        map.put("peakLimit", String.valueOf(this.peakLimit));
        map.put("exitLimit", String.valueOf(this.exitLimit));
        map.put("exitPoint", String.valueOf(this.exitPoint));
        map.put("closePoint", String.valueOf(this.closePoint));
        map.put("recursiveDrive", String.valueOf(this.recursiveDrive));
        map.put("recursionLimit", String.valueOf(this.recursionLimit));
        map.put("recursions", String.valueOf(this.recursions));
        map.put("choke", String.valueOf(this.choke));
        map.put("turn", String.valueOf(this.turn));
        return map;
    }

    private boolean sims;
    private UUID id;
    private UUID buyOrderID;
    private UUID sellOrderID;
    private UUID parentID;
    private UUID bigSiblingID;
    private UUID littleSiblingID;
    private Timestamp startTS;
    private Timestamp endTS;
    private int status;
    private String name;
    private double size;
    private double sizeBTC;
    private boolean explorationDrive;
    private double entryPoiht;
    private boolean improbabilityDrive;
    private double improbabilityLimit;
    private double dipLimit;
    private double momReversalLimit;
    private double buyPrice;
    private double peakLimit;
    private double exitLimit;
    private double exitPoint;
    private double closePoint;
    private boolean recursiveDrive;
    private int recursionLimit;
    private int recursions;
    private boolean choke;
    private int turn;
    private int[] turnStatuses = {
            9,
            0,
            3,
            5
    };
}
