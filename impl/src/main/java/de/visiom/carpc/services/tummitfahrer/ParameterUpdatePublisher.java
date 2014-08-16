package de.visiom.carpc.services.tummitfahrer;

import de.visiom.carpc.asb.rest.entities.NumericParameterEntity;

import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.async.ParallelWorker;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.parametervalueregistry.exceptions.UninitalizedValueException;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.exceptions.NoSuchParameterException;
import de.visiom.carpc.asb.servicemodel.parameters.NumericParameter;
import de.visiom.carpc.asb.servicemodel.parameters.StringParameter;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
//import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.serviceregistry.ServiceRegistry;
import de.visiom.carpc.asb.serviceregistry.exceptions.NoSuchServiceException;
import de.visiom.carpc.services.tummitfahrer.notification.*;

public class ParameterUpdatePublisher extends ParallelWorker {

	/*Changed by Behroz - Changed the name of the Parameter*/
    //private NumericParameter testParameter;
	private StringParameter notificationParamter;

    private EventPublisher eventPublisher;

    private ServiceRegistry serviceRegistry;

    private int currentValue;

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void initialize() throws NoSuchParameterException,
            NoSuchServiceException, UninitalizedValueException {
        Service tummitfahrerService = serviceRegistry.getService("tummitfahrer");
        
        /*Changed by Behroz - Changed the name of the parameter from test to notification*/
        //testParameter = (NumericParameter) tummitfahrerService.getParameter("test");
        notificationParamter = (StringParameter) tummitfahrerService.getParameter("notification");
        
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
        
        GetNotification notification = new GetNotification();
        ValueObject valueObject = StringValueObject.valueOf(notification.getNotificationData());
        ValueChangeEvent valueChangeEvent = ValueChangeEvent
                .createValueChangeEvent(notificationParamter, valueObject);
        eventPublisher.publishValueChange(valueChangeEvent);
    }

    }