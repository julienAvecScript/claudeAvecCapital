package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.JsonNode;

public class WalletProducer implements Runnable {

    public WalletProducer(Dashboard dash) {
        this.dash = dash;
        System.out.println("WALLET PRODUCER. ");
    }

    @Override
    public void run() {
        System.out.println("WALLET PRODUCE. ");
        CoinbaseClient client = this.dash.getClient();
        if (client == null) return;
        JsonNode[] accounts = client.getAccounts();
        this.dash.updateWallet(accounts);
    }

    public void setGo(boolean go) {
        this.go = go;
    }

    private boolean go = false;
    private Dashboard dash;
}
