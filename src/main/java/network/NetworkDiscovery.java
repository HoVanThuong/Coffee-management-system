package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkDiscovery {
    private static final int DISCOVERY_PORT = 8888;
    private static final String DISCOVERY_REQUEST = "COFFEE_SERVER_DISCOVER_REQUEST";
    private static final String DISCOVERY_RESPONSE = "COFFEE_SERVER_DISCOVER_RESPONSE";

    /**
     * Phía Server: Chạy luồng này để phản hồi các yêu cầu tìm kiếm từ Client
     */
    public static void startServerDiscovery() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
                socket.setBroadcast(true);
                System.out.println("Discovery Service started on port " + DISCOVERY_PORT + "...");

                while (true) {
                    byte[] recvBuf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);

                    String message = new String(packet.getData()).trim();
                    if (message.equals(DISCOVERY_REQUEST)) {
                        byte[] sendData = DISCOVERY_RESPONSE.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                        System.out.println("Sent discovery response to: " + packet.getAddress().getHostAddress());
                    }
                }
            } catch (Exception e) {
                System.err.println("Discovery Service Error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Phía Client: Gửi broadcast để tìm IP của Server
     * @return IP của Server nếu tìm thấy, null nếu không
     */
    public static String discoverServerIp() {
        try (DatagramSocket c = new DatagramSocket()) {
            c.setBroadcast(true);
            c.setSoTimeout(3000); // Đợi tối đa 3 giây

            byte[] sendData = DISCOVERY_REQUEST.getBytes();

            // Thử gửi đến địa chỉ broadcast chung
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
                c.send(sendPacket);
            } catch (Exception e) {}

            // Thử gửi đến tất cả các interface mạng (chắc chắn hơn trên một số hệ điều hành)
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) continue;

                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, DISCOVERY_PORT);
                    c.send(sendPacket);
                }
            }

            // Đợi phản hồi
            byte[] recvBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            c.receive(receivePacket);

            String message = new String(receivePacket.getData()).trim();
            if (message.equals(DISCOVERY_RESPONSE)) {
                return receivePacket.getAddress().getHostAddress();
            }
        } catch (Exception e) {
            System.out.println("No server found via discovery: " + e.getMessage());
        }
        return null;
    }
}
