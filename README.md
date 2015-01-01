aws-mailer
==========

Utility to send bulk mails using [Amazon AWS SES](http://aws.amazon.com/ses/). 
You first need to register and get credentials (access key and secret key) from 
AWS to use this tool, which is provided using any of the methods mentioned in 
the [documentation](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html). 

To build, you need maven2 and java >= 1.7.
	
	mvn clean package 
	 
Once built, the binaries will be present inside the target directory.

	usage: java -jar aws-mailer.jar -b <arg> -f <arg> [-F <arg>] [-i <arg>] -r
		   <arg> -s <arg> [-t <arg>] [-T <arg>]
	Utility to send bulk mails using Amazon AWS SES
	 -b,--body <arg>        Mail body content file location
	 -f,--from <arg>        From address
	 -F,--from-name <arg>   From name
	 -i,--interval <arg>    Interval between two mails in case -T is provided
	 -r,--region <arg>      Region of AWS to use
	 -s,--subject <arg>     Mail subject
	 -t,--to <arg>          To address
	 -T,--to-list <arg>     To address list file location
	 
For any support & feedback, please mail me at kartikssj@gmail.com.
