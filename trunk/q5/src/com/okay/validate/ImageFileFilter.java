package com.okay.validate;

import java.io.File;
import java.io.FileFilter;

public class ImageFileFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.getName().endsWith("jpg"))
			return true;
		else
			return false;
	}

}
