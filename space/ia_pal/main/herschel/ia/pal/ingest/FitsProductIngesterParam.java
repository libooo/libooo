package herschel.ia.pal.ingest;

import java.io.File;

public class FitsProductIngesterParam implements ProductIngesterParam {
	private File dir;

	public File getDir() {
		return dir;
	}

	public void setDir(File dir) {
		this.dir = dir;
	}
	
	public FitsProductIngesterParam(File dir){
		this.dir=dir;
	}
}
