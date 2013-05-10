package kfs.kfsAsnDecode;

import java.io.*;
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

    private static int depth = 0;
    static ASNClass asnarr;

    // Whatever ASNClass I am going to return will be inside a Field class
    // called (its container). This container Field's longname is provided as arg
    // so all children fields in this class should be prefixed with 
    // containerFieldLongName + "." + "fieldName"
    public static ASNClass toASNClass(RawClass rd, String containerFieldLongName) {
        try {
            depth++;

            if (ASNConst.isPrimitive(rd.className)) {
                return getPrimitiveASNClass(rd.className);
            }

            ArrayList<ASNClass> childTypes = new ArrayList<ASNClass>();
            ArrayInfo[] childIsArray = rd.arrInfo;

            for (int i = 0; i < rd.fields.length; i++) { // for all fields inside this class
                if (rd.type[i].equals("")) {
                    throw new ASNException("There is no Type for RawClass" + rd + "field index i=" + i);
                }

                RawClass tmp = null;
                try {
                    tmp = RawClassFactory.getRawClass(rd.fileName, rd.type[i]);
                } catch (ASNException asnexp) {
                    String s = "\n Error in " + rd.className + ".getRawClass1(" + rd.fileName + "," + rd.type[i] + ") Details: " + asnexp.getMessage();
                    throw new ASNException(s);
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
                    if (ASNConst.isPrimitive(tmp.synonymn)) // PRIMITIVE
                    {
                        ASNClass tmpClass = getPrimitiveASNClass(tmp.synonymn);
                        childTypes.add(tmpClass);
                    } else // COMPOSITE or a COMPOSITE which is actually a synonym
                    {
                        boolean hitMultiLiner = false;
                        boolean hitMultiLinerWithPrimitive = false;
                        while (!hitMultiLiner) { // Keep searching amongst
                            // synonyms till Primitive
                            // is encountered.
                            try {
                                tmp = RawClassFactory.getRawClass(tmp.fileName, tmp.synonymn);
                            } catch (ASNException asnexp) {
                                String s = " Error in " + "RawASNClass(" + rd.className + ") Field(" + rd.fields[i] + ") Type(" + rd.type[i] + ") Position(" + (i + 1) + "/" + rd.fields.length + ") getRawClass2(" + tmp.fileName + "," + tmp.synonymn + ") Details: " + asnexp.getMessage();
                                throw new ASNException(s);
                            }



                            hitMultiLiner = tmp.singleLiner ? false : true;
                            if (!hitMultiLiner) {
                                if (ASNConst.isPrimitive(tmp.synonymn)) // PRIMITIVE
                                {
                                    hitMultiLinerWithPrimitive = true;
                                    childTypes.add(getPrimitiveASNClass(tmp.synonymn));
                                }
                            }
                        }
                        if (!hitMultiLinerWithPrimitive) {
                            ASNClass tmpClass = toASNClass(tmp, fieldLongName);
                            childTypes.add(tmpClass);
                            // If this field is an array and this field's Class is a CHOICE OF
                            if (childIsArray[i].isArray() && tmpClass.relation != null && tmpClass.relation.equals(ASNConst.RELATION_CHOICE)) {
                                childIsArray[i].setType(ArrayInfo.CHO);
                            }
                        }
                    }
                } else // multiliner.
                {
                    ASNClass tmpClass = toASNClass(tmp, fieldLongName);
                    childTypes.add(tmpClass);
                    // Can use tmpClass.isReference() instead of tmpClass.relation.equals(RELATION_CHOICE) since we are 
                    // setting the variable internally by tmp.isReference().
                    if (childIsArray[i].isArray() && tmpClass.relation != null && tmpClass.relation.equals(ASNConst.RELATION_CHOICE)) {
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

        } catch (Exception e) {
            String s = "Exception caught inside toASNClass(RawClass(" + rd.className + ")," + rd.fileName + ")";
            s += "\n e.getClass() = \"" + e.getClass() + "\" e.getMessage = {" + e.getMessage() + "}";
            StackTraceElement[] st = e.getStackTrace();
            for (int i = 0; i < st.length; i++) {
                s += "\n" + st[i];
            }
            throw new ASNException(s);
        }
        return asnarr;
    }
// ===============================================================================
    // Makes ASNClass out of File
    static Map<String, Field> mapOfFileASNClass = new HashMap<String, Field>();

    /**
     *
     * @param fileName - The Grammar file to be used create ASNClass
     * @return - ASNClass representing the top level ASNClass in the specified grammar file. Always
     * the array version of the ASNClass is returned.
     */
    public static Field getField(String fileName) {


        if (!mapOfFileASNClass.containsKey(fileName)) {

            // TODO: currently the root Class is the first RawClass in module. Make it smart 
            // enough to detect root class if it isn't
            String rootNodeName = RawClassFactory.getRootClassName(fileName);

            RawClass rawclass = RawClassFactory.getRawClass(fileName, rootNodeName);
            //System.out.printf("\n Got RawClass for %s from file %s = \n %s ", rootNodeName, fileName ,rawclass);

            ASNClass asnClass = ASNClassFactory.toASNClass(rawclass, Field.ROOTFIELD);
            asnClass.toArray();  // We assume that the Top Level ASNClass is always an Array Type.

            Map<String, String> options = loadControlFile(fileName);
            applyOptions(asnClass, options);

            Field rootField = new Field(Field.ROOTFIELD, Field.ROOTFIELD, ASNConst.POS_NOT_SPECIFIED, asnClass);
            mapOfFileASNClass.put(fileName, rootField);
        }
        return mapOfFileASNClass.get(fileName);
    }
    // ========================================================================

    /**
     * Applies options to asnClass
     */
    //TODO: check access level public/private..
    public static void applyOptions(ASNClass asnClass, Map<String, String> mapOptions) {
        final String OPTION_BLOCKSIZE = "BLOCKSIZE";
        final String OPTION_PADDINGBYTE = "PADDINGBYTE";

        if (mapOptions.containsKey(OPTION_BLOCKSIZE)) {
            asnClass.blockSize = Integer.parseInt(mapOptions.get(OPTION_BLOCKSIZE));
        }
        if (mapOptions.containsKey(OPTION_PADDINGBYTE)) {
            asnClass.paddingByte = (byte) Integer.parseInt(mapOptions.get(OPTION_PADDINGBYTE), 16);
        }
        //System.out.printf("\n valueint(%s) valuebyte(%s) ", asnClass.blockSize, asnClass.paddingByte );
    }

    /**
     * Assumes the format of Control file to be as Follows	* First Line of file should be
     * --OPTIONS-- name1=value1 , name2 = value2 , ... , namen = valuen Note - Comma and equals char
     * cannot be used in name and value.
     */
    public static Map<String, String> loadControlFile(String fileName) {

        String line = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            line = bufferedReader.readLine();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            throw new ASNException("Caught FileNotFound Exception:" + e.getMessage());
        } catch (IOException e) {
            throw new ASNException("Caught IOException:" + e.getMessage());
        }


        Map<String, String> retMap = new HashMap<String, String>();
        final String tagStr = "--OPTIONS--";
        int tagStrLen = tagStr.length();   // 11

        if (line != null && line.startsWith(tagStr) && line.length() > tagStrLen) {
            line = line.substring(tagStrLen, line.length());

            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; i++) {
                String option = parts[i].trim();
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

    /**
     * Returns a ASNClass corresponding to the String primitiveName.
     *
     * @param - Name of the primitive ASN Class.
     */
    public static ASNClass getPrimitiveASNClass(String primitiveName) {

        if (!ASNConst.isPrimitive(primitiveName)) {
            throw new ASNException("getPrimitive(str) called where str is not Primitive");
        }

        ASNClass aclass = new ASNClass();

        aclass.name = primitiveName;
        aclass.setPrimitiveName(primitiveName);

        Map<String, Integer> map = ASNConst.getPrimitiveMap();
        aclass.associatedTag = map.get(primitiveName);

        return aclass;
    }
}
