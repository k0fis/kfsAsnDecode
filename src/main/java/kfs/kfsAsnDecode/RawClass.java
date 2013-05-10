package kfs.kfsAsnDecode;

/**
 * Models an ASNClass found in the grammar file.
 *
 * RawClass are non recursive unlike ASNClass. They are used as an intermediate class before
 * constructing ASNClass itself. Purpose of this class to abstract the low level Parsing logic of
 * Grammar files involving RegEx and ASNClass syntax. So that ASNClass can be built using RawClass
 * objects instead of ASNGrammar file.
 *
 * Two kinds of class constructs found in a ASN Grammar file are encapsulated in RawClass.
 *
 * #1. MultiLiner Construct --------------
 *
 * CallEventRecord	::= CHOICE { moCallRecord [0] MOCallRecord, mtCallRecord [1] SET OF MTCallRecord,
 * }
 *
 * The above ASNClass is parsed and stored in a RawClass as -
 *
 * RawClass.className ::= RawClass.relation { RawClass.field[0] RawClass.pos[0] RawClass.type[0]
 * RawClass.field[1] RawClass.pos[1] RawClass.arrInfo[1] RawClass.type[1] }
 *
 *
 * #2. SingleLiner Construct -------------
 *
 * IMEI ::= SEQUENCE OF OCTET STRING
 *
 * The above ASNClass is parsed and stored in a RawClass as RawClass.className ::= RawClass.relation
 * RawClass.synonymn
 *
 */
public class RawClass {

    /**
     * Tells if this RawClass is a SingleLiner or MultiLiner.
     */
    public boolean singleLiner;
    /**
     * The file from which the RawClass was built
     */
    public String fileName; // Name of the Grammar file where this class exists.
    /**
     * The line number at which this class can be found.
     */
    public int lineNumber;
    /**
     * The name of the RawClass
     */
    public String className;
    /**
     * The relation Eg. CHOICE/SET/SEQUENCE used to describe the class. In case the RawClass is a
     * SingleLiner then relation may be SET OF/SEQUENCE OF
     *
     * Example of Single Liner - SomeClass ::= SET OF OCTET STRING Here the relation is "SET OF"
     */
    public String relation;
    /**
     * All Primitive ASNClass are associated with pre-defined tag.
     *
     * ASNCLass associatedTag ---------------------------- BOOLEAN 1 INTEGER 2 OCTETSTRING 4
     * IA5STRING 22
     *
     * UserDefined ASNClass may be associated with associatedTag if defined in the Grammar file.	*
     *
     * Example of 'UserDefinedclass' with associatedTag '0'
     *
     * UserDefinedClass ::=[0] IMPLICIT SEQUENCE { recordType [1] INTEGER }
     *
     * associatedTag is set to NO_ASSOCIATED_TAG if the class is not associated with any tag.
     *
     *
     */
    public int associatedTag = NO_ASSOCIATED_TAG;
    public final static int NO_ASSOCIATED_TAG = -1;
    public final static String RELATION_NIL = "NoRelation";
    public final static String RELATION_SET_OF = "SET OF";
    public final static String RELATION_SEQUENCE_OF = "SEQUENCE OF";
    // ------------------ SUB FIELD INFO ----------------------
    /**
     * All field names in a MultiLiner Class
     */
    public String[] fields;
    /**
     * All Type names in a MultiLiner Class
     */
    public String[] type;
    /**
     * Positions of all fields in a MultiLiner Class
     */
    public int[] pos;
    /**
     * Array Information of all fields in a MultiLiner Class
     */
    public ArrayInfo[] arrInfo; // whether this sub field is an array SEQ OF/SET OF
    // --------------------------------------------------------
    /**
     * For MultiLiners synonym is null, For Single Liner ASN Class declaration see below example.
     *
     * Example - IMEI ::= OCTET STRING ( SIZE(8) )
     *
     * In above singleLiner construct OCTET STRING is a synonym of IMEI
     */
    public String synonymn; // For Single Liners only

    RawClass() {
    }

    RawClass(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        String ret = "\n[RawClass]= { class=" + className + ",singleLiner" + singleLiner
                + ",relation=" + relation
                + ",synonymn=" + synonymn
                + ",fileName=" + fileName
                + ",lineNumber=" + lineNumber;
        if (fields == null) {
            ret = ret + "\n No sub fields present";
        } else {
            ret = ret + ",children=" + fields.length;
            for (int i = 0; i < fields.length; i++) {
                String isArrStr = arrInfo[i].isArray() ? "[]" : "";
                ret = ret + "\n{Field=" + i + ",position=" + pos[i] + ",type="
                        + type[i] + isArrStr + "}";
            }
        }
        return ret;
    }
} // End of Raw Class ==============================================
