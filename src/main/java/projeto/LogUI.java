package projeto;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class LogUI {

    private static TextArea logArea;
    private static StringBuilder historico = new StringBuilder();

    public static void init(TextArea log) {
        log.setEditable(false);
        logArea = log;
    }

    // Adiciona mensagens no log
    public static void log(String mensagem) {
        historico.append(mensagem).append("\n");
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(mensagem + "\n"));
        }
    }
}
