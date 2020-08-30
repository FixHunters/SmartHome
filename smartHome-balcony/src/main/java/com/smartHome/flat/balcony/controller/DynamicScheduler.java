package com.smartHome.flat.balcony.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.smartHome.flat.balcony.service.BalconyService;

/**
 * @author JPojezdala
 *
 * More resource
 * http://mbcoder.com/dynamic-task-scheduling-with-spring/
 */
@Service
public class DynamicScheduler implements SchedulingConfigurer {

    private static Logger log = LoggerFactory.getLogger(DynamicScheduler.class);
    
	@Autowired
	private BalconyService balconyService;

    ScheduledTaskRegistrar scheduledTaskRegistrar;
    ScheduledFuture future;
    Map<ScheduledFuture, Boolean> futureMap = new HashMap<>();
    
    Boolean isSet = true;

    @Bean
    public TaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        scheduler.setPoolSize(1);
        scheduler.initialize();
        return scheduler;
    }

    // We can have multiple tasks inside the same registrar as we can see below.
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (scheduledTaskRegistrar == null) {
            scheduledTaskRegistrar = taskRegistrar;
        }
        if (taskRegistrar.getScheduler() == null) {
            taskRegistrar.setScheduler(poolScheduler());
        }
    }
    
	/**
	 * 
	 * Nastavenie funkcii ktore chcem v crone jobe spustat.
	 * 
	 * @param croneEx
	 * @param enabled
	 * @param cycleTime
	 * @return
	 */
	public String setCron(String croneEx, Boolean enabled, String cycleTime) {
		if (isSet == false) {
			balconyService.getData();
			balconyService.checkWater();
			balconyService.checkSoil();
			balconyService.setAutomateWatering(cycleTime, true, null);
			//balconyService.runPython("camera");
			log.info("<setCron> Actual crone job are execute in time : {} and water cycle time : {}", croneEx, cycleTime);
		}
		return croneEx;
	}

    public void scheduleFixed(int frequency) {
        log.info("<scheduleFixed> Next execution time of this will always be {} seconds", frequency);
    }

    // Only reason this method gets the cron as parameter is for debug purposes.
    public void scheduleCron(String cron) {
        log.info("<scheduleCron> Next execution time of this taken from cron expression -> {}", cron);
    }
    
    
	/**
	 * Tuna sa nastavuje cron job a dlzka zalievania.
	 * 
	 * @param croneEx
	 * @param cycleTime
	 */
	public void setActivate(String croneEx, String cycleTime) {
		if (future != null)
			future.cancel(true);
		futureMap.remove(future);
		isSetConfig(true);
		log.info("<setActivate> Next crone execution crone time -> {}, watering time: {}s", croneEx, cycleTime);
		CronTrigger croneTrigger = new CronTrigger(setCron(croneEx, isSet, cycleTime), TimeZone.getDefault());
		future = scheduledTaskRegistrar.getScheduler().schedule(() -> setCron(croneEx, isSet, cycleTime), croneTrigger);
		isSetConfig(false);
		activateFuture(future);
	}

    /**
     * @param mayInterruptIfRunning {@code true} if the thread executing this task
     * should be interrupted; otherwise, in-progress tasks are allowed to complete
     */
    public void cancelFuture(boolean mayInterruptIfRunning, ScheduledFuture future) {
        log.info("<cancelFuture> Cancelling a future");
        future.cancel(mayInterruptIfRunning); // set to false if you want the running task to be completed first.
        futureMap.put(future, false);
    }

    public void activateFuture(ScheduledFuture future) {
        log.info("<activateFuture> Re-Activating a future");
        futureMap.put(future, true);
        configureTasks(scheduledTaskRegistrar);
    }

    public void cancelAll() {
        cancelFuture(true, future);
    }

    public void activateAll() {
        activateFuture(future);
    }
    
    public boolean isSetConfig(Boolean isSetConfig){
    	isSet =  isSetConfig;
		return isSet; 	
    }

}