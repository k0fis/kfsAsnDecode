package kfs.kfsAsnDecode;

/**
 * Models a primitive type data.
 *
 * class Node 'has a' PrimitiveClass where it stores the data if the Node corresponds to a Primitive
 * ASNClass.
 *
 */
public class PrimitiveClass {

    public int intValue;
    public double doubleValue;
    public byte[] byteArray;
    public String stringValue;
    public boolean boolValue;
    public int type; // One of the below types
    final static int UNKNOWN = 0;
    final static int INTEGER = 1;
    final static int DOUBLE = 2;
    final static int BYTEARRAY = 3;
    final static int STRING = 4;
    final static int BOOLEAN = 5;

    PrimitiveClass(int n) {
        intValue = n;
        type = INTEGER;
    }

    PrimitiveClass(double d) {
        doubleValue = d;
        type = DOUBLE;
    }

    PrimitiveClass(byte[] arr) {
        byteArray = arr;
        type = BYTEARRAY;
    }

    PrimitiveClass(String s) {
        stringValue = s;
        type = STRING;
    }

    PrimitiveClass(boolean b) {
        boolValue = b;
        type = BOOLEAN;
    }

    PrimitiveClass(String s, boolean unknown) {
        stringValue = s;
        type = UNKNOWN;
    }

    @Override
    public String toString() {
        String s = "";
        switch (type) {
            case INTEGER:
                s = s + intValue;
                break;
            case DOUBLE:
                s = s + doubleValue;
                break;
            case STRING:
                s = s + stringValue;
                break;
            case UNKNOWN:
                s = s + (stringValue == null ? "UNKNOWN" : stringValue);
                break;
            case BYTEARRAY:
                s = s + Util.byteArrayToIA5String(Util.nibbleSwap(byteArray));
                break;
        }
        return s;
    }
}
