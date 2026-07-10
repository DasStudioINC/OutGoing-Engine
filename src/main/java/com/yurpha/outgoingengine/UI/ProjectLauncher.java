package com.yurpha.outgoingengine.UI;

import com.yurpha.outgoingengine.build.BuildManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class ProjectLauncher {

    public static String ACTIVE_PROJECT_NAME = null;


    public void start(Stage stage){
        stage.setTitle("OutGoing Engine - Project Selector");

        Label header = new Label("OutGoing Engine Project Page");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        ListView<String> projectListView = new ListView<>();
        projectListView.setPrefHeight(250);

        Path engineHome = BuildManager.getEngineHomeDirectories();
        Path projectDir = engineHome.resolve("Projects");

        ObservableList<String> projectNames = FXCollections.observableArrayList();
        File folder = projectDir.toFile();

        if(folder.exists() && folder.isDirectory()){
            File[] listOfFiles = folder.listFiles();
            if(listOfFiles != null){
                for(File file : listOfFiles){
                    if(file.isDirectory()){
                        projectNames.add(file.getName());
                    }
                }
            }
        }

        if(projectNames.isEmpty()){
            projectListView.setPlaceholder(new Label("No projects found in /Project folder."));
        } else {
            projectListView.setItems(projectNames);
        }

        Button openButton = new Button("Open Project");
        openButton.setDisable(true);

        projectListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            openButton.setDisable(newVal == null);
        });


        Runnable handleProjectSelection = () -> {
            String selectedProject = projectListView.getSelectionModel().getSelectedItem();
            if (selectedProject != null){
                ACTIVE_PROJECT_NAME = selectedProject;
                System.out.println("[LAUNCHER] Project selected: " + ACTIVE_PROJECT_NAME);

                stage.close();
                openMainEngineDashboard();
            }
        };

        openButton.setOnAction(e -> handleProjectSelection.run());

//        projectListView.setOnMouseClicked(event -> {
//            if (event.getClickCount() == 2 && projectListView.getSelectionModel().getSelectedItem() != null) {
//                handleProjectSelection.run();
//            }
//        });

        VBox layout = new VBox(15, header, projectListView, openButton);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #1e1e24");

        Scene scene = new Scene(layout, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void openMainEngineDashboard(){
        Stage engineStage = new Stage();

        EngineApp engineApp = new EngineApp(ACTIVE_PROJECT_NAME);

        try{
            engineApp.start(engineStage);
        } catch (Exception e) {
            System.err.println("Failed to open Engine Workspace: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
