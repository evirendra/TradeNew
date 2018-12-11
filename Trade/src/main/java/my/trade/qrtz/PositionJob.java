package my.trade.qrtz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import my.trade.KeyCache;
import my.trade.Position;
import my.trade.PositionCache;
import my.trade.order.OrderService;

public class PositionJob extends QuartzJobBean {

	private static final String DAY_SELL_QUANTITY = "DAY_SELL_Quantity";
	private static final Logger logger = LoggerFactory.getLogger(PositionJob.class);

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		if (KeyCache.containAccessToken()) {
			try {
				// logger.info("fetching positions");
				fetchPositions();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void fetchPositions() throws IOException {
		String ltpURL = "https://api.kite.trade/portfolio/positions";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());
		HttpMethod httpMethod = HttpMethod.GET;

		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(ltpURL, httpMethod, entity, String.class);
		// System.out.println(response.getBody());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(response.getBody());
		// System.out.println("jsonNode :" + jsonNode);
		JsonNode dataObject = jsonNode.get("data");
		// System.out.println("data :" + dataObject );
		
		JsonNode netObject = dataObject.get("net");
		// System.out.println("netObject :" + netObject );
		Iterator<JsonNode> jsonNodeElements = netObject.elements();

		while (jsonNodeElements.hasNext()) {
			JsonNode netObjectElement = jsonNodeElements.next();
			String tradingSymbol = netObjectElement.get("tradingsymbol").asText();
			String exchange = netObjectElement.get("exchange").asText();
			Integer quantity = netObjectElement.get("quantity").asInt();
			Double averagePrice = netObjectElement.get("average_price").asDouble();
			Integer daySellQuantity = netObjectElement.get("day_sell_quantity").asInt();

			if (quantity < 0) {
				logger.info(dataObject.toString());
				if (PositionCache.contains(tradingSymbol)) {
					Integer orderPlacedForDaySellqty = Integer.valueOf(PositionCache.getKey(tradingSymbol));

					if (daySellQuantity > orderPlacedForDaySellqty) {
						actAsPerPosition(tradingSymbol, exchange, quantity, averagePrice, daySellQuantity);
					}
				} else {
					actAsPerPosition(tradingSymbol, exchange, quantity, averagePrice, daySellQuantity);
				}
			}
		}

		// for (String instrumentSymbol : DataCache.getSubscriptions()) {
		// Number lastPrice = getLastPrice(dataObject, instrumentSymbol);
		// DataCache.add(instrumentSymbol, lastPrice);
		// }

	}

	private void actAsPerPosition(String tradingSymbol, String exchange, Integer quantity, Double averagePrice,
			Integer daySellQuantity) {

		Position position = new Position();
		position.populate(tradingSymbol, exchange, quantity, averagePrice, daySellQuantity, position);
		try {
			fetchOrders(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		logger.info(position.toString());

		// Place SL Order

		OrderService orderService = new OrderService();
		int pq = position.getQuantity() * -1;
		String slot = "SL-M";
		Double sp = position.getRoundedAveragePrice() + 8;
		orderService.placeBuySLMOrder(position.getTradingSymbol(), position.getExchange(), pq, sp, slot);

		// Place target Order

		Double tp = position.getRoundedAveragePrice() - 3;
		String tot = "LIMIT";
		orderService.placeBuyLimitOrder(position.getTradingSymbol(), position.getExchange(), pq, tp, tot);
		PositionCache.addKey(tradingSymbol, position.getDaySellQuantity().toString());
		logger.info(PositionCache.convertToString());
	}

	private Number getLastPrice(JsonNode dataObject, String instrumentName) {
		JsonNode instrumentDetails = dataObject.get(instrumentName);
		Number lastPrice = (Number) instrumentDetails.get("last_price").doubleValue();
		return lastPrice;
	}

	private void fetchOrders(Position position) throws IOException {
		System.out.println("Fetching Orders");
		String ltpURL = "https://api.kite.trade/orders";
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());
		HttpMethod httpMethod = HttpMethod.GET;

		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(ltpURL, httpMethod, entity, String.class);
		// System.out.println(response.getBody());
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(response.getBody());
		// System.out.println("jsonNode :" + jsonNode);
		JsonNode dataObject = jsonNode.get("data");
//		logger.info("data :" + dataObject);
		Iterator<JsonNode> jsonNodeElements = dataObject.elements();

		List<JsonNode> completedOrders = new ArrayList<>();
		while (jsonNodeElements.hasNext()) {
			JsonNode element = jsonNodeElements.next();
			String status = element.get("status").asText();
			if("COMPLETE".equalsIgnoreCase(status)) {
				completedOrders.add(element);
			}
		}
		
		
		for(int i =completedOrders.size()-1;  i>=0; i--) {
			JsonNode completedOrder = completedOrders.get(i);
			int quantity = completedOrder.get("quantity").asInt() *-1;
			String tradingSymbol = completedOrder.get("tradingsymbol").asText();
			String exchange = completedOrder.get("exchange").asText();
			Double averagePrice = completedOrder.get("average_price").asDouble();
			if(quantity == position.getQuantity() &&  tradingSymbol.equalsIgnoreCase(position.getTradingSymbol())
					&& exchange.equals(position.getExchange())) {
				position.setAveragePrice(averagePrice);
				break;
			}
		}
	}
}
