package net.otlg.mcmapper.module.visitor;

import net.otlg.mcmapper.MCMapper;
import net.otlg.mcmapper.record.ChildRecord;
import net.otlg.mcmapper.record.ClassRecord;
import org.objectweb.asm.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassTransformer extends ClassVisitor {
    private final HashMap<String, ClassRecord> classes;
    private final String zipEntryName;
    private final Pattern signaturePattern = Pattern.compile("(L)([a-zA-Z0-9$/]+)([;<\\[])");
    private final ClassRecord classRecord;
    private String outName;

    public ClassTransformer(ClassVisitor visitor, HashMap<String, ClassRecord> classes, String zipEntryName) {
        super(Opcodes.ASM8, visitor);
        this.classes = classes;
        this.zipEntryName = zipEntryName;

        classRecord = classes.get(zipEntryName.substring(0, zipEntryName.length() - 6).replace('/', '.'));
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        if (owner != null) {
            ClassRecord record = classes.get(owner.replace('/', '.'));
            if (record != null) {
                owner = transformName(owner);

                if (name != null) {
                    ChildRecord childRecord = record.getField(name);
                    if (childRecord != null) {
                        name = childRecord.getName();
                    }
                }
            }
        }
        if (desc != null) desc = transformDescriptor(desc);
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name != null) {
            name = transformName(name);
            String[] split = name.split("\\$");
            outerName = split[0];
            innerName = split[1];
        }
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (classRecord != null) {
            {
                String className = name;
                name = classRecord.getOriginalName().replace('.', '/');
                outName = name + ".class";
                MCMapper.logger.info("Mapping " + className + " -> " + name);

                for (int i = 0; i < interfaces.length; i++) {
                    String intName = interfaces[i].replace('/', '.');
                    ClassRecord record = classes.get(intName);
                    if (record == null) continue;
                    interfaces[i] = record.getOriginalName().replace('.', '/');
                }
            }
            if (superName != null) {
                String classSuperName = superName.replace('/', '.');
                if (classes.containsKey(classSuperName)) {
                    ClassRecord record = classes.get(classSuperName);
                    superName = record.getOriginalName().replace('.', '/');
                }
            }
            if (signature != null) {
                signature = transformDescriptor(signature);
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (classRecord == null) {
            return super.visitField(access, name, desc, signature, value);
        }

        desc = transformDescriptor(desc);
        ChildRecord childRecord = classRecord.getField(name);
        if (childRecord != null) {
            name = childRecord.getName();
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (desc != null) desc = transformDescriptor(desc);
        if (signature != null) signature = transformDescriptor(signature);

        if (classRecord != null) {
            ChildRecord record = classRecord.getMethod(name + "-" + extractBracket(desc));
            if (record == null) return super.visitMethod(access, name, desc, signature, exceptions);
            name = record.getName();
        }

        if (exceptions != null) {
            for (int i = 0; i < exceptions.length; i++) {
                if (exceptions[i] == null) continue;
                exceptions[i] = transformName(exceptions[i]);
            }
        }

        return new MethodVisitor(Opcodes.ASM8, super.visitMethod(access, name, desc, signature, exceptions)) {

            @Override
            public void visitLdcInsn(Object cst) {
                if (cst instanceof Type) {
                    Type type = (Type) cst;
                    String name = transformDescriptor(type.getDescriptor());
                    Type newType = Type.getType(name);
                    injectTypeSort(newType, type.getSort());
                    cst = newType;
                }
                super.visitLdcInsn(cst);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                if (desc != null) desc = transformDescriptor(desc);

                for (int i = 0; i < bsmArgs.length; i++) {
                    Object object = bsmArgs[i];
                    if (object instanceof Handle) {
                        Handle handle = (Handle) object;

                        String handleOwner = transformName(handle.getOwner());
                        ClassRecord handleClassRecord = classes.get(handle.getOwner().replace('/', '.'));
                        String handleName = (handle.getName());
                        String handleDesc = transformDescriptor(handle.getDesc());

                        if (handleClassRecord != null) {
                            ChildRecord childRecord = handleClassRecord.getMethod(handleName + "-" + extractBracket(handleDesc));
                            if (childRecord != null) handleName = childRecord.getName();
                        }
                        bsmArgs[i] = new Handle(handle.getTag(), handleOwner, handleName, handleDesc, handle.isInterface());
                    } else if (object instanceof Type) {
                        Type type = (Type) object;
                        String typeName = type.getDescriptor();
                        typeName = transformDescriptor(typeName);
                        Type newType = Type.getType(typeName);
                        injectTypeSort(newType, type.getSort());
                        bsmArgs[i] = newType;
                    } else {
                        MCMapper.logger.warning("Unknown invoke dynamic argument: " + object.getClass().getName());
                    }
                }

                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                ClassRecord ownerClass = classes.get(owner.replace('/', '.'));
                desc = transformDescriptor(desc);

                if (ownerClass != null) {
                    ChildRecord methodRecord = ownerClass.getMethod(name + "-" + extractBracket(desc));

                    if (methodRecord == null) {
                        if (name.length() < 3) {
                            MCMapper.logger.warning("Method " + name + "-" + extractBracket(desc) + " not found in " + ownerClass.getOriginalName());
                        }
                    } else {
                        name = methodRecord.getName();
                    }
                }
                owner = transformName(owner);

                if (opcode == Opcodes.INVOKEVIRTUAL) {
                    owner = transformDescriptor(owner);
                }
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }

            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                if (desc != null) desc = transformDescriptor(desc);
                if (signature != null) signature = transformDescriptor(signature);
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                ClassRecord ownerClass = classes.get(owner.replace('/', '.'));
                if (ownerClass != null) {
                    ChildRecord fieldRecord = ownerClass.getField(name);
                    if (fieldRecord != null) {
                        name = fieldRecord.getName();
                    }
                }
                owner = transformName(owner);
                desc = transformDescriptor(desc);

                super.visitFieldInsn(opcode, owner, name, desc);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
                if (type != null) {
                    type = transformName(type);
                    type = transformDescriptor(type);
                }
                super.visitTypeInsn(opcode, type);
            }

            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                if (local != null) {
                    for (int i = 0; i < local.length; i++) {
                        Object obj = local[i];

                        if (obj instanceof String) {
                            local[i] = transformName((String) obj);
//                        MCMapper.logger.info(Arrays.toString(local));
                        }
                    }
                }
                if (stack != null) {
                    for (int i = 0; i < stack.length; i++) {
                        Object obj = stack[i];
                        if (obj instanceof String) {
                            stack[i] = transformName((String) obj);
//                        MCMapper.logger.info(Arrays.toString(stack));
                        }
                    }
                }
                super.visitFrame(type, nLocal, local, nStack, stack);
            }

            @Override // Never use by server code
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc != null) desc = transformDescriptor(desc);
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
                if (descriptor != null) descriptor = transformDescriptor(descriptor);
                super.visitMultiANewArrayInsn(descriptor, numDimensions);
            }

            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
                if (type != null) type = transformName(type);
                super.visitTryCatchBlock(start, end, handler, type);
            }

            /*=======================================================================
                This section doesn't seems to be used by current Minecraft Jar file
             =======================================================================*/

            @Override
            public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                MCMapper.logger.warning(">>>> TYPE_ANNO");
                MCMapper.logger.warning(typePath.toString());
                MCMapper.logger.warning(desc);

                return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
            }

            @Override
            public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                MCMapper.logger.warning(">>>> INSN_ANNO");
                MCMapper.logger.warning(typePath.toString());
                MCMapper.logger.warning(desc);
                return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
            }

            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
                MCMapper.logger.warning(">>>> LOCAL_VAR_ANNO");
                MCMapper.logger.warning(typePath.toString());
                MCMapper.logger.warning(Arrays.toString(start));
                MCMapper.logger.warning(Arrays.toString(end));
                MCMapper.logger.warning(desc);

                return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            }

            @Override
            public void visitAttribute(Attribute attr) {
                MCMapper.logger.warning(">>>> ATTR");
                MCMapper.logger.warning(attr.toString());
                super.visitAttribute(attr);
            }

            @Override
            public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                MCMapper.logger.warning(">>>> TRY_CATCH_ANNO");
                MCMapper.logger.warning(typePath.toString());
                MCMapper.logger.warning(desc);

                return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
            }
        };
    }

    public void injectTypeSort(Type type, int sort) {
        // TODO cache this field object
        try {
            Field sortField = Type.class.getDeclaredField("sort");
            sortField.setAccessible(true);
            sortField.set(type, sort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String transformName(String name) {
        if (name.startsWith("[") || name.startsWith("L")) {
            return transformDescriptor(name);
        } else if (name.endsWith("]")) {
            int bracket = 0;
            while (name.endsWith("]")) {
                bracket++;
                name = name.substring(0, name.length() - 2);
            }
            StringBuilder nameBuilder = new StringBuilder(transformName(name));
            for (int i = 0; i < bracket; i++) {
                nameBuilder.append("[]");
            }
            return nameBuilder.toString();
        }

        String classSuperName = name.replace('/', '.');
        if (classes.containsKey(classSuperName)) {
            ClassRecord record = classes.get(classSuperName);
            return record.getOriginalName().replace('.', '/');
        }
        return name;
    }

    public String transformDescriptor(String signature) {
        signature = signature.replace('/', '.');
        int startIndex = 0;
        while (true) {
            Matcher matcher = signaturePattern.matcher(signature);
            if (!matcher.find(startIndex)) break;
            startIndex = matcher.end();
            String className = matcher.group(2);
            if (className == null) throw new IllegalArgumentException("Failed to match signature: " + signature);
            if (classes.containsKey(className)) {
                className = classes.get(className).getOriginalName();
            }
            signature = signature.replace(matcher.group(), matcher.group(1) + className + matcher.group(3));
        }
        return signature.replace('.', '/');
    }

    public String extractBracket(String input) {
        boolean en = false;
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c == '(') {
                en = true;
            } else if (c == ')') {
                return builder.toString();
            } else {
                builder.append(c);
            }
        }
        if (en) {
            throw new IllegalArgumentException("unclosed bracket");
        } else return "";
    }

    public String getOutName() {
        return outName;
    }
}
