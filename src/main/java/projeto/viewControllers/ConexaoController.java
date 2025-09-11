package projeto.viewControllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import projeto.LogUI;
import projeto.Session;
import projeto.Validator;
import projeto.handlers.SceneHandler;

public class ConexaoController {

    @FXML
    private TextArea logArea;

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

        ipField.setDisable(true);
        portField.setDisable(true);
        LogUI.log("Tentando conectar... (" + ip + ":" + porta + ")");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Socket s = null;
                PrintWriter pw = null;
                BufferedReader br = null;

                try {
                    // Usa connect com timeout (ex.: 3000 ms)
                    s = new Socket();
                    s.connect(new InetSocketAddress(ip, porta), 3000);
                    // Timeout de leitura (evita travar em reads)
                    s.setSoTimeout(5000);

                    pw = new PrintWriter(s.getOutputStream(), true);
                    br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    // Salva na sessão e troca de cena na UI thread
                    Socket finalS = s;
                    PrintWriter finalPw = pw;
                    BufferedReader finalBr = br;
                    Platform.runLater(() -> {
                        Session.getInstance().setConnection(finalS, finalPw, finalBr);
                        status.setText("Conectado!");
                        LogUI.log("Conectado!");
                        SceneHandler.changeScene("/projeto/views/Login.fxml");
                    });
                } catch (UnknownHostException e) {
                    LogUI.log("Falha na conexão... (" + ip + ":" + porta + ")\n" + "Verifique o ip e porta novamente");
                    Platform.runLater(() -> status.setText("Host desconhecido: " + ip));
                    closeQuietly(s, pw, br);
                } catch (SocketTimeoutException e) {
                    Platform.runLater(() -> status.setText("Tempo esgotado ao conectar (timeout)."));
                    LogUI.log("Falha na conexão... (" + ip + ":" + porta + ")\n" + "Verifique o ip e porta novamente");
                    closeQuietly(s, pw, br);
                } catch (ConnectException e) {
                    Platform.runLater(() -> status.setText("Não foi possível conectar: " + e.getMessage()));
                    LogUI.log("Falha na conexão... (" + ip + ":" + porta + ")\n" + "Verifique o ip e porta novamente");
                    closeQuietly(s, pw, br);
                } catch (Exception e) {
                    Platform.runLater(() -> status.setText("Erro: " + e.getMessage()));
                    LogUI.log("Falha na conexão... (" + ip + ":" + porta + ")\n" + "Verifique o ip e porta novamente");
                    closeQuietly(s, pw, br);
                } finally {
                    // Reabilita inputs se não tiver mudado de cena
                    Platform.runLater(() -> {
                        ipField.setDisable(false);
                        portField.setDisable(false);
                    });
                }
                return null;
            }
        };

        Thread th = new Thread(task, "conectar-thread");
        th.setDaemon(true);
        th.start();
    }

    private void closeQuietly(Socket s, PrintWriter pw, BufferedReader br) {
        try {
            if (br != null)
                br.close();
        } catch (Exception ignored) {
        }
        try {
            if (pw != null)
                pw.close();
        } catch (Exception ignored) {
        }
        try {
            if (s != null && !s.isClosed())
                s.close();
        } catch (Exception ignored) {
        }
    }

    @FXML
    public void initialize() {
        LogUI.init(logArea);
    }
}
