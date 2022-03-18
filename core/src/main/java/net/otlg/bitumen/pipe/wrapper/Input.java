package net.otlg.bitumen.pipe.wrapper;

import net.otlg.bitumen.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public class Input {
    private final InputStream inputStream;

    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    private final ZipEntry zipEntry;

    public Input(InputStream inputStream, ZipEntry zipEntry) {
        this.inputStream = inputStream;
        this.zipEntry = zipEntry;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public byte[] getInputBytes() throws IOException {
        return StreamUtil.toByteArray(inputStream);
    }
}
