package MegaHAL;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.encog.mathutil.Convert;

public class TriePaths {
	private Map<Symbol, Long> map = null;
	long usage;
	
	public TriePaths() {
		map = new HashMap<Symbol, Long>();
		usage = (long) 0;
	}
	
	public void increasePath(Symbol aSymbol) {
		if (map.containsKey(aSymbol)) {
			//Theres already a key, increase the values:
			Long value = map.get(aSymbol);
			value++;
			map.put(aSymbol, value);
		} else {
			//No key place it in
			map.put(aSymbol, new Long(1));
		}
		usage++;
	}
	
	public Symbol getRandomPath(Random rng) {
		Object[] keys = map.keySet().toArray();
		long n = (long)(rng.nextDouble()*(double)usage);
		for(int i = 0; i < keys.length; i++) {
			n -= map.get(keys[i]);
			if (n <= 0) {
				return (Symbol)keys[i];
			}
		}
		System.out.println( "Error with calculating random path.");
		return null;
	}
}
