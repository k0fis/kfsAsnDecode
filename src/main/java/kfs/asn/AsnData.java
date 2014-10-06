package kfs.asn;

import kfs.asn.utils.AsnConst;
import kfs.asn.utils.AsnUtil;
import kfs.asn.utils.ASNException;

/**
 *
 * @author pavedrim
 */
public class AsnData {

    private final int asnTag;
    private final byte[] byteArray;

    public AsnData(int tag, byte[] value) {
        this.asnTag = tag;
        this.byteArray = value;
    }

    public int getTag() {
        return asnTag;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public String getStringValue() {
        return AsnUtil.bytesToHex(byteArray).toString();
    }

    public int getIntValue() {
        return AsnUtil.byteArrayToInt(byteArray);
    }

    public Boolean getBoolValue() {
        if (asnTag == AsnConst.TAG_BOOLEAN) {
            return byteArray[0] != 0;
        }
        //Logger.getLogger(AsnData.class).info("Boolean with values: " + getStringValue());
        return true;
    }

    public Object getData(Class obj) {
        if (Integer.class.equals(obj) || int.class.equals(obj)) {
            return getIntValue();
        }
        if (String.class.equals(obj)) {
            return getStringValue();
        }
        if (Boolean.class.equals(obj) || boolean.class.equals(obj)) {
            return getBoolValue();
        }
        throw new ASNException(null, "Cannot get data from class: " + obj.getSimpleName());
    }
}
