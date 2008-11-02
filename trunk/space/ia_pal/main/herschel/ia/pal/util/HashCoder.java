// -*-java-*-
//
// File:      MD5.java
// Author:    Jaime Saiz Santos (jaime.saiz@sciops.esa.int)
// Generated: Feb 25, 2008
// Usage:     -
// Info:      -

package herschel.ia.pal.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.logging.Logger;

import herschel.ia.io.serial.SerialArchive;

/**
 * This class gives a hash code, in hexadecimal representation, for any object.
 * Current implementation uses MD5 algorithm.
 */
public class HashCoder {

    private static Logger LOG = Logger.getLogger(HashCoder.class.getName());

    private static class TrashOutputStream extends OutputStream {
	public void write(int b) throws IOException { /* does nothing! */ }
    }

    public String getHash(Object o) {
	ObjectOutputStream oos = null;
	try {
	    MessageDigest encoder = MessageDigest.getInstance("MD5");
	    TrashOutputStream tos = new TrashOutputStream();
	    DigestOutputStream dos = new DigestOutputStream(tos, encoder);
	    oos = SerialArchive.createOutputStream(dos);
	    oos.writeObject(o);
	    byte[] md5 = encoder.digest();
	    return toHexString(md5);
	} catch (Exception e) {
	    LOG.warning("Using hashCode for getting hash code of " + o);
	    return Integer.toHexString(o.hashCode());
	} finally {
	    if (oos != null) {
		try { oos.close(); } catch (IOException e) {}
	    }
	}
    }

    private String toHexString(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xff);
            if (hex.length() < 2) { hex = '0' + hex; }
            buffer.append(hex);
        }
        return buffer.toString();
    }
}
