package server.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection{
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

    public void sendFile(byte[] fileData) throws IOException {
        output.writeInt(fileData.length);
        output.write(fileData);
    }

    public byte[] getFile() throws IOException {
        int length = input.readInt();
        byte[] fileData = new byte[length];
        input.readFully(fileData, 0, fileData.length);
        return fileData;
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