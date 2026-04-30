package network;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 9999;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response sendRequest(Request request) {
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, null, "Lỗi kết nối Server");
        }
    }
    
    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}
