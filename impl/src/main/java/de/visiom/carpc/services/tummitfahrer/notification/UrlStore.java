package de.visiom.carpc.services.tummitfahrer.notification;

import java.util.HashMap;
import java.util.Map;

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
	static Map<Integer,NotificationData> urlStoreHashMap = new HashMap<Integer,NotificationData>();

	public static void saveData(int key, String url, String lat, String lon)
	{
		urlStoreHashMap.put(key, new NotificationData(lat, lon, url));
	}
	
	public static NotificationData getData(int key)
	{		
		return urlStoreHashMap.get(key);				
	}
	
}


