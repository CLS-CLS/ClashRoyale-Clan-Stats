package org.lytsiware.heroku;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Pinger not allowing the app to sleep, for usage by a scheduler.
 * Example of syntax to run the main : 
 *  <code>
 * java -cp target\clash.royale-1.0-SNAPSHOT.jar -D"loader.main=org.lytsiware.heroku.Pinger" 
 * 	org.springframework.boot.loader.PropertiesLauncher "13:23" "http://thegreeknoobs.herokuapp.com"
 * </code>
 * this will ping the http://thegreeknoobs.herokuapp.com" until 13:23 each day
 * 
 * 
 * @deprecated no need to use this class anymore as the 6 hours sleep per day requirement is dropped
 * Use a {@code Scheduled} method instead
 *
 */
@Deprecated 
public class CLIPinger {

	public static void main(String[] args) throws MalformedURLException {

		final LocalTime stopTime = LocalTime.parse(args[0], DateTimeFormatter.ofPattern("HH:mm"));

		URL url = new URL(args[1]);

		new CLIPinger().startPinging(stopTime, url);

	}
	
	/**
	 * 
	 * @param stopTime time the app should go to sleep. From that time after the pinger will be suppresed
	 * @param url the url to ping
	 */
	public void startPinging(LocalTime stopTime, URL url) {
		Timer timer = new Timer();

		TimerTask task = new CustomTimerTask(timer) {

			@Override
			public void run() {
				System.out.println("pinger task executing...");
				if (LocalDateTime.now().isAfter(LocalDate.now().atTime(stopTime))) {
					System.out.println("Cancelling pinger task");
					timer.cancel();
					return;
				}

				try {
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
					connection.setConnectTimeout(50 * 1000);
					int responseCode = connection.getResponseCode();
					if (responseCode != 200) {
						throw new RuntimeException("Expected response code 200 but was " + responseCode);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
		};

		timer.scheduleAtFixedRate(task, new Date(), 1000);

	}

	abstract static class CustomTimerTask extends TimerTask {

		protected Timer timer;

		public CustomTimerTask(Timer timer) {
			this.timer = timer;
		}

	}

}
