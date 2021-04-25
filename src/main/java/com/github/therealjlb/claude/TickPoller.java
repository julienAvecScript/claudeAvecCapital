package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.JsonNode;

public class TickPoller implements Runnable {

    public TickPoller(Dashboard dash, String productID) {
        this.dash = dash;
        this.productID = productID;
    }

    @Override
    public void run() {
        CoinbaseClient client = this.dash.getClient();
        JsonNode tick = client.getProductTicker(this.productID);
        if (tick.equals(this.lastTick)) return;
        this.lastTick = tick;
        this.dash.updateDashboard(tick);
    }

    private Dashboard dash;
    private String productID;
    private JsonNode lastTick;
}
