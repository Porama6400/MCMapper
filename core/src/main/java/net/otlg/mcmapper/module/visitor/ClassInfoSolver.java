package net.otlg.mcmapper.module.visitor;

import net.otlg.mcmapper.record.ClassRecord;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;

public class ClassInfoSolver extends ClassVisitor {

    private final HashMap<String, ClassRecord> classes;
    private final String zipEntryName;

    public ClassInfoSolver(ClassVisitor classVisitor, HashMap<String, ClassRecord> classes, String zipEntryName) {
        super(Opcodes.ASM9, classVisitor);
        this.classes = classes;
        this.zipEntryName = zipEntryName;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        ClassRecord classRecord = classes.get(name.replace('/', '.'));
        if (classRecord != null) {
            classRecord.setSuperClass(superName.replace('/', '.'));
            String[] myInterfaces = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                myInterfaces[i] = interfaces[i].replace('/', '.');
            }
            classRecord.setInterfaces(myInterfaces);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
