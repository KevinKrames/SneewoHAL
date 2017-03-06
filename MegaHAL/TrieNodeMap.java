package MegaHAL;
import java.util.*;

/**
 * Wrapper class for mapping String node names to TrieNode node objects.
 *
 * This was created to do away with the inefficiency of
 * (a) searching for nodes in a List, and
 * (b) iterating through nodes in a Map.
 *
 * In addition, it should give us a little more control over what types are used
 * in the map.
 *
 * @author Trejkaz
 * @see TrieNode
 */
public class TrieNodeMap {
    // The two underlying data structures which we'll be using for this data type.
    private Map<String,TrieNode> map = new HashMap<String,TrieNode>();
    private Map<TrieNode, TriePaths> pMap = new HashMap<TrieNode, TriePaths>();
    private TrieNode nullNode = null;

    public TrieNodeMap() {
    	nullNode = new TrieNode(Symbol.NULL, Symbol.NULL, "", "", false);
    }

    public synchronized TrieNode get(Symbol symbol) {
        return map.get(symbol.toString());
    }

    /**
     * Puts a node inside of the map, and list of TrieNodes
     * @param node the node to put inside the map & list
     */
    public synchronized void put(TrieNode node, Symbol path) {
    	if (node == null) {
    		node = nullNode;
    	}
        map.put(node.symbol.toString(), node);
        
        putPath(node, path);
    }
    
    public void putPath(TrieNode node, Symbol path) {
    	if (node == null) {
    		node = nullNode;
    	}
    	if (pMap.containsKey(node)) {
			//Theres already a key, increase the values:
			TriePaths paths = pMap.get(node);
			paths.increasePath(path);
		} else {
			//No key place it in
			TriePaths paths = new TriePaths();
			paths.increasePath(path);
			pMap.put(node, paths);
		}
    }

    /*public synchronized List getList() {
        return Collections.unmodifiableList(list);
    }*/
    public Symbol getRandomPath(TrieNode node, Random rng) {
    	if (node == null) {
    		node = nullNode;
    	}
    	return pMap.get(node).getRandomPath(rng);
    }

    public synchronized List getMap() {
        return new ArrayList<TrieNode>(map.values());
    }
}