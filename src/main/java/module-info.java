module OutGoingEngine {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.compiler;
    requires org.fxmisc.richtext;
    requires reactfx;


    //opens com.yurpha.outgoingengine to

    exports com.yurpha.outgoingengine;
    exports com.yurpha.outgoingengine.UI;
    exports com.yurpha.outgoingengine.UI.enginewindows;

    opens com.yurpha.outgoingengine to javafx.graphics, javafx.fxml;
    opens com.yurpha.outgoingengine.UI to javafx.graphics, javafx.fxml;
    exports com.yurpha.outgoingengine.UI.codeeditor;
    opens com.yurpha.outgoingengine.UI.codeeditor to javafx.fxml, javafx.graphics;
}