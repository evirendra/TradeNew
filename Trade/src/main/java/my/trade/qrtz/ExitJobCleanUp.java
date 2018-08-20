package my.trade.qrtz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import my.trade.ExitCache;

public class ExitJobCleanUp extends QuartzJobBean {

	private static final Logger logger = LoggerFactory.getLogger(ExitJobCleanUp.class);

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

		logger.info("running ExitClean Up  job");
		ExitCache.resetCache();
	}

}
