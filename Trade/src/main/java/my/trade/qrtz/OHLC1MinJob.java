package my.trade.qrtz;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;

import my.trade.IchimokuCache;
import my.trade.IchimokuData;
import my.trade.IchimokuDataMapper;
import my.trade.IchimokuIndicator;
import my.trade.KeyCache;
import my.trade.OHLCData;

public class OHLC1MinJob extends QuartzJobBean {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private static final Logger logger = LoggerFactory.getLogger(OHLC1MinJob.class);

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

		// logger.info("running ohlc 1 min job" );
		if (KeyCache.containAccessToken() && !DataCache.getSubscriptions().isEmpty()) {

			insertOhlc1Min();
			// logger.info("ohlc 1 min job update size : " + batchUpdate.length);
			insertichimoku1Min();

			/*List<IchimokuData> loadIchimokuDataList = loadIchimokuData();
			for (IchimokuData ichimokuData : loadIchimokuDataList) {
				populateIchimokuCache(ichimokuData);
			}*/
		}
	}

	private void populateIchimokuCache(IchimokuData ichimokuData) {
		String symbol = ichimokuData.getSymbol();
		IchimokuIndicator indicator = null;
		if (IchimokuCache.contains1Min(symbol)) {
			indicator = IchimokuCache.get1Min(symbol);
			ichimokuData.checkForCrossOver(indicator);
			if (indicator.isCheckForAlerts()) {
				int checkAlerts = indicator.checkAlerts();
				logger.info("indicator" + indicator); 
				if(checkAlerts ==1) logger.info("Buy signal generated for above");
				if(checkAlerts == -1) logger.info("Sell signal generated for above");
			}
		} else {
			indicator = new IchimokuIndicator();
			indicator.setSymbol(symbol);
			IchimokuCache.add1Min(symbol, indicator);
			ichimokuData.checkForCrossOver(indicator);
			int checkAlerts = indicator.checkAlerts();
			
			logger.info("indicator" + indicator);
			if(checkAlerts ==1) logger.info("Buy signal generated for above at init");
			if(checkAlerts == -1) logger.info("Sell signal generated for above at Init");
		}
	}

	private List<IchimokuData> loadIchimokuData() {
		String sql = "select ichimoku1min.*, ohlc1min.open, ohlc1min.high, ohlc1min.low, ohlc1min.close from ichimoku1min, ohlc1min "
				+ " where ichimoku1min.symbol in (:symbol) AND ohlc1min.symbol = ichimoku1min.symbol AND ohlc1min.time = ichimoku1min.time  "
				+ " order by time desc limit :size ";

		String cloudSQL = "select * from ichimoku1min  where symbol  = :symbol  order by time desc limit 1  offset 25";

		String chikouSQL = "select * from ichimoku1min  where symbol  = :symbol  order by time desc limit 1  offset 52";

		MapSqlParameterSource source = new MapSqlParameterSource().addValue("symbol", DataCache.getSubscriptions())
				.addValue("size", DataCache.getSubscriptions().size());
		;

		List<IchimokuData> ichimokuDataList = namedParameterJdbcTemplate.query(sql, source,
				new RowMapper<IchimokuData>() {

					@Override
					public IchimokuData mapRow(ResultSet rs, int rowNum) throws SQLException {
						IchimokuData ichimokuData = new IchimokuData();
						ichimokuData.setTenkan(rs.getDouble("tenkan"));
						ichimokuData.setKijun(rs.getDouble("kijun"));
						ichimokuData.setSpanA(rs.getDouble("spana"));
						ichimokuData.setSpanB(rs.getDouble("spanb"));
						ichimokuData.setChikou(rs.getDouble("chikou"));
						ichimokuData.setSymbol(rs.getString("symbol"));
						OHLCData ohlcData = new OHLCData();
						ohlcData.setSymbol(rs.getString("symbol"));
						ohlcData.setOpen(rs.getDouble("open"));
						ohlcData.setHigh(rs.getDouble("high"));
						ohlcData.setLow(rs.getDouble("low"));
						ohlcData.setClose(rs.getDouble("close"));
						ichimokuData.setOhlcData(ohlcData);

						MapSqlParameterSource source = new MapSqlParameterSource().addValue("symbol",
								ichimokuData.getSymbol());
						List<IchimokuData> cloudList = namedParameterJdbcTemplate.query(cloudSQL, source,
								new IchimokuDataMapper());
						if (cloudList.isEmpty() == false)
							ichimokuData.setCloudData(cloudList.get(0));

						List<IchimokuData> chikouList = namedParameterJdbcTemplate.query(chikouSQL, source,
								new IchimokuDataMapper());
						if (cloudList.isEmpty() == false)
							ichimokuData.setChikouData(chikouList.get(0));

						return ichimokuData;
					}
				});
		return ichimokuDataList;

	}

	private void insertOhlc1Min() {
		String sql = "INSERT INTO ohlc1min (time, symbol, open,close, high, low)"
				+ " SELECT time_bucket('1 minutes', time) AS one_min, ? as symbol,"
				+ " first(ltp, time) AS open, last(ltp, time) AS close, MAX(ltp) AS high, MIN(ltp) AS low"
				+ " FROM data WHERE time > NOW() - interval '1 minutes'   AND symbol = ?"
				+ " GROUP BY one_min  ORDER BY one_min DESC limit 1";

		executeOhlcSql(sql);
	}

	private void insertichimoku1Min() {
		String sql = "INSERT into ichimoku1min (time, symbol, tenkan, kijun, spana, spanb, chikou) "
				+ "select time, ? as symbol, tenkan, kijun, (tenkan+kijun)/2 as spanA, spanB, ? as chikou from ( "
				+ "select time, (tenkanHigh+tenkanLow)/2 as tenkan, " + "	(kijunHigh+kijunLow)/2 as kijun, "
				+ "	(spanBHigh+spanBLow)/2 as spanB " + " from ( " + "SELECT time, "
				+ "	max(high) OVER(ORDER BY time " + " ROWS BETWEEN 8 PRECEDING AND CURRENT ROW) as tenkanHigh, "
				+ "  min(low) OVER(ORDER BY time " + " ROWS BETWEEN 8 PRECEDING AND CURRENT ROW) as tenkanLow, "
				+ "	max(high) OVER(ORDER BY time " + " ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) as kijunHigh, "
				+ "  min(low) OVER(ORDER BY time " + " ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) as kijunLow, "
				+ "	max(high) OVER(ORDER BY time " + " ROWS BETWEEN 51 PRECEDING AND CURRENT ROW) as spanBHigh, "
				+ "  min(low) OVER(ORDER BY time " + " ROWS BETWEEN 51 PRECEDING AND CURRENT ROW) as spanBLow "
				+ "	 FROM ohlc1min " + " WHERE  symbol= ? AND time > NOW() - interval '7 days' "
				+ " ORDER BY time DESC limit 1 ) as innerqr " + " ) as innerQry1";

		executeIchimokuSql(sql);
	}

	private void executeIchimokuSql(String sql) {
		ConcurrentHashMap<String, Number> cachedData = DataCache.getCachedData();

		if (!cachedData.isEmpty()) {
			List<Object[]> inputList = new ArrayList<Object[]>();
			KeySetView<String, Number> keySet = cachedData.keySet();
			for (String symbol : keySet) {
				Number ltp = DataCache.get(symbol);
				List<Object> tmpList = new ArrayList<>();
				tmpList.add(symbol);
				tmpList.add(ltp.doubleValue());
				tmpList.add(symbol);
				Object[] array = tmpList.toArray();
				inputList.add(array);
			}

			int[] batchUpdate = jdbcTemplate.batchUpdate(sql, inputList);
		}
	}

	private void executeOhlcSql(String sql) {
		ConcurrentHashMap<String, Number> cachedData = DataCache.getCachedData();

		if (!cachedData.isEmpty()) {
			List<Object[]> inputList = new ArrayList<Object[]>();
			KeySetView<String, Number> keySet = cachedData.keySet();
			for (String symbol : keySet) {
				if (StringUtils.startsWithIgnoreCase(symbol, "NFO:BANKNIFTY") == false) {
					List<Object> tmpList = new ArrayList<>();
					tmpList.add(symbol);
					tmpList.add(symbol);
					Object[] array = tmpList.toArray();
					inputList.add(array);
				}
			}

			int[] batchUpdate = jdbcTemplate.batchUpdate(sql, inputList);
		}
	}
}
