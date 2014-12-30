/**
* http://www.proveyourworth.net/level3/ Solution
* @author Raydelto Hernandez 
*/
package org.raydelto.proveworth.net;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HTTPConnector {

	private static String token;
	private static String sessionId;
	
	private static void printHeaders(Header[] headers){
		for (Header header : headers) {
			System.out.println(header.getName() + " = " + header.getValue());
		}
	}

	public static void start() throws Exception {
		Response response = Request.Get("http://www.proveyourworth.net/level3/start").execute();
		Header[] headers = response.returnResponse().getAllHeaders();
		
		for (Header header : headers) {
			System.out.println(header.getName() + " = " + header.getValue());
			if(header.getName().equals("Set-Cookie")){
				sessionId = header.getValue();
				System.out.println("Session id = " + sessionId);
			}
		}

		response = Request.Get("http://www.proveyourworth.net/level3/start").execute();
		Content get = response.returnContent();
		String responseText = get.asString();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(responseText.getBytes())));
		String line;

		while ((line = reader.readLine()) != null) {
			if (line.contains("statefulhash")) {
				String rawToken = line.trim();
				String[] parts = rawToken.split("value=");
				token = parts[1].substring(1, 33);
				System.out.println("Got token: " + token);
				break;
			}
		}		
	}

	public static void activate() throws Exception {
		Response response = Request.Get("http://www.proveyourworth.net/level3/activate?statefulhash=" + token).execute();		
		Content get = response.returnContent();
		String responseText = get.asString();
		System.out.println(responseText);
	}

	public static void payload() throws Exception {
		Response response = Request.Get("http://www.proveyourworth.net/level3/payload").execute();
		Content get = response.returnContent();
		DataInputStream reader = new DataInputStream(get.asStream());
		int byteRead = -1;
		DataOutputStream outFile = new DataOutputStream(new FileOutputStream(new File("image.jpg")));
		System.out.println("Downloading image ...");
		while ((byteRead = reader.read()) != -1) {
			outFile.writeByte(byteRead);
		}
		outFile.flush();
		outFile.close();
		System.out.println("Image downloaded");
	}

	public static void signImage() throws Exception {
		final BufferedImage image = ImageIO.read(new File("image.jpg"));
		Graphics graphic = image.getGraphics();
		graphic.setFont(graphic.getFont().deriveFont(30f));
		graphic.drawString("Raydelto Hernandez", image.getWidth() - 350, image.getHeight() - 100);
		graphic.drawString("Token: " + token, 100, image.getHeight() - 50);
		graphic.dispose();
		ImageIO.write(image, "jpg", new File("prove.jpg"));
	}

	public static void submitForm() throws Exception {
		HttpClient httpclient = HttpClients.createDefault();
		HttpResponse response = null;
		HttpPost httppost = new HttpPost("http://www.proveyourworth.net/level3/reaper");
		httppost.addHeader("Cookie",sessionId);
		FileBody image = new FileBody(new File("prove.jpg"));
		FileBody code = new FileBody(new File("src\\org\\raydelto\\proveworth\\net\\HTTPConnector.java"));
		FileBody resume = new FileBody(new File("Your-Resume.pdf"));
		StringBody email = new StringBody("YOUR-EMAIL@gmail.com", ContentType.TEXT_PLAIN);
		StringBody name = new StringBody("Your Name", ContentType.TEXT_PLAIN);
		StringBody aboutme = new StringBody(
				"About you.",
				ContentType.TEXT_PLAIN);

		HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("image", image).addPart("code", code).addPart("resume", resume).addPart("email", email)
				.addPart("name", name).addPart("aboutme", aboutme).build();

		httppost.setEntity(reqEntity);

		System.out.println("executing request " + httppost.getRequestLine());
		response = httpclient.execute(httppost);

		System.out.println("----------------------------------------");
		System.out.println(response.getStatusLine());		
		printHeaders(response.getAllHeaders());
		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			System.out.println("Response content length: " + resEntity.getContentLength());
			InputStream is = resEntity.getContent();
			int byteRead = 0;
			System.out.println("Form submission response:");
			while ((byteRead = is.read()) != -1) {
				System.out.print((char) byteRead);
			}
			System.out.println("**END** Form submission response:");

		}
		EntityUtils.consume(resEntity);

	}

	public static void main(String[] args) throws Exception {
		System.out.println("---Start---");
		start();
		System.out.println("---END Start---");

		System.out.println("---Start activate---");
		activate();
		System.out.println("--- end activate---");

		System.out.println("---Start payload---");
		payload();
		System.out.println("---end  payload---");

		System.out.println("---Start signImage---");
		signImage();
		System.out.println("---end  signImage---");

		System.out.println("---Start submitForm---");
		submitForm();
		System.out.println("---end  submitForm---");

	}

}
