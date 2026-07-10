package com.yurpha.outgoingengine;

import com.yurpha.outgoingengine.UI.EngineApp;
import com.yurpha.outgoingengine.build.BuildManager;
import javafx.application.Application;
public class Main {

    public static void main(String[] args){

        BuildManager.initializeEngineWorkspace();
        Application.launch(EngineApp.class, args);

    }
}
