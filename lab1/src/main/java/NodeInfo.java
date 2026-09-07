import java.net.InetAddress;
import java.util.Objects;

public class NodeInfo {
    private final InetAddress ip;
    private final int id;

    private NodeInfo(InetAddress ip, int id) {
        this.ip = ip;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(ip, nodeInfo.ip) && Objects.equals(id, nodeInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, id);
    }
}
