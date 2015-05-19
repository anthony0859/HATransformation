package com.ha.solution;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.acmestudio.acme.core.IAcmeType;
import org.acmestudio.acme.core.IAcmeTypedObject;
import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.acme.core.type.IAcmeStringType;
import org.acmestudio.acme.model.DefaultAcmeModel;
import org.acmestudio.basicmodel.core.AcmeStringValue;
import org.acmestudio.basicmodel.element.AcmeAttachment;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeConnector;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.property.AcmeProperty;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentation;

import com.ha.util.TransUtils;


/*
 * High availability transformation solution for stateless service in active/active mode.
 */

public class StatelessAASolution extends Solution{
	public void haTrans(AcmeComponent component, AcmeSystem system) throws Exception{
		System.out.println("StatelessAA");
			
		String componentName = component.getName();
		System.out.println("Component: " + componentName);
		    
		TransUtils transUtils = new TransUtils();
		Set<AcmeConnector> asRequesteeConnectors = transUtils.getRequesteeConnectors(component, system);
		Iterator<AcmeConnector> asRequesteeConnectorIterator = asRequesteeConnectors.iterator(); 
		
		Set<AcmeConnector> asRequestorConnectors = transUtils.getRequestorConnectors(component, system);
		Iterator<AcmeConnector> asRequestorConnectorIterator = asRequestorConnectors.iterator(); 
		
		Set<String> requestorPortNames = transUtils.getRequestorPortNames(component, asRequestorConnectors, system);
		Map<String, String> requestorPortMap = transUtils.getRequeestorPortMap(component, asRequestorConnectors, system);
		
		Map<String, String> configParas = transUtils.getConfigParameters(component);

		//remove current component	
		system.removeComponent(component);
		
		//create new component
		AcmeComponent lvsEntity1 = system.createComponent("LVS-Entity1");
		lvsEntity1.createPort("collaborationPort");
		lvsEntity1.createPort("healthCheckPort");
		lvsEntity1.createPort("directorPort");
		
		AcmeRepresentation lvsEntity1Rep = lvsEntity1.createRepresentation(lvsEntity1.getName()+"_Rep");
		AcmeSystem lvsEntity1RepSystem = lvsEntity1Rep.getSystem();
		
		AcmeComponent keepalived = lvsEntity1RepSystem.createComponent("Keepalived");
		keepalived.createPort("collaborationPort");
		keepalived.createPort("healthCheckPort");
		keepalived.createPort("bindingPort");
	    
	    Iterator configParasIt = configParas.entrySet().iterator();
	    while (configParasIt.hasNext()) {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)configParasIt.next();
	    	String key = entry.getKey();
	    	if (key.equals("ServiceIP"))
	    		continue;
	    	String value = entry.getValue();
	    	AcmeProperty property = keepalived.createProperty(key);
	    	property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    	property.setValue(new AcmeStringValue(value));
	    }
		
	    AcmeProperty stateProperty = keepalived.createProperty("state");
    	stateProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
    	stateProperty.setValue(new AcmeStringValue("MASTER"));
	    
    	AcmeProperty priorityProperty = keepalived.createProperty("priority");
    	priorityProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
    	priorityProperty.setValue(new AcmeStringValue("100"));
    	
    	AcmeProperty lb_algoProperty = keepalived.createProperty("lb_algo");
    	lb_algoProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
    	lb_algoProperty.setValue(new AcmeStringValue("rr"));
    	
    	AcmeProperty lb_kindProperty = keepalived.createProperty("lb_kind");
    	lb_kindProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
    	lb_kindProperty.setValue(new AcmeStringValue("DR"));
	    
		AcmeComponent lvs = lvsEntity1RepSystem.createComponent("LVS");
		lvs.createPort("bindingPort");
		lvs.createPort("directorPort");
		
		AcmeConnector bindingConnector = lvsEntity1RepSystem.createConnector("Binding_Connector");
		bindingConnector.createRole("requestor");
		bindingConnector.createRole("requestee");    
		
		lvsEntity1RepSystem.createAttachment(keepalived.getPort("bindingPort"), bindingConnector.getRole("requestor"));
		lvsEntity1RepSystem.createAttachment(lvs.getPort("bindingPort"), bindingConnector.getRole("requestee"));
 		
		lvsEntity1Rep.createBinding("collaborationPort", "Keepalived.collaborationPort");
		lvsEntity1Rep.createBinding("healthCheckPort", "Keepalived.healthCheckPort");
		lvsEntity1Rep.createBinding("directorPort", "LVS.directorPort");
		
		//create another new component
		AcmeComponent lvsEntity2 = system.createComponent("LVS-Entity2");
		lvsEntity2.createPort("collaborationPort");
		lvsEntity2.createPort("healthCheckPort");
		lvsEntity2.createPort("directorPort");
		AcmeRepresentation lvsEntity2Rep = lvsEntity2.createRepresentation(lvsEntity2.getName()+"_Rep");
		AcmeSystem lvsEntity2RepSystem = lvsEntity2Rep.getSystem();
		
		AcmeComponent keepalived2 = lvsEntity2RepSystem.createComponent("Keepalived");
		keepalived2.createPort("collaborationPort");
		keepalived2.createPort("healthCheckPort");
		keepalived2.createPort("bindingPort");
		
		Iterator configParasIt2 = configParas.entrySet().iterator();
	    while (configParasIt2.hasNext()) {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)configParasIt2.next();
	    	String key = entry.getKey();
	    	if (key.equals("ServiceIP"))
	    		continue;
	    	String value = entry.getValue();
	    	AcmeProperty property = keepalived2.createProperty(key);
	    	property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    	property.setValue(new AcmeStringValue(value));
	    }
		
	    AcmeProperty state2Property = keepalived2.createProperty("state");
    	state2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
    	state2Property.setValue(new AcmeStringValue("BACKUP"));
	    
    	AcmeProperty priority2Property = keepalived2.createProperty("priority");
    	priority2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
    	priority2Property.setValue(new AcmeStringValue("80"));
    	
    	AcmeProperty lb_algo2Property = keepalived2.createProperty("lb_algo");
    	lb_algo2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
    	lb_algo2Property.setValue(new AcmeStringValue("rr"));
    	
    	AcmeProperty lb_kind2Property = keepalived2.createProperty("lb_kind");
    	lb_kind2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
    	lb_kind2Property.setValue(new AcmeStringValue("DR"));
		
		AcmeComponent lvs2 = lvsEntity2RepSystem.createComponent("LVS");
		lvs2.createPort("bindingPort");
		lvs2.createPort("directorPort");
		
		AcmeConnector bindingConnector2 = lvsEntity2RepSystem.createConnector("Binding_Connector");
		bindingConnector2.createRole("requestor");
		bindingConnector2.createRole("requestee");
		
		lvsEntity2RepSystem.createAttachment(keepalived2.getPort("bindingPort"), bindingConnector2.getRole("requestor"));
		lvsEntity2RepSystem.createAttachment(lvs2.getPort("bindingPort"), bindingConnector2.getRole("requestee"));
 		
		lvsEntity2Rep.createBinding("collaborationPort", "Keepalived.collaborationPort");
		lvsEntity2Rep.createBinding("healthCheckPort", "Keepalived.healthCheckPort");
		lvsEntity2Rep.createBinding("directorPort", "LVS.directorPort");
		
		
		AcmeComponent newComponent=system.createComponent(componentName+"1");
		newComponent.createPort("healthCheckPort");
		newComponent.createPort("realServerPort");
		Iterator<String> requestorPortNamesIterator = requestorPortNames.iterator();
		while (requestorPortNamesIterator.hasNext()) {
			String str = requestorPortNamesIterator.next();
			newComponent.createPort(str);
		}
		Iterator configParasIt3 = configParas.entrySet().iterator();
	    while (configParasIt3.hasNext()) {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)configParasIt3.next();
	    	String key = entry.getKey();
	    	if (key.equals("ServiceIP")) {
	    		AcmeProperty property =newComponent.createProperty("virtual_ip");
	    		property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    		String value = entry.getValue();
		    	property.setValue(new AcmeStringValue(value));
	    		continue;
	    	}
	    	String value = entry.getValue();
	    	AcmeProperty property = newComponent.createProperty(key);
	    	property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    	property.setValue(new AcmeStringValue(value));
	    } 
	    
		AcmeComponent newComponent2=system.createComponent(componentName+"2");
		newComponent2.createPort("healthCheckPort");
		newComponent2.createPort("realServerPort");
		Iterator<String> requestorPortNamesIterator2 = requestorPortNames.iterator();
		while (requestorPortNamesIterator2.hasNext()) {
			String str = requestorPortNamesIterator2.next();
			newComponent2.createPort(str);
		}	   
		Iterator configParasIt4 = configParas.entrySet().iterator();
	    while (configParasIt4.hasNext()) {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)configParasIt4.next();
	    	String key = entry.getKey();
	    	if (key.equals("ServiceIP")) {
	    		AcmeProperty property =newComponent2.createProperty("virtual_ip");
	    		property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    		String value = entry.getValue();
		    	property.setValue(new AcmeStringValue(value));
	    		continue;
	    	}
	    	String value = entry.getValue();
	    	AcmeProperty property = newComponent2.createProperty(key);
	    	property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    	property.setValue(new AcmeStringValue(value));
	    }
		
		AcmeConnector healthCheckConnector1 = system.createConnector("HealthCheck_Connector1");
		healthCheckConnector1.createRole("monitor");
		healthCheckConnector1.createRole("monitored1");
		healthCheckConnector1.createRole("monitored2");
		AcmeConnector loadBalancingConnector1 = system.createConnector("LoadBalancing_Connector1");
		loadBalancingConnector1.createRole("director");
		loadBalancingConnector1.createRole("realServer1");
		loadBalancingConnector1.createRole("realServer2");
		AcmeProperty lb1_vipProperty = loadBalancingConnector1.createProperty("virtual_ip");
		lb1_vipProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
		lb1_vipProperty.setValue(new AcmeStringValue(configParas.get("ServiceIP")));
		
		AcmeConnector healthCheckConnector2 = system.createConnector("HealthCheck_Connector2");
		healthCheckConnector2.createRole("monitor");
		healthCheckConnector2.createRole("monitored1");
		healthCheckConnector2.createRole("monitored2");
		AcmeConnector loadBalancingConnector2 = system.createConnector("LoadBalancing_Connector2");
		loadBalancingConnector2.createRole("director");
		loadBalancingConnector2.createRole("realServer1");
		loadBalancingConnector2.createRole("realServer2");
		AcmeProperty lb2_vipProperty = loadBalancingConnector2.createProperty("virtual_ip");
		lb2_vipProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
		lb2_vipProperty.setValue(new AcmeStringValue(configParas.get("ServiceIP")));
		
		system.createAttachment(lvsEntity1.getPort("healthCheckPort"), healthCheckConnector1.getRole("monitor"));
		system.createAttachment(newComponent.getPort("healthCheckPort"), healthCheckConnector1.getRole("monitored1"));
		system.createAttachment(newComponent2.getPort("healthCheckPort"), healthCheckConnector1.getRole("monitored2"));
		
		system.createAttachment(lvsEntity2.getPort("healthCheckPort"), healthCheckConnector2.getRole("monitor"));
		system.createAttachment(newComponent.getPort("healthCheckPort"), healthCheckConnector2.getRole("monitored1"));
		system.createAttachment(newComponent2.getPort("healthCheckPort"), healthCheckConnector2.getRole("monitored2"));
		
		system.createAttachment(lvsEntity1.getPort("directorPort"), loadBalancingConnector1.getRole("director"));
		system.createAttachment(newComponent.getPort("realServerPort"), loadBalancingConnector1.getRole("realServer1"));
		system.createAttachment(newComponent2.getPort("realServerPort"), loadBalancingConnector1.getRole("realServer2"));
		
		system.createAttachment(lvsEntity2.getPort("directorPort"), loadBalancingConnector2.getRole("director"));
		system.createAttachment(newComponent.getPort("realServerPort"), loadBalancingConnector2.getRole("realServer1"));
		system.createAttachment(newComponent2.getPort("realServerPort"), loadBalancingConnector2.getRole("realServer2"));
		
		//create collaboration connector
		int connectorNum = asRequesteeConnectors.size();
		int num=0;
		while (asRequesteeConnectorIterator.hasNext()) {
			num++;
			AcmeConnector conn = asRequesteeConnectorIterator.next();
			Set<AcmeAttachment> attach = system.getAttachments();
			Iterator<AcmeAttachment> attachIt = attach.iterator();
			String referencedPortName = null;
			while (attachIt.hasNext()) {
				AcmeAttachment attachment = attachIt.next();
				if (attachment.getReferencedRoleName().equals(conn.getName()+".requestor")) {
					referencedPortName = attachment.getReferencedPortName();
				}
			}
			system.removeConnector(conn);
			if (connectorNum == 1) {
				AcmeConnector vrrpCollaborationConnector = system.createConnector("VRRPCollaboration_Connector");
				vrrpCollaborationConnector.createRole("requestor");
				vrrpCollaborationConnector.createRole("master");
				vrrpCollaborationConnector.createRole("slave");
				system.createAttachment(referencedPortName, vrrpCollaborationConnector.getName()+".requestor");
				system.createAttachment(lvsEntity1.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("master"));
				system.createAttachment(lvsEntity2.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("slave"));
				
				AcmeProperty vipProperty = vrrpCollaborationConnector.createProperty("virtual_ip");
				vipProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
				vipProperty.setValue(new AcmeStringValue(configParas.get("ServiceIP")));
				
				AcmeProperty newCom1Property = vrrpCollaborationConnector.createProperty("master_ip");
				newCom1Property.setPropertyType(DefaultAcmeModel.defaultStringType());
				newCom1Property.setValue(new AcmeStringValue("#" + newComponent.getName() +"_ip"));
				
				AcmeProperty newCom2Property = vrrpCollaborationConnector.createProperty("slave_ip");
				newCom2Property.setPropertyType(DefaultAcmeModel.defaultTypeType());
				newCom2Property.setValue(new AcmeStringValue("#" + newComponent2.getName() + "_ip"));
				
				AcmeProperty implemetationProperty = vrrpCollaborationConnector.createProperty("implemetation");
				implemetationProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
				implemetationProperty.setValue(new AcmeStringValue("VRRP stack of keepalived"));
			} else {
				AcmeConnector vrrpCollaborationConnector = system.createConnector("VRRPCollaboration_Connector" + num);
				vrrpCollaborationConnector.createRole("requestor");
				vrrpCollaborationConnector.createRole("master");
				vrrpCollaborationConnector.createRole("slave");
				system.createAttachment(referencedPortName, vrrpCollaborationConnector.getName()+".requestor");
				system.createAttachment(lvsEntity1.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("master"));
				system.createAttachment(lvsEntity2.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("slave"));
			}
		}
		
		//create connectors for new components
		while (asRequestorConnectorIterator.hasNext()) {
			AcmeConnector conn = asRequestorConnectorIterator.next();
			Set<AcmeAttachment> attach = system.getAttachments();
			Iterator<AcmeAttachment> attachIt = attach.iterator();
			String referencedPortName = null;
			while (attachIt.hasNext()) {
				AcmeAttachment attachment = attachIt.next();
				if (attachment.getReferencedRoleName().equals(conn.getName()+".requestee")) {
					referencedPortName = attachment.getReferencedPortName();
				}
			}
			system.removeConnector(conn);
			AcmeConnector newConn1= system.createConnector(conn.getName()+"1");
			newConn1.createRole("requestor");
			newConn1.createRole("requestee");
			AcmeConnector newConn2= system.createConnector(conn.getName()+"2");
			newConn2.createRole("requestor");
			newConn2.createRole("requestee");
			system.createAttachment(referencedPortName, newConn1.getName()+".requestee");
			system.createAttachment(newComponent.getPort(requestorPortMap.get(conn.getName())), newConn1.getRole("requestor"));
			system.createAttachment(referencedPortName, newConn2.getName()+".requestee");
			system.createAttachment(newComponent2.getPort(requestorPortMap.get(conn.getName())), newConn2.getRole("requestor"));
		}
	}
}