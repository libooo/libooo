package com.okay.validate.srm;

//Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 2008-10-6 11:45:37
//Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
//Decompiler options: packimports(3) 
//Source File Name:   Srmjava.java

import java.awt.*;
import java.awt.event.*;

class InputPanel extends Panel
 implements ActionListener, ItemListener, AdjustmentListener
{

 InputPanel(Srmjava srmjava)
 {
     applet = srmjava;
     setBackground(Color.white);
     setLayout(new BorderLayout());
     title = new Label("SRMj - Statistical Region Merging in Java by F. Nielsen and R. Nock", 1);
     title.setFont(srmjava.helveticafont1);
     title.setBackground(Color.white);
     add("North", title);
     slider = new Scrollbar(0, 32, 10, 1, 1024);
     add("South", slider);
     slider.addAdjustmentListener(this);
     applyButton = new Button("Push me to Segment!");
     applyButton.addActionListener(this);
     add("Center", applyButton);
 }

 public void adjustmentValueChanged(AdjustmentEvent adjustmentevent)
 {
     applet.Q = slider.getValue();
     applet.imageCanvas.repaint();
 }

 public void actionPerformed(ActionEvent actionevent)
 {
     if(actionevent.getActionCommand().equals("Push me to Segment!"))
     {
         applyButton.setEnabled(false);
         restore();
     }
 }

 public void itemStateChanged(ItemEvent itemevent)
 {
 }

 public void restore()
 {
     applet.OneRound();
     applet.imageCanvas.repaint();
     applyButton.setEnabled(true);
 }

 Button applyButton;
 Label title;
 Scrollbar slider;
 Srmjava applet;
}