package MegaHAL;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class DatabaseManager {
	private Map<Long,TrieNode> nodeMemory = new HashMap<Long,TrieNode>();
	private static Instrumentation instrumentation;
	private MongoDatabase db;
	private MongoClient mongoClient;
	
	public DatabaseManager () {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase("Sneewo");
		MongoIterable iter = db.listCollectionNames();
		
		MongoCursor curs = iter.iterator();
		ObjectMapper mapper = new ObjectMapper();
		
		/*while (curs.hasNext()) {
			db.getCollection(curs.next().toString()).drop();
		}*/
	}
	/**
	 * Searchs the database manager for a child node, takes in boolean value to create if it exists
	 * @param parentID ID of the parents node whom we are searching for
	 * @param childName string name of the child node
	 * @param createIfDoesntExist set to true if you want to create the node if it doesnt exist
	 */
	public TrieNode getChild(TrieNode parent, String childName, boolean createIfDoesntExist) {
		//Error:
		if (parent._id <= 0 || childName == null)
			return null;
		
		return checkForNode(childName, parent);
	}
	
	@SuppressWarnings("unchecked")
	public List getChildren(Map<String,Long> children, TrieNode parent) {
		List childrenList = new ArrayList<TrieNode>();
		for (Long childID : children.values()) {
		    childrenList.add(getNodeFromDB(childID, parent));
		}
		return childrenList;
	}
	
	public void createNode(Long id, TrieNode node) {
		//map.put(id, node);
		insertToDB(node);
		//System.out.print(map.size()+",");
	}
	public TrieNode getRootNode(String direction, String channel, boolean dirty) {
		TrieNode root = null;
		Document search = new Document("symbol", null).append("stem", direction);
		FindIterable<Document> docs = db.getCollection(channel.replace('#', dirty?'D':'X')).find(search);
		if (docs.first() == null) {
			//System.out.println("DB ERROR");
			return null;
		}
		Document current = docs.first();
		Map<String, Long> map = new HashMap<String,Long>();
		Document childDoc = (Document)current.get("children");
		
		for (Map.Entry<String, Object> entry : childDoc.entrySet()) {
			if (entry.getValue() instanceof Long) {
				map.put(entry.getKey(), (Long)entry.getValue());
			}
		}
		
		root = new TrieNode(current.getLong("_id"), 
				null,
				current.getString("stem"),
				current.getString("channel"),
				current.getInteger("count", 0),
				current.getInteger("usage", 0),
				map,
				current.getBoolean("dirty", false)
				);
		return root;
	}
	
	public TrieNode checkForNode(String childName, TrieNode parent) {
		if (nodeMemory.containsKey(childName)) {
			return nodeMemory.get(childName);
		} else {
			TrieNode child = getNodeFromDB(parent.children.get(childName), parent);
			//nodeMemory.put(child._id, child);
			//System.out.println("miss");
			return child;
		}
	}
	@SuppressWarnings("unchecked")
	public TrieNode getNodeFromDB(Long id, TrieNode parent) {
		Document search = new Document("_id", id);
		FindIterable<Document> docs = db.getCollection(parent.channel.replace('#', parent.dirty?'D':'X')).find(search);
		if (docs.first() == null) {
			//System.out.println("DB ERROR");
			return null;
		}
		Document current = docs.first();
		TrieNode child;
		Map<String, Long> map = new HashMap<String,Long>();
		Document childDoc = (Document)current.get("children");
		
		for (Map.Entry<String, Object> entry : childDoc.entrySet()) {
			if (entry.getValue() instanceof Long) {
				map.put(entry.getKey(), (Long)entry.getValue());
			}
		}
		
		child = new TrieNode(current.getLong("_id"), 
				new Symbol(current.getString("symbol"), false),
				current.getString("stem"),
				current.getString("channel"),
				current.getInteger("count", 0),
				current.getInteger("usage", 0),
				map,
				current.getBoolean("dirty", false)
				);
		return child;
		
	}
	
	public void insertToDB(TrieNode node) {
		Document doc = new Document();
		ObjectMapper mapper = new ObjectMapper();
		doc.append("_id", node._id);
		doc.append("stem", node.stem);
		if (node.symbol != null) {
			doc.append("symbol", node.symbol.toString());
		} else {
			doc.append("symbol", null);
		}
		doc.append("count", node.count);
		doc.append("dirty", node.dirty);
		doc.append("channel", node.channel);
		doc.append("usage", node.usage);
		doc.append("children", new Document());
		db.getCollection(node.channel.replace('#', node.dirty?'D':'X')).insertOne(doc);
	}
	
	public void updateDB(TrieNode node, TrieNode child) {

		try {
			Document update = new Document("_id", node._id);
			Document update2 = new Document("$set", new Document("count", node.count).append("usage", node.usage).append("children." + encodeMongoSafeField(child.symbol.toString()), child._id)
			);
			db.getCollection(node.channel.replace('#', node.dirty?'D':'X')).updateOne(update, update2
					);
			update = new Document("_id", child._id);
			update2 = new Document("$set", new Document("count", child.count).append("usage", child.usage)
			);
			db.getCollection(child.channel.replace('#', child.dirty?'D':'X')).updateOne(update, update2
		);
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}
	
	public String encodeMongoSafeField(String s) {
		s = s.replace("\\", "\\u0020");
		s = s.replace("$", "\\u0024");
		s = s.replace(".", "\\u002e");
		return s;
	}
	public String decodeMongoSafeField(String s) {
		s = s.replace("\\u002e", ".");
		s = s.replace("\\u0024", "$");
		s = s.replace("\\u0020", "\\");
		return s;
	}
	
}
