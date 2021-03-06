package kfs.kfsAsnDecode;

public class ASNException extends RuntimeException {

    private String type;

    public ASNException(String s) {
        super(s);
    }

    public ASNException(String t, String s) {
        super(s);
        type = t;
    }

    public ASNException(String t, String s, Throwable ex) {
        super(s, ex);
        type = t;
    }

    public boolean isType(String s) {
        boolean ret;
        if (s != null) {
            if (type == null) {
                ret = false;
            } else {
                ret = type.equals(s);
            }
        } else {
            ret = type == null;
        }
        return ret;
    }
}
