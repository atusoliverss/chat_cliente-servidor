import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList; // Alternativa thread-safe para ArrayList

public class Server {
    private static final String LOG_FILE = "chat.txt";
    // Usamos CopyOnWriteArrayList para segurança de thread em iterações e modificações
    // É uma boa escolha para listas que são frequentemente iteradas e raramente modificadas.
    private static List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Digite o IP do servidor (ex.: 192.168.1.100):");
            String ip = scanner.nextLine();

            System.out.println("Digite a porta (ex.: 8080):");
            int port = Integer.parseInt(scanner.nextLine());

            ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Servidor iniciado em " + ip + ":" + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            // e.printStackTrace(); // Para depuração
        }
    }

    // Método para transmitir uma mensagem a todos os clientes conectados
    public static void broadcastMessage(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // Imprime a mensagem de broadcast no console do servidor
        System.out.println("[" + timestamp + "] BROADCAST: " + message);
        saveToFile("[" + timestamp + "] BROADCAST: " + message);

        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message);
            } catch (Exception e) {
                // Se houver um erro ao escrever (cliente desconectado), o writer será removido depois
                System.err.println("Erro ao enviar mensagem para um cliente (pode estar desconectado): " + e.getMessage());
            }
        }
    }

    // Método auxiliar para salvar no arquivo de log do servidor
    private static void saveToFile(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Erro ao salvar no arquivo de log do servidor: " + e.getMessage());
            // e.printStackTrace(); // Para depuração
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Adiciona o writer deste cliente à lista de escritores para broadcast
                clientWriters.add(out);
                // Notifica todos os clientes que um novo cliente se conectou
                broadcastMessage(clientId + " entrou no chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    System.out.println("[" + timestamp + "] " + clientId + ": " + message);
                    saveToFile("[" + timestamp + "] " + clientId + ": " + message);

                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("Cliente " + clientId + " desconectado.");
                        // Notifica todos os clientes que este cliente saiu
                        broadcastMessage(clientId + " saiu do chat.");
                        break;
                    } else {
                        // Retransmite a mensagem para todos os outros clientes
                        broadcastMessage(clientId + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro de comunicação com o cliente " + clientId + ": " + e.getMessage());
                // e.printStackTrace(); // Para depuração
            } finally {
                try {
                    // Remove o writer da lista quando o cliente se desconecta
                    if (out != null) {
                        clientWriters.remove(out);
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao fechar socket do cliente " + clientId + ": " + e.getMessage());
                    // e.printStackTrace(); // Para depuração
                }
            }
        }
    }
}