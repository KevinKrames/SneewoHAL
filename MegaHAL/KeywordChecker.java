package MegaHAL;
/**
 * Abstract class implementing a check to see if a word is a 'keyword'.
 * TODO: Make KeywordChecker an interface, and have SimpleKeywordChecker implement it.
 *
 * @author Trejkaz
 */
public abstract class KeywordChecker {
    public abstract boolean isKeyword(String symbol);

    public boolean isWord(String symbol) {
    	try {
        return isWordCharacter(symbol.charAt(0));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return false;
    }

    public boolean isWordCharacter(char ch) {
        return Character.isLetter(ch) || (ch == '\'') || (ch == ',');
    }
}