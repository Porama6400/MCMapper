package dev.porama.mcmapper.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    public static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int readSize;

        while ((readSize = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, readSize);
        }
    }

    public static byte[] streamToByteArray(InputStream stream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copyStream(stream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static String streamToString(InputStream stream) throws IOException {
        return new String(streamToByteArray(stream));
    }
}
