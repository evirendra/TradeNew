package my.trade;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import my.trade.qrtz.DataCache;

@RestController
public class VirtualTradeController {

	private static final Logger logger = LoggerFactory.getLogger(VirtualTradeController.class);
	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping("/api/virtualTrade/add")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public List<VirtualTrade> addVirtualTrade(@RequestParam(value = "symbol") String symbol, @RequestParam(value = "qty") int qty,
			@RequestParam(value = "transactionType") String transactionType) throws Exception {

		String symbolU = symbol.toUpperCase();
		DataCache.subscribe(symbolU);
		Thread.sleep(1500);
		logger.info("Symbol :" + symbol);
		logger.info("qty :" + qty);
		StringBuilder builder = createInsertSQL(transactionType);

		String sql = builder.toString();
		List<Object> tmpList = new ArrayList<>();
		tmpList.add(symbolU);
		tmpList.add(qty);
		while (!DataCache.contains(symbolU))
			;
		tmpList.add(DataCache.get(symbolU));
		Object[] array = tmpList.toArray();
		List<Object[]> inputList = new ArrayList<Object[]>();
		inputList.add(array);

		int[] batchUpdate = jdbcTemplate.batchUpdate(sql, inputList);

		return getAllVirtualTradesFromDB();
	}

	@RequestMapping("/api/virtualTrade/getAll")
	@CrossOrigin(origins = { "http://localhost:4200" })
	public List<VirtualTrade> getAllVirtualTrades() throws Exception {

		return getAllVirtualTradesFromDB();
	}

	private List<VirtualTrade> getAllVirtualTradesFromDB() {
		String sql = "select * from virtualtrade order by createdtime desc";

		List<VirtualTrade> virtualTrades = jdbcTemplate.query(sql, new RowMapper<VirtualTrade>() {

			@Override
			public VirtualTrade mapRow(ResultSet rs, int rowNum) throws SQLException {
				VirtualTrade virtualTrade = new VirtualTrade();
				virtualTrade.setSymbol(rs.getString("symbol"));
				DataCache.subscribe(virtualTrade.getSymbol());
				virtualTrade.setCreatedTime(rs.getTimestamp("createdtime"));
				virtualTrade.setClosedTime(rs.getTimestamp("closedtime"));
				virtualTrade.setDisplay(rs.getBoolean("display"));
				virtualTrade.setId(rs.getInt("id"));
				virtualTrade.setQty(rs.getInt("qty"));
				virtualTrade.setPurchasedPrice(rs.getDouble("purchasedprice"));
				virtualTrade.setSoldPrice(rs.getDouble("soldprice"));
				virtualTrade.settType(rs.getInt("transactiontype"));
				Number ltp = DataCache.get(virtualTrade.getSymbol());
				if (ltp != null) {
					virtualTrade.setLtp(ltp.doubleValue());
				}
				return virtualTrade;
			}
		});
		return virtualTrades;
	}

	private StringBuilder createInsertSQL(String transactionType) throws Exception {
		StringBuilder builder = new StringBuilder("INSERT INTO virtualtrade (createdtime, symbol, qty,");
		int transactionCode=0;
		if (transactionType != null) {
			if (transactionType.equalsIgnoreCase("Sell")) {
				builder.append("soldPrice");
				transactionCode = 2;
			}
			if (transactionType.equalsIgnoreCase("Buy")) {
				builder.append("purchasedPrice");
				transactionCode = 1;
			}
		} else {
			throw new Exception("TransactionType is missing");
		}

		builder.append(" ,transactiontype) values (NOW(), ?, ?, ?, " + transactionCode +  ")");
		return builder;
	}

}
