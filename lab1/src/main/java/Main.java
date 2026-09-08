import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: <group ip>");
            return;
        }
        String groupIpAddress = args[0];
        InetAddress groupAddress = InetAddress.getByName(groupIpAddress);

        if (!groupAddress.isMulticastAddress()) {
            System.out.println("Multicast address not supported");
            return;
        }

        NetworkSelfChecker networkSelfChecker = new NetworkSelfChecker(groupAddress, 8888);
        networkSelfChecker.run();
    }

}
