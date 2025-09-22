package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class Main {

    private static final Map<Integer, File> treeMap = new TreeMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");
        String address = "127.0.0.1"; // Адрес сервера (локальный)
        int port = 23456; // Порт сервера

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
    private static void start(Socket clientSocker) throws IOException {
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
                switch (receivedRequest.split("\\s+")[0]) {
                    case "EXIT":
                        break label;
                    case "PUT":
                        //add
                        respond[0] = createFile(receivedRequest);
                        break;
                    case "DELETE":
                        //delete
                        respond[0] = deleteFile(receivedRequest);
                        break;
                    case "GET":
                        //get
                        respond = getFile(receivedRequest);
                        break;
                }
                String respondToClient;
                if (receivedRequest.split("\\s+")[0].equals("GET")) {
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
    private static String createFile(String receivedRequest) {
        String[] commands = receivedRequest.split("", 5);
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File specialForlder = new File(path ,commands[3]);
        if (!specialForlder.exists()){
            specialForlder.mkdirs();
        }
        File file = new File(specialForlder,commands[1]);
        try {
            if (!file.exists() && file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write(commands[5]);
                writer.close();
                int id = addRecordToMap(file);
                return "200" + " Id " + id;//OK
            } else {
                return "403";//Error
            }
        } catch (IOException e) {
            return "403";//Error
        }
    }

    private static String deleteFile(String receivedRequest) {
        String[] commands = receivedRequest.split(" ", 2);
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,"name");
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

    private static String[] getFile(String receivedRequest) {
        String[] commands = receivedRequest.split(" ", 2);
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,"name");
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
    private static int addRecordToMap(File file){
        Map.Entry<Integer, File> lastEntry = ((TreeMap<Integer, File>) treeMap).lastEntry();
        if (lastEntry == null){
            treeMap.put(1, file);
            return 1;
        }
        int id = lastEntry.getKey() + 1;
        treeMap.put(id, file);
        return id;
    }

    private static File getFileById(int id){
        return treeMap.get(id);
    }
}