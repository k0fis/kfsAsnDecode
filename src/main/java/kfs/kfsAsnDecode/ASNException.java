package kfs.kfsAsnDecode;

public class ASNException extends RuntimeException {

    String msg;
    String type;

    @Override
    public String getMessage() {
        return msg;
    }

    public ASNException(String s) {
        msg = s;
    }

    public ASNException(String t, String s) {
        msg = s;
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
            ret = type == null ? true : false;
        }
        return ret;
    }
}
