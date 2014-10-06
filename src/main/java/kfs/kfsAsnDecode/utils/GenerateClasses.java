package kfs.kfsAsnDecode.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import kfs.kfsAsnDecode.ASNClass;
import kfs.kfsAsnDecode.ASNClassFactory;
import kfs.kfsAsnDecode.ASNConst;
import kfs.kfsAsnDecode.Field;
import kfs.kfsAsnDecode.kfsGramarFile;

/**
 *
 * @author pavedrim
 */
public class GenerateClasses {

    private final String asnName;
    private final String className;
    private final String javaName;
    private final String humanName;
    private final String dbName;
    private final ArrayList<GenerateClasses> classes;
    private final StringBuilder output;

    public GenerateClasses(String packageName, ASNClass cls/*, kfsCreatePojoClass master*/) {
        this.classes = new ArrayList<GenerateClasses>();
        this.asnName = cls.name;
        String[] names = Util.getNamesFromJavaName(asnName);
        this.humanName = Util.getHumanName(names).toString();
        this.dbName = Util.getDbName(names).toString();
        this.className = Util.getHumanName(names, "").toString();
        this.javaName = Util.getJavaName(names).toString();

        output = new StringBuilder().append(getClassString(packageName, cls/*, master*/));
    }

    private CharSequence getClassString(String packageName, ASNClass cls/*, kfsCreatePojoClass master*/) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sg = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import java.util.Arrays;\n");
        sb.append("import java.util.List;\n");
        sb.append("import kfs.kfsAsnDecode.utils.ASNCls;\n");
        sb.append("import kfs.kfsAsnDecode.utils.ASNDef;\n");
        sb.append("import kfs.kfsAsnDecode.utils.AsnData;\n");
        sb.append("import javax.persistence.GeneratedValue;\n");
        sb.append("import javax.persistence.GenerationType;\n");
        sb.append("import javax.persistence.Id;\n");
        sb.append("import javax.persistence.Column;\n");
        sb.append("import javax.persistence.OneToMany;\n");
        //sb.append("import javax.persistence.ManyToOne;\n");
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
                String[] names = Util.getNamesFromJavaName(field.name);
                String javaVarName = Util.getJavaName(names).toString();
                String sgName = javaVarName.substring(0, 1).toUpperCase() + javaVarName.substring(1);
                maxJavaNameLength = Math.max(maxJavaNameLength, javaVarName.length());
                sb.append("    @ASNDef(asnPos = ").append(field.pos).append(", asnName = \"")//
                        .append(field.name).append("\", label=\"").append(Util.getHumanName(names))//
                        .append("\", asnType = \"").append(field.type.name).append("\" )\n");
                if (ASNConst.isPrimitive(field.type.name)) {
                    if ("servingNodeType".equals(field.name)) {
                        System.out.println(field.toConciseString() + " " + field.type.isSequence());
                    }
                    Class varCls = ASNConst.getPrimitiveClass(field.type.name);
                    if (String.class.equals(varCls)) {
                        sb.append("    @Column(length = 50, name = \"").append(Util.getDbName(names)).append("\")\n");
                    } else {
                        sb.append("    @Column(name = \"").append(Util.getDbName(names)).append("\")\n");
                    }
                    sb.append("    private ").append(varCls.getSimpleName())//
                            .append(" ").append(javaVarName).append(";\n\n");

                    sg.append(getGetterSetter(varCls.getSimpleName(), sgName, javaVarName));

                } else {
                    GenerateClasses inCls = new GenerateClasses(packageName, field.type);
                    sb.append("    @OneToMany\n");
                    if (field.isArray() || field.type.isSet() || field.type.isSequence()) {
                        sb.append("    private List<").append(inCls.className).append("> ").append(javaVarName).append(";\n\n");
                        sg.append(getGetterSetter("List<" + inCls.className + ">", sgName, javaVarName));
                    } else {
                        sb.append("    private ").append(inCls.className).append(" ").append(javaVarName).append(";\n\n");
                        sg.append(getGetterSetter(inCls.className, sgName, javaVarName));
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
                String[] names = Util.getNamesFromJavaName(field.name);
                String javaVarName = Util.getJavaName(names).toString();
                if (ASNConst.isPrimitive(field.type.name) || (!field.isArray() && !field.type.isSet() && !field.type.isSequence())) {
                    sb.append("        sb.append(\"  ").append(javaVarName)//
                            .append(new String(new char[maxJavaNameLength - javaVarName.length() + 1]).replace("\0", " "))//
                            .append(" = \").append(").append(javaVarName).append(").append(\"\\n\");\n");
                } else {
                    sb.append("        sb.append(\"  ").append(javaVarName)//
                            .append(new String(new char[maxJavaNameLength - javaVarName.length() + 1]).replace("\0", " "))//
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

    private CharSequence getGetterSetter(String className, String sgName, String javaVarName) {
        return new StringBuilder()//
                .append("    public ").append(className).append(" get")//
                .append(sgName).append("() { \n")//
                .append("        return ").append(javaVarName)//
                .append(";\n    }\n\n")//
                .append("    public void set")//
                .append(sgName).append("(").append(className).append(" ")//
                .append(javaVarName).append(" ) { \n")//
                .append("        this.").append(javaVarName).append(" = ")//
                .append(javaVarName).append(";\n")//
                .append("    }\n\n")
                .append("    //public void set"+sgName+"(AsnData "+javaVarName+" ) { \n"
                        + "  //      this."+javaVarName+" = "+javaVarName+".getByteArray().toString();\n"
                        + "  //  }\n\n");
    }
    
    public static void save(String dir, String packageName, kfsGramarFile gramar) throws IOException {
        Field ff = ASNClassFactory.getField(gramar);

        GenerateClasses cc = new GenerateClasses(packageName, ff.type);
        ArrayDeque<GenerateClasses> al = new ArrayDeque<GenerateClasses>();
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

    public List<GenerateClasses> innerClasses() {
        return classes;
    }

    public String getJavaName() {
        return javaName;
    }
}
