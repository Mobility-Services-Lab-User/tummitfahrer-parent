package de.visiom.carpc.services.tummitfahrer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import de.visiom.carpc.asb.messagebus.CommandPublisher;
import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.commands.GenericResponse;
import de.visiom.carpc.asb.messagebus.commands.ValueChangeRequest;
import de.visiom.carpc.asb.messagebus.handlers.ValueChangeRequestHandler;
import de.visiom.carpc.asb.messagebus.commands.Response;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.parametervalueregistry.exceptions.UninitalizedValueException;
import de.visiom.carpc.asb.rest.entities.Parameters;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.exceptions.IncompatibleValueException;
import de.visiom.carpc.asb.servicemodel.exceptions.NoSuchParameterException;
import de.visiom.carpc.asb.servicemodel.parameters.NumericParameter;
import de.visiom.carpc.asb.servicemodel.parameters.Parameter;
import de.visiom.carpc.asb.servicemodel.parameters.SetParameter;
import de.visiom.carpc.asb.servicemodel.parameters.StateParameter;
import de.visiom.carpc.asb.servicemodel.parameters.StringParameter;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.SetValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StateValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.serviceregistry.ServiceRegistry;
import de.visiom.carpc.asb.serviceregistry.exceptions.NoSuchServiceException;
import de.visiom.carpc.services.tummitfahrer.notification.GetNotification;
import de.visiom.carpc.services.tummitfahrer.notification.HttpRequest;
import de.visiom.carpc.services.tummitfahrer.notification.UrlStore;
import de.visiom.carpc.services.tummitfahrer.notification.Utilities;


public class ParameterChangeRequestHandler extends ValueChangeRequestHandler {

	private static final String RECEIVE_LOG_MESSAGE = "Received a value change request for {},{}. Dispatching the request to the ValueStore...";
	
	private static final Logger LOG = LoggerFactory
	            .getLogger(ParameterChangeRequestHandler.class);
	
	private NumericParameter acceptParamter;
	private StringParameter notificationParamter;
	private SetParameter timelineSetParamter;
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
		
		Parameter parameter = request.getParameter();
		
		LOG.info(RECEIVE_LOG_MESSAGE, request.getParameter(), request.getValue());
		 
		boolean result = false;
		
		/*if(parameter.getService().getName().equals(Utilities.readConfigFile("acceptParameterServiceName"))
						&& parameter.getName().equals(Utilities.readConfigFile("numericAcceptParameterName")))			
		{			
			result = handleAccept(request);
		}
		else if(parameter.getService().getName().equals(Utilities.readConfigFile("declineParameterServiceName"))
						&& parameter.getName().equals(Utilities.readConfigFile("numericDeclineParameterName")))			
		{			
			result = handleDecline(request);
		}
		else*/
		{	
			//result = publishStringParameterChangeEvent(request);
			result = publishSetParameterChangeEvent(request);
		}
		
		int responseStatus = getResponseStatus(result);
		
        Response response = GenericResponse
                .createGenericResponse(responseStatus);
		
		commandPublisher.publishResponse(response, requestID, request
                .getParameter().getService());   
	}
	
	private boolean handleAccept(ValueChangeRequest request)
	{
		HttpRequest putRequest = new HttpRequest();
		try {
			//TODO: Parse the request to get the ID, Parse the ID to get your own ID i.e 04. Post response.
			String response = putRequest.sendPUTRequest("http://localhost:3000/api/v2/rides/67/requests/18?passenger_id=2", "1"); //TODO: 1 = dummy value remove it
			LOG.info("Response successfully sent:" + response);
		} catch (Exception e) {				
			LOG.info("Error occured while sending the PUT request", e);
		}
		
		LOG.info("Publishing accept parameter");
		return publishNumericParameterChangeEvent(request);		
	}
	
	private boolean handleDecline(ValueChangeRequest request)
	{
		//TODO: Handle this
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
		try {			
			tummitfahrerService = serviceRegistry.getService(Utilities.readConfigFile("setParameterServiceName"));
		
			timelineSetParamter = (SetParameter) tummitfahrerService.getParameter(Utilities.readConfigFile("setParameterName"));			
		
			Parameter typeParam = timelineSetParamter.getParameter("type");
			Parameter idParam = timelineSetParamter.getParameter("id");
			Parameter nameParam = timelineSetParamter.getParameter("name");
			Parameter addressParam = timelineSetParamter.getParameter("address");
			Parameter imageParam = timelineSetParamter.getParameter("image");
			
			//Get values from the request
			SetValueObject receivedRequest = (SetValueObject)request.getValue();
			Map<Parameter, ValueObject> requestValue = receivedRequest.getValue();
			
			StringValueObject typeValueObj = (StringValueObject) requestValue.get(typeParam);
			NumberValueObject idValueObj = (NumberValueObject) requestValue.get(idParam);
			StringValueObject nameValueObj = (StringValueObject) requestValue.get(nameParam);
			StringValueObject addressValueObj = (StringValueObject) requestValue.get(addressParam);
			StringValueObject imageValueObj = (StringValueObject) requestValue.get(imageParam);
			
			//Add the service number to the id
			int notificationID = Utilities.addServiceNumberToID(idValueObj.getValue().intValue());
			
			//TODO: Fix the URL. ASk David whether I can declare a new parameter named URL in the rest service.xml
			//Save the notificationId and url in the memory. Will be used later on when Accept/Decline will be called
			UrlStore.saveUrl(notificationID, Utilities.getCompleteTUMitfahrerServerURL("/url/test/123"));
			
			//Do the modifications to the input
			Map<Parameter, ValueObject> updates = new HashMap<Parameter, ValueObject>();
			updates.put(typeParam, typeValueObj);
			updates.put(idParam, NumberValueObject.valueOf(notificationID));
			updates.put(nameParam, nameValueObj);
			updates.put(addressParam, addressValueObj);
			updates.put(imageParam, imageValueObj);
			
			//Publish the result to bus
			ValueObject valueObject = SetValueObject.valueOf(updates);
			
			//TODO: Use SetValueObject once we have the implementation	        
			//ValueObject valueObject = generateFakeSetObject(request);

	        ValueChangeEvent valueChangeEvent = ValueChangeEvent
	                .createValueChangeEvent(timelineSetParamter, valueObject);
	        eventPublisher.publishValueChange(valueChangeEvent);
	        
	        return true;
        
		} catch (NoSuchServiceException e) {			
			LOG.info("Unable to find the service!", e);
			return false;
		}
		catch(NoSuchParameterException e)
		{
			LOG.info("Unable to find the parameter!", e);
			return false;
		}
	}
			
	
	
	/* This function publishes a change event on the bus. First it looks for the service and its parameter and then
	 * it pushes the event on this bus. 
	 * @param request It is the Change request. This change request was posted from the TUMitfahrer server using the
	 * PUT command.
	 * @return true = if event was published without errors. false = some exception was raised while accessing the service.
	 */
	private boolean publishStringParameterChangeEvent(ValueChangeRequest request)	
	{	
		Service tummitfahrerService;
		try {
			//TODO: Put the service name in some configuration file.
			tummitfahrerService = serviceRegistry.getService(Utilities.readConfigFile("stringParameterServiceName"));
		
			notificationParamter = (StringParameter) tummitfahrerService.getParameter(Utilities.readConfigFile("stringParameterName"));			
			
	        ValueObject valueObject = StringValueObject.valueOf(request.getValue());
	        ValueChangeEvent valueChangeEvent = ValueChangeEvent
	                .createValueChangeEvent(notificationParamter, valueObject);
	        eventPublisher.publishValueChange(valueChangeEvent);
	        
	        return true;
        
		} catch (NoSuchServiceException e) {			
			LOG.info("Unable to find the service!", e);
			return false;
		}
		catch(NoSuchParameterException e)
		{
			LOG.info("Unable to find the parameter!", e);
			return false;
		}
	}
		
	/* This function publishes a change event on the bus. First it looks for the service and its parameter and then
	 * it pushes the event on this bus. 
	 * @param request It is the Change request. This change request was posted from the TUMitfahrer server using the
	 * PUT command.
	 * @return true = if event was published without errors. false = some exception was raised while accessing the service.
	 */
	private boolean publishNumericParameterChangeEvent(ValueChangeRequest request)	
	{	
		Service tummitfahrerService;
		try {
			//TODO: Put the service name in some configuration file.
			tummitfahrerService = serviceRegistry.getService(Utilities.readConfigFile("acceptParameterServiceName"));
		
			acceptParamter = (NumericParameter) tummitfahrerService.getParameter(Utilities.readConfigFile("numericAcceptParameterName"));			
			
	        ValueObject valueObject = NumberValueObject.valueOf(Integer.parseInt(request.getValue().toString()));
	        ValueChangeEvent valueChangeEvent = ValueChangeEvent
	                .createValueChangeEvent(acceptParamter, valueObject);
	        eventPublisher.publishValueChange(valueChangeEvent);
	        
	        return true;
        
		} catch (NoSuchServiceException e) {			
			LOG.info("Unable to find the service!", e);
			return false;
		}
		catch(NoSuchParameterException e)
		{
			LOG.info("Unable to find the parameter!", e);
			return false;
		}
	}

	
	

	private boolean fakeHandleAcceptRequest(ValueChangeRequest request)
	{
		HttpRequest putRequest = new HttpRequest();
		try {
			String response = putRequest.sendPUTRequest("http://localhost:3000/api/v2/rides/67/requests/18?passenger_id=2", "1"); //TODO: 1 = dummy value remove it
			LOG.info("Response successfully sent:" + response);
		} catch (Exception e) {				
			LOG.info("Error occured while sending the PUT request", e);
		}
		
		LOG.info("Publishing accept parameter");
		return publishNumericParameterChangeEvent(request);	
	}
	
	/**
	 * TODO: REMOVE THIS function
	 * @throws ParseException 
	 * 
	 */
	private ValueObject generateFakeSetObject(ValueChangeRequest request)
	{	
		//Create a new request
		//Assign it the value
		//Use the value to get the SetvalueObject
		//Get ID from the request and attach the service id and put it back in the request
		//Save the URL received from the server in the hash map and clear the URL field.
		/*StringValueObject jsonString = (StringValueObject) request.getValue();
		LOG.info("JSON: => {}", Utilities.getValueFromJSON(jsonString.toString(), "image"));*/
		
		List<Parameter> list = timelineSetParamter.getParameters();
		StringParameter type = (StringParameter) list.get(0);
		NumericParameter id = (NumericParameter) list.get(1);
		StringParameter name = (StringParameter) list.get(2);
		StringParameter address = (StringParameter) list.get(3);
		/*StringParameter url = (StringParameter) list.get(4);*/
		StringParameter image = (StringParameter) list.get(4);
		/*StringParameter other = (StringParameter) list.get(6);*/
		/*StateParameter state = (StateParameter) list.get(5);*/
		
		int notificationId = Utilities.addServiceNumberToID(4);
		
		Map<Parameter,ValueObject> map = new HashMap<Parameter, ValueObject>();
		StringValueObject typeObj = StringValueObject.valueOf("TumMitfahrer");		
		NumberValueObject idObj = NumberValueObject.valueOf(notificationId);		
		StringValueObject nameObj = StringValueObject.valueOf("Driver Pickup Alert");
		StringValueObject addressObj = StringValueObject.valueOf("address");
		
		UrlStore.saveUrl(notificationId, "url/test/123"); //Save URL
		
		/*StringValueObject urlObj = StringValueObject.valueOf("");*/
		StringValueObject imageObj = StringValueObject.valueOf("image");
		/*StringValueObject otherObj = StringValueObject.valueOf("other");*/
		/*StateValueObject stateObj = StateValueObject.valueOf("state");*/
		
		map.put(type, typeObj);
		map.put(id, idObj);
		map.put(name, nameObj);
		map.put(address, addressObj);
		/*map.put(url, urlObj);*/
		map.put(image, imageObj);
		/*map.put(other, otherObj);*/
		/*map.put(state, stateObj);*/
			
		
		return SetValueObject.valueOf(map);			
	}
	
}
