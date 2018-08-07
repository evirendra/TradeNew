package my.trade.qrtz;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataCache {

private static Set<String> subscriptionSet = new HashSet<>();
	private static ConcurrentHashMap<String, Number> cachedData = new ConcurrentHashMap<>();
	
	static {
		subscriptionSet.add("NSE:NIFTY BANK");
		subscriptionSet.add("NSE:BANKINDIA");
		subscriptionSet.add("NSE:ADANIENT");
		subscriptionSet.add("NSE:NIFTY BANK");
		subscriptionSet.add("NSE:JSWSTEEL");
		subscriptionSet.add("NSE:ADANIPORTS");
		subscriptionSet.add("NSE:YESBANK");
		subscriptionSet.add("NSE:AXISBANK");
		subscriptionSet.add("NSE:RBLBANK");
		subscriptionSet.add("NSE:ICICIBANK");
		subscriptionSet.add("NSE:SBIN");
		subscriptionSet.add("NSE:HEXAWARE");
		subscriptionSet.add("NSE:WIPRO");
	}
	public static void add(String key, Number value) {
		cachedData.put(key, value);
	}

	public static Number get(String key) {
		Number value = cachedData.get(key);
		return value;
	}

	public static String toStringValue() {
		return cachedData.toString();
	}

	public static void subscribe(String instrumentSymbol) {
		subscriptionSet.add(instrumentSymbol);
	}

	public static void subscribe(List<String> instrumentSymbols) {
		subscriptionSet.addAll(instrumentSymbols);
	}

	public static Set<String> getSubscriptions() {
		return subscriptionSet;
	}

	public static ConcurrentHashMap<String, Number> getCachedData() {
		return cachedData;
	}
	
	public static boolean contains(String symbol) {
	
		return cachedData.containsKey(symbol);
	}
	
	
}
