package server.model;

    //"EXIT";
    //"GET" + "BY_ID || BY_NAME" + id / name;
    //"PUT" + "SPECIAL_NAME" + name;
    //"DELETE" + "BY_ID || BY_NAME "+ id / name;

public class Request {
    private final String[] args;
    private final byte[] fileData;

    public Request(String requestDataFromClient, byte[] fileData) {
        this.args = requestDataFromClient.split(" ", 3);
        this.fileData = fileData;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String[] getArgs() {
        return args;
    }
}
