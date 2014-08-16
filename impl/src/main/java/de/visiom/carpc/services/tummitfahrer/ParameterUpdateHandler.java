package de.visiom.carpc.services.tummitfahrer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.messagebus.handlers.ValueChangeEventHandler;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.SetValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.servicemodel.parameters.Parameter;

public class ParameterUpdateHandler extends ValueChangeEventHandler {

    private static final Logger LOG = LoggerFactory
            .getLogger(ParameterUpdateHandler.class);
    
    @Override
    public void onValueChangeEvent(ValueChangeEvent valueChangeEvent) {
        /*NumberValueObject numberValueObject = (NumberValueObject) valueChangeEvent
                .getValue();
        Parameter parameter = valueChangeEvent.getParameter();
        Double value = numberValueObject.getValue().doubleValue();
        LOG.info("Received an update for {}/{}: {}", parameter.getName(), 
                parameter.getService().getName(), value);*/
        
    	
    	
        StringValueObject stringValueObject = (StringValueObject) valueChangeEvent
                .getValue();
        Parameter parameter = valueChangeEvent.getParameter();
        String value = stringValueObject.getValue();
        LOG.info("Received an update for {}/{}: {}", parameter.getName(), 
                parameter.getService().getName(), value);
    	
    	/*SetValueObject stringValueObject = (SetValueObject) valueChangeEvent.getValue();
        Parameter parameter = valueChangeEvent.getParameter();
        Map<Parameter, ValueObject> value = stringValueObject.getValue();
        LOG.info("Received an update for a {}/{}: {}", parameter.getName(), 
                parameter.getService().getName(), value.size());*/
        
    }
}
