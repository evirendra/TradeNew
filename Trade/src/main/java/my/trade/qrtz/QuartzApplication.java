package my.trade.qrtz;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuartzApplication {

/*	public static void main(String[] args) {

		SpringApplication.run(QuartzApplication.class, args);
	}*/

/*	@Bean(name = { "dataJob" })
	public JobDetail dataJobDetail() {
		return JobBuilder.newJob(DataJob.class).withIdentity("dataJob").storeDurably().build();
	}

	@Bean(name = { "dataTrigger" })
	public Trigger dataJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(1500)
				.repeatForever();

		// CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule( "0/1 6
		// 22 * * ?");

		GregorianCalendar startCalendar = getStartCalendar(9,15,0);
		GregorianCalendar endCalendar = getEndCalendar(15,30,0);

		SimpleTrigger build = null;
		build = TriggerBuilder.newTrigger().forJob(dataJobDetail()).withIdentity("dataTrigger")
				 .startAt(startCalendar.getTime())
				 .endAt(endCalendar.getTime())
				.withSchedule(scheduleBuilder).build();
		return build;
	}*/
	
	@Bean(name = { "positionJob" })
	public JobDetail positionJobDetail() {
		return JobBuilder.newJob(PositionJob.class).withIdentity("positionJob").storeDurably().build();
	}

	@Bean(name = { "positionTrigger" })
	public Trigger positionJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(1500)
				.repeatForever();

		// CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule( "0/1 6
		// 22 * * ?");

		GregorianCalendar startCalendar = getStartCalendar(9,15,0);
		GregorianCalendar endCalendar = getEndCalendar(15,30,0);

		SimpleTrigger build = null;
		build = TriggerBuilder.newTrigger().forJob(positionJobDetail()).withIdentity("positionTrigger")
				 .startAt(startCalendar.getTime())
				 .endAt(endCalendar.getTime())
				.withSchedule(scheduleBuilder).build();
		return build;
	}
	

	private GregorianCalendar getEndCalendar(int hourOfDay, int minute, int second) {
		GregorianCalendar endCalendar = new GregorianCalendar();
		endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		endCalendar.set(Calendar.MINUTE, minute);
		endCalendar.set(Calendar.SECOND, second);
		return endCalendar;
	}

	private GregorianCalendar getStartCalendar(int hourOfDay, int minute, int second) {
		GregorianCalendar startCalendar = new GregorianCalendar();
		startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		startCalendar.set(Calendar.MINUTE, minute);
		startCalendar.set(Calendar.SECOND, second);
		return startCalendar;
	}

/*	@Bean(name = { "OHLC1MINJOB" })
	public JobDetail ohlc1MinJobDetail() {
		return JobBuilder.newJob(OHLC1MinJob.class).withIdentity("ohlc1MinJob").storeDurably().build();
	}

	@Bean(name = { "OHLC5MINJOB" })
	public JobDetail ohlc5MinJobDetail() {
		return JobBuilder.newJob(OHLC5MinJob.class).withIdentity("ohlc5MinJob").storeDurably().build();
	}

	@Bean(name = { "OHLC15MINJOB" })
	public JobDetail ohlc15MinJobDetail() {
		return JobBuilder.newJob(OHLC15MinJob.class).withIdentity("ohlc15MinJob").storeDurably().build();
	}

	@Bean(name = { "OHLC1minTrigger" })
	public Trigger ohlc1MinJobJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1)
				.repeatForever();

		GregorianCalendar startCalendar = getStartCalendar(9,16,0);
		GregorianCalendar endCalendar = getEndCalendar(15,30,5);

		SimpleTrigger build = null;
		build = TriggerBuilder.newTrigger().forJob(ohlc1MinJobDetail()).withIdentity("ohlc1minJobTrigger")
				 .startAt(startCalendar.getTime())
				 .endAt(endCalendar.getTime())
				.withSchedule(scheduleBuilder).build();
		return build;
	}
	
	@Bean(name = { "OHLC5minTrigger" })
	public Trigger ohlc5MinJobJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(5)
				.repeatForever();


		GregorianCalendar startCalendar = getStartCalendar(9,25,0);
		GregorianCalendar endCalendar = getEndCalendar(15,30,5);

		SimpleTrigger build = null;
		build = TriggerBuilder.newTrigger().forJob(ohlc5MinJobDetail()).withIdentity("ohlc5minJobTrigger")
				 .startAt(startCalendar.getTime())
				 .endAt(endCalendar.getTime())
				.withSchedule(scheduleBuilder).build();
		return build;
	}
	
	@Bean(name = { "OHLC15minTrigger" })
	public Trigger ohlc15MinJobJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(15)
				.repeatForever();


		GregorianCalendar startCalendar = getStartCalendar(9,30,0);
		GregorianCalendar endCalendar = getEndCalendar(15,30,5);

		SimpleTrigger build = null;
		build = TriggerBuilder.newTrigger().forJob(ohlc15MinJobDetail()).withIdentity("ohlc15minJobTrigger")
				 .startAt(startCalendar.getTime())
				 .endAt(endCalendar.getTime())
				.withSchedule(scheduleBuilder).build();
		return build;
	}
	
	
	@Bean(name = { "ExitJOB" })
	public JobDetail exitJobDetail() {
		return JobBuilder.newJob(ExitJob.class).withIdentity("exitJob").storeDurably().build();
	}

	@Bean(name = { "ExitTrigger" })
	public Trigger exitJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(1500)
				.repeatForever();

		GregorianCalendar startCalendar = getStartCalendar(9,16,0);
		GregorianCalendar endCalendar = getEndCalendar(15,30,5);

		SimpleTrigger build = null;
		build = TriggerBuilder.newTrigger().forJob(exitJobDetail()).withIdentity("exitJobTrigger")
				 .startAt(startCalendar.getTime())
				 .endAt(endCalendar.getTime())
				.withSchedule(scheduleBuilder).build();
		return build;
	}
	
	@Bean(name = { "ExitCleanUpJOB" })
	public JobDetail exitJobCleanUp() {
		return JobBuilder.newJob(ExitJobCleanUp.class).withIdentity("exitJobCleanUp").storeDurably().build();
	}

	@Bean(name = { "ExitTriggerCleanUp" })
	public Trigger exitJobTriggerCleanUp() {
		GregorianCalendar startCalendar = getStartCalendar(15,32,0);
		
		Trigger build = TriggerBuilder.newTrigger().forJob(exitJobCleanUp()).withIdentity("exitJobCleanUpTrigger")
				 .startAt(startCalendar.getTime()). build();
		return build;
	}*/

}
