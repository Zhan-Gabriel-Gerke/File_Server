package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        final ClientConnection connection = ClientConnection.startClient();
        final File file = new File("C:\\Users\\zange\\IdeaProjects\\File Server\\File Server\\task\\src\\client\\data");
        final Scanner sc = new Scanner(System.in);
        System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
        String actionString = sc.nextLine();
        int action = 0;
        if (actionString.equals("exit")){
            connection.sendMessage("EXIT");
            System.out.println("The request was sent.");
            System.exit(0);
        } else if (actionString.equals("1") || actionString.equals("2") || actionString.equals("3")){
            action = Integer.parseInt(actionString);
        }
        String identifier = "";
        String specialNameForFile = "";
        if (action == 1 || action == 3){
            System.out.printf("Do you want to %s the file by name or by id (1 - name, 2 - id): ", action == 1 ? "get" : "delete");
            int nameOrId = sc.nextInt();
            sc.nextLine();
            if (nameOrId == 1){
                System.out.print("Enter filename: ");
                identifier = sc.nextLine();
            } else if (nameOrId == 2){
                System.out.print("Enter id: ");
                identifier = sc.nextLine();
            }
        } else if (action == 2){
            System.out.print("Enter name of the file: ");
            identifier = sc.nextLine();
            System.out.print("Enter name of the file to be saved on server: ");
            specialNameForFile = sc.nextLine();
        }

        if (action == 1 || action == 3){
            String request = createRequest(action, identifier);
            try{
                connection.sendMessage(request);
                System.out.println("The request was sent.");
            } catch (Exception e){
                System.out.println("An error occurred.");
            }
        }
        else if (action == 2){
            if (specialNameForFile.isEmpty()){
                specialNameForFile = identifier;
            }
            String request = createRequest(action, specialNameForFile);
            try{
                connection.sendMessage(request);
                File finalFile = new File(file, identifier);
                //checker for file existence
                byte[] dataBytes = Files.readAllBytes(finalFile.toPath());
                connection.sendFile(dataBytes);
                System.out.println("The request was sent.");
            } catch (Exception e){
                System.out.println("An error occurred.");
            }
        }
        String respond = connection.getMessage();
        if (action == 1){
            if (respond.equals("200")){
                byte[] byteFile = connection.getFile();
                System.out.print("The file was downloaded! Specify a name for it:");
                String name = sc.nextLine();
                if (name.isEmpty()){
                    name = identifier;
                }
                Files.write(Paths.get(file.getPath(), name), byteFile);
            }
            String result = convertRespond(respond, action);
            System.out.println(result);
            connection.close();
        } else if (action == 2 || action == 3){
            String result = convertRespond(respond, action);
            System.out.println(result);
            connection.close();
        }
    }
    public static String convertRespond(String respond, int action){
        String result = "";
        boolean isSuccess = respond.startsWith("200");
        if (action == 1){
            //GET
            if (isSuccess){
                result = "File saved on the hard drive!";
            } else if (respond.startsWith("404")){
                result = "The response says that the file was not found!";
            }
        } else if (action == 2){
            //PUT
            if (isSuccess){
                int id = Integer.parseInt(respond.substring(respond.indexOf(" Id ") + 4));
                result = "Response says that file is saved! ID = " + id;
            } else {
                result = "The response says that the file was not found!";
            }
        } else if (action == 3){
            //DELETE
            if (isSuccess){
                result = "The response says that the file was successfully deleted!";
            } else {
                result = "The response says that the file was not found!";
            }
        }
        return result;
    }

    public static String createRequest(int action, String identifier) {
        String request;
        if (identifier.matches("\\d+")){
            request = switch (action) {
                case 0 -> "EXIT";
                case 1 -> "GET " + "BY_ID " + identifier;//2
                case 2 -> "PUT" + " SPECIAL_NAME " + identifier;
                case 3 -> "DELETE " + "BY_ID "+ identifier;//2
                default -> "ERROR";
            };
        }else{
            request = switch (action) {
                case 0 -> "EXIT";
                case 1 -> "GET " + "BY_NAME " + identifier;//2
                case 2 -> "PUT" + " SPECIAL_NAME " + identifier;
                case 3 -> "DELETE " + "BY_NAME "+ identifier;//2
                default -> "ERROR";
            };
        }
        return request;
    }
}