package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;

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

    public JsonNode postStopBuy(String price, String size) {
        String side = "buy";
        String stop = "entry";
        JsonNode bodyNode = buildBody(size, price, side, stop);
        JsonNode result = buildPOST(bodyNode);
        return result;
    }

    public JsonNode postStopSell(String price, String size) {
        String side = "sell";
        String stop = "loss";
        JsonNode bodyNode = buildBody(size, price, side, stop);
        JsonNode result = buildPOST(bodyNode);
        return result;
    }

    private JsonNode buildBody(String size, String price, String side, String stop) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put("size", size);
        node.put("price", price);
        node.put("side", side);
        node.put("product_id", this.product);
        node.put("stop", stop);
        node.put("stop_limit", price);
        return node;
    }

    private JsonNode buildPOST(JsonNode bodyNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        String time = getTime();
        String dir = "/orders/";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("accept", "application/json");
            headers.add("content-type", "application/json");
            headers.add("User-Agent", "Claude");
            headers.add("CB-ACCESS-KEY", this.key);
            String bodyString = objectMapper.writeValueAsString(bodyNode);
            //String bodyString = "{\"size\":\"" + size + "\",\"price\":\"" + price + "\",\"side\":\"" + side + "\",\"product_id\":\"" + this.product + "\"}";
            String signature = sign(dir, bodyString, time, "POST");
            headers.add("CB-ACCESS-SIGN", signature);
            headers.add("CB-ACCESS-TIMESTAMP", time);
            headers.add("CB-ACCESS-PASSPHRASE", this.passphrase);
            HttpEntity<String> httpEnt = new HttpEntity<>(bodyString, headers);
            RestTemplate template = new RestTemplate();
            ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>() {};
            ResponseEntity<String> response = template.exchange(this.endpoint + dir, HttpMethod.POST, httpEnt, typeRef);
            System.out.println(response.getBody());
            JsonNode order = objectMapper.readValue(response.getBody(), JsonNode.class);
            return order;
        } catch (Exception e) {
            e.printStackTrace();
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
