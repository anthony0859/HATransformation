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
 * High availability transformation solution for stateful service in active/passive mode with NFS.
 */

public class StatefulAP_NFS_Solution extends Solution {
	public void haTrans(AcmeComponent component, AcmeSystem system) throws Exception{
		System.out.println("Stateful_NFS_AP");	
				
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
		AcmeComponent componentEntity1 = system.createComponent(componentName+"-Entity1");
		componentEntity1.createPort("collaborationPort");
		componentEntity1.createPort("dataAccessPort");
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
	
		
 		AcmeComponent pacemaker = componentEntity1RepSystem.createComponent("Pacemaker");
		pacemaker.createPort("clusterInfoTransPort");
		pacemaker.createPort("resourceAgentPort");
		AcmeComponent newComponent = componentEntity1RepSystem.createComponent(componentName);
		newComponent.createPort("resourceAgentPort");
		newComponent.createPort("dataAccessPort");
		Iterator<String> requestorPortNamesIterator2 = requestorPortNames.iterator();
		while (requestorPortNamesIterator2.hasNext()) {
			String str = requestorPortNamesIterator2.next();
			newComponent.createPort(str);
		}
		
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
		componentEntity1Rep.createBinding("dataAccessPort", newComponent.getName()+".dataAccessPort");
		Iterator<String> requestorPortNamesIterator3 =requestorPortNames.iterator();
		while (requestorPortNamesIterator3.hasNext()) {
			String str = requestorPortNamesIterator3.next();
			componentEntity1Rep.createBinding(str, componentName + "." + str);
		}
		
		//create another new component
		AcmeComponent componentEntity2 = system.createComponent(componentName+"-Entity2");
		componentEntity2.createPort("collaborationPort");
		componentEntity2.createPort("dataAccessPort");
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
		AcmeComponent pacemaker2 = componentEntity2RepSystem.createComponent("Pacemaker");
		pacemaker2.createPort("clusterInfoTransPort");
		pacemaker2.createPort("resourceAgentPort");
		AcmeComponent newComponent2 = componentEntity2RepSystem.createComponent(componentName);
		newComponent2.createPort("resourceAgentPort");
		newComponent2.createPort("dataAccessPort");
		Iterator<String> requestorPortNamesIterator5 = requestorPortNames.iterator();
		while (requestorPortNamesIterator5.hasNext()) {
			String str = requestorPortNamesIterator5.next();
			newComponent2.createPort(str);
		}
		
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
		componentEntity2Rep.createBinding("dataAccessPort", newComponent2.getName()+".dataAccessPort");
		Iterator<String> requestorPortNamesIterator6 =requestorPortNames.iterator();
		while (requestorPortNamesIterator6.hasNext()) {
			String str = requestorPortNamesIterator6.next();
			componentEntity2Rep.createBinding(str, componentName + "." + str);
		}
		
		
		//create NFS Server component
		AcmeComponent nfs = system.createComponent("NFSServer");
		nfs.createPort("dataAccessPort");
		
		//create RPC connector for NFS 
		AcmeConnector RPC_Connector1 = system.createConnector("RPC_Connector1");
		RPC_Connector1.createRole("caller");
		RPC_Connector1.createRole("callee");
		AcmeConnector RPC_Connector2 = system.createConnector("RPC_Connector2");
		RPC_Connector2.createRole("caller");
		RPC_Connector2.createRole("callee");
		system.createAttachment(componentEntity1.getPort("dataAccessPort"), RPC_Connector1.getRole("caller"));
		system.createAttachment(nfs.getPort("dataAccessPort"), RPC_Connector1.getRole("callee"));
		system.createAttachment(componentEntity2.getPort("dataAccessPort"), RPC_Connector2.getRole("caller"));
		system.createAttachment(nfs.getPort("dataAccessPort"), RPC_Connector2.getRole("callee"));
		
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
