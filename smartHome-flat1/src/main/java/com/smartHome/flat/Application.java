package com.smartHome.flat;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableScheduling
public class Application {
	
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) throws Exception {
		context = SpringApplication.run(Application.class, args);
}
	
	public static void restart() {
		ApplicationArguments args = context.getBean(ApplicationArguments.class);

		Thread thread = new Thread(() -> {
			logger.debug("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
			logger.debug("NonOptionArgs: {}", args.getNonOptionArgs());
			logger.debug("OptionNames: {}", args.getOptionNames());
			for (String name : args.getOptionNames()) {
				logger.debug("arg-" + name + "=" + args.getOptionValues(name));
			}

			context.close();
			context = SpringApplication.run(Application.class, args.getSourceArgs());
		});

		thread.setDaemon(false);
		thread.start();
	}

}
