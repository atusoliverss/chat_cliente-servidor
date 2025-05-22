import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Server {
    private static final String LOG_FILE = "chat.txt";

    public static void main(String[] args) {
        try {
            // Solicitar IP e porta
            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite o IP do servidor (ex.: 192.168.1.100):");
            String ip = scanner.nextLine();
            System.out.println("Digite a porta (ex.: 8080):");
            int port = Integer.parseInt(scanner.nextLine());

            ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
            System.out.println("Servidor iniciado em " + ip + ":" + port);

            // Loop para aceitar múltiplos clientes
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress().getHostAddress());

                // Criar nova thread para cada cliente
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe para gerenciar cada cliente
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
                // Inicializar streams de entrada e saída
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String message;
                // Loop para receber mensagens do cliente
                while ((message = in.readLine()) != null) {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    System.out.println("[" + timestamp + "] " + clientId + ": " + message);

                    // Salvar mensagem no arquivo
                    saveToFile("[" + timestamp + "] " + clientId + ": " + message);

                    // Enviar resposta ao cliente
                    out.println("Servidor recebeu: " + message);

                    // Verificar se o cliente quer sair
                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("Cliente " + clientId + " desconectado.");
                        break;
                    }

                    // Opcional: Enviar mensagem do servidor (para interação bidirecional)
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Digite resposta para " + clientId + ":");
                    String response = scanner.nextLine();
                    out.println("Servidor: " + response);
                    saveToFile("[" + timestamp + "] Servidor para " + clientId + ": " + response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Função para salvar mensagens no arquivo
        private void saveToFile(String message) {
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(message + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}