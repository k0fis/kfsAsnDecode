package kfs.kfsAsnDecode;

/**
 *
 * Enables the Node class to be able to convert itself to CSV.
 *
 * The information to build a ASCIIFormattable object will typically come from a config file.
 *
 * Eg. The Mapping logic of individual fields. Notion of a Record in the Node.
 *
 */
public interface ASCIIFormattable {

    public Node[] nodeToRecords(Node rootNode);

    public String recordToString(Node recordNode);
}
