package net.otlg.mcmapper.gui;

import javafx.application.Platform;
import net.otlg.mcmapper.gui.adapter.container.DownloadEntry;
import net.otlg.mcmapper.gui.adapter.container.VersionDetails;
import net.otlg.mcmapper.gui.adapter.container.VersionInfo;

import java.io.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BuildController {
    public static void runTaskBuild(ScheduledExecutorService executor, UIController ui) {
        ui.setUIState(UIState.BUSY);

        final String version = ui.choiceBoxVersion.getValue();
        final boolean server = ui.choiceBoxJarType.getValue().equals("Server");

        executor.execute(() -> {
            try {
                VersionInfo versionInfo = ui.versionMap.get(version);
                VersionDetails details = versionInfo.fetchDetails();

                DownloadEntry binaryDownloader = server ? details.getServer() : details.getClient();
                DownloadEntry mapDownloader = server ? details.getServerMap() : details.getClientMap();

                ui.log("Preparing directory...");
                new File("./data/").mkdir();

                final String binaryPath = "./data/binary.jar";
                final String mapPath = "./data/map.txt";

                ui.log("Downloading binary...");
                binaryDownloader.download(new File(binaryPath));

                ui.log("Downloading mapping file...");
                mapDownloader.download(new File(mapPath));

                ui.log("Download completed!");
                ui.log("Mapping...");
                try {
                    if (!new File("./MCMapper.jar").exists()) {
                        ui.log("can not find MCMapper.jar!");
                        throw new FileNotFoundException("./MCMapper.jar");
                    }

                    Runtime runtime = Runtime.getRuntime();
                    Process exec = runtime.exec("java -jar MCMapper.jar -in " + binaryPath + " -map " + mapPath);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                    String temp;

                    ui.log("Queuing jobs...");

                    while (exec.isAlive()) {
                        while ((temp = reader.readLine()) != null) {
                            System.out.println(temp);
                            if (temp.contains("jobs left")) {
                                ui.log(temp);
                            }
                        }
                        Thread.sleep(1000);
                    }

                    ui.log("Mapping completed!");
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }

                executor.schedule(() -> {
                    Platform.runLater(() -> {
                        ui.setUIState(UIState.READY);
                    });
                }, 2, TimeUnit.SECONDS);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
