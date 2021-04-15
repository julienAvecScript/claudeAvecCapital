/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.therealjlb.claude;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;

/**
 *
 * @author Jonathan
 */
public class MonGraph extends JComponent {
    
    public MonGraph(Dashboard dashboard) {
        Dimension dimension = new Dimension(310, 155);
        setPreferredSize(dimension);
        setOpaque(true);
        setBackground(Color.white);
        setVisible(true);
        System.out.println("GRAPH GO: " + Double.toString(getLocation().getX()));
    }
    
    protected void paintChildren(Graphics g) {
        super.paintComponent(g);
    }
    
}
