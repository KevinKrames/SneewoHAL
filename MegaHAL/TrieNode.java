package MegaHAL;
import java.sql.Time;
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
	 * Channel this node resides in
	 */
	String channel;
    /**
     * The symbol which occurs at this node.
     */
    Symbol symbol;
    
    /**
     * Stem of the symbol word
     */
    String stem;

    /**
     * The number of times this context occurs.
     */
    int usage;
    
    /**
     * the current times this is getting used
     */
    int currentUsage = 0;
    
    /**
     * The time in milliseconds since the last use of this TrieNode
     */
    Long timeSinceLastUse = System.currentTimeMillis();
    
    /**
     * Unique ID for TrieNode
     */
    Long _id;

    /**
     * The total of the children's usages.
     */
    int count;

    /**
     * whether or not this is a node that is in the dirty sneewo's mind
     */
    boolean dirty;
    
    /**
     * The mapping of child symbols to TrieNode objects.
     */
    public Map<String,Long> children;

    /**
     * Constructs a root trie node.
     */
    public TrieNode(DatabaseManager databaseManager, String direction, String channel, boolean dirty) {
        this((Symbol)null, null, channel, dirty);
        this.stem = direction;
        databaseManager.createNode(_id, this);
    }
    public TrieNode(Symbol symbol, String stem, String channel, boolean dirty) {
    	this(System.nanoTime(), symbol, stem, channel, 0, 0, null, dirty);
    }
    public TrieNode(Long id, Symbol symbol, String stem, String channel, int count, int usage, Map<String,Long> children, boolean dirty) {
        this._id = id;
        this.symbol = symbol;
        this.stem = stem;
        this.count = count;
        this.usage = usage;
        this.channel = channel;
        if (this.channel == null) {
        	System.out.println("null channel error");
        }
        this.dirty = dirty;
        if (children == null) {
        	this.children = new HashMap<String,Long>();
        } else {
        	this.children = children;
        }
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
    	TrieNode child = null;
    	if(children.containsKey(symbol.toString())) {
    		child = databaseManager.getChild(this, symbol.toString(), false);
    	}
        if (child == null && createIfNull) {
            child = new TrieNode(symbol, stemmer.stem(symbol.toString()).toString(), channel, dirty);
            
            databaseManager.createNode(child.getID(), child);
            children.put(child.getSymbol().toString(), child.getID());
            //databaseManager.updateDB(this);
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
    	return databaseManager.getChildren(children, this);
        //return children.getList();
    }
    
    public Long getID() {
    	return _id;
    }
    
    public Map<String,Long> getChildren() {
    	return children;
    }
    
}
