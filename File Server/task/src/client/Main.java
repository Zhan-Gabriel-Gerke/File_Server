package client;


import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        final ClientConnection connection = ClientConnection.startClient();
        connection.sendMessage("Give me everything you have!");
        System.out.println("Sent: Give me everything you have!");
        System.out.println("Received: " + connection.getInput());
        connection.close();
    }
}
