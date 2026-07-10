package com.yurpha.outgoingengine.UI;

import com.yurpha.outgoingengine.UI.enginewindows.ProjectWindow;
import com.yurpha.outgoingengine.build.BuildManager;
import com.yurpha.outgoingengine.data.DataGetter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;


public class EngineApp extends Application {

    ProjectWindow bottomPanel;

    //TextArea consoleOutput;
    private String currentProjectName;

    public EngineApp(){
        this.currentProjectName = "DemoGame";
    }

    public EngineApp(String currentProjectName){
        this.currentProjectName = currentProjectName;
    }


    @Override
    public void start(Stage stage){

        BuildManager.initializeEngineWorkspace();

        if(currentProjectName == null && ProjectLauncher.ACTIVE_PROJECT_NAME == null){
            ProjectLauncher launcher = new ProjectLauncher();
            launcher.start(stage);
            System.out.println("Test");
            return;
        }

        if(currentProjectName == null){
            currentProjectName = ProjectLauncher.ACTIVE_PROJECT_NAME;
        }

        //Top bar (toolbar)
        HBox topBar = TOPBAR();


        // Left Panel
        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(new Label("Project Explorer (coming soon)"));
        leftPanel.setMinWidth(200);

        // Center Workspace
        StackPane center = new StackPane();
        center.getChildren().add(new Label("OutGoing Engine - " + currentProjectName));

        // Bottom Console
        bottomPanel = new ProjectWindow(currentProjectName);
        bottomPanel.setMinHeight(120);

        // Root Layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(leftPanel);
        root.setCenter(center);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("OutGoing Engine - " + currentProjectName);
        stage.setScene(scene);
        stage.show();
    }


    private HBox TOPBAR(){
        HBox parent = new HBox(10);

        Button buildButton = new Button("Build Game");
        buildButton.setOnAction(e -> {
            bottomPanel.bottomAction("console");
            String log = BuildManager.runBuild(currentProjectName, "Main", null);
            bottomPanel.Console().setText(log);
        });

        Button projectButton = new Button("Projects");

        parent.getChildren().addAll(buildButton, projectButton);

        return parent;
    }





}
