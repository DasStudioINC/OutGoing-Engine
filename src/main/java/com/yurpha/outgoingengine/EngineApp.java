package com.yurpha.outgoingengine;

import com.yurpha.outgoingengine.build.BuildManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



public class EngineApp extends Application {

    TextArea consoleOutput;

    @Override
    public void start(Stage stage){

        //Top bar (toolbar)
        HBox topBar = TOPBAR();


        // Left Panel
        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(new Label("Project Explorer (coming soon)"));
        leftPanel.setMinWidth(200);

        // Center Workspace
        StackPane center = new StackPane();
        center.getChildren().add(new Label("OutGoing Engine Workspace"));

        // Bottom Console
        VBox bottomPanel = new VBox();
        consoleOutput = new TextArea();
        bottomPanel.getChildren().add(consoleOutput);
        bottomPanel.setMinHeight(120);

        // Root Layout
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setLeft(leftPanel);
        root.setCenter(center);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1000, 700);

        stage.setTitle("OutGoing Engine");
        stage.setScene(scene);
        stage.show();
    }


    private HBox TOPBAR(){
        HBox parent = new HBox(10);

        Button buildButton = new Button("Build Game");
        buildButton.setOnAction(e -> {
            String log = BuildManager.runBuild("DemoGame", "Main", null);
            consoleOutput.setText(log);
        });

        Button projectButton = new Button("Projects");

        parent.getChildren().addAll(buildButton, projectButton);

        return parent;
    }
}
