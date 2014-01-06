package kfs.kfsAsnDecode;

import java.io.*;
import java.util.Map;

/**
 * Encapsulates parsed ASN Data. A Node contains parsed ASN Data + Meta Data (Grammar information)
 * corresponding to the ASNData.
 */
public class Node {

    public boolean indicator = false;
    
    /**
     * If Node is a Primitive ASN Class. The the primitive data is stored within primitive object,
     * else it is set to null.
     */
    EBlock primitiveBlock;

    // Start of BugFix #1 ===============
    // As per Bug, the isChoice member does not show the correct value after parsing.
    /**
     * Commented as a part of BugFix #1 Note - "Reference Node" and "Choice Node" are used
     * interchangeably in comments. If false, then this node is a normal Node, else it is a
     * reference Node. In case of reference node, there is only 1 subNode which is the referenced
     * Node. Also the corresponding field should be a reference (i.e Choice) TODO: Optimize see if
     * this member is really needed or can be replace by a method isChoice() which internally check
     * field.type.relation.equals("CHOICE");      *
     * //public boolean isChoice = false; // Okay testing above fix.
     */
    public boolean isChoice() {
        if (field == null) {
            return false;
        }
        return field.type.isReference();
    }
    // End of BugFix #1 ==================

    /**
     * @return referenced node if this node is a reference node, else returns NULL_NODE;
     */
    public Node getSubNodeChoice() {
        if (subNodes == null) return NULL_NODE;
        return isChoice() ? subNodes[0] : NULL_NODE;
    }
    /**
     * All Meta Information (type information) about this Node's data is stored here. In case of
     * NULL_NODE this field is null.
     */
    public Field field;
    /**
     * If Node is not a Primitive ASN Class, then the node's data is stored in subNodes recursively.
     */
    public Node[] subNodes;

    /**
     * @return true if the Node corresponds to a primitive ASN Class.
     */
    public boolean isPrimitive() {
        if (field == null) {
            return true;
        }
        return field.type.isPrimitive();
    }
    /**
     * Indicates a dummy Null Node
     */
    public static Node NULL_NODE = new Node();
    private static Node[] EMPTY_ARR = new Node[0];
    private static String NULL_NODE_STR = "NULL_NODE";
    private static final PrimitiveClass nullNodeValue = new PrimitiveClass(NULL_NODE_STR);

    private Node() { // Private constructor used only by NULL_NODE.
        field = null;
        subNodes = null;
        primitiveBlock = null;
    }

    /**
     * @param field_ - The Field containing type information for this Node.
     * @param subNodes_ - The sub nodes contain the Node data.
     * @param primitiveBlock - If the node is a primitive then its corresponding primitive Block
     * else null
     */
    Node(Field field_, Node[] subNodes_, EBlock primitiveBlock_) {
        field = field_;
        subNodes = subNodes_;
        primitiveBlock = primitiveBlock_;

        if (subNodes_ == null && primitiveBlock_ == null) {
            throw new ASNException("Invalid args to Node constructor. Both subNodes and "
                    + "Primitive Block cannot be null");
        }

    }

    public EBlock getBlock() {
        return this.primitiveBlock;
    }

    /**
     * @return PrimitiveClass if the Node is primitive, else returns dummy PrimitiveClass If called
     * on Node.NULL_NODE returns dummy PrimitiveClass.
     * @throws ASNException - If EBlock and ASNClass are not consistent.
     */
    public PrimitiveClass getValue() {

        /**
         * If composite then return a dummyPrimitiveClass with String set to "composite:numfields"
         * else based on type's string, get assoc int and switch. do Conversion to appropriate
         */
        if (field == null) {
            return Node.nullNodeValue;
        }

        PrimitiveClass obj = null;
        if (!isPrimitive()) { // if block composite
            if (field.type.isPrimitive() && !field.type.isArray()) {
                throw new ASNException("Inconsistant Node - Field is non array primitive but "
                        + "Block is Composite. " + this.field + " " + this.getBlock());
            }
            if (this.subNodes != null) {
                obj = new PrimitiveClass("Composite1(" + this.subNodes.length + ")");
            } else {
                obj = new PrimitiveClass("Composite1(0 )");
            }
        } else {
            Map<String, Integer> map = ASNConst.getPrimitiveMap();
            int tag = map.get(field.type.name);

            switch (tag) {
                case ASNConst.TAG_BOOLEAN:
                    obj = new PrimitiveClass(NodeFactory.makeBoolean(primitiveBlock));
                    break;
                case ASNConst.TAG_INTEGER:
                    obj = new PrimitiveClass(NodeFactory.makeInteger(primitiveBlock));
                    break;
                case ASNConst.TAG_ENUMERATED:
                    obj = new PrimitiveClass(NodeFactory.makeInteger(primitiveBlock));
                    break;
                case ASNConst.TAG_REAL:
                    obj = new PrimitiveClass(NodeFactory.makeReal(primitiveBlock));
                    break;
                case ASNConst.TAG_BIT_STRING:
                case ASNConst.TAG_OCTET_STRING:
                case ASNConst.TAG_NULL:
                case ASNConst.TAG_OBJECT_IDENTIFIER:
                case ASNConst.TAG_NUMERIC_STRING:
                case ASNConst.TAG_PRINTABLE_STRING:
                case ASNConst.TAG_TELETEX_STRING:
                case ASNConst.TAG_VIDEOTEX_STRING:
                case ASNConst.TAG_IA5STRING:
                case ASNConst.TAG_GRAPHIC_STRING1:
                case ASNConst.TAG_VISIBLE_STRING:
                case ASNConst.TAG_GRAPHIC_STRING2:
                    obj = new PrimitiveClass(NodeFactory.makeByteArray(primitiveBlock));
                    break;
            }
            if (obj == null) {
                throw new ASNException("Unknown primitive Tag (" + tag + ") for ASNClass = " 
                        + field.type.getName());
            }
        }
        return obj;
    }

    /**
     * The Type name includes [] and & if the type is an array or reference. Useful for debugging or
     * understanding ASN Data
     */
    public String getConciseTypeName(String typeName) {
        if (field == null) {
            return "null";
        }
        return field.type.getName();
    }

    /**
     * @return - String representing ASNClass that corresponds to this Node. returns string "null"
     * if called on NULL_NODE; The string returned does not include array/reference information
     */
    public String getTypeName() {
        if (field == null) {
            return "null";
        }
        return field.type.name;
    }

    /**
     * Similar to getTypeName() but this one returns a Node.
     *
     * @return itself if "typeName" matches with this.getTypeName() else returns NULL_NODE;
     *
     * Important, Note the type name format should not contain [] and & which are normally used to
     * describe the type.
     *
     * For array types - Eg. INTEGER[] For reference/choice types - Eg. CallEventRecord& For array
     * of references - Eg. CallEventRecord&[]
     *
     */
    public Node confirmType(String typeName) {
        if (field == null) {
            return NULL_NODE;
        }
        if (field.type.name.equals(typeName)) {
            return this;
        } else {
            return NULL_NODE;
        }
    }

    /**
     * @return - The name of the field associated with this Node.
     */
    public String getFieldName() {
        if (field == null) {
            return "null";
        }
        return field.name;
    }

    public boolean isArray() {
        return field.isArray();
    }

    /**
     * @return - String representation of this Node.
     */
    @Override
    public String toString() { // TODO: Print in standard ASN notation for ASNvalues.
        if (field == null) {
            return NULL_NODE_STR;
        }
        return getFieldName() + ":" + getTypeName() + "(" + getValue() + ")";
    }

    /**
     * TODO: check documentation. Getting rid of this method to keep interface lighter and simpler
     *
     * @return - String representing the value of the requested sub field inside this Node. If the
     * requested field is not present in the Node it returns empty string "".
     *
     * public PrimitiveClass getSubField(String subFieldName) { Node subnode = getSubNode(
     * subFieldName); return ( subnode != null ) ? subnode.getValue() : null;
	}
     */
    /**
     * @return sub node corresponding to the subField or NULL_NODE if the subnode is not found.
     */
    public Node getSubNode(String subFieldName) {
        if (field == null) {
            return NULL_NODE;
        }
        for (int i = 0; i < subNodes.length; i++) {
            if (subNodes[i].getFieldName().equals(subFieldName)) {
                return subNodes[i];
            }
        }
        return NULL_NODE;
    }

    /**
     * Similar to getSubNode() except that this method assumes that sub node corresponding to
     * subField is an Array type. and hence it returns an Array of Node.
     *
     * @return - Empty Array if the subnode does not exist. An array of nodes if the corresponding
     * subnode array exists.
     * @throws - ASNException if the subnode is not an array as per Grammar file.
     */
    public Node[] getSubNodeAsArray(String subFieldName) {
        Node node = getSubNode(subFieldName);
        if (node == Node.NULL_NODE) {
            return Node.EMPTY_ARR;
        }
        if (!node.field.type.isArray()) {
            String str = "Error in Node(" + getTypeName() + ").getArray(" + subFieldName + ") \"" 
                    + subFieldName + "\" is not an Array Type";
            throw new ASNException(str);
        }
        return node.subNodes;
    }

    public void toASCII(ASCIIFormattable af, String fileName) {
        if (field == null) {
            return;
        }
        try {
            FileWriter fw = new FileWriter(fileName);
            Node[] recordNodes = af.nodeToRecords(this);

            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < recordNodes.length; i++) {
                String asciiRecord = af.recordToString(recordNodes[i]);
                bw.write(asciiRecord);
            }
            bw.close();
        } catch (IOException e) {
            throw new ASNException("Error while writing to file. File(" + fileName + ") : " 
                    + e.getMessage());
        }
    }

    /**
     * Test Method to scramble/overwrite all data in Memory with zero. But maintains Node's tree
     * structure intact. Not exactly useful method, but here it is anyways. It might give you
     * insight on how to write your own encode method.
     */
    public void scramble() {

        if (field == null) {
            return;
        }

        if (!isArray() && isPrimitive()) {  // If we hit an array of Primitives, let us treat it like composite.			 
            getBlock().scramble();
        } else {
            for (Node n : subNodes) {
                n.scramble();
            }
        }
    }

    /**
     * Writes the Node to file. (Kind of like encoding)
     *
     * @param file
     */
    public void write(File outputFile) {
        if (outputFile == null) {
            return;
        }
        BufferedOutputStream bufferedOutputStream = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

            // ----------- Copy from input to Output stream ----------------
            byte[] b = getBlock().getValue();
            bufferedOutputStream.write(b);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
