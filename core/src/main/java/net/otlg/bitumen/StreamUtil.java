package net.otlg.bitumen;

import java.io.*;

public class StreamUtil {
    public static byte[] toByteArray(InputStream input) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copyStream(input, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[1024];

        int readSize;
        while ((readSize = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, readSize);
        }
    }

    public static void saveStream(InputStream inputStream, File output) {
        try (FileOutputStream outputStream = new FileOutputStream(output)) {
            copyStream(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
