package MegaHAL;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A splitter which splits on word boundaries, splitting the string into a list
 * of 'word' fragments and 'non-word' fragments.
 *
 * @author Trejkaz
 */
public class WordNonwordSplitter implements Splitter {

	/**
	 * The regex pattern which defines word boundaries.
	 */
	private static Pattern boundaryPattern = Pattern.compile("([^\\s']+|[\\s']+)");

	/**
	 * Symbol factory for creating symbols.
	 */
	private final SymbolFactory symbolFactory;

	/**
	 * Creates the splitter.
	 *
	 * @param symbolFactory
	 *            symbol factory for creating symbols.
	 */
	public WordNonwordSplitter(SymbolFactory symbolFactory) {
		this.symbolFactory = symbolFactory;
	}

	/**
	 * splits the symbols into important/unimportant symbols against the
	 * unimportant symbols:
	 * 
	 * @return List of symbols split based on the unimportant symbols
	 * @param text
	 *            The raw text input for the splitter
	 * @param unimportantSymbols
	 *            The list of small unimportant symbols to sort by.
	 */

	public List<Symbol> split(String text, List<String> unimportantSymbols) {
		List<Symbol> symbolList = new ArrayList<Symbol>();
		String currentSymbol = "";
		symbolList.add(Symbol.START);
		if (text.length() > 0) {
			// Loop through all string segments separated by spaces
			for (String temp : text.split(" ")) {
				if (temp.length() > 0) {
					// If this is an unimportant symbol, add it string
					if (unimportantSymbols.contains(temp.toUpperCase())) {
						currentSymbol += temp + " ";
					} else {
						//This is an important symbol, act accordingly:
						if (currentSymbol.length() > 0) {
							//Add the unimportant symbols sentence as a single symbol
							currentSymbol = currentSymbol.substring(0, currentSymbol.length()-1);
							
							Symbol s = symbolFactory.createSymbol(currentSymbol);
							if (s != null) {
								if (s.toString() != " ")
									symbolList.add(s);
							}
							currentSymbol = "";
						}
						
						Symbol s = symbolFactory.createImportantSymbol(temp);
						if (s != null) {
							if (s.toString() != " ")
								symbolList.add(s);
						}
					}
				}
			}
		}
		symbolList.add(Symbol.END);
		return symbolList;
	}

	public String join(List<Symbol> symbols) {
		// Chop off the <START> and <END>
		symbols = symbols.subList(1, symbols.size() - 1);

		// Build up the rejoined list.
		StringBuffer result = new StringBuffer();
		for (Symbol symbol : symbols) {
			result.append(symbol.toString());
			result.append(" ");
		}
		if (result.length() > 0)
			result.deleteCharAt(result.length() - 1);
		return result.toString();
	}
}