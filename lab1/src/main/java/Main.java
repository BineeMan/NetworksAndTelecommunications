import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main {
    static int port = 9876;
    public static void main(String[] args) throws Exception {
        String groupIpAddress = "";
        InetAddress groupAddress = InetAddress.getByName(groupIpAddress);

        if (!groupAddress.isMulticastAddress()) {
            System.out.println("Multicast address not supported");
            return;
        }
    }

    private NetworkInterface findSuitableInterface(InetAddress groupAddress) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        boolean isIPv6 = groupAddress instanceof Inet6Address;

        while (interfaces.hasMoreElements()) {
            Networ
        }
    }

    private static void run(InetAddress groupAddress) {
        String myID = UUID.randomUUID().toString();
        Map<NodeInfo, Long> liveNodes = new HashMap<>();

    }
}
