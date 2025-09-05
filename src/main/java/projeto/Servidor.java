package projeto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import projeto.handlers.ClientHandler;

public class Servidor {

    public static void main(String[] args) throws IOException {
        Database.init();
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));) {
            System.out.println("Digite a porta que o servidor deve utilizar: ");
            int porta = Integer.parseInt(br.readLine());

            try (
                    ServerSocket serverSocket = new ServerSocket(porta);) {
                System.out.println(String.format("Servidor %s escutando na porta %d",
                        InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort()));

                System.out.println("Criando socket de conexão com o cliente...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException ex) {
                System.err.println("Erro na comunicação com o cliente: " + ex.getMessage());
            }
        }
    }
}
