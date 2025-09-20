package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class ClientConnection {
    private final DataInputStream input;
    private final DataOutputStream output;

    public ClientConnection(Socket socket) throws IOException {
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new  DataOutputStream(socket.getOutputStream());
    }

    public void sendMessage(String message) throws IOException{
        output.writeUTF(message);
    }
    public String getInput() throws IOException {
        return input.readUTF();
    }

    public void close() throws IOException {
        output.close();
        input.close();
    }
    public static ClientConnection startClient() throws IOException {
        System.out.println("Client started!");
        String address = "127.0.0.1";
        int port = 23456;

        Socket socket = new Socket(InetAddress.getByName(address), port);
        return new ClientConnection(socket);
    }
}