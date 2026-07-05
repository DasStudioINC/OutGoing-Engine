import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // This is the logic that runs AFTER the splash screen fades out completely
        Runnable launchGameLoop = () -> {
            Stage gameStage = new Stage();
            // Set up your actual game view layout here
            // MainGameWindow mainGame = new MainGameWindow(gameStage);

            gameStage.setTitle("DemoGame - Running on OutGoing Engine");
            gameStage.setWidth(1280);
            gameStage.setHeight(720);
            gameStage.show();
        };

        // Instantiate and trigger the splash sequence
        SplashScreen splash = new SplashScreen(primaryStage, launchGameLoop);
        splash.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}