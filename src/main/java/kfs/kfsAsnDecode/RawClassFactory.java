package kfs.kfsAsnDecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstracts the creation of RawClass Objects from ASNGrammar File. The RawClass objects are created
 * on first use and cached for subsequent calls.
 */
public class RawClassFactory {

    /**
     * Returns the root ASN Class name in a grammar file. caches Grammar file in memory.
     *
     * @param fileName - Name of ASN Grammar file.
     * @return - Name of the root ASN Class inside 'fileName'
     */
    public static String getRootClassName(String fileName) {

        if (!mapOfFileNameRawClass.containsKey(fileName)) {
            loadGrammarFile(fileName);
        }

        String rootClassName = null;
        if (mapOfFileNameRootClass.containsKey(fileName)) {
            rootClassName = mapOfFileNameRootClass.get(fileName);
        }
        return rootClassName;
    }

    /**
     * Caches Grammar file in memory.
     *
     * @param fileName - ASNGrammar File containing 'className'
     * @param className - The ASN class whose RawClass object is required.
     * @return RawClass object corresponding to input parameters.
     * @throws ASNException if ASN Class is not present in fileName.
     *
     */
    @SuppressWarnings({"unchecked"})
    public static RawClass getRawClass(String fileName, String className) {
        if (ASNConst.isPrimitive(className)) {
            RawClass ret = new RawClass();
            ret.className = className;
            return ret;
        }

        if (!mapOfFileNameRawClass.containsKey(fileName)) {
            loadGrammarFile(fileName);
        }

        Map<String, RawClass> mapRawClass = (Map<String, RawClass>) mapOfFileNameRawClass.get(fileName);

        if (mapRawClass.containsKey(className)) {
            return mapRawClass.get(className);
        } else {
            throw new ASNException("There is no RawClass(" + className + ") in file(" + fileName + ")");
        }
    }

    /**
     * Loads Grammar file. Assumes Grammar file was not cached and caches it. Not to be called
     * directly. Is used internally by caching methods.
     *
     * @param fileName - ASN Grammar file to be loaded
     */
    private static void loadGrammarFile(String fileName) {
        Map<String, RawClass> mapRawClass;
        mapRawClass = new HashMap<String, RawClass>();

        String[] fileStr = Util.toStringArray(fileName); //fileToString(inputFile);		
        //System.out.printf("\n File %s = \n(%s)",inputFile, fileStr);

        String[] convStr = convertStr(fileStr);
        if (convStr == null) {
            throw new ASNException("ASN Grammar File(" + fileName + ") does not contain any module.");
        }

        for (int i = 0; i < convStr.length; i++) {
            RawClass rawclass = stringToRawClass(convStr[i], fileName);
            if (i == 0) { // Assuming the first Raw Class is the root Class
                mapOfFileNameRootClass.put(fileName, rawclass.className);
            }
            mapRawClass.put(rawclass.className, rawclass);
        }
        mapOfFileNameRawClass.put(fileName, mapRawClass);
    }

    /**
     * Converts input Array of String to a different format.	* The method assumes that the grammar
     * file contains only 1 module (BEGIN/END BLock)
     *
     * @param arr - The grammar file contents
     * @return String[] - Each String in the array corresponds to a RawClass. ie All information
     * required to build a RawClass is present in the String.
     * @throws ASNException if unable to parse the input parameter 'arr'
     */
    static String[] convertStr(String[] arr) {
        int beginPos = -1;
        int endPos = -1;

        for (int i = 0; i < arr.length; i++) {
            String s = arr[i].trim();
            if (s.equals("BEGIN")) {
                beginPos = i;
            }
            if (s.equals("END")) {
                endPos = i;
            }
        }
        if (beginPos == -1 || endPos == -1) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        boolean skipLine = false;
        boolean firstLine = true;
        //$2 = replace all \n (not followed by word ::=) with 'space'
        for (int i = beginPos + 1; i < endPos; i++) {
            String curLine = filterComments(arr[i]);
            boolean isknownClassDefination = isClassDefination(curLine);
            boolean isClassDefination = curLine.contains("::=");

            if (isClassDefination && !isknownClassDefination) {
                if (curLine.startsWith(" ")) { // Defensive Check -- No point in skipping this.  
                    throw new ASNException("\nError Parsing Grammar at Line " + (i + 1) + " : \"" + curLine + "\"");
                }

                //System.out.printf("\n#### %d Skipping: %s",i, curLine);
                continue;
            }

            if (isknownClassDefination) {
                if (firstLine) { // Don't append "\n for the first line, Append i (line number), which is usefull if linenumber needs to be reported.
                    sb = new StringBuffer(i + "," + curLine);
                    firstLine = false;
                } else {
                    sb.append("\n").append(i).append(",").append(curLine);
                }
                //System.out.printf("\n@@@" + curLine);
            } else {
                sb.append(" ").append(curLine);
                //System.out.printf("$$$$$(%d) %s",i,curLine);
            }
        }
        //System.out.printf("\n sb = (%s)", sb.toString());

        String[] parts = sb.toString().split("\n");
        return parts;
    }

    /**
     * Filters out comments in ASN Grammar file.
     *
     * @param s - Any Single line in a Grammar file.
     * @return If 's' contains a comment then the comment is removed and returned, if no comments
     * returns s
     */
    static String filterComments(String s) {
        if (s.contains("--")) {
            int pos = s.indexOf("--");
            return s.substring(0, pos);
        }
        return s;
    }

    /**
     * Checks if a input String is a ASN Class declaration.
     *
     * @param s - The String to be check for ASN Class declaration.
     * @return true if s starts with <word> ::=
     */
    static boolean isClassDefination(String s) {
        String hword = "((\\w|\\-)*)";                       // 2 Groups		
        String regEx = hword + "\\s*" + "::=" + ".*";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matchResult = pattern.matcher(s);
        return matchResult.matches();
    }

    /**
     * @param s - Assumes s to be in the format "blah1 := { blah2, blah3 }"
     * @return null if 's' is not in the expected format else splits the 's' using "::=" as
     * delimiter and returns an array of 2 Strings.
     *
     * Example -
     *
     * If s = "blah1 := { blah2, blah3 }" and if this method returns 'ret', then ret[0] = "blah1 :="
     * # which corresponds to MLFL syntax (Multi Liner First Line) ret[1] = " blah2 , blah3 " #
     * corresponds to contents of MultiLiner declaration.
     *
     * === Terminology === Any ASNClass declaration which contains multiple fields defined within
     * braces is referred to as MultiLiner, which is the input string 's'.
     *
     * The first n characters in MultiLiner until the character '{' is referred to as MLFL i.e -
     * multi-Liner First Line
     *
     */
    static String[] splitString(String s) {
        int p1 = s.indexOf("{");
        int p2 = s.indexOf("}");

        if (p1 == -1 || p2 == -1) {
            return null;
        }

        String name = s.substring(0, p1);
        String value = s.substring(p1 + 1, p2);

        return new String[]{name, value};
    }

    /**
     * Converts a RawClass String to a RawClass Object.
     *
     * A RawClass String closely resembles, the ASN class definition in a grammar file. In case of
     * RawClass String the complete ASN class is defined in a single Line.
     *
     * @param s - RawClass String
     * @param fileName - Name of the file in which RawClass String is present
     * @return - RawClass object corresponding to input RawClass String
     */
    static RawClass stringToRawClass(String s, String fileName) {

        if (s == null) {
            throw new ASNException("Cannot convert null string to RawClass");
        }

        // Extract the line number from the String s;
        int lineNoPos = s.indexOf(",");
        String lineNo = s.substring(0, lineNoPos);
        s = s.substring(lineNoPos + 1, s.length());
//		System.out.printf("\n lineNo(%s) s(%s)",lineNo,s);

        RawClass ret = new RawClass();
        ret.lineNumber = Integer.parseInt(lineNo);
        ret.fileName = fileName;
        ret.singleLiner = s.contains("{") ? false : true;

        if (ret.singleLiner) {
            Matcher matchResultSL = singleLinerPattern.matcher(s);

            if (!matchResultSL.matches()) {
                String err = "Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") Format of String(" + s + ") is as per Single Liner Class declaration.";
                throw new ASNException(err);
            }

            boolean classNameMissing = (matchResultSL.group(sName) == null) ? true : false;
            if (!classNameMissing) {
                ret.className = matchResultSL.group(sName).trim();
                classNameMissing = ret.className.equals("") ? true : false;
                if (ASNConst.isPrimitive(ret.className)) {
                    return ret;
                }
            }
            if (classNameMissing) {
                throw new ASNException("Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") Missing ClassName in String(" + s + ") describing a Single Line Class declaration");
            }

            if (matchResultSL.group(sRelation) != null) {
                ret.relation = matchResultSL.group(sRelation).trim();
            } else {
                ret.relation = RawClass.RELATION_NIL; // Relation is optional
            }

            boolean typeMissing = matchResultSL.group(sType) == null ? true : false;
            if (!typeMissing) {
                ret.synonymn = matchResultSL.group(sType).trim();
                typeMissing = ret.synonymn.equals("") ? true : false;
            }
            if (typeMissing) {
                throw new ASNException("Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") Missing Synonymn in String(" + s + ") describing a Single Line Class declaration");
            }
        } else { // ret.MultiLiner =============================================			

            String[] splitStr = splitString(s);
            if (splitStr == null) {
                throw new ASNException("Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") String(" + s + ") describing MultiLiner Class declaration. Check Missing Braces {}");
            }
            String mlfl = splitStr[0].trim();
            String mlcontents = splitStr[1].trim();

            // -----------------------------------
            Matcher matchResultMLFL = multiLinerFirstLinePattern.matcher(mlfl);

            if (!matchResultMLFL.matches()) {
                String err = "Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") Format of String(" + mlfl + ") is as per syntax of First Line MultiLiner Class declaration.";
                throw new ASNException(err);
            }

            boolean classNameMissing = (matchResultMLFL.group(sName) == null) ? true : false;
            if (!classNameMissing) {
                ret.className = matchResultMLFL.group(sName).trim();
                classNameMissing = ret.className.equals("") ? true : false;
                if (ASNConst.isPrimitive(ret.className)) {
                    return ret;
                }
            }
            if (classNameMissing) {
                throw new ASNException("Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") Missing ClassName in String(" + s + ") describing a First Line of MultiLiner Class declaration");
            }

            if (matchResultMLFL.group(sPos) != null) {
                String assocTag = matchResultMLFL.group(sPos).trim();
                ret.associatedTag = Integer.parseInt(assocTag);
            }

            if (matchResultMLFL.group(sRelation) != null) {
                ret.relation = matchResultMLFL.group(sRelation).trim();
            } else {
                ret.relation = RawClass.RELATION_NIL; // Relation is optional
            }

            if (matchResultMLFL.group(sType) != null) {
                ret.synonymn = matchResultMLFL.group(sType).trim();
            } else {
                throw new ASNException("Error Parsing File(" + ret.fileName + ") Line(" + ret.lineNumber + ") Missing ClassName in String(" + mlfl + ") describing First Line of MultiLiner Class declaration.");
            }

            if (!ret.singleLiner && (ret.synonymn.equals("ENUMERATED")
                    || ret.synonymn.equals("INTEGER") || ret.synonymn.equals("BIT STRING"))) {
                ret.singleLiner = true;
                return ret;
            }
            // -----------------------------------------------------
            String[] parts = mlcontents.split(",");
            ArrayList<String> tmpFields = new ArrayList<String>();
            ArrayList<String> tmpType = new ArrayList<String>();
            ArrayList<Integer> tmpPos = new ArrayList<Integer>();
            ArrayList<ArrayInfo> tmpisArr = new ArrayList<ArrayInfo>();

            // The above ArrayLists are set in the below For loop 
            for (int i = 0; i < parts.length; i++) {
                String curLine = parts[i].trim();

                Matcher matchResult = multiLinerChildPattern.matcher(curLine);
                if (matchResult.matches()) {
                    //System.out.printf("\n children matches %d",i);
                    boolean hasPos = false;
                    boolean hasTag = false;
                    boolean hasArr = false;
                    boolean hasSeq = false;
                    String fieldName = matchResult.group(gName).trim();
                    String fieldType = matchResult.group(gType).trim();
                    String relationStr = matchResult.group(gRelation);
                    tmpFields.add(fieldName);
                    tmpType.add(fieldType);
                    ArrayInfo fieldIsArray = new ArrayInfo(0, 0); // ------------ 
                    if (relationStr == null) {
                        fieldIsArray.setArray(false);
                    } else {
                        if (relationStr.trim().equals(RawClass.RELATION_SEQUENCE_OF)) {
                            fieldIsArray.setArray(true);
                            fieldIsArray.setType(ArrayInfo.SEQ);
                        } else if (relationStr.trim().equals(RawClass.RELATION_SET_OF)) {
                            fieldIsArray.setArray(true);
                            fieldIsArray.unsetType(ArrayInfo.SEQ);
                        } else {
                            fieldIsArray.setArray(false); // = new Boolean( false );		
                        }
                    }
                    // ---------------------------
                    String pos_str = matchResult.group(gPos);
                    if (pos_str == null) {
                        pos_str = "-2";
                    } // -2 means it was not present in file
                    int posvalue = (Integer.parseInt(pos_str));
                    tmpPos.add(posvalue);
                    if (posvalue == -2 || posvalue == -1) {  // -1 may turn out to be array if its defination is a single liner with SEQ OF
                        fieldIsArray.unsetType(ArrayInfo.POS);
                    } else {
                        fieldIsArray.setType(ArrayInfo.POS);
                        // Implicitly assume that no position means pseudo position -1 ??
                        // posvalue = -1; // Not recommended, but this is the place if you want to do so..
                    }

                    tmpisArr.add(fieldIsArray);

                    if (posvalue == -2 && !fieldType.equals("OBJECT IDENTIFIER") && !fieldIsArray.isArray()) {
                        throw new ASNException("Error Parsing File(" + ret.fileName + ") Class(" + ret.className + ") at line (" + ret.lineNumber + ") FieldPos(" + i + ") Field(" + curLine + ") does not match syntax of Field in a MultiLiner Class declaration. Field Position is missing. ");

                    }
                } else {
                    throw new ASNException("Error Parsing File(" + ret.fileName + ") Class(" + ret.className + ") at line (" + ret.lineNumber + ") FieldPos(" + i + ") Field(" + curLine + ") does not match syntax of Field in a MultiLiner Class declaration");
                }
            }
            ret.fields = new String[tmpFields.size()];
            ret.type = new String[tmpType.size()];

            tmpFields.toArray(ret.fields);
            tmpType.toArray(ret.type);
            ret.pos = Util.toIntArray(tmpPos);
            ret.arrInfo = Util.toBoolArray(tmpisArr);
        }
        return ret;
    }

    /**
     * Returns the internal cache object stored by RawClass factory.
     *
     * @return
     */
    public Map<String, Object> getMapOfFileNameRawClass() {
        return mapOfFileNameRawClass;
    }
    // ====================================================================
    // Private members   
    // ====================================================================
    private static String singleLinerRegEx = getRegEx("singleLiner");
    private static String multiLinerChildRegEx = getRegEx("multiLinerChild");
    private static String multiLinerFirstLineRegEx = getRegEx("multiLinerFirstLine");
    private static Pattern singleLinerPattern = Pattern.compile(singleLinerRegEx);
    private static Pattern multiLinerChildPattern = Pattern.compile(multiLinerChildRegEx);
    private static Pattern multiLinerFirstLinePattern = Pattern.compile(multiLinerFirstLineRegEx);
    /**
     * The cache object where all RawClasses are stored -
     *
     * Is a map of String and Object where String is filename and where Object is
     * Map<RawClassName,RawClass>
     */
    private static Map<String, Object> mapOfFileNameRawClass = new HashMap<String, Object>();
    private static Map<String, String> mapOfFileNameRootClass = new HashMap<String, String>();
    /**
     * Group Positions in MultiLinerChild RegEx
     */
    private static int gName, gPos, gRelation, gType, gSize, gOthers;
    /**
     * Group Position in MultiLinerFirstLine and SingleLiner Reg Ex
     */
    private static int sName, sPos, sRelation, sType;

    /**
     * @param name - should be either "singleLiner" | "multiLinerFirstLine" | "multiLinerChild"
     * @return - The regex string corresponding to name
     */
    private static String getRegEx(String name) {
        if (!name.equals("singleLiner") && !name.equals("multiLinerFirstLine") && !name.equals("multiLinerChild")) {
            throw new ASNException("Invalid Argument recieved by getRegEx");
        }

        String hword = "((\\w|\\-)*)";                                      // 2 Groups 
        String pos = "(\\[\\s*(\\-?\\d+)\\s*\\])*";                                // 2 Group
        String primitive = "OBJECT IDENTIFIER|OCTET STRING|INTEGER|ENUMERATED|BIT STRING|NULL|IA5String|GraphicString|REAL|BOOLEAN";
        String type = "(" + primitive + "|" + hword + ")";           // 3 Groups
        String relationNorm = "(SET|SEQUENCE|CHOICE)*\\s*";  // optional 1 Group
        String relationArray = "(SET OF|SEQUENCE OF)*\\s*";  // optional 1 Group
        String others = "(DEFAULT|OPTIONAL)*.*";                               // optional 1 Group
        String implicit = "(IMPLICIT)*\\s*";

        String openBrace = "\\(\\s*";
        String closeBrace = "\\s*\\)";
        String range = "[\\d[\\.]]+";
        String val = openBrace + "(" + range + ")" + closeBrace;
        String size = "(" + openBrace + "SIZE" + "\\s*" + val + "\\s*" + closeBrace + ")*"; // optional 2 Group

        String multiLinerChild = "\\s*" + hword + "\\s*" + pos + "\\s+" + implicit + relationArray + type + "\\s*" + size + "\\s*" + others;
        // GROUPS OF INTEREST -             1               4                                   [6]           7              [11]             [12]
        gName = 1;
        gPos = 4;
        gRelation = 6;
        gType = 7;
        gSize = 11;
        gOthers = 12;
        // Eg.		 -field-name  [ 10 ] SET OF -class-name-(SIZE(15..43))OPTIONAL";


        // NOTE: Group Positions are same for singleLiner and MultiLinerFirstLine
        String singleLiner = "\\s*" + hword + "\\s*::=\\s*" + pos + "\\s+" + implicit + relationArray + type + "\\s*" + size + "\\s*.*";
        // Groups of Interest           1                                                         [6]            7              [11] 		
        sName = 1;
        sPos = 4;
        sRelation = 6;
        sType = 7;

        String multiLinerFirstLine = "\\s*" + hword + "\\s*::=\\s*" + pos + "\\s+" + implicit + relationNorm + type + "\\s*" + size + "\\s*.*";
        // Groups of Interest                    1                      [4]                                                    [9] 		


        return name.equals("multiLinerChild") ? multiLinerChild : (name.equals("singleLiner") ? singleLiner : multiLinerFirstLine);
    }
}
