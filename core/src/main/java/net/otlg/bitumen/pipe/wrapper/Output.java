package net.otlg.bitumen.pipe.wrapper;

import net.otlg.bitumen.pipe.PipeAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Output {

    ZipEntry zipEntry;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PipeAction pipeAction = PipeAction.COMMIT;

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

    public PipeAction getState() {
        return pipeAction;
    }

    public void setState(PipeAction pipeAction) {
        this.pipeAction = pipeAction;
    }

    public void setBytes(byte[] data) throws IOException {
        outputStream = new ByteArrayOutputStream();
        outputStream.write(data);
    }

    public void writeToZip(ZipOutputStream zipOutputStream) throws IOException {
        byte[] data = getOutputStream().toByteArray();

        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(-1);

        ZipEntry newEntry = new ZipEntry(zipEntry.getName());
        newEntry.setComment(zipEntry.getComment());

        zipOutputStream.putNextEntry(newEntry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }
}
