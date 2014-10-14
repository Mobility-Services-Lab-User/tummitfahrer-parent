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
import de.visiom.carpc.services.tummitfahrer.notification.UrlStore;
import de.visiom.carpc.services.tummitfahrer.notification.Utilities;


public class ParameterChangeRequestHandler extends ValueChangeRequestHandler {

	private static final String RECEIVE_LOG_MESSAGE = "Received a value change request for {},{}. Dispatching the request to the ValueStore...";
	
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
		
		LOG.info(RECEIVE_LOG_MESSAGE, request.getParameter(), request.getValue());
		 
		boolean result = false;
		result = publishSetParameterChangeEvent(request);
		
		int responseStatus = getResponseStatus(result);
		
        Response response = GenericResponse
                .createGenericResponse(responseStatus);
		
		commandPublisher.publishResponse(response, requestID, request
                .getParameter().getService());   
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
			LOG.info("Unable to find the service!", e);
			return false;
		}
		catch(NoSuchParameterException e)
		{
			LOG.info("Unable to find the parameter!", e);
			return false;
		}
	}
			
}
