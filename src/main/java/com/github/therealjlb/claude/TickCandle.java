/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.therealjlb.claude;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *
 * @author Jonathan
 */
public class TickCandle {
    
    public TickCandle(int duration) {
        this.duration = duration;
        this.tickPoints = new ArrayList<>();
        //this.logJSON = new JSONObject();
    }
    
    public void addTick(TickPoint point) {
        if (countTicks() == this.duration) return;
        this.tickPoints.add(point);
        double spotPrice = point.getPrice();
        if (this.lo == 0) this.lo = spotPrice;
        if (spotPrice > this.hi) this.hi = spotPrice;
        if (spotPrice < this.lo) this.lo = spotPrice;
        if (countTicks() == 1) this.open = spotPrice;
        if (countTicks() < 2) return;
        //Juju = has spot price inc (+1) or dec (-1) from previous price
        double prevTickPrice = this.tickPoints.get(countTicks()-2).getPrice();
        int juju = 0;
        if (spotPrice > prevTickPrice) juju = 1;
        else if (spotPrice < prevTickPrice) juju = -1;
        point.setJuju(juju);
        this.momentum += juju;
        if (countTicks() < this.duration) return;
        this.close = spotPrice;
    }
    
    public double getTickPrice(int i) {
        if (i >= this.tickPoints.size()) return 0;
        return this.tickPoints.get(i).getPrice();
    }
    
    public int getTickJuju(int i) {
        if (i >= this.tickPoints.size()) return 0;
        return this.tickPoints.get(i).getJuju();
    }
        
    public double getHi() {
        return this.hi;
    }
    
    public double getLo() {
        return this.lo;
    }

    public int getMomentum() {
        return this.momentum;
    }

    public ArrayList<TickPoint> getTickPoints() {
        return this.tickPoints;
    }
    
    public int countTicks() {
        return this.tickPoints.size();
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private ArrayList <TickPoint> tickPoints;
    private int duration; //in seconds, ex.: 60 (1m), 300 (5m), 900 (15m)
    private double hi = 0;
    private double lo = 0;
    private double open = 0;
    private double close = 0;
    private int momentum = 0;
    //private JSONObject logJSON;
}
