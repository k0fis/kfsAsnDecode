package kfs.asn.utils;

import java.util.Map;

/**
 * Encapsulates ASNClass defined in a ASN Grammar File. Like regular Class in real world, ASNClass
 * can be defined recursively.
 *
 */
public class ASNClass {

    private boolean isIndicator;
    
    public boolean isIndicator() {
        return isIndicator;
    }
    
    public ASNClass setIndicator(boolean  indi) {
        this.isIndicator = indi;
        return this;
    }
    
    /**
     * Non zero value indicates a fixed block. By default this is zero. Typically fixed block sizes
     * are either 1024 or 2048 or
     */
    public int blockSize = 0;
    /**
     * Applicable only if blockSize is non-zero. Typically padding bytes are '0xFF' or '0x20'
     */
    public byte paddingByte = (byte) 0;
    /**
     * Name of the ASNClass. If the ASNClass is a primitive then the name contains primitive name.
     */
    public String name;
    /**
     * Class Relation. Can have one of the following values - RELATION_CHOICE, RELATION_SEQUENCE,
     * RELATION_SET If primitive class, the relation is null.
     *
     * Relation field is relevant (has meaningful data) only when the ASNClass is not an array. SET
     * OF/SEQUENCE OF information can be found using arrInfo property.
     *
     * Default access privileges given so that it is visible to ASNClassFactory
     *
     */
    String relation;

    public boolean isSequence() {
        return !isArray() && relation.equals(AsnConst.RELATION_SEQUENCE);
    }

    public boolean isSet() {
        return !isArray() && relation.equals(AsnConst.RELATION_SET);
    }
    /**
     * Contains Array information for this ASNClass
     */
    ArrayInfo arrInfo = new ArrayInfo(0, 0);

    public boolean isArray() {
        return arrInfo.isArray();
    }

    /**
     * Contains Reference information for this ASNClass This typically happens if the ASNClass has
     * CHOICE relation.
     *
     */
    public boolean isReference() {
        return (relation != null && relation.equals(AsnConst.RELATION_CHOICE));
    }
    /**
     * If isPrimitive, it contains the name of primitive ASNClass Primitive Class else it is null.
     *
     */
    private String isPrimitive;
    /**
     * Every ASNClass is associated with a Tag. All Primitive types are associated with a tag as per
     * ASN standard. If the class is an Primitive type, then its corresponding tag is available.
     * Composite types may be associated with a Tag depending on their definition in the grammar
     * file.
     */
    int associatedTag = NO_ASSOCIATED_TAG;
    final static int NO_ASSOCIATED_TAG = -1;
    /**
     * Fields of the ASNClass
     */
    public Field[] fields;

    /**
     * @return true if the ASNClass is a primitive.
     */
    public boolean isPrimitive() {
        return isPrimitive == null ? false : true;
    }

    /**
     * @return The name of ASN Primitive class if the class is a primitive, else returns null;
     */
    public String getPrimitiveName() {
        return isPrimitive;
    }

    // Setter for isPrimitive
    void setPrimitiveName(String primitiveName) {
        isPrimitive = primitiveName;
    }

    /**
     * returns the cloned instance of ASNClass
     */
    @Override
    public ASNClass clone() {
        ASNClass ret = new ASNClass();
        ret.name = this.name;
        ret.relation = this.relation;
        ret.arrInfo = this.arrInfo.clone();

        ret.isPrimitive = this.isPrimitive;
        ret.associatedTag = this.associatedTag;
        if (fields == null) {
            ret.fields = null;
        } else {
            ret.fields = new Field[fields.length];
            for (int i = 0; i < fields.length; i++) {
                ret.fields[i] = this.fields[i].clone();
            }
        }
        return ret;
    }

    /**
     * Returns the array version of this ASNClass. Example - If this ASNClass is 'INTEGER' then it
     * is converted to 'INTEGER[]'
     *
     * Note the concept of Array is built within ASNClass.
     */
    public void toArray() {
        if (arrInfo.isArray()) {
            throw new ASNException("Array of Arrays not supported.");
        }
        ArrayInfo arrClass = this.arrInfo;
        arrClass.setArray(true);
    }

    /**
     *
     * @return Cloned instance, which is not a array type.
     */
    public ASNClass getNonArrayClone() {
        ASNClass ret = this.clone();
        ret.arrInfo.setArray(false);
        return ret;
    }

    /**
     * @return true if this ASNClass is not associated with Tag.
     */
    public boolean isAssociatedWithTag() {
        return (associatedTag != NO_ASSOCIATED_TAG);
    }
    
    public boolean isEnumerated() {
        return associatedTag == AsnConst.TAG_ENUMERATED;
    }

    /**
     *
     * @return associated tag of the ASNClass. If there is no associated tag returns
     * NO_ASSOCIATED_TAG.
     */
    int getAssociatedTag() {
        int retTag;

        if (isAssociatedWithTag()) {
            if (isPrimitive != null) {
                Map<String, Integer> map = AsnConst.getPrimitiveMap();
                retTag = map.get(name);
            } else {
                retTag = associatedTag;
            }
        } else {
            retTag = NO_ASSOCIATED_TAG;
        }
        return retTag;
    }

    // Default Constructor
    // TODO: Check what should be the ideal constructor.
    ASNClass() {
    }

    ASNClass(String name_, String relation_, Field[] fields_) {
        name = name_;
        relation = relation_;
        fields = fields_;
    }

    /*
     * Gets the complete name including array and reference information
     */
    public String getName() {
        return this.name + (isReference() ? "&" : "") + (isArray() ? "[]" : "");
    }

    //TODO: Improve this return String format (readability). Add conciseString() if required.
    @Override
    public String toString() {
        String ret = "";
        if (this.fields != null) {
            ret += "\n[ASNClass] Name=" + name + ",isPrimitive=" + isPrimitive + ",relation=" + relation + ",children=" + fields.length + "";
        }
        return ret;
    }
 
    public String toStringTree(int depth) { // For ASNClass
        String ret = "";
        int numChildren = 0;
        if (depth == 0) {
            if (this.fields != null) {
                numChildren = fields.length;
            }
            ret += "\n[ASNClass] Name=" + name + ",isPrimitive=" + isPrimitive + ",relation=" + relation + ",children=" + numChildren + "{";
        }

        String assocTag = (this.associatedTag == -1) ? "" : "[" + associatedTag + "]";
        ret = ret + "\n" + AsnUtil.getSpace(depth) + getName() + " :=" + assocTag + " " + relation + " {";
        if (fields == null) // if there are no sub fields
        {
            ret = ret + "}";
            return ret;
        }
        // Ok lets print the sub fields recursively.
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].pos == -1) {
                ret += fields[i].toStringTree(depth);
            } else {
                ret += fields[i].toStringTree(depth + 1);
            }
        }
        ret += "\n" + AsnUtil.getSpace(depth) + "}";
        return ret;

    }

    // ---------------------------------------------------------------------------------------------------------------
    // Return the child Class =======================  Changed
    // given a tag returns corresponding child Field inside ASNClass
    Field getChild(int tag) {
        if (!arrInfo.isArray()) {
            Field asnField = getFieldWithPos(tag);
            String fieldName = asnField == null ? "null" : (asnField.type.getName() + " " + asnField.name);
            //System.out.printf("\n %s is searching for pos %d : Found %s",getName(),tag, fieldName );
            if (asnField == null) { // check embedded case				
                asnField = getEmbeddedFieldWithPos(tag);
                fieldName = asnField == null ? "null" : (asnField.type.getName() + " " + asnField.name);
                //System.out.printf("\n %s is searching in embeddedFields for pos %d : Found %s",getName(),tag, fieldName );
            }
            return asnField; // == null ? null : asnField.type;
        } else {
            // TODO: check if all scenarios are handled.
            Field asnField = getFieldWithPos(tag);
            String fieldName = asnField == null ? "null" : (asnField.type.getName() + " " + asnField.name);
            // System.out.printf("\n %s is searching for pos %d : Found %s",getName(),tag, fieldName );
            // There is no concept of embedded in array.
            return asnField; // == null ? null : asnField.type;
            //throw new ASNException("Not yet handled this..");
        }
    }

    Field getFieldWithPos(int position_) {
        if (isPrimitive != null) { // if this is a primitive
            return null;
            //throw new ASNException("Cannot get subfield's position for a Primitive Type");
        }
        if (position_ != 6) { // Optimization for Tag 6 Object Identifier.
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].pos == position_) {
                    return fields[i];
                }
            }
        } else {
            int emptyPos = -1;
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].pos == position_) {
                    return fields[i];
                }
                if (fields[i].pos < 0) {
                    emptyPos = i;
                }
            }
            if (emptyPos != -1) { // We hit an empty Pos
                //System.out.println("Here...OBJECT IDENTIFIER");
                if (fields[emptyPos].type.name.equals("OBJECT IDENTIFIER")) {
                    return fields[emptyPos];
                }
            }
        }
        return null;
        //throw new ASNException("Unable to find subfield's position " + position + " inside ASNClass \"" + this.name + "\"" );
    }

    // Check all embedded classes to see if any of them has given '_pos'
    // if found return that child's index in childarr, else return -1
    Field getEmbeddedFieldWithPos(int _pos) {
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].pos == -1) {
                // Got a possible candidate child to explore and see
                Field childField = fields[i].type.getFieldWithPos(_pos);
                //System.out.printf("\n embedded class: %s  %s %d",childField.type.getName(), childField.name, childField.pos);
                if (childField != null) {
                    return childField;
                }
            }
        }

        return null;
    }

    Field getArrayFieldWithPos(int pos) {
        return null;
    }
} 
