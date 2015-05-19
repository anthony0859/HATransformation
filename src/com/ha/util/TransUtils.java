package com.ha.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.acmestudio.basicmodel.core.AcmeStringValue;
import org.acmestudio.basicmodel.element.AcmeAttachment;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeConnector;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.property.AcmeProperty;


//Set of operations for get related element data about this component from target system. 
public class TransUtils {
	// Get set of connectors for which this component plays role as a requestee.
	public Set<AcmeConnector> getRequesteeConnectors(AcmeComponent component, AcmeSystem system) {
		Set<AcmeConnector> asRequesteeConnectors = new HashSet<AcmeConnector>();
		
		Set <AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeAttachment> attachmentIterator = attachments.iterator();
		StringOperation strOperation = new StringOperation();
		while (attachmentIterator.hasNext()) {
			AcmeAttachment attachment = attachmentIterator.next();
			String portString = attachment.getReferencedPortName();
			String roleString = attachment.getReferencedRoleName();
			if ((strOperation.getStrBeforeDot(portString)).equals(component.getName()) && (strOperation.getStrAfterDot(roleString)).equals("requestee")) {
				String connectorName = strOperation.getStrBeforeDot(roleString);
				asRequesteeConnectors.add(system.getConnector(connectorName));
			}
		}
		return asRequesteeConnectors;
	}
	
	// Get set of connectors for which this component plays role as a requestor.
	public Set<AcmeConnector> getRequestorConnectors(AcmeComponent component, AcmeSystem system) {
		Set<AcmeConnector> asRequestorConnectors = new HashSet<AcmeConnector>();
		
		Set <AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeAttachment> attachmentIterator = attachments.iterator();
		StringOperation strOperation = new StringOperation();
		while (attachmentIterator.hasNext()) {
			AcmeAttachment attachment = attachmentIterator.next();
			String portString = attachment.getReferencedPortName();
			String roleString = attachment.getReferencedRoleName();
			if ((strOperation.getStrBeforeDot(portString)).equals(component.getName()) && (strOperation.getStrAfterDot(roleString)).equals("requestor")) {
				String connectorName = strOperation.getStrBeforeDot(roleString);
				asRequestorConnectors.add(system.getConnector(connectorName));
			}
		}
		return asRequestorConnectors;
	}
	// Get config parameters for the component
	public Map<String, String> getConfigParameters(AcmeComponent component) {
		Map<String, String> configPara = new HashMap<String, String>();
		Set<AcmeProperty> properties = component.getProperties();
		Iterator<AcmeProperty> propertyIterator = properties.iterator();
		while (propertyIterator.hasNext()) {
			AcmeProperty property = propertyIterator.next();
			if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeStringValue")) {
				configPara.put(property.getName(), ((AcmeStringValue)(property.getValue())).getValue());
			}
		}
		return configPara;
	}
	
	
	// Get name of the port for the connector requestee role.
	public String getRequesteePortName(AcmeConnector connector, AcmeSystem system) {
		String requesteePortName = null;
		Set <AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeAttachment> attachmentIterator = attachments.iterator();
		while (attachmentIterator.hasNext()) {
			AcmeAttachment attachment = attachmentIterator.next();
			if ((attachment.getReferencedRoleName()).equals(connector.getName()+"."+"requestee")) {
				requesteePortName=attachment.getPort().getName();
			}
		}
		return requesteePortName;
	}
	
	// Get name of the port for the connector requestor role.
	public String getRequestorPortName(AcmeConnector connector, AcmeSystem system) {
		String requestorPorName = null;
		Set<AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeAttachment> attachmentIterator = attachments.iterator();
		while (attachmentIterator.hasNext()) {
			AcmeAttachment attachment = attachmentIterator.next();
			if ((attachment.getReferencedRoleName()).equals(connector.getName()+"."+"requestor")) {
				requestorPorName = attachment.getPort().getName();
			}
		}
		return requestorPorName;
	}
	
	// Get port names which play requestor role for asRequestorConnectors
	public Set<String> getRequestorPortNames(AcmeComponent component, Set<AcmeConnector> asRequestorConnectors, AcmeSystem system) {
		Set<String> asRequestorPortNames = new HashSet<String>();
		Set<AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeConnector> it = asRequestorConnectors.iterator();
		while (it.hasNext()) {
			AcmeConnector connector = it.next();
			Iterator<AcmeAttachment> attachmentsIterator = attachments.iterator();
			while (attachmentsIterator.hasNext()) {
				AcmeAttachment attachment = attachmentsIterator.next();
				if ((attachment.getReferencedRoleName()).equals(connector.getName()+".requestor")) {
					asRequestorPortNames.add(attachment.getPort().getName());
				}
			}
		}
		return asRequestorPortNames;
	}
	
	// Get map of connector name and port name.
	public Map<String, String> getRequeestorPortMap (AcmeComponent component, Set<AcmeConnector> asRequestorConnectors, AcmeSystem system) {
		Map<String, String> asRequestorPortMap = new HashMap<String, String>();
		Set<AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeConnector> it = asRequestorConnectors.iterator();

		while (it.hasNext()) {
			AcmeConnector connector = it.next();
			Iterator<AcmeAttachment> attachmentsIterator = attachments.iterator();
			while (attachmentsIterator.hasNext()) {
				AcmeAttachment attachment = attachmentsIterator.next();
				if ((attachment.getReferencedRoleName()).equals(connector.getName()+".requestor")) {
					asRequestorPortMap.put(connector.getName(), attachment.getPort().getName());
				}
			}
		}
		return asRequestorPortMap;
	}	
}
