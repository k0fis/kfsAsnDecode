package kfs.asn.utils;

import java.util.HashMap;
import java.util.Map;

public class AsnConst {

    public final static int TAG_BOOLEAN = 1;
    public final static int TAG_INTEGER = 2;
    public final static int TAG_BIT_STRING = 3;
    public final static int TAG_OCTET_STRING = 4;
    public final static int TAG_NULL = 5;
    public final static int TAG_OBJECT_IDENTIFIER = 6;
    public final static int TAG_REAL = 9;
    public final static int TAG_ENUMERATED = 10;
    public final static int TAG_NUMERIC_STRING = 18;
    public final static int TAG_PRINTABLE_STRING = 19;
    public final static int TAG_TELETEX_STRING = 20;
    public final static int TAG_VIDEOTEX_STRING = 21;
    public final static int TAG_IA5STRING = 22;
    public final static int TAG_GRAPHIC_STRING1 = 25;
    public final static int TAG_VISIBLE_STRING = 26;
    public final static int TAG_GRAPHIC_STRING2 = 27;
    public final static String RELATION_CHOICE = "CHOICE";
    public final static String RELATION_SEQUENCE = "SEQUENCE";
    public final static String RELATION_SET = "SET";
    /**
     * If Position of a field is not specified.
     */
    public static final int POS_NOT_SPECIFIED = -2;
    /**
     * For future use.
     */
    public static final int POS_RESERVED = -1;

    /**
     *
     * @param className - The string to be checked as Primitive.
     * @return true if className is a known ASN Primitive class.
     *
     * Note - SET OF/SEQUENCE OF are not considered as ASN Primitive class
     */
    public static boolean isPrimitive(String className) {
        if (primitiveMap == null) {
            getPrimitiveMap();
        }
        return primitiveMap.containsKey(className);
    }

    /**
     * @return map of primitive name and corresponding tag value.
     */
    public static Map<String, Integer> getPrimitiveMap() {
        if (primitiveMap == null) {
            primitiveMap = new HashMap<String, Integer>();  								// Maps to Java type
            primitiveMap.put("BOOLEAN", new Integer(TAG_BOOLEAN)); 	// Boolean
            primitiveMap.put("INTEGER", new Integer(TAG_INTEGER)); 	// Integer
            primitiveMap.put("BIT STRING", new Integer(TAG_BIT_STRING)); 	// Byte[]
            primitiveMap.put("OCTET STRING", new Integer(TAG_OCTET_STRING));	// Byte[]
            primitiveMap.put("NULL", new Integer(TAG_NULL)); 	// Byte[]
            primitiveMap.put("OBJECT IDENTIFIER", new Integer(TAG_OBJECT_IDENTIFIER));	// Byte[]
            primitiveMap.put("DOUBLE", new Integer(TAG_REAL)); 	// Double
            primitiveMap.put("ENUMERATED", new Integer(TAG_ENUMERATED)); 	// Integer
            primitiveMap.put("NumericString", new Integer(TAG_NUMERIC_STRING)); 	// Byte[]
            primitiveMap.put("PrintableString", new Integer(TAG_PRINTABLE_STRING));	// Byte[]
            primitiveMap.put("TeletexString", new Integer(TAG_TELETEX_STRING)); 	// Byte[]
            primitiveMap.put("VideotexString", new Integer(TAG_VIDEOTEX_STRING)); 	// Byte[]
            primitiveMap.put("IA5String", new Integer(TAG_IA5STRING)); 	// Byte[]
            primitiveMap.put("GraphicString", new Integer(TAG_GRAPHIC_STRING1)); 	// Byte[]
            primitiveMap.put("VisibleString", new Integer(TAG_VISIBLE_STRING)); 	// Byte[]
            primitiveMap.put("GraphicString2", new Integer(TAG_GRAPHIC_STRING2)); 	// Byte[]			
        }
        return primitiveMap;
    }
    private static Map<String, Integer> primitiveMap;
    private static Map<String, Class<?>> primitiveClassMap;

    public static Map<String, Class<?>> getPrimitiveClassMap() {
        if (primitiveClassMap == null) {
            primitiveClassMap = new HashMap<String, Class<?>>();
            primitiveClassMap.put("BOOLEAN", Boolean.class);
            primitiveClassMap.put("INTEGER", Integer.class);
            primitiveClassMap.put("BIT STRING", String.class);
            primitiveClassMap.put("OCTET STRING", String.class);
            primitiveClassMap.put("NULL", Integer.class);
            primitiveClassMap.put("OBJECT IDENTIFIER", String.class);
            primitiveClassMap.put("DOUBLE", Double.class);
            primitiveClassMap.put("ENUMERATED", Integer.class);
            primitiveClassMap.put("NumericString", String.class);
            primitiveClassMap.put("PrintableString", String.class);
            primitiveClassMap.put("TeletexString", String.class);
            primitiveClassMap.put("VideotexString", String.class);
            primitiveClassMap.put("IA5String", String.class);
            primitiveClassMap.put("GraphicString", String.class);
            primitiveClassMap.put("VisibleString", String.class);
            primitiveClassMap.put("GraphicString2", String.class);
        }
        return primitiveClassMap;
    }

    public static Class<?> getPrimitiveClass(String str) {
        Class<?> cls = getPrimitiveClassMap().get(str);
        if (cls == null) {
            return String.class;
        }
        return cls;
    }

}
