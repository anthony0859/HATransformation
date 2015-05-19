package com.ha.solution;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
 * High availability transformation solution for stateless service in active/passive mode.
 */
public class StatelessAPSolution extends Solution{
	public void haTrans(AcmeComponent component, AcmeSystem system) throws Exception{
		System.out.println("StatelessAP");	
				
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
		AcmeComponent componentEntity1 = system.createComponent(componentName+"-Entity1");
		componentEntity1.createPort("collaborationPort");
		Iterator<String> requestorPortNamesIterator = requestorPortNames.iterator();
		while (requestorPortNamesIterator.hasNext()) {
			String str = requestorPortNamesIterator.next();
			componentEntity1.createPort(str);
		}
	    
		AcmeRepresentation componentEntity1Rep = componentEntity1.createRepresentation(componentEntity1.getName()+"_Rep");
		
		AcmeSystem componentEntity1RepSystem = componentEntity1Rep.getSystem();
	
		AcmeComponent corosync = componentEntity1RepSystem.createComponent("Corosync");
		corosync.createPort("collaborationPort");
		corosync.createPort("clusterInfoTransPort");
	
		AcmeProperty bindnetaddrProperty = corosync.createProperty("bindnetaddr");
		bindnetaddrProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
		bindnetaddrProperty.setValue(new AcmeStringValue("#network_segment_addr"));
		
		AcmeProperty serviceNameProperty =corosync.createProperty("service_name");
		serviceNameProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
		serviceNameProperty.setValue(new AcmeStringValue("pacemaker"));
		
 		AcmeComponent pacemaker = componentEntity1RepSystem.createComponent("Pacemaker");
		pacemaker.createPort("clusterInfoTransPort");
		pacemaker.createPort("resourceAgentPort");
		
		
		AcmeComponent newComponent = componentEntity1RepSystem.createComponent(componentName);
		newComponent.createPort("resourceAgentPort");
		Iterator<String> requestorPortNamesIterator2 = requestorPortNames.iterator();
		while (requestorPortNamesIterator2.hasNext()) {
			String str = requestorPortNamesIterator2.next();
			newComponent.createPort(str);
		}
		
		Iterator configParasIt = configParas.entrySet().iterator();
	    while (configParasIt.hasNext()) {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)configParasIt.next();
	    	String key = entry.getKey();
	    	if (key.equals("ServiceIP"))
	    		continue;
	    	String value = entry.getValue();
	    	AcmeProperty property = newComponent.createProperty(key);
	    	property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    	property.setValue(new AcmeStringValue(value));
	    }
		
	    
	    AcmeProperty vipRAProperty = pacemaker.createProperty("VIP_RA ");
		vipRAProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
		vipRAProperty.setValue(new AcmeStringValue("ocf:heartbeat:IPaddr"));
		
		AcmeProperty newComRAProperty = pacemaker.createProperty(newComponent.getName()+"_RA");
		newComRAProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
		newComRAProperty.setValue(new AcmeStringValue("#"+ newComponent.getName() +"_ResourceAgent"));
		
		AcmeConnector clusterInfoTransConnector = componentEntity1RepSystem.createConnector("ClusterInfoTrans_Connector");
		clusterInfoTransConnector.createRole("requestee");
		clusterInfoTransConnector.createRole("requestor");
		AcmeConnector resourceAgentConnector = componentEntity1RepSystem.createConnector("ResourceAgent_Connector");
		resourceAgentConnector.createRole("requestee");
		resourceAgentConnector.createRole("requestor");
		
		componentEntity1RepSystem.createAttachment(corosync.getPort("clusterInfoTransPort"), clusterInfoTransConnector.getRole("requestee"));
		componentEntity1RepSystem.createAttachment(pacemaker.getPort("clusterInfoTransPort"), clusterInfoTransConnector.getRole("requestor"));
		componentEntity1RepSystem.createAttachment(pacemaker.getPort("resourceAgentPort"), resourceAgentConnector.getRole("requestor"));
		componentEntity1RepSystem.createAttachment(newComponent.getPort("resourceAgentPort"), resourceAgentConnector.getRole("requestee"));
		
		componentEntity1Rep.createBinding("collaborationPort", "Corosync.collaborationPort");
		Iterator<String> requestorPortNamesIterator3 =requestorPortNames.iterator();
		while (requestorPortNamesIterator3.hasNext()) {
			String str = requestorPortNamesIterator3.next();
			componentEntity1Rep.createBinding(str, componentName + "." + str);
		}
		
		//create another new component
		AcmeComponent componentEntity2 = system.createComponent(componentName+"-Entity2");
		componentEntity2.createPort("collaborationPort");
		Iterator<String> requestorPortNamesIterator4 = requestorPortNames.iterator();
		while (requestorPortNamesIterator4.hasNext()) {
			String str = requestorPortNamesIterator4.next();
			componentEntity2.createPort(str);
		}

		AcmeRepresentation componentEntity2Rep = componentEntity2.createRepresentation(componentEntity2.getName()+"_Rep");
		AcmeSystem componentEntity2RepSystem = componentEntity2Rep.getSystem();
		
		AcmeComponent corosync2 = componentEntity2RepSystem.createComponent("Corosync");
		corosync2.createPort("collaborationPort");
		corosync2.createPort("clusterInfoTransPort");
		AcmeProperty bindnetaddr2Property = corosync2.createProperty("bindnetaddr");
		bindnetaddr2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
		bindnetaddr2Property.setValue(new AcmeStringValue("#network_segment_addr"));
		
		AcmeProperty serviceName2Property =corosync2.createProperty("service_name");
		serviceName2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
		serviceName2Property.setValue(new AcmeStringValue("pacemaker"));
		
		AcmeComponent pacemaker2 = componentEntity2RepSystem.createComponent("Pacemaker");
		pacemaker2.createPort("clusterInfoTransPort");
		pacemaker2.createPort("resourceAgentPort");
		AcmeComponent newComponent2 = componentEntity2RepSystem.createComponent(componentName);
		newComponent2.createPort("resourceAgentPort");
		Iterator<String> requestorPortNamesIterator5 = requestorPortNames.iterator();
		while (requestorPortNamesIterator5.hasNext()) {
			String str = requestorPortNamesIterator5.next();
			newComponent2.createPort(str);
		}
		
		Iterator configParasIt2 = configParas.entrySet().iterator();
	    while (configParasIt2.hasNext()) {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)configParasIt2.next();
	    	String key = entry.getKey();
	    	if (key.equals("ServiceIP"))
	    		continue;
	    	String value = entry.getValue();
	    	AcmeProperty property = newComponent2.createProperty(key);
	    	property.setPropertyType(DefaultAcmeModel.defaultStringType());
	    	property.setValue(new AcmeStringValue(value));
	    }
		
	    AcmeProperty vipRA2Property = pacemaker2.createProperty("VIP_RA ");
		vipRA2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
		vipRA2Property.setValue(new AcmeStringValue("ocf:heartbeat:IPaddr"));
		
		AcmeProperty newComRA2Property = pacemaker2.createProperty(newComponent2.getName()+"_RA");
		newComRA2Property.setPropertyType(DefaultAcmeModel.defaultStringType());
		newComRA2Property.setValue(new AcmeStringValue("#"+ newComponent2.getName() +"_ResourceAgent"));
	    
		AcmeConnector clusterInfoTransConnector2 = componentEntity2RepSystem.createConnector("ClusterInfoTrans_Connector");
		clusterInfoTransConnector2.createRole("requestee");
		clusterInfoTransConnector2.createRole("requestor");
		AcmeConnector resourceAgentConnector2 = componentEntity2RepSystem.createConnector("ResourceAgent_Connector");
		resourceAgentConnector2.createRole("requestee");
		resourceAgentConnector2.createRole("requestor");
		
		componentEntity2RepSystem.createAttachment(corosync2.getPort("clusterInfoTransPort"), clusterInfoTransConnector2.getRole("requestee"));
		componentEntity2RepSystem.createAttachment(pacemaker2.getPort("clusterInfoTransPort"), clusterInfoTransConnector2.getRole("requestor"));
		componentEntity2RepSystem.createAttachment(pacemaker2.getPort("resourceAgentPort"), resourceAgentConnector2.getRole("requestor"));
		componentEntity2RepSystem.createAttachment(newComponent2.getPort("resourceAgentPort"), resourceAgentConnector2.getRole("requestee"));
		
		componentEntity2Rep.createBinding("collaborationPort", "Corosync.collaborationPort");
		Iterator<String> requestorPortNamesIterator6 =requestorPortNames.iterator();
		while (requestorPortNamesIterator6.hasNext()) {
			String str = requestorPortNamesIterator6.next();
			componentEntity2Rep.createBinding(str, componentName + "." + str);
		}
		
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
			if (connectorNum==1) {
				AcmeConnector newConn = system.createConnector("ClusterManagementCollaboration_Connector");
				newConn.createRole("requestor");
				newConn.createRole("master");
				newConn.createRole("slave");			
				system.createAttachment(referencedPortName, newConn.getName()+".requestor");
				system.createAttachment(componentEntity1.getPort("collaborationPort"), newConn.getRole("master"));
				system.createAttachment(componentEntity2.getPort("collaborationPort"), newConn.getRole("slave"));
				
				AcmeProperty vipProperty = newConn.createProperty("virtual_ip"); 
				vipProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
				vipProperty.setValue(new AcmeStringValue(configParas.get("ServiceIP")));
				
				AcmeProperty newCom1Property = newConn.createProperty("master_ip");
				newCom1Property.setPropertyType(DefaultAcmeModel.defaultStringType());
				newCom1Property.setValue(new AcmeStringValue("#" + newComponent.getName() +"_ip"));
				
				AcmeProperty newCom2Property = newConn.createProperty("slave_ip");
				newCom2Property.setPropertyType(DefaultAcmeModel.defaultTypeType());
				newCom2Property.setValue(new AcmeStringValue("#" + newComponent2.getName() + "_ip"));
				
				AcmeProperty implemetationProperty = newConn.createProperty("implemetation");
				implemetationProperty.setPropertyType(DefaultAcmeModel.defaultStringType());
				implemetationProperty.setValue(new AcmeStringValue("Cluster management by pacemaker"));
				
			} else {
				AcmeConnector newConn = system.createConnector("ClusterManagementCollaboration_Connector" + num);
				newConn.createRole("requestor");
				newConn.createRole("master");
				newConn.createRole("slave");			
				system.createAttachment(referencedPortName, newConn.getName()+".requestor");
				system.createAttachment(componentEntity1.getPort("collaborationPort"), newConn.getRole("master"));
				system.createAttachment(componentEntity2.getPort("collaborationPort"), newConn.getRole("slave"));
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
			system.createAttachment(componentEntity1.getPort(requestorPortMap.get(conn.getName())), newConn1.getRole("requestor"));
			system.createAttachment(referencedPortName, newConn2.getName()+".requestee");
			system.createAttachment(componentEntity2.getPort(requestorPortMap.get(conn.getName())), newConn2.getRole("requestor"));
		}
	}
}