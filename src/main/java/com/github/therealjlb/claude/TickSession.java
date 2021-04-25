/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.therealjlb.claude;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.sql.Timestamp;
import java.net.*;
import java.util.List;

/**
 *
 * @author Jonathan
 */
public class TickSession {
           
    public TickSession(Dashboard dashboard, String productID) {
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

    public void start() {
        System.out.println("SESSION START. ");
        this.startTS = new Timestamp(System.currentTimeMillis());
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        SockJsClient sockJsClient = new SockJsClient(List.of(new WebSocketTransport(webSocketClient)));
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        String socketURL = "wss://ws-feed.pro.coinbase.com";
        try {
            this.session = stompClient.connect(socketURL, new TickSessionHandler(this.dashboard, this.productID)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.session.disconnect();
    }

    private Timestamp startTS;
    private StompSession session;
    private URL url;
    private String productID;
    private Dashboard dashboard;
}