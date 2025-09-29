package com.workflow.textExtraction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.util.MimeMessageParser;

public class ProcessEmail {

	public static void main(String[] args) throws Throwable {
		//String filePath = "sample/sample.msg";
		//String filePath = "sample/emailBase64.txt";
		String filePath = "sample/eml_w_attachmentsB64.txt";
		
		File file = new File(filePath);
		String base64 = new String(Files.readAllBytes(file.toPath()));
		//System.out.println("base64: " + base64);
		
		byte[] bytes = Base64.getDecoder().decode(base64);
		
		InputStream is = new ByteArrayInputStream(bytes);
		Properties properties = new Properties();
		Session session = Session.getDefaultInstance(properties, null);
		MimeMessage message = new MimeMessage(session, is);
		
		//System.out.println("message: " + message);
		System.out.println("message subject: " + message.getSubject());
		System.out.println("message quick recipients: " + message.getRecipients(Message.RecipientType.TO).toString());
		String toRecipients = "";
		Address[] toRecipientAddresses = message.getRecipients(Message.RecipientType.TO);
		for(Address address : toRecipientAddresses) {
			if(toRecipients.equals(""))
				toRecipients += ((InternetAddress) address).getAddress();
			else
				toRecipients += "," + ((InternetAddress) address).getAddress();
		}
		System.out.println("message toRecipients: " + toRecipients);
		//String messageBody = new MimeMessageParser(message).parse().getHtmlContent();
		String messageBody = new MimeMessageParser(message).parse().getPlainContent();
		System.out.println("message body: " + messageBody);

		if (message.isMimeType("multipart/*")) {
			MimeMultipart multipart = (MimeMultipart) message.getContent();

			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
					String fileName = bodyPart.getFileName();
					InputStream bodyStream = bodyPart.getInputStream();
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					byte[] data = new byte[1024];
					int read;
					while((read = bodyStream.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, read);
					}
					buffer.flush();
					String attachmentB64 = Base64.getEncoder().encodeToString(buffer.toByteArray());

					System.out.println("Filename: " + fileName);
					System.out.println("Base64.length: " + attachmentB64.length());
					//System.out.println("Base64: " + attachmentB64.substring(0, Math.min(attachmentB64.length(), 50)) + "..."); // Print first 50 chars
				}
			}
		}		
		
		
	}
}
