package MegaHAL;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
	private Map<Long,TrieNode> map = new HashMap<Long,TrieNode>();
	private static Instrumentation instrumentation;
	public DatabaseManager () {
		
	}
	/**
	 * Searchs the database manager for a child node, takes in boolean value to create if it exists
	 * @param parentID ID of the parents node whom we are searching for
	 * @param childName string name of the child node
	 * @param createIfDoesntExist set to true if you want to create the node if it doesnt exist
	 */
	public TrieNode getChild(Long parentID, String childName, boolean createIfDoesntExist) {
		//Error:
		if (parentID <= 0 || childName == null)
			return null;
		
		if (map.containsKey(parentID)) {
			TrieNode parent = map.get(parentID);
			Map<String,Long> childMap = parent.getChildren();
			if (childMap.containsKey(childName)) {
				return map.get(childMap.get(childName));
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List getChildren(Map<String,Long> children) {
		List childrenList = new ArrayList<TrieNode>();
		for (Long childID : children.values()) {
		    childrenList.add(map.get(childID));
		}
		return childrenList;
	}
	
	public void createNode(Long id, TrieNode node) {
		map.put(id, node);
		//System.out.print(map.size()+",");
	}
}
