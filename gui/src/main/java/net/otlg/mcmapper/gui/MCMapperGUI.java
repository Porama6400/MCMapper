package net.otlg.mcmapper.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MCMapperGUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);
        UIController controller = new UIController(executor);


        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/mcmapper-gui.fxml"));
        loader.setController(controller);
        Pane pane = loader.load();

        Scene scene = new Scene(pane, pane.getPrefWidth(), pane.getPrefHeight());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("MCMappper GUI");
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Stage is closing");
            executor.shutdown();
        });

        executor.schedule(controller::init, 100, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(controller::logUpdate, 250, 250, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(controller::tick, 1, 1, TimeUnit.SECONDS);
    }
}
