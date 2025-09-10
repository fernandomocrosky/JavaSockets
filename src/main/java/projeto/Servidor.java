package projeto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;

import projeto.handlers.ClientHandler;

public class Servidor {
    private static DefaultListModel<String> clientesModel = new DefaultListModel<>();
    private static JTextArea logArea = new JTextArea();

    public static void main(String[] args) throws IOException {
        Database.init();

        // Configura interface Swing
        JFrame frame = new JFrame("Servidor - Clientes Conectados");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JList<String> clientesList = new JList<>(clientesModel);
        JScrollPane clientesScroll = new JScrollPane(clientesList);

        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(clientesScroll, BorderLayout.CENTER);
        painel.add(logScroll, BorderLayout.SOUTH);

        // Ajusta tamanho das áreas
        clientesScroll.setPreferredSize(new java.awt.Dimension(500, 200));
        logScroll.setPreferredSize(new java.awt.Dimension(500, 350));

        frame.add(painel);
        frame.setVisible(true);

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

                    // Atualiza Swing (precisa ser na EDT)
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        clientesModel.addElement(
                                clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                    });
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException ex) {
                System.err.println("Erro na comunicação com o cliente: " + ex.getMessage());
            }
        }
    }

    public static void log(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(mensagem + "\n");
        });
    }

    public static void removerCliente(String ip) {
        SwingUtilities.invokeLater(() -> clientesModel.removeElement(ip));
    }
}
