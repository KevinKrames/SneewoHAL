package MegaHAL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Bot.Stemmer;

/**
 * Represents a node in the language model.  Contains the symbol at this position, a count of the number of
 * times this context has occurred (usage), a list/map of children, and a total count of the usages of all its children
 * (count).
 *
 * @author Trejkaz
 * @see TrieNodeMap
 */
public class TrieNode {
    /**
     * The symbol which occurs at this node.
     */
    Symbol symbol;
    
    /*
     * Stem of the symbol word
     */
    String stem;

    /**
     * The number of times this context occurs.
     */
    int usage;
    
    /**
     * Unique ID for TrieNode
     */
    Long id;

    /**
     * The total of the children's usages.
     */
    int count;

    /**
     * The mapping of child symbols to TrieNode objects.
     */
    public Map<String,Long> children = new HashMap<String,Long>();

    /**
     * Constructs a root trie node.
     */
    public TrieNode(DatabaseManager databaseManager) {
        this(null, null);
        databaseManager.createNode(id, this);
    }

    public TrieNode(Symbol symbol, String stem) {
        this.symbol = symbol;
        this.stem = stem;
        this.count = 0;
        this.usage = 0;
        this.id = System.nanoTime();
    }

    /**
     * Gets the child trie for the given symbol.  Optionally, grows the tree if this child node did
     * not already exist.
     *
     * @param symbol the symbol we are searching for.
     * @param createIfNull if true, grows the tree if the child node did not exist.
     * @return the trie node for this symbol, newly created if necessary.
     */
    public TrieNode getChild(Symbol symbol, boolean createIfNull, Stemmer stemmer, DatabaseManager databaseManager) {
        TrieNode child = databaseManager.getChild(id, symbol.toString(), false);
        if (child == null && createIfNull) {
            child = new TrieNode(symbol, stemmer.stem(symbol.toString()).toString());
            databaseManager.createNode(child.getID(), child);
            children.put(child.getSymbol().toString(), child.getID());
            if (this.symbol != null) {
            	//System.out.println("Symbol: " + this.symbol.toString() + ", add Child:" + symbol.toString() + " " + children.values().size());
            }
        }
        return child;
    }

    private Symbol getSymbol() {
		return symbol;
	}

	public List getChildList(DatabaseManager databaseManager) {
    	return databaseManager.getChildren(children);
        //return children.getList();
    }
    
    public Long getID() {
    	return id;
    }
    
    public Map<String,Long> getChildren() {
    	return children;
    }
    
}
