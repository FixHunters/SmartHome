package com.smartHome.flat.balcony.sensors;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.wiringpi.Spi;

/**
 * GPIO functions for Balcony Raspberry Pi Zero W
 *
 * @author Jan Pojezdala
 */
public class GpioBalcony {

	private static final Logger log = LoggerFactory.getLogger(GpioBalcony.class);

	Pin pinWaterPump = RaspiPin.GPIO_00; // RELAY GPIO_25, LED GPIO_05
	Pin pinWaterLevelSensor = RaspiPin.GPIO_26; // bolo GPIO_27
	Pin pinVccWaterLevelSensor = RaspiPin.GPIO_27;
	Pin pinSoilSensor = RaspiPin.GPIO_11;
	Pin pinVccSoilSensor = RaspiPin.GPIO_31;
	Pin pinVccADC = RaspiPin.GPIO_21; // bolo GPIO_03
	Pin pinVccPIR = RaspiPin.GPIO_28;
	Pin pinOutPIR = RaspiPin.GPIO_15;// bolo GPIO_15
	Pin pinVccBH1750 = RaspiPin.GPIO_06;
	Pin pinPowerBank = RaspiPin.GPIO_03;

	final byte TEMPERATURE_LSB = (byte) 0b10011010;
	final byte WRITE_MSB = (byte) 0b11000011;
	final byte WRITE_LSB = (byte) 0b10001010;
	final byte CONFIG = (byte) 0b00000000;
	final double adcConversion = 0.0002658626; // actual 1.2Mohm adc conversion
	final double temperatureConversion = 0.03125;

	private Boolean state;

	public enum DataPacket {
		DATA("Data"), CONFIG("Config"), TEMPERATURE("Temperature"), PERCENTAGE("Percentage");

		public final String label;

		private DataPacket(String label) {
			this.label = label;
		}
	}

	Session session;

	public double adcFunction() throws InterruptedException {

		// Create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// Enable Vcc pin
		GpioPinDigitalOutput inputPinOutputVccOut = gpio.provisionDigitalOutputPin(pinVccADC, PinState.HIGH);

		Thread.sleep(500);

		setupSpi();

		short packet[] = new short[4];

		// Setup binary data input for read adc0
		packet[0] = WRITE_MSB;
		packet[1] = WRITE_LSB;
		packet[2] = CONFIG;
		packet[3] = CONFIG;

		transferSpiData(packet);

		Thread.sleep(500);

		transferSpiData(packet);

		double finalResult = setDataConfig(packet, DataPacket.DATA);

		inputPinOutputVccOut.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(inputPinOutputVccOut);

		return finalResult;
	}

	/**
	 * Establish data conversion for specific packet
	 * 
	 * @param packet
	 * @param dataPacket
	 *            Apply specific data conversion
	 * @return double
	 */
	public double setDataConfig(short[] packet, DataPacket dataPacket) {
		double finalResult = 0;
		if (dataPacket.equals(DataPacket.DATA)) {
			double resultData = (short) (packet[0] * 256) + packet[1];
			finalResult = resultData * adcConversion;
		}
		if (dataPacket.equals(DataPacket.CONFIG)) {
			finalResult = packet[2] * 256 + packet[3];
		}

		if (dataPacket.equals(DataPacket.TEMPERATURE)) {
			double resultData = (short) (packet[0] * 256) + packet[1];
			finalResult = resultData * temperatureConversion;
			System.out.println("[RX-Data] Temperature Converted: " + finalResult);
		}
		if (dataPacket.equals(DataPacket.PERCENTAGE)) {
			double resultData = (short) (packet[0] * 256) + packet[1];
			double Vmax = 4.2;
			double Vmin = 2.9;
			double Vcur = resultData * adcConversion;
			finalResult = ((Vcur - Vmin) * 100) / (Vmax - Vmin);
			log.info("Battery Volage: {}V", Math.round(Vcur * 10000.0) / 10000.0);
			log.info("Battery Percentage: {}%", Math.round(finalResult * 100.0) / 100.0);
		}

		return finalResult;
	}

	/**
	 * Write/read transaction over the selected SPI bus
	 * 
	 * @param packet
	 *            32bit WRITE_MSB WRITE_LSB CONFIG CONFIG
	 * @return true
	 */
	public boolean transferSpiData(short[] packet) {
		int err = Spi.wiringPiSPIDataRW(0, packet, 4);
		if (err <= -1) {
			System.out.println(" ==>> SPI TRANSFER FAILED");
			return false;
		}
		return true;
	}

	/**
	 * Setup SPI for communication
	 * 
	 * @return true
	 */
	public boolean setupSpi() {
		int fd = Spi.wiringPiSPISetupMode(0, 500000, Spi.MODE_1);
		if (fd <= -1) {
			System.out.println(" ==>> SPI SETUP FAILED");
			return false;
		}
		return true;
	}

	public Boolean waterCheck() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// Enable Vcc pin
		GpioPinDigitalOutput inputPinOutputVccOut = gpio.provisionDigitalOutputPin(pinVccWaterLevelSensor, PinState.HIGH);
		log.info("<GpioBalcony> Enable Vcc pin for WaterCheck senzor, Pin: " + pinVccWaterLevelSensor + " State: "
				+ inputPinOutputVccOut.getState().toString());

		// provision gpio pin as an input pin with its internal pull down
		GpioPinDigitalInput inputPin = gpio.provisionDigitalInputPin(pinWaterLevelSensor, PinPullResistance.PULL_DOWN);
		log.info("<GpioBalcony> GPIO check state WaterCheck sensor, Pin: " + pinWaterLevelSensor + " State: " + inputPin.getState().toString());
		inputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		inputPinOutputVccOut.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(inputPin);
		gpio.unprovisionPin(inputPinOutputVccOut);
		if (inputPin.getState().isHigh())
			return true;
		else
			return false;

	}

	public Boolean soilHumidityCheck() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// Enable Vcc pin
		GpioPinDigitalOutput inputPinOutputVccOut = gpio.provisionDigitalOutputPin(pinVccSoilSensor, PinState.HIGH);
		log.info("<GpioBalcony> Enable Vcc pin for SoilSensor senzor, Pin: " + pinVccSoilSensor + " State: "
				+ inputPinOutputVccOut.getState().toString());

		// provision gpio pin as an input pin with its internal pull down
		// resistor enabled
		GpioPinDigitalInput inputPin = gpio.provisionDigitalInputPin(pinSoilSensor, PinPullResistance.PULL_DOWN);
		log.info("<GpioBalcony> GPIO check state SoilSensor sensor, Pin: " + pinSoilSensor + " State: " + inputPin.getState().toString());
		inputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		inputPinOutputVccOut.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(inputPin);
		gpio.unprovisionPin(inputPinOutputVccOut);
		if (inputPin.getState().isHigh())
			return true;
		else
			return false;

	}

	public PinState waterPumpCheck() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin as an input pin with its internal pull down
		GpioPinDigitalInput inputPin = gpio.provisionDigitalInputPin(pinWaterPump);
		log.info("<GpioBalcony> GPIO check state checkWaterPump, Pin: " + pinWaterPump + " State: " + inputPin.getState().toString());
		PinState pinState = inputPin.getState();
		inputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(inputPin);
		return pinState;
	}

	public void waterPumpStart() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin as an output pin with PinState HIGH
		GpioPinDigitalOutput outputPin = gpio.provisionDigitalOutputPin(pinWaterPump, PinState.HIGH);
		log.info("<GpioBalcony> GPIO StartWaterPump, Pin: " + pinWaterPump + " State: " + outputPin.getState().toString());

		outputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(outputPin);
	}

	public void waterPumpStop() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin as an output pin with PinState LOW
		GpioPinDigitalOutput outputPin = gpio.provisionDigitalOutputPin(pinWaterPump, PinState.LOW);
		log.info("<GpioBalcony> GPIO StopWaterPump, Pin: " + pinWaterPump + " State: " + outputPin.getState().toString());

		outputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(outputPin);
	}

	public void waterPumpAutomat(int workTime) throws InterruptedException {
		GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalInput inputPinWaterCheck = gpio.provisionDigitalInputPin(pinSoilSensor, PinPullResistance.PULL_DOWN);
		GpioPinDigitalOutput outputPinWaterPump = gpio.provisionDigitalOutputPin(pinWaterPump);

		String lineSeparator = System.getProperty("line.separator");
		log.info("<GpioBalcony> GPIO WaterPumpAutomat, Pin: " + pinWaterPump + " State: " + outputPinWaterPump.getState().toString() + lineSeparator
				+ "PinWaterCheck, Pin: " + pinSoilSensor + " State: " + inputPinWaterCheck.getState().toString());

		automatWaterPumpListener(inputPinWaterCheck, outputPinWaterPump, workTime);

		outputPinWaterPump.setState(PinState.LOW);
		inputPinWaterCheck.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		outputPinWaterPump.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(inputPinWaterCheck);
		gpio.unprovisionPin(outputPinWaterPump);
	}

	public void automatWaterPumpListener(GpioPinDigitalInput inputPin, GpioPinDigitalOutput outputPin, int workTime) throws InterruptedException {

		log.info("<GpioBalcony> I turn on the water pump started as long as the soilSensor registers water on pin: " + inputPin
				+ "or does not set the watering time: " + workTime + "s");
		do {

			if (inputPin.getState().equals(PinState.HIGH))
				outputPin.setState(PinState.HIGH);
			Thread.sleep(1000);
			workTime--;
		} while (inputPin.getState().equals(PinState.HIGH) && workTime > 0);
		log.info("<GpioBalcony> I turn on the water pump finished");
	}

	public Boolean checkMotionSensorPIR() throws InterruptedException {

		// create gpio controller
		final GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalInput inputPIRCheck = gpio.provisionDigitalInputPin(pinOutPIR, PinPullResistance.PULL_DOWN);

		if (inputPIRCheck.getState().equals(PinState.HIGH)) {
			log.info("<GpioBalcony> Motion detected!");
			state = true;
			// Execute camera script, if motion check was true
			executePython("camera");
		} else {
			state = false;
		}
		log.info(" Status: " + state.toString());
		inputPIRCheck.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(inputPIRCheck);
		return state;

		/*
		 * PrintTask printTask = new PrintTask("task"); printTask.run(); return
		 * true;
		 */
	}

	public void executePython(String mode) {
		String createPath = "/home/pi/Project/" + mode + ".py";
		ProcessBuilder pb = new ProcessBuilder("sudo", "python", createPath);
		pb.redirectErrorStream(true);
		Process proc = null;
		String out = "";
		try {
			proc = pb.start();
			Reader reader = new InputStreamReader(proc.getInputStream());
			int ch;
			while ((ch = reader.read()) != -1) {
				out = out + String.valueOf((char) ch);
			}
			log.info("<Python script {}> {}", mode, out);

			reader.close();
			out = "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendEmail();
		

		// Second implementation way
		/*
		 * try { Process p = Runtime.getRuntime().
		 * exec("sudo python /home/pi/Project/hello-world.py"); // get an
		 * InputStream for the python stdout InputStream inStream =
		 * p.getInputStream(); int ch; // read all stdout data one char at a
		 * time. while ((ch = inStream.read()) != -1) {
		 * System.out.print((char)ch); } } catch (IOException e) { }
		 */
	}

	/**
	 * method for check light BH1750
	 *
	 * 
	 * @return BigDecimal
	 * @throws UnsupportedBusNumberException
	 * @throws IOException
	 */
	public BigDecimal getLight() throws UnsupportedBusNumberException, IOException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();
		BigDecimal value = null;

		// Enable Vcc pin
		GpioPinDigitalOutput pinVcc = gpio.provisionDigitalOutputPin(pinVccBH1750, PinState.HIGH);
		log.debug("<GpioBalcony> Enable Vcc pin for BH1750 Light senzor, Pin: " + pinVccBH1750 + " State: " + pinVcc.getState().toString());
		I2CBus bus;
		bus = I2CFactory.getInstance(I2CBus.BUS_3);
		BH1750 bh1750 = new BH1750(bus);
		
		bh1750.init();
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		value = bh1750.read();
		log.info("Light sensor lx: " + value.toString());
		
		pinVcc.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(pinVcc);
		return value;
	}

	/**
	 * Utility method to send image in email body
	 * 
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 */
	public static void sendImageEmail(Session session, String toEmail, String subject, String body) {
		try {
			MimeMessage msg = new MimeMessage(session);
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"));

			msg.setReplyTo(InternetAddress.parse("sleepo1111@gmail.com", false));

			msg.setSubject(subject, "UTF-8");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

			// Create the message body part
			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText(body);

			// Create a multipart message for attachment
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Second part is image attachment
			messageBodyPart = new MimeBodyPart();
			String filename = "image.png";
			String image = "/home/pi/Desktop/image.jpg";
			DataSource source = new FileDataSource(image);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(filename);
			// Trick is to add the content-id header here
			messageBodyPart.setHeader("Content-ID", "image_id");
			multipart.addBodyPart(messageBodyPart);

			// third part for displaying image in the email body
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent("<h1>Attached Image</h1>" + "<img src='cid:image_id'>", "text/html");
			multipart.addBodyPart(messageBodyPart);

			// Set the multipart message to the email message
			msg.setContent(multipart);

			// Send message
			Transport.send(msg);
			System.out.println("EMail Sent Successfully with image!!");
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void enablePW() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin as an output pin with PinState HIGH
		GpioPinDigitalOutput outputPin = gpio.provisionDigitalOutputPin(pinPowerBank, PinState.HIGH);
		log.info("<GpioBalcony> GPIO pinPowerBank, Pin: " + pinPowerBank + " State: " + outputPin.getState().toString());

		outputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(outputPin);
	}

	public void disablePW() throws InterruptedException {
		// create gpio controller
		GpioController gpio = GpioFactory.getInstance();

		// provision gpio pin as an output pin with PinState LOW
		GpioPinDigitalOutput outputPin = gpio.provisionDigitalOutputPin(pinPowerBank, PinState.LOW);
		log.info("<GpioBalcony> GPIO pinPowerBank, Pin: " + pinPowerBank + " State: " + outputPin.getState().toString());

		outputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
		gpio.shutdown();
		gpio.unprovisionPin(outputPin);
	}
	
	public void sendEmail(){
		      // Recipient's email ID needs to be mentioned.
		      String to = "sleepo1111@gmail.com";

		      // Sender's email ID needs to be mentioned
		      String from = "balconysender@gmail.com";

		      final String username = "SmartHome123456";//change accordingly
		      final String password = "SmartHome123456";//change accordingly

		      // Assuming you are sending email through relay.jangosmtp.net
		      String host = "smtp.gmail.com";
		     // String host = "balconysender@gmail.com";
		      
		      Properties props = new Properties();
		      props.put("mail.smtp.auth", "true");
		      props.put("mail.smtp.starttls.enable", "true");
		      props.put("mail.smtp.host", host);
		      props.put("mail.smtp.port", "587");
		      props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		     // props.put("mail.username", "1990");
		     // props.put("mail.password", "1990");

		      // Get the Session object.
		      Session session = Session.getInstance(props,
		         new javax.mail.Authenticator() {
		            protected PasswordAuthentication getPasswordAuthentication() {
		               return new PasswordAuthentication(username, password);
		            }
		         });

		      try {
		         // Create a default MimeMessage object.
		         Message message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.setRecipients(Message.RecipientType.TO,
		            InternetAddress.parse(to));

		         // Set Subject: header field
		         message.setSubject("Testing Subject");

		         // Create the message part
		         BodyPart messageBodyPart = new MimeBodyPart();

		         // Now set the actual message
		         messageBodyPart.setText("This is message body");

		         // Create a multipar message
		         Multipart multipart = new MimeMultipart();

		         // Set text message part
		         multipart.addBodyPart(messageBodyPart);

		         // Part two is attachment
		         messageBodyPart = new MimeBodyPart();
		         String filename = "/home/pi/Desktop/image.jpg";
		         DataSource source = new FileDataSource(filename);
		         messageBodyPart.setDataHandler(new DataHandler(source));
		         messageBodyPart.setFileName(filename);
		         multipart.addBodyPart(messageBodyPart);

		         // Send the complete message parts
		         message.setContent(multipart);

		         // Send message
		         Transport.send(message);

		         System.out.println("Sent message successfully....");
		  
		      } catch (MessagingException e) {
		         throw new RuntimeException(e);
		      }
		   }
	

}
