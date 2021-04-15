package com.github.therealjlb.claude;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class TickProducer implements Runnable {

    public TickProducer(URL url, Dashboard dash) {
        this.url = url;
        this.dash = dash;
    }

    @Override
    public void run() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
            HashMap<String, String> map = objectMapper.readValue(stream, typeRef);
            this.dash.updateDashboard(map);
        } catch (Exception e) {
            return;
        }
    }

    private URL url;
    private Dashboard dash;
}
