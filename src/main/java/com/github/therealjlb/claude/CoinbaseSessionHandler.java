package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import java.lang.reflect.Type;

public class CoinbaseSessionHandler implements StompSessionHandler {

    public CoinbaseSessionHandler(Dashboard dash, String productID) {
        System.out.println("HANDER CREATED. ");
        this.dash = dash;
        this.productID = productID;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("AFTER CONNECTED. ");
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "subscribe");
        ArrayNode productIDList = objectMapper.createArrayNode();
        productIDList.add(this.productID);
        node.set("product_ids", productIDList);
        ArrayNode channelList = objectMapper.createArrayNode();
        channelList.add("ticker");
        node.set("channels", channelList);
        String payload = node.toPrettyString();
        session.send(connectedHeaders, payload);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("HANDLE FRAME. ");
        ObjectMapper mapper = new ObjectMapper();
        try {
            //JsonNode node = mapper.readValue((String) payload, JsonNode.class);
            //System.out.println(node.toPrettyString());
            //this.dash.updateDashboard(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Dashboard dash;
    private String productID;
}
