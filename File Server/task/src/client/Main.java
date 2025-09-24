package client;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        final ClientConnection connection = ClientConnection.startClient();
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
        String nameKey = "";
        String content = "";
        String specialForlder = "";
        if (action == 1 || action == 3){
            System.out.printf("Do you want to %s the file by name or by id (1 - name, 2 - id): ", action == 1 ? "get" : "delete");
            int nameOrId = sc.nextInt();
            sc.nextLine();
            if (nameOrId == 1){
                System.out.print("Enter filename: ");
                nameKey = sc.nextLine();
            } else if (nameOrId == 2){
                System.out.print("Enter id: ");
                nameKey = sc.nextLine();
            }
        } else if (action == 2){
            System.out.print("Enter name of the file: ");
            nameKey = sc.nextLine();
            System.out.print("Enter file content: ");
            content = sc.nextLine();
            System.out.print("Enter name of the file to be saved on server: ");
            specialForlder = sc.nextLine();
        }
        String request = createRequest(action, nameKey, content, specialForlder);
        try{
            connection.sendMessage(request);
            System.out.println("The request was sent.");
        } catch (Exception e){
            System.out.println("An error occurred.");
        }
        String respond = connection.getInput();
        String result = convertRespond(respond, action);
        System.out.println(result);
        connection.close();
    }
    public static String convertRespond(String respond, int action){
        String result = "";
        boolean isSuccess = respond.startsWith("200");

        if (action == 1){
            //GET
            if (isSuccess){
                int index = respond.indexOf("FILE_CONTENT") + 13;
                result = "The content of the file is: " + respond.substring(index);
            } else {
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

    public static String createRequest(int action, String name, String content, String specialForlder) {
        String request;
        if (name.matches("\\d+")){
            request = switch (action) {
                case 0 -> "EXIT";
                case 1 -> "GET " + "BY_ID " + name;//2
                case 2 -> "PUT " + name  + " PATH " + specialForlder + " FILE_CONTENT " + content;//5
                case 3 -> "DELETE " + "BY_ID "+ name;//2
                default -> "ERROR";
            };

        }else{
            request = switch (action) {
                case 0 -> "EXIT";
                case 1 -> "GET " + "BY_NAME " + name;//2
                case 2 -> "PUT " + name + " PATH " + specialForlder + " FILE_CONTENT " + content;//5
                case 3 -> "DELETE " + "BY_NAME "+ name;//2
                default -> "ERROR";
            };
        }
        return request;
    }
}