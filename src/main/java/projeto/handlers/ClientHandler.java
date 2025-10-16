package projeto.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;

import projeto.Multiplex;
import projeto.Servidor;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        String initMessage = String.format("Thread iniciada para o cliente %s:%d",
                clientSocket.getInetAddress().getHostAddress(),
                clientSocket.getPort());
        System.out.println(initMessage);

        try (
                Socket closedInTheEnd = clientSocket;
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));) {

            String clientData;
            Servidor.log(String.format("Nova conexão com o cliente %s:%d",
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));

            while ((clientData = in.readLine()) != null) {
                System.out.println("Cliente -> Servidor:\n " + JsonHandler.prettyFormatFromString(clientData));
                Servidor.log(
                        String.format("Cliente %s:%d -> Servidor:\n %s ",
                                clientSocket.getInetAddress().getHostAddress(),
                                clientSocket.getPort(), JsonHandler.prettyFormatFromString(clientData)));
                JsonElement response = Multiplex.handle(clientData);
                
                System.out.println("Servidor -> Cliente:\n " + JsonHandler.prettyFormatFromJson(response));

                out.println(JsonHandler.jsonToString(response));

                Servidor.log(String.format("Servidor -> Cliente %s:%d:\n %s",

                        clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort(),

                        JsonHandler.prettyFormatFromJson(response)));

                if (clientData.equalsIgnoreCase("exit")) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(
                    "Erro na comunicação com cliente " + clientSocket.getInetAddress() + ": " + ex.getMessage());
        } finally {
            Servidor.log(String.format("Fechando a conexão com o cliente %s:%d",
                    clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
            Servidor.removerCliente(
                    String.format("%s:%d", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
            System.out.println("Cliente desconectado: " +
                    clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        }
    }
}
