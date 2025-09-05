package projeto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import projeto.handlers.JsonHandler;
import projeto.models.User;
import projeto.requests.LoginRequest;

public class Cliente extends Application {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void hiddenUx(Node... nodes) {
        for (Node n : nodes) {
            n.setVisible(false);
            n.setManaged(false);
        }
    }

    public void showUx(Node... nodes) {
        for (Node n : nodes) {
            n.setVisible(true);
            n.setManaged(true);
        }
    }

    @Override
    public void start(Stage stage) {

        // Campos ip e porta
        TextField ipField = new TextField("192.168.1.10");
        ipField.setPromptText("Digite o IP do servidor");

        TextField portField = new TextField("21000");
        portField.setPromptText("Digite a porta do servidor");

        // Botão de conexão
        Button connectButton = new Button("Conectar");
        Label status = new Label("Desconectado");

        // Botão de disconnect
        Button disconnectButton = new Button("Desconectar");

        // === Campos de login ===
        TextField userField = new TextField();
        userField.setPromptText("Usuário");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Senha");
        Button loginButton = new Button("Login");
        hiddenUx(userField, passField, loginButton);

        connectButton.setOnAction(e -> {
            String ip = ipField.getText();
            int porta = Integer.parseInt(portField.getText());

            try {
                socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(ip, porta), 5000);
                socket.setSoTimeout(30000);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                status.setText("Conectando ao servidor...");
                new Thread(() -> {
                    String response;
                    try {
                        Platform.runLater(() -> status.setText("Conectado ao servidor"));
                        showUx(userField, passField, loginButton);
                        hiddenUx(ipField, portField, connectButton);

                        while ((response = in.readLine()) != null) {
                            String finalReponse = response;
                            System.out.println("Servidor: " + JsonHandler.prettyFormatFromString(finalReponse) + "\n");
                        }
                    } catch (java.net.SocketTimeoutException ex) {
                        Platform.runLater(() -> status.setText("Tempo limite de resposta excedido"));
                    } catch (Exception ex) {
                        System.out.println("Conexao encerrada.\n");
                    }
                }).start();
            } catch (Exception ex) {
                status.setText("Erro ao conectar ao servidor: " + ex.getMessage());

                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.seconds(2));
                pause.setOnFinished(ev -> status.setText("Conectar"));
                pause.play();
            }
        });

        loginButton.setOnAction(e -> {
            User user = new User(userField.getText(), passField.getText(), "");

            // Criação de request
            LoginRequest request = new LoginRequest(user);

            String msg = JsonHandler.modelToString(request);
            if (out != null && !msg.isEmpty()) {
                out.println(msg);
                System.out.println("Cliente: " + JsonHandler.prettyFormatFromString(msg) + "\n");
            }
        });

        disconnectButton.setOnAction(e -> {
            if (socket == null || socket.isClosed() || in == null || out == null)
                return;

            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (socket != null && !socket.isClosed())
                    socket.close();

                status.setText("Desconectado");
                System.out.println("Voce se desconectou do servidor.\n");

                out = null;
                in = null;
                socket = null;

                hiddenUx(userField, passField, loginButton);
                showUx(ipField, portField, connectButton);
            } catch (Exception ex) {
                status.setText("Erro ao desconectar: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(10, ipField, portField, connectButton, userField, passField, loginButton,
                disconnectButton, status);
        layout.setStyle("-fx-padding: 20px;");
        layout.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(layout, 420, 420));
        stage.setTitle("Cliente Servidor - JavaFX");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
