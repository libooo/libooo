package com.okay.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.okay.validate.srm.copy.BaseImageReader;
import com.okay.validate.srm.copy.Srmjava;

public class ImageDecoder {

	final static String tmpDir = "tmp";
	
	public static String decode(File img) throws FileNotFoundException, IOException{
		StringBuffer code =new StringBuffer();
		
		// Do SRM
		Srmjava srm = new Srmjava();
		String processedImg=(new File(tmpDir, img.getName())).getAbsolutePath();
		int q=300;
		srm.convert(img.getAbsolutePath(), processedImg, q);
		
		// Split
		ImageData[] data= UnCodebase.split(processedImg, 0);
		int index=0;
		for(ImageData d : data){
			String c="#";
			try{
			 c= BaseImageReader.getBestGuess(d);
			}catch(Exception e){
				e.printStackTrace();
			}
			code.append(c);
		}
		return code.toString();
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File img = new File("D://11.jpg");
		System.out.println(" code: "+ decode(img));
	}
}
