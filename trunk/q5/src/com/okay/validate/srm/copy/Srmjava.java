package com.okay.validate.srm.copy;

//Decompiled by DJ v3.9.9.91 Copyright 2005 Atanas Neshkov  Date: 2008-10-6 11:16:44
//Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
//Decompiler options: packimports(3) 
//Source File Name:   Srmjava.java

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Srmjava {

	
	public static void main(String[] args) {
		Srmjava srm = new Srmjava();
		try {
//			String oriImg="D://11.jpg";
			String oriImg="D://elong1.jpg";
			String processedImg="D://elong2.jpg";
			int q = 200;
			srm.convert(oriImg,processedImg, q);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Srmjava() {
	}

	public void convert(String oriImg, String processedImg, int q) throws IOException {
		// filenameimg = getParameter("img");
//		filenameimg = "D://11.jpg";
		filenameimg = oriImg;
		System.out.println((new StringBuilder()).append("IMG TAG=").append(
				filenameimg).toString());

		// img = getImage(getDocumentBase(), filenameimg);

		img = ImageIO.read(new File(filenameimg));
//		Q = 200D;
		Q=q;
		g = 256D;
		borderthickness = 0;
		pg = new PixelGrabber(img, 0, 0, -1, -1, true);
		try {
			pg.grabPixels();
		} catch (InterruptedException interruptedexception) {
		}
		raster = (int[]) (int[]) pg.getPixels();
		w = pg.getWidth();
		h = pg.getHeight();
		aspectratio = (double) h / (double) w;
		n = w * h;
		logdelta = 2D * Math.log(6D * (double) n);
		smallregion = (int) (0.001D * (double) n);
		OneRound(processedImg);
		

	}

	void OneRound(String processedImg) throws IOException {
		UF = new UnionFind(n);
		Ravg = new double[n];
		Gavg = new double[n];
		Bavg = new double[n];
		N = new int[n];
		C = new int[n];
		rastero = new int[n];
		InitializeSegmentation();
		FullSegmentation();
		MemoryImageSource memoryimagesource = new MemoryImageSource(pg
				.getWidth(), pg.getHeight(), rastero, 0, pg.getWidth());
		Image _image=Toolkit.getDefaultToolkit().createImage(memoryimagesource);
		BufferedImage _bi = new BufferedImage(pg
				.getWidth(), pg.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) _bi.getGraphics();
		g2.drawImage(_image, 0, 0, null);
//		ImageIO.write(_bi, "JPEG", new File("d://2.jpg"));
		ImageIO.write(_bi, "JPEG", new File(processedImg));
		// imgseg = createImage(memoryimagesource);
	}

	void InitializeSegmentation() {
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				int j1 = j * w + i;
				int k = raster[j * w + i] & 0xff;
				int l = (raster[j * w + i] & 0xff00) >> 8;
				int i1 = (raster[j * w + i] & 0xff0000) >> 16;
				Ravg[j1] = k;
				Gavg[j1] = l;
				Bavg[j1] = i1;
				N[j1] = 1;
				C[j1] = j1;
			}

		}

	}

	void FullSegmentation() {
		Segmentation();
		MergeSmallRegion();
		OutputSegmentation();
		DrawBorder();
	}

	double min(double d, double d1) {
		if (d < d1)
			return d;
		else
			return d1;
	}

	double max(double d, double d1) {
		if (d > d1)
			return d;
		else
			return d1;
	}

	double max3(double d, double d1, double d2) {
		return max(d, max(d1, d2));
	}

	boolean MergePredicate(int i, int j) {
		double d = Ravg[i] - Ravg[j];
		d *= d;
		double d1 = Gavg[i] - Gavg[j];
		d1 *= d1;
		double d2 = Bavg[i] - Bavg[j];
		d2 *= d2;
		double d3 = min(g, N[i]) * Math.log(1.0D + (double) N[i]);
		double d4 = min(g, N[j]) * Math.log(1.0D + (double) N[j]);
		double d5 = ((g * g) / (2D * Q * (double) N[i])) * (d3 + logdelta);
		double d6 = ((g * g) / (2D * Q * (double) N[j])) * (d4 + logdelta);
		double d7 = d5 + d6;
		return d < d7 && d1 < d7 && d2 < d7;
	}

	Rmpair[] BucketSort(Rmpair armpair[], int i) {
		int ai[] = new int[256];
		int ai1[] = new int[256];
		Rmpair armpair1[] = new Rmpair[i];
		for (int j = 0; j < 256; j++)
			ai[j] = 0;

		for (int k = 0; k < i; k++)
			ai[armpair[k].diff]++;

		ai1[0] = 0;
		for (int l = 1; l < 256; l++)
			ai1[l] = ai1[l - 1] + ai[l - 1];

		for (int i1 = 0; i1 < i; i1++)
			armpair1[ai1[armpair[i1].diff]++] = armpair[i1];

		return armpair1;
	}

	void MergeRegions(int i, int j) {
		int k = UF.UnionRoot(i, j);
		int l = N[i] + N[j];
		double d = ((double) N[i] * Ravg[i] + (double) N[j] * Ravg[j])
				/ (double) l;
		double d1 = ((double) N[i] * Gavg[i] + (double) N[j] * Gavg[j])
				/ (double) l;
		double d2 = ((double) N[i] * Bavg[i] + (double) N[j] * Bavg[j])
				/ (double) l;
		N[k] = l;
		Ravg[k] = d;
		Gavg[k] = d1;
		Bavg[k] = d2;
	}

	void Segmentation() {
		int l2 = 0;
		int k2 = 2 * (w - 1) * (h - 1) + (h - 1) + (w - 1);
		Rmpair armpair[] = new Rmpair[k2];
		System.out.println((new StringBuilder()).append(
				"Building the initial image RAG (").append(k2)
				.append(" edges)").toString());
		for (int i = 0; i < h - 1; i++) {
			for (int l = 0; l < w - 1; l++) {
				int j1 = i * w + l;
				armpair[l2] = new Rmpair();
				armpair[l2].r1 = j1;
				armpair[l2].r2 = j1 + 1;
				int k3 = raster[j1] & 0xff;
				int j4 = (raster[j1] & 0xff00) >> 8;
				int i5 = (raster[j1] & 0xff0000) >> 16;
				int l5 = raster[j1 + 1] & 0xff;
				int k6 = (raster[j1 + 1] & 0xff00) >> 8;
				int j7 = (raster[j1 + 1] & 0xff0000) >> 16;
				armpair[l2].diff = (int) max3(Math.abs(l5 - k3), Math.abs(k6
						- j4), Math.abs(j7 - i5));
				l2++;
				armpair[l2] = new Rmpair();
				armpair[l2].r1 = j1;
				armpair[l2].r2 = j1 + w;
				l5 = raster[j1 + w] & 0xff;
				k6 = (raster[j1 + w] & 0xff00) >> 8;
				j7 = (raster[j1 + w] & 0xff0000) >> 16;
				armpair[l2].diff = (int) max3(Math.abs(l5 - k3), Math.abs(k6
						- j4), Math.abs(j7 - i5));
				l2++;
			}

		}

		for (int j = 0; j < h - 1; j++) {
			int k1 = (j * w + w) - 1;
			armpair[l2] = new Rmpair();
			armpair[l2].r1 = k1;
			armpair[l2].r2 = k1 + w;
			int l3 = raster[k1] & 0xff;
			int k4 = (raster[k1] & 0xff00) >> 8;
			int j5 = (raster[k1] & 0xff0000) >> 16;
			int i6 = raster[k1 + w] & 0xff;
			int l6 = (raster[k1 + w] & 0xff00) >> 8;
			int k7 = (raster[k1 + w] & 0xff0000) >> 16;
			armpair[l2].diff = (int) max3(Math.abs(i6 - l3), Math.abs(l6 - k4),
					Math.abs(k7 - j5));
			l2++;
		}

		for (int i1 = 0; i1 < w - 1; i1++) {
			int l1 = (h - 1) * w + i1;
			armpair[l2] = new Rmpair();
			armpair[l2].r1 = l1;
			armpair[l2].r2 = l1 + 1;
			int i4 = raster[l1] & 0xff;
			int l4 = (raster[l1] & 0xff00) >> 8;
			int k5 = (raster[l1] & 0xff0000) >> 16;
			int j6 = raster[l1 + 1] & 0xff;
			int i7 = (raster[l1 + 1] & 0xff00) >> 8;
			int l7 = (raster[l1 + 1] & 0xff0000) >> 16;
			armpair[l2].diff = (int) max3(Math.abs(j6 - i4), Math.abs(i7 - l4),
					Math.abs(l7 - k5));
			l2++;
		}

		System.out.println((new StringBuilder()).append("Sorting all ").append(
				l2).append(" edges using BucketSort").toString());
		armpair = BucketSort(armpair, k2);
		System.out.println("Testing the merging predicate in a single loop");
		for (int k = 0; k < k2; k++) {
			int i2 = armpair[k].r1;
			int i3 = UF.Find(i2);
			int j2 = armpair[k].r2;
			int j3 = UF.Find(j2);
			if (i3 != j3 && MergePredicate(i3, j3))
				MergeRegions(i3, j3);
		}

	}

	void OutputSegmentation() {
		boolean flag = false;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int k = i * w + j;
				int l = UF.Find(k);
				int i1 = (int) Ravg[l];
				int j1 = (int) Gavg[l];
				int k1 = (int) Bavg[l];
				int l1 = 0xff000000 | k1 << 16 | j1 << 8 | i1;
				rastero[k] = l1;
			}

		}

	}

	void MergeSmallRegion() {
		boolean flag = false;
		for (int i = 0; i < h; i++) {
			for (int j = 1; j < w; j++) {
				int i1 = i * w + j;
				int k = UF.Find(i1);
				int l = UF.Find(i1 - 1);
				if (l != k && (N[l] < smallregion || N[k] < smallregion))
					MergeRegions(k, l);
			}

		}

	}

	void DrawBorder() {
		for (int i = 1; i < h; i++) {
			label0: for (int j = 1; j < w; j++) {
				int k1 = i * w + j;
				int i1 = UF.Find(k1);
				int j1 = UF.Find(k1 - 1 - w);
				if (j1 == i1)
					continue;
				int k = -borderthickness;
				do {
					if (k > borderthickness)
						continue label0;
					for (int l = -borderthickness; l <= borderthickness; l++) {
						int l1 = (i + k) * w + (j + l);
						if (l1 >= 0 && l1 < w * h)
							rastero[l1] = -1;
					}

					k++;
				} while (true);
			}

		}

	}

	Image img;
	PixelGrabber pg;
	Image imgseg;
	String filenameimg;

	int appletw;
	int appleth;
	Font helveticafont1;
	Font helveticafont2;
	int raster[];
	int rastero[];
	int w;
	int h;
	int n;
	double aspectratio;
	double Q;
	UnionFind UF;
	double g;
	double logdelta;
	int N[];
	double Ravg[];
	double Gavg[];
	double Bavg[];
	int C[];
	int smallregion;
	int borderthickness;
	TextArea messageArea;
	Label title;
}