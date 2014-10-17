package de.visiom.carpc.services.tummitfahrer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.async.ParallelWorker;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.parametervalueregistry.exceptions.UninitalizedValueException;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.exceptions.NoSuchParameterException;
import de.visiom.carpc.asb.servicemodel.parameters.SetParameter;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.SetValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StateValueObject;
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
	
	private SetParameter notificationParamter;
	private SetParameter timelineEventParamter;
	

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
    	
    	setMaximumNumberOfExecutions(0);
        
    	Service tummitfahrerService = serviceRegistry.getService(Utilities.readConfigFile("notificationSetParameterServiceName"));
    	Service recommenderService = serviceRegistry.getService(Utilities.readConfigFile("setParameterServiceName"));
        
        notificationParamter = (SetParameter) tummitfahrerService.getParameter(Utilities.readConfigFile("timelineDataSetParameterName"));
        timelineEventParamter = (SetParameter) recommenderService.getParameter("timelineEvent");        
    }

    @Override
	public long getExecutionInterval() {
        return 5000;    	
    }

    @Override
    public void execute() {
    	
    	//Get Current set parameter and publish it
    	//TODO: If this handler is not required then remove it.
    	Map<Parameter, ValueObject> currentValueObjects;
    	
		//Adding dummy data
		List<Parameter> parameters = notificationParamter.getParameters();
		Map<Parameter, ValueObject> newValues = new HashMap<Parameter, ValueObject>();
		Parameter param0 = parameters.get(0);
		Parameter param1 = parameters.get(1);
		Parameter param2 = parameters.get(2);
		Parameter param3 = parameters.get(3);
		Parameter param4 = parameters.get(4);
		Parameter param5 = parameters.get(5);
		Parameter param6 = parameters.get(6);
		Parameter param7 = parameters.get(7);
		/*Parameter param5 = parameters.get(5);*/
		
		//LOG.info("\n Parameter Size: => {}", parameters.size());/
		LOG.info("Initializing the paramters");
		
		newValues.put(param0, StringValueObject.valueOf(""));
		newValues.put(param1, NumberValueObject.valueOf(0));
		newValues.put(param2, StringValueObject.valueOf(""));
		newValues.put(param3, StringValueObject.valueOf(""));
		newValues.put(param4, StringValueObject.valueOf(""));
		newValues.put(param5, StringValueObject.valueOf(""));
		newValues.put(param6, StringValueObject.valueOf(""));
		newValues.put(param7, StringValueObject.valueOf(""));
		/*newValues.put(param5, StateValueObject.valueOf("State"));*/
				
		
    	ValueObject newValueObject = SetValueObject.valueOf(newValues);
		ValueChangeEvent valueChangeEvent = ValueChangeEvent
				.createValueChangeEvent(notificationParamter, newValueObject);
		eventPublisher.publishValueChange(valueChangeEvent);
		
			
		//TODO: Remove them once we have integrated everything
		initializeParameters(timelineEventParamter);
			
    }
    
    private void initializeParameters(SetParameter param)
    {
    	List<Parameter> parameters = param.getParameters();
		Map<Parameter, ValueObject> newValues = new HashMap<Parameter, ValueObject>();
		Parameter param0 = parameters.get(0);
		Parameter param1 = parameters.get(1);
		Parameter param2 = parameters.get(2);
		Parameter param3 = parameters.get(3);
		Parameter param4 = parameters.get(4);
		Parameter param5 = parameters.get(5);
		
		
		newValues.put(param0, StringValueObject.valueOf(""));
		newValues.put(param1, NumberValueObject.valueOf(0));
		newValues.put(param2, StringValueObject.valueOf(""));
		newValues.put(param3, StringValueObject.valueOf(""));
		newValues.put(param4, StringValueObject.valueOf(""));
		newValues.put(param5, StateValueObject.valueOf(""));
		
		ValueObject newValueObject = SetValueObject.valueOf(newValues);
		ValueChangeEvent valueChangeEvent = ValueChangeEvent
				.createValueChangeEvent(param, newValueObject);
		eventPublisher.publishValueChange(valueChangeEvent);
    }

    }