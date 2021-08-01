package net.otlg.mcmapper.gui;

import javafx.application.Platform;
import net.otlg.bitumen.pipe.PipeAction;
import net.otlg.bitumen.pipe.ZipPipe;

import java.io.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BuildController {

    static final String MCMAPPER_PATH = "./MCMapper.jar";
    static final String FERNFLOWER_PATH = "./fernflower.jar";

    static final String DATA_PATH = "./data/";
    static final String DECOMPILED_PATH = "./decompiled/";
    static final String BINARY_PATH = "./data/binary.jar";
    static final String BINARY_MAPPED_PATH = "./data/binary-mapped.jar";
    static final String BINARY_MAPPED_STRIPPED_PATH = "./data/binary-mapped-stripped.jar";
    static final String MAP_PATH = "./data/map.txt";

    public static void runTaskMap(UIController ui, String version, boolean client) throws IOException, InterruptedException {
        if (!new File(MCMAPPER_PATH).exists()) {
            ui.log("can not find " + MCMAPPER_PATH);
            throw new FileNotFoundException(MCMAPPER_PATH);
        }

        Runtime runtime = Runtime.getRuntime();

        StringBuilder commandBuilder = new StringBuilder()
                .append("java -jar ").append(MCMAPPER_PATH)
                .append(" -in ").append(BINARY_PATH)
                .append(" -map ").append(MAP_PATH)
                .append(" -out ").append(BINARY_MAPPED_PATH)
                .append(" -version ").append(version);

        if (client)
            commandBuilder.append(" -client");

        Process exec = runtime.exec(commandBuilder.toString());

        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String temp;

        while (exec.isAlive()) {
            while ((temp = reader.readLine()) != null) {
                System.out.println(temp);
                if (temp.contains("jobs left")) {
                    ui.log(temp);
                }
            }
            Thread.sleep(1000);
        }
    }

    public static void runTaskDecompile(UIController ui) throws IOException, InterruptedException {
        if (!new File(FERNFLOWER_PATH).exists()) {
            ui.log("can not find " + FERNFLOWER_PATH);
            throw new FileNotFoundException(FERNFLOWER_PATH);
        }

        Runtime runtime = Runtime.getRuntime();
        Process exec = runtime.exec("java -jar " + FERNFLOWER_PATH + " " + BINARY_MAPPED_STRIPPED_PATH + " " + DECOMPILED_PATH);

        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        String temp;

        while (exec.isAlive()) {
            while ((temp = reader.readLine()) != null) {
                System.out.println(temp);
            }
            Thread.sleep(1000);
        }
    }

    public static void runTaskBuild(ScheduledExecutorService executor, UIController ui) {
        ui.setUIState(UIState.BUSY);

        final String version = ui.choiceBoxVersion.getValue();
        final boolean client = ui.choiceBoxJarType.getValue().equals("Client");

        executor.execute(() -> {
            try {

                ui.log("Preparing directory...");
                new File(DATA_PATH).mkdir();
                new File(DECOMPILED_PATH).mkdir();

                ui.log("Mapping...");
                runTaskMap(ui, version, client);
                ui.log("Mapping completed!");

                ui.log("Stripping unrelated components...");

                ZipPipe.setLogger(Logger.getLogger("ZipPipe"));
                ZipPipe zipPipe = new ZipPipe(executor, (input, output) -> {
                    String message = input.getZipEntry().getName();

                    if (message.startsWith("net/minecraft") || message.startsWith("com/mojang")) {
                        output.setState(PipeAction.PASSTHROUGHS);
                    } else {
                        output.setState(PipeAction.DISCARD);
                    }
                });
                zipPipe.process(new File(BINARY_MAPPED_PATH), new File(BINARY_MAPPED_STRIPPED_PATH));
                ui.log("Stripping complete...");

                ui.log("Decompiling...");
                runTaskDecompile(ui);
                ui.log("Decompiling completed!");

                ui.log("All done!");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                executor.schedule(() -> {
                    Platform.runLater(() -> {
                        ui.setUIState(UIState.READY);
                    });
                }, 2, TimeUnit.SECONDS);
            }
        });
    }
}
