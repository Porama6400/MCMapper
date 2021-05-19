package net.otlg.mcmapper.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.otlg.mcmapper.gui.adapter.MojangAPI;
import net.otlg.mcmapper.gui.adapter.container.VersionInfo;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UIController {
    final List<String> versionListDisplayBuffer = new LinkedList<>();
    final LinkedList<String> logQueue = new LinkedList<>();
    final Image image = new Image(this.getClass().getResourceAsStream("/image.jpg"));
    private final ScheduledExecutorService executor;
    HashMap<String, VersionInfo> versionMap;
    boolean choiceBoxVersionUpdatePending = false;
    @FXML
    ImageView imageView;
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
        BuildController.runTaskBuild(executor, this);
    }

    public void init() {
        Platform.runLater(() -> {
            ObservableList items = choiceBoxJarType.getItems();
            items.add("Server");
            items.add("Client");
            choiceBoxJarType.setValue("Server");
            setUIState(UIState.BUSY);
            setUIState(UIState.INITIALIZING);

            executor.execute(() -> {
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
            });
        });
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
            case INITIALIZING:
                imageView.setImage(image);
                break;
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
