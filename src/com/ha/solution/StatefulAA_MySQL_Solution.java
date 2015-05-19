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
 * High availability transformation solution for MySQL cluster.
 */
public class StatefulAA_MySQL_Solution extends Solution {
	public void haTrans(AcmeComponent component, AcmeSystem system) throws Exception{
		System.out.println("StatefulAA_MySQL_Cluster");
			
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
		newComponent.createPort("sendDataRequestPort");
		newComponent.createPort("managementPort");
		Iterator<String> requestorPortNamesIterator = requestorPortNames.iterator();
		while (requestorPortNamesIterator.hasNext()) {
			String str = requestorPortNamesIterator.next();
			newComponent.createPort(str);
		}
		
		AcmeComponent newComponent2=system.createComponent(componentName+"2");
		newComponent2.createPort("healthCheckPort");
		newComponent2.createPort("realServerPort");
		newComponent2.createPort("sendDataRequestPort");
		newComponent2.createPort("managementPort");
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
		
		//create NDB component
		AcmeComponent ndb = system.createComponent("NDB" + 1);
		ndb.createPort("receiveDataRequestPort");
		ndb.createPort("managementPort");
		AcmeComponent ndb2 = system.createComponent("NDB" + 2);
		ndb2.createPort("receiveDataRequestPort");
		ndb2.createPort("managementPort");
		
		//create MGM component
		AcmeComponent mgm = system.createComponent("MGM");
		mgm.createPort("managementPort");
		
		AcmeConnector dataAccessmentConnector = system.createConnector("DataAccessment_Connetor" + 1);
		dataAccessmentConnector.createRole("requestor");
		dataAccessmentConnector.createRole("requestee");
		
		AcmeConnector dataAccessmentConnector2 = system.createConnector("DataAccessment_Connetor" + 2);
		dataAccessmentConnector2.createRole("requestor");
		dataAccessmentConnector2.createRole("requestee");
		
		AcmeConnector mysqlManagementConnector = system.createConnector("MySQLManagement_Connector" + 1);
		mysqlManagementConnector.createRole("requestor");
		mysqlManagementConnector.createRole("requestee");
		
		AcmeConnector mysqlManagementConnector2 = system.createConnector("MySQLManagement_Connector" + 2);
		mysqlManagementConnector2.createRole("requestor");
		mysqlManagementConnector2.createRole("requestee");
		
		AcmeConnector ndbManagementConnector = system.createConnector("NDBManagement_Connector" + 1);
		ndbManagementConnector.createRole("requestor");
		ndbManagementConnector.createRole("requestee");
		
		AcmeConnector ndbManagementConnector2 = system.createConnector("NDBManagement_Connector" + 2);
		ndbManagementConnector2.createRole("requestor");
		ndbManagementConnector2.createRole("requestee");
		
		system.createAttachment(newComponent.getPort("sendDataRequestPort"), dataAccessmentConnector.getRole("requestor"));
		system.createAttachment(ndb.getPort("receiveDataRequestPort"), dataAccessmentConnector.getRole("requestee"));
		system.createAttachment(newComponent2.getPort("sendDataRequestPort"), dataAccessmentConnector2.getRole("requestor"));
		system.createAttachment(ndb2.getPort("receiveDataRequestPort"), dataAccessmentConnector2.getRole("requestee"));
		system.createAttachment(newComponent.getPort("managementPort"), mysqlManagementConnector.getRole("requestee"));
		system.createAttachment(mgm.getPort("managementPort"), mysqlManagementConnector.getRole("requestor"));
		system.createAttachment(newComponent2.getPort("managementPort"), mysqlManagementConnector2.getRole("requestee"));
		system.createAttachment(mgm.getPort("managementPort"), mysqlManagementConnector2.getRole("requestor"));
		system.createAttachment(ndb.getPort("managementPort"), ndbManagementConnector.getRole("requestee"));
		system.createAttachment(mgm.getPort("managementPort"), ndbManagementConnector.getRole("requestor"));
		system.createAttachment(ndb2.getPort("managementPort"), ndbManagementConnector2.getRole("requestee"));
		system.createAttachment(mgm.getPort("managementPort"), ndbManagementConnector2.getRole("requestor"));
		
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
