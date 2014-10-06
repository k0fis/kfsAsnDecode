package kfs.asn.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds instance of ASNClass using a ASN Grammar File.
 *
 * TODO: Support for INTEGER hour (0..23) min day notation in Huawie Support for Filters Support for
 * notations where INTEGER value is missing chk Huawei
 */
public class ASNClassFactory {

    private int depth = 0;
    private ASNClass asnarr;
    private final Field field;
    

    public ASNClassFactory(GramarFile fileName) {
        RawClassFactory rcf = new RawClassFactory(fileName);
        String rootNodeName = rcf.getRootClassName();

        RawClass rawclass = rcf.getRawClass(rootNodeName);
        //System.out.printf("\n Got RawClass for %s from file %s = \n %s ", rootNodeName, fileName ,rawclass);

        ASNClass asnClass = toASNClass(rcf, rawclass, Field.ROOTFIELD);
        asnClass.toArray();  // We assume that the Top Level ASNClass is always an Array Type.

        Map<String, String> options = loadControlFile(fileName);
        String OPTION_BLOCKSIZE = "BLOCKSIZE";
        String OPTION_PADDINGBYTE = "PADDINGBYTE";

        if (options.containsKey(OPTION_BLOCKSIZE)) {
            asnClass.blockSize = Integer.parseInt(options.get(OPTION_BLOCKSIZE));
        }
        if (options.containsKey(OPTION_PADDINGBYTE)) {
            asnClass.paddingByte = (byte) Integer.parseInt(options.get(OPTION_PADDINGBYTE), 16);
        }

        field = new Field(Field.ROOTFIELD, Field.ROOTFIELD, AsnConst.POS_NOT_SPECIFIED, asnClass);
    }

    private ASNClass getPrimitive(String className) {
        if (AsnConst.isPrimitive(className)) {
            ASNClass aclass = new ASNClass();
            aclass.name = className;
            aclass.setPrimitiveName(className);
            Map<String, Integer> map = AsnConst.getPrimitiveMap();
            aclass.associatedTag = map.get(className);
            return aclass;
        }
        throw new ASNException(null, "Cannot create primitive for " + className);
    }

    // Whatever ASNClass I am going to return will be inside a Field class
    // called (its container). This container Field's longname is provided as arg
    // so all children fields in this class should be prefixed with 
    // containerFieldLongName + "." + "fieldName"
    private ASNClass toASNClass(RawClassFactory rcf, RawClass rd, String containerFieldLongName) {
        depth++;
        try {
            if (AsnConst.isPrimitive(rd.className)) {
                return getPrimitive(rd.className);
            }
            ArrayList<ASNClass> childTypes = new ArrayList<ASNClass>();
            ArrayInfo[] childIsArray = rd.arrInfo;

            for (int i = 0; i < rd.fields.length; i++) { // for all fields inside this class
                if (rd.type[i].equals("")) {
                    throw new ASNException("There is no Type for RawCls" + rd + "field inx = " + i);
                }
                RawClass tmp = null;
                try {
                    tmp = rcf.getRawClass(rd.type[i]);
                } catch (ASNException asnexp) {
                    throw new ASNException(null, "Error in " + rd.className + ".getRawClass1("
                            + rd.type[i] + ")", asnexp);
                }
                String fieldLongName = containerFieldLongName + "." + rd.fields[i];

                if (tmp.singleLiner) {
                    if (tmp.relation.equals("SET OF") || tmp.relation.equals("SEQUENCE OF")) {
                        childIsArray[i].setArray(true);
                        if (tmp.relation.equals("SEQUENCE OF")) {
                            childIsArray[i].setType(ArrayInfo.SEQ);
                        } else {
                            childIsArray[i].unsetType(ArrayInfo.SEQ);
                        }
                        //childIsArray[i].set(1,ArrayInfo.TYPE3); // = true; // As of now we only support single dimension arrays!
                    }
                    if (AsnConst.isPrimitive(tmp.synonymn)) {// PRIMITIVE
                        ASNClass tmpClass = getPrimitive(tmp.synonymn);
                        childTypes.add(tmpClass);
                    } else { // COMPOSITE or a COMPOSITE which is actually a synonym
                        boolean hitMultiLiner = false;
                        boolean hitMultiLinerWithPrimitive = false;
                        while (!hitMultiLiner) { // Keep searching amongst
                            // synonyms till Primitive
                            // is encountered.
                            try {
                                tmp = rcf.getRawClass(tmp.synonymn);
                            } catch (ASNException asnexp) {
                                throw new ASNException(null, "Error in " + "RawASNClass("
                                        + rd.className + ") Field(" + rd.fields[i] + ") Type("
                                        + rd.type[i] + ") Position(" + (i + 1) + "/"
                                        + rd.fields.length + ") getRawClass2("+ tmp.synonymn + 
                                        ")", asnexp);
                            }

                            hitMultiLiner = !tmp.singleLiner;
                            if (!hitMultiLiner) {
                                if (AsnConst.isPrimitive(tmp.synonymn)) // PRIMITIVE
                                {
                                    hitMultiLinerWithPrimitive = true;
                                    childTypes.add(getPrimitive(tmp.synonymn));
                                }
                            }
                        }
                        if (!hitMultiLinerWithPrimitive) {
                            ASNClass tmpClass = toASNClass(rcf, tmp, fieldLongName);
                            childTypes.add(tmpClass);
                            // If this field is an array and this field's Class is a CHOICE OF
                            if (childIsArray[i].isArray() && tmpClass.relation != null && //
                                    tmpClass.relation.equals(AsnConst.RELATION_CHOICE)) {
                                childIsArray[i].setType(ArrayInfo.CHO);
                            }
                        }
                    }
                } else {// multiliner.
                    ASNClass tmpClass = toASNClass(rcf, tmp, fieldLongName);
                    childTypes.add(tmpClass);
                    // Can use tmpClass.isReference() instead of tmpClass.relation.equals(RELATION_CHOICE) since we are 
                    // setting the variable internally by tmp.isReference().
                    if (childIsArray[i].isArray() && tmpClass.relation != null && //
                            tmpClass.relation.equals(AsnConst.RELATION_CHOICE)) {
                        childIsArray[i].setType(ArrayInfo.CHO);
                    }
                }
            }
            ASNClass[] childArr = new ASNClass[childTypes.size()];
            childTypes.toArray(childArr);

            for (int i = 0; i < childArr.length; i++) {
                if (childArr[i].isAssociatedWithTag()) {
                    childIsArray[i].setType(ArrayInfo.TAG);
                } else {
                    childIsArray[i].unsetType(ArrayInfo.TAG);
                }
                childArr[i].arrInfo = childIsArray[i];
            }
            // Create a Field out of what we have =========================
            Field[] fieldList = new Field[childArr.length];

            for (int i = 0; i < fieldList.length; i++) {
                ASNClass tmp_class = childArr[i];
                String longFieldName = containerFieldLongName + "." + rd.fields[i];
                fieldList[i] = new Field(longFieldName, rd.fields[i], rd.pos[i], tmp_class);
            }

            //ASNClass(String name_, String relation_, Field[] fields_, ArrayInfo arrInfo_,int associatedTag_) {
            asnarr = new ASNClass(rd.className, rd.relation, fieldList);
            //asnarr = new ASNClass(rd.className, rd.relation, rd.pos, rd.fields, childArr );
            asnarr.associatedTag = rd.associatedTag;

        } catch (ASNException e) {
            throw new ASNException("asn cls fctr", "Exception caught inside toASNClass(RawClass(" //
                    + rd.className + "))", e);
        } finally {
            depth--;
        }
        return asnarr;
    }

    public Field getField() {
        return field;
    }
    // ========================================================================

    /**
     * Assumes the format of Control file to be as Follows	* First Line of file should be
     * --OPTIONS-- name1=value1 , name2 = value2 , ... , namen = valuen Note - Comma and equals char
     * cannot be used in name and value.
     *
     * @param fileName
     * @return
     */
    private static Map<String, String> loadControlFile(GramarFile fileName) {

        String line = fileName.lines[0];

        Map<String, String> retMap = new HashMap<String, String>();
        final String tagStr = "--OPTIONS--";
        int tagStrLen = tagStr.length();   // 11

        if (line != null && line.startsWith(tagStr) && line.length() > tagStrLen) {
            line = line.substring(tagStrLen, line.length());

            String[] parts = line.split(",");
            for (String part : parts) {
                String option = part.trim();
                String[] nameValPair = option.split("=");
                if (nameValPair.length >= 2) {
                    retMap.put(nameValPair[0].trim(), nameValPair[1].trim());
                }
            }
            //System.out.printf("\n inside >> line=(%s)",line);
        } else {
            //System.out.printf("\n No Options Found in File",line);
        }
        return retMap;
    }

}
