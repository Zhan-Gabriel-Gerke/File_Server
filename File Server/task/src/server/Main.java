package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");
        String address = "127.0.0.1"; // Адрес сервера (локальный)
        int port = 23456; // Порт сервера

        // Создаём серверный сокет
        // Основной серверный сокет
        ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address));
        try {
            // Основной цикл сервера — слушает клиентов, пока сервер "жив"
            while (ServerConnection.isServerRunning()) {
                // Ждём подключения клиента
                Socket clientSocker = server.accept();
                start(clientSocker); // Запускаем обработку клиента
            }
        } catch (IOException e) {
            // Если сервер был остановлен намеренно — не выводим ошибку
            if (!ServerConnection.isServerRunning()) {
                //System.out.println("Server stopped!");
            } else {
                throw e; // если ошибка не связана с остановкой — пробрасываем дальше
            }
        }
    }
    public static void start(Socket clientSocker) throws IOException {
        // Создаём объект для удобного общения с клиентом
        ServerConnection connection = new ServerConnection(clientSocker);

        // Пока соединение открыто — читаем запросы
        while (!connection.isClosed()) {
            String receivedRequest;
            try {
                receivedRequest = connection.getInput();
            } catch (IOException e) {
                connection.close(); // если ошибка — закрываем соединение
                break;
            }

            label:
            while (true) {
                String[] respond = {"", ""};
                String[] commands = receivedRequest.split(" ", 3);
                switch (commands[0]) {
                    case "EXIT":
                        break label;
                    case "PUT":
                        //add
                        respond[0] = createFile(commands[1], commands[3]);
                        break;
                    case "DELETE":
                        //delete
                        respond[0] = deleteFile(commands[1]);
                        break;
                    case "GET":
                        //get
                        respond = getFile(commands[1]);
                        break;
                }
                String respondToClient;
                if (commands[0].equals("GET")) {
                    respondToClient = respond[0] + " FILE_CONTENT " + respond[1];
                } else{
                    respondToClient = respond[0];
                }
                // Отправляем клиенту ответ
                connection.sendMessage(respondToClient);
                // Закрываем соединение после обработки
                connection.close();
            }
        }
    }
    public static String createFile(String name, String content) {
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,name);
        try {
            if (!file.exists() && file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write(content);
                writer.close();
                return "200";//OK
            } else {
                return "403";//Error
            }
        } catch (IOException e) {
            return "403";//Error
        }
    }

    public static String deleteFile(String name) {
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,name);
        try {
            if (file.delete()){
                return "200";//OK
            } else {
                return "404";//Error
            }
        } catch (Exception e) {
            return "404";//Error
        }
    }

    public static String[] getFile(String name) {
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,name);
        String[] result = new String[2];
        try {
            if (file.exists() && file.isFile()) {
                result[0] = "200";//OK
                try{
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    while (reader.readLine() != null){
                        result[1] += reader.readLine() + "\n";
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

            } else {
                result[0] = "404";//Error
            }
        } catch (Exception e){
            result[0] = "404";//Error
        }
        return result;
    }

}