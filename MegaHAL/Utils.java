package MegaHAL;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Class for static utility methods.
 *
 * @author Trejkaz
 */
public class Utils {

    public static Map<Symbol,Symbol> readSymbolMapFromFile(String filename) throws IOException {
        Map<Symbol,Symbol> map = new HashMap<Symbol,Symbol>();

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.charAt(0) == '#') {
                continue;
            }
            String[] words = line.split("\t");
            if (words.length != 2) {
                continue;
            }
            map.put(new Symbol(words[0], false), new Symbol(words[1], false));
        }
        reader.close();

        return map;
    }

    public static Set<Symbol> readSymbolSetFromFile(String filename) throws IOException {
        HashSet<Symbol> set = new HashSet<Symbol>();

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.charAt(0) == '#') {
                continue;
            }
            set.add(new Symbol(line, false));
        }
        reader.close();

        return set;
    }
    
    public static Set<String> readStringSetFromFile(String filename, boolean upper) throws IOException {
        
    	HashSet<String> set = new HashSet<String>();

    	try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
    	
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.charAt(0) == '#') {
                continue;
            }
            if (upper)
            	line = line.toUpperCase();
            set.add(line);
        }
        reader.close();
    } catch (IOException e) {
		e.printStackTrace();
	} 

        return set;
    }
    
    public static String cleanSentence(String aSentence) {
    	String upper;
    	int index;
    	while (true) {
    	upper = aSentence.toUpperCase();
    	index = upper.indexOf("SNEEWO");
    	if (index == -1) {
    		break;
    	}
    	if (index+6 <= aSentence.length()) {
    		aSentence = aSentence.substring(0, index) + aSentence.substring(index+6, aSentence.length());
    		aSentence = aSentence.trim();
    	}
    	}
    	
    	return aSentence;
    }

    public static boolean equals(List l1, List l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        Iterator il1 = l1.iterator();
        Iterator il2 = l2.iterator();
        while (il1.hasNext()) {
            if (!il1.next().equals(il2.next())) {
                return false;
            }
        }
        return true;
    }
    
  //Function to check for bad words:
  	public static boolean checkBadWords(Set badWords, List<Symbol> symbols, String userText) {
  		Object[] badWordsArray = badWords.toArray();
  		
  		for (int j = 0; j < badWords.size(); j++) {

  			String currentWord = badWordsArray[j].toString();
  			if (badWordsArray[j].toString().charAt(0) == '*') {
  				//Check for exclusive use of word, ie does it have spaces
  				//make sure our string is long enough
  				if (badWordsArray[j].toString().length() < userText.length()) {
  					//Check if the first word is bad
  					if (userText.substring(0, currentWord.length()).equalsIgnoreCase(currentWord.substring(1) + " ")) {
  						return true;
  					}
  					//Check if the last word is bad
  					if (userText.substring(userText.length()-currentWord.length(), userText.length()).equalsIgnoreCase(" " + currentWord.substring(1))) {
  						return true;
  					}
  					//check if it's a middle word
  					if (userText.toUpperCase().contains(" " + currentWord + " ")) {
  						return true;
  					}
  				}
  			} 
  			
  			//Check for any occurance of the word:
  			if (userText.toUpperCase().contains(currentWord.toUpperCase())) {
  				//else it is non exclusive, just check if it's anywhere
				//System.out.println("Bad word found.");
				return true;
  			}
  		}
  		//nothing bad in sentence
  		return false;
  	}
  	
  	public static boolean checkBadWords(Set badWords, String word) {
  		Object[] badWordsArray = badWords.toArray();
  		String currentWord = "";
  		for (int j = 0; j < badWords.size(); j++) {

  			currentWord = badWordsArray[j].toString();
  			//Check for any occurance of the word:
  			if (word.equalsIgnoreCase(currentWord)) {
  				//else it is non exclusive, just check if it's anywhere
				//System.out.println("Bad word found.");
				return true;
  			}
  		}
  		//nothing bad in sentence
  		return false;
  	}
}