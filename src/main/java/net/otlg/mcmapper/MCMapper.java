package net.otlg.mcmapper;

import net.otlg.bitrumen.pipe.ZipPipe;
import net.otlg.mcmapper.module.JarTransformer;
import net.otlg.mcmapper.module.JarVerifier;
import net.otlg.mcmapper.util.MapperLogger;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MCMapper {


    public static final Logger logger = new MapperLogger();
    public static ExecutorService executor;

    public static void main(String[] args) throws IOException, ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Option optionInput = new Option("in", "input", true, "File to perform mapping on");
        optionInput.setRequired(true);
        options.addOption(optionInput);

        Option optionMap = new Option("map", true, "Obfuscation map file");
        optionMap.setRequired(true);
        options.addOption(optionMap);

        options.addOption(new Option("output", "output", true, "File to write to"));

        options.addOption(new Option("?", "help", false, "Show help page"));
        options.addOption(new Option("verify", false, "Whether to verify jar file"));
        options.addOption(new Option("thread", true, "Number of threads to use"));

        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException exception) {
            logger.info(exception.getMessage());
            logger.info("For more information, use \"java -jar MCMapper.jar -help\"");
            return;
        }

        if (commandLine.hasOption("help")) {
            logger.info("java -jar MCMapper.jar [arguments]");
            logger.info("-in [jar file]         - set jar file to map");
            logger.info("-map [map file]        - set obfuscation map to read from");
            logger.info("-out [jar out]         - set output jar file");
            logger.info("-thread [n]            - set number of threads to use");
            logger.info("-verify                - verify jar file after mapping process");
            return;
        }

        ZipPipe.setLogger(logger);

        String pathIn = commandLine.getOptionValue("input");
        File fileIn = new File(pathIn);
        File fileMap = new File(commandLine.getOptionValue("map"));
        File fileOut;

        if (commandLine.hasOption("output")) {
            fileOut = new File(commandLine.getOptionValue("output"));
        } else {
            fileOut = new File(pathIn.substring(0, pathIn.length() - 4) + "-out.jar");
        }

        int threadCount = 8;
        if (commandLine.hasOption("thread")) {
            try {
                threadCount = Integer.parseInt(commandLine.getOptionValue("thread"));
            } catch (NumberFormatException ex) {
                MCMapper.logger.severe(commandLine.getOptionValue("thread") + " is not a number!");
                return;
            }
        }
        executor = Executors.newFixedThreadPool(threadCount);
        logger.info("Running using " + threadCount + " threads");

        JarTransformer.transformJar(fileIn, fileMap, fileOut);

        if (commandLine.hasOption("verify")) {
            MCMapper.logger.info("Verifying...");
            JarVerifier.verify(fileOut);
        }

        MCMapper.logger.info("Done!");
        executor.shutdown();
    }
}
