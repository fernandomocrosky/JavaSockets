package projeto.handlers;

import java.util.function.Consumer;

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

    // Novo método que recebe um "callback" para injetar dados no controller
    public static <T> void changeSceneWithData(String fxmlPath, Consumer<T> controllerConsumer) {
        try {
            Stage stage = Session.getInstance().getCurrentStage();

            FXMLLoader loader = new FXMLLoader(SceneHandler.class.getResource(fxmlPath));
            Scene newScene = new Scene(loader.load(), SCENE_WIDTH, SCENE_HEIGHT);

            // pega o controller da nova tela
            T controller = loader.getController();

            // passa os dados para o controller via callback
            controllerConsumer.accept(controller);

            stage.setScene(newScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
