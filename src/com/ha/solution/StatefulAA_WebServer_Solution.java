package com.ha.solution;

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
 * High availability transformation solution for stateful web service using memcached for session share.
 */
public class StatefulAA_WebServer_Solution extends Solution {
	public void haTrans(AcmeComponent component, AcmeSystem system) throws Exception{
		System.out.println("StatefulAA_WebServer");
			
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
		newComponent.createPort("sessionRequestPort");
		Iterator<String> requestorPortNamesIterator = requestorPortNames.iterator();
		while (requestorPortNamesIterator.hasNext()) {
			String str = requestorPortNamesIterator.next();
			newComponent.createPort(str);
		}
		
		AcmeComponent newComponent2=system.createComponent(componentName+"2");
		newComponent2.createPort("healthCheckPort");
		newComponent2.createPort("realServerPort");
		newComponent2.createPort("sessionRequestPort");
		Iterator<String> requestorPortNamesIterator2 = requestorPortNames.iterator();
		while (requestorPortNamesIterator2.hasNext()) {
			String str = requestorPortNamesIterator2.next();
			newComponent2.createPort(str);
		}	
	
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
		
		//create memcached component and using repcached for its HA.
		AcmeComponent memcachedEntity1 = system.createComponent("Memcached-Entity1");
		memcachedEntity1.createPort("collaborationPort");
		AcmeRepresentation memcachedEntity1Rep = memcachedEntity1.createRepresentation("Memcached-Entity1_Rep");
		AcmeSystem memcachedEntity1RepSystem = memcachedEntity1Rep.getSystem();
		
		AcmeComponent repcached = memcachedEntity1RepSystem.createComponent("Repcached");
		repcached.createPort("collaborationPort");
		repcached.createPort("monitorPort");
		
		AcmeComponent memcached = memcachedEntity1RepSystem.createComponent("Memcached");
		memcached.createPort("monitoredPort");
		
		AcmeConnector monitorConnector = memcachedEntity1RepSystem.createConnector("Monitor_Connector");
		monitorConnector.createRole("monitor");
		monitorConnector.createRole("monitored");
		
		memcachedEntity1RepSystem.createAttachment(repcached.getPort("monitorPort"), monitorConnector.getRole("monitor"));
		memcachedEntity1RepSystem.createAttachment(memcached.getPort("monitoredPort"), monitorConnector.getRole("monitored"));
		
		memcachedEntity1Rep.createBinding("collaborationPort", "Repcached.collaborationPort");
		
		AcmeComponent memcachedEntity2 = system.createComponent("Memcached-Entity2");
		memcachedEntity2.createPort("collaborationPort");
		AcmeRepresentation memcachedEntity2Rep = memcachedEntity2.createRepresentation("Memcached-Entity2_Rep");
		AcmeSystem memcachedEntity2RepSystem = memcachedEntity2Rep.getSystem();
		
		AcmeComponent repcached2 = memcachedEntity2RepSystem.createComponent("Repcached");
		repcached2.createPort("collaborationPort");
		repcached2.createPort("monitorPort");
		
		AcmeComponent memcached2 = memcachedEntity2RepSystem.createComponent("Memcached");
		memcached2.createPort("monitoredPort");
		
		AcmeConnector monitorConnector2 = memcachedEntity2RepSystem.createConnector("Monitor_Connector");
		monitorConnector2.createRole("monitor");
		monitorConnector2.createRole("monitored");
		
		memcachedEntity2RepSystem.createAttachment(repcached2.getPort("monitorPort"), monitorConnector2.getRole("monitor"));
		memcachedEntity2RepSystem.createAttachment(memcached2.getPort("monitoredPort"), monitorConnector2.getRole("monitored"));
		
		memcachedEntity2Rep.createBinding("collaborationPort", "Repcached.collaborationPort");
		
		AcmeConnector repcachedCollaborationConnector = system.createConnector("RepcachedCollaboration_Connector1");
		repcachedCollaborationConnector.createRole("requestor");
		repcachedCollaborationConnector.createRole("master");
		repcachedCollaborationConnector.createRole("slave");
		AcmeConnector repcachedCollaborationConnector2 = system.createConnector("RepcachedCollaboration_Connector2");
		repcachedCollaborationConnector2.createRole("requestor");
		repcachedCollaborationConnector2.createRole("master");
		repcachedCollaborationConnector2.createRole("slave");
		
		system.createAttachment(newComponent.getPort("sessionRequestPort"), repcachedCollaborationConnector.getRole("requestor"));
		system.createAttachment(memcachedEntity1.getPort("collaborationPort"), repcachedCollaborationConnector.getRole("master"));
		system.createAttachment(memcachedEntity2.getPort("collaborationPort"), repcachedCollaborationConnector.getRole("slave"));
		system.createAttachment(newComponent2.getPort("sessionRequestPort"), repcachedCollaborationConnector2.getRole("requestor"));
		system.createAttachment(memcachedEntity1.getPort("collaborationPort"), repcachedCollaborationConnector2.getRole("master"));
		system.createAttachment(memcachedEntity2.getPort("collaborationPort"), repcachedCollaborationConnector2.getRole("slave"));
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
