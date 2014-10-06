package kfs.asn.utils;

import java.io.InputStream;

/**
 *
 * @author pavedrim
 */
public class GramarFile {

    public final String filename;
    public final String []lines;
    
    public GramarFile(String filename, String []lines) {
        this.filename = filename;
        this.lines = lines;
    }
    
    public GramarFile(String filename) {
        this.filename = filename;
        this.lines = AsnUtil.toStringArray(filename);
    }

    public GramarFile(String filename, InputStream inpuStream) {
        this.filename = filename;
        this.lines = AsnUtil.toStringArray(inpuStream);
    }
}
