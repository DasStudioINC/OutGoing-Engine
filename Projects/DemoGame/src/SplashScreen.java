
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    private final Stage splashStage;
    private final Runnable onSplashFinished;

    public SplashScreen(Stage stage, Runnable onSplashFinished) {
        this.splashStage = stage;
        this.onSplashFinished = onSplashFinished;
    }

    public void show() {
        // 1. Set window formatting
        splashStage.initStyle(StageStyle.TRANSPARENT);

        ImageView splashImage = new ImageView();
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER);

        // Default sizing
        double width = 600;
        double height = 400;

        try {
            // Try absolute root path first
            var imageStream = getClass().getResourceAsStream("/assets/textures/splash.png");

            // Fallback option if the stream returns null
            if (imageStream == null) {
                imageStream = getClass().getResourceAsStream("assets/textures/splash.png");
            }

            if (imageStream != null) {
                System.out.println("[SPLASH LOG] Graphic successfully located!");
                Image img = new Image(imageStream);
                splashImage.setImage(img);
                width = img.getWidth();
                height = img.getHeight();
                root.getChildren().add(splashImage);
            } else {
                System.out.println("[SPLASH LOG] WARNING: Stream returned null. File not found in build path.");
                // Safe fallback so you see an actual window instead of a blank screen
                root.setStyle("-fx-background-color: #1e1e24; -fx-background-radius: 10;");
            }
        } catch (Exception e) {
            System.out.println("[SPLASH LOG] Exception reading file: " + e.getMessage());
            root.setStyle("-fx-background-color: #1e1e24; -fx-background-radius: 10;");
        }

        // 2. Scene layout configuration
        Scene scene = new Scene(root, width, height);
        scene.setFill(Color.TRANSPARENT);

        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();

        // 3. Trigger the animation sequence
        runAnimationSequence(root);
    }

    private void runAnimationSequence(StackPane root) {
        // Fade In
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Fade Out (Triggered after holding)
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setDelay(Duration.seconds(2.5)); // How long the logo stays visible

        // When fade out finishes, shut down this stage and launch the actual game window
        fadeOut.setOnFinished(event -> {
            splashStage.close();
            Platform.runLater(onSplashFinished);
        });

        fadeIn.play();
        fadeIn.setOnFinished(e -> fadeOut.play());
    }
}