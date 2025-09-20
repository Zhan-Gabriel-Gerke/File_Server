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
        System.out.print("Enter filename: ");
        String name = sc.next();
        String content = "";
        if (action == 2){
            System.out.print("Enter file content: ");
            content = sc.next();
        }
        String request = createRequest(action, name, content);
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
                int index = respond.indexOf("FILE_CONTENT") + 10;
                result = "The content of the file is: " + respond.substring(index);
            } else {
                result = "The response says that the file was not found!";
            }
        } else if (action == 2){
            //PUT
            if (isSuccess){
                result = "File was created successfully.";
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

    public static String createRequest(int action, String name, String content) {
        String request = switch (action) {
            case 0 -> "EXIT";
            case 1 -> "GET " + name;
            case 2 -> "PUT " + name + " FILE_CONTENT " + content;
            case 3 -> "DELETE " + name;
            default -> "Error";
        };
        return request;
    }
}
