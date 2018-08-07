package my.trade.qrtz;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import my.trade.KeyCache;

public class OHLC15MinJob extends QuartzJobBean {

	@Autowired
	JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(OHLC15MinJob.class);

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

		// logger.info("running ohlc 15 min job" );
		if (KeyCache.containAccessToken() && !DataCache.getSubscriptions().isEmpty()) {

			insertOhlc15Min();
			insertichimoku15Min();
			// logger.info("ohlc 15 min job update size : " + batchUpdate.length);
		}
	}

	private void insertOhlc15Min() {
		String sql = "INSERT INTO ohlc15min (time, symbol, open,close, high, low)"
				+ " SELECT time_bucket('15 minutes', time) AS fifteen_min, ? as symbol,"
				+ " first(ltp, time) AS open, last(ltp, time) AS close, MAX(ltp) AS high, MIN(ltp) AS low"
				+ " FROM data WHERE time > NOW() - interval '15 minutes'   AND symbol = ?"
				+ " GROUP BY fifteen_min  ORDER BY fifteen_min DESC limit 1";

		executeOhlcSql(sql);
	}

	private void insertichimoku15Min() {
		String sql = "INSERT into ichimoku15min (time, symbol, tenkan, kijun, spana, spanb, chikou) "
				+ "select time, ? as symbol, tenkan, kijun, (tenkan+kijun)/2 as spanA, spanB, ? as chikou from ( "
				+ "select time, (tenkanHigh+tenkanLow)/2 as tenkan, " + "	(kijunHigh+kijunLow)/2 as kijun, "
				+ "	(spanBHigh+spanBLow)/2 as spanB " + " from ( " + "SELECT time, "
				+ "	max(high) OVER(ORDER BY time " + " ROWS BETWEEN 8 PRECEDING AND CURRENT ROW) as tenkanHigh, "
				+ "  min(low) OVER(ORDER BY time " + " ROWS BETWEEN 8 PRECEDING AND CURRENT ROW) as tenkanLow, "
				+ "	max(high) OVER(ORDER BY time " + " ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) as kijunHigh, "
				+ "  min(low) OVER(ORDER BY time " + " ROWS BETWEEN 25 PRECEDING AND CURRENT ROW) as kijunLow, "
				+ "	max(high) OVER(ORDER BY time " + " ROWS BETWEEN 51 PRECEDING AND CURRENT ROW) as spanBHigh, "
				+ "  min(low) OVER(ORDER BY time " + " ROWS BETWEEN 51 PRECEDING AND CURRENT ROW) as spanBLow "
				+ "	 FROM ohlc15min " + " WHERE  symbol= ? AND time > NOW() - interval '7 days' "
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
