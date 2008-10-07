package com.okay.validate;

public class ArrayTransform{
    public ArrayTransform(){}
    
    public static int[] array2D21D(int[][] original,int W,int H) {
        int[] array=new int[H*W];
        int index=0;
        for(int i=0;i<H;i++) {
            for(int j=0;j<W;j++) {
                array[index]=original[i][j];
                index++;
            }
        }
        return array;
    }
    
    public static boolean [] array2D21D(boolean [][] original,int W,int H) {
        boolean [] array=new boolean [H*W];
        int index=0;
        for(int i=0;i<W;i++) {
            for(int j=0;j<H;j++) {
                array[index]=original[i][j];
                index++;
            }
        }
        return array;
    }
    
    public static double [][] array1D22D(int[] original,int W,int H) {
        double [][] array=new double [H][W];
        int index=0;
        for(int i=0;i<H;i++) {
            for(int j=0;j<W;j++) {
                array[i][j]=(double) original[index];
                index++;
            }
        }
        return array;
    }
    
    public static  double [][]Convolution(double IM[][],double
    		 Temple[][])
    		    {
    		    	int IW,IH;
    				int TW,TH;
    				IH= IM.length;
    				IW=IM[0].length;
    				TH=Temple.length;
    				TW=Temple[0].length;
    				int H=IH+TH-1;
    				int W=IW+TW-1;
    				double result[][]=new double [H-2][W-2];
    				for(int i=0;i<H;i++)
    					for(int j=0;j<W;j++)
    					{
    						{
    							 double s=0.0;
    							 for(int k=0;k<=i;k++)
    								 for(int l=0;l<=j;l++)
    								 {
    									 {
    										 if( (k<IH )&& (l<IW))
    										 if(((i-k)<TH)&&((j-l)<TW))
    											 s=s+IM[k][l]*Temple[i-k][j-l];
    									 }
    								 }
    							 if(i>0&&i<H-1 && j>0&& j<W-1)
    							 {
    								 result[i-1][j-1]= s;
    								 if((i==1) &&(j==1))
    								 {
							// System.out.println("s");
							// System.out.println(s);
    								 }
    							 }
    							 
    						}
    					}
    				return result;
    		    }

    /*
	 * public static double [][]Convolution(double IM[][],double Temple[][]) {
	 * int IW,IH; int TW,TH; IH= IM.length; IW=IM[0].length; TH=Temple.length;
	 * TW=Temple[0].length; int H=IH+TH-1; int W=IW+TW-1; double result[][]=new
	 * double [H-2][W-2]; for(int i=0;i<H;i++) for(int j=0;j<W;j++) { { double
	 * s=0.0; for(int k=0;k<=i;k++) for(int l=0;l<=j;l++) { { if( (k<IH )&&
	 * (l<IW)) if(((i-k)<TH)&&((j-l)<TW)) s=s+IM[k][l]*Temple[i-k][j-l]; } }
	 * if(i>0&&i<H-1 && j>0&& j<W-1) { result[i-1][j-1]= s; }
	 *  } } return result; }
	 */
}
