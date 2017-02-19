package MegaHAL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import Bot.Stemmer;

/**
 * Class implementing the language model used by MegaHAL to record text it sees.
 *
 * @author Trejkaz
 */
public class Model {

	/**
	 * The order of this model.
	 */
	private int order;

	/**
	 * The forward trie.
	 */
	private TrieNode forwardTrie;
	private MegaHAL mega;
	private Stemmer stemmer;
	public DatabaseManager databaseManager;

	/**
	 * The backward trie.
	 */
	private TrieNode backwardTrie;
	private Set badWords;
	private Set spellIgnores;
	/**
	 * Create a new model with the default order of 4.
	 */
	//Spell checker

	

	public Model() {
		this(4);
	}


	public Model(Set<String> badWords, Set<String> spellIgnores, MegaHAL mega, Stemmer stemmer, DatabaseManager databaseManager) {
		this(4);
		this.badWords = badWords;
		this.spellIgnores = spellIgnores;
		this.mega = mega;
		this.stemmer = stemmer;
		this.databaseManager = databaseManager;
		this.forwardTrie = databaseManager.getRootNode("f", mega.channel, mega.dirty);
		if (this.forwardTrie == null) {
			this.forwardTrie = new TrieNode(databaseManager, "f", mega.channel, mega.dirty);
		}
		this.backwardTrie = databaseManager.getRootNode("b", mega.channel, mega.dirty);
		if (this.backwardTrie == null) {
			this.backwardTrie = new TrieNode(databaseManager, "b", mega.channel, mega.dirty);
		}
	}


	/**
	 * Create a new model with the given order.
	 * The order is the maximum number of symbols which can occur in a given context.
	 *
	 * @param order the desired order.
	 */
	public Model(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	/**
	 * Finds the context associated with the end of the given list of symbols.
	 * I'm not sure if I've named this appropriately...
	 *
	 * @param trie the trie to use.
	 * @param symbols the list of symbols.
	 * @return the trie node representing the last symbol in the list, in the context of the symbols before it.
	 */
	private TrieNode findLongestContext(TrieNode trie, List<Symbol> symbols) {
		
		int start = symbols.size() - order;
		if (start < 0) {
			start = 0;
		}
		TrieNode node = trie;
		TrieNode nextNode;
		for (int i = start; i < symbols.size(); i++) {
			
			nextNode = node.getChild(symbols.get(i), false, stemmer, databaseManager);
			if (nextNode == null) {
				//System.out.print("1");
				break;
			}
			node = nextNode;
		}
		return node;
	}

	// ---------------- METHODS FOR TRAINING THE BRAIN ----------------

	/**
	 * Train the model with a list of symbols.
	 *
	 * @param symbols the list of symbols.
	 */
	public void train(List<Symbol> symbols, String userText, String channel) {
		// If there aren't enough symbols, then don't bother training from the list.
		if (symbols.size() < order + 1) {
			return;
		}

		
		
		
		// Train in the forward direction.
		train(forwardTrie, symbols);

		// Train in the backward direction.
		Collections.reverse(symbols);
		train(backwardTrie, symbols);
		Collections.reverse(symbols);
	}
	

	/**
	 * For convenience, to avoid duplicating code.  This is called from the public train(List)
	 * method, once for each of the forward and backward tries.
	 *
	 * @param trie the trie to train.
	 * @param symbols the list of symbols.
	 */
	private void train(TrieNode trie, List<Symbol> symbols) {

		// Iterate from the start to the end of the list.
		for (int i = 0; i < symbols.size(); i++) {
			TrieNode node = trie;

			// Iterate over the five symbols occurring at the current position.
			for (int j = i; j < i + order + 1 && j < symbols.size(); j++) {
				Symbol symbol = symbols.get(j);
				TrieNode child = node.getChild(symbol, true, stemmer, databaseManager);
				child.usage++;
				node.count++;

	            databaseManager.updateDB(node, child);
				node = child;
			}
		}
	}

	// ---------------- METHODS TO GENERATE RESPONSES ----------------
	/**
	 * Generates a list of random symbols, forming a random response to the given lists of keywords.
	 *
	 * @param rng a random number generator.
	 * @param userKeywords the list of keywords from the user's input.
	 * @return the list of symbols generated.
	 */
	public List<Symbol> generateRandomSymbols(Random rng, List<Symbol> userKeywords) {
		// This is the list which will be returned.
		ArrayList<Symbol> symbols = new ArrayList<Symbol>();

		// Generate in the forward direction.
		generateRandomSymbols(forwardTrie, symbols, rng, userKeywords, Symbol.END);
		if (symbols.size() < 1) {
			return null;
		}

		if (!symbols.get(symbols.size()-1).equals(Symbol.END)) {
			return null;
		}

		// Reverse the list, and then generate in the backward direction.
		Collections.reverse(symbols);
		generateRandomSymbols(backwardTrie, symbols, rng, userKeywords, Symbol.START);
		if (symbols.size() < 1) {
			return null;
		}
		if (!symbols.get(symbols.size()-1).equals(Symbol.START)) {
			return null;
		}

		// Reverse the list back again, and return it.
		Collections.reverse(symbols);
		return symbols;
	}

	/**
	 * Generates random symbols until a list terminator is found.
	 *
	 * @param trie the trie to use to generate the symbol.
	 * @param symbols the current list of symbols.
	 * @param rng a random number generator.
	 * @param userKeywords the list of keywords from the user's input.
	 * @param stopSymbol the magic symbol which signals to stop adding to the list.
	 */
	protected void generateRandomSymbols(TrieNode trie, List<Symbol> symbols, Random rng, List<Symbol> userKeywords, Symbol stopSymbol) {
		//System.out.print("(generateRandomSymbols)");
		Symbol symbol;
		do {
			symbol = generateRandomSymbol(trie, symbols, rng, userKeywords);
			if (symbol == null)
				break;
			symbols.add(symbol);
			//System.out.print("{"+symbol+"}");
		} while (!symbol.equals(stopSymbol));
	}

	/**
	 * Generates a random symbol for the next symbol in the list.
	 *
	 * @param trie the trie to use to generate the symbol.
	 * @param symbols the current list of symbols.
	 * @param rng a random number generator.
	 * @param userKeywords the list of keywords from the user's input.
	 * @return the randomly-determined next symbol in the list.
	 */
	protected Symbol generateRandomSymbol(TrieNode trie, List<Symbol> symbols, Random rng, List<Symbol> userKeywords) {
		//System.out.println("symbols=" + symbols);
		// Trivial case: If the list of generated symbols is empty, use a random keyword, if one exists.
		if (symbols.isEmpty() && !userKeywords.isEmpty()) {
			return userKeywords.get(rng.nextInt(userKeywords.size()));
		}

		// Find the longest context available in the list of symbols.
		TrieNode node = findLongestContext(trie, symbols);

		if (node == null) {
			return null;
		}
		
		if (node.count <= 0) {
			return null;
		}
		
		// Pick a random number, which will be used as a count-down.
		int total = rng.nextInt(node.count); // remember, our 'count' is the total of all children's 'usages'

		// Pick a random number, which will be used as an initial index into the list of children.
		List childNodes = node.getChildList(databaseManager);
		int index = rng.nextInt(childNodes.size());

		TrieNode subnode;
		Symbol subnodeSymbol;

		do {
			subnode = (TrieNode) childNodes.get(index);
			subnodeSymbol = subnode.symbol;

			// If the child is a keyword the user used, use it immediately.
			if (userKeywords.contains(subnodeSymbol)) {
				return subnodeSymbol;
			}

			// Otherwise, subtract the count of the child off the total, and look at the
			// next word in the list.  We'll actually loop around backwards because it's faster
			// to compare with 0 than to compare with the size of the list every iteration around
			// this loop.
			//System.out.print("{subnode.usage=" + subnode.usage + "}");
			total -= subnode.usage;
			index--;
			if (index < 0) {
				index = childNodes.size() - 1;
			}
		} while (total >= 0);

		// Once total hits zero, return the current child.
		return subnodeSymbol;
	}

	// ---------------- METHODS TO ANALYSE RESPONSES ----------------

	/**
	 * Calculates the amount of 'information' contained in the given candidate response.
	 *
	 * @param reply the candidate reply.
	 * @param userKeywords the list of keywords in the user's original line of text.
	 * @return a measure of the amount of information in the response.
	 */
	protected double calculateInformation(List<Symbol> reply, List<Symbol> userKeywords) {
		double info = 0.0;

		// Calculate information for the forward trie.
		info += calculateInformation(forwardTrie, reply, userKeywords);

		// Calculate information for the backward trie.
		Collections.reverse(reply);
		info += calculateInformation(backwardTrie, reply, userKeywords);
		Collections.reverse(reply);

		// Count the keywords in the reply, and scale the information value depending on how
		// many keywords are present.
		int num = 0;
		for (Symbol aReply : reply) {
			if (userKeywords.contains(aReply)) {
				num++;
			}
		}
		if (num > 4) {
			info /= Math.sqrt(num * 2 - 1);
			if (num > 8) {
				info /= (num * 2);
			}
		}
		return info;
	}
	
	protected double calculateInformation2(List<Symbol> reply, List<Symbol> userKeywords, String channel) {
		double info = 10.0;
		int wordCount = 0;

		if (!mega.keywordMemory.containsKey(channel)) {
			mega.keywordMemory.put(channel, new HashMap());
		}
		HashMap tempMap = (HashMap)mega.keywordMemory.get(channel);
		
		HashSet<Symbol> set = new HashSet<Symbol>();
		info = info / userKeywords.size();
		if (reply.size() < 15) {
			info -= 0.5;
		}
		else if (reply.size() < 20) {
			info -= 1;
		}
		else if (reply.size() < 25) {
			info -= 2;
		}
		else if (reply.size() <= 30) {
			info -= 3;
		}
		else {
			info -= 5;
		}
		
		for (Symbol s : reply) {
			if (userKeywords.contains(s)) {
				info += 0.5;
				if (set.contains(s)) {
					info -= 3;
				}
				if (!set.contains(s)) {
					set.add(s);
				}
				info -= 0.25 * wordCount;
				wordCount++;
			}
			if (tempMap.containsKey(s)) {
				info += 0.75 * ((double)tempMap.get(s) / 100);
				if (set.contains(s)) {
					info -= 0.25;
				}
				if (!set.contains(s)) {
					set.add(s);
				}
				info -= 0.5 * wordCount;
				wordCount++;
			}
			
		}
		
		return info;
	}

	/**
	 * Calculates the amount of 'information' contained in the given list of symbols, with
	 * respect to the given trie.
	 *
	 * @param trie the trie.
	 * @param reply the candidate reply.
	 * @param userKeywords the list of keywords in the user's original line of text.
	 * @return a measure of the amount of information in the response.
	 */
	protected double calculateInformation(TrieNode trie, List<Symbol> reply, List<Symbol> userKeywords) {
		double info = 0.0;

		for (int i = 0; i < reply.size(); i++) {
			Symbol symbol = reply.get(i);
			if (userKeywords.contains(symbol)) {
				int lowerBound = i - order - 1;
				if (lowerBound < 0) {
					lowerBound = 0;
				}
				info += -Math.log(calculateAverageProbability(trie, reply.subList(lowerBound, i + 1)));
			}
		}
		return info;
	}

	/**
	 * Calculates the average probability of the last symbol in a list being in each context.
	 *
	 * @param trie the trie.
	 * @param symbols the list of symbols.
	 * @return the average of the probability counts for each context this symbol is appearing in.
	 */
	protected double calculateAverageProbability(TrieNode trie, List<Symbol> symbols) {
		double total = 0.0;
		for (int i = 0; i < symbols.size(); i++) {
			TrieNode node = trie;
			if (node == null)
				return 0.0;
			TrieNode parent = null;
			for (int j = i; j < symbols.size(); j++) {
				parent = node;
				node = trie.getChild(symbols.get(i), false, stemmer, databaseManager);
			}
			assert parent != null; // because there is at least one symbol.
			total += (double) node.usage / (double) parent.count;
		}
		return (total / symbols.size());
	}
	
	public void updateFiles() {
		try {
        badWords = Utils.readStringSetFromFile("files/badWords.txt", true);
        spellIgnores = Utils.readStringSetFromFile("files/spellIgnores.txt", true);
		} catch (IOException e) {
			
		}
    }

}