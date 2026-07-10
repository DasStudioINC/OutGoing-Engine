package com.yurpha.outgoingengine.UI.codeeditor;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorIndentEngine {

    public static void configureIndentation(CodeArea codeArea) {
        // Capture keys via filter to lock down formatting events cleanly
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                codeArea.insertText(codeArea.getCaretPosition(), "    ");
            }
            else if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                handleAutoIndent(codeArea);
            }
        });
    }

    private static void handleAutoIndent(CodeArea codeArea) {
        int currentParagraph = codeArea.getCurrentParagraph();
        String currentLineText = codeArea.getParagraph(currentParagraph).getText();

        String leadingSpaces = "";
        Matcher matcher = Pattern.compile("^\\s*").matcher(currentLineText);
        if (matcher.find()) {
            leadingSpaces = matcher.group();
        }

        int caretPosition = codeArea.getCaretPosition();

        // Check if block bracket layout formatting is required
        if (currentLineText.trim().endsWith("{")) {
            String indentInsideBlock = leadingSpaces + "    ";
            String closingBraceLine = "\n" + leadingSpaces + "}";

            // Build text fragment layout structure securely in a single batch
            codeArea.insertText(caretPosition, "\n" + indentInsideBlock + closingBraceLine);

            // Focus caret precisely inside the newly opened structure scope
            codeArea.moveTo(caretPosition + 1 + indentInsideBlock.length());
        } else {
            codeArea.insertText(caretPosition, "\n" + leadingSpaces);

        }
    }
}