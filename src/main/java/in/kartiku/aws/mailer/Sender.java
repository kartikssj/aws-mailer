package in.kartiku.aws.mailer;

import com.amazonaws.regions.Region;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class to send a mail using AWS SES
 * @author kartikbu
 * @since 01/01/15
 */
public class Sender {

	private static final Logger logger = LoggerFactory.getLogger(Sender.class);

	private final AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();

	public Sender(Region region) {
		client.setRegion(region);
	}

	public void send(String to, Mail mail) {
		try {
			client.sendEmail(buildRequest(to, mail));
			logger.debug("SEND_SUCCESS to=" + to);
		} catch (IllegalArgumentException e) {
			logger.error("INVALID_EMAIL to=" + to);
		} catch (Exception ex) {
			logger.error("SEND_FAILED to=" + to + " error=" + ex.getMessage().replace(" ", "+"));
		}
	}

	public void sendBulk(File file, final Mail mail, int interval, int concurrency) throws IOException {

		// start executor service and initialize stats
		ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
		final Stats stats = new Stats();

		// read file one at a time
		BufferedReader reader = new BufferedReader(new FileReader(file));
		for (String line; (line = reader.readLine()) != null; ) {
			final String to = line.trim();

			// check for commented or empty lines
			if (to.isEmpty() || to.startsWith("#")) {
				logger.info("SEND_SKIP to=" + to);
				stats.skipped();
				continue;
			}

			// send mail async
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        client.sendEmail(buildRequest(to, mail));
                        logger.debug("SEND_SUCCESS to=" + to);
                        stats.success();
                    } catch (IllegalArgumentException e) {
                        logger.error("INVALID_EMAIL to=" + to);
                        stats.invalid();
                    } catch (Exception ex) {
                        logger.error("SEND_FAILED to=" + to + " error=" + ex.getMessage().replace(" ", "+"));
                        stats.failure();
                    }
					// print stats every 1000 mails processed
                    if (stats.getTotal() % 1000 == 0) {
                        stats.log();
                    }
                }
            });

			// sleep to avoid excess rate
            try { Thread.sleep(interval); } catch (Exception ignored) {}

		}

		// close file reader
		try {
			reader.close();
		} catch (IOException ignored) {}

		// shutdown executor service
		try {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logger.error("Exception while waiting for executor to shutdown", e);
		}

		// final print stats
		stats.log();

	}

	private SendEmailRequest buildRequest(String to, Mail mail) {

		// Validate email
		if (!Utils.isValidEmail(to)) {
			throw new IllegalArgumentException("Invalid email");
		}

		// Construct an object to contain the recipient address.
		Destination destination = new Destination().withToAddresses(to);

		// Create the subject and body of the message.
		Content subject = new Content().withData(mail.getSubject());
		Content htmlBody = new Content().withData(mail.getBody());
		Body body = new Body().withHtml(htmlBody);

		// Create a message with the specified subject and body.
		Message message = new Message().withSubject(subject).withBody(body);

		// Assemble the email.
		return new SendEmailRequest().withSource(mail.getFrom()).withDestination(destination).withMessage(message);
	}

	private static class Stats {

		private long total = 0;
		private long invalids = 0;
		private long success = 0;
		private long failures = 0;
		private long skipped = 0;

		public void failure() {
			total++;
			failures++;
		}

		public void success() {
			total++;
			success++;
		}

		public void skipped() {
			total++;
			skipped++;
		}

		public void invalid() {
			total++;
			invalids++;
		}

		public long getTotal() {
			return total;
		}

		public void log() {
			logger.info("=================================================");
			logger.info(String.format("=========== Total: %d", total));
			logger.info(String.format("=========== Success: %d", success));
			logger.info(String.format("=========== Failures: %d", failures));
			logger.info(String.format("=========== Skipped: %d", skipped));
			logger.info(String.format("=========== Invalids: %d", invalids));
			logger.info("=================================================");
		}

	}



}
