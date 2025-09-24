package server;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            byte[] fileBytes = new byte[0];
            String respond = "";
            switch (receivedRequest.split("\\s+")[0]) {
                case "EXIT":
                    saveTheMap();
                    connection.sendMessage("200 Server shutting down");
                    ServerConnection.stopServer();
                    server.close();
                    connection.close();
                    return;
                case "PUT":
                    //add
                    byte[] file = connection.getFile();
                    respond = createFile(receivedRequest, file);
                    break;
                case "DELETE":
                    //delete
                    respond = deleteFile(receivedRequest);
                    break;
                case "GET":
                    //get
                    fileBytes = getFile(receivedRequest);
                    break;
            }
            // Отправляем клиенту ответ
                if (receivedRequest.split("\\s+")[0].equals("GET")) {
                    if (fileBytes != null) {
                    connection.sendMessage("200");//OK
                    connection.sendFile(fileBytes);
                    }
                } else {
                    connection.sendMessage("404");//ERROR
                }
            // Закрываем соединение после обработки
            connection.close();
            }
        }
    private static String createFile(String receivedRequest, byte[] fileData) {
        String[] commands = receivedRequest.split(" ", 3);
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,commands[2]);
        try {
            if (!file.exists() && file.createNewFile()) {
                Files.write(Paths.get(file.getPath()), fileData);
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

    private static byte[] getFile(String receivedRequest) {
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
        try {
            if (file.exists() && file.isFile()) {
                //here all
                return Files.readAllBytes(file.toPath());
            } else {
            }
        } catch (Exception ignored){
        }
        return null;
    }
    private static int addRecordToMap(File file){
        Map.Entry<Integer, File> lastEntry = ((TreeMap<Integer, File>) treeMap).lastEntry();
        if (lastEntry == null){
            treeMap.put(1, file);
            return 1;
        }
        int id = lastEntry.getKey() + 1;
        treeMap.put(id, file);
        saveTheMap();//saves the map after every file creation
        return id;
    }

    private static File getFileById(int id){
        return treeMap.get(id);
    }

    private static void saveTheMap(){
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