package de.visiom.carpc.services.tummitfahrer.DummyPublish;
//package de.visiom.carpc.services.recommender.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.visiom.carpc.asb.messagebus.EventPublisher;
import de.visiom.carpc.asb.messagebus.events.ValueChangeEvent;
import de.visiom.carpc.asb.servicemodel.Service;
import de.visiom.carpc.asb.servicemodel.exceptions.NoSuchParameterException;
import de.visiom.carpc.asb.servicemodel.parameters.NumericParameter;
import de.visiom.carpc.asb.servicemodel.parameters.Parameter;
import de.visiom.carpc.asb.servicemodel.parameters.StringParameter;
import de.visiom.carpc.asb.servicemodel.valueobjects.NumberValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.StringValueObject;
import de.visiom.carpc.asb.servicemodel.valueobjects.ValueObject;
import de.visiom.carpc.asb.serviceregistry.ServiceRegistry;
import de.visiom.carpc.asb.serviceregistry.exceptions.NoSuchServiceException;

public class DummyPublisher {

	private EventPublisher eventPublisher;
	private ServiceRegistry serviceRegistry;

	public void publishDummy (){
		
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter something: ");
		String zeile = null;
		try {
			while ((zeile = console.readLine()) != null) {
				System.out.println("Read Line: " + zeile);
				String[] result = zeile.split("=");
				if (result.length == 2){
					if (result[1].contains("."))
						publish (result[0], Double.valueOf(result[1]));
					else
						publish (result[0], result[1]);
				} else {
					System.out.println("Incorrect Input");
				}
			}
		} catch (IOException e) {
			// Sollte eigentlich nie passieren
			e.printStackTrace();
		}
		
	}
	
	public void publish(String parameterName,
			String parameterValue) {

		try {
			Service service = serviceRegistry.getService("recommender");
			Parameter parameter = (StringParameter) service
					.getParameter(parameterName);
			ValueObject value = StringValueObject.valueOf(parameterValue);
			ValueChangeEvent valueChangeEvent = ValueChangeEvent
					.createValueChangeEvent(parameter, value);
			eventPublisher.publishValueChange(valueChangeEvent);
		} catch (NoSuchServiceException e) {
			e.printStackTrace();
		} catch (NoSuchParameterException e) {
			System.out.println("Can't find this Parameter: " + parameterName);
			e.printStackTrace();
		}

	}
	
	public void publish(String parameterName,
			Double parameterValue) {

		try {
			Service service = serviceRegistry.getService("recommender");
			Parameter parameter = (NumericParameter) service
					.getParameter(parameterName);

			ValueObject value = NumberValueObject.valueOf(parameterValue);
			ValueChangeEvent valueChangeEvent = ValueChangeEvent
					.createValueChangeEvent(parameter, value);
			eventPublisher.publishValueChange(valueChangeEvent);
		} catch (NoSuchServiceException e) {
				e.printStackTrace();
		} catch (NoSuchParameterException e) {
			System.out.println("Can't find this Parameter: " + parameterName);
			e.printStackTrace();
		}

	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
}
