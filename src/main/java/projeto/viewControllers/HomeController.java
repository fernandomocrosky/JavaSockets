package projeto.viewControllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import projeto.LogUI;
import projeto.Session;
import projeto.handlers.JwtHandle;
import projeto.handlers.SceneHandler;

public class HomeController {

    @FXML
    private TextArea logArea;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button createFilme;

    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() {
        LogUI.init(logArea);

        // pega o usuário atual da Session
        String token = Session.getInstance().getToken();
        if (token != null) {
            String usuario = Session.getInstance().getUser().getUsuario();

            welcomeLabel.setText("Olá, " + usuario + "!");
        }

        if(!Session.getInstance().isAdmin()) {
            createFilme.setVisible(false);
        }
    }

    @FXML
    private void goCreateFilme() {
        try {
            SceneHandler.changeScene("/projeto/views/CadastroFilme.fxml");
        } catch (Exception e) {
            System.out.println("Erro ao trocar de tela: " + e.getMessage());
        }
    }

    // @FXML
    // private void logout() {
    // JsonObject logoutResponse = Session.getInstance().desconectar();
    // System.out
    // .println("\nServidor -> Cliente: " +
    // JsonHandler.prettyFormatFromString(logoutResponse.toString()));

    // if (logoutResponse.get("status").getAsString().equals(StatusCode.OK)) {
    // try {
    // FXMLLoader loader = new
    // FXMLLoader(getClass().getResource("/projeto/views/CONEXAO.fxml"));
    // Scene conexaoScene = new Scene(loader.load(), 420, 420);
    // Stage stage = (Stage) welcomeLabel.getScene().getWindow();
    // stage.setScene(conexaoScene);
    // } catch (Exception e) {
    // System.out.println("Erro ao trocar de tela: " + e.getMessage());
    // }
    // } else {
    // showError(logoutResponse.get("message").getAsString());
    // }
    // }
}
