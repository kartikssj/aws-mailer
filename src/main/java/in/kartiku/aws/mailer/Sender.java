package in.kartiku.aws.mailer;

import com.amazonaws.regions.Region;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Class to send a mail using AWS SES
 * @author kartikbu
 * @since 01/01/15
 */
public class Sender {

	private Logger logger = LoggerFactory.getLogger(Sender.class);

	private Region region;

	public Sender(Region region) {
		this.region = region;
	}

	public void send(String to, Mail mail) {

		// Construct an object to contain the recipient address.
		Destination destination = new Destination().withToAddresses(new String[]{to});

		// Create the subject and body of the message.
		Content subject = new Content().withData(mail.getSubject());
		Content textBody = new Content().withData(mail.getBody());
		Body body = new Body().withText(textBody);

		// Create a message with the specified subject and body.
		Message message = new Message().withSubject(subject).withBody(body);

		// Assemble the email.
		SendEmailRequest request = new SendEmailRequest().withSource(mail.getFrom()).withDestination(destination).withMessage(message);

		// Send mail
		try {
			AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
			client.setRegion(region);
			client.sendEmail(request);
			logger.debug("SEND_SUCCESS to=" + to + " from=" + mail.getFrom() + " subject=" + mail.getSubject());
		} catch (Exception ex) {
			logger.error("SEND_FAILED to=" + to + " from=" + mail.getFrom() + " subject=" + mail.getSubject());
		}

	}

	public void sendBulk(File file, Mail mail, int interval) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		for (String line; (line = reader.readLine()) != null; ) {
			String email = line.trim();
			if (!email.isEmpty() && !email.startsWith("#")) {
				send(email, mail);
				try { Thread.sleep(interval); } catch (Exception ignored) {}
			} else {
				logger.info("SEND_SKIP to=" + email + " from=" + mail.getFrom() + " subject=" + mail.getSubject());
			}
		}
		try {
			reader.close();
		} catch (IOException ignored) {

		}
	}



}
