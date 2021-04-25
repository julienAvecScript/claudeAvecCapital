package com.github.therealjlb.claude;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Ticker implements Runnable {

    public Ticker(TickPoller poller) {
        this.poller = poller;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public Ticker(TickSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        System.out.println("PRODUCER RUN. ");
        if (this.poller == null) {
            System.out.println("SESSION START. ");
            this.session.start();
        } else {
            System.out.println("POLLER START. ");
            this.executor.scheduleAtFixedRate(this.poller, 1, 1, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (this.session != null) this.session.stop();
        if (this.executor != null) this.executor.shutdown();
    }

    private TickSession session;
    private TickPoller poller;
    private ScheduledExecutorService executor;
}
