/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.therealjlb.claude;

import java.sql.Timestamp;
import java.util.HashMap;
import java.net.*;
import java.util.concurrent.*;

/**
 *
 * @author Jonathan
 */
public class Ticker {
           
    public Ticker(Dashboard dashboard, String productID) {
        this.dashboard = dashboard;
        this.productID = productID;
        try {
            this.url = new URL("https://api.pro.coinbase.com/products/" + this.productID + "/ticker");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("TICKER TASK GO.");
    }

    public void tick() {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        BlockingQueue<HashMap<String, String>> queue = new ArrayBlockingQueue<HashMap<String, String>>(1024);
        TickProducer tickProducer = new TickProducer(this.url, this.dashboard);
        executor.scheduleAtFixedRate(tickProducer, 1, 1, TimeUnit.SECONDS);
    }

    public long getStartMils() {
        return startTS.getTime();
    }

    public void die() {
        this.executor.shutdown();
    }

    private Timestamp startTS = new Timestamp(System.currentTimeMillis());
    private ScheduledExecutorService executor;
    private URL url;
    private String productID;
    private Dashboard dashboard;
}