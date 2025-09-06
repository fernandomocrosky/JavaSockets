package projeto.viewControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import projeto.Session;
import projeto.handlers.JwtHandle;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label infoLabel;

    @FXML
    public void initialize() {
        // pega o usuário atual da Session
        String token = Session.getInstance().getToken();
        if (token != null) {
            String usuario = JwtHandle.getClaim(Session.getInstance().getToken(), "usuario");

            welcomeLabel.setText("Olá, " + usuario + "!");
            infoLabel.setText(
                    "Seu token JWT: " + (token != null ? token.substring(0, 15) + "..." : "não encontrado"));
        } else {
            welcomeLabel.setText("Olá, visitante!");
            infoLabel.setText("Nenhum token encontrado.");
        }

    }
}
