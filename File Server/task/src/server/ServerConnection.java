package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class ServerConnection{
    private final DataInputStream input;
    private final DataOutputStream output;
    private boolean isClosed = false;
    private static boolean running = true;

    public ServerConnection(Socket socket) throws IOException {
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    public static ServerConnection startServer(Socket clientSocket) throws IOException {
        return new ServerConnection(clientSocket);
    }

    public String getInput() throws IOException {
        return input.readUTF();
    }

    public void sendMessage(String message) throws IOException {
        output.writeUTF(message);
    }

    public static void stopServer() {
        running = false;
    }

    public static boolean isServerRunning() {
        return running;
    }

    public void close() throws IOException {
        output.close();
        input.close();
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }
}