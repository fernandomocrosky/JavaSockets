package projeto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

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
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        
        JPanel row = new JPanel(new FlowLayout());
        JTextField portaField = new JTextField(10);
        JButton startButton = new JButton("Iniciar Servidor");
        
        row.add(new JLabel("Porta:"));
        row.add(portaField);
        row.add(startButton);
        
        JLabel statusLabel = new JLabel("Status: aguardando...");
        
        // Adiciona linha dos botões
        inputPanel.add(row);
        // Adiciona status abaixo
        inputPanel.add(statusLabel);

        JPanel painel = new JPanel(new BorderLayout());
        painel.add(inputPanel, BorderLayout.NORTH);
        painel.add(clientesScroll, BorderLayout.CENTER);
        painel.add(logScroll, BorderLayout.SOUTH);

        clientesScroll.setPreferredSize(new java.awt.Dimension(500, 200));
        logScroll.setPreferredSize(new java.awt.Dimension(500, 350));

        frame.add(painel);
        frame.setVisible(true);

        startButton.addActionListener(e -> {
            try {
                int porta = Integer.parseInt(portaField.getText());

                if (porta < 0 || porta > 65535) {
                    throw new IllegalArgumentException("A porta deve estar entre 0 e 65535.");
                }

                startButton.setEnabled(false);
                new Thread(() -> listen(porta, frame)).start();
                statusLabel.setText(
                        "Servdiro escutando em " + InetAddress.getLocalHost().getHostAddress() + ":" + porta + "...");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Porta deve ser um número inteiro!", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Erro inesperado: " + ex.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void listen(int porta, JFrame frame) {
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));) {
            System.out.println("Digite a porta que o servidor deve utilizar: ");

            try (
                    ServerSocket serverSocket = new ServerSocket(porta);) {
                System.out.println(String.format("Servidor %s escutando na porta %d",
                        InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort()));

                System.out.println("Criando socket de conexão com o cliente...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        clientesModel.addElement(
                                clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                    });
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (IOException ex) {
                System.err.println("Erro na comunicação com o cliente: " + ex.getMessage());
            }
        } catch (Exception ex) {
            System.err.println("Erro na comunicação com o servidor: " + ex.getMessage());
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
