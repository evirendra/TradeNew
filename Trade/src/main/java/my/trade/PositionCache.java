package my.trade;

import java.util.HashMap;

public class PositionCache {

	public static final HashMap<String, String> data = new HashMap<>();

	public static void addKey(String key, String value) {
		data.put(key, value);
	}

	public static String getKey(String key) {
		return data.get(key);
	}

	public static String convertToString() {
		return data.toString();
	}

	public static boolean contains(String key) {
		return data.containsKey(key);
	}

}
