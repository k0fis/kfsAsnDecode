package kfs.asn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author pavedrim
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ASNDef {

    int asnPos();
    
    String asnName();

    String label() default "";
    
    String asnType() default "";

}
