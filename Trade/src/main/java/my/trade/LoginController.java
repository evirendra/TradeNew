package my.trade;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import my.trade.order.OrderService;
import my.trade.qrtz.DataCache;

@RestController
public class LoginController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@RequestMapping("/api/login")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public ResponseEntity<String> login(@RequestParam(value = "name", defaultValue = "World") String name) {

		String loginURL = "https://kite.trade/connect/login?v= {v}&api_key={api_key}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		Map<String, String> params = new HashMap<>();
		params.put("v", "3");
		params.put("api_key", KeyCache.getAPIKey());
		HttpMethod httpMethod = HttpMethod.POST;

		ResponseEntity<String> response = fetchResponse(loginURL, headers, params, httpMethod);

		return response;
	}

	@RequestMapping("/api/home")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public ResponseEntity<String> home(@RequestParam(value = "request_token") String requestToken) throws IOException {

		String hashableText = KeyCache.getAPIKey() + requestToken + KeyCache.getAPISecretKey();
		String sha256hex = DigestUtils.sha256Hex(hashableText);
		String SessionTokenURL = "https://api.kite.trade/session/token";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("api_key", KeyCache.getAPIKey());
		map.add("request_token", requestToken);
		map.add("checksum", sha256hex);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(SessionTokenURL, request, String.class);

		String jsonSessionInfo = response.getBody();

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(jsonSessionInfo);
		JsonNode dataNode = jsonNode.get("data");

		String accessToken = dataNode.get("access_token").textValue();

		KeyCache.addAccessTokenKey(accessToken);
		logger.info("KeyCache :" + KeyCache.convertToString());
		// ResponseEntity<String> responseforLTP = fetchLTP(accessToken);
		return response;

	}

	@RequestMapping("/api/bankNiftyData")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public BankNiftyData bankNiftyData() throws InterruptedException {

		BankNiftyData bankNiftyData = new BankNiftyData();

		DataCache.subscribe(bankNiftyData.getInstrumentName());
		bankNiftyData.setLtpPrice(DataCache.get(bankNiftyData.getInstrumentName()));

		String populateOptionData = bankNiftyData.populateOptionData();
		Thread.sleep(2000);
		fillLastPriceOptionData(bankNiftyData);

		BankNiftyPosition bnp = new BankNiftyPosition();
		bnp.populateBankNiftyPosition();
		bankNiftyData.setBankNiftyPosition(bnp);
		return bankNiftyData;

	}

	@RequestMapping("/api/refreshBankNiftyData")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public BankNiftyData refreshBankNiftyData(@RequestParam(value = "bankNiftyLtpPrice") Double bankNiftyLtpPrice) {

		BankNiftyData bankNiftyData = new BankNiftyData();
		bankNiftyData.setLtpPrice(bankNiftyLtpPrice);
		String populateOptionData = bankNiftyData.populateOptionData();
		bankNiftyData.setLtpPrice(DataCache.get(bankNiftyData.getInstrumentName()));
		fillLastPriceOptionData(bankNiftyData);
		BankNiftyPosition bnp = new BankNiftyPosition();
		bnp.populateBankNiftyPosition();
		bankNiftyData.setBankNiftyPosition(bnp);
		return bankNiftyData;

	}

	@RequestMapping("/api/exitNow")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public boolean exitNow() {

		OrderService os = new OrderService();
		ExitCache ec = ExitCache.getCache();

		logger.info("Exit Immediately triggerd");

		if (ec.isExitActionEnabled()) {

			String callInstrumentSymbol = BankNiftyOptionData.getInstrumentName(ec.getCallOption(), true);
			String putInstrumentSymbol = BankNiftyOptionData.getInstrumentName(ec.getPutOption(), false);
			os.placeBuyOrder(callInstrumentSymbol, ec.getCallOptionQty());
			os.placeBuyOrder(putInstrumentSymbol, ec.getPutOptionQty());
			ec.setExitActionEnabled(false);
			logger.info("Exited");
		} else {
			logger.info("Exit Action is Disabled");
		}

		return true;

	}

	private void fillBankNiftyLtpPrice(BankNiftyData bankNiftyData, ResponseEntity<String> optionResponse)
			throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(optionResponse.getBody());
		JsonNode dataObject = jsonNode.get("data");
		Number lastPrice = getLastPrice(bankNiftyData, dataObject);
		bankNiftyData.setLtpPrice(lastPrice);
	}

	@RequestMapping("/api/updateExitCache")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public boolean updateExitCache(@RequestParam(value = "bankNiftyUpperThreshHold") String bankNiftyUpperThreshHold,
			@RequestParam(value = "bankNiftyLowerThreshHold") String bankNiftyLowerThreshHold,
			@RequestParam(value = "callOptionSymbol") String callOptionSymbol,
			@RequestParam(value = "callOptionQty") String callOptionQty,
			@RequestParam(value = "putOptionSymbol") String putOptionSymbol,
			@RequestParam(value = "putOptionQty") String putOptionQty,
			@RequestParam(value = "optionTotalUpper") String optionTotalUpper,
			@RequestParam(value = "optionTotalLower") String optionTotalLower,
			@RequestParam(value = "exitAtCallOption") String exitAtCallOption,
			@RequestParam(value = "exitAtPutOption") String exitAtPutOption,
			@RequestParam(value = "exitActionEnabled") boolean exitActionEnabled,
			@RequestParam(value = "testMode") boolean testMode,
			@RequestParam(value = "sellIfTotalReachesFlag") boolean sellIfTotalReachesFlag,
			@RequestParam(value = "sellIfTotalReachesAmount") String sellIfTotalReachesAmount) {

		ExitCache.getCache().populateExitCache(bankNiftyUpperThreshHold, bankNiftyLowerThreshHold, optionTotalUpper,
				optionTotalLower, exitAtCallOption, exitAtPutOption, testMode, callOptionSymbol, callOptionQty,
				putOptionSymbol, putOptionQty, exitActionEnabled, sellIfTotalReachesFlag, sellIfTotalReachesAmount);

		insertExitDataInDB();

		logger.info("Exit Action Defined :" + ExitCache.getCache());

		return true;
	}

	@RequestMapping("/api/retrieveExitCond")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public ExitCache retrieveExitCond() {

		String sql = "select * from exitcriteria where exit_sys_enabled = true";

		List<ExitCache> exitCache = jdbcTemplate.query(sql, new RowMapper<ExitCache>() {

			@Override
			public ExitCache mapRow(ResultSet rs, int rowNum) throws SQLException {
				ExitCache ec = new ExitCache();
				ec.setBankNiftyUpperLimit(rs.getDouble("bn_upper_limit"));
				ec.setBankNiftyLowerLimit(rs.getDouble("bn_lower_limit"));
				ec.setOptionTotalUpperLimit(rs.getDouble("option_total_upper_limit"));
				ec.setOptionTotalLowerLimit(rs.getDouble("option_total_lower_limit"));
				ec.setCallOptionUpperLimit(rs.getDouble("call_option_upper_limit"));
				ec.setPutOptionUpperLimit(rs.getDouble("put_option_upper_limit"));
				ec.setSellIfTotalReachesFlag(rs.getBoolean("option_sell_Enabled"));
				ec.setSellIfTotalReachesAmount(rs.getDouble("option_sell_total"));
				ec.setTestMode(rs.getBoolean("testing_mode"));
				ec.setCallOption(rs.getDouble("action_call_option_ref"));
				ec.setCallOptionQty(rs.getInt("action_call_option_qty"));
				ec.setPutOption(rs.getDouble("action_put_option_ref"));
				ec.setPutOptionQty(rs.getInt("action_put_option_qty"));
				ec.setExitActionEnabled(rs.getBoolean("action_exit_enabled"));
				ec.setExitSystemEnabled(rs.getBoolean("exit_sys_enabled"));

				return ec;
			}
		});
		ExitCache ec = null;
		if (exitCache.isEmpty() == false) {
			ec = exitCache.get(0);
			logger.info("Exit Action Retrieved :" + ec);
		}
		return ec;
	}

	private void insertExitDataInDB() {

		jdbcTemplate.update("update exitcriteria set exit_sys_enabled = false  where exit_sys_enabled = true");

		ExitCache ec = ExitCache.getCache();

		String sql = "INSERT INTO exitcriteria(bn_upper_limit,bn_lower_limit,option_total_upper_limit,option_total_lower_limit,"
				+ " call_option_upper_limit,put_option_upper_limit,option_sell_Enabled,option_sell_total,"
				+ " testing_mode,action_call_option_ref,action_call_option_qty,action_put_option_ref,"
				+ " action_put_option_qty,action_exit_enabled,exit_sys_enabled) "
				+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

		int update = jdbcTemplate.update(sql, ec.getBankNiftyUpperLimit(), ec.getBankNiftyLowerLimit(),
				ec.getOptionTotalUpperLimit(), ec.getOptionTotalLowerLimit(), ec.getCallOptionUpperLimit(),
				ec.getPutOptionUpperLimit(), ec.isSellIfTotalReachesFlag(), ec.getSellIfTotalReachesAmount(),
				ec.isTestMode(), ec.getCallOption(), ec.getCallOptionQty(), ec.getPutOption(), ec.getPutOptionQty(),
				ec.isExitActionEnabled(), true);

		if (update > 0)
			ec.setExitSystemEnabled(true);

	}

	private void fillLastPriceOptionData(BankNiftyData bankNiftyData) {
		List<BankNiftyOptionData> callOptionData = bankNiftyData.getCallOptionData();
		for (BankNiftyOptionData bankNiftyOptionData : callOptionData) {
			bankNiftyOptionData.setLtpPrice(DataCache.get(bankNiftyOptionData.getInstrumentName()));
		}

		List<BankNiftyOptionData> putOptionData = bankNiftyData.getPutOptionData();
		for (BankNiftyOptionData bankNiftyOptionData : putOptionData) {
			bankNiftyOptionData.setLtpPrice(DataCache.get(bankNiftyOptionData.getInstrumentName()));
		}

	}

	private Number getLastPrice(BankNiftyData bankNiftyData, JsonNode dataNode) {
		JsonNode instrumentDetails = (JsonNode) dataNode.get(bankNiftyData.getInstrumentName());
		Number lastPrice = (Number) instrumentDetails.get("last_price").doubleValue();
		return lastPrice;
	}

	private ResponseEntity<String> fetchResponse(String url, HttpHeaders headers, Map<String, String> params,
			HttpMethod httpMethod) {
		RestTemplate restTemplate = new RestTemplate();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class, params);
		return response;
	}

	@RequestMapping("/api/options/sell")
	@CrossOrigin(origins = { "http://localhost:4200",
			"http://ec2-13-232-178-151.ap-south-1.compute.amazonaws.com:4200" })
	public String Sell(@RequestParam(value = "callOptionSymbol") String callOptionSymbol,
			@RequestParam(value = "callOptionQty") Integer callOptionQty,
			@RequestParam(value = "putOptionSymbol") String putOptionSymbol,
			@RequestParam(value = "putOptionQty") Integer putOptionQty) throws InterruptedException {

		OrderService os = new OrderService();
		String callOptionResponse = os.placeSellOrder(callOptionSymbol, callOptionQty);
		String putOptionResponse = os.placeSellOrder(putOptionSymbol, putOptionQty);
		return putOptionResponse;

	}

}
