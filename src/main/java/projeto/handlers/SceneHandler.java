package projeto.handlers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import projeto.Session;

public class SceneHandler {
    public static final double SCENE_WIDTH = 1024;
    public static final double SCENE_HEIGHT = 768;

    public static void changeScene(String fxmlPath) {
        try {
            Stage stage = Session.getInstance().getCurrentStage();

            FXMLLoader loader = new FXMLLoader(SceneHandler.class.getResource(fxmlPath));
            Scene newScene = new Scene(loader.load(), SCENE_WIDTH, SCENE_HEIGHT);

            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
