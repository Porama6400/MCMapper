package dev.porama.bitumen.exploder;

import dev.porama.bitumen.pipe.ZipPipe;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Exploder {

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        ZipPipe zipPipe = new ZipPipe(executor, (in, out) -> {
            try {
                ClassReader classReader = new ClassReader(in.getInputStream());
                ClassWriter classWriter = new ClassWriter(classReader, 0);
                ExploderVisitor exploderVisitor = new ExploderVisitor(Opcodes.ASM9, classWriter);
                classReader.accept(exploderVisitor, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        zipPipe.process(new File("E:\\Projects\\Java\\Bitumen\\build\\libs\\bitumen-2.0.1.jar"), null);
    }
}
