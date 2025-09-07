package projeto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import projeto.handlers.SceneHandler;

public class Cliente extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Session.getInstance().setCurrentStage(stage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/projeto/views/Conexao.fxml"));
        Scene scene = new Scene(loader.load(), SceneHandler.SCENE_WIDTH, SceneHandler.SCENE_HEIGHT);

        stage.setTitle("Cliente Servidor - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
