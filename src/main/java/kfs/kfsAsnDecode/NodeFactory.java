package kfs.kfsAsnDecode;

import java.util.ArrayList;

public class NodeFactory {

    /**
     * Converts a ASN data file to a Node
     */
    public static Node kfsParse(byte [] byteArr, String grammarFile, kfsNodeCallBack cb) {
        Field rootField = ASNClassFactory.getField(grammarFile);
        EBlock rootBlock = new EBlock(0, 0, byteArr, 0, 0, byteArr.length);

        Node topNode = NodeFactory.makeNode(rootField, rootBlock, -1, 0, cb);
        // Assumption is that for all Grammar files the topNode's type is always an array
        // ie.   topNode.getType().isArray() == true
        //return topNode.subNodes;
        return topNode;
    }

    public static Node parse(String dataFile, String grammarFile, kfsNodeCallBack cb) {
        Field rootField = ASNClassFactory.getField(grammarFile);
        byte[] byteArr = Util.FileToByteArray(dataFile);
        EBlock rootBlock = new EBlock(0, 0, byteArr, 0, 0, byteArr.length);

        Node topNode = NodeFactory.makeNode(rootField, rootBlock, -1, 0, cb);
        // Assumption is that for all Grammar files the topNode's type is always an array
        // ie.   topNode.getType().isArray() == true
        //return topNode.subNodes;
        return topNode;
    }
    static byte[] dummyArr = new byte[10];
    static EBlock dummyBlock = new EBlock(0, 8, dummyArr, 0, 2, 10);

    /**
     * Creates a dummy Node associated with given asnField
     */
    public static Node createDummyNode(Field asnField, kfsNodeCallBack cb) {
        Node retNode = new Node(asnField, null, dummyBlock);
        cb.kfsCb(retNode);
        return retNode;
    }

    /**
     * Creates a int from a Block
     */
    static int makeInteger(EBlock b) {
        return Util.byteArrayToInt(b.fileBytes, b.valStart, b.valEnd);
    }

    /**
     * Creates a byte[] from a Block
     */
    static byte[] makeByteArray(EBlock b) {
        return b.getValue();
    }

    // TODO: remove the dummy implementation for this methods.
    static boolean makeBoolean(EBlock b) {
        return true;
    }

    // TODO: remove the dummy implementation for this methods.
    static double makeReal(EBlock b) {
        return 3.1415;
    }

    /**
     * Creates a Node out of a primitive ASNBlock.
     *
     * @param b - The block to be converted to Node.
     * @param asnField - Type information corresponding to the block
     * @throws ASNException - If block b is not a primitive.
     */
    public static Node createPrimitiveNode(final EBlock b, Field asnField, kfsNodeCallBack cb) { // TODO: get appropriate primitive
        if (!b.isPrimitive) {
            throw new ASNException("Defensive check failed");
        }
        if (asnField.type.isArray()) {
            throw new ASNException("Defensive check failed. This is array: " + asnField.longName + ", "+b.getMetaString());
        }

        Node retNode = new Node(asnField, null, b);
        cb.kfsCb(retNode);
        return retNode;
    }

    /**
     * @return - If subBlock is Universal 16/17 and field f has a array subfield then it creates and
     * returns a node else it returns null.
     * @throws ASNException if subBlock is Universal 16/17 but there is no array child field.
     *
     * Q. What if field f has multiple array subfields? A. We are interested only in array subfields
     * with ASNConst.POS_NOT_SPECIFIED There can only be one such array subfield in any ASNClass,
     * otherwise it will lead to ambiguous situation. The code doesn't do any check but simply
     * returns the first occurrence of subfield that is an array subfield and whose pos is
     * ASNConst.POS_NOT_SPECIFIED.
     *
     */
    private static Node makeUniversalSetSeqNode(Field f, EBlock subBlock, int maxBlocks, int depth, kfsNodeCallBack cb) {
        if (subBlock.isUniversalSetSeq()) { // composite | array
            Field farrChild = f.getArrayChildWithoutPos();
            if (farrChild != null) {
                return makeNode(farrChild, subBlock, maxBlocks, depth + 1, cb);
            } else {
                throw new ASNException("" + f + " " + subBlock 
                        + "Field does not have a array subField, but encountered Universal tag 16|17: " + subBlock.toDetailedString(true));
            }
        }
        return null;
    }

    public static Node makeReferenceNode(Field f, Node referencedNode, kfsNodeCallBack cb) {
        Node[] subNodes = new Node[1];
        subNodes[0] = referencedNode;
        Node retNode = new Node(f, subNodes, null);
        cb.kfsCb(retNode);
        //retNode.isChoice = true; | commented as a part of Bug fix #1
        return retNode;
    }
    public static int numCalls = 0;

    public static Node makeNode(Field f, EBlock b, int maxBlocks, int depth, kfsNodeCallBack cb) {
        numCalls++;
        if (numCalls > 646500) {
            //System.out.printf("\n %d makeNode("+f + "," + b + ")", numCalls );
        }
        //System.out.printf("\n makeNode("+f + "," + b + ")" );
        // Recursive makeNode(f,b)
        
        Node[] subNodes = null;
        try {
            if (b.isLeaf()) {
                try {
                    return createPrimitiveNode(b, f, cb);
                } catch (OutOfMemoryError e) {
                    System.out.printf("\n ### %s ", e.getMessage());
                    String s = "Error in createPrimitive() " + f + " " + b + " : ";
                    throw new ASNException("makeNode", s + e.getMessage());
                }
            }
            if (f.isArray()) {
                return makeNodeArray(f, b, maxBlocks, depth, cb);
            }
            // ==== Composite and Non Array ==== 
            ArrayList<EBlock> subBlocks = b.getSubBlocks(EBlock.MAX_BLOCKS, 0, (byte) 0); // Non Arrays don't worry about Fixed Blocks.		
            subNodes = new Node[subBlocks.size()];

            for (int i = 0; i < subBlocks.size(); i++) {
                EBlock subBlock = subBlocks.get(i);
                Node univSetSeqNode = makeUniversalSetSeqNode(f, subBlock, maxBlocks, depth, cb);
                if (univSetSeqNode != null) {
                    subNodes[i] = univSetSeqNode;
                    continue;
                }
                int sbpos = subBlock.tag;
                Field childField = f.getChildField(sbpos);
                if (childField != null) {
                    subNodes[i] = makeNode(childField, subBlock, maxBlocks, depth + 1, cb);
                } else { // -- composite | not array | choice
                    Field[] retFieldArr = f.getGrandChildField(sbpos);
                    if (retFieldArr == null) {
                        String str = "Unable to find child field or grandchild field for given tag. subBlock.tag(" + sbpos + ") field(" + f + ")";
                        throw new ASNException("makeNode", str);
                    } else {
                        childField = retFieldArr[0];
                        Field grandChildField = retFieldArr[1];
                        Node grandChildNode = makeNode(grandChildField, subBlock, maxBlocks, depth + 1, cb);
                        Node childNode = makeReferenceNode(childField, grandChildNode, cb);
                        subNodes[i] = childNode;
                    }
                }
            } // for ( subBlocks ) 
        } catch (ASNException e) { // Catch Exception if thrown by recursive makeNode(), append stack Info and re throw. 
            if (e.isType("makeNode")) {
                throw new ASNException("makeNode", "\n makeNode(" + f + "," + b + ") " + e.getMessage());
            }
            if (e.isType("makeNodeArray")) {
                throw new ASNException("makeNodeArray", "\n makeNodeArray(" + f + "," + b + ") " + e.getMessage());
            }
            throw e;
        }
        Node n = new Node(f, subNodes, b);
        cb.kfsCb(n);
        return n;
    }

    private static Node makeNodeArray(Field f, EBlock b, int maxBlocks, int depth, kfsNodeCallBack cb) {


        ArrayList<EBlock> subBlocks = b.getSubBlocks(EBlock.MAX_BLOCKS, f.type.blockSize, f.type.paddingByte);
        Node[] subNodes = new Node[subBlocks.size()];

        if (f.isReference()) { // Heterogeneous Array
            for (int i = 0; i < subBlocks.size(); i++) {
                EBlock subBlock = subBlocks.get(i);
                Node univSetSeqNode = makeUniversalSetSeqNode(f, subBlock, maxBlocks, depth, cb);
                if (univSetSeqNode != null) {
                    subNodes[i] = univSetSeqNode;
                    continue;
                }
                int sbpos = subBlock.tag;
                Field refChildField = f.getChildField(sbpos);
                if (refChildField == null) {
                    throw new ASNException("makeNodeArray", "No child field at pos 'sbpos' for Reference Array.");
                } else {
                    Node refChildNode = NodeFactory.makeNode(refChildField, subBlock, maxBlocks, depth + 1, cb);
                    Field f_nav = f.getCachedCloneNoneArray();
                    Node childNode = makeReferenceNode(f_nav, refChildNode, cb);
                    subNodes[i] = childNode;
                }
            }
        } else { // Homogeneous Array
            Field f_nav = f.getCachedCloneNoneArray();
            for (int i = 0; i < subBlocks.size(); i++) {
                EBlock subBlock = subBlocks.get(i);
                subNodes[i] = makeNode(f_nav, subBlock, maxBlocks, depth + 1, cb);

                // ------------------Defensive Checks ------------------
                if (f_nav.type.isAssociatedWithTag()) { // Array of Primitives ??
                    if (subBlock.isUniversalSetSeq()) {
                        throw new ASNException("makeNodeArray", "Unexpected Universal 16|17 Tag ");
                    }
                } else { // Array of Composite
                    //int sbpos = subBlock.tag;
                    if (!subBlock.isUniversalSetSeq()) { // Furthermore the subBlock.tag = 16 or 17 based on f_nav.relation (SEQ/SET) 
                        throw new ASNException("makeNodeArray", "!subBlock.isUniversalSetSeq()");
                    }
                } // -------------End of Defensive checks------------				
            }
        }
        Node retNode = new Node(f, subNodes, b);
        cb.kfsCb(retNode);
        return retNode;
    }
}
