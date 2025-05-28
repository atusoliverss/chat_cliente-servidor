import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Client {
    private static final String LOG_FILE = "chat.txt";
    private static PrintWriter out; // Torna o PrintWriter acessível à thread de leitura

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Digite o IP do servidor (ex.: 192.168.1.100):");
            String ip = scanner.nextLine();

            System.out.println("Digite a porta (ex.: 8080):");
            int port = Integer.parseInt(scanner.nextLine());

            Socket socket = new Socket(ip, port);
            System.out.println("Conectado ao servidor " + ip + ":" + port);

            out = new PrintWriter(socket.getOutputStream(), true); // Inicializa o PrintWriter estático
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Inicia uma nova thread para ler mensagens do servidor
            Thread serverReaderThread = new Thread(new ServerReader(in));
            serverReaderThread.start();

            String message;
            while (true) {
                System.out.println("Digite uma mensagem (ou 'exit' para sair):");
                message = console.readLine();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                out.println(message);
                saveToFile("[" + timestamp + "] Cliente (enviado): " + message);

                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Conexão encerrada.");
                    // Interrompe a thread de leitura do servidor
                    serverReaderThread.interrupt();
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Erro na conexão ou comunicação: " + e.getMessage());
            // e.printStackTrace(); // Para depuração
        }
    }

    // Classe interna para ler mensagens do servidor em uma thread separada
    private static class ServerReader implements Runnable {
        private BufferedReader in;

        public ServerReader(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    System.out.println("\n[" + timestamp + "] " + serverMessage); // Imprime a mensagem
                    saveToFile("[" + timestamp + "] Cliente (recebido): " + serverMessage);
                    // Garante que o prompt "Digite uma mensagem" seja exibido novamente após a mensagem recebida
                    System.out.print("Digite uma mensagem (ou 'exit' para sair):\n");
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) { // Ignora exceção se a thread foi interrompida intencionalmente
                    System.err.println("Erro ao ler do servidor: " + e.getMessage());
                    // e.printStackTrace(); // Para depuração
                }
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    System.err.println("Erro ao fechar BufferedReader do servidor: " + e.getMessage());
                }
            }
        }
    }

    private static void saveToFile(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Erro ao salvar no arquivo de log: " + e.getMessage());
            // e.printStackTrace(); // Para depuração
        }
    }
}