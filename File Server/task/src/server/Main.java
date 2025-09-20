package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
public class Main {

    // Основной серверный сокет
    private static ServerSocket server;

    public static void main(String[] args) throws IOException {
        System.out.println("Server started!");
        String address = "127.0.0.1"; // Адрес сервера (локальный)
        int port = 23456; // Порт сервера

        // Создаём серверный сокет
        server = new ServerSocket(port, 50, InetAddress.getByName(address));
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
        // Создаём объект базы данных (работа с JSON-хранилищем)

        // Пока соединение открыто — читаем запросы
        while (!connection.isClosed()) {
            String receivedJson;
            try {
                receivedJson = connection.getInput(); // читаем строку JSON от клиента
                System.out.println("Received: " + receivedJson);
            } catch (IOException e) {
                connection.close(); // если ошибка — закрываем соединение
                break;
            }


            // Отправляем клиенту ответ
            connection.sendMessage("All files were sent!");
            System.out.println("Sent: All files were sent!");
            // Закрываем соединение после обработки
            connection.close();
        }
    }
        /*Scanner sc = new Scanner(System.in);
        label:
        while(true){
            String command = sc.nextLine();
            String[] commands = command.split(" ");
            switch (commands[0]) {
                case "exit":
                    deleteFolder();
                    break label;
                case "add":
                    //add
                    createFile(commands[1]);
                    break;
                case "delete":
                    //delete
                    deleteFile(commands[1]);
                    break;
                case "get":
                    //get
                    getFile(commands[1]);
                    break;
            }
        }*/





    /*public static boolean checkTheName(String name){
        for (int i = 1; i < 11; i++){
            if (name.equals("file" + i)){
                return true;
            }
        }
        return false;
    }

    public static void createFile(String name) {
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,name);
        try {
            if (checkTheName(name)){
                if (!file.exists() && file.createNewFile()) {
                    System.out.println("The file " + file.getName() + " added successfully");
                } else {
                    System.out.println("Cannot add the file " + file.getName());
                }
            } else {
                System.out.println("Cannot add the file " + file.getName());
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
        }
    }

    public static void deleteFile(String name) {
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,name);
        try {
            if (file.delete()){
                System.out.println("The file " + file.getName() + " was deleted");
            } else {
                System.out.println("The file " + file.getName() + " not found");
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
        }
    }

    public static void getFile(String name) {
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");
        File file = new File(path,name);
        try {
            if (file.exists() && file.isFile()) {
                System.out.println("The file " + file.getName() + " was sent");
            } else {
                System.out.println("The file " + file.getName() + " not found");
            }
        } catch (Exception e){
            System.out.println("An error occurred.");
        }
    }

    public static void deleteFolder(){
        File path = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data");

        String[] fileNames = path.list();
        if (fileNames != null){
            for (String fileName : fileNames) {
                File file = new File(path,fileName);
                file.delete();
            }
        }
    }
}*/
}