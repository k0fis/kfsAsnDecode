package kfs.asn.utils;

public class ASNException extends RuntimeException {

    private final String type;

    public ASNException(String s) {
        super(s);
        type = null;
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
        if (type == null) {
            return s == null;
        }
        return type.equals(s);
    }
}
