/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.therealjlb.claude;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.Timestamp;

/**
 *
 * @author Jonathan
 */
public class TickPoint {
    
    public TickPoint(double price, int tick) {
        this.price = price;
        this.tick = tick;
    }

    public Circle newBead() {
        this.bead = new Circle();
        this.bead.setRadius(1.5);
        this.bead.setFill(Color.WHITE);
        this.bead.setStroke(Color.WHITE);
        this.bead.setOpacity(0.5);
        return getBead();
    }

    public void translateBead(long axisX, double price){
        if (this.bead == null) return;
        long deltaX = getMils()-axisX;
        if (deltaX > Dashboard.CANDLE_15M_TIME*Dashboard.CANDLE_15M_MAX) return;
        int roundedDiff = ((int) deltaX/Dashboard.CANDLE_15M_TIME)*Dashboard.CANDLE_15M_TIME;
        double beadX = (Dashboard.CELL_WIDTH*3)+(roundedDiff*3);
        bead.setTranslateX(beadX);
        double deltaY = getPrice()-price;
        double beadY = (Dashboard.CELL_HEIGHT*5)*deltaY;
        bead.setTranslateY(beadY);
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setJuju(int juju) {
        this.juju = juju;
    }
    
    public double getPrice() {
        return this.price;
    }
    
    public int getJuju() {
        return this.juju;
    }

    public long getMils() {
        return timestamp.getTime();
    }

    public Circle getBead() {
        return this.bead;
    }

    private Circle bead;
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private double price;
    private int tick;
    private int juju;
}