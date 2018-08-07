package my.trade;

import java.util.HashMap;

public class IchimokuCache {

	private static HashMap<String, IchimokuIndicator> ichimoku1MinIndicators = new HashMap<>();
	private static HashMap<String, IchimokuIndicator> ichimoku5MinIndicators = new HashMap<>();
	private static HashMap<String, IchimokuIndicator> ichimoku15MinIndicators = new HashMap<>();

	public static void add1Min(String symbol, IchimokuIndicator ichimokuIndicator) {
		ichimoku1MinIndicators.put(symbol, ichimokuIndicator);
	}

	public static IchimokuIndicator get1Min(String symbol) {
		return ichimoku1MinIndicators.get(symbol);
	}

	public static boolean contains1Min(String symbol) {
		return ichimoku1MinIndicators.containsKey(symbol);
	}

	public static void add5Min(String symbol, IchimokuIndicator ichimokuIndicator) {
		ichimoku5MinIndicators.put(symbol, ichimokuIndicator);
	}

	public static IchimokuIndicator get5Min(String symbol) {
		return ichimoku5MinIndicators.get(symbol);
	}

	public static boolean contains5Min(String symbol) {
		return ichimoku5MinIndicators.containsKey(symbol);
	}

	public static void add15Min(String symbol, IchimokuIndicator ichimokuIndicator) {
		ichimoku15MinIndicators.put(symbol, ichimokuIndicator);
	}

	public static IchimokuIndicator get15Min(String symbol) {
		return ichimoku15MinIndicators.get(symbol);
	}

	public static boolean contains15Min(String symbol) {
		return ichimoku15MinIndicators.containsKey(symbol);
	}
}
