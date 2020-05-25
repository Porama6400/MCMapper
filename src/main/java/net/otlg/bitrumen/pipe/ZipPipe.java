package net.otlg.bitrumen.pipe;

import net.otlg.bitrumen.wrapper.Input;
import net.otlg.bitrumen.wrapper.Output;
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
    private ExecutorService executor;

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

    public void process(File in, @Nullable File out) throws IOException {
        ZipInputStream zipInputStream = null;
        ZipOutputStream zipOutputStream = null;
        AtomicInteger atomicInteger = new AtomicInteger();

        try {
            zipInputStream = new ZipInputStream(new FileInputStream(in));
            if (out != null) zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
            final ZipOutputStream finalZipOutputStream = zipOutputStream;

            ZipEntry zipEntry;
            byte[] buffer = new byte[100000];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {


                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int length;
                while ((length = zipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }

                Input input = new Input(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), zipEntry);
                Output output = new Output(zipEntry);
                atomicInteger.incrementAndGet();
                executor.submit(() -> {
                    try {

                        pipe.process(input, output);

                        if (finalZipOutputStream != null) {
                            synchronized (finalZipOutputStream) {
                                switch (output.getState()) {
                                    case PASSTHROUGHS:
                                        output.setBytes(input.getInputBytes());
                                    case COMMIT:
                                        output.write(finalZipOutputStream);
                                        break;
                                }
                            }
                        }

                        atomicInteger.decrementAndGet();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }

            int jobLeft;
            while ((jobLeft = atomicInteger.get()) > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                logger.info(jobLeft + " jobs left");
            }
        } finally {
            if (zipInputStream != null) zipInputStream.close();
            if (zipOutputStream != null) zipOutputStream.close();
        }
    }
}
