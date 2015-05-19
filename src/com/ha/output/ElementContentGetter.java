package com.ha.output;


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
import org.acmestudio.basicmodel.element.AcmePort;
import org.acmestudio.basicmodel.element.AcmeRole;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.property.AcmeProperty;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentation;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentationBinding;


/*
 * Get each type of element content of the system to String. 
 */
public class ElementContentGetter {
	public String getPropertyStringValueContent(AcmeProperty property, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Property " + property.getName() + " : string = \"" +((AcmeStringValue)(property.getValue())).getValue() +"\";\n");
		str.append("\n");
		return str.toString();
	}
	
	public String getPropertySetValueContent(AcmeProperty property, String indentation) {
		StringBuilder str = new StringBuilder();
		AcmeSetValue value = (AcmeSetValue)property.getValue();
		Set<? extends IAcmePropertyValue> valueSet = value.getValues();
		Iterator<? extends IAcmePropertyValue> it = valueSet.iterator();
		str.append(indentation + "Property " + property.getName() +" = {");
		while (it.hasNext()) {
			AcmePropertyValue tmpValue = (AcmePropertyValue)it.next();
			String keyString = ((AcmeRecordValue)(tmpValue)).getFields().iterator().next().getName();
			String valueString = ((AcmeStringValue)(((AcmeRecordValue)(tmpValue)).getFields().iterator().next().getValue())).getValue();
			str.append("[" + keyString + " = \"" + valueString + "\";],");
		}
		str.replace(str.length()-1, str.length(), "");
		str.append("};\n");
		str.append("\n");
		return str.toString();
	}
	
	public String getPortContent(AcmePort port, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Port " + port.getName() + " = {\n");
		str.append("\n");
		str.append(indentation + "}\n");
		return str.toString();
	}
	
	//component has no representations 
	public String getSimpleComponentContent(AcmeComponent component, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Component " + component.getName() + " = {\n");
		Set<AcmePort> ports = component.getPorts();
		Iterator<AcmePort> portsIterator = ports.iterator();
		while (portsIterator.hasNext()) {
			AcmePort port = portsIterator.next();
			str.append(getPortContent(port, indentation + "    "));
		}
		Set<AcmeProperty> properties = component.getProperties();
		
		Iterator<AcmeProperty> propertiesIterator = properties.iterator();
		while (propertiesIterator.hasNext()) {
			AcmeProperty property = propertiesIterator.next();
			if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeStringValue")) {
				str.append(getPropertyStringValueContent(property, indentation + "    "));
			} else if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeSetValue")) {
				str.append(getPropertySetValueContent(property, indentation + "    "));
			}
		}
		str.append(indentation + "}\n");
		return str.toString();
	}
	
	public String getRoleContent(AcmeRole role, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Role " + role.getName() + " = {\n");
		str.append("\n");
		str.append(indentation + "}\n");
		return str.toString();
	}
	
	public String getConnectorContent(AcmeConnector connector, String indentation) {
		 StringBuilder str = new StringBuilder();
		 str.append(indentation + "Connector " + connector.getName() + " = {\n");
		 Set<AcmeRole> roles = connector.getRoles();
		 Iterator<AcmeRole> rolesIterator = roles.iterator();
		 while (rolesIterator.hasNext()) {
			 AcmeRole role = rolesIterator.next();
			 str.append(getRoleContent(role, indentation + "    "));
		 }
		Set<AcmeProperty> properties = connector.getProperties();
		Iterator<AcmeProperty> propertiesIterator = properties.iterator();
		while (propertiesIterator.hasNext()) {
			AcmeProperty property = propertiesIterator.next();
			if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeStringValue")) {
				str.append(getPropertyStringValueContent(property, indentation + "    "));
			} else if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeSetValue")) {
				str.append(getPropertySetValueContent(property, indentation + "    "));
			}
		}
		str.append(indentation + "}\n");
		return str.toString();		 
	}
	
	public String getAttachmentContent(AcmeAttachment attachment, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Attachment " + attachment.getName() + ";\n");
		return str.toString();
	}
	
	public String getRepresentationContent(AcmeRepresentation representation, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Representation " + representation.getName() + " = {\n");
		AcmeSystem innerSystem = representation.getSystem();
		str.append(indentation + "    " + "System " + innerSystem.getName() + "  = {\n");
		Set <AcmeComponent> innerComponents = innerSystem.getComponents();
		Iterator <AcmeComponent> innerComponentsIterator = innerComponents.iterator();
		while (innerComponentsIterator.hasNext()) {
			AcmeComponent component = innerComponentsIterator.next();
			str.append(getSimpleComponentContent(component, indentation + "        "));
		}
		
		Set <AcmeConnector> innerConnectors = innerSystem.getConnectors();
		Iterator <AcmeConnector> innerConnectorIterator = innerConnectors.iterator();
		while (innerConnectorIterator.hasNext()) {
			AcmeConnector connector = innerConnectorIterator.next();
			str.append(getConnectorContent(connector, indentation+"        "));
		}
		
		Set <AcmeAttachment> innerAttachments = innerSystem.getAttachments();
		Iterator <AcmeAttachment> innerAttachmentsIterator = innerAttachments.iterator();
		while (innerAttachmentsIterator.hasNext()) {
			AcmeAttachment attachment = innerAttachmentsIterator.next();
			str.append(getAttachmentContent(attachment, indentation+"        "));
		}
		str.append(indentation + "    }\n");
		
		Set <? extends AcmeRepresentationBinding> bindings = representation.getBindings();
		Iterator<? extends AcmeRepresentationBinding> bindingsIterator = bindings.iterator();
		str.append(indentation + "    " + "Bindings {\n");
		while (bindingsIterator.hasNext()) {
			AcmeRepresentationBinding binding = bindingsIterator.next();
			str.append(indentation + "        " + binding.getOuterReference().getReferencedName()+" to "+binding.getInnerReference().getReferencedName() +";\n");	
		}
		str.append(indentation + "    }\n");
		str.append(indentation + "}\n");
		return str.toString();
	}
	
	//component has representations 
	public String getCompoundComponentContent(AcmeComponent component, String indentation) {
		StringBuilder str = new StringBuilder();
		str.append(indentation + "Component " + component.getName() +  " = {\n");
		Set <AcmePort> ports = component.getPorts();
		Iterator<AcmePort> portsIterator = ports.iterator();
		while(portsIterator.hasNext()) {
			AcmePort port = portsIterator.next();
			str.append(getPortContent(port, indentation + "    "));
		}
		
		Set <? extends AcmeRepresentation> representations = component.getRepresentations();
		Iterator<? extends AcmeRepresentation> representationsIterator = representations.iterator();
		while (representationsIterator.hasNext()) {
			AcmeRepresentation representation = representationsIterator.next();
			str.append(getRepresentationContent(representation, indentation+"    "));
		}
		str.append(indentation + "}\n");
		return str.toString();
	}
}
