package my.trade.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import my.trade.BankNiftyData;
import my.trade.BankNiftyOptionData;
import my.trade.ExitCache;
import my.trade.KeyCache;

public class OrderService {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);


	public String placeSellOrder(String symbol, Integer qty) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		String orderType = "SELL";
		params.add("quantity", Integer.toString(qty));
		String[] split = symbol.split(":");
		params.add("exchange", split[0]);
		params.add("tradingsymbol", split[1]);
		params.add("transaction_type", orderType);

		return placeOrder(params);
	}

	public String placeBuyOrder(String symbol, Integer qty) {
		if (!ExitCache.getCache().isTestMode()) {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
			String orderType = "BUY";
			params.add("quantity", Integer.toString(qty));
			String[] split = symbol.split(":");
			params.add("exchange", split[0]);
			params.add("tradingsymbol", split[1]);
			params.add("transaction_type", orderType);

			return placeOrder(params);
		} else {
			logger.info("Running in Test Mode, Buy order will not be Placed");
		}
		return "";
	}

	public String placeOrder(MultiValueMap<String, String> params) {
		String tradeURL = "https://api.kite.trade/orders/regular";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());

		params.add("order_type", "MARKET");
		params.add("product", "MIS");
		params.add("validity", "DAY");

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params,
				headers);

		String response = fetchResponse(tradeURL, requestEntity);
		return response;
	}

	public String fetchResponse(String url, HttpEntity<MultiValueMap<String, String>> requestEntity) {
		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.postForObject(url, requestEntity, String.class);
		return response;
	}

	
}
