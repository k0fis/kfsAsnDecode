package kfs.kfsAsnDecode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Util {

    /**
     * Utility method in toStringTree() methods of ASNClass and Field.
     */
    public static String getSpace(int n) {
        String ret = "";
        for (int i = 0; i < n; i++) {
            ret += "\t";
        }
        return ret;
    }

    /**
     *
     * @param filename File to convert to byte[]
     * @return byte[]
     */
    public static byte[] FileToByteArray(String filename) {

        File file = new File(filename);

        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return b;
    }

    // TODO: Is caching really required for this method.
    // Converts a file into an array of Strings and caches it for future requests.
    public static String[] toStringArray(String filename) {

        //if (mapOfFileNameToStringArray.containsKey(filename)) { return mapOfFileNameToStringArray.get(filename); }
        List<String> lines = new ArrayList<String>();

        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            throw new ASNException("Caught FileNotFound Exception:" + e.getMessage());
        } catch (IOException e) {
            throw new ASNException("Caught IOException:" + e.getMessage());
        }

        //String[] linesArr = new String[lines.size()];
        return lines.toArray(new String[lines.size()]);
        //mapOfFileNameToStringArray.put(filename, linesArr);
        //return linesArr;
    }

    /**
     * TODO: check if this is properly used/really needed
     */
    static int[] toIntArray(ArrayList<Integer> arr) {
        int[] rarr = new int[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            rarr[i] = arr.get(i).intValue();

        }
        return rarr;
    }

    /**
     * TODO: check if this is properly used/really needed
     */
    static ArrayInfo[] toBoolArray(ArrayList<ArrayInfo> arr) {
        ArrayInfo[] rarr = new ArrayInfo[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            rarr[i] = arr.get(i);
        }
        return rarr;
    }
    //private static HashMap<String, String[]> mapOfFileNameToStringArray = new HashMap<String, String[]>();

    // Useful in PrimitiveClass methods.
    public static String byteArrayToIA5String(byte[] inByte) {
        if (inByte == null) {
            return "";
        }
        String strByte = "";
        for (int i = 0; i < inByte.length; i++) {
            int intbyte = inByte[i] & (0xff);
            String hexByte = Integer.toString((intbyte & 0xff) + 0x100, 16).substring(1);
            strByte = strByte + hexByte;
        }
        return strByte;
    }

    // TODO: chec if used.
    static int byteArrayToInt(byte[] arr) {
        if (arr == null) {
            throw new ASNException("recieved null byte[]");
        }
        int valueint[] = new int[arr.length];
        int totalValueInt = 0;
        for (int i = 0; i < arr.length; i++) {
            valueint[i] = arr[i] & (0xff);
            totalValueInt = (totalValueInt << 8) + valueint[i];

        }
        return totalValueInt;
    }
    // [2] [0] [3] [8] [8] [8]    offset = 1, valStart = 3, valEnd = 6   
    // [8] [8] [8]    offset = 0, valStart = 0, valEnd = 3
    // should be equivalent to ( arr, 0, arr.length );

    static int byteArrayToInt(byte[] arr, int valStart, int valEnd) {
        if (arr == null) {
            throw new ASNException("recieved null byte[]");
        }
        //int valueint[] = new int[arr.length];
        int totalValueInt = 0;
        for (int i = 0 + valStart; i < valEnd; i++) {
            int n = arr[i] & (0xff);
            totalValueInt = (totalValueInt << 8) + n;

        }
        return totalValueInt;
    }

    // Used in PrimitiveClass
    public static byte[] nibbleSwap(byte[] inByte) {
        if (inByte == null) {
            return null;
        }
        int nibble0;// = new int[inByte.length];
        int nibble1;// = new int[inByte.length];
        byte[] b = new byte[inByte.length];
        for (int i = 0; i < inByte.length; i++) {
            nibble0 = (inByte[i] << 4) & 0xf0;
            nibble1 = (inByte[i] >>> 4) & 0x0f;
            b[i] = (byte) ((nibble0 | nibble1));
        }
        return b;
    }
}
