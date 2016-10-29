package MegaHAL;

import java.util.*;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

/**
 * Main class implementing the main MegaHAL engine.
 * Provides methods to train the brain, and to generate text
 * responses from it.
 *
 * @author Trejkaz
 */
public class MegaHAL {

	// Fixed word sets.
	private final Map<Symbol, Symbol> swapWords;     // A mapping of words which will be swapped for other words.

	// Hidden Markov first_attempt.Model
	private final Model model;

	// Parsing utilities
	public Splitter splitter;
	private boolean dirty;

	private Set badWords;
	private Set spellIgnores;
	private Set smallWords;
	public JLanguageTool languageTool = new JLanguageTool(new AmericanEnglish());
	public HashMap keywordMemory = new HashMap();

	// Random Number Generator
	private final Random rng = new Random();

	/**
	 * Constructs the engine, reading the configuration from the data directory.
	 *
	 * @throws IOException if an error occurs reading the configuration.
	 */
	public MegaHAL(boolean dirty) throws IOException {
		/*
		 * 0. Initialise. Add the special "<BEGIN>" and "<END>" symbols to the
		 * dictionary. Ex: 0:"<BEGIN>", 1:"<END>"
		 *
		 * NOTE: Currently debating the need for a dictionary.
		 */
		//dictionary.add("<BEGIN>");
		//dictionary.add("<END>");

		this.dirty = dirty;
		swapWords = Utils.readSymbolMapFromFile("files/swap.txt");
		Set<Symbol> banWords = Utils.readSymbolSetFromFile("files/ban.txt");
		Set<Symbol> auxWords = Utils.readSymbolSetFromFile("files/auxiliary.txt");
		smallWords = Utils.readStringSetFromFile("files/smallWords.txt", true);
		badWords = Utils.readStringSetFromFile("files/badWords.txt", true);
		spellIgnores = Utils.readStringSetFromFile("files/spellIgnores.txt", true);

		for (Rule rule : languageTool.getAllActiveRules()) {
			if (rule instanceof SpellingCheckRule) {
				List<String> list = new ArrayList();
				list.addAll(spellIgnores);
				((SpellingCheckRule)rule).addIgnoreTokens(list);
			}
		}
		// TODO: Implement first message to user (formulateGreeting()?)
		Set<Symbol> greetWords = Utils.readSymbolSetFromFile("files/greetings.txt");
		SymbolFactory symbolFactory = new SymbolFactory(new SimpleKeywordChecker(banWords, auxWords));
		splitter = new WordNonwordSplitter(symbolFactory);

		model = new Model(badWords, spellIgnores, this);

		BufferedReader reader = new BufferedReader(new FileReader("files/log.txt"));
		String line;
		int trainCount = 0;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			line = line.substring(line.indexOf(":")+1);

			trainOnly(line, null);
			trainCount++;
		}
		reader.close();
		System.out.println("Trained with " + trainCount + " sentences.");
	}

	/**
	 * Trains on a single line of text.
	 *
	 * @param userText the line of text.
	 */
	public void trainOnly(String userText, String channel) {
		// Split the user's line into symbols.
		//Spell check our sentence now:
		//System.out.println("Start " + userText);
		userText = timeSpellGrammarCheck(1000, userText);
		//System.out.println("End " + userText);

		List<Symbol> userWords = splitter.split(userText);
		if (!dirty) {
			if (Utils.checkBadWords(badWords, userWords, userText)) {
				return;
			}
		}
		//update keword memory:
		if (channel != null) {
			updateMemory(userWords, channel, userText);
		}
		// Train the brain from the user's list of symbols.
		model.train(userWords, userText, channel);
	}

	/*
	 * Updates the memory for the given keywords assosciated with this channel:
	 */
	public void updateMemory(List userWords, String Channel, String userText) {
		if (!keywordMemory.containsKey(Channel)) {
			keywordMemory.put(Channel, new HashMap());
		}
		HashMap tempMap = (HashMap)keywordMemory.get(Channel);

		

		for (int j = 0; j < userWords.size(); j++) {
			if (userWords.get(j).toString() != "<START>" && userWords.get(j).toString() != "<END>" &&  userWords.get(j).toString() != " ") {
				if (!tempMap.containsKey(userWords.get(j))) {
					if (!Utils.checkBadWords(smallWords, userWords.get(j).toString())) {
						double k = 100;
						tempMap.put(userWords.get(j), k);
					}
					
				} else   {
					if (!Utils.checkBadWords(smallWords, userWords.get(j).toString())) {
						double k = (double)tempMap.get(userWords.get(j));
						k += 100;
						tempMap.put(userWords.get(j), k);
					}
				}
			}
		}

		//Now decay the memory:
		Set tempSet = tempMap.entrySet();
		Iterator i = tempSet.iterator();

		HashSet<Object> remove = new HashSet<Object>();

		while (i.hasNext()) {
			Map.Entry me = (Map.Entry)i.next();
			double tempDouble = (double)me.getValue();
			tempDouble = ((double)me.getValue())*0.90 - 5;
			tempMap.put(me.getKey(), tempDouble);
			if (tempDouble < 0) {
				remove.add(me.getKey());
			}
			System.out.print(me.getKey() + " ");
			System.out.print(me.getValue() + " | ");
		}
		System.out.println("");
		Object[] removeArray = remove.toArray();
		for (int j = 0; j < removeArray.length; j++) {
			tempMap.remove(removeArray[j]);
		}
	}

	/*
	 * Repeatedly check for spelling errors until the allotted time is up or there is no change:
	 * time to take is in milliseconds
	 */

	public String timeSpellGrammarCheck(int timeToTake, String userText) {
		if (dbids.spellCheck) {
			String lastText = "";
			long t0 = System.currentTimeMillis();
			//While our time is not up OR we have the same result:
			while (System.currentTimeMillis() - t0 < timeToTake && !lastText.equals(userText)) {
				lastText = userText;
				userText = checkSpellingAndGrammar(userText);
			}
		}

		return userText;
	}

	/*
	 * Check the spelling and grammar of the sentence and attempt to fix it
	 */
	public String checkSpellingAndGrammar(String userText) {
		List<RuleMatch> matches = null;
		//System.out.println("Start " + userText);
		//if (userText.equalsIgnoreCase("gl gl gl zammie baby")) {
		//int test = 0;
		//}

		try {
			matches = languageTool.check(userText);
		} catch (IOException e) {

		}
		String replace;
		List<String> replacements;
		if (matches != null) {
			//int index = 0;
			for (RuleMatch match : matches) {

				//System.out.println("Potential error: " + match.getMessage());

				replacements = match.getSuggestedReplacements();

				//If we have a replacement, do it
				if (!replacements.isEmpty() && !match.getMessage().contains("This phrase is duplicated.")) {
					//Choose first and try it:
					replace = replacements.get(0);
					userText = userText.substring(0, match.getFromPos()) + replace + userText.substring(match.getToPos(), userText.length());
					//System.out.println("Replaced " + match.getMessage() + " with " + replace);
					break;

				}
			}
		}

		//System.out.println("End " + userText);
		return userText;
	}

	/**
	 * Formulates a line back to the user, and also trains from the user's text.
	 *
	 * @param userText the line of text.
	 * @return the reply.
	 */
	public String formulateReply(String userText, String channel) {






		//Remove sneewo, trim edges:
		userText = Utils.cleanSentence(userText);
		// Split the user's line into symbols.
		List<Symbol> userWords = splitter.split(userText);
		if (!dirty) {
			if (Utils.checkBadWords(badWords, userWords, userText)) {
				return null;
			}
		}
		// Train the brain from the user's list of symbols.
		model.train(userWords, userText, channel);

		// Find keywords in the user's input.
		List<Symbol> userKeywords = new ArrayList<Symbol>(userWords.size());
		for (Symbol s : userWords) {
			if (s.isKeyword()) {
				Symbol swap = swapWords.get(s);
				if (swap != null) {
					s = swap;
				}
				userKeywords.add(s);
			}
		}

		// Generate candidate replies.
		int candidateCount = 0;
		double bestInfoContent = 0.0;
		List<Symbol> bestReply = null;
		int timeToTake = 1000 * 1; // 1 second(s).s
		long t0 = System.currentTimeMillis();
		while (System.currentTimeMillis() - t0 < timeToTake) {
			//System.out.print("Generating... ");
			List<Symbol> candidateReply = model.generateRandomSymbols(rng, userKeywords);
			candidateCount++;
			//System.out.println("Candidate: " + candidateReply);
			if (candidateReply != null) {
				//double infoContent = model.calculateInformation(candidateReply, userKeywords);
				double infoContent = model.calculateInformation2(candidateReply, userKeywords, channel);
				//System.out.println("infoContent="+infoContent);
				if (infoContent > bestInfoContent && !Utils.equals(candidateReply, userWords)) {
					bestInfoContent = infoContent;
					bestReply = candidateReply;
				}
			}
		}
		System.out.println("After " + candidateCount + " tries.");
		//System.out.println("Candidates generated: " + candidateCount);
		//System.out.println("Best reply generated: " + bestReply);

		// Return the generated string, tacked back together.
		return (bestReply == null) ? null : splitter.join(bestReply);
	}

	public void updateFiles() {
		model.updateFiles();
		try {
			badWords = Utils.readStringSetFromFile("files/badWords.txt", true);
			spellIgnores = Utils.readStringSetFromFile("files/spellIgnores.txt", true);
		} catch (IOException e) {

		}
	}

	public void wordIgnore(String ignoreWord, boolean add) {
		if (add) {
			for (Rule rule : languageTool.getAllActiveRules()) {
				if (rule instanceof SpellingCheckRule) {
					List<String> wordsToIgnore = Arrays.asList(ignoreWord);
					((SpellingCheckRule)rule).addIgnoreTokens(wordsToIgnore);
				}
			}
		} else {
			try {
				spellIgnores = Utils.readStringSetFromFile("files/spellIgnores.txt", true);
			} catch (IOException e) {

			}
			languageTool = new JLanguageTool(new AmericanEnglish());

			for (Rule rule : languageTool.getAllActiveRules()) {
				if (rule instanceof SpellingCheckRule) {
					List<String> list = new ArrayList();
					list.addAll(spellIgnores);
					((SpellingCheckRule)rule).addIgnoreTokens(list);
				}
			}
		}
	}
}