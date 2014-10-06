package kfs.asn.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kfs.asn.ASNCls;

public class AsnUtil {

    
    public static String getSpace(int n) {
        String ret = "";
        for (int i = 0; i < n; i++) {
            ret += "\t";
        }
        return ret;
    }
    public static int[] toIntArray(ArrayList<Integer> arr) {
        int[] rarr = new int[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            rarr[i] = arr.get(i).intValue();

        }
        return rarr;
    }
    public static ArrayInfo[] toBoolArray(ArrayList<ArrayInfo> arr) {
        ArrayInfo[] rarr = new ArrayInfo[arr.size()];

        for (int i = 0; i < arr.size(); i++) {
            rarr[i] = arr.get(i);
        }
        return rarr;
    }
    public static String[] toStringArray(String filename) {
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
        return lines.toArray(new String[lines.size()]);
    }    
    public static String[] toStringArray(InputStream filename) {
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
        return lines.toArray(new String[lines.size()]);
    }

    
    
    
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
