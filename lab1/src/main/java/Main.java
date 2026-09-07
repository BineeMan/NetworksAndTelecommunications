import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.*;

public class Main {
    static int port = 9876;

    public static void main(String[] args) throws Exception {
        String groupIpAddress = "";
        InetAddress groupAddress = InetAddress.getByName(groupIpAddress);

        if (!groupAddress.isMulticastAddress()) {
            System.out.println("Multicast address not supported");
            return;
        }
        run(groupAddress);
    }

    private static NetworkInterface findSuitableInterface(InetAddress groupAddress) throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

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

    private static void run(InetAddress groupAddress) throws SocketException {
        String myID = UUID.randomUUID().toString();
        Map<NodeInfo, Long> liveNodes = new HashMap<>();
        NetworkInterface networkInterface = findSuitableInterface(groupAddress);
        if (networkInterface == null) {
            System.out.println("networkInterface == null");
        }
        
    }
}
