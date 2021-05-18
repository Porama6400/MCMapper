package net.otlg.mcmapper.record;

import net.otlg.mcmapper.module.JarTransformer;

import java.util.HashMap;

public class ClassRecord {
    private final String originalName;
    private final String obfuscatedName;
    private final HashMap<String, ChildRecord> methods = new HashMap<>();
    private final HashMap<String, ChildRecord> fields = new HashMap<>();
    private String superClass = null;
    private String[] interfaces = null;

    public ClassRecord(String originalName, String obfuscatedName) {
        this.originalName = originalName;
        this.obfuscatedName = obfuscatedName;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public HashMap<String, ChildRecord> getDeclaredFields() {
        return fields;
    }

    public HashMap<String, ChildRecord> getDeclaredMethods() {
        return methods;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getObfuscatedName() {
        return obfuscatedName;
    }

    public ChildRecord getMethod(String name) {
        ChildRecord record = methods.get(name);
        if (record != null) return record;
        if (superClass != null) {
            ClassRecord record1 = JarTransformer.classes.get(superClass);
            if (record1 != null) {
                ChildRecord method = record1.getMethod(name);
                if (method != null) return method;
            }
        }
        if (interfaces != null) {
            for (String i : interfaces) {
                ClassRecord record2 = JarTransformer.classes.get(i);
                if (record2 == null) continue;
                ChildRecord method = record2.getMethod(name);
                if (method != null) return method;
            }
        }
        return null;
    }

    public ChildRecord getField(String name) {
        ChildRecord record = fields.get(name);
        if (record != null) {
            return record;
        }
        if (superClass == null) {
            return null;
        }
        ClassRecord record1 = JarTransformer.classes.get(superClass);
        if (record1 != null) {
            return record1.getField(name);
        }
        return null;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }
}
