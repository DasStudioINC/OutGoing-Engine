package com.yurpha.outgoingengine.UI.codeeditor;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;

public class ScriptEditorWindow {

    private final Path filePath;
    private CodeArea codeArea;
    private Popup autocompletePopup;
    private ListView<String> suggestionList;

    private String ghostTextString = "";
    private int ghostTextPosition = -1;

    public ScriptEditorWindow(Path filePath) {
        this.filePath = filePath;
    }

    public void open() {
        Stage stage = new Stage();
        stage.setTitle("OutGoing Script Editor - " + filePath.getFileName().toString());

        codeArea = new CodeArea();
        codeArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px; -fx-text-fill: white");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        loadFileContent();
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
        EditorIndentEngine.configureIndentation(codeArea);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            syncSymbolsWithEditor(newText);
            codeArea.setStyleSpans(0, computeHighlighting(newText));
            handleLiveAutocomplete(newText);
        });

        setupAutocompletePopup(stage);

        BorderPane root = new BorderPane();
        root.setCenter(codeArea);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/editor-styles.css").toExternalForm());
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                saveFileContent();
                event.consume();
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    private void syncSymbolsWithEditor(String text) {
        List<String> foundSymbols = new ArrayList<>();
        String[] tokens = text.split("\\W+");
        for (String token : tokens) {
            if (token.length() >= 2 && !EditorKeywords.getAllKeyWords().contains(token)) {
                foundSymbols.add(token);
            }
        }
        EditorKeywords.setDiscoveredSymbols(foundSymbols);
    }

    private void handleLiveAutocomplete(String currentText) {
        int caretPos = codeArea.getCaretPosition();
        if (caretPos <= 0) {
            autocompletePopup.hide();
            ghostTextString = "";
            return;
        }

        char charBeforeCaret = codeArea.getText().charAt(caretPos - 1);
        if (!Character.isLetterOrDigit(charBeforeCaret)) {
            autocompletePopup.hide();
            ghostTextString = "";
            return;
        }

        String text = codeArea.getText().substring(0, caretPos);
        String[] words = text.split("\\W+");
        String lastWord = (words.length > 0) ? words[words.length - 1] : "";

        if (lastWord.length() < 2) {
            autocompletePopup.hide();
            ghostTextString = "";
            return;
        }

        Set<String> matches = new TreeSet<>();
        for (String item : EditorKeywords.getAllKeyWords()) {
            if (item.startsWith(lastWord) && !item.equals(lastWord)) matches.add(item);
        }

        Platform.runLater(() -> {
            if (!matches.isEmpty()) {
                suggestionList.getItems().setAll(matches);
                suggestionList.getSelectionModel().selectFirst();
                Bounds b = codeArea.caretBoundsProperty().getValue().orElse(null);
                if (b != null) autocompletePopup.show(codeArea.getScene().getWindow(), b.getMinX(), b.getMaxY() + 5);

                String firstMatch = matches.iterator().next();
                ghostTextString = firstMatch.substring(lastWord.length());
                ghostTextPosition = caretPos;
            } else {
                autocompletePopup.hide();
                ghostTextString = "";
            }
            codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
        });
    }

    private void setupAutocompletePopup(Stage stage) {
        autocompletePopup = new Popup();
        suggestionList = new ListView<>();
        suggestionList.setPrefSize(180, 120);

        // This handles the selection commit
        suggestionList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                insertSelectedSuggestion();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                autocompletePopup.hide();
            }
        });

        suggestionList.setOnMouseClicked(e -> insertSelectedSuggestion());
        autocompletePopup.getContent().add(suggestionList);

        // This ensures Enter key in the editor also commits the list if visible
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && autocompletePopup.isShowing()) {
                insertSelectedSuggestion();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER && !ghostTextString.isEmpty()) {
                codeArea.insertText(codeArea.getCaretPosition(), ghostTextString);
                ghostTextString = "";
                e.consume();
            }
        });
    }

    private void insertSelectedSuggestion() {
        String selection = suggestionList.getSelectionModel().getSelectedItem();
        if (selection != null) {
            int caretPos = codeArea.getCaretPosition();
            String fullText = codeArea.getText();
            String textBeforeCaret = fullText.substring(0, caretPos);
            String[] words = textBeforeCaret.split("\\W+");
            String lastWord = words[words.length - 1];

            codeArea.insertText(caretPos, selection.substring(lastWord.length()));
            autocompletePopup.hide();
            ghostTextString = "";
            codeArea.requestFocus();
        }
    }

    private void loadFileContent() {
        try {
            if (Files.exists(filePath)) codeArea.replaceText(Files.readString(filePath));
        } catch (IOException e) {
            codeArea.replaceText(0, 0, "Error loading file: " + e.getMessage());
        }
    }

    private void saveFileContent() {
        try {
            Files.writeString(filePath, codeArea.getText());
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = EditorKeywords.COMBINED_PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = matcher.group("MODIFIER") != null ? "modifier" :
                    matcher.group("TYPE") != null ? "data-type" :
                            matcher.group("CONTROL") != null ? "control-flow" :
                                    matcher.group("DECLARATION") != null ? "declaration" : "keyword";

            spansBuilder.add(Collections.singleton("plain-text"), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.singleton("plain-text"), text.length() - lastKwEnd);

        if (!ghostTextString.isEmpty()) {
            return spansBuilder.create().overlay(
                    new StyleSpansBuilder<Collection<String>>()
                            .add(Collections.emptyList(), ghostTextPosition)
                            .add(Collections.singleton("ghost-text"), ghostTextString.length())
                            .create(), (a, b) -> b.isEmpty() ? a : b
            );
        }
        return spansBuilder.create();
    }
}