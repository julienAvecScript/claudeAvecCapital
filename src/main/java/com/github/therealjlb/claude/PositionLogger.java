package com.github.therealjlb.claude;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PositionLogger implements Runnable {

    public PositionLogger(File log, ArrayList<Position> prevPositions) {
        this.log = log;
        this.prevPositions = prevPositions;
    }

    @Override
    public void run() {
        if (this.positions == null) return;
        if (this.positions.equals(this.prevPositions)) return;
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new FileOutputStream(this.log.getAbsolutePath()), this.positions);
        } catch (Exception e) {
            return;
        }
        this.prevPositions = this.positions;
        this.positions = null;
    }

    public void updatePositions(ArrayList<Position> positions) {
        this.positions = positions;
    }

    private File log;
    private ArrayList<Position> positions;
    private ArrayList<Position> prevPositions;
}