package kfs.kfsAsnDecode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author pavedrim
 */
public class kfsTest /*
 * extends kfsADb
 */ {
/*
    
    private final kfsMSOriginating mso;
    private final kfsMSTerminating mst;
    private final kfsTransit tran;
    private final kfsCallForwarding cfw;
*/
    public kfsTest( /* kfsDbServerType dbt
             * String schema, kfsDbServerType dbt, Connection con
             */) {
        //super(schema, dbt, con);
        /*
        mso = new kfsMSOriginating(dbt);
        mst = new kfsMSTerminating(dbt);
        tran = new kfsTransit(dbt);
        cfw = new kfsCallForwarding(dbt);
        */
    }
/*
    //@Override
    protected Collection<kfsDbiTable> getDbObjects() {
        return Arrays.<kfsDbiTable>asList(mso, mst, tran);
    }
    private static final String ss1 = "mSOriginating";
    private static final String ss2 = "mSTerminating";
    private static final String ss3 = "transit";
    private static final String ss4 = "callForwarding";
    private static final String inputDataFileC = "dist/data.bin.cmp";
    private static final String inputDataFile = "dist/data.bin";
    private static final String inputGrammarFile = "/home/pavedrim/src/kfsMscCdr/dist/Ericsson.txt";

    public static void main(String[] aa) throws Exception {
        testRead();
    }

    public static String getCapitalize(String inp) {
        return inp.substring(0, 1).toUpperCase() + inp.substring(1);
    }
    */ 
/*
    private static void pako2() throws Exception {
        java.io.BufferedOutputStream outStream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(inputDataFile));
        ZipFile zipFile = new ZipFile(inputDataFileC);
        Enumeration entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (!entry.isDirectory()) {
                System.out.println(entry.getName());
                InputStream in = zipFile.getInputStream(entry);
                IOUtils.copy(in, outStream);
                outStream.flush();
                outStream.close();
            }
        }
        zipFile.close();
*/
        /*
         * java.io.File inFile = new java.io.File(inputDataFileC); //java.io.File outFile = new
         * java.io.File(inputDataFile);
         *
         * //java.io.BufferedOutputStream outStream = new java.io.BufferedOutputStream(new
         * java.io.FileOutputStream(outFile));
         *
         * MyRandomAccessFile istream = new MyRandomAccessFile(inputDataFileC, "r");
         *
         * IInArchive archive = new Handler();
         *
         * int ret = archive.Open(istream);
         *
         * Vector<String> listOfNames = new Vector<String>();
         *
         * J7zip.testOrExtract(archive,listOfNames,IInArchive.NExtract_NAskMode_kExtract);
         *
         *
         * //outStream.flush(); //outStream.close(); //inStream.close();
         */
    //}
/*
    private static void testRead() throws Exception {
        final kfsTest tst = new kfsTest(kfsDbServerType.kfsDbiOracle);

        Node rootNode = NodeFactory.parse(inputDataFile, inputGrammarFile, new kfsNodeCallBack() {

            @Override
            public void kfsCb(Node cdrNode) {

                String s = cdrNode.getFieldName();
                ISetDataOnAsnPos bi = null;
                if (ss1.equals(s)) {
                    bi = tst.mso.getPojo();
                } else if (ss2.equals(s)) {
                    bi = tst.mst.getPojo();
                } else if (ss3.equals(s)) {
                    bi = tst.tran.getPojo();
                } else if (ss4.equals(s)) {
                    bi = tst.cfw.getPojo();
                    System.out.print(".");
                } else {
                    //System.err.println(s);
                }
                if (bi != null) {
                    for (Node nd : cdrNode.subNodes) {
                        if (nd.isPrimitive()) {
                            bi.setDataOnAsnPos(nd.field.pos, nd.getValue());
                        } else {
                            int ppr = nd.field.pos * 1000;
                            for (int j = 0; j < nd.subNodes.length; j++) {
                                bi.setDataOnAsnPos(ppr + nd.subNodes[j].field.pos, nd.subNodes[j].getValue());
                            }
                        }
                    }
                    if (ss4.equals(s)) {
                        kfsCallForwarding.pojo pj = (kfsCallForwarding.pojo)bi;
                        System.out.println(Arrays.toString(pj.kfsGetRow().getContent()));
                    }
                }
            }
        });

    }
*/
    public static void createDbFile(String inputGrammarFile) throws IOException {
        File f = new File(inputGrammarFile);
        if (!f.exists()) {
            System.out.println("Cannot open file:  " + inputGrammarFile);

        } else {
            Field ff = ASNClassFactory.getField(new kfsGramarFile(inputGrammarFile));

            for (int ii : new int[]{}) {
                ArrayList<String> s = new ArrayList<String>();
                kfsCreateClass kcc = new kfsCreateClass(ff.getChildField(0).getChildField(-1).getChildField(ii).name, "kfs.kfsCdrLoader");
                ff.getChildField(0).getChildField(-1).getChildField(ii).type.getSubNames("", 0, kcc);
                FileWriter fw = new FileWriter("src/kfs/kfsCdrLoader/t_" + kcc.clsName + ".java");
                fw.write(kcc.toString());
                fw.close();
            }
        }
    }
}
