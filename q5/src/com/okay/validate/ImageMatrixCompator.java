package com.okay.validate;

import javax.swing.*;
import java.io.*; //import javax.media.jai.*;
import java.awt.*;
import java.awt.color.*;

//import com.sun.media.jai.codec.*;
//import javax.media.jai.operator.*;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.PixelGrabber;
import java.awt.Color; //import java.awt.Image.Raster;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

public class ImageMatrixCompator {

	String _fileName = "";
	String fileName = "";

	final int COLORDIMENSION = 162;
	final int DIMENSION = 168;
	final double PARAM = 0.09;
	final double THRESHOLD = 0.0119;
	final double MAXDOUBLE = 99999999.0;
	int type = 1;
	double threa = 0.05;

	public boolean SearchImage(String str1, String str2, int t) {
		_fileName = str1;
		fileName = str2;
		type = t;
		return ReadPixel(type);
	}

	public double VectorDistance(double v1[], double v2[], double param) {
		double distance = 0.0;
		double colorDis = 0.0, textureDis = 0.0;
		for (int i = 0; i < COLORDIMENSION; i++) {
			colorDis += (v1[i] - v2[i]) * (v1[i] - v2[i]);
		}
		colorDis /= COLORDIMENSION;
		colorDis = Math.sqrt(colorDis);

		// System.out.println("color distance:" + colorDis);

		for (int i = COLORDIMENSION; i < v1.length; i++) {

			textureDis += (v1[i] - v2[i]) * (v1[i] - v2[i]);
		}
		textureDis /= (v1.length - COLORDIMENSION);
		textureDis = Math.sqrt(textureDis);
		// System.out.println("texture distance:" + textureDis);
		distance = colorDis * (1 - param) + textureDis * param;
		return distance;
	}

	public double ComputDis(double f1[], double f2[]) {
		double dis = 0.0;
		double dif[] = new double[128];
		int k = f1.length;
		// System.out.println("asf");
		// System.out.println(k);

		if (k >= 128) {
			for (int i = 0; i < 128; i++) {
				if ((i > 40 & i < 57) || (i > 72 & i < 89)) {
					dis = dis + 5 * (f1[i] - f2[i]) * (f1[i] - f2[i]);

				} else
					dis = dis + (f1[i] - f2[i]) * (f1[i] - f2[i]);
				// System.out.println(i);
				// System.out.println(f1[i]);
				// System.out.println(f2[i]);
				// dis =dis +dif[i]*dif[i];
			}
			dis = Math.sqrt(dis);
			return dis;
		} else {
			for (int i = 0; i < k; i++) {
				dis = dis + (f1[i] - f2[i]) * (f1[i] - f2[i]);

			}
		}
		dis = Math.sqrt(dis);
		return dis;

	}

	public boolean MyCompare(double[] source, double[] dest) {
		if (VectorDistance(source, dest, PARAM) <= THRESHOLD)
			return true;
		else
			return false;

	}

	public boolean compareFeature(double[] source, double[] dest, int type) {
		double k;
		if (type == 1) {
			k = ComputDis(source, dest);
		} else if (type == 2) {
			k = ComputDis(source, dest);
		} else {
			k = ComputDis(source, dest);
		}
		if (type == 1) {
			if (k <= threa) {
				// System.out.println("The image is the same scenery");
				return true;
			} else {
				// System.out.println("The image is not the same scenery");
				return false;
			}
		} else if (type == 2) {
			k = ComputDis(source, dest);
			return false;
		} else {
			return false;
		}
		// System.out.println(ff[127]);
		// b.getGrayValue();
		// b.getGrayValue2D();
		// b.getGrayValue();
	}

	public boolean ReadPixel(int type) {

		Imagemanager a = new Imagemanager();
		a.load(_fileName, type);
		Imagemanager b = new Imagemanager();
		b.load(fileName, type);
		double[] f1 = new double[128];
		double[] f2 = new double[128];
		// f1=a.Get128Feature();
		// f2=b.Get128Feature();
		// f1=a.GetTotalFeature();
		// double []f1 = new double[8];
		// f1=a.Get8Feature();
		// double []f2 = new double [8];
		// f2=b.GetTotalFeature();
		// double []f2 = new double[8];

		// System.out.println(f1[1]);
		return compareFeature(a.Get128Feature(), b.Get128Feature(), type);

	}

	public static void main(String[] args) throws FileNotFoundException {
		String fileName1 = "base/imgs/J_1.jpg";
//		String fileName1 = "tmp/1223368829140.jpg";
		String fileName2 = "";
		File baseDir = new File("base/imgs");
		File ff[] = baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".jpg")) {
					return true;
				} else {
					return false;
				}
			}

		});
		
		double min=1;
		String bestGuess="";
		for (File f : ff) {
			fileName2 = f.getAbsolutePath();
			Imagemanager file1 = new Imagemanager();
			Imagemanager file2 = new Imagemanager();
			try {
				file1.load(fileName1, 1);
				file2.load(fileName2, 1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ImageMatrixCompator compator = new ImageMatrixCompator();
			double dis=compator.ComputDis(file1.Get128Feature(), file2
					.Get128Feature());
			if(dis!=0 && dis<min){
				min=dis;
				bestGuess=fileName2;
			}
			System.out.println(fileName1
					+ " "
					+ fileName2
					+ ": "
					+ dis);
		}
		System.out.println("min: "+min +" bestGuess:"+ bestGuess);
	}

}
