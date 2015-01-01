package in.kartiku.aws.mailer;

import org.apache.commons.validator.EmailValidator;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

/**
 * @author kartikbu
 * @since 01/01/15
 */
public class Utils {

	public static String getEncodedEmail(String address, String name) throws UnsupportedEncodingException {
		if (name != null) {
			return new InternetAddress(address, name).toString();
		} else {
			return address;
		}
	}

	public static boolean isValidEmail(String address) throws IllegalArgumentException {
		return EmailValidator.getInstance().isValid(address);
	}

}
