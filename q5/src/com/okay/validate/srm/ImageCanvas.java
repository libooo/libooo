package com.okay.validate.srm;

//Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 2008-10-6 11:45:05
//Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
//Decompiler options: packimports(3) 
//Source File Name:   Srmjava.java

import java.awt.*;

class ImageCanvas extends Canvas
{

 ImageCanvas(Srmjava srmjava)
 {
     applet = srmjava;
     setBackground(Color.white);
 }

 public void paint(Graphics g)
 {
     g.setColor(Color.red);
     int i = getSize().width / 2;
     int j = (int)((applet.aspectratio * (double)getSize().width) / 2D);
     double d;
     if(j < applet.appleth - 140)
         d = 1.0D;
     else
         d = (double)(applet.appleth - 140) / (double)j;
     i = (int)(d * (double)i);
     j = (int)(d * (double)j);
     g.drawImage(applet.img, 0, 0, i, j, this);
     g.drawImage(applet.imgseg, getSize().width / 2 + 10, 0, i, j, this);
     g.setFont(applet.helveticafont2);
     g.drawString((new StringBuilder()).append("Q=").append(applet.Q).toString(), 5, 50);
 }

 Srmjava applet;
}
