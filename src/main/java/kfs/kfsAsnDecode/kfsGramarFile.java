package kfs.kfsAsnDecode;

import java.io.InputStream;

/**
 *
 * @author pavedrim
 */
public class kfsGramarFile {

    public final String filename;
    public final String []lines;
    
    public kfsGramarFile(String filename, String []lines) {
        this.filename = filename;
        this.lines = lines;
    }
    
    public kfsGramarFile(String filename) {
        this.filename = filename;
        this.lines = Util.toStringArray(filename);
    }

    public kfsGramarFile(String filename, InputStream inpuStream) {
        this.filename = filename;
        this.lines = Util.toStringArray(inpuStream);
    }
}
