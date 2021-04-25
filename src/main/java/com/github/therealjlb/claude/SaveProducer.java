package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class SaveProducer implements Runnable {

    public SaveProducer(ArrayList<Position> positions, File positionFile) {
        this.positions = positions;
        this.positionFile = positionFile;
    }

    @Override
    public void run() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap<String, HashMap<String, String>> posMap = new HashMap<String, HashMap<String, String>>();
            for (Position pos : positions) {
                posMap.put(pos.getId().toString(), pos.getMap());
            }
            mapper.writeValue(new FileOutputStream(this.positionFile.getAbsolutePath()), posMap);
        } catch (Exception e) {
            return;
        }
    }

    private ArrayList<Position> positions;
    private File positionFile;
}
