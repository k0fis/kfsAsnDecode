package kfs.kfsAsnDecode;

import kfs.asn.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsnUtil {

    /**
     * Utility method in toStringTree() methods of ASNClass and Field.
     *
     * @param n
     * @return
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
            throw new ASNException(null, "File Not Found.", e);
        } catch (IOException e) {
            throw new ASNException(null, "Error Reading The File.", e);
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
            throw new ASNException(null, "Caught FileNotFound Exception", e);
        } catch (IOException e) {
            throw new ASNException(null, "Caught IOException", e);
        }

        //String[] linesArr = new String[lines.size()];
        return lines.toArray(new String[lines.size()]);
        //mapOfFileNameToStringArray.put(filename, linesArr);
        //return linesArr;
    }

    public static String[] toStringArray(InputStream filename) {

        //if (mapOfFileNameToStringArray.containsKey(filename)) { return mapOfFileNameToStringArray.get(filename); }
        List<String> lines = new ArrayList<String>();

        try {
            InputStreamReader fileReader = new InputStreamReader(filename);
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
     * @param arr
     * @return 
     */
    public static int[] toIntArray(ArrayList<Integer> arr) {
        int[] rarr = new int[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            rarr[i] = arr.get(i).intValue();

        }
        return rarr;
    }

    /**
     * TODO: check if this is properly used/really needed
     * @param arr
     * @return 
     */
    public static ArrayInfo[] toBoolArray(ArrayList<ArrayInfo> arr) {
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
    public static int byteArrayToInt(byte[] arr) {
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

    public static int byteArrayToInt(byte[] arr, int valStart, int valEnd) {
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

    public static String[] getNamesFromJavaName(String name) {
        return name.replace("-", "").split("(?=\\p{Lu})");
    }

    public static CharSequence getDbName(String[] names) {
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        int ll = 0;
        int al = 0;
        for (String s : names) {
            al = s.length();
            if (f) {
                f = false;
            } else {
                if ((al > 1) || ((al == 1) && (ll > 1))) {
                    sb.append("_");
                }
            }
            sb.append(s.toUpperCase());
            ll = al;
        }
        return sb;
    }

    public static CharSequence getHumanName(String[] names) {
        return getHumanName(names, " ");
    }

    public static CharSequence getHumanName(String[] names, String space) {
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        int al, ll = 0;
        for (String s : names) {
            al = s.length();
            if (f) {
                f = false;
            } else {
                if ((al > 1) || ((al == 1) && (ll > 1))) {
                    sb.append(space);
                }
            }
            sb.append(getCapitalize(s));
            ll = al;
        }
        return sb;
    }

    public static CharSequence getJavaName(String[] names) {
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        for (String s : names) {
            if (f) {
                f = false;
                sb.append(s.toLowerCase());
            } else {
                sb.append(getCapitalize(s));
            }
        }
        return sb;
    }

    public static CharSequence getCapitalize(String inp) {
        StringBuilder sb = new StringBuilder();
        if (inp != null) {
            if (inp.length() > 0) {
                sb.append(inp.substring(0, 1).toUpperCase());
                if (inp.length() > 1) {
                    sb.append(inp.substring(1).toLowerCase());
                }
            }
        }
        return sb;
    }

    public static Map<String, Class> getNodeClassMap(Class... classes) {
        HashMap<String, Class> ret = new HashMap<String, Class>(classes.length);
        for (Class cls : classes) {
            ASNCls asnCls = (ASNCls) cls.getAnnotation(ASNCls.class);
            if (asnCls != null) {
                ret.put(asnCls.value(), cls);
            }
        }
        return ret;
    }

    public static CharSequence getIp(byte[] ip) {
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        if (ip != null) {
            for (byte b : ip) {
                if (f) {
                    f = false;
                } else {
                    sb.append(".");
                }
                int num = b & 0xFF;
                sb.append(num);
            }
        }
        return sb;
    }

    private final static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String byteToHex(byte v) {
        return "" + hexArray[v >>> 4] + hexArray[v & 0x0F];
    }

    public static CharSequence bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            sb.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
        }
        return sb;
    }

    public static CharSequence bytesToHexSwapHL(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            sb.append(hexArray[v & 0x0F]).append(hexArray[v >>> 4]);
        }
        return sb;
    }

    public static CharSequence removeLastF(CharSequence s) {
        while ((s.length() > 0) && (s.charAt(s.length() - 1) == 'F')) {
            s = s.subSequence(0, s.length() - 2);
        }
        return s;
    }
}
