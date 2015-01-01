package in.kartiku.aws.mailer;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author kartikbu
 * @since 01/01/15
 */
public class AWSMailer {

	private static Logger logger = LoggerFactory.getLogger(AWSMailer.class);

	public static void main(String[] args) {

		// parse command arguments
		Options options = new Options();
		CommandLine cmd = null;
		try {

			Option option_f = new Option("f", "from", true, "From address");
			option_f.setRequired(true);
			options.addOption(option_f);

			Option option_s = new Option("s", "subject", true, "Mail subject");
			option_s.setRequired(true);
			options.addOption(option_s);

			Option option_b = new Option("b", "body", true, "Mail body content file location");
			option_b.setRequired(true);
			options.addOption(option_b);

			Option option_r = new Option("r", "region", true, "Region of AWS to use");
			option_r.setRequired(true);
			options.addOption(option_r);

			Option option_i = new Option("i", "interval", true, "Interval between two mails in case -T is provided");
			option_i.setRequired(true);
			options.addOption(option_i);

			Option option_t = new Option("t", "to", true, "To address");
			option_t.setRequired(false);
			options.addOption(option_t);

			Option option_T = new Option("T", "to-list", true, "To address list file location");
			option_T.setRequired(false);
			options.addOption(option_T);

			// parse
			CommandLineParser parser = new BasicParser();
			cmd = parser.parse(options, args);

		} catch (Exception e) {
			usage(e.getMessage(), options);
		}

		if (cmd != null) {

			// read "to"
			String to = cmd.getOptionValue("t");
			String toFileLocation = cmd.getOptionValue("T");
			if (to == null && toFileLocation == null) {
				usage("Atleast one of -t or -T options must be provided.", options);
			}

			// read body content
			File contentFile = new File(cmd.getOptionValue("b"));
			String body = "";
			try {
				body = new Scanner(contentFile).useDelimiter("\\Z").next();
			} catch (FileNotFoundException e) {
				usage("Body content file not found: " + contentFile, options);
			}

			// read from, subject
			Mail mail = null;
			try {
				String from = cmd.getOptionValue("f");
				String subject = cmd.getOptionValue("s");
				mail = Mail.builder().from(from).subject(subject).body(body).build();
			} catch (IllegalArgumentException e) {
				usage(e.getMessage(), options);
			}

			// read region
			Region region = Region.getRegion(Regions.valueOf(cmd.getOptionValue("r")));


			// send using appropriate method
			Sender sender = new Sender(region);
			try {
				if (toFileLocation != null && !toFileLocation.isEmpty()) {

					// get file
					File toFile = new File(toFileLocation);
					if (!toFile.exists()) {
						usage("To address list file not found: " + toFileLocation, options);
					}

					// get interval
					int interval = 1000;
					try {
						 interval = Integer.parseInt(cmd.getOptionValue("i"));
					} catch (Exception e) {
						usage("Invalid interval: " + cmd.getOptionValue("i"), options);
					}

					// start sending bulk
					sender.sendBulk(toFile, mail, interval);

				} else {

					// send single email
					sender.send(to, mail);
				}
			} catch (Exception e) {
				usage(e.getMessage(), options);
			}


		}

	}

	private static void usage(String error, Options options) {
		System.out.println("Error: " + error);
		new HelpFormatter().printHelp(
				"java -jar aws-mailer.jar",
				"Utility to send bulk mails using Amazon AWS SES",
				options,
				"For any help, contact kartikssj@gmail.com",
				true
		);
		System.exit(1);

	}

}
