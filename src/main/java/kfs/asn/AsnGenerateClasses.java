package kfs.asn;

import kfs.asn.utils.AsnConst;
import kfs.asn.utils.AsnUtil;
import kfs.asn.utils.ASNClass;
import kfs.asn.utils.ASNClassFactory;
import kfs.asn.utils.Field;
import kfs.asn.utils.GramarFile;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavedrim
 */
public class AsnGenerateClasses {

    private final String asnName;
    private final String className;
    private final String javaName;
    private final String humanName;
    private final String dbName;
    private final ArrayList<AsnGenerateClasses> classes;
    private final StringBuilder output;

    public AsnGenerateClasses(String packageName, ASNClass cls/*, kfsCreatePojoClass master*/) {
        this.classes = new ArrayList<AsnGenerateClasses>();
        this.asnName = cls.name;
        String[] names = AsnUtil.getNamesFromJavaName(asnName);
        this.humanName = AsnUtil.getHumanName(names).toString();
        this.dbName = AsnUtil.getDbName(names).toString();
        this.className = AsnUtil.getHumanName(names, "").toString();
        this.javaName = AsnUtil.getJavaName(names).toString();

        output = new StringBuilder().append(getClassString(packageName, cls/*, master*/));
    }

    private CharSequence getClassString(String packageName, ASNClass cls/*, kfsCreatePojoClass master*/) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sg = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import java.util.Arrays;\n");
        sb.append("import java.util.List;\n");
        sb.append("import kfs.asn.ASNCls;\n");
        sb.append("import kfs.asn.ASNDef;\n");
        sb.append("import kfs.asn.AsnData;\n");
        sb.append("import javax.persistence.GeneratedValue;\n");
        sb.append("import javax.persistence.GenerationType;\n");
        sb.append("import javax.persistence.Id;\n");
        sb.append("import javax.persistence.Column;\n");
        sb.append("import javax.persistence.OneToMany;\n");
        sb.append("\n/**\n *\n * @author Kofis\n */\n");
        sb.append("@ASNCls(\"").append(cls.name).append("\")\n");
        sb.append("public class ").append(className).append(" {\n\n");

        sb.append("    @Id\n");
        sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        sb.append("    private long id;\n\n");
        sg.append("    public long getId() {\n"
                + "        return id;\n"
                + "    }\n"
                + "\n"
                + "    public void setId(long id) {\n"
                + "        this.id = id;\n"
                + "    }\n"
                + "");
        /*
         if (master != null) {
         sb.append("    @ManyToOne\n");
         sb.append("    private ").append(master.className).append(" ").append(master.javaName).append(";\n\n");
         }
         */
        int maxJavaNameLength = 0;

        if (cls.fields != null) {
            for (Field field : cls.fields) {
                String[] names = AsnUtil.getNamesFromJavaName(field.name);
                String javaVarName = AsnUtil.getJavaName(names).toString();
                String sgName = javaVarName.substring(0, 1).toUpperCase() + javaVarName.substring(1);
                maxJavaNameLength = Math.max(maxJavaNameLength, javaVarName.length());
                sb.append("    @ASNDef(asnPos = ").append(field.pos).append(", asnName = \"")//
                        .append(field.name).append("\", label=\"").append(AsnUtil.getHumanName(names))//
                        .append("\", asnType = \"").append(field.type.name).append("\" )\n");
                if (AsnConst.isPrimitive(field.type.name)) {
                    if ("servingNodeType".equals(field.name)) {
                        System.out.println(field.toConciseString() + " " + field.type.isSequence());
                    }
                    Class varCls = AsnConst.getPrimitiveClass(field.type.name);
                    if (String.class.equals(varCls)) {
                        sb.append("    @Column(length = 50, name = \"").append(AsnUtil.getDbName(names)).append("\")\n");
                    } else {
                        sb.append("    @Column(name = \"").append(AsnUtil.getDbName(names)).append("\")\n");
                    }
                    sb.append("    private ").append(varCls.getSimpleName())//
                            .append(" ").append(javaVarName).append(";\n\n");

                    sg.append(getGetterSetter(varCls.getSimpleName(), sgName, javaVarName, true, false));

                } else {
                    AsnGenerateClasses inCls = new AsnGenerateClasses(packageName, field.type);
                    sb.append("    @OneToMany\n");
                    if (field.isArray() || field.type.isSet() || field.type.isSequence()) {
                        sb.append("    private List<").append(inCls.className).append("> ").append(javaVarName).append(";\n\n");
                        sg.append(getGetterSetter("List<" + inCls.className + ">", sgName, javaVarName, false, true));
                    } else {
                        sb.append("    private ").append(inCls.className).append(" ").append(javaVarName).append(";\n\n");
                        sg.append(getGetterSetter(inCls.className, sgName, javaVarName, false, false));
                    }
                    classes.add(inCls);

                }
            }
        }
        sb.append("    @Override\n");
        sb.append("    public String toString() {\n");
        sb.append("        StringBuilder sb = new StringBuilder();\n");
        sb.append("        sb.append(getClass().getSimpleName()).append(\" {\\n\");\n");

        if (cls.fields != null) {
            for (Field field : cls.fields) {
                String[] names = AsnUtil.getNamesFromJavaName(field.name);
                String javaVarName = AsnUtil.getJavaName(names).toString();
                if (AsnConst.isPrimitive(field.type.name) || (!field.isArray() && !field.type.isSet() && !field.type.isSequence())) {
                    sb.append("        sb.append(\"  ").append(javaVarName)//
                            .append(AsnUtil.getSpace(maxJavaNameLength - javaVarName.length() + 1," "))//
                            .append(" = \").append(").append(javaVarName).append(").append(\"\\n\");\n");
                } else {
                    sb.append("        sb.append(\"  ").append(javaVarName)//
                            .append(AsnUtil.getSpace(maxJavaNameLength - javaVarName.length() + 1, " "))//
                            .append(" = \").append(Arrays.deepToString(").append(javaVarName).append(".toArray())).append(\"\\n\");\n");
                }
            }
        }
        sb.append("        sb.append(\"}\\n\");\n");
        sb.append("        return sb.toString();\n");
        sb.append("    }\n\n");
        sb.append(sg);
        sb.append("\n}\n");
        return sb;
    }

    private CharSequence getGetterSetter(String className, String sgName, String javaVarName, boolean pri, boolean list) {
        StringBuilder sb = new StringBuilder()//
                .append("    public ").append(className).append(" get")//
                .append(sgName).append("() { \n")//
                .append("        return ").append(javaVarName)//
                .append(";\n    }\n\n")//
                .append("    public void set")//
                .append(sgName).append("(").append(className).append(" ")//
                .append(javaVarName).append(" ) { \n")//
                .append("        this.").append(javaVarName).append(" = ")//
                .append(javaVarName).append(";\n")//
                .append("    }\n\n");
        if (pri) {
            sb
                    .append("//    public void set").append(sgName).append("(AsnData ").append(javaVarName).append(" ) { \n")
                    .append("//        this.").append(javaVarName).append(" = ").append(javaVarName).append(".getByteArray().toString();\n")
                    .append("//    }\n\n");
        }
        if (list) {
            sb
                    .append("    public void set").append(sgName).append("Asn (").append(className).append(" ").append(javaVarName).append(" ) { \n")
                    .append("        ArrayList al = new ArrayList(").append(javaVarName).append(".size());\n")
                    .append("        for (Object o : ").append(javaVarName).append(") {\n")
                    .append("            if (o instanceof AsnData)\n")
                    .append("//                al.add(AsnUtil.getIp(((AsnData)o).getByteArray()));\n")
                    .append("                al.add(((AsnData)o).getData(String.class));\n")
                    .append("            else\n")
                    .append("                System.err.println(o.getClass().getSimpleName());\n")
                    .append("        }\n")
                    .append("        this.").append(javaVarName).append(" = al;\n")
                    .append("//    }\n\n");
        }

        return sb;
    }

    public static void save(String dir, String packageName, GramarFile gramar) throws IOException {
        Field ff = new ASNClassFactory(gramar).getField();

        AsnGenerateClasses cc = new AsnGenerateClasses(packageName, ff.type);
        ArrayDeque<AsnGenerateClasses> al = new ArrayDeque<AsnGenerateClasses>();
        al.add(cc);
        while (!al.isEmpty()) {
            cc = al.pop();
            al.addAll(cc.innerClasses());
            cc.save(dir);
        }

    }

    public void save(String dir) throws IOException {
        FileWriter fw = new FileWriter(new File(dir, className + ".java"));
        fw.append(output);
        fw.flush();
        fw.close();
    }

    @Override
    public String toString() {
        return output.toString();
    }

    public String getAsnName() {
        return asnName;
    }

    public String getClassName() {
        return className;
    }

    public String getHumanName() {
        return humanName;
    }

    public String getDbName() {
        return dbName;
    }

    public List<AsnGenerateClasses> innerClasses() {
        return classes;
    }

    public String getJavaName() {
        return javaName;
    }
}
