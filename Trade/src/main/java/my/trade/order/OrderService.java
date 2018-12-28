package my.trade.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.sun.java_cup.internal.runtime.Symbol;

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

	public String placeBuyLimitOrder(String tradingSymbol, String exchange, Integer qty, Double price, String orderType) {
		String transactionType = "BUY";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("quantity", Integer.toString(qty));
		params.add("exchange", exchange);
		params.add("tradingsymbol", tradingSymbol);
		params.add("transaction_type", transactionType);
		params.add("price", price.toString());
		params.add("order_type", orderType);

		return placeOrderType(params);
	}
	
	public String placeBuySLMOrder(String tradingSymbol, String exchange, Integer qty, Double price, String orderType) {
		String transactionType = "BUY";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("quantity", Integer.toString(qty));
		params.add("exchange", exchange);
		params.add("tradingsymbol", tradingSymbol);
		params.add("transaction_type", transactionType);
		params.add("trigger_price", price.toString());
		params.add("order_type", orderType);

		return placeOrderType(params);
	}
	
	public String placeSellLimitOrder(String tradingSymbol, String exchange, Integer qty, Double price, String orderType) {
		String transactionType = "SELL";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("quantity", Integer.toString(qty));
		params.add("exchange", exchange);
		params.add("tradingsymbol", tradingSymbol);
		params.add("transaction_type", transactionType);
		params.add("price", price.toString());
		params.add("order_type", orderType);

		return placeOrderType(params);
	}
	
	public String placeSellSLMOrder(String tradingSymbol, String exchange, Integer qty, Double price, String orderType) {
		String transactionType = "SELL";
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("quantity", Integer.toString(qty));
		params.add("exchange", exchange);
		params.add("tradingsymbol", tradingSymbol);
		params.add("transaction_type", transactionType);
		params.add("trigger_price", price.toString());
		params.add("order_type", orderType);

		return placeOrderType(params);
	}
	
	public String placeOrderType(MultiValueMap<String, String> params) {
		String tradeURL = "https://api.kite.trade/orders/regular";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());

		
		params.add("product", "MIS");
		params.add("validity", "DAY");

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params,
				headers);

		String response = fetchResponse(tradeURL, requestEntity);
		return response;
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
