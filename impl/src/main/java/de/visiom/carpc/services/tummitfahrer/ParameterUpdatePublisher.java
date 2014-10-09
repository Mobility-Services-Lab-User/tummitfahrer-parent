package de.visiom.carpc.services.tummitfahrer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.visiom.carpc.asb.rest.entities.NumericParameterEntity;
import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.async.ParallelWorker;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.parametervalueregistry.exceptions.UninitalizedValueException;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.exceptions.NoSuchParameterException;
import de.visiom.carpc.asb.servicemodel.parameters.NumericParameter;
import de.visiom.carpc.asb.servicemodel.parameters.SetParameter;
import de.visiom.carpc.asb.servicemodel.parameters.StringParameter;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.SetValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StateValueObject;
//import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.serviceregistry.ServiceRegistry;
import de.visiom.carpc.asb.serviceregistry.exceptions.NoSuchServiceException;
import de.visiom.carpc.services.tummitfahrer.notification.*;
import de.visiom.carpc.asb.servicemodel.parameters.Parameter;
import de.visiom.carpc.asb.parametervalueregistry.ParameterValueRegistry;

public class ParameterUpdatePublisher extends ParallelWorker {

	private static final Logger LOG = LoggerFactory
			.getLogger(ParameterUpdatePublisher.class);
	/*Changed by Behroz - Changed the name of the Parameter*/
    //private NumericParameter testParameter;
	//private StringParameter notificationParamter;
	private SetParameter notificationParamter;

    private EventPublisher eventPublisher;

    private ServiceRegistry serviceRegistry;

    private ParameterValueRegistry parameterValueRegistry;
    
    private int currentValue;

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    public void setParameterValueRegistry(
			ParameterValueRegistry parameterValueRegistry) {
		this.parameterValueRegistry = parameterValueRegistry;
	}

    @Override
    public void initialize() throws NoSuchParameterException,
            NoSuchServiceException, UninitalizedValueException {
        //Service tummitfahrerService = serviceRegistry.getService("tummitfahrer");
    	Service tummitfahrerService = serviceRegistry.getService(Utilities.readConfigFile("setParameterServiceName"));
        
        /*Changed by Behroz - Changed the name of the parameter from test to notification*/
        //testParameter = (NumericParameter) tummitfahrerService.getParameter("test");
        //notificationParamter = (StringParameter) tummitfahrerService.getParameter("notification");
        notificationParamter = (SetParameter) tummitfahrerService.getParameter(Utilities.readConfigFile("setParameterName"));
        
        //resetCurrentValue();
    }

    @Override
	public long getExecutionInterval() {
        return 5000;    	
    }

    @Override
    public void execute() {
    	/*ValueObject valueObject = NumberValueObject.valueOf(currentValue++);
        ValueChangeEvent valueChangeEvent = ValueChangeEvent
                .createValueChangeEvent(testParameter, valueObject);
        eventPublisher.publishValueChange(valueChangeEvent);*/
        
        
        /*if (currentValueIsTooBig()) {
            resetCurrentValue();
        }*/
        
        /*GetNotification notification = new GetNotification();
        ValueObject valueObject = StringValueObject.valueOf(notification.getNotificationData());
        ValueChangeEvent valueChangeEvent = ValueChangeEvent
                .createValueChangeEvent(notificationParamter, valueObject);
        eventPublisher.publishValueChange(valueChangeEvent);*/
    	
    	//Get Current set parameter and publish it
    	//TODO: If this handler is not required then remove it.
    	Map<Parameter, ValueObject> currentValueObjects;
    	/*try
    	{*/
    		//LOG.info("\n Notification Parameter: => {}", notificationParamter.getParameters().size() );
    		//currentValueObjects = ((SetValueObject) parameterValueRegistry.getValue(notificationParamter)).getValue();
    	
    		//Adding dummy data
    		List<Parameter> parameters = notificationParamter.getParameters();
			Map<Parameter, ValueObject> newValues = new HashMap<Parameter, ValueObject>();
			Parameter param0 = parameters.get(0);
			Parameter param1 = parameters.get(1);
			Parameter param2 = parameters.get(2);
			Parameter param3 = parameters.get(3);
			Parameter param4 = parameters.get(4);
			/*Parameter param5 = parameters.get(5);*/
			
			//LOG.info("\n Parameter Size: => {}", parameters.size());
			
			newValues.put(param0, StringValueObject.valueOf("Driver Pickup Alert"));
			newValues.put(param1, NumberValueObject.valueOf(5));
			newValues.put(param2, StringValueObject.valueOf("Name"));
			newValues.put(param3, StringValueObject.valueOf("Address"));
			newValues.put(param4, StringValueObject.valueOf("Image URL"));
			/*newValues.put(param5, StateValueObject.valueOf("State"));*/
					
			
	    	ValueObject newValueObject = SetValueObject.valueOf(newValues);
			ValueChangeEvent valueChangeEvent = ValueChangeEvent
					.createValueChangeEvent(notificationParamter, newValueObject);
			eventPublisher.publishValueChange(valueChangeEvent);
    	/*} 
    	catch (UninitalizedValueException e) {
    		e.printStackTrace();
    	}*/
    }

    }