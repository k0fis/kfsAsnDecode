package kfs.kfsAsnDecode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is simple tool for assemble names by some conventions.
 * Extends ArrayList&lt;String&gt;
 *
 * @author pavedrim
 */
public class kfsNames extends ArrayList<String> {

    /**
     * add new item - and split this text by specified space and really
     * space (" ")
     * @param inp - input name
     * @param space - defined space - for example "_"
     * @return this - return itself for concatenate method calling
     */
    public kfsNames add(String inp, String space) {
        ArrayList<String> sa = new ArrayList<String>();
        ArrayList<String> sb = new ArrayList<String>();
        sa.addAll(Arrays.asList(inp.split(space)));
        for (String ch : new String[]{" ", ",", ";", "\\(", "\\)"}) {
            for (String g : sa) {
                sb.addAll(Arrays.asList(g.split(ch.toString())));
            }
            sa.clear();
            sa.addAll(sb);
            sb.clear();
        }
        for (String s : sa) {
            super.add(s);
        }
        return this;
    }

    @Override
    public boolean add(String e) {
        add(e, " ");
        return true;
    }

    /**
     * create java name for names with prefix - especialy for getter/setter
     * @param prefix
     * @return
     */
    public String getJavoizeName(String prefix) {
        return prefix.toLowerCase() + getCapitalizeName();
    }

    /**
     * Create Name with javoize convetions
     * @return javoized name
     */
    public String getJavoizeName() {
        kfsSb sb = new kfsSb();
        boolean f = true;
        for (String s : this) {
            if ((s != null) && (s.length() > 0)) {
                if (f) {
                    f = false;
                    sb.a(s.toLowerCase());
                } else {
                    sb.a(getCapitalize(s));
                }
            }
        }
        return sb.toString().trim();
    }

    /**
     * Create name with oracle SQL convetions
     * @return name in oracle convetions
     */
    public String getOraclizeName() {
        kfsSb sb = new kfsSb();
        boolean f = true;
        for (String s : this) {
            if (f) {
                f = false;
            } else {
                sb.a("_");
            }
            sb.a(s.toUpperCase());
        }
        return sb.toString();
    }

    public String getCapitalizeName(String space) {
        kfsSb sb = new kfsSb();
        boolean f = true;
        for (String s : this) {
            if (f) {
                f = false;
            } else {
                sb.a(space);
            }
            sb.a(getCapitalize(s));
        }
        return sb.toString();
    }

    public String getCapitalizeName() {
        kfsSb sb = new kfsSb();
        for (String s : this) {
            sb.a(getCapitalize(s));
        }
        return sb.toString();
    }

    public static String getCapitalize(String s) {
        return (s.length() > 1)? s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase():s.toUpperCase();
    }
}
