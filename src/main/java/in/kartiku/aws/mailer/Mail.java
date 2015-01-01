package in.kartiku.aws.mailer;

/**
 * Mail POJO and Builder classes.
 * @author kartikbu
 * @since 01/01/15
 */
public class Mail {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String from;
		private String subject;
		private String body;

		public Builder from(String from) {
			this.from = from;
			return this;
		}

		public Builder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder body(String body) {
			this.body = body;
			return this;
		}

		public Mail build() throws IllegalArgumentException {
			if (from == null || from.isEmpty()) {
				throw new IllegalArgumentException("FROM cannot be empty");
			}
			if (subject == null || subject.isEmpty()) {
				throw new IllegalArgumentException("SUBJECT cannot be empty");
			}
			if (body == null || body.isEmpty()) {
				throw new IllegalArgumentException("BODY cannot be empty");
			}
			return new Mail(from,subject,body);
		}

	}

	private String from;
	private String subject;
	private String body;

	private Mail(String from, String subject, String body) {
		this.from = from;
		this.subject = subject;
		this.body = body;
	}

	public String getFrom() {
		return from;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

}
