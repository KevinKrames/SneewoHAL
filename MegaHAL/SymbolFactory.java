package MegaHAL;
/**
 * Factory which creates symbols.
 *
 * @author Trejkaz
 */
public class SymbolFactory {
    /**
     * Checks whether symbols are keywords.
     */
    private final KeywordChecker checker;

    /**
     * Constructs the factory.
     *
     * @param checker checks whether symbols are keywords.
     */
    public SymbolFactory(KeywordChecker checker) {
        this.checker = checker;
    }

    /**
     * Creates a symbol.
     *
     * @param symbol the string value.
     * @return the symbol.
     */
    public Symbol createSymbol(String symbol) {
    	if (symbol == null)
    		return null;
        return new Symbol(symbol, checker.isKeyword(symbol));
    }

	public Symbol createImportantSymbol(String symbol) {
		if (symbol == null)
			return null;
		return new ImportantSymbol(symbol, checker.isKeyword(symbol));
	}


}