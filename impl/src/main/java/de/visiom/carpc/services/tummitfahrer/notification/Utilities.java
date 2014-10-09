package de.visiom.carpc.services.tummitfahrer.notification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.visiom.carpc.services.tummitfahrer.ParameterChangeRequestHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Utilities {

	private static final Logger LOG = LoggerFactory
            .getLogger(Utilities.class);
	
	public static int addServiceNumberToID(int id)
	{
		String stringId = readConfigFile("serviceId") + String.valueOf(id);
		return Integer.parseInt(stringId);		
	}
	
	public static String getCompleteTUMitfahrerServerURL(String path)
	{
		return readConfigFile("TUMitfahrerServerAddress") + path;
	}
	
	/**
	* This function reads the configuration file and returns the value against the key propertyName
	 * @param propertyName The property whose value needs to be returned
	 * @return The value found in the config.properties file
	 */
	public static String readConfigFile(String propertyName)
	{
		Properties prop = new Properties();
		InputStream input = null;
	 
		try {
			
			String filename = "/config.properties";
			input = ParameterChangeRequestHandler.class.getClassLoader().getResourceAsStream(filename); //reads from the resource folder
			
    		if(input==null){
    			LOG.info("Sorry, unable to find " + filename);
    		    return "";
    		}
	  
			// load a properties file
			prop.load(input);
	 
			// get the property value and print it out
			return prop.getProperty(propertyName);			
	 
		} catch (IOException e) {
			e.printStackTrace();
			LOG.info("Unable to find the service!", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "";		
	}
	
	
	public static String getValueFromJSON(String jsonString, String key)
	{
		/*Gson gson = new GsonBuilder().create();*/
		/*JsonObject jobj = gson.fromJson(jsonString, JsonObject.class);*/
		/*{
		    "id": "5",
		    "name": "Driver Pickup Alert",
		    "address": "16 H Ghulam Nabi Colony",
		    "image": "http://hostingride.com/wp-content/uploads/2014/07/fond-ecran-wallpaper-image-arriere-plan-hd-29-HD.jpg",
		    "url": "http://localhost:3000/api/v2/rides/67/requests/18?passenger_id=2"
		}*/
		
		Gson gson = new Gson();
		
		JsonObject jobj = new JsonParser().parse(jsonString).getAsJsonObject();
		return jobj.get(key).toString();	
	}
	
}
