package com.ha.manager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.acmestudio.basicmodel.core.AcmeStringValue;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeConnector;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.property.AcmeProperty;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentation;

import com.ha.input.ElementGetter;

//Get the configure information for HASA.
public class ConfigInfoManager {
	public void genConfigFile(AcmeSystem system, String path) throws IOException {	
		ElementGetter eg = new ElementGetter();
		Set<AcmeComponent> components = eg.getComponents(system);	
		Iterator<AcmeComponent> componentIt = components.iterator();
		String vipStr =null;
		AcmeConnector vrrpConnector = system.getConnector("VRRPCollaboration_Connector");
		if (vrrpConnector != null) {
			vipStr = ((AcmeStringValue)(vrrpConnector.getProperty("virtual_ip")).getValue()).getValue();
		}
		String vipStr2=null;
		AcmeConnector clusterConnector = system.getConnector("ClusterManagementCollaboration_Connector");
		if(clusterConnector!=null) {
			vipStr2= ((AcmeStringValue)(clusterConnector.getProperty("virtual_ip")).getValue()).getValue();
		}
		while (componentIt.hasNext()) {
			AcmeComponent component = componentIt.next();
			FileWriter fw = new FileWriter(path + "/" + component.getName());		
			StringBuilder sb = new StringBuilder();
			sb.append(component.getName() +" :\n" );
			
			Set <? extends AcmeRepresentation> representations = component.getRepresentations();
			if (representations.size() > 0) {
				sb.append("\n");
				Iterator<? extends AcmeRepresentation> representationsIterator = representations.iterator();
				while (representationsIterator.hasNext()) {
					AcmeRepresentation representation = representationsIterator.next();
					AcmeSystem innerSystem = representation.getSystem();
					Set <AcmeComponent> innerComponents = innerSystem.getComponents();
					Iterator <AcmeComponent> innerComponentsIterator = innerComponents.iterator();	
					while (innerComponentsIterator.hasNext()) {
						AcmeComponent innerComponent = innerComponentsIterator.next();
						sb.append(innerComponent.getName()+" :\n");
						Set<AcmeProperty> properties = innerComponent.getProperties();
						Iterator<AcmeProperty> propertiesIterator = properties.iterator();
						while (propertiesIterator.hasNext()) {
							AcmeProperty property = propertiesIterator.next();
							if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeStringValue")) {
								sb.append(property.getName() + "  : "+ ((AcmeStringValue)(property.getValue())).getValue() +"\n");
							}
						}
						if (innerComponent.getName().equals("Keepalived")) {
							sb.append("virtual_ip : " + vipStr + "\n");
							sb.append("realserver1_ip : " + ((AcmeStringValue)(vrrpConnector.getProperty("master_ip")).getValue()).getValue() + "\n");
							sb.append("realserver2_ip : " + ((AcmeStringValue)(vrrpConnector.getProperty("slave_ip")).getValue()).getValue() + "\n");
						}
						
						if (innerComponent.getName().equals("Pacemaker")) {
							sb.append("virtual_ip : " + vipStr2 + "\n");
						}
						sb.append("\n");
					}
					fw.write(sb.toString());
					fw.close();	
				}
			} else {
				Set<AcmeProperty> properties = component.getProperties();
				Iterator<AcmeProperty> propertiesIterator = properties.iterator();
				while (propertiesIterator.hasNext()) {
					AcmeProperty property = propertiesIterator.next();
					if (property.getValue().getClass().getName().equals("org.acmestudio.basicmodel.core.AcmeStringValue")) {
						sb.append(property.getName() + " : "+ ((AcmeStringValue)(property.getValue())).getValue() +"\n");
					}
				}
				fw.write(sb.toString());
				fw.close();	
			}	
		}    
		return;
	}
}
