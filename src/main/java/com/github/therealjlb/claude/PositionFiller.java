package com.github.therealjlb.claude;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class PositionFiller implements Runnable {

    public PositionFiller(PositionCard positionCard, URL url) {
        this.positionCard = positionCard;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            //OAUTH ENCRYPTION
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
            HashMap<String, String> map = objectMapper.readValue(stream, typeRef);
            this.positionCard.updateLimit(map);
        } catch (Exception e) {
            return;
        }
    }

    private PositionCard positionCard;
    private URL url;
}
