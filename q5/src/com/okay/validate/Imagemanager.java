package com.okay.validate;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

class HSV
{
	double H;
	double S;
	double V;
}

class RGB
{
	int R;
	int G;
	int B;
}

public class Imagemanager {

	private Image Disp; // Image currently in display.
	private Image Undo; // Image before last operation.
	BufferedImage img1;
	public int W, H; // Width and height of the image.
	private String fileName; // Filename of original image.
	public int ipixels[]; // Array dump of image during reading
	private int[] grayValue1D; //
	private double[][] grayValue2D;
	private double[] ft128; // 128维特征向量
	private double[] ft8; // 8维特征向量
	private double[] fttotal; // 8维特征向量
	private double[] ft16; // 16维特征向量
	public int type = 1;
	int[][][] subpic;
	int size;
	final int COLORDIMENSION = 162;
	final int DIMENSION = 168;
	final double PARAM = 0.09;
	final double THRESHOLD = 0.0105;
	final double MAXDOUBLE = 99999999.0;
	
	//try to extract my feature
	private double[] ft168;

	/**
	 * Construct image file loader
	 * 
	 * @param fileName
	 *            file name 构造函数，处理fileName所对应的文件
	 */
	public void load(String fileName, int t) {
		this.fileName = fileName;
		type = t;
		ft128 = new double[128];
		// ImageIcon imgIcon = new ImageIcon( fileName );
		// Image img =imgIcon.getImage();
		// PlanarImage planar = JAI.create("fileload",fileName);
		// img1 = planar.getAsBufferedImage();
		img1 = ScaleChange(fileName);

		W = img1.getWidth(null);
		H = img1.getHeight(null);
		this.Disp = img1;

		initVal();
		
		ft168 = new double[168];
		ft168 = FeatureExtraction(this.fileName);
	}

	public BufferedImage ScaleChange(String filename) {
		BufferedImage im;
		PlanarImage planar = JAI.create("fileload", fileName);
		
		ParameterBlock pb = new ParameterBlock();

		im = planar.getAsBufferedImage();
		int w = im.getWidth();
		int h = im.getHeight();
		int s = w / 200;
		float sc;
		if (s > 1) {
			sc = 1 / (1f * s);
			// sc=1f;
		}
		// else if(s==1)
		// {
		// sc=0.5f;
		// }
		else {
			sc = 1f;
		}
		/*
		 * System.out.println("sc"); System.out.println(w);
		 * System.out.println(s); System.out.println(sc);
		 * System.out.println("end");
		 */
		pb.addSource(im);

		pb.add(sc);
		pb.add(sc);
		pb.add(0f);
		pb.add(0f);
		// System.out.println(sc);

		pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
		PlanarImage p2 = JAI.create("scale", pb);
		return p2.getAsBufferedImage();
	}

	public double[] Get128Feature() {
		return ft128;
	}
	
	
	public double[] Get8Feature() {
		return ft8;
	}

	public double[] Get16Feature() {
		return ft16;
	}

	public double[] GetTotalFeature() {
		return fttotal;
	}

	public int GetType() {
		return size;
	}

	
	public double[] Get168Feature(){
		return ft168;
	}
	/**
	 * 
	 * 构造函数，处理当前所显示的图像
	 */
	public void ImageManager() {
		// this.Disp = Touch.dispImage;
		// this.Undo = Touch.undoImage;
		// initVal();
	}

	/**
	 * 
	 * 构造函数，处理传入的image图像
	 */
	public void ImageManager(Image image) {
		this.Disp = image;
		initVal();
	}

	private	HSV RGBToHSV (RGB rgb)
	{
		final double ERR = 0.0000000001;
		double r, g, b;
		double MIN, MAX, delta;
		r = rgb.R/255.0;
		g = rgb.G/255.0;
		b = rgb.B/255.0;
		
		MIN = Math.min(Math.min(r, g), b);
		MAX = Math.max(Math.max(r, g), b);
		
		HSV hsv = new HSV();
		hsv.V = MAX;
		
		delta = MAX - MIN;
		if (MAX != 0)
		{
			hsv.S = delta/MAX;
		}
		else
		{
			hsv.S = 0;
			hsv.H = -1;
			return hsv;
			
		}
		if (Math.abs(r - MAX) < ERR )
		{
			
			hsv.H = (g-b)/delta;
		}
		
		else if (Math.abs(g - MAX) < ERR)
		{
			hsv.H = 2 + ( b - r ) / delta;
		}
		else
		{
			hsv.H = 4 + ( r - g ) / delta;
		}
		
		hsv.H *= 60;				// degrees
		if( hsv.H < 0 )
			hsv.H += 360;
		return hsv;
	}

private double[] AverageAndDiversity(double a[], int count)
{
	double[] UD = new double[2];
	double sum = 0.0;
	UD[0] = 0.0;
	UD[1] = 0.0;
	int i;
	for ( i = 0; i < count; i++)
	{
		sum += a[i];
	}
	UD[0] = sum/count;
	double temp = 0.0;
	for (i = 0; i < count; i++)
	{
		temp += (a[i] - UD[0]) * (a[i] - UD[0]);
	}
	UD[1] = Math.sqrt(temp/count);
	return UD;
}

private int RGBtoIndex(RGB rgb)
{
	int gray = (rgb.R + rgb.G + rgb.B)/3;
	int i;
	for (i = 0; i < 64; i++)
	{
		if (gray >= i*4 && gray < (i+1)*4)
			break;
	}
	return i;
} 

private int HSVtoIndex(HSV hsv)
{
	double h, s, v;
	h = hsv.H;
	s = hsv.S;
	//v = hsv.V;
	v = 0.0;
	int index = 0;

	//decide the section of H
	int i;
	for ( i = 0; i < 18; i++) //totally 18 values
	{
		if (h >= i * 20 && h < (i+1) * 20)
			break;
	}
	index += i * 9;
	for ( i = 0; i < 3; i++)
	{
		if (s >= i * 1/(double)3 && s < (i+1) * 1/(double)3)
			break;
	}
	index += i * 3;
	for ( i = 0; i < 3; i++)
	{
		if (v >= i * 1/(double)3 && v < (i+1) * 1/(double)3)
			break;
	}
	index += i;
	return index;

} 

private RGB GetRGB(BufferedImage image, int x, int y)
{
	RGB rgb = new RGB();
     
     if (image  !=   null   &&  x  <  image.getWidth()  &&  y  <  image.getHeight()) {
       
         int  pixel  =  image.getRGB(x,y);
        rgb.R  =  (pixel  &   0xff0000 )  >>   16 ;
        rgb.G  =  (pixel  &   0xff00 )  >>   8 ;
        rgb.B  =  (pixel  &   0xff );
    }
    
     return  rgb;
}
	
	private double[] FeatureExtraction(String ImgFile)
	{
		File file = new File(ImgFile);
		return FeatureExtraction(file);
	}

	private double[] FeatureExtraction(File ImgFile)
	{
		BufferedImage image;
		try       
		 {
			     image  =  ImageIO.read( ImgFile);
			    
		 }
		 catch  (IOException ex)
		{
			    ex.printStackTrace();
			    double[] tmp = new double[0];
				return tmp;
		} 
		 if (image == null)
		 {
			 
			 double[] tmp = new double[0];
				return tmp;
		 }
		long width = image.getWidth();
		long height = image.getHeight();
		
		double[] imageVec = new double[DIMENSION];

		int i;	
		for (i = 0; i < COLORDIMENSION; i++)		
		{
			imageVec[i] = 0.0;
		}
		double M10[][] = new double[64][64];
		double M01[][] = new double[64][64];
		double M1_1[][] = new double[64][64];
		double M11[][] = new double[64][64];
		for(i = 0; i < 64; i++)
		{
			for( int j = 0; j < 64; j++)
			{
				M10[i][j] = 0.0;
				M01[i][j] = 0.0;
				M1_1[i][j] = 0.0;
				M11[i][j] = 0.0;
			}
		}
		for(int y = 0 ; y < height ; ++y)
		{
			for(int x = 0 ; x < width ; ++x)
			{
				RGB rgb = GetRGB(image,x, y);
				HSV hsv = RGBToHSV(rgb);
				int index = HSVtoIndex(hsv);
				if (index < 0 || index > 161)
					continue;
				imageVec[index] += (1.0/height/width); 
				
				//try to build 4 matrix
				RGB rgb10, rgb01, rgb1_1, rgb11;
				if ( x+1 < width )
				{
					rgb10 = GetRGB(image, x+1, y);
					M10[RGBtoIndex(rgb)][RGBtoIndex(rgb10)] += 1.0;
				}
				if (y+1 < height)
				{
					rgb01 = GetRGB(image, x, y+1);
					M01[RGBtoIndex(rgb)][RGBtoIndex(rgb01)] += 1.0;
				}
				if (x+1 <width && y-1>=0)
				{

					rgb1_1 = GetRGB(image, x+1, y-1);
					M1_1[RGBtoIndex(rgb)][RGBtoIndex(rgb1_1)] += 1.0;
				}
				if (x+1 < width && y+1 < height)
				{
					rgb11 = GetRGB(image, x+1, y+1);
					M11[RGBtoIndex(rgb)][RGBtoIndex(rgb11)] += 1.0;
				}

			}
		}



		//double con10, con01, con1_1, con11, asm10, asm01, asm1_1, asm11, ent10, ent01, ent1_1, ent11, cor10, cor01, cor1_1, cor11;
		double con[] = new double[4];
		double asm1[] = new double[4];
		double ent[] = new double[4];
		for (i = 0; i < 4; i++)
		{
			con[i] = 0.0;
			asm1[i] = 0.0;
			ent[i] = 0.0;
		}
		for(i = 0; i < 64; i++)
		{
			for( int j = 0; j < 64; j++)
			{
				M10[i][j] = M10[i][j]/64/64;
				con[0] += (i-j)*(i-j)*M10[i][j];

				//	con[0] += (i-j)*(i-j)*M10[i][j]/64/64;
				asm1[0] += M10[i][j]*M10[i][j];
				if (M10[i][j] > 0.0)
				{
					ent[0] += (-1)*M10[i][j]*Math.log(M10[i][j])/64/64;
				}
				//
				M01[i][j] = M01[i][j]/64/64;
				con[1] += (i-j)*(i-j)*M01[i][j];
				//	con[1] += (i-j)*(i-j)*M01[i][j]/64/64;
				asm1[1] += M10[i][j]*M01[i][j];
				if (M01[i][j] > 0.0)
				{
					ent[1] += (-1)*M01[i][j]*Math.log(M01[i][j])/64/64;
				}


				M1_1[i][j] = M1_1[i][j]/64/64;
				con[2] += (i-j)*(i-j)*M1_1[i][j];
				//	con[2] += (i-j)*(i-j)*M1_1[i][j]/64/64;
				asm1[2] += M10[i][j]*M1_1[i][j];
				if (M1_1[i][j] > 0.0)
				{
					ent[2] += (-1)*M1_1[i][j]*Math.log(M1_1[i][j])/64/64;
				}


				M11[i][j] = M11[i][j]/64/64;
				con[3] += (i-j)*(i-j)*M11[i][j];
				//		con[3] += (i-j)*(i-j)*M11[i][j]/64/64;
				asm1[3] += M10[i][j]*M11[i][j];
				if (M11[i][j] > 0.0)
				{
					ent[3] += (-1)*M11[i][j]*Math.log(M11[i][j])/64/64;
				}
			}
		}
		double[] dVec = new double[DIMENSION];
		double UD[];
		UD = AverageAndDiversity(imageVec, COLORDIMENSION);
		UD[0] = 0.0;
		UD[1] = 1.0;
		for (i = 0; i < COLORDIMENSION; i++)
		{
			
			dVec[i] = (imageVec[i]-UD[0])/UD[1];
		}
		double textureVec[] = new double[6];

		UD = AverageAndDiversity(con, 4);
		textureVec[0] = UD[0];
		textureVec[1] = UD[1];

		UD = AverageAndDiversity(asm1, 4);
		textureVec[2] = UD[0];
		textureVec[3] = UD[1];

		UD = AverageAndDiversity(ent, 4);

		textureVec[4] = UD[0];
		textureVec[5] = UD[1];


		UD = AverageAndDiversity(textureVec, 6);

		int j;
		for (i = COLORDIMENSION, j = 0; i < DIMENSION; i++,j++)
		{
			
			dVec[i] = ((textureVec[j] - UD[0])/UD[1]);
		}

		return dVec;

	}

	public void initVal() {
		this.W = this.Disp.getWidth(null);
		this.H = this.Disp.getHeight(null);
		ipixels = new int[this.W * this.H];
		getGrayValue2D();
		// System.out.println("length ");
		// ft8=ComputFeature(grayValue2D,W,H,1);
		if (type == 1) {
			ft128 = Comput128Feature(grayValue2D, W, H);
		} else if (type == 2) {
			fttotal = ComputTotalFeature(grayValue2D, W, H);
		} else {
			ft128 = Comput128Feature(grayValue2D, W, H);
		}
		// ft8=Comput8Feature(grayValue2D,W,H,1);
		// fttotal =ComputTotalFeature(grayValue2D,W,H);

		// ft16=ComputByAllimage(grayValue2D,W,H);
		if (W < 150 && H < 150) {
			size = 0;
			// 
		} else {
			// 
			size = 1;
		}
		// System.out.println(ipixels.length);
		// System.out.println(grayValue2D.length);
		// startOp();
		// test1();
	}

	// Methods to be used by any image enhancement routines
	// This method is to be called before starting any enhancement routine
	public void startOp() {
		// swap Disp and Undo;
		Image t = Undo;
		Undo = Disp;
		Disp = t;

		// now the enhancement has to happen from
		// Undo->Disp
		try {
			PixelGrabber pg = new PixelGrabber(this.Undo, 0, 0, this.W, this.H,
					ipixels, 0, this.W);
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		;
	}

	// 返回从图像中读入的像素的一维数组
	public int[] getIpixels() {
		return ipixels;
	}

	// 返回与图像对应的整形灰度值的一维数组
	public int[] getGrayValue() {

		int grayValue[] = new int[W * H];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				int[] rgb = new int[3];
				int pixel = img1.getRGB(j, i);
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				grayValue[i * W + j] = (int) (Math.round((0.3 * rgb[0] + 0.59
						* rgb[1] + 0.11 * rgb[2])));
			}
		}
		return grayValue;
		/*
		 * startOp();
		 * 
		 * int grayValue[] = new int[ipixels.length]; for ( int i = 0; i <
		 * ipixels.length; i++ ){ int r = (ipixels[i] >> 16)& 0xff; int g =
		 * (ipixels[i] >> 8 )& 0xff; int b = (ipixels[i] )& 0xff; grayValue[i] = (
		 * int )(Math.round(( 0.3 * r + 0.59 * g + 0.11 * b ))); } //
		 * System.out.println("gray "); // System.out.println(grayValue[1020]);
		 * return grayValue;
		 */

	}

	// 返回图象对应的整形灰度值的二维数组
	public double[][] getGrayValue2D() {
		grayValue1D = getGrayValue();
		grayValue2D = ArrayTransform.array1D22D(grayValue1D, W, H);
		// System.out.println("2d Value 0 0 ");
		// System.out.println(grayValue2D.length);
		// for(int i=0;i<350;i++)
		// System.out.println(grayValue2D[0][i]);
		return grayValue2D;
	}

	// 测试函数
	public void test1() {
		int sh = H / 4;
		int sw = W / 4;
		double[][][] subpic = new double[16][sh][sw];
		double[] ft128 = new double[128];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				int sub_i = i / sh;
				int sub_j = j / sw;
				int d_i = i % sh;
				int d_j = j % sw;
				int k = sub_i * 4 + sub_j;
				if (k < 16) {
					subpic[k][d_i][d_j] = grayValue2D[i][j];
				}
			}
		}
		double[] ft = new double[8];
		ft = ComputFeature(subpic[0], sw, sh, 1);
		// for(int i=0;i<8;i++)
		// {
		// System.out.println(ft[i]);
		// }
	}

	public void test(double img1[][], double img2[][], int sh, int sw) {
		int label = 0;
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++) {
				// if((img1[i][j]-img2[i][j])!=0)
				// label=1;
				// System.out.println(img1[i][j]);

			}
		double[] ft = new double[8];
		ft = ComputFeature(img1, sw, sh, 1);
		// for (int i=0;i<8;i++)

		// {
		// System.out.println(ft[i]);
		// }

		// System.out.println("label ");
		// System.out.println(label);
	}

	// 通过图象计算16bin直方图
	public double[] ComputByAllimage(double Array[][], int Width, int Height) {
		double[] feature = new double[16]; // 8维特征向量
		double[] resultArray1D = new double[Width * Height];
		double[][] resultArray2D_1 = new double[Height][Width]; // 垂直方向
		double[][] resultArray2D_2 = new double[Height][Width]; // 水平方向
		double[][] Orientation2D = new double[Height][Width]; // 梯度方向
		double[][] Magnitude2D = new double[Height][Width]; // 梯度大小
		double[][] Distance2D = new double[Height][Width]; // 距离加劝

		// minThr = 100000;
		// maxThr = 0;
		double th1, th2;
		double t = 0.0;

		// 计算水平和垂直方向梯度
		// 计算梯度,采用sobel梯度算子 W_H = [ 1 2 1; 0 0 0; -1 -2 -1]; % the model in the
		// horizon direction
		// W_V = [1 0 -1; 2 0 -2; 1 0 -1]; % the model in the vetical direction
		double[][] W_H = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		double[][] W_V = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };

		// resultArray2D_1= ArrayTransform.Convolution(Array, W_H);
		// resultArray2D_2= ArrayTransform.Convolution(Array, W_V);
		resultArray2D_1 = ArrayTransform.Convolution(Array, W_H);
		resultArray2D_2 = ArrayTransform.Convolution(Array, W_V);
		// if(number==0)

		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				th1 = resultArray2D_1[i][j];
				th2 = resultArray2D_2[i][j];
				double X = 1.0 * th1;
				double Y = 1.0 * th2;
				if (X == 0 && Y == 0)
					Orientation2D[i][j] = -9.0;
				else
					Orientation2D[i][j] = Math.atan2(Y, X);

				Magnitude2D[i][j] = Math.sqrt((Y * Y + X * X));
				// if(i==1 && j==Width-5 )
				// {
				// System.out.println( Orientation2D[i][j]);
				// System.out.println(X);
				// System.out.println(Y);

				// }
				if ((i != 0) || (j != 0) || (i != Height - 1)
						|| (j != Width - 1))
					t = t + Magnitude2D[i][j];
			}
		}
		// System.out.print("print");
		// System.out.println(resultArray2D_1[0][0] );
		// 梯度大小加权
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Magnitude2D[i][j] = Magnitude2D[i][j] / t;
				// if(i==1)
				// System.out.println(Magnitude2D[i][j]);
			}
		}
		// System.out.println("dsf");
		// 计算像素点离子快中心的距离加权
		double cx = (Height + 1) / 2.0;
		double cy = (Width + 1) / 2.0;
		double totaldis = 0.0;
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				if (((i - cx) == 0) & ((j - cy) == 0))
					Distance2D[i][j] = 1 / 0.01;
				else
					Distance2D[i][j] = 1 / ((Math.sqrt((i - cx) * (i - cx)
							+ (j - cy) * (j - cy))));

				totaldis = totaldis + Distance2D[i][j];
				// Magnitude2D[i][j]=Magnitude2D[i][j]/t;
			}
		}
		// 距离加劝值归一化
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Distance2D[i][j] = Distance2D[i][j] / totaldis;

			}
		}
		// 计算16bin直方图
		double PI = 3.1415926;
		for (int k = 0; k < 16; k++) {
			feature[k] = 0.0;
		}

		for (int i = 1; i < Height - 1; i++) {
			for (int j = 1; j < Width - 1; j++) {
				for (int k = 1; k < 17; k++) {
					if (Orientation2D[i][j] >= (-PI + PI * (k - 1) / 8)
							&& Orientation2D[i][j] <= (-PI + PI * k / 8)) {
						// if((Distance2D[i][j])<0.0||(Orientation2D[i][j])<0.0)
						// System.out.println("little zeor ");
						feature[k - 1] = feature[k - 1]
								+ (0.05 * Distance2D[i][j] + 0.5 * Magnitude2D[i][j]);
					}
				}

			}
		}
		double s = 0.0;
		for (int i = 0; i < 16; i++) {
			s = s + feature[i];
		}
		s = s / 8.0;
		double vl = 0.0;
		// 去掉图象旋转变化
		for (int i = 0; i < 16; i++) {
			feature[i] = feature[i] - s;
			feature[i] = Math.abs(feature[i]);
			vl = vl + feature[i];
			// System.out.println( feature[i]);
		}
		// System.out.println("feature");
		for (int i = 0; i < 16; i++) {
			feature[i] = feature[i] / vl;
			// System.out.println(feature[i]);
		}

		return feature;

	}

	// 图像分块并计算其128维特征向量
	public double[] Comput128Feature(double imagevalue2D[][], int W, int H) {
		// 图像分成为4*4的子快
		int sh = H / 4;
		int sw = W / 4;
		double[][][] subpic = new double[16][sh][sw];
		double[] ft128 = new double[128];
		for (int i = 0; i < H; i++) {
			for (int j = 0; j < W; j++) {
				int sub_i = i / sh;
				int sub_j = j / sw;
				int d_i = i % sh;
				int d_j = j % sw;
				int v = sub_i * 4 + sub_j;
				if (v < 16) {
					subpic[v][d_i][d_j] = imagevalue2D[i][j];
				}
			}
		}

		// test(subpic[15],subpic[15], sh,sw);
		double total_ff = 0.0;
		for (int k = 0; k < 16; k++) {
			double[] ft = new double[8];
			int nw = subpic[k][0].length;
			int nh = subpic[k].length;
			// if(k==0)
			// {
			// System.out.println("sub size");
			// System.out.println(subpic[0].length);
			// System.out.println(subpic[0][0].length);
			// }
			ft = ComputFeature(subpic[k], nw, nh, k);
			// if(k==0)
			// {
			// for(int i=0;i<8;i++)
			// {
			// System.out.println(ft[i]);
			// }
			// System.out.println("feat");
			// }

			for (int i = 0; i < 8; i++) {
				ft128[8 * k + i] = ft[i];
				total_ff = total_ff + ft[i];
			}
		}

		// System.out.print("feature : ");

		// 向量归一化
		for (int i = 0; i < 128; i++) {
			ft128[i] = ft128[i] / total_ff;

			// System.out.println(ft128[i]);
			// System.out.println(" ");

		}
		// System.out.println("c");
		return ft128;

	}

	// 根据给定的图象块计算图象的8维特征向量值
	public double[] Comput8Feature(double Array[][], int Width, int Height,
			int number) {
		double[] feature = new double[8]; // 8维特征向量
		double[] resultArray1D = new double[Width * Height];
		double[][] resultArray2D_1 = new double[Height][Width]; // 垂直方向
		double[][] resultArray2D_2 = new double[Height][Width]; // 水平方向
		double[][] Orientation2D = new double[Height][Width]; // 梯度方向
		double[][] Magnitude2D = new double[Height][Width]; // 梯度大小
		double[][] Distance2D = new double[Height][Width]; // 距离加劝

		// minThr = 100000;
		// maxThr = 0;
		double th1, th2;
		double t = 0.0;

		// 计算水平和垂直方向梯度
		// 计算梯度,采用sobel梯度算子 W_H = [ 1 2 1; 0 0 0; -1 -2 -1]; % the model in the
		// horizon direction
		// W_V = [1 0 -1; 2 0 -2; 1 0 -1]; % the model in the vetical direction
		double[][] W_H = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		double[][] W_V = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
		resultArray2D_1 = ArrayTransform.Convolution(Array, W_H);
		// resultArray2D_1= ArrayTransform.Convolution(Array, W_H);
		// resultArray2D_2= ArrayTransform.Convolution(Array, W_V);
		// if(number==0)

		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				th1 = resultArray2D_1[i][j];
				th2 = resultArray2D_2[i][j];
				double X = 1.0 * th1;
				double Y = 1.0 * th2;
				if (X == 0 && Y == 0)
					Orientation2D[i][j] = -9.0;
				else
					Orientation2D[i][j] = Math.atan2(Y, X);

				Magnitude2D[i][j] = Math.sqrt((Y * Y + X * X));
				// if(i==1 && j==Width-5 )
				// {
				// System.out.println( Orientation2D[i][j]);
				// System.out.println(X);
				// System.out.println(Y);

				// }
				if ((i != 0) || (j != 0) || (i != Height - 1)
						|| (j != Width - 1))
					t = t + Magnitude2D[i][j];
			}
		}
		// System.out.print("print");
		// System.out.println(resultArray2D_1[0][0] );
		// 梯度大小加权
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Magnitude2D[i][j] = Magnitude2D[i][j] / t;
				// if(i==1)
				// System.out.println(Magnitude2D[i][j]);
			}
		}
		// System.out.println("dsf");
		// 计算像素点离子快中心的距离加权
		double cx = (Height + 1) / 2.0;
		double cy = (Width + 1) / 2.0;
		double totaldis = 0.0;
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				if (((i - cx) == 0) & ((j - cy) == 0))
					Distance2D[i][j] = 1 / 0.01;
				else
					Distance2D[i][j] = 1 / ((Math.sqrt((i - cx) * (i - cx)
							+ (j - cy) * (j - cy))));

				totaldis = totaldis + Distance2D[i][j];
				// Magnitude2D[i][j]=Magnitude2D[i][j]/t;
			}
		}
		// 距离加劝值归一化
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Distance2D[i][j] = Distance2D[i][j] / totaldis;

			}
		}
		// 计算8bin直方图
		double PI = 3.1415926;
		for (int k = 0; k < 8; k++) {
			feature[k] = 0.0;
		}

		for (int i = 1; i < Height - 1; i++) {
			for (int j = 1; j < Width - 1; j++) {
				for (int k = 1; k < 9; k++) {
					if (Orientation2D[i][j] >= (-PI + PI * (k - 1) / 4)
							&& Orientation2D[i][j] <= (-PI + PI * k / 4)) {
						// if((Distance2D[i][j])<0.0||(Orientation2D[i][j])<0.0)
						// System.out.println("little zeor ");
						feature[k - 1] = feature[k - 1]
								+ (0.05 * Distance2D[i][j] + 0.5 * Magnitude2D[i][j]);
					}
				}

			}
		}
		double s = 0.0;
		for (int i = 0; i < 8; i++) {
			s = s + feature[i];
		}
		s = s / 8.0;
		double vl = 0.0;
		for (int i = 0; i < 8; i++) {
			feature[i] = Math.abs(feature[i] - s);
			vl = vl + feature[i];
			// System.out.println( feature[i]);
		}
		for (int i = 0; i < 8; i++) {
			feature[i] = feature[i] / vl;
		}

		return feature;

	}

	// 根据给定的图象块计算图象的8维特征向量值
	public double[] ComputFeature(double Array[][], int Width, int Height,
			int number) {
		double[] feature = new double[8]; // 8维特征向量
		double[] resultArray1D = new double[Width * Height];
		double[][] resultArray2D_1 = new double[Height][Width]; // 垂直方向
		double[][] resultArray2D_2 = new double[Height][Width]; // 水平方向
		double[][] Orientation2D = new double[Height][Width]; // 梯度方向
		double[][] Magnitude2D = new double[Height][Width]; // 梯度大小
		double[][] Distance2D = new double[Height][Width]; // 距离加劝

		// minThr = 100000;
		// maxThr = 0;
		double th1, th2;
		double t = 0.0;

		// 计算水平和垂直方向梯度
		// 计算梯度,采用sobel梯度算子 W_H = [ 1 2 1; 0 0 0; -1 -2 -1]; % the model in the
		// horizon direction
		// W_V = [1 0 -1; 2 0 -2; 1 0 -1]; % the model in the vetical direction
		double[][] W_H = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		double[][] W_V = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };

		resultArray2D_1 = ArrayTransform.Convolution(Array, W_H);
		resultArray2D_2 = ArrayTransform.Convolution(Array,W_V);
		// if(number==0)

		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				th1 = resultArray2D_1[i][j];
				th2 = resultArray2D_2[i][j];
				double X = 1.0 * th1;
				double Y = 1.0 * th2;
				if (X == 0 && Y == 0)
					Orientation2D[i][j] = -9.0;
				else
					Orientation2D[i][j] = Math.atan2(Y, X);

				Magnitude2D[i][j] = Math.sqrt((Y * Y + X * X));
				// if(i==1 && j==Width-5 )
				// {
				// System.out.println( Orientation2D[i][j]);
				// System.out.println(X);
				// System.out.println(Y);

				// }
				if ((i != 0) || (j != 0) || (i != Height - 1)
						|| (j != Width - 1))
					t = t + Magnitude2D[i][j];
			}
		}
		// System.out.print("print");
		// System.out.println(resultArray2D_1[0][0] );
		// 梯度大小加权
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Magnitude2D[i][j] = Magnitude2D[i][j] / t;
				// if(i==1)
				// System.out.println(Magnitude2D[i][j]);
			}
		}
		// System.out.println("dsf");
		// 计算像素点离子快中心的距离加权
		double cx = (Height + 1) / 2.0;
		double cy = (Width + 1) / 2.0;
		double totaldis = 0.0;
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				if (((i - cx) == 0) & ((j - cy) == 0))
					Distance2D[i][j] = 1 / 0.01;
				else
					Distance2D[i][j] = 1 / ((Math.sqrt((i - cx) * (i - cx)
							+ (j - cy) * (j - cy))));

				totaldis = totaldis + Distance2D[i][j];
				// Magnitude2D[i][j]=Magnitude2D[i][j]/t;
			}
		}
		// 距离加劝值归一化
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Distance2D[i][j] = Distance2D[i][j] / totaldis;

			}
		}
		// 计算8bin直方图
		double PI = 3.1415926;
		for (int k = 0; k < 8; k++) {
			feature[k] = 0.0;
		}

		for (int i = 1; i < Height - 1; i++) {
			for (int j = 1; j < Width - 1; j++) {
				for (int k = 1; k < 9; k++) {
					if (Orientation2D[i][j] >= (-PI + PI * (k - 1) / 4)
							&& Orientation2D[i][j] <= (-PI + PI * k / 4)) {
						// if((Distance2D[i][j])<0.0||(Orientation2D[i][j])<0.0)
						// System.out.println("little zeor ");
						feature[k - 1] = feature[k - 1]
								+ (0.05 * Distance2D[i][j] + 0.5 * Magnitude2D[i][j]);
					}
				}

			}
		}
		double s = 0.0;
		for (int i = 0; i < 8; i++) {
			s = s + feature[i];
		}
		for (int i = 0; i < 8; i++) {
			feature[i] = feature[i] / s;
			// System.out.println( feature[i]);
		}
		return feature;

	}

	public double[] ComputTotalFeature(double Array[][], int Width, int Height) {
		double[] feature = new double[8]; // 8维特征向量
		double[] resultArray1D = new double[Width * Height];
		double[][] resultArray2D_1 = new double[Height][Width]; // 垂直方向
		double[][] resultArray2D_2 = new double[Height][Width]; // 水平方向
		double[][] resultArray2D_3 = new double[Height][Width]; // 45度方向
		double[][] resultArray2D_4 = new double[Height][Width]; // 135度方向

		double[][] Orientation2D = new double[Height][Width]; // 梯度方向
		double[][] Magnitude2D = new double[Height][Width]; // 梯度大小
		double[][] Distance2D = new double[Height][Width]; // 距离加劝

		// minThr = 100000;
		// maxThr = 0;
		double th1, th2;
		double t = 0.0;

		// 计算水平和垂直方向梯度
		// 计算梯度,采用sobel梯度算子 W_H = [ 1 2 1; 0 0 0; -1 -2 -1]; % the model in the
		// horizon direction
		// W_V = [1 0 -1; 2 0 -2; 1 0 -1]; % the model in the vetical direction
		double[][] W_H = { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		double[][] W_V = { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
		double[][] W_C1 = { { 0, 0, 2 }, { 0, 0, 0 }, { -2, 0, 0 } };
		double[][] W_C2 = { { 2, 0, 0 }, { 0, 0, 0 }, { 0, 0, -2 } };
		resultArray2D_1 = ArrayTransform.Convolution(Array, W_H);
		resultArray2D_2 = ArrayTransform.Convolution(Array, W_V);
		resultArray2D_3 = ArrayTransform.Convolution(Array, W_C1);
		resultArray2D_4 = ArrayTransform.Convolution(Array, W_C2);

		// if(number==0)

		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				th1 = resultArray2D_1[i][j];
				th2 = resultArray2D_2[i][j];
				double X = 1.0 * th1;
				double Y = 1.0 * th2;
				if (X == 0 && Y == 0)
					Orientation2D[i][j] = -9.0;
				else
					Orientation2D[i][j] = Math.atan2(Y, X);

				Magnitude2D[i][j] = Math.sqrt((Y * Y + X * X));
				// if(i==1 && j==Width-5 )
				// {
				// System.out.println( Orientation2D[i][j]);
				// System.out.println(X);
				// System.out.println(Y);

				// }
				if ((i != 0) || (j != 0) || (i != Height - 1)
						|| (j != Width - 1))
					t = t + Magnitude2D[i][j];
			}
		}
		// System.out.print("print");
		// System.out.println(resultArray2D_1[0][0] );
		// 梯度大小加权
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Magnitude2D[i][j] = Magnitude2D[i][j] / t;
				// if(i==1)
				// System.out.println(Magnitude2D[i][j]);
			}
		}
		// System.out.println("dsf");
		// 计算像素点离子快中心的距离加权
		double cx = (Height + 1) / 2.0;
		double cy = (Width + 1) / 2.0;
		double totaldis = 0.0;
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				if (((i - cx) == 0) & ((j - cy) == 0))
					Distance2D[i][j] = 1 / 0.01;
				else
					Distance2D[i][j] = 1 / ((Math.sqrt((i - cx) * (i - cx)
							+ (j - cy) * (j - cy))));

				totaldis = totaldis + Distance2D[i][j];
				// Magnitude2D[i][j]=Magnitude2D[i][j]/t;
			}
		}
		// 距离加劝值归一化
		for (int i = 0; i < Height; i++) {
			for (int j = 0; j < Width; j++) {

				Distance2D[i][j] = Distance2D[i][j] / totaldis;

			}
		}
		// 计算8bin直方图
		double PI = 3.1415926;
		for (int k = 0; k < 8; k++) {
			feature[k] = 0.0;
		}

		for (int i = 1; i < Height - 1; i++) {
			for (int j = 1; j < Width - 1; j++) {
				// for(int k=1;k<9;k++)
				// {
				// if ( Orientation2D[i][j]>=(-PI+PI*(k-1)/4) &&
				// Orientation2D[i][j]<=(-PI+PI*k/4) )
				// {
				// if((Distance2D[i][j])<0.0||(Orientation2D[i][j])<0.0)
				// System.out.println("little zeor ");
				// feature[k-1]= feature[k-1]+(0.05*Distance2D[i][j]+0.5*
				// Magnitude2D[i][j]);
				// }
				// }
				if (resultArray2D_1[i][j] > 0)
					feature[0] = feature[0]
							+ (0.05 * Distance2D[i][j] + 0.5 * resultArray2D_1[i][j]);
				else if (resultArray2D_1[i][j] < 0) {
					feature[1] = feature[1]
							+ (0.05 * Distance2D[i][j] - 0.5 * resultArray2D_1[i][j]);
				}
				if (resultArray2D_2[i][j] > 0)
					feature[2] = feature[2]
							+ (0.05 * Distance2D[i][j] + 0.5 * resultArray2D_2[i][j]);
				else if (resultArray2D_2[i][j] < 0) {
					feature[3] = feature[3]
							+ (0.05 * Distance2D[i][j] - 0.5 * resultArray2D_2[i][j]);
				}
				if (resultArray2D_3[i][j] > 0)
					feature[4] = feature[4]
							+ (0.05 * Distance2D[i][j] + 0.5 * resultArray2D_3[i][j]);
				else if (resultArray2D_2[i][j] < 0) {
					feature[5] = feature[5]
							+ (0.05 * Distance2D[i][j] - 0.5 * resultArray2D_3[i][j]);
				}
				if (resultArray2D_4[i][j] > 0)
					feature[6] = feature[6]
							+ (0.05 * Distance2D[i][j] + 0.5 * resultArray2D_4[i][j]);
				else if (resultArray2D_2[i][j] < 0) {
					feature[7] = feature[7]
							+ (0.05 * Distance2D[i][j] - 0.5 * resultArray2D_4[i][j]);
				}

			}
		}
		double s = 0.0;
		for (int i = 0; i < 8; i++) {
			s = s + feature[i];
		}
		s = s / 8.0;
		for (int i = 0; i < 8; i++) {
			feature[i] = (feature[i] - s);

			// System.out.println( feature[i]);
		}
		s = 0.0;
		for (int i = 0; i < 8; i++) {
			s = s + Math.abs(feature[i]);
		}
		// System.out.println("s");
		// System.out.println(s);
		for (int i = 0; i < 8; i++) {

			feature[i] = Math.abs((feature[i]) / s);
			// System.out.println(feature[i]);
		}
		double ll = 0.0;
		for (int i = 0; i < 8; i++) {
			ll = ll + feature[i];
		}
		// System.out.println("ll");
		// System.out.println(ll);
		return feature;

	}

	// 返回与图像对应的双精度灰度值的一维数组
	public double[] getGrayValue(String db) {
		if (db == "db") {
			double grayValue[] = new double[ipixels.length];
			for (int i = 0; i < ipixels.length; i++) {
				int r = (ipixels[i] >> 16) & 0xff;
				int g = (ipixels[i] >> 8) & 0xff;
				int b = (ipixels[i]) & 0xff;
				grayValue[i] = 0.2990 * r + 0.5870 * g + 0.1140 * b;
			}
			return grayValue;
		}
		return null;
	}

	// 返回图像中相应单色的二维维数组：R=2;G=1;B=0;ALPHA=3
	public int[][] getColor2DArray(int[] sourceArray, int w, int h, int color) {
		int index = 0;
		int[][] array = new int[w][h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				array[i][j] = (sourceArray[index] >> color * 8) & 0xff;
				index++;
			}
		}
		return array;
	}

	// 返回图像中相应单色的一维数组：R=2;G=1;B=0;ALPHA=3
	public int[] getColorArray(int[] sourceArray, int w, int h, int color) {
		int[] array = new int[w * h];
		for (int i = 0; i < sourceArray.length; i++) {
			array[i] = (sourceArray[i] >> color * 8) & 0xff;
		}
		return array;
	}

}
