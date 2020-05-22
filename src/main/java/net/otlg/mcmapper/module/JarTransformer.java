package net.otlg.mcmapper.module;

import net.otlg.mcmapper.MCMapper;
import net.otlg.mcmapper.record.ChildRecord;
import net.otlg.mcmapper.record.ClassRecord;
import net.otlg.mcmapper.visitor.ClassInfoSolver;
import net.otlg.mcmapper.visitor.ClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarTransformer {
    public static HashMap<String, ClassRecord> classes;

    public static void transformJar(File fileIn, File fileMap, File fileOut) throws IOException {
        if (!fileIn.exists()) throw new IllegalArgumentException("Input file doesn't exist!");
        if (!fileMap.exists()) throw new IllegalArgumentException("Map file doesn't exist!");
        // LOAD OBFUSCATION MAP

        MCMapper.logger.info("Loading obfuscation map...");

        BufferedReader reader = new BufferedReader(new FileReader(fileMap));

        Pattern classMatcher = Pattern.compile("^(.*) -> (.*)$");
        Pattern methodMatcher = Pattern.compile("^ {4}([^a-zA-Z]*)(.*) (.*) -> (.*)$");
        classes = new HashMap<>();
        ClassRecord currentClass = null;

        while (reader.ready()) {
            String s = reader.readLine();
            if (s.startsWith("#")) continue;
            if (s.startsWith("    ")) {
                Matcher matcher = methodMatcher.matcher(s);
                matcher.matches();
                ChildRecord record = new ChildRecord(matcher.group(2), matcher.group(3), matcher.group(4));

                if (matcher.group(1).contains(":") || matcher.group(3).contains("(")) {
                    // Is a method
                    currentClass.getDeclaredMethods().put(record.getObfuscatedName() + '-' + record.getSignature(), record);
                } else {
                    currentClass.getDeclaredFields().put(record.getObfuscatedName(), record);
                }

            } else {
                if (currentClass != null) {
                    classes.put(currentClass.getObfuscatedName(), currentClass);
                }

                Matcher matcher = classMatcher.matcher(s);
                matcher.matches();
                String targetName = matcher.group(2);
                currentClass = new ClassRecord(matcher.group(1), targetName.substring(0, targetName.length() - 1));
            }
        }

        if (currentClass != null) {
            classes.put(currentClass.getObfuscatedName(), currentClass);
        }

        // SOLVE CLASS SUPERCLASS

        {
            final byte[] buffer = new byte[1024];
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileIn));
            ZipEntry zipEntry;
            int length = 0;

            // ITERATE THROUGH ALL ENTRIES (AND COPY OVER TO NEW FILE)
            MCMapper.logger.info("Remapping...");
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {


                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while ((length = zipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }

                // PATCHING IF THE FILE MATCHED WITH NAME
                String zipEntryName = zipEntry.getName();

                if (zipEntryName.endsWith(".class") && (zipEntryName.startsWith("com/mojang")
                        || zipEntryName.startsWith("net/minecraft")
                        || !zipEntryName.contains("/"))) {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                    ClassReader cr = new ClassReader(inputStream);
                    ClassWriter cw = new ClassWriter(cr, 0);
                    ClassInfoSolver solver = new ClassInfoSolver(cw, classes, zipEntryName);
                    cr.accept(solver, 0);
                }
            }
        }

        // PROCESS JAR FILE

        final byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileIn));
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(fileOut));

        ZipEntry zipEntry;
        int length = 0;

        // ITERATE THROUGH ALL ENTRIES (AND COPY OVER TO NEW FILE)
        MCMapper.logger.info("Remapping...");
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((length = zipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            // PATCHING IF THE FILE MATCHED WITH NAME
            String zipEntryName = zipEntry.getName();

            if (zipEntryName.endsWith(".class") && (zipEntryName.startsWith("com/mojang")
                    || zipEntryName.startsWith("net/minecraft")
                    || !zipEntryName.contains("/"))) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                ClassReader cr = new ClassReader(inputStream);
                ClassWriter cw = new ClassWriter(cr, 0);
                ClassTransformer transformer = new ClassTransformer(cw, classes, zipEntryName);
                cr.accept(transformer, 0);
                byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] byteArray = cw.toByteArray();
                byteArrayOutputStream.write(byteArray);

                if (transformer.getOutName() != null) zipEntry = new ZipEntry(transformer.getOutName());
            }


            zipEntry.setSize(byteArrayOutputStream.size());
            zipEntry.setCompressedSize(-1);
            zipOutputStream.putNextEntry(zipEntry);
            byteArrayOutputStream.writeTo(zipOutputStream);
        }

        MCMapper.logger.info("Flushing files...");

        zipInputStream.close();
        zipOutputStream.closeEntry();
        zipOutputStream.close();
    }
}
