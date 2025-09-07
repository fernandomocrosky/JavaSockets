package projeto.viewControllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.SceneHandler;

public class ConexaoController {

    @FXML
    private TextField ipField;
    @FXML
    private TextField portField;
    @FXML
    private Label status;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    private void conectar() {
        String ip;
        int porta;

        List<String> errors = Validator.validateFields(List.of("ip", "porta"), ipField, portField);

        if (!errors.isEmpty()) {
            status.setText(String.join("\n", errors));
            return;
        }

        try {
            ip = ipField.getText();
            porta = Integer.parseInt(portField.getText());
        } catch (Exception ex) {
            status.setText("Erro: ip deve ser um endereço IP e porta deve ser um número inteiro.");
            return;
        }

        try {
            socket = new Socket(ip, porta);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Session.getInstance().setConnection(socket, out, in);

            status.setText("Conectado!");
            SceneHandler.changeScene("/projeto/views/Login.fxml");

        } catch (Exception e) {
            status.setText("Erro: " + e.getMessage());
        }
    }
}
