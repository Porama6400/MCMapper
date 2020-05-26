package net.otlg.bitumen;

import java.io.*;

public class StreamUtil {
    public static byte[] toByteArray(InputStream stream) throws IOException {
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int bufferSize = 0;

        while ((bufferSize = stream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, bufferSize);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
