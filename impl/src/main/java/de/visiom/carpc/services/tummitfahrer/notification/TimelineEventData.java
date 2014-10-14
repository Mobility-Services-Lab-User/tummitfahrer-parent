package de.visiom.carpc.services.tummitfahrer.notification;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.visiom.carpc.asb.messagebus.commands.ValueChangeRequest;
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
import de.visiom.carpc.services.tummitfahrer.ParameterChangeRequestHandler;

public class TimelineEventData {

	private static final Logger LOG = LoggerFactory
            .getLogger(TimelineEventData.class);
	public String type;
	public Integer id;
	public String name;
	public String address;
	public String image;
	public String state;
	
	public TimelineEventData()
	{
		type = "TUMitfahrer";
		id = 0;
		name = "";
		image = "";
		state = "";
	}
	
	public TimelineEventData processRequest(ValueChangeRequest request, ServiceRegistry serviceRegistry, String parameterName)
	{
		Service service;
		SetParameter params;
		try {			
			
			// 1 - Fetch Service and Parameter
			service = serviceRegistry.getService(Utilities.readConfigFile("setParameterServiceName"));		
			params = (SetParameter) service.getParameter(Utilities.readConfigFile(parameterName));
		
			SetValueObject receivedRequest = (SetValueObject)request.getValue();
			Map<Parameter, ValueObject> requestValue = receivedRequest.getValue();
			
			//2 - Fetch the parameters
			Parameter typeParam = params.getParameter("type");
			Parameter idParam = params.getParameter("id");
			Parameter nameParam = params.getParameter("name");
			Parameter addressParam = params.getParameter("address");
			Parameter imageParam = params.getParameter("image");
			
			//3 - Fetch Data
			StringValueObject typeValueObj = (StringValueObject) requestValue.get(typeParam);
			NumberValueObject idValueObj = (NumberValueObject) requestValue.get(idParam);
			StringValueObject nameValueObj = (StringValueObject) requestValue.get(nameParam);
			StringValueObject addressValueObj = (StringValueObject) requestValue.get(addressParam);
			StringValueObject imageValueObj = (StringValueObject) requestValue.get(imageParam);
			
			this.type = typeValueObj.getValue();
			this.id = (Integer) idValueObj.getValue();
			this.name =  nameValueObj.getValue();
			this.address =  addressValueObj.getValue();
			this.image = imageValueObj.getValue();					
			
		}
		catch (NoSuchServiceException e) {			
			LOG.info("Unable to find the service!", e);
			//return false;
		}
		catch(NoSuchParameterException e)
		{
			LOG.info("Unable to find the parameter!", e);					
		}
		return this;
	}
	
	public TimelineEventData processValueChange(ValueChangeEvent request)
	{
		Service service;
		SetParameter params;
		try {			
			
			// 1 - Fetch Service and Parameter
			//service = serviceRegistry.getService(Utilities.readConfigFile("setParameterServiceName"));		
			//params = (SetParameter) service.getParameter(Utilities.readConfigFile("timelineSetParameterName"));
			params = (SetParameter) request.getParameter();
		
			SetValueObject receivedRequest = (SetValueObject)request.getValue();
			Map<Parameter, ValueObject> requestValue = receivedRequest.getValue();
			
			//2 - Fetch the parameters
			Parameter typeParam = params.getParameter("type");
			Parameter idParam = params.getParameter("id");
			Parameter nameParam = params.getParameter("name");
			Parameter addressParam = params.getParameter("address");
			Parameter imageParam = params.getParameter("image");
			Parameter stateParam = params.getParameter("state");
			
			//3 - Fetch Data
			StringValueObject typeValueObj = (StringValueObject) requestValue.get(typeParam);
			NumberValueObject idValueObj = (NumberValueObject) requestValue.get(idParam);
			StringValueObject nameValueObj = (StringValueObject) requestValue.get(nameParam);
			StringValueObject addressValueObj = (StringValueObject) requestValue.get(addressParam);
			StringValueObject imageValueObj = (StringValueObject) requestValue.get(imageParam);
			StateValueObject stateValueObj = (StateValueObject) requestValue.get(stateParam);
			
			this.type = typeValueObj.getValue();
			this.id = (Integer) idValueObj.getValue();
			this.name =  nameValueObj.getValue();
			this.address =  addressValueObj.getValue();
			this.image = imageValueObj.getValue();		
			this.state = stateValueObj.getValue();
						
		}		
		catch(NoSuchParameterException e)
		{
			LOG.info("Unable to find the parameter!", e);					
		}
		return this;
	}
}
