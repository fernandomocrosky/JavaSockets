package projeto;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Session {
    private static final Session instance = new Session();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String token;

    private Session() {
    }

    public static Session getInstance() {
        return instance;
    }

    public void setConnection(Socket socket, PrintWriter out, BufferedReader in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void clear() {
        this.socket = null;
        this.out = null;
        this.in = null;
    }

    public void desconectar() {

    }
}
