package kfs.kfsAsnDecode;

import java.util.ArrayList;

/**
 *
 * @author pavedrim
 */
public class kfsCreateClass {
    
    public final String name;
    public final String clsName;
    public final String pkg;
    public final ArrayList<kfsCCcItem> subs;
    
    public kfsCreateClass(final String name, final String pkg) {
        this.subs = new ArrayList<kfsCCcItem>();
        this.name = name;
        this.pkg = pkg;
        this.clsName = "kfs"+getCapitalize(name.replace("-", "_"));
    }
    
    public void add(String str, Integer pos) {
        subs.add(new kfsCCcItem(str, pos));
    }
    
    public static String getCapitalize(String inp) {
        return inp.substring(0, 1).toUpperCase() + inp.substring(1);
    }

    @Override
    public String toString() {
        return toString2();
    }
    
    public String toString2() {
        kfsSb s = new kfsSb();
        s.anl("package ",pkg,";").nl();
        s.nl().anl("import java.util.HashMap;")//
                .anl("import kfs.kfsAsnDecode.ISetDataOnAsnPos;")//
                .anl("import kfs.kfsDbi.*;").nl().nl();
        s.anl("public class ",clsName," extends kfsDbObject {").nl();
        for (kfsCCcItem cn : subs) {
        s.anl("    private final kfsString ",cn.getJcn(),";");
        }
        s.anl("    private final kfsInt dayNo;");
        
        s.anl("    private final HashMap<Integer, Integer> rePos = new HashMap<Integer, Integer>();");
        s.anl("    public ",clsName,"(kfsDbServerType dbType) {");
        s.anl("        super(dbType, \"",name,"\", \"",name,"\");");
        s.anl("        int pos = 0;");
        for (kfsCCcItem cn : subs) {
        s.anl("        rePos.put(", cn.getPos(),", pos);");   
        s.anl("        this.",cn.getJcn()," = new kfsString(\"",cn.getJcn(),"\", \"",cn.getCn(),"\", 50, pos++);");
        }
        s.anl("        dayNo = new kfsInt(\"DAY_NO\", \"DAY_NO\", 10, pos++, false);");
        s.anl("        super.setColumns(new kfsDbiColumn[] {");
        for (kfsCCcItem cn : subs) {
        s.anl("        this.",cn.getJcn(),",");
        }        
        s.anl("        this.dayNo");        
        s.anl("        });");
        s.anl("        /*");
        s.anl("        for (kfsDbiColumn cc : allCols) {");
        s.anl("            if (cc.getColumnName().length() > 30) {");
        s.anl("                System.out.println(getLabel()+\" - \"+ cc.getColumnName());");
        s.anl("            }");
        s.anl("        }");
        s.anl("        */");
        s.anl("    }").nl();
        s.anl("    public pojo getPojo() {");
        s.anl("        return new pojo(new kfsRowData(this));");
        s.anl("    }").nl();
        s.anl("    @Override");
        s.anl("    public pojo getPojo(kfsRowData row) {");
        s.anl("        return new pojo(row);");
        s.anl("    }").nl();
        s.anl("    @Override");
        s.anl("    public String getCreateTable() {");
        s.anl("        return super.getCreateTable() + //");
        s.anl("                \" PARTITION BY LIST(\" + dayNo.getColumnName() + \") ( PARTITION P_20110904 VALUES ( 20110904 ) )\";");
        s.anl("    }").nl();
        
        s.anl("    public class pojo extends kfsPojoObj<",clsName,"> implements ISetDataOnAsnPos {").nl();
        s.anl("        public pojo(kfsRowData rd) {");
        s.anl("            super(",clsName,".this, rd);");
        s.anl("        }").nl();
        s.anl("        @Override");
        s.anl("        public void setDataOnAsnPos(int asnPos, Object data) {");
        s.anl("            Integer rdPos = rePos.get(asnPos);");
        s.anl("            if ((rdPos != null) && (rdPos >= 0) && (rdPos < rd.getCount())) {");
        s.anl("                rd.setObject(rePos.get(asnPos), data);");
        s.anl("            }");
        s.anl("        }");
        s.anl("").nl();
        for (kfsCCcItem cn : subs) {
        s.anl("        public String get",cn.getJcn(),"() {");
        s.anl("            return inx.",cn.getJcn(),".getData(rd);");
        s.anl("        }").nl();
        }         
        s.anl("    }");
        s.anl("}");
        return s.toString();        
    }
    
    public String toString3() {
        kfsSb s = new kfsSb();
        s.anl("package ",pkg,";").nl();
        s.nl().anl("import kfs.kfsDbi.*;").nl().nl();
        s.anl("public class ",clsName," extends kfsDbObject {").nl().nl();
        s.anl("    public static String tableName = \"",name,"\";");
        s.anl("    public static String tableLabel = \"",name,"\";");
        for (kfsCCcItem cn : subs) {
        s.anl("    public static String ",cn.getJcn(),"Name = \"",cn.getJcn(),"\";");
        s.anl("    public static String ",cn.getJcn(),"Label = \"",cn.getCn(),"\";");
        }
        for (kfsCCcItem cn : subs) {
        s.anl("    public kfsString ",cn.getJcn(),";");
        }
        
        s.anl("    public ",clsName,"(kfsDbServerType dbType) {");
        s.anl("        super(dbType, tableName, tableLabel);");
        s.anl("        int pos = 0;");
        for (kfsCCcItem cn : subs) {
        s.anl("    this.",cn.getJcn()," = new kfsString(",cn.getJcn(),"Name, ",cn.getJcn(),"Label, 50, pos++);");
        }
        s.anl("        super.setColumns(new kfsDbiColumn[] {");
        for (kfsCCcItem cn : subs) {
        s.anl("        this.",cn.getJcn(),",");
        }        
        s.anl("        });");
        s.anl("    }").nl();
        s.anl("    public pojo getPojo() {");
        s.anl("        return new pojo(new kfsRowData(this));");
        s.anl("    }").nl();
        s.anl("    @Override");
        s.anl("    public pojo getPojo(kfsRowData row) {");
        s.anl("        return new pojo(row);");
        s.anl("    }").nl();
        s.anl("    public class pojo extends kfsPojoObj<",clsName,"> {").nl();
        s.anl("        public pojo(kfsRowData rd) {");
        s.anl("            super(",clsName,".this, rd);");
        s.anl("        }").nl();
        for (kfsCCcItem cn : subs) {
        s.anl("        public String get",cn.getJcn(),"() {");
        s.anl("            return inx.",cn.getJcn(),".getData(rd);");
        s.anl("        }").nl();
        s.anl("        public void set",cn.getJcn(),"(String s) {");
        s.anl("            inx.",cn.getJcn(),".setData(s, rd);");
        s.anl("        }").nl();
        }         
        s.anl("    }");
        s.anl("}");
        return s.toString();
    }
    
    
}
