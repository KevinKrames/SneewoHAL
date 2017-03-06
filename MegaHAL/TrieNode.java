package MegaHAL;
import java.sql.Time;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Bot.Stemmer;

/**
 * A node of information about an important word used in creating sentences
 *
 * @author Moltov
 * @see TrieNodeMap
 */
public class TrieNode {
	
	/**
	 * Channel this node resides in
	 */
	protected String channel;
    /**
     * The symbol which occurs at this node.
     */
	protected Symbol symbol;
    
    /**
     * Path to the current symbol
     */
    protected String path;
    
    /**
     * Stem of the symbol word
     */
    protected String stem;

    /**
     * The number of times this context occurs.
     */
    protected int usage;
    
    /**
     * the current times this is getting used
     */
    protected int currentUsage = 0;
    
    /**
     * The time in milliseconds since the last use of this TrieNode
     */
    protected Long timeSinceLastUse = System.currentTimeMillis();
    
    /**
     * Unique ID for TrieNode
     */
    protected Long _id;

    /**
     * The total of the children's usages.
     */
    protected int count;

    /**
     * whether or not this is a node that is in the dirty sneewo's mind
     */
    protected boolean dirty;
    
    /**
     * The mapping of child symbols to TrieNode objects.
     */
    protected TrieNodeMap children;

    /**
     * Constructs a root trie node.
     */
    //public TrieNode(DatabaseManager databaseManager, String direction, String channel, boolean dirty) {
        //this((Symbol)null, null, channel, dirty);
        //this.stem = direction;
        //databaseManager.createNode(_id, this);
    //}
    public TrieNode(Symbol symbol, Symbol path, String stem, String channel, boolean dirty) {
    	this(System.nanoTime(), symbol, stem, channel, 0, 0, dirty);
    }
    
    /**
     * Global instance creation of a trienode, includes all features
     */
    public TrieNode(Long id, Symbol symbol, String stem, String channel, int count, int usage, boolean dirty) {
        this._id = id;
        this.symbol = symbol;
        this.stem = stem;
        this.count = count;
        this.usage = usage;
        this.channel = channel;
        if (this.channel == null) {
        	//System.out.println("null channel error");
        }
        this.dirty = dirty;
        if (children == null && symbol != Symbol.NULL) {
        	this.children = new TrieNodeMap();
        } 
    }

    /**
     * Gets the child trie for the given symbol.  Optionally, grows the tree if this child node did
     * not already exist.
     *
     * @param symbol the symbol we are searching for.
     * @param createIfNull if true, grows the tree if the child node did not exist.
     * @param stemmer Pass in the reference to the stemmer.
     * @param DatabaseManager Pass in a reference to the database manager
     * @return the trie node for this symbol, newly created if necessary.
     */
    public TrieNode getChild(Symbol symbol, Symbol path, boolean createIfNull) {
    	TrieNode child = children.get(symbol);
    	//Create the child if it does not exist, record it inside of this map
        if (child == null && createIfNull) {
            child = new TrieNode(symbol, path, new String(Stemmer.stem(symbol.toString())), channel, dirty);
            children.put(child, path);
        } else if (createIfNull) {
        	children.putPath(child, path);
        }
        return child;
    }

    private Symbol getSymbol() {
		return symbol;
	}

	@SuppressWarnings("unchecked")
	public List getChildList() {
    	return children.getMap();
    }
    
    public Long getID() {
    	return _id;
    }
    
    public TrieNodeMap getChildren() {
    	return children;
    }
    
    public Symbol getRandomPath(TrieNode node, Random rng) {
    	return children.getRandomPath(node, rng);
    }
    
    public String toString() {
    	return "Symbol: " + this.symbol.toString();
    }
}
