package kfs.asn;


/**
 *
 * @author pavedrim
 */
public interface AsnNodeCallBack {

    boolean acceptCls(Class cls);
    void kfsCb(Object node);
}
