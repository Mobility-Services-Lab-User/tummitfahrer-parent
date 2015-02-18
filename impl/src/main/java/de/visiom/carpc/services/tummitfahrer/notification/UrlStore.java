package de.visiom.carpc.services.tummitfahrer.notification;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.visiom.carpc.services.tummitfahrer.ParameterChangeRequestHandler;

/*public class UrlStore {
	static Map<Integer,String> urlStoreHashMap = new HashMap<Integer,String>();

	public static void saveUrl(int key, String url)
	{
		urlStoreHashMap.put(key, url);		
	}
	
	public static String getURL(int key)
	{
		return urlStoreHashMap.get(key);
	}
	
}
*/

public class UrlStore {
	private static final Logger LOG = LoggerFactory
            .getLogger(UrlStore.class);
	
	static Map<Integer,NotificationData> urlStoreHashMap = new HashMap<Integer,NotificationData>();

	public static void saveData(int key, String url, String lat, String lon)
	{
		urlStoreHashMap.put(key, new NotificationData(lat, lon, url));
		LOG.info("URL Stored in the memory => Key ->{} / URL ->{} / Lat ->{} / Lon ->{}",key,url,lat,lon);
	}
	
	public static NotificationData getData(int key)
	{		
		return urlStoreHashMap.get(key);				
	}
	
	public static void removeData(int key)
	{
		urlStoreHashMap.remove(key);
		
	}
	
}


