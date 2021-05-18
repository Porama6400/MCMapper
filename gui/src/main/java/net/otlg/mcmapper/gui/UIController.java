package net.otlg.mcmapper.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.otlg.mcmapper.gui.adapter.DownloadEntry;
import net.otlg.mcmapper.gui.adapter.MojangAPI;
import net.otlg.mcmapper.gui.adapter.VersionDetails;
import net.otlg.mcmapper.gui.adapter.VersionInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UIController {
    final List<String> versionListDisplayBuffer = new LinkedList<>();
    final LinkedList<String> logQueue = new LinkedList<>();
    private final ScheduledExecutorService executor;
    HashMap<String, VersionInfo> versionMap;
    boolean choiceBoxVersionUpdatePending = false;

    @FXML
    TextArea textAreaConsole;
    @FXML
    ChoiceBox<String> choiceBoxVersion;
    @FXML
    TextField textFieldSearch;
    @FXML
    CheckBox checkBoxReleaseOnly;
    @FXML
    ChoiceBox<String> choiceBoxJarType;
    @FXML
    Button buttonBuild;

    public UIController(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @FXML
    void onSearchUpdate() {
        executor.schedule(() -> {
            applySearch();
            choiceBoxVersionUpdatePending = true;
        }, 50, TimeUnit.MILLISECONDS);
    }

    @FXML
    void onButtonBuildPressed() {
        setUIState(UIState.BUSY);

        final String version = choiceBoxVersion.getValue();
        final boolean server = choiceBoxJarType.getValue().equals("Server");

        executor.execute(() -> {
            try {
                VersionInfo versionInfo = versionMap.get(version);
                VersionDetails details = versionInfo.fetchDetails();

                DownloadEntry binaryDownloader = server ? details.getServer() : details.getClient();
                DownloadEntry mapDownloader = server ? details.getServerMap() : details.getClientMap();

                log("Preparing directory...");
                new File("./data/").mkdir();

                final String binaryPath = "./data/binary.jar";
                final String mapPath = "./data/map.txt";

                log("Downloading binary...");
                binaryDownloader.download(new File(binaryPath));

                log("Downloading mapping file...");
                mapDownloader.download(new File(mapPath));

                log("Download completed!");
                log("Mapping...");
                try {
                    Runtime runtime = Runtime.getRuntime();
                    Process exec = runtime.exec("java -jar MCMapper.jar -in " + binaryPath + " -map " + mapPath);
                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(exec.getInputStream()));

                    log("Mapper process spawned");

                    String message;
                    while (exec.isAlive()) {
                        try {
                            while ((message = inputStream.readLine()) != null) {
                                log(message);
                            }
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                log("Mapping completed!");

                executor.schedule(() -> {
                    Platform.runLater(() -> {
                        setUIState(UIState.READY);
                    });
                }, 2, TimeUnit.SECONDS);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void init() {
        Platform.runLater(() -> {
            ObservableList items = choiceBoxJarType.getItems();
            items.add("Server");
            items.add("Client");
            choiceBoxJarType.setValue("Server");
            setUIState(UIState.INITIALIZING);
        });

        try {
            log("Downloading Minecraft version list...");
            versionMap = MojangAPI.fetchVersionList();
            log("Download completed");
            applySearch();
            choiceBoxVersionUpdatePending = true;
            setUIState(UIState.READY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void attemptChoiceBoxVersionUpdate() {
        if (choiceBoxVersionUpdatePending) {
            choiceBoxVersionUpdatePending = false;
            ObservableList items = choiceBoxVersion.getItems();
            items.clear();
            items.addAll(versionListDisplayBuffer);

            if (versionListDisplayBuffer.size() > 0) {
                choiceBoxVersion.setValue(versionListDisplayBuffer.get(0));
            }
        }
    }

    public void applySearch() {
        applySearch(textFieldSearch.getText(), checkBoxReleaseOnly.isSelected());
    }

    public void applySearch(String searchKey, boolean releaseOnly) {
        boolean searchEnabled = searchKey != null && !searchKey.isEmpty();
        versionListDisplayBuffer.clear();
        for (String key : versionMap.keySet()) {
            if (searchEnabled && !key.contains(searchKey)) continue;
            if (releaseOnly && (key.contains("-") || key.contains("w"))) continue;
            versionListDisplayBuffer.add(key);
        }
        versionListDisplayBuffer.sort(String::compareTo);
        versionListDisplayBuffer.sort(Comparator.reverseOrder());
    }

    public void setUIState(UIState state) {
        switch (state) {
            case BUSY:
                choiceBoxVersion.setDisable(true);
                choiceBoxJarType.setDisable(true);
                checkBoxReleaseOnly.setDisable(true);
                textFieldSearch.setDisable(true);
                buttonBuild.setDisable(true);
                textAreaConsole.setVisible(true);
                break;
            case READY:
                choiceBoxVersion.setDisable(false);
                choiceBoxJarType.setDisable(false);
                checkBoxReleaseOnly.setDisable(false);
                textFieldSearch.setDisable(false);
                buttonBuild.setDisable(false);
                textAreaConsole.setVisible(false);
                break;
        }
    }

    public void tick() {
        if (choiceBoxVersionUpdatePending) {
            Platform.runLater(this::attemptChoiceBoxVersionUpdate);
        }
    }

    public void log(String info) {
        logQueue.add(info);
        System.out.println(info);
    }

    public void logClear() {
        Platform.runLater(() -> {
            textAreaConsole.clear();
        });
    }

    public void logUpdate() {
        Platform.runLater(() -> {
            while (!logQueue.isEmpty()) {
                textAreaConsole.appendText(logQueue.remove() + "\n");
            }
        });
    }
}
