package kfs.asn.utils;


/*
 * Used as a member of a RawClass and ASNClass to add hold information if the corresponding class is
 * an array type.
 */
public class ArrayInfo {

    /**
     * Dimension of the array. Currently only single Dimension array is supported. If zero,
     * indicates non array.
     */
    private int arraySize;
    /**
     * Array internally may have many sub types (based on implementation) By default arrayType is
     * zero.
     */
    private int arrayType;     // by default 0

    @Override
    public ArrayInfo clone() {
        return new ArrayInfo(arraySize, arrayType);
    }

    /**
     * @param size - Dimension of the array - (Expected values 0 or 1)
     * @param type_ - internal subType of array. (Default 0)
     */
    ArrayInfo(int size_, int type_) {
        arraySize = size_;
        arrayType = type_;
    }
    // Possible Bit values that can be set for arrayType
    final public static int POS = 1;
    final public static int TAG = 1 << 1;
    final public static int SEQ = 1 << 2;
    final public static int CHO = 1 << 3;  // means SET/SEQ OF CHOICE OF {...}

    // Getters and Setters for 'arrayType'
    void setType(int type) {
        arrayType = arrayType | type;
    }

    void unsetType(int type) {
        arrayType = arrayType & ~type;
    }

    boolean getType(int type) {
        return (arrayType & type) == type;
    }

    //static int NOPOS_NOTAG_SEQ = 0; // [16SEQ] [16.x]  [16SEQ] [16.x] 
    //static int NOPOS_TAG_SEQ = 1;   // [TAG] [TAG.x] [TAG] [TAG.x]
    //static int POS_NOTAG_SEQ = 2;   // [POS] [16SEQ] [16.x]  [16SEQ] [16.x]
    //static int POS_TAG_SEQ = 3;     // [POS] [TAG] [TAG.x] [TAG] [TAG.x]
    /**
     * Returns a String representing the status of 'arrayType'
     */
    String getType() {
        String pos = getType(POS) ? "POS" : "NOPOS";
        String tag = getType(TAG) ? "TAG" : "NOTAG";
        String seq = getType(SEQ) ? "SEQ" : "SET";
        String cho = getType(CHO) ? "CHO" : "NCH";
        return isArray() ? "[" + pos + "|" + tag + "|" + seq + "|" + cho + "]" : "";
    }

    /**
     * @return true if type is an array.
     */
    public boolean isArray() {
        return (arraySize != 0);
    }

    /**
     * @param bool - if true converts to an arrayType, else non array Type.
     */
    void setArray(boolean bool) {
        arraySize = bool ? 1 : 0;
    }
}
