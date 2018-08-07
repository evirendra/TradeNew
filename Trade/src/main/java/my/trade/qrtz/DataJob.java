package my.trade.qrtz;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import my.trade.KeyCache;

public class DataJob extends QuartzJobBean {

	private static final Logger logger = LoggerFactory.getLogger(DataJob.class);

	String instruments[] = new String[] { "NSE:NIFTY BANK", "NFO:BANKNIFTY12JUL1826100PE",
			"NFO:BANKNIFTY12JUL1826300PE", "NFO:BANKNIFTY12JUL1826400PE", "NFO:BANKNIFTY12JUL1826500CE",
			"NFO:BANKNIFTY12JUL1826600CE", "NFO:BANKNIFTY12JUL1826700CE", "NFO:BANKNIFTY12JUL1826800CE" };

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

		Set<String> subscriptions = DataCache.getSubscriptions();
		// logger.info("Subscriptions are :" + subscriptions);
		if (KeyCache.containAccessToken() && !subscriptions.isEmpty()) {

			// String instrnsumentsStr = StringUtils.arrayToDelimitedString(instruments,
			// "&i=");

			String instrnsumentsStr = StringUtils.collectionToDelimitedString(subscriptions, "&i=");

			String instrns = "i=" + instrnsumentsStr;
			try {
				fetchLTP(instrns);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			insertIntoDB();

			// logger.info(Cache.toStringValue());
		}

	}

	private void insertIntoDB() {
		ConcurrentHashMap<String, Number> cachedData = DataCache.getCachedData();
		if (!cachedData.isEmpty()) {
			List<Object[]> inputList = new ArrayList<Object[]>();
			Set<Entry<String, Number>> entrySet = cachedData.entrySet();
			for (Entry<String, Number> entry : entrySet) {
				List<Object> tmpList = new ArrayList<>();
				tmpList.add(entry.getKey());
				tmpList.add(entry.getValue());
				Object[] array = tmpList.toArray();
				inputList.add(array);
			}

			String sql = "INSERT INTO data(time, symbol, ltp)  VALUES (NOW(), ?, ?)";
			int[] batchUpdate = jdbcTemplate.batchUpdate(sql, inputList);
			// logger.info("inserted into DB: " + batchUpdate.length);
		}
	}

	private void fetchLTP(String instrument) throws IOException {
		String ltpURL = "https://api.kite.trade/quote/ltp?" + instrument;
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Kite-Version", "3");
		headers.add("Authorization", KeyCache.getAuthorizationStr());
		HttpMethod httpMethod = HttpMethod.GET;

		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(ltpURL, httpMethod, entity, String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(response.getBody());
		JsonNode dataObject = jsonNode.get("data");

		for (String instrumentSymbol : DataCache.getSubscriptions()) {
			Number lastPrice = getLastPrice(dataObject, instrumentSymbol);
			DataCache.add(instrumentSymbol, lastPrice);
		}

	}

	private Number getLastPrice(JsonNode dataObject, String instrumentName) {
		JsonNode instrumentDetails = dataObject.get(instrumentName);
		Number lastPrice = (Number) instrumentDetails.get("last_price").doubleValue();
		return lastPrice;
	}
}
