package MegaHAL;

import java.util.ArrayList;
import java.util.List;

public class TrieNodePath {

	/**
	 * The destination node of this path
	 */
	private TrieNode destination;
	/**
	 * The pathway to the destination node, in terms of the string inbetween:
	 * aka the un relevant words.
	 */
	private List<String> pathways;
	
	/**
	 * Constructor
	 * @param destination node that this pathway ends at: if null then it is ending
	 */
	public TrieNodePath(TrieNode destination) {
		this.destination = destination;
		pathways = new ArrayList<String>();
	}
	/**
	 * Finds a random pathway to the destination node
	 * @return string of pathway
	 */
	public String getRandomPathway() {
		String returnString = "";
		
		return returnString;
	}
}
