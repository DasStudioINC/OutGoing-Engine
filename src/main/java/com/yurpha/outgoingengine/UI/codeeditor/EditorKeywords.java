package com.yurpha.outgoingengine.UI.codeeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class EditorKeywords {
    public static final String[] ACCESS_MODIFIERS = {"public", "private", "protected"};
    public static final String[] DATA_TYPES = {"int", "double", "float", "boolean", "void", "String", "null"};
    public static final String[] CONTROL_FLOW = {"if", "else", "for", "while", "return"};
    public static final String[] DECLARATIONS = {"class", "interface", "package", "import", "new"}; // Fixed typo 'DECLERATIONS'

    // 2. Pre-compiled combined regex pattern
    public static final Pattern COMBINED_PATTERN = Pattern.compile(
            "(?<MODIFIER>\\b(" + String.join("|", ACCESS_MODIFIERS) + ")\\b)"
                    + "|(?<TYPE>\\b(" + String.join("|", DATA_TYPES) + ")\\b)"
                    + "|(?<CONTROL>\\b(" + String.join("|", CONTROL_FLOW) + ")\\b)"
                    + "|(?<DECLARATION>\\b(" + String.join("|", DECLARATIONS) + ")\\b)"
    );

    // New list to hold variables/methods found by your tracker
    private static final List<String> discoveredSymbols = new ArrayList<>();

    public static void setDiscoveredSymbols(List<String> symbols) {
        discoveredSymbols.clear();
        discoveredSymbols.addAll(symbols);
    }

    public static List<String> getAllKeyWords() {
        List<String> all = new ArrayList<>();
        // Correctly populate the list with individual elements instead of array string formats
        all.addAll(Arrays.asList(ACCESS_MODIFIERS));
        all.addAll(Arrays.asList(DATA_TYPES));
        all.addAll(Arrays.asList(CONTROL_FLOW));
        all.addAll(Arrays.asList(DECLARATIONS));
        all.addAll(discoveredSymbols);
        return all;
    }
}