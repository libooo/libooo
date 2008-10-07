package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.okay.validate.ImageDecoder;
import com.okay.validate.ImageFileFilter;
import com.okay.validate.Util;

public class ImageDecoderTest extends TestCase {
	
	public void setUp(){
		File temp=new File("tmp");
		System.out.println(temp.getAbsolutePath());
		Util.delete(temp);
		temp.mkdir();
	}

	public void testDecode() throws FileNotFoundException, IOException {
		File sampleDir = new File("base/sample");
		File[] ff = sampleDir.listFiles(new ImageFileFilter());
		for(File f: ff){
			String code = ImageDecoder.decode(f);
			System.out.println(f.getName()+" "+ code);
		}
		
	}

}
