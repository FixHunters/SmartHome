package com.smartHome.flat.radio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartHome.flat.radio.model.Station;
import com.smartHome.flat.radio.rda5807.RDA5807M;
import com.smartHome.flat.radio.rda5807.Register;
import com.smartHome.flat.radio.rda5807.RegisterMasks;
import com.smartHome.flat.radio.rda5807.StatusResult;
import com.smartHome.flat.radio.rda5807.util.Util;

/**
 *
 * @author Jan Pojezdala
 */
public class RadioApplication {
	private static final Logger log = LoggerFactory.getLogger(RadioApplication.class);
	int defaultVolume = 5;
	boolean isConfigured;
	int frequency;
	RDA5807M radio = new RDA5807M();
	
	public void initRadio() {
		if (!isConfigured) {
			isConfigured = true;
			radio = new RDA5807M();

			log.info("<Radio> Initialize");
			log.debug(
					"Grove - I2C FM Receiver module (by Seeed) based on RDA5807M single-chip (by RDA Microelectronics Inc.)");
			log.debug(radio.getI2cInfo());
			StatusResult statusResult = null;

			// power up
			log.debug("Power Up result: " + radio.powerUp().name());

			log.debug("specialInit() START");
			radio.specialInit();
			log.debug("specialInit() END");

			log.debug("CHIPID reading...");
			int readResult = radio.readRegisterFromDevice(Register.REG_0x00);
			if (readResult > 0) {
				log.debug(String.format("CHIPID: 0x%04x", Util.valueFromReg(readResult, RegisterMasks.CHIP_ID)));
			} else {
				log.debug("CHIPID result: " + readResult);
			}

			log.debug("set unmute");
			statusResult = radio.setMute(false);
			if (statusResult != StatusResult.SUCCESS) {
				log.debug("set unmute result: " + statusResult.name());
			} else {
				// set volume
				log.debug("set volume");
				statusResult = radio.setVolume(defaultVolume);
				if (statusResult != StatusResult.SUCCESS) {
					log.debug(String.format("Set volume to %d, result: %s", defaultVolume, statusResult.name()));
				}
			}
			log.debug("set seek mode to: 0 - STOP_AT_LIMIT");
			statusResult = radio.setSeekMode(0); // 0 - STOP_AT_LIMIT; 1 - WRAP_AT_LIMIT
			log.debug("set seek mode result: " + statusResult.name());
			log.info("<Radio> Initialize successful");
		}
	}

	public Map<String, String> setupRadio(Integer volume, String volumeDirection, Integer freq, Boolean mute, Boolean bass,
			Boolean stereo, Boolean info, String power) {
		Map<String, String> result = new HashMap<String, String>();
		Boolean isMute = null;
		Boolean isBassBoost = null;
		Boolean isStereo = null;

		initRadio();

		if (volume != null) {
			defaultVolume = volume;
			radio.setVolume(volume);
		}
		
		if (volumeDirection != null) {
			if (defaultVolume < 15) {
				if (volumeDirection.contains("+")) {
					defaultVolume = defaultVolume + 1;
					radio.setVolume(Integer.valueOf(defaultVolume));
				}
			}
			if (defaultVolume > 0) {
				if (volumeDirection.contains("-")) {
					defaultVolume = defaultVolume - 1;
					radio.setVolume(Integer.valueOf(defaultVolume));
				}
			}
		}
		
		if (freq != null) {
			frequency = freq;
			radio.setFrequency(freq);
		} else {
			radio.setFrequency(frequency);
		}

		if (mute != null) {
			radio.setMute(mute);
			if (mute.equals(Boolean.TRUE)) {
				isMute = true;
			} else {
				isMute = false;
			}
		}

		if (bass != null) {
			radio.setBassBoost(bass);
			if (bass.equals(Boolean.TRUE)) {
				isBassBoost = true;
			} else {
				isBassBoost = false;
			}
		}

		if (stereo != null) {
			radio.setStereo(stereo);
			if (stereo.equals(Boolean.TRUE)) {
				isStereo = true;
			} else {
				isStereo = false;
			}
		}

		if (info != null) {
			if (info.equals(Boolean.TRUE)) {
				result.put("station", radio.getStatusString());
			}
		}

		if (power != null) {
			if (power.contains("on")) {
				if (radio == null)
					initRadio();
			}
			if (power.contains("off")) {
				isConfigured = false;
				radio.powerDown();
			}
		}

		result.put("volume", String.valueOf(defaultVolume));
		result.put("channel", String.valueOf(frequency));
		result.put("mute", String.valueOf(isMute));
		result.put("bassBoost", String.valueOf(isBassBoost));
		result.put("stereo", String.valueOf(isStereo));
		log.info("<Radio> Configured: " + result.toString());
		return result;
	}

	public List<Station> searchStation(String seekDirection, Boolean getAllStations) {
		List<Station> stations = new ArrayList<Station>();
		initRadio();
		if (seekDirection != null) {
			if (seekDirection != null) {
				if (seekDirection.contains("+")) {
					radio.setSeekDirection(1);
				}
				if (seekDirection.contains("-")) {
					radio.setSeekDirection(0);
				}
			}

			int fLast = 0;
			frequency = radio.getBand().getMinimumFrequency();
			long startSeek; // after 300 msec must be tuned. after 500 msec must have RDS.
			if (frequency <= radio.getBand().getMaximumFrequency()) {
				radio.setSeek(true);
				try {
					Thread.sleep(1500);
				} catch (InterruptedException iex) {
					iex.printStackTrace();
				}
				startSeek = System.currentTimeMillis();

				// wait for seek complete
				boolean isStcComplete;
				boolean didSeekFail;
				boolean isFmTrue;
				boolean cond;
				do {
					isStcComplete = radio.isStcComplete();
					didSeekFail = radio.didSeekFail();
					isFmTrue = radio.isFmTrue();
					cond = isStcComplete && !didSeekFail && isFmTrue;
				} while (!cond && ((startSeek + 1500) > System.currentTimeMillis()));

				// check frequency
				frequency = radio.getReadChannel();
				if (frequency < fLast) {
					System.out.println("/// BREAK /// " + frequency);
					// break;
				}
				fLast = frequency;
			}
		}
		Station station = getStationInfo();
		stations.add(station);
		log.info("<Radio> " + station.getChannel() + "MHz tuned");
		if (getAllStations && seekDirection == null) {
			radio.setSeekDirection(Integer.valueOf(1));

			List<Station> tunedFrequencies = new ArrayList<Station>();
			List<Integer> tunedFrequenciess = new ArrayList<Integer>();
			int fLast = 0;
			int freq = 870;
			radio.setFrequency(freq);
			long startSeek; // after 300 msec must be tuned. after 500 msec must have RDS.
			while (freq <= radio.getBand().getMaximumFrequency()) {
				// Station station = new Station();
				radio.setSeek(true);
				try {
					Thread.sleep(1500);
				} catch (InterruptedException iex) {
					iex.printStackTrace();
				}
				startSeek = System.currentTimeMillis();

				// wait for seek complete
				boolean isStcComplete;
				boolean didSeekFail;
				boolean isFmTrue;
				boolean cond;
				do {
					isStcComplete = radio.isStcComplete();
					didSeekFail = radio.didSeekFail();
					isFmTrue = radio.isFmTrue();
					cond = isStcComplete && !didSeekFail && isFmTrue;
				} while (!cond && ((startSeek + 1500) > System.currentTimeMillis()));

				// check frequency
				freq = radio.getReadChannel();
				if (freq < fLast) {
					break;
				}
				fLast = freq;

				Station stationInfo = getStationInfo();

				if (cond) {
					if (tunedFrequencies.isEmpty()) {
						tunedFrequencies.add(stationInfo);
					} else {
						int fLastF = fLast;
						Optional<Integer> tunedFrequency = tunedFrequenciess.stream().filter(freqq -> (freqq == fLastF))
								.findAny();
						if (!tunedFrequency.isPresent()) {
							tunedFrequencies.add(stationInfo);
						}
					}
				}
			}
			radio.setFrequency(frequency);
			log.info("Number of tuned frequencies: " + String.valueOf(tunedFrequencies.size()));
			return tunedFrequencies;
		}
		return stations;
	}

	public Station getStationInfo() {
		Station station = new Station();
		station.setRdsReady(radio.isRdsReady());
		station.setSeekComplete(radio.isStcComplete());
		station.setSeekFail(radio.didSeekFail());
		station.setRdsSynchronized(radio.isRdsDecoderSynchronized());
		station.seteBlockFound(radio.hasBlkEBeenFound());
		station.setAudioType(radio.isStereoEnabled() ? "Stereo" : "Mono");
		station.setChannel(radio.getReadChannel());
		station.setRssi(radio.getRssi());
		station.setIsStation(radio.isFmTrue());
		station.setIsFMReady(radio.isFmReady());
		return station;
	}

}
