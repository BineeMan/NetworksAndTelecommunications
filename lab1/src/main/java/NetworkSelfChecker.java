import java.io.IOException;
import java.net.*;
import java.util.*;

public class NetworkSelfChecker {
    private int port = 8888;

    private final long sendInterval = 500;

    private final long timeoutThreshold = 2000;

    private final int socketTimeoutMs = 500;

    private InetAddress groupAddress;

    NetworkSelfChecker(InetAddress groupAddress, int port) {
        this.groupAddress = groupAddress;
        this.port = port;
    }

    private NetworkInterface findSuitableInterface(InetAddress groupAddress) throws SocketException {
        List<NetworkInterface> interfaces
                = Collections.list(NetworkInterface.getNetworkInterfaces());

        boolean isIPv6 = groupAddress instanceof Inet6Address;

        for (NetworkInterface networkInterface : interfaces) {
            if (!networkInterface.isUp() || networkInterface.isLoopback() || !networkInterface.supportsMulticast()) {
                continue;
            }

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (isIPv6 && address instanceof Inet6Address) {
                    return networkInterface;
                }

                if (!isIPv6 && address instanceof Inet4Address) {
                    return networkInterface;
                }
            }
        }

        return null;
    }

    private boolean cleanOldNodes(Map<NodeInfo, Long> nodes, long threshold) {
        long now = System.currentTimeMillis();
        int initialSize = nodes.size();
        nodes.entrySet().removeIf(entry ->
                (now - entry.getValue()) > threshold
        );

        return initialSize != nodes.size();
    }

    private void printLiveNodes(Map<NodeInfo, Long> nodes) {
        System.out.println("=== Alive copies (" + nodes.size() + ") ===");
        if (nodes.isEmpty()) {
            System.out.println("  [Not found anyone]");
        } else {
            for (NodeInfo node : nodes.keySet()) {
                System.out.println("  - IP: " + node.ip().getHostAddress() + " | ID: " + node.id());
            }
        }
        System.out.println("============================================\n");
    }

    public void run() throws IOException {
        NetworkInterface networkInterface = findSuitableInterface(groupAddress);
        if (networkInterface == null) {
            System.out.println("networkInterface == null");
            return;
        }

        MulticastSocket socket = new MulticastSocket(port);
        socket.setReuseAddress(true);
        socket.setSoTimeout(socketTimeoutMs);
        InetSocketAddress groupSocketAddress = new InetSocketAddress(groupAddress, port);
        socket.joinGroup(groupSocketAddress, networkInterface);

        String myId = UUID.randomUUID().toString();
        Map<NodeInfo, Long> liveNodes = new HashMap<>();

        long lastTimeSend = 0;
        byte[] buffer = new byte[1024];

        System.out.println("Start listening...\n");
        while (true) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimeSend > sendInterval) {
                DatagramPacket packet = new DatagramPacket(
                        myId.getBytes(), myId.length(), groupAddress, port
                );

                socket.send(packet);
                lastTimeSend = currentTime;
            }

            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivePacket);
                String receivedUID = new String(
                        receivePacket.getData(), 0, receivePacket.getLength()
                );

                NodeInfo newNode = new NodeInfo(receivePacket.getAddress(), receivedUID);

                if (!receivedUID.equals(myId)) {
                    boolean isNewNode = !liveNodes.containsKey(newNode);
                    liveNodes.put(
                            new NodeInfo(
                                    receivePacket.getAddress(),
                                    receivedUID),
                            System.currentTimeMillis()
                    );
                    if (isNewNode) {
                        printLiveNodes(liveNodes);
                    }
                }

            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean isListChanged = cleanOldNodes(liveNodes, timeoutThreshold);
            if (isListChanged) {
                printLiveNodes(liveNodes);
            }
        }
    }
}
