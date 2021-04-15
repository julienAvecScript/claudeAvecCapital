package com.github.therealjlb.claude;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.Alert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;

public class CoinbaseClient {

    public CoinbaseClient(String key, String passphrase, String secret) {
        this.key = key;
        this.passphrase = passphrase;
        this.secret = secret;
    }

    public JsonNode[] getAccounts() {
        String time = getTime();
        try {
            String dir = "/accounts/";
            URL url = new URL(this.endpoint + dir);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("CB-ACCESS-KEY", this.key);
            String signature = sign(dir, "", time, "GET");
            if (signature == null) return null;
            connection.setRequestProperty("CB-ACCESS-SIGN", signature);
            connection.setRequestProperty("CB-ACCESS-TIMESTAMP", time);
            connection.setRequestProperty("CB-ACCESS-PASSPHRASE", this.passphrase);
            connection.setRequestProperty("Content-Type", "application/json");
            InputStream stream = connection.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode[] accounts = objectMapper.readValue(stream, JsonNode[].class);

            System.out.println("PRODUCTS::::");
            JsonNode[] products = getProducts();
            for (JsonNode product : products) {
                System.out.println(product.toPrettyString());
            }
            return accounts;
        } catch (Exception e) {
            return null;
        }
    }

    public JsonNode submitOrder(String side, String price, String size) {
        String time = getTime();
        price = "61000";
        size = "0.0003";
        try {
            String dir = "/orders/";
            URL url = new URL(this.endpoint + dir);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("CB-ACCESS-KEY", this.key);
            ObjectMapper objectMapper = new ObjectMapper();
            /*ObjectNode body = objectMapper.createObjectNode();
            body.put("size", size);
            body.put("price", price);
            body.put("side", side);
            body.put("product_id", this.product);
            String bodyString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAS(body);
            String cleanString = bodyString.substring(1, bodyString.length()-1);*/
            String cleanString = "\"size\":\"" + size + "\",\"price\":\"" + price + "\",\"side\":\"" + side + "\",\"product_id\":\"" + this.product + "\"";
            System.out.println(cleanString);
            String signature = sign(dir, cleanString, time, "POST");
            if (signature == null) return null;
            connection.setRequestProperty("CB-ACCESS-SIGN", signature);
            connection.setRequestProperty("CB-ACCESS-TIMESTAMP", time);
            connection.setRequestProperty("CB-ACCESS-PASSPHRASE", this.passphrase);
            connection.setRequestProperty("Content-Type", "application/json");
            InputStream stream = connection.getInputStream();
            String result = stream.toString();
            System.out.println(result);
            JsonNode order = objectMapper.readValue(stream, JsonNode.class);
            return order;
        } catch (Exception e) {
            System.out.println("????????? " + e.getMessage() + " !!!!!!!!!!!");
            return null;
        }
    }

    public JsonNode getOrder(String orderID) {
        String time = getTime();
        try {
            String dir = "/orders/" + orderID;
            URL url = new URL(this.endpoint + dir);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("CB-ACCESS-KEY", this.key);
            String signature = sign(dir, "", time, "GET");
            if (signature == null) return null;
            connection.setRequestProperty("CB-ACCESS-SIGN", signature);
            connection.setRequestProperty("CB-ACCESS-TIMESTAMP", time);
            connection.setRequestProperty("CB-ACCESS-PASSPHRASE", this.passphrase);
            connection.setRequestProperty("Content-Type", "application/json");
            InputStream stream = connection.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode order = objectMapper.readValue(stream, JsonNode.class);
            return order;
        } catch (Exception e) {
            return null;
        }
    }

    private String getTime() {
        try {
            String dir = "/time/";
            URL url = new URL(this.endpoint + dir);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            InputStream stream = con.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readValue(stream, JsonNode.class);
            String time = node.get("epoch").asText();
            return time;
        } catch (Exception e) {
            return null;
        }
    }

    private JsonNode[] getProducts() {
        try {
            String dir = "/products/";
            URL url = new URL(this.endpoint + dir);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            InputStream stream = con.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode[] nodes = mapper.readValue(stream, JsonNode[].class);
            return nodes;
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String requestPath, String body, String timestamp, String method) {
        String nakedSignature = timestamp + method + requestPath + body;
        try {
            Charset asciiCs = Charset.forName("US-ASCII");
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            byte[] decodedSecret = Base64.getDecoder().decode(this.secret);
            SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(decodedSecret, "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] macData = sha256HMAC.doFinal(asciiCs.encode(nakedSignature).array());
            String hashedString = Base64.getEncoder().encodeToString(macData);
            return hashedString;
        } catch (Exception e) {
            return null;
        }
    }

    private String key;
    private String passphrase;
    private String secret;
    private String endpoint = "https://api.pro.coinbase.com";
    private String product = "BTC-USDC";
}
