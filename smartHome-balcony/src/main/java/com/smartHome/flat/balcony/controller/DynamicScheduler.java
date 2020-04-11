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
    
	public String setCron(String croneEx, Boolean enabled) {
		if (isSet == false) {
			log.info("Crone job are execute in time -> {}", croneEx);
			balconyService.getData();
		}
		return croneEx;
	}

    public void scheduleFixed(int frequency) {
        log.info("scheduleFixed: Next execution time of this will always be {} seconds", frequency);
    }

    // Only reason this method gets the cron as parameter is for debug purposes.
    public void scheduleCron(String cron) {
        log.info("scheduleCron: Next execution time of this taken from cron expression -> {}", cron);
    }
    
	public void setActivate(String croneEx) {
		if (future != null)
			future.cancel(true);
		futureMap.remove(future);
		isSetConfig(true);
		log.info("Next crone execution crone time -> {}", croneEx);
		CronTrigger croneTrigger = new CronTrigger(setCron(croneEx, isSet), TimeZone.getDefault());
		future = scheduledTaskRegistrar.getScheduler().schedule(() -> setCron(croneEx, isSet), croneTrigger);
		isSetConfig(false);
		activateFuture(future);
	}

    /**
     * @param mayInterruptIfRunning {@code true} if the thread executing this task
     * should be interrupted; otherwise, in-progress tasks are allowed to complete
     */
    public void cancelFuture(boolean mayInterruptIfRunning, ScheduledFuture future) {
        log.info("Cancelling a future");
        future.cancel(mayInterruptIfRunning); // set to false if you want the running task to be completed first.
        futureMap.put(future, false);
    }

    public void activateFuture(ScheduledFuture future) {
        log.info("Re-Activating a future");
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