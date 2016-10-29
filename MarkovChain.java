
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.util.ArrayList;

public class MarkovChain {
	
	static Random rnd = new Random();
	public static Hashtable<String, Vector<String>> markovChain = new Hashtable<String, Vector<String>>();
	public Vector<String> startWords, wordLink, endWords, newPhrase;
	int wordSelectionLen, startWordsLen, i;
	String nextWord;
	String[] words;
	
	public MarkovChain() {
		markovChain.put("_start", new Vector<String>());
		markovChain.put("_end", new Vector<String>());
	}
	
	public void addWords(String phrase) {
		
		words = phrase.split(" ");
		
		for (i=0; i<words.length; i++) {
			
			// Add the start and end words to their own
			if (i == 0) {
				startWords = markovChain.get("_start");
				startWords.add(words[i]);
				
				wordLink = markovChain.get(words[i]);
				if (wordLink == null) {
					wordLink = new Vector<String>();
					wordLink.add(words[i+1]);
					markovChain.put(words[i], wordLink);
				}
				
			} else if (i == words.length-1) {
				endWords = markovChain.get("_end");
				endWords.add(words[i]);
				
			} else {	
				wordLink = markovChain.get(words[i]);
				if (wordLink == null) {
					wordLink = new Vector<String>();
					wordLink.add(words[i+1]);
					markovChain.put(words[i], wordLink);
				} else {
					wordLink.add(words[i+1]);
					markovChain.put(words[i], wordLink);
				}
			}
		}
	}
		
	public String generateSentence() {

		// Vector to hold the phrase
		newPhrase = new Vector<String>();

		// String for the next word
		nextWord = "";

		// Select the first word
		startWords = markovChain.get("_start");
		startWordsLen = startWords.size();
		nextWord = startWords.get(rnd.nextInt(startWordsLen));
		newPhrase.add(nextWord);

		// Keep looping through the words until we've reached the end
		while (nextWord.charAt(nextWord.length() - 1) != '.') {
			wordLink = markovChain.get(nextWord);
			wordSelectionLen = wordLink.size();
			nextWord = wordLink.get(rnd.nextInt(wordSelectionLen));
			newPhrase.add(nextWord);
		}
		return newPhrase.toString();
	}
}
