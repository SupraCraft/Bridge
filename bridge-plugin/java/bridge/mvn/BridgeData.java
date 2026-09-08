package bridge.mvn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

final class BridgeData {
    boolean adopted;
    final HashMap<String, LinkedList<BridgeAnnotation.Data>> bridges = new HashMap<>();
    final HashMap<String, Integer> members = new HashMap<>();
    String signature;

    @Override
    public String toString() {
        int bridges = 0;
        for (Iterator<LinkedList<BridgeAnnotation.Data>> it = this.bridges.values().iterator(); it.hasNext();) {
            bridges += it.next().size();
        }
        return (
                "adopted = " + adopted + '\n' +
                "bridges = " + bridges + '\n' +
                "members = " + members.size() + '\n' +
                "signature = " + ((signature == null)? "null" : '"' + signature + '"')
        );
    }
}
