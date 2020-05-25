package net.otlg.bitrumen.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Output {

    ZipEntry zipEntry;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private State state = State.COMMIT;

    public Output() {
    }

    public Output(ZipEntry zipEntry) {
        this.zipEntry = zipEntry;
    }

    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    public void setZipEntry(ZipEntry zipEntry) {
        this.zipEntry = zipEntry;
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setBytes(byte[] data) throws IOException {
        outputStream = new ByteArrayOutputStream();
        outputStream.write(data);
    }

    public void write(ZipOutputStream zipOutputStream) throws IOException {
        byte[] data = getOutputStream().toByteArray();

        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(-1);

        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(data);
    }

    public static enum State {
        /**
         * Submit the current state
         */
        COMMIT,
        /**
         * Ignore the current state and passthroughs input
         */
        PASSTHROUGHS,
        /**
         * Discard and remove output target (e.g. file)
         */
        DISCARD
    }
}
