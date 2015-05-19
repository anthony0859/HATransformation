package com.ha.solution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.acmestudio.basicmodel.element.AcmeAttachment;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeConnector;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentation;

import com.ha.util.TransUtils;

/*
 * High availability transformation solution for RabbitMQ mirrored queues. 
 */
public class StatefulAA_RabbitMQ_Solution extends Solution {
	public void haTrans(AcmeComponent component, AcmeSystem system) throws Exception{
		System.out.println("StatefulAA_RabbitMQ");
			
		String componentName = component.getName();
		System.out.println("Component: " + componentName);
		
		TransUtils transUtils = new TransUtils();
		Set<AcmeConnector> asRequesteeConnectors = transUtils.getRequesteeConnectors(component, system);
		Iterator<AcmeConnector> asRequesteeConnectorIterator = asRequesteeConnectors.iterator(); 
		
		Set<AcmeConnector> asRequestorConnectors = transUtils.getRequestorConnectors(component, system);
		Iterator<AcmeConnector> asRequestorConnectorIterator = asRequestorConnectors.iterator(); 
		
		Set<String> requestorPortNames = transUtils.getRequestorPortNames(component, asRequestorConnectors, system);
		Map<String, String> requestorPortMap = transUtils.getRequeestorPortMap(component, asRequestorConnectors, system);
		
		//remove current component
		system.removeComponent(component);
		
		//create new component using HAProxy for load balancing.
		AcmeComponent haproxyEntity1 = system.createComponent("HAProxy-Entity1");
		haproxyEntity1.createPort("collaborationPort");
		haproxyEntity1.createPort("healthCheckPort");
		haproxyEntity1.createPort("directorPort");
		
		AcmeRepresentation haproxyEntity1Rep = haproxyEntity1.createRepresentation(haproxyEntity1.getName()+"_Rep");
		AcmeSystem haproxyEntity1RepSystem = haproxyEntity1Rep.getSystem();
		
		AcmeComponent keepalived = haproxyEntity1RepSystem.createComponent("Keepalived");
		keepalived.createPort("collaborationPort");
		keepalived.createPort("healthCheckPort");
		keepalived.createPort("bindingPort");
		
		AcmeComponent haproxy = haproxyEntity1RepSystem.createComponent("HAProxy");
		haproxy.createPort("bindingPort");
		haproxy.createPort("directorPort");
		
		AcmeConnector bindingConnector = haproxyEntity1RepSystem.createConnector("Binding_Connector");
		bindingConnector.createRole("requestor");
		bindingConnector.createRole("requestee");
		
		haproxyEntity1RepSystem.createAttachment(keepalived.getPort("bindingPort"), bindingConnector.getRole("requestor"));
		haproxyEntity1RepSystem.createAttachment(haproxy.getPort("bindingPort"), bindingConnector.getRole("requestee"));
 		
		haproxyEntity1Rep.createBinding("collaborationPort", "Keepalived.collaborationPort");
		haproxyEntity1Rep.createBinding("healthCheckPort", "Keepalived.healthCheckPort");
		haproxyEntity1Rep.createBinding("directorPort", "HAProxy.directorPort");
		
		//create another new component
		AcmeComponent haproxyEntity2 = system.createComponent("HAProxy-Entity2");
		haproxyEntity2.createPort("collaborationPort");
		haproxyEntity2.createPort("healthCheckPort");
		haproxyEntity2.createPort("directorPort");
		AcmeRepresentation haproxyEntity2Rep = haproxyEntity2.createRepresentation(haproxyEntity2.getName()+"_Rep");
		AcmeSystem haproxyEntity2RepSystem = haproxyEntity2Rep.getSystem();
		
		AcmeComponent keepalived2 = haproxyEntity2RepSystem.createComponent("Keepalived");
		keepalived2.createPort("collaborationPort");
		keepalived2.createPort("healthCheckPort");
		keepalived2.createPort("bindingPort");
		
		AcmeComponent haproxy2 = haproxyEntity2RepSystem.createComponent("HAProxy");
		haproxy2.createPort("bindingPort");
		haproxy2.createPort("directorPort");
		
		AcmeConnector bindingConnector2 = haproxyEntity2RepSystem.createConnector("Binding_Connector");
		bindingConnector2.createRole("requestor");
		bindingConnector2.createRole("requestee");
		
		haproxyEntity2RepSystem.createAttachment(keepalived2.getPort("bindingPort"), bindingConnector2.getRole("requestor"));
		haproxyEntity2RepSystem.createAttachment(haproxy2.getPort("bindingPort"), bindingConnector2.getRole("requestee"));
 		
		haproxyEntity2Rep.createBinding("collaborationPort", "Keepalived.collaborationPort");
		haproxyEntity2Rep.createBinding("healthCheckPort", "Keepalived.healthCheckPort");
		haproxyEntity2Rep.createBinding("directorPort", "HAProxy.directorPort");
		
		
		AcmeComponent newComponent=system.createComponent(componentName+"1");
		newComponent.createPort("healthCheckPort");
		newComponent.createPort("realServerPort");
		newComponent.createPort("mirroredPort");
		Iterator<String> requestorPortNamesIterator = requestorPortNames.iterator();
		while (requestorPortNamesIterator.hasNext()) {
			String str = requestorPortNamesIterator.next();
			newComponent.createPort(str);
		}
		
		AcmeComponent newComponent2=system.createComponent(componentName+"2");
		newComponent2.createPort("healthCheckPort");
		newComponent2.createPort("realServerPort");
		newComponent2.createPort("mirroredPort");
		Iterator<String> requestorPortNamesIterator2 = requestorPortNames.iterator();
		while (requestorPortNamesIterator2.hasNext()) {
			String str = requestorPortNamesIterator2.next();
			newComponent2.createPort(str);
		}	
		
		//create mirrored queue connector for RabbitMQ cluster.
		AcmeConnector mirroredQueueConnector = system.createConnector("MirroredQueue_Connector");
		mirroredQueueConnector.createRole("master");
		mirroredQueueConnector.createRole("slave");
		
		AcmeConnector healthCheckConnector1 = system.createConnector("HealthCheck_Connector1");
		healthCheckConnector1.createRole("monitor");
		healthCheckConnector1.createRole("monitored1");
		healthCheckConnector1.createRole("monitored2");
		
		AcmeConnector loadBalancingConnector1 = system.createConnector("LoadBalancing_Connector1");
		loadBalancingConnector1.createRole("director");
		loadBalancingConnector1.createRole("realServer1");
		loadBalancingConnector1.createRole("realServer2");
		
		AcmeConnector healthCheckConnector2 = system.createConnector("HealthCheck_Connector2");
		healthCheckConnector2.createRole("monitor");
		healthCheckConnector2.createRole("monitored1");
		healthCheckConnector2.createRole("monitored2");
		
		AcmeConnector loadBalancingConnector2 = system.createConnector("LoadBalancing_Connector2");
		loadBalancingConnector2.createRole("director");
		loadBalancingConnector2.createRole("realServer1");
		loadBalancingConnector2.createRole("realServer2");
		
		system.createAttachment(newComponent.getPort("mirroredPort"), mirroredQueueConnector.getRole("master"));
		system.createAttachment(newComponent2.getPort("mirroredPort"), mirroredQueueConnector.getRole("slave"));
		
		system.createAttachment(haproxyEntity1.getPort("healthCheckPort"), healthCheckConnector1.getRole("monitor"));
		system.createAttachment(newComponent.getPort("healthCheckPort"), healthCheckConnector1.getRole("monitored1"));
		system.createAttachment(newComponent2.getPort("healthCheckPort"), healthCheckConnector1.getRole("monitored2"));
		
		system.createAttachment(haproxyEntity2.getPort("healthCheckPort"), healthCheckConnector2.getRole("monitor"));
		system.createAttachment(newComponent.getPort("healthCheckPort"), healthCheckConnector2.getRole("monitored1"));
		system.createAttachment(newComponent2.getPort("healthCheckPort"), healthCheckConnector2.getRole("monitored2"));
		
		system.createAttachment(haproxyEntity1.getPort("directorPort"), loadBalancingConnector1.getRole("director"));
		system.createAttachment(newComponent.getPort("realServerPort"), loadBalancingConnector1.getRole("realServer1"));
		system.createAttachment(newComponent2.getPort("realServerPort"), loadBalancingConnector1.getRole("realServer2"));
		
		system.createAttachment(haproxyEntity2.getPort("directorPort"), loadBalancingConnector2.getRole("director"));
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
				system.createAttachment(haproxyEntity1.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("master"));
				system.createAttachment(haproxyEntity2.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("slave"));
			} else {
				AcmeConnector vrrpCollaborationConnector = system.createConnector("VRRPCollaboration_Connector" + num);
				vrrpCollaborationConnector.createRole("requestor");
				vrrpCollaborationConnector.createRole("master");
				vrrpCollaborationConnector.createRole("slave");
				system.createAttachment(referencedPortName, vrrpCollaborationConnector.getName()+".requestor");
				system.createAttachment(haproxyEntity1.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("master"));
				system.createAttachment(haproxyEntity2.getPort("collaborationPort"), vrrpCollaborationConnector.getRole("slave"));
			}
		}
		
		//create connectors for new components
		while (asRequestorConnectorIterator.hasNext()) {
			AcmeConnector conn = asRequestorConnectorIterator.next();
			Set<AcmeAttachment> attach = system.getAttachments();
			Iterator<AcmeAttachment> attachIt = attach.iterator();
//			String referencedPortName = null;
			ArrayList<String> referencedPortNames = new ArrayList<String>();
			while (attachIt.hasNext()) {
				AcmeAttachment attachment = attachIt.next();
				if (attachment.getReferencedRoleName().indexOf(conn.getName()+".realServer")!=-1) {
					referencedPortNames.add(attachment.getReferencedPortName());
				}
			}
			system.removeConnector(conn);
			int portsNum = referencedPortNames.size();
			AcmeConnector newConn1= system.createConnector(conn.getName()+"1");
			newConn1.createRole("requestor");
			for (int i=0; i<portsNum; i++) {
				newConn1.createRole("realServer" + (i+1));
			}
			
			AcmeConnector newConn2= system.createConnector(conn.getName()+"2");
			newConn2.createRole("requestor");
			for (int i=0; i<portsNum; i++) {
				newConn2.createRole("realServer" + (i+1));
			}
			
			for (int i=0; i<portsNum; i++) {
				system.createAttachment(referencedPortNames.get(i), newConn1.getName()+".realServer"+(i+1));
			}
			for (int i=0; i<portsNum; i++) {
				system.createAttachment(referencedPortNames.get(i), newConn2.getName()+".realServer"+(i+1));
			}
			system.createAttachment(newComponent.getPort(requestorPortMap.get(conn.getName())), newConn1.getRole("requestor"));
			system.createAttachment(newComponent2.getPort(requestorPortMap.get(conn.getName())), newConn2.getRole("requestor"));
		}
	}
}