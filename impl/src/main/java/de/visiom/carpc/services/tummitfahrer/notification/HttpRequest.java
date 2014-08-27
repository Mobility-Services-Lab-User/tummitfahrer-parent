package de.visiom.carpc.services.tummitfahrer.notification;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

	private final String USER_AGENT = "Mozilla/5.0";
	
	/** 
	 * @param url The url from where we have to fetch the data
	 * @return the data fetched from the url
	 * @throws Exception
	 */
	private String sendGetRequest(String url) throws Exception {	  
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		/*System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);*/
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		return response.toString();	 
	}
		
	
	/** 
	* @param url   The URL where the post request needs to be sent
	 * @param urlParameters   The parameters of the request
	 * @return The return value of the post request
	 * @throws Exception
	 */
	public String sendPUTRequest(String url, String urlParameters) throws Exception {	 		
		URL obj = new URL(url);		
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		//add request header
		con.setRequestMethod("PUT");		
 		 
		// Send post request
		con.setDoOutput(true);
		
		if(!urlParameters.equals(""))
		{		
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
		}
		//int responseCode = con.getResponseCode();
		/*System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);*/
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 		
		return response.toString();			
	}
	
	
}
