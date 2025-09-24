package server;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class Main {

    private static final Map<Integer, File> treeMap = takeMap();
    private static ServerSocket server;
    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");
        String address = "127.0.0.1"; // Адрес сервера (локальный)
        int port = 23456; // Порт сервера
        server = new ServerSocket(port, 50, InetAddress.getByName(address));
        try {
            // Основной цикл сервера — слушает клиентов, пока сервер "жив"
            while (ServerConnection.isServerRunning()) {
                // Ждём подключения клиента
                Socket clientSocker = server.accept();
                new Thread(() -> {
                    try {
                        start(clientSocker);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start(); // Запускаем обработку клиента
            }
        } catch (IOException e) {
            // Если сервер был остановлен намеренно — не выводим ошибку
            if (!ServerConnection.isServerRunning()) {
                //System.out.println("Server stopped!");
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
            String[] respond = {"", ""};
            switch (receivedRequest.split("\\s+")[0]) {
                case "EXIT":
                    savaTheMap();
                    connection.sendMessage("200 Server shutting down");
                    ServerConnection.stopServer();
                    server.close();
                    connection.close();
                    return;
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
    private static String createFile(String receivedRequest) {
        String[] commands = receivedRequest.split(" ", 6);
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
        String[] commands = receivedRequest.split(" ", 3);
        File file;
        if (commands[1].equals("BY_NAME")){
            File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
            file = new File(path,commands[2]);
        } else {
            file = getFileById(Integer.parseInt(commands[2]));
        }

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
        String[] commands = receivedRequest.split(" ", 3);
        File file;
        if (commands[1].equals("BY_NAME")){
            if (commands[2].contains("\\"))
            {
                int index = commands[2].lastIndexOf("\\");
                commands[2] = commands[2].substring(0, index) + "\\" + commands[2].substring(index + 1);
            }
            File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
            file = new File(path,commands[2]);
        } else {
            file = getFileById(Integer.parseInt(commands[2]));
        }
        String[] result = {"", ""};
        try {
            if (file.exists() && file.isFile()) {
                result[0] = "200";//OK
                try{
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line = "";
                    while ((line = reader.readLine()) != null){
                        result[1] += line + "\n";
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

    private static void savaTheMap(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("map.json");

        try{
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, treeMap);
        } catch (IOException ignored){
        }
    }

    private static Map<Integer, File> takeMap(){
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("map.json");
        if (!file.exists() || file.length() == 0){
            return new TreeMap<>();
        }
        try{
            Map<Integer, String> tempMap = mapper.readValue(file, new TypeReference<>() {
            });
            Map<Integer, File> resultMap = new TreeMap<>();
            for (Map.Entry<Integer, String> entry : tempMap.entrySet()){
                resultMap.put(entry.getKey(), new File(entry.getValue()));
            }
            return resultMap;
        } catch (IOException e){
            return new TreeMap<>();
        }
    }
}