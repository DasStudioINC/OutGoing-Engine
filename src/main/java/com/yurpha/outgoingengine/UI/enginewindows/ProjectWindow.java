package com.yurpha.outgoingengine.UI.enginewindows;

import com.yurpha.outgoingengine.UI.codeeditor.ScriptEditorWindow;
import com.yurpha.outgoingengine.data.DataGetter;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.nio.file.Path;
import java.util.List;

public class ProjectWindow extends VBox {

    private TextArea consoleOutput;
    private HBox projectsView;
    private HBox consoleView;


    public ProjectWindow(String currentProjectName) {
        // Set up layout spacing for this custom VBox panel
        this.setSpacing(10);
        build(currentProjectName);
    }

    private void build(String currentProjectName) {
        System.out.println("Build Started");

        // 1. Setup Tab Header Row
        HBox tabHeaderRow = new HBox(5);

        // 2. Setup the Pages Stack (StackPane overlays views perfectly for tab systems!)
        StackPane contentStack = new StackPane();
        VBox.setVgrow(contentStack, Priority.ALWAYS); // Tell the content area to take up all vertical room

        // --- Projects View ---
        projectsView = new HBox();
        ListView<Button> filesList = new ListView<>();
        HBox.setHgrow(filesList, Priority.ALWAYS); // Stretch the file list horizontally

        List<Path> projectFiles = DataGetter.getProjectSourceFiles(currentProjectName);
        for (Path filePath : projectFiles) {
            Button project = new Button(filePath.getFileName().toString());
            project.setOnAction(e ->  {
                ScriptEditorWindow window = new ScriptEditorWindow(filePath.toAbsolutePath());
                window.open();
            });
            filesList.getItems().add(project);
        }
        projectsView.getChildren().add(filesList);

        // --- Console View ---
        consoleView = new HBox();
        consoleOutput = new TextArea();
        HBox.setHgrow(consoleOutput, Priority.ALWAYS); // Stretch console text area horizontally
        consoleView.getChildren().add(consoleOutput);

        // Add both sub-views to our stack
        contentStack.getChildren().addAll(projectsView, consoleView);

        // Start with only the project view visible
        projectsView.setVisible(true);
        consoleView.setVisible(false);

        // --- Wire Up Header Button Logic ---
        Button projectTabButton = new Button("Project");
        projectTabButton.setOnAction(e -> {
             bottomAction("projects");
        });

        Button consoleTabButton = new Button("Console");
        consoleTabButton.setOnAction(e -> {
            bottomAction("console");
        });

        tabHeaderRow.getChildren().addAll(projectTabButton, consoleTabButton);

        // FIX: Add all elements directly to 'this' container so your layout renders!
        this.getChildren().addAll(tabHeaderRow, contentStack);
    }

    public TextArea Console() {
        return consoleOutput;
    }


    public void bottomAction(String action){
        if(action.equals("projects")){
            projectsView.setVisible(true);
            consoleView.setVisible(false);
        }else if(action.equals("console")){
            projectsView.setVisible(false);
            consoleView.setVisible(true);
        }
    }
}
