package my.trade.qrtz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.StringUtils;

import my.trade.BankNiftyData;
import my.trade.BankNiftyOptionData;
import my.trade.ExitCache;
import my.trade.order.OrderService;

public class ExitJob extends QuartzJobBean {

	@Autowired
	JdbcTemplate jdbcTemplate;
	private static final Logger logger = LoggerFactory.getLogger(ExitJob.class);

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

		if (ExitCache.getCache().isExitSystemEnabled()) {
			validateAndPerformSellORExit();
		}
	}
	

	/**
	 * returns 1 for exit(buy) . returns 2 to sell based on amount , returns 0 - do
	 * not take any action
	 * 
	 * @param bankNiftyLtpPrice
	 * @return
	 */
	public static int determineAction(Number bankNiftyLtpPrice) {

		ExitCache ec = ExitCache.getCache();
		double upperLevel = ec.getBankNiftyUpperLimit();
		double lowerLevel = ec.getBankNiftyLowerLimit();
		if (bankNiftyLtpPrice != null) {

			if (!StringUtils.isEmpty(upperLevel) && bankNiftyLtpPrice.doubleValue() >= upperLevel) {
				logger.info("Upper Threshhold Reached- Must Exit :" + bankNiftyLtpPrice);
				return 1;
			}
			if (!StringUtils.isEmpty(lowerLevel) && bankNiftyLtpPrice.doubleValue() <= lowerLevel) {
				logger.info("Lower Threshhold Reached- Must Exit :" + bankNiftyLtpPrice);
				return 1;
			}

			double exitCallOption = ec.getCallOption();
			Double callOptionLtpPrice = DataCache.get(BankNiftyOptionData.getInstrumentName(exitCallOption, true))
					.doubleValue();
			if (callOptionLtpPrice >= ec.getCallOptionUpperLimit()) {
				logger.info("Call Option Upper  threshold Reached- Must Exit :");
				return 1;
			}

			double exitPutOption = ec.getPutOption();
			Double putOptionLtpPrice = DataCache.get(BankNiftyOptionData.getInstrumentName(exitPutOption, false))
					.doubleValue();
			if (putOptionLtpPrice >= ec.getPutOptionUpperLimit()) {
				logger.info("Put Option Upper  threshold Reached- Must Exit :");
				return 1;
			}

			if (callOptionLtpPrice != null && putOptionLtpPrice != null) {
				Double optionTotal = Double.sum(callOptionLtpPrice, putOptionLtpPrice);
				if (optionTotal >= ec.getOptionTotalUpperLimit()) {
					logger.info(" Option total  Upper  threshold Reached- Must Exit :");
					return 1;
				}

				if (optionTotal <= ec.getOptionTotalLowerLimit()) {
					logger.info(" Option total  Lower  threshold Reached- Must Exit :");
					return 1;
				}
				boolean sellIfTotalReachesFlag = ec.isSellIfTotalReachesFlag();
				if (sellIfTotalReachesFlag && optionTotal >= ec.getSellIfTotalReachesAmount()) {
					return 2;
				}
			}
		}
		return 0;
	}

	public void validateAndPerformSellORExit() {
		
		Number bankNiftyLtpPrice = DataCache.get(BankNiftyData.INSTR_SYMBOL);
		ExitCache ec = ExitCache.getCache();
		if (ec.isExitActionEnabled()) {
			int action = determineAction(bankNiftyLtpPrice);

			if (action == 1) {
				OrderService  os = new OrderService();
				logger.info("Exit Condition triggerd at :" + bankNiftyLtpPrice);

				String callInstrumentSymbol = BankNiftyOptionData.getInstrumentName(ec.getCallOption(), true);
				String putInstrumentSymbol = BankNiftyOptionData.getInstrumentName(ec.getPutOption(), false);
				os.placeBuyOrder(callInstrumentSymbol, ec.getCallOptionQty());
				os.placeBuyOrder(putInstrumentSymbol, ec.getPutOptionQty());
				ec.setExitActionEnabled(false);
				logger.info("Exited");
			}
			if (action == 2) {
				logger.info("Sell Condition triggerd at :" + bankNiftyLtpPrice);
				OrderService  os = new OrderService();
				String callInstrumentSymbol = BankNiftyOptionData.getInstrumentName(ec.getCallOption(), true);
				String putInstrumentSymbol = BankNiftyOptionData.getInstrumentName(ec.getPutOption(), false);
				if (!ec.isTestMode()) {
					os.placeSellOrder(callInstrumentSymbol, ec.getCallOptionQty());
					os.placeSellOrder(putInstrumentSymbol, ec.getPutOptionQty());
					ec.setSellIfTotalReachesFlag(false);
					logger.info("Sold");
				} else {
					logger.info("Running in Test Mode, Sell order will not be Placed");
				}
			}
		}
	}

}
