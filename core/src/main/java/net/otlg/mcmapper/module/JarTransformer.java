package net.otlg.mcmapper.module;

import net.otlg.bitumen.pipe.PipeAction;
import net.otlg.bitumen.pipe.ZipPipe;
import net.otlg.mcmapper.MCMapper;
import net.otlg.mcmapper.module.visitor.ClassInfoSolver;
import net.otlg.mcmapper.module.visitor.ClassTransformer;
import net.otlg.mcmapper.record.ChildRecord;
import net.otlg.mcmapper.record.ClassRecord;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class JarTransformer {
    public static HashMap<String, ClassRecord> classes;

    public static void transformJar( ExecutorService executor, File fileIn, File fileMap, File fileOut) throws IOException {
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
                if(currentClass == null) throw new IllegalStateException("currentClass is null while trying to access");

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
        MCMapper.logger.info("Solving class tree...");

        ZipPipe classSolverPipe = new ZipPipe(executor, (in, out) -> {
            ClassReader cr = null;
            try {
                String zipEntryName = in.getZipEntry().getName();

                if (!JarTransformer.isRelevantFile(zipEntryName)) {
                    out.setState(PipeAction.PASSTHROUGHS);
                    return;
                }

                cr = new ClassReader(in.getInputStream());
                ClassWriter cw = new ClassWriter(cr, 0);
                ClassInfoSolver solver = new ClassInfoSolver(cw, classes, in.getZipEntry().getName());
                cr.accept(solver, 0);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
        classSolverPipe.process(fileIn, null);

        // PROCESS JAR FILE
        MCMapper.logger.info("Transforming...");

        ZipPipe transformPipe = new ZipPipe(executor, (in, out) -> {
            try {
                String zipEntryName = in.getZipEntry().getName();

                if (!JarTransformer.isRelevantFile(zipEntryName)) {
                    out.setState(PipeAction.PASSTHROUGHS);
                    return;
                }

                ClassReader cr = new ClassReader(in.getInputStream());
                ClassWriter cw = new ClassWriter(cr, 0);
                ClassTransformer transformer = new ClassTransformer(cw, classes, zipEntryName);
                cr.accept(transformer, 0);
                byte[] byteArray = cw.toByteArray();
                out.setBytes(byteArray);
                if (transformer.getOutName() != null) out.setZipEntry(new ZipEntry(transformer.getOutName()));

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });


        transformPipe.process(fileIn, fileOut);
    }

    public static boolean isRelevantFile(String zipEntryName) {
        if (!zipEntryName.endsWith(".class")) return false;

        if (!zipEntryName.contains("/")) return true;
        if (zipEntryName.startsWith("com/mojang")) return true;
        if (zipEntryName.startsWith("net/minecraft")) return true;

        return false;
    }
}
