package net.otlg.mcmapper.module;

import net.otlg.mcmapper.MCMapper;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarVerifier {
    public static void verify(File file) throws IOException {
        File temp1 = new File("temp1.jar");
        File temp2 = new File("temp2.jar");
        FileUtils.copyFile(file, temp1);
        FileUtils.copyFile(file, temp2);

        URLClassLoader classLoader = new URLClassLoader(new URL[]{temp1.toURI().toURL()}, Thread.currentThread().getContextClassLoader());

        final byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(temp2));
        ZipEntry zipEntry;
        int length = 0;

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
                MCMapper.logger.info("Checking " + zipEntryName);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                ClassReader reader = new ClassReader(inputStream);
                try {
                    PrintWriter writer = new PrintWriter(System.out);
                    CheckClassAdapter.verify(reader, classLoader, true, writer);
                } catch (Throwable e) {
                    e.printStackTrace();
                    MCMapper.logger.severe(e.getMessage());
                }
            }
        }
        zipInputStream.close();
        temp1.deleteOnExit();
        temp2.deleteOnExit();
    }
}
