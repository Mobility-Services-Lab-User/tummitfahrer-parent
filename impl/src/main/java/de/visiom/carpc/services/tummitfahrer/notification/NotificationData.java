package de.visiom.carpc.services.tummitfahrer.notification;

public class NotificationData {
	public String lattitude;
	public String longitude;
	public String callbackURL;
	
	public NotificationData(String lat, String lon, String url )
	{
		lattitude = lat;
		longitude = lon;
		callbackURL = url;
	}

}
