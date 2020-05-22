package net.otlg.mcmapper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MapperLogger extends Logger {
    public MapperLogger() {
        super("MCMapper", null);

        for (Handler handler : getHandlers().clone()) {
            this.removeHandler(handler);
        }
        addHandler(new MCMapperLogHandler(new File("log.txt")));
    }

    public static class MCMapperLogHandler extends Handler {

        private static PrintWriter writer;
        private final File file;
        public MCMapperLogHandler(File file) {
            this.file = file;
            try {
                file.delete();
                writer = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public static PrintWriter getWriter() {
            return writer;
        }

        @Override
        public void publish(LogRecord record) {
            String message = String.format("[%s] %s", record.getLevel().getName(), record.getMessage());
            System.out.println(message);
            writer.println(message);
        }

        @Override
        public void flush() {
            writer.flush();
        }

        @Override
        public void close() throws SecurityException {
            writer.close();
        }
    }
}
