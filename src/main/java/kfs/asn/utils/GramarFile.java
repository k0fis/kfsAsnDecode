package kfs.asn.utils;

import java.io.InputStream;

/**
 *
 * @author pavedrim
 */
public class GramarFile {

    public final String []lines;
    
    public GramarFile(String []lines) {
        this.lines = lines;
    }
    
    public GramarFile(String filename) {
        this.lines = AsnUtil.toStringArray(filename);
    }

    public GramarFile(InputStream inpuStream) {
        this.lines = AsnUtil.toStringArray(inpuStream);
    }
}
