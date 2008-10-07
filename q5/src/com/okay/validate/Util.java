package com.okay.validate;

import java.io.File;

public class Util {

	public static void delete(File f){
		if(f.isDirectory()){
			File[] fs = f.listFiles();
			for(File ff: fs){
				delete(ff);
			}
		}else{
			f.delete();
		}
	}

}
