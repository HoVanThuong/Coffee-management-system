package network;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class Client {
    private String host;
    private int port;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client() {
        loadConfig();
    }

    private void loadConfig() {
        Properties prop = new Properties();
        // Thử đọc từ file bên ngoài (ưu tiên)
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            this.host = prop.getProperty("server.ip", "auto");
            this.port = Integer.parseInt(prop.getProperty("server.port", "9999"));
        } catch (IOException ex) {
            // Nếu không có file bên ngoài, dùng chế độ tự động
            this.host = "auto";
            this.port = 9999;
        }

        // Tự động tìm IP nếu host là "auto"
        if (this.host.equalsIgnoreCase("auto")) {
            System.out.println("Dang tim server");
            String discoveredIp = NetworkDiscovery.discoverServerIp();
            if (discoveredIp != null) {
                this.host = discoveredIp;
                System.out.println("Da tim thay server: " + host);
            } else {
                this.host = "localhost";
                System.out.println("Khong tim thay server, su dung mac dinh: localhost");
            }
        } else {
            System.out.println("Su dung ip cau hinh: " + host + ":" + port);
        }
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public synchronized Response sendRequest(Request request) {
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, null, "Loi ket noi server tai " + host);
        }
    }

    public void close() throws IOException {
        if (socket != null)
            socket.close();
    }
}
