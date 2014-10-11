package de.visiom.carpc.services.tummitfahrer.notification;

public class NotificationData {
	String lattitude;
	String longitude;
	String callbackURL;
	
	public NotificationData(String lat, String lon, String url )
	{
		lattitude = lat;
		longitude = lon;
		callbackURL = url;
	}

}
