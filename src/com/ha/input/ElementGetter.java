package com.ha.input;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.basicmodel.core.AcmePropertyValue;
import org.acmestudio.basicmodel.core.AcmeRecordValue;
import org.acmestudio.basicmodel.core.AcmeSetValue;
import org.acmestudio.basicmodel.core.AcmeStringValue;
import org.acmestudio.basicmodel.element.AcmeAttachment;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeConnector;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.property.AcmeProperty;
import org.acmestudio.basicmodel.model.AcmeModel;


/*
 * Utility tools used to get element value of AcmeModel & AcmeSystem & AcmeComponent.
 */
public class ElementGetter {
	public AcmeSystem getSystem(AcmeModel model) {
		return model.getSystems().iterator().next();
	}
	
	public Set<AcmeComponent> getComponents(AcmeSystem system) {
		return system.getComponents();
	}
	
	public Set<AcmeConnector> getConnectors(AcmeSystem system) {
		return system.getConnectors();
	}
	
	public Set<AcmeAttachment> getAcmeAttachments(AcmeSystem system) {
		return system.getAttachments();
	}
	
	
	// Get parameters for HA solutions.
	public HashMap<String, String> getHAParams (AcmeComponent component) {
		HashMap<String, String> HAParamsMap = new HashMap<String, String>();

		Set <AcmeProperty> properties = component.getProperties();
		Iterator<AcmeProperty> propertiesIterator = properties.iterator();
		
		while (propertiesIterator.hasNext()) {
			AcmeProperty property = propertiesIterator.next();				
			if (property.getName().equals("HAParams")) {
				AcmeSetValue value = (AcmeSetValue)property.getValue();
				Set <? extends IAcmePropertyValue> valueSet = value.getValues();
				Iterator <? extends IAcmePropertyValue> it = valueSet.iterator();
				while (it.hasNext()) {
					AcmePropertyValue tmpValue = (AcmePropertyValue)it.next();
					String keyString = ((AcmeRecordValue)(tmpValue)).getFields().iterator().next().getName();
					String valueString = ((AcmeStringValue)(((AcmeRecordValue)(tmpValue)).getFields().iterator().next().getValue())).getValue();
					HAParamsMap.put(keyString, valueString);
				}
			}
		}
		return HAParamsMap;
	}	
}
