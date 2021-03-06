package kfs.asn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import kfs.asn.utils.AsnConst;
import kfs.asn.utils.ASNException;
import kfs.asn.utils.ASNClassFactory;
import kfs.asn.utils.EBlock;
import kfs.asn.utils.Field;
import kfs.asn.utils.GramarFile;
import org.apache.log4j.Logger;

public class AsnDecoder {

    private final Map<String, Class> nodeMap;
    private final ArrayList<AsnNodeCallBack> asnNodeCallBackList;

    public AsnDecoder(Map<String, Class> nodeClsMap) {
        this.nodeMap = nodeClsMap;
        asnNodeCallBackList = new ArrayList<AsnNodeCallBack>();
    }

    public void addAsnNodeCallBack(AsnNodeCallBack cb) {
        asnNodeCallBackList.add(cb);
    }

    public void removeAsnNodeCallBack(AsnNodeCallBack cb) {
        asnNodeCallBackList.remove(cb);
    }

    protected void fireCallBack(Object node) {
        for (AsnNodeCallBack cb : asnNodeCallBackList) {
            if (cb.acceptCls(node.getClass())) {
                cb.kfsCb(node);
            }
        }
    }

    public Object parse(InputStream dataFile, GramarFile grammarFile) {
        return parse(dataFile, grammarFile, null);
    }
    public Object parse(InputStream dataFile, GramarFile grammarFile, AsnNodeCallBack directCb)  {
        Field rootField = new ASNClassFactory(grammarFile).getField();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int r;
            try {
                r = dataFile.read(buffer);
            } catch (IOException ex) {
                throw new ASNException(null, "Cannot read data", ex);
            }
            if (r == -1) {
                break;
            }
            out.write(buffer, 0, r);
        }
        byte[] byteArr = out.toByteArray();
        EBlock rootBlock = new EBlock(0, 0, byteArr, 0, 0, byteArr.length);

        Object topNode = makeNode(rootField, rootBlock, -1, 0, directCb);
        // Assumption is that for all Grammar files the topNode's type is always an array
        // ie.   topNode.getType().isArray() == true
        //return topNode.subNodes;
        return topNode;
    }

    private Object makeNode(Field asnField, EBlock b, int maxBlocks, int depth, AsnNodeCallBack directCb) {
        try {
            if (b.isLeaf()) {
                Integer tag = AsnConst.getPrimitiveMap().get(asnField.type.name);
                if (tag == null) {
                    return new AsnData(0, b.getValue());
                } else {
                    return new AsnData(tag, b.getValue());
                }
            }
            if (asnField.isArray()) {
                return makeNodeArray(asnField, b, maxBlocks, depth, directCb);
            }
            Class cls = nodeMap.get(asnField.type.name);
            //Logger.getLogger(getClass()).info(cls.getSimpleName());
            java.lang.reflect.Field fields[] = cls.getDeclaredFields();
            Map<Integer, Method> objFieldMap = new HashMap<Integer, Method>(fields.length);
            for (java.lang.reflect.Field field : fields) {
                ASNDef asnDef = field.getAnnotation(ASNDef.class);
                if (asnDef == null) {
                    continue;
                }
                Method setMethod = null;
                String s = field.getName();
                String setMethodName = "set" + s.substring(0, 1).toUpperCase() + s.substring(1);

                Class type = List.class;
                if (type.equals(field.getType())) {
                    setMethod = getMethod(cls, setMethodName + "Asn", type);
                }

                if (setMethod == null) {
                    type = AsnData.class;
                    setMethod = getMethod(cls, setMethodName, type);
                }
                if (setMethod == null) {
                    if (AsnConst.isPrimitive(asnDef.asnType())) {
                        type = AsnConst.getPrimitiveClass(asnDef.asnType());
                    } else {
                        type = this.nodeMap.get(asnDef.asnType());
                    }
                    setMethod = getMethod(cls, setMethodName + "Asn", type);
                    if (setMethod == null) {
                        setMethod = getMethod(cls, setMethodName, type);
                    }
                    if (setMethod == null) {
                        type = field.getType();
                        setMethod = getMethod(cls, setMethodName + "Asn", type);
                        if (setMethod == null) {
                            setMethod = getMethod(cls, setMethodName, type);
                        }
                    }

                }
                if (setMethod == null) {
                    throw new ASNException("kfs", "Cannot find set method " + cls.getSimpleName()//
                            + "." + setMethodName + " - " + type.getSimpleName());
                }
                objFieldMap.put(asnDef.asnPos(), setMethod);
            }
            Object obj;
            try {
                obj = cls.newInstance();
            } catch (InstantiationException ex) {
                throw new ASNException("kfs", "Cannot init object " + asnField.type.name, ex);
            } catch (IllegalAccessException ex) {
                throw new ASNException("kfs", "Cannot init object " + asnField.type.name, ex);
            }

            ArrayList<EBlock> subBlocks = b.getSubBlocks(EBlock.MAX_BLOCKS, 0, (byte) 0);

            for (int i = 0; i < subBlocks.size(); i++) {
                EBlock subBlock = subBlocks.get(i);
                Object param = null;// = makeUniversalSetSeqNode(asnField, subBlock, maxBlocks, depth);
                int sbpos = subBlock.tag;
                if (param == null) {
                    Field childAsnField = asnField.getChildField(sbpos);
                    if (childAsnField != null) {
                        param = makeNode(childAsnField, subBlock, maxBlocks, depth + 1, directCb);
                    }
                }
                if (param != null) {
                    Method objMethod = objFieldMap.get(sbpos);
                    if (objMethod == null) {
                        //throw new ASNException(null, "Cannot find set method for asn pos = " + sbpos + " in class " + cls.getSimpleName());
                        Logger.getLogger(AsnDecoder.class).fatal("Cannot find set method for asn pos = " + sbpos + " in class " + cls.getSimpleName() + " tag: " + subBlock.tag);
                    } else {
                        setData(obj, objMethod, param);
                    }
                } else { // -- composite | not array | choice
                    String str = "Unable to find child field or grandchild field for given tag. subBlock.tag("
                            + sbpos + ") field(" + asnField + ")";
                    Logger.getLogger(AsnDecoder.class).fatal(str);
                }
            }
            if (directCb != null) {
                if (directCb.acceptCls(obj.getClass())) 
                    directCb.kfsCb(obj);
            }
            fireCallBack(obj);
            return obj;
        } catch (ASNException e) {
            // Catch Exception if thrown by recursive makeNode(), append stack Info and re throw. 
            if (e.isType("makeNode")) {
                throw new ASNException("makeNode", "\n makeNode(" + asnField + "," + b + ") " + e.getMessage(), e);
            }
            if (e.isType("makeNodeArray")) {
                throw new ASNException("makeNodeArray", "\n makeNodeArray(" + asnField + "," + b + ") " + e.getMessage(), e);
            }
            throw e;
        }
    }

    private static void setData(Object obj, Method objMethod, Object param) {
        try {
            Class []ptt = objMethod.getParameterTypes();
            if (ptt == null) {
                throw new ASNException(null, "Cannot find parameters in method: " + objMethod.toGenericString());
            }
            Class pt = ptt[0];
            if (pt.isInstance(param)) {
                objMethod.invoke(obj, param);
                return;
            }
            if (AsnData.class.equals(param.getClass())) {
                objMethod.invoke(obj, ((AsnData) param).getData(pt));
                return;
            }
            if (pt.equals(List.class)) {
                objMethod.invoke(obj, Arrays.asList(param));
                return;
            }
            if (List.class.isInstance(param)) {
                List lst = (List) param;
                if (lst.size() == 1) {
                    setData(obj, objMethod, lst.get(0));
                    return;
                }
            }
            throw new ASNException("kfs", "Cannot run set method: " + objMethod.toGenericString() + " but parameter is " + param.getClass().getSimpleName());
        } catch (IllegalAccessException ex) {
            throw new ASNException("kfs", "Cannot set object " + obj.getClass().getSimpleName() + "." + objMethod.getName() + "( " + param.getClass().getSimpleName() + " )", ex);
        } catch (IllegalArgumentException ex) {
            throw new ASNException("kfs", "Cannot set object " + obj.getClass().getSimpleName() + "." + objMethod.getName() + "( " + param.getClass().getSimpleName() + " ) - " + objMethod.toGenericString(), ex);
        } catch (InvocationTargetException ex) {
            throw new ASNException("kfs", "Cannot set object " + obj.getClass().getSimpleName() + "." + objMethod.getName() + "( " + param.getClass().getSimpleName() + " )", ex);
        }
    }

    private static Method getMethod(Class cls, String setMethodName, Class type) {
        try {
            return cls.getMethod(setMethodName, type);
        } catch (NoSuchMethodException ex) {
            //throw new ASNException("kfs", "Cannot find set method " + cls.getSimpleName() + "." + setMethodName, ex);
        } catch (SecurityException ex) {
            //throw new ASNException("kfs", "Cannot find set method " + cls.getSimpleName() + "." + setMethodName, ex);
        }
        return null;
    }

    private Object makeNodeArray(Field f, EBlock b, int maxBlocks, int depth, AsnNodeCallBack directCb) {

        ArrayList<EBlock> subBlocks = b.getSubBlocks(EBlock.MAX_BLOCKS, f.type.blockSize, f.type.paddingByte);
        ArrayList list = new ArrayList(subBlocks.size());

        if (f.isReference()) { // Heterogeneous Array
            for (int i = 0; i < subBlocks.size(); i++) {
                EBlock subBlock = subBlocks.get(i);
                int sbpos = subBlock.tag;
                Field refChildField = f.getChildField(sbpos);
                if (refChildField == null) {
                    throw new ASNException("makeNodeArray", "No child field at pos 'sbpos' for Reference Array.");
                } else {
                    Object refChildNode = makeNode(refChildField, subBlock, maxBlocks, depth + 1, directCb);
                    if (refChildNode != null) {
                        list.add(refChildNode);
                    }
                }
            }
        } else { // Homogeneous Array

            Field f_nav = f.getCachedCloneNoneArray();
            for (EBlock subBlock : subBlocks) {
                Object node = makeNode(f_nav, subBlock, maxBlocks, depth + 1, directCb);
                if (node != null) {
                    list.add(node);
                }

                // ------------------Defensive Checks ------------------
                if (f_nav.type.isAssociatedWithTag()) { // Array of Primitives ??
                    if (subBlock.isUniversalSetSeq()) {
                        throw new ASNException("makeNodeArray", "Unexpected Universal 16|17 Tag ");
                    }
                } else { // Array of Composite
                    //int sbpos = subBlock.tag;
                    if (!subBlock.isUniversalSetSeq()) { // Furthermore the subBlock.tag = 16 or 17 based on f_nav.relation (SEQ/SET) 
                        throw new ASNException("makeNodeArray", "!subBlock.isUniversalSetSeq()");
                    }
                } // -------------End of Defensive checks------------				
            }
        }
        return list;
    }
}
