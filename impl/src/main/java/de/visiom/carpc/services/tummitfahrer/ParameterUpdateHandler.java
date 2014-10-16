package de.visiom.carpc.services.tummitfahrer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.commands.ValueChangeRequest;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.messagebus.handlers.ValueChangeEventHandler;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.SetValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.servicemodel.parameters.Parameter;
import de.visiom.carpc.asb.servicemodel.parameters.SetParameter;
import de.visiom.carpc.asb.serviceregistry.ServiceRegistry;
import de.visiom.carpc.services.tummitfahrer.notification.HttpRequest;
import de.visiom.carpc.services.tummitfahrer.notification.NotificationData;
import de.visiom.carpc.services.tummitfahrer.notification.TimelineEventData;
import de.visiom.carpc.services.tummitfahrer.notification.UrlStore;
import de.visiom.carpc.services.tummitfahrer.notification.Utilities;

public class ParameterUpdateHandler extends ValueChangeEventHandler {

    private static final Logger LOG = LoggerFactory
            .getLogger(ParameterUpdateHandler.class);
    
    private ServiceRegistry serviceRegistry;
    private EventPublisher eventPublisher;
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void onValueChangeEvent(ValueChangeEvent valueChangeEvent) {
    	TimelineEventData aTimelineEvent = new TimelineEventData();
    	aTimelineEvent.processValueChange(valueChangeEvent);
    	
    	NotificationData notifData = UrlStore.getData(aTimelineEvent.id);
    	
    	if( notifData != null )
    	{
    		// Check the name and process the request
    		LOG.info("In Update Handler -> ID matched => {}", aTimelineEvent.id);
    		
    		//TODO: Revert the check
    		if(aTimelineEvent.state.equals("Anfahren"))
    		//if(aTimelineEvent.state.equals("Warten"))
    		{
    			handleAccept(aTimelineEvent.type, notifData);    			
    		}
    		else if(aTimelineEvent.state.equals("Löschen"))
    		{
    			handleDecline(aTimelineEvent.type, notifData);
    		}
    		else
    		{	
    			//LOG here
    			LOG.info("Recommender service sent some other state -> {}", aTimelineEvent.state);
    		}
    	}
    	else
    	{
    		//Ignore the request    		
    		LOG.info("In Update Handler -> ID not found in URL store");
    	}
    }
    
    private boolean handleAccept(String type, NotificationData notifData)
	{
		HttpRequest putRequest = new HttpRequest();
		try {			

			if (type.equals("Driver Pickup Alert"))
			{				
				// Push coordinates to Manual Parameter				
				
				// 1- Get Navigation service and parameters
				Service navigationService = serviceRegistry.getService(Utilities.readConfigFile("navigationServiceName"));		
				SetParameter navigationParams = (SetParameter) navigationService.getParameter(Utilities.readConfigFile("navigationSetParameterName"));
				
				Parameter latParam = navigationParams.getParameter("waypointLatitude");
				Parameter longParam = navigationParams.getParameter("waypointLongitude");
				Parameter typeParam = navigationParams.getParameter("waypointType");	
				
				// 2- Create a set parameter with the new values
				Map<Parameter, ValueObject> updates = new HashMap<Parameter, ValueObject>();
				updates.put(latParam, NumberValueObject.valueOf(notifData.lattitude));
				updates.put(longParam, NumberValueObject.valueOf(notifData.longitude));
				updates.put(typeParam, StringValueObject.valueOf(Utilities.readConfigFile("bundleName")));
				
				//Publish the result to bus
				ValueObject valueObject = SetValueObject.valueOf(updates);
				
		        ValueChangeEvent valueChangeEvent = ValueChangeEvent
		                .createValueChangeEvent(navigationParams, valueObject);
		        eventPublisher.publishValueChange(valueChangeEvent);
		        
		        LOG.info("Driver Pickup Alert pushed to bus for navigation service.");
				
			}
			else if (type.equals("User Join Request"))
			{
				// Get the call back URL
				// Do a PUT request				
				//TODO: Parse the request to get the ID, Parse the ID to get your own ID i.e 04. Post response.
				//String response = putRequest.sendPUTRequest("http://localhost:3000/api/v2/rides/67/requests/18?passenger_id=2", "1"); //TODO: 1 = dummy value remove it
				//LOG.info("Response successfully sent:" + response);
				
				String response = putRequest.sendPUTRequest(notifData.callbackURL, "1"); //TODO: 1 = dummy value remove it
				LOG.info("Accept -> Response successfully sent:" + response);				
			}
			
			
			LOG.info("Accept handled");
		} catch (Exception e) {				
			LOG.info("Error occured while sending the PUT request", e);
			return false;
		}
		
		//LOG.info("Publishing accept parameter");
		//return publishNumericParameterChangeEvent(request);	
		return true;
	}
	
	private boolean handleDecline(String type, NotificationData notifData)
	{
		HttpRequest putRequest = new HttpRequest();
		try {
			// 1- Process the request and get all the values posted by the iPad
			/*TimelineEventData data = new TimelineEventData();
			data.processRequest(request, serviceRegistry, "setDeclineParameterName");
			
			NotificationData notifData = UrlStore.getData(data.id);*/
			
			if (type.equals("Driver Pickup Alert"))
			{	
				// Do nothing
			}
			else if (type.equals("User Join Request"))
			{
				// Get the call back URL
				// Do a PUT request			
				String response = putRequest.sendPUTRequest(notifData.callbackURL, "1"); //TODO: 1 = dummy value remove it
				LOG.info("Decline -> Response successfully sent:" + response);	
			}
			
			LOG.info("Response successfully sent: DECLINE");
		} catch (Exception e) {				
			LOG.info("Error occured while sending the PUT request", e);
			return false;
		}
		
		return true;
	}	
}
