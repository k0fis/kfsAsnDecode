package kfs.kfsAsnDecode;

import java.util.ArrayList;

public class Ericsson implements ASCIIFormattable {

    @Override
    public Node[] nodeToRecords(Node rootNode) {
        ArrayList retList = new ArrayList();
        for (int i = 0; i < rootNode.subNodes.length; i++) {
            Node n = rootNode.subNodes[i]; // root CallDataRecord&
            n = n.getSubNodeChoice(); // Type should be
            // uMTSGSMPLMNCallDataRecord OR
            // uMTSGSMPLMNCallDataRecord[]

            if (n.isArray()) { // UMTSGSMPLMNCallDataRecord[]
                for (int j = 0; j < n.subNodes.length; j++) {
                    Node umt = n.subNodes[j]; // UMTSGSMPLMNCallDataRecord
                    Node recType = umt.getSubNode("recordType"); // RecordType
                    retList.add(recType);
                }
            } else { // uMTSGSMPLMNCallDataRecord [0] UMTSGSMPLMNCallDataRecord
                Node recType = n.getSubNode("recordType"); // RecordType
                retList.add(recType);
            }
        }
        Node[] ret = new Node[retList.size()];
        retList.toArray(ret);
        return ret;
    }

    // Assumes only UMTSGSMPLMNCallDataRecord Node is passed.
    @Override
    public String recordToString(Node cdrNode) {
        // RecordType => MO | MT | ...
        cdrNode = cdrNode.isChoice() ? cdrNode.getSubNodeChoice() : cdrNode;
        if (cdrNode == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(cdrNode.getTypeName());//.append(",");

        for (Node nd : cdrNode.subNodes) {
            String v;
            if (nd.isPrimitive()) {
                v = nd.getValue().toString();
            } else {
                v = "[ ";
                for (int j = 0; j < nd.subNodes.length; j++){
                    v += nd.subNodes[j].toString();
                    System.out.println(".");
                }
                v += "]";
            }
            sb.append(",").append(nd.getFieldName()).append("(").append(v).append(")");
            
        }
        /*
        for (int i = 0; i < cdrNode.subNodes.length; i++) {

            String t = cdrNode.subNodes[i].getFieldName();
            String v;
            if (cdrNode.subNodes[i].isPrimitive()) {
                v = cdrNode.subNodes[i].getValue().toString();
            } else {
                v = "[ ";
                for (int j = 0; j < cdrNode.subNodes[i].subNodes.length; j++){
                    v += cdrNode.subNodes[i].subNodes[j].toString();
                    System.out.println(".");
                }
                v += "]";
            }
            sb.append(t).append("(").append(v).append(")");
            if (i + 1 < cdrNode.subNodes.length) {
                sb.append(",");
            }
        }\
        * 
        */
        sb.append("\n");
        return sb.toString();
    }
}
