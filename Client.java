import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Client {
    private static final String LOG_FILE = "chat.txt";

    public static void main(String[] args) {
        try {
            // Solicitar IP e porta
            Scanner scanner = new Scanner(System.in);
            System.out.println("Digite o IP do servidor (ex.: 192.168.1.100):");
            String ip = scanner.nextLine();
            System.out.println("Digite a porta (ex.: 8080):");
            int port = Integer.parseInt(scanner.nextLine());

            // Conectar ao servidor
            Socket socket = new Socket(ip, port);
            System.out.println("Conectado ao servidor " + ip + ":" + port);

            // Inicializar streams
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            String message;
            // Loop para enviar/receber mensagens
            while (true) {
                System.out.println("Digite uma mensagem (ou 'exit' para sair):");
                message = console.readLine();
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                // Enviar mensagem ao servidor
                out.println(message);
                saveToFile("[" + timestamp + "] Cliente: " + message);

                // Receber resposta do servidor
                String response = in.readLine();
                System.out.println("Servidor: " + response);
                saveToFile("[" + timestamp + "] Servidor: " + response);

                // Verificar se o cliente quer sair
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Conexão encerrada.");
                    break;
                }
            }

            // Fechar conexão
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Função para salvar mensagens no arquivo
    private static void saveToFile(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}