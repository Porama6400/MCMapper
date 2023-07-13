package dev.porama.mcmapper;

import dev.porama.bitumen.StreamUtil;
import dev.porama.bitumen.pipe.ZipPipe;
import dev.porama.mcmapper.adapter.MojangAPI;
import dev.porama.mcmapper.adapter.container.DownloadEntry;
import dev.porama.mcmapper.adapter.container.VersionDetails;
import dev.porama.mcmapper.adapter.container.VersionInfo;
import dev.porama.mcmapper.module.JarTransformer;
import dev.porama.mcmapper.module.JarVerifier;
import dev.porama.mcmapper.module.visitor.ClassTransformer;
import dev.porama.mcmapper.util.MapperLogger;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class MCMapper {
    public static final Logger logger = new MapperLogger();

    private static final Options OPTIONS;

    static {
        OPTIONS = new Options();

        OPTIONS.addOption(new Option("ver", "version", true, "version"));
        OPTIONS.addOption(new Option("in", "input", true, "File to perform mapping on"));
        OPTIONS.addOption(new Option("map", true, "Obfuscation map file"));
        OPTIONS.addOption(new Option("output", "output", true, "File to write to"));
        OPTIONS.addOption(new Option("client", true, "Use with 'version' option, "));

        OPTIONS.addOption(new Option("?", "help", false, "Show help page"));
        OPTIONS.addOption(new Option("verify", false, "Whether to verify jar file"));
        OPTIONS.addOption(new Option("rename", "renamevar", false, "Attempt to rename local var based on their class"));
        OPTIONS.addOption(new Option("thread", true, "Number of threads to use"));
    }

    public static void main(String[] args) throws IOException {
        DefaultParser parser = new DefaultParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(MCMapper.OPTIONS, args);
        } catch (ParseException exception) {
            logger.info(exception.getMessage());
            logger.info("For more information, use \"java -jar MCMapper.jar -help\"");
            return;
        }

        if (commandLine.hasOption("help")) {
            logger.info("java -jar MCMapper.jar [arguments]");
            logger.info("");
            logger.info("Example:");
            logger.info("Map a server jar: java -jar MCMapper.jar -version 1.20.1");
            logger.info("Map a clientJar jar:  java -jar MCMapper.jar -version 1.20.1 -clientJar true");
            logger.info("Map a custom jar: java -jar MCMapper.jar -in input.jar -map map.txt -out output.jar");
            logger.info("");
            logger.info("Then output can be decompile, for an example");
            logger.info("mkdir out");
            logger.info("java -jar fernflower.jar output.jar out");
            logger.info("");
            logger.info("-version [version]     - build from specific version");
            logger.info("-in [jar file]         - set jar file to map");
            logger.info("-map [map file]        - set obfuscation map to read from");
            logger.info("-out [jar out]         - set output jar file");
            logger.info("-thread [n]            - set number of threads to use");
            logger.info("-renamevar false       - disable renaming");
            logger.info("-verify                - verify jar file after mapping process");
            return;
        }


        /*
         * configure input file
         */
        boolean clientJar = commandLine.hasOption("clientJar");
        String fileInPath = (clientJar ? "client" : "server") + ".jar";
        String fileMapPath = "map.txt";

        if (commandLine.hasOption("input")) {
            fileInPath = commandLine.getOptionValue("input");
        }

        if (commandLine.hasOption("map")) {
            fileMapPath = commandLine.getOptionValue("map");
        }

        File fileIn = new File(fileInPath);
        fileIn.deleteOnExit();
        File fileMap = new File(fileMapPath);
        fileMap.deleteOnExit();

        /*
         * configure output file
         */
        File fileOut;
        if (commandLine.hasOption("output")) {
            fileOut = new File(commandLine.getOptionValue("output"));
        } else {
            fileOut = new File(fileInPath.substring(0, fileInPath.length() - 4) + "-mapped.jar");
        }

        /*
         * Thread count setting
         */
        int threadCount = Runtime.getRuntime().availableProcessors();
        if (commandLine.hasOption("thread")) {
            try {
                threadCount = Integer.parseInt(commandLine.getOptionValue("thread"));
            } catch (NumberFormatException ex) {
                MCMapper.logger.severe(commandLine.getOptionValue("thread") + " is not a number!");
                return;
            }
        }

        /*
         * Rename variable setting
         */
        if (!commandLine.hasOption("renamevar") || !commandLine.getOptionValue("renamevar").equals("false")) {
            // TODO pass setting into instance instead
            ClassTransformer.flagTransformLocalVarName = true;
        }


        if (commandLine.hasOption("version")) {
            executeDownload(commandLine.getOptionValue("version"), clientJar, fileIn, fileMap);
        }

        executeTransform(threadCount, fileIn, fileMap, fileOut, commandLine.hasOption("verify"));
    }

    public static void executeDownload(String version, boolean client, File fileIn, File fileMap) throws IOException {
        final HashMap<String, VersionInfo> versionList = MojangAPI.fetchVersionList();

        final VersionInfo versionInfo = versionList.get(version);

        if (versionInfo == null)
            throw new IOException("Unknown version " + version);

        System.out.println("Using version: " + versionInfo.toString());
        final VersionDetails versionDetails = versionInfo.fetchDetails();

        DownloadEntry jarFileEntry = client ? versionDetails.getClient() : versionDetails.getServer();
        DownloadEntry mapFileEntry = client ? versionDetails.getClientMap() : versionDetails.getServerMap();

        jarFileEntry.download(fileIn);
        mapFileEntry.download(fileMap);
    }

    public static void executeTransform(int threadCount, File fileIn, File fileMap, File fileOut, boolean verify) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        File innerJar = new File("./inner.jar");
        innerJar.deleteOnExit();
        AtomicBoolean hasInnerJar = new AtomicBoolean(false);
        logger.info("Running using " + threadCount + " threads");

        logger.info("Performing preflight");
        Pattern jarPattern = Pattern.compile("META-INF/versions/[0-9.]+/server-[0-9.]+.jar");
        ZipPipe pipe = new ZipPipe(executor, (in, out) -> {
            String name = in.getZipEntry().getName();
            if (jarPattern.matcher(name).matches()) {
                System.out.println("Found inner version file, mapping that instead... (1.18+ mode)");
                StreamUtil.saveStream(in.getInputStream(), innerJar);
                hasInnerJar.set(true);
            }
        });
        pipe.process(fileIn, null);

        if (hasInnerJar.get()) {
            fileIn = innerJar;
        }

        logger.info("Transforming");
        try {
            JarTransformer.transformJar(executor, fileIn, fileMap, fileOut);

            if (verify) {
                MCMapper.logger.info("Verifying...");
                JarVerifier.verify(fileOut);
            }
        } finally {
            executor.shutdown();
        }

        MCMapper.logger.info("Done!");
    }
}
