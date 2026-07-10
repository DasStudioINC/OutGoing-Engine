package com.yurpha.outgoingengine.UI.codeeditor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ReflectionIndexer {

    // Dynamically look up available methods for a class type string (e.g., "String" or "System")
    public static Set<String> getMethodsForClass(String className) {
        Set<String> methods = new TreeSet<>();
        try {
            Class<?> clazz = null;

            // Handle common global shortcuts automatically
            if (className.equals("String")) clazz = String.class;
            else if (className.equals("System")) clazz = System.class;
            else if (className.equals("Math")) clazz = Math.class;
            else {
                clazz = Class.forName(className);
            }

            if (clazz != null) {
                for (Method method : clazz.getMethods()) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        methods.add(method.getName() + "()");
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // Class isn't fully qualified or loaded yet, fail silently
        }
        return methods;
    }

    // Contextual fallback handler for packages
    public static List<String> getSuggestionsForPath(String inputPath) {
        List<String> suggestions = new ArrayList<>();
        if (inputPath.startsWith("import ")) {
            inputPath = inputPath.substring(7);
        }

        // Basic structural routing for common engine frames
        if (inputPath.startsWith("javafx.")) {
            String sub = inputPath.substring(7);
            if (sub.isEmpty()) return Arrays.asList("application", "stage", "scene", "control", "layout");
            if (sub.startsWith("scene.")) return Arrays.asList("control", "layout", "paint", "Scene");
        }
        if (inputPath.startsWith("java.util.")) {
            return Arrays.asList("List", "ArrayList", "HashMap", "Map", "Set", "HashSet", "Collections");
        }

        return suggestions;
    }
}