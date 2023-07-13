package dev.porama.bitumen.pipe;

import dev.porama.bitumen.pipe.wrapper.Input;
import dev.porama.bitumen.pipe.wrapper.Output;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipPipe {

    private static Logger logger = Logger.getLogger("ZipPipe");
    private final Pipe pipe;
    private final ExecutorService executor;

    public ZipPipe(ExecutorService executor, Pipe pipe) {
        this.executor = executor;
        this.pipe = pipe;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        ZipPipe.logger = logger;
    }

    public void process(File fileIn, @Nullable File fileOut) throws IOException {
        ZipInputStream zipInputStream = null;
        ZipOutputStream zipOutputStream = null;

        try {
            zipInputStream = new ZipInputStream(new FileInputStream(fileIn));

            if (fileOut != null) {
                zipOutputStream = new ZipOutputStream(new FileOutputStream(fileOut));
            }

            process(zipInputStream, zipOutputStream);
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }

            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
        }
    }

    public void process(ZipInputStream zipInputStream, @Nullable ZipOutputStream zipOutputStream) throws IOException {
        AtomicInteger atomicInteger = new AtomicInteger();
        byte[] buffer = new byte[1024];

        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int readLength;
            while ((readLength = zipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, readLength);
            }

            Input input = new Input(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), zipEntry);
            Output output = new Output(zipEntry);
            atomicInteger.incrementAndGet();
            executor.submit(() -> {
                try {

                    pipe.process(input, output);

                    if (zipOutputStream != null) {
                        switch (output.getState()) {
                            case PASSTHROUGHS:
                                output.setBytes(input.getInputBytes());
                            case COMMIT:
                                synchronized (zipOutputStream) {
                                    output.writeToZip(zipOutputStream);
                                }
                                break;
                            case DISCARD:
                                // do nothing
                                break;
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                } finally {
                    atomicInteger.decrementAndGet();
                }
            });
        }

        int jobLeft;
        while ((jobLeft = atomicInteger.get()) > 0) {
            try {
                logger.info(jobLeft + " jobs left");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
