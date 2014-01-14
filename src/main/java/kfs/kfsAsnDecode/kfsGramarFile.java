package kfs.kfsAsnDecode;

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
}
