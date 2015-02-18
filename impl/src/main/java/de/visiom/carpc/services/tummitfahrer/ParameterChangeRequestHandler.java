package de.visiom.carpc.services.tummitfahrer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.visiom.carpc.asb.messagebus.CommandPublisher;
import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.commands.GenericResponse;
import de.visiom.carpc.asb.messagebus.commands.ValueChangeRequest;
import de.visiom.carpc.asb.messagebus.handlers.ValueChangeRequestHandler;
import de.visiom.carpc.asb.messagebus.commands.Response;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.exceptions.NoSuchParameterException;
import de.visiom.carpc.asb.servicemodel.parameters.Parameter;
import de.visiom.carpc.asb.servicemodel.parameters.SetParameter;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.SetValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StateValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.serviceregistry.ServiceRegistry;
import de.visiom.carpc.asb.serviceregistry.exceptions.NoSuchServiceException;
import de.visiom.carpc.services.tummitfahrer.notification.HttpRequest;
import de.visiom.carpc.services.tummitfahrer.notification.NotificationData;
import de.visiom.carpc.services.tummitfahrer.notification.TimelineEventData;
import de.visiom.carpc.services.tummitfahrer.notification.UrlStore;
import de.visiom.carpc.services.tummitfahrer.notification.Utilities;


public class ParameterChangeRequestHandler extends ValueChangeRequestHandler {

	private static final String RECEIVE_LOG_MESSAGE = "TUMitfahrer => Received a value change request for {},{}. Dispatching the request to the ValueStore...";
	
	private static final Logger LOG = LoggerFactory
	            .getLogger(ParameterChangeRequestHandler.class);
	
	private SetParameter timelineSetParamter;
	private SetParameter timelineDataSetParamter;
	private ServiceRegistry serviceRegistry;
    private EventPublisher eventPublisher;
	
	private CommandPublisher commandPublisher; 
	 
	public void setCommandPublisher(CommandPublisher commandPublisher) {
        this.commandPublisher = commandPublisher;
    }
	
	public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
	
	@Override
	public void onValueChangeRequest(ValueChangeRequest request, Long requestID) {	
		try
		{
		
		LOG.info(RECEIVE_LOG_MESSAGE, request.getParameter(), request.getValue());
		Parameter parameter = request.getParameter();
		
		boolean result = false;
		
		/*LOG.info("TUMitfahrer => Service Name => {} ",parameter.getService().getName());
		LOG.info("TUMitfahrer => Service Parameter => {}" ,parameter.getName());
		
		LOG.info("TUMitfahrer => Utility => {} ", Utilities.readConfigFile("setParameterServiceName") + "|");
		LOG.info("TUMitfahrer => Utility => {} ", Utilities.readConfigFile("timelineSetParameterName") + "|");*/
		
		if(parameter.getService().getName().equals(Utilities.readConfigFile("setParameterServiceName"))
				&& parameter.getName().equals(Utilities.readConfigFile("timelineSetParameterName"))) 	
		 { 	
			// Parse the timelineEvent data
			TimelineEventData aTimelineEvent = new TimelineEventData();
	    	aTimelineEvent.processRequest(request, serviceRegistry, Utilities.readConfigFile("timelineSetParameterName") );
	    	
	    	NotificationData notifData = UrlStore.getData(aTimelineEvent.id);
	    	
	    	if( notifData != null )
	    	{
	    		// Check the name and process the request
	    		LOG.info("In Update Handler -> ID matched => {}  STATE =>{}", aTimelineEvent.id, aTimelineEvent.state);
	    		
	    		if(aTimelineEvent.state.equals("Anfahren"))	    	
	    		{
	    			LOG.info("TUMitfahrer => TimelineEvent with Accept received");
	    			handleAccept(aTimelineEvent.type, notifData);
	    			UrlStore.removeData(aTimelineEvent.id);
	    		}
	    		else if(aTimelineEvent.state.equals("Löschen"))
	    		{
	    			LOG.info("TUMitfahrer => TimelineEvent with Decline received");
	    			handleDecline(aTimelineEvent.type, notifData);
	    			UrlStore.removeData(aTimelineEvent.id);
	    		}
	    		else
	    		{	
	    			//LOG here
	    			LOG.info("TUMitfahrer => Recommender service sent some other state -> {}", aTimelineEvent.state);
	    		}
	    	}
	    	else
	    	{
	    		//Ignore the request    		
	    		LOG.info("TUMitfahrer => In Update Handler -> ID not found in URL store");
	    	}
	    	
			//result = handleAccept(request);
		 }	
		 else 	
		 { 	
			 LOG.info("TUMitfahrer => Publishing Set parameter to Recommender Service");
			 result = publishSetParameterChangeEvent(request);
		 }
		
		int responseStatus = getResponseStatus(result);
		
        Response response = GenericResponse
                .createGenericResponse(responseStatus);
		
		commandPublisher.publishResponse(response, requestID, request
                .getParameter().getService());   
		}
		catch(Exception e)
		{
			LOG.info("TUMitfahrer => Exception -> {}", e);			
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
				/*Service navigationService = serviceRegistry.getService(Utilities.readConfigFile("navigationServiceName"));		
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
		        
		        LOG.info("TUMitfahrer => Driver Pickup Alert pushed to bus for navigation service.");*/
				LOG.info("TUMitfahrer => Driver Pickup Alert received but ignored!");
				
			}
			else if (type.equals("User Join Request"))
			{
				// Get the call back URL
				// Do a PUT request				
				//TODO: Parse the request to get the ID, Parse the ID to get your own ID i.e 04. Post response.
				//String response = putRequest.sendPUTRequest("http://localhost:3000/api/v2/rides/67/requests/18?passenger_id=2", "1"); //TODO: 1 = dummy value remove it
				//LOG.info("Response successfully sent:" + response);
				
				String response = putRequest.sendPUTRequest(notifData.callbackURL, "1"); //TODO: 1 = dummy value remove it
				LOG.info("TUMitfahrer => Accept -> Response successfully sent:" + response);				
			}
			
			
			LOG.info("TUMitfahrer => Accept handled");
		} catch (Exception e) {				
			LOG.info("TUMitfahrer => Error occured while sending the PUT request", e);
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
				String response = "";
				// Get the call back URL
				// Do a PUT request			
				response = putRequest.sendDELETERequest(notifData.callbackURL, "1");
				
				LOG.info("TUMitfahrer => Decline -> Response successfully sent:" + response);	
			}
			
			LOG.info("Response successfully sent: DECLINE");
		} catch (Exception e) {				
			LOG.info("Error occured while sending the PUT request", e);
			return false;
		}
		
		return true;
	}
	
	
	private int getResponseStatus(boolean wasSuccessfull) {
        if (wasSuccessfull) {
            return GenericResponse.STATUS_OK;
        } else {
            return GenericResponse.STATUS_ERROR;
        }
    }
	
	/* This function publishes a change event on the bus. First it looks for the service and its parameter and then
	 * it pushes the event on this bus. 
	 * @param request It is the Change request. This change request was posted from the TUMitfahrer server using the
	 * PUT command.
	 * @return true = if event was published without errors. false = some exception was raised while accessing the service.
	 */
	private boolean publishSetParameterChangeEvent(ValueChangeRequest request)	
	{	
	
		Service tummitfahrerService;
		Service recommenderService;
		try {			
			recommenderService = serviceRegistry.getService(Utilities.readConfigFile("setParameterServiceName"));
			tummitfahrerService = serviceRegistry.getService(Utilities.readConfigFile("notificationSetParameterServiceName"));
		
			timelineDataSetParamter = (SetParameter) tummitfahrerService.getParameter(Utilities.readConfigFile("timelineDataSetParameterName"));
			
			timelineSetParamter = (SetParameter) recommenderService.getParameter(Utilities.readConfigFile("timelineSetParameterName"));
		
			//Get TIMELINEEVENT parameters
			Parameter typeParam = timelineSetParamter.getParameter("type");
			Parameter idParam = timelineSetParamter.getParameter("id");
			Parameter nameParam = timelineSetParamter.getParameter("name");
			Parameter addressParam = timelineSetParamter.getParameter("address");
			Parameter imageParam = timelineSetParamter.getParameter("image");
			Parameter stateParam = timelineSetParamter.getParameter("state");
			
			
			//Get TIMELINEEVENTDATA parameters
			Parameter typeParamData = timelineDataSetParamter.getParameter("type");
			Parameter idParamData = timelineDataSetParamter.getParameter("id");
			Parameter nameParamData = timelineDataSetParamter.getParameter("name");
			Parameter addressParamData = timelineDataSetParamter.getParameter("address");
			Parameter imageParamData = timelineDataSetParamter.getParameter("image");
			Parameter urlParamData = timelineDataSetParamter.getParameter("url");
			Parameter lattParamData = timelineDataSetParamter.getParameter("latt");
			Parameter longParamData = timelineDataSetParamter.getParameter("long");
			
			//Get values from the request using TIMELINEEVENTDATA
			SetValueObject receivedRequest = (SetValueObject)request.getValue();
			Map<Parameter, ValueObject> requestValue = receivedRequest.getValue();
			
			
			StringValueObject typeValueObj = (StringValueObject) requestValue.get(typeParamData);
			NumberValueObject idValueObj = (NumberValueObject) requestValue.get(idParamData);
			StringValueObject nameValueObj = (StringValueObject) requestValue.get(nameParamData);
			StringValueObject addressValueObj = (StringValueObject) requestValue.get(addressParamData);
			StringValueObject imageValueObj = (StringValueObject) requestValue.get(imageParamData);
			StringValueObject urlValueObj = (StringValueObject) requestValue.get(urlParamData);
			StringValueObject lattValueObj = (StringValueObject) requestValue.get(lattParamData);
			StringValueObject longValueObj = (StringValueObject) requestValue.get(longParamData);
			
			//Add the service number to the id
			int notificationID = Utilities.addServiceNumberToID(idValueObj.getValue().intValue());
			
			//TODO: Fix the URL. ASk David whether I can declare a new parameter named URL in the rest service.xml
			//Save the notificationId and url in the memory. Will be used later on when Accept/Decline will be called
			UrlStore.saveData(notificationID, Utilities.getCompleteTUMitfahrerServerURL(urlValueObj.getValue()), lattValueObj.getValue(),longValueObj.getValue());
			
			//CREATE A TIMELINEEVENT Map object
			//Do the modifications to the input
			Map<Parameter, ValueObject> updates = new HashMap<Parameter, ValueObject>();
			updates.put(typeParam, typeValueObj);
			updates.put(idParam, NumberValueObject.valueOf(notificationID));
			updates.put(nameParam, nameValueObj);
			updates.put(addressParam, addressValueObj);
			updates.put(imageParam, imageValueObj);
			updates.put(stateParam, StateValueObject.valueOf("Warten"));
			
			//Publish the result to bus
			ValueObject valueObject = SetValueObject.valueOf(updates);
			
			//TODO: Use SetValueObject once we have the implementation	        
			//ValueObject valueObject = generateFakeSetObject(request);

	        ValueChangeEvent valueChangeEvent = ValueChangeEvent
	                .createValueChangeEvent(timelineSetParamter, valueObject);
	        eventPublisher.publishValueChange(valueChangeEvent);
	        return true;
        
		} catch (NoSuchServiceException e) {			
			LOG.info("TUMitfahrer => Unable to find the service!", e);
			return false;
		}
		catch(NoSuchParameterException e)
		{
			LOG.info("TUMitfahrer => Unable to find the parameter!", e);
			return false;
		}
	}
			
}