package com.ha.output;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.Set;

import org.acmestudio.basicmodel.element.AcmeAttachment;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeConnector;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentation;


/*
 * In charge of writing AcmeSystem to ACME file.
 */
public class OutputUtil {
	public void writeSystem(AcmeSystem system, String fileName) throws Exception {
		FileWriter fw = new FileWriter(fileName);
		ElementContentGetter ecg = new ElementContentGetter();
		
		StringBuilder str = new StringBuilder();
		str.append("System " + system.getName() +" = {\n");
		Set<AcmeComponent> components = system.getComponents();
		Iterator<AcmeComponent> componentsIterator = components.iterator();
		while (componentsIterator.hasNext()) {
			AcmeComponent component = componentsIterator.next();
			Set <? extends AcmeRepresentation> reps = component.getRepresentations();
			if (reps.size() == 0) {
				str.append(ecg.getSimpleComponentContent(component, "    "));
			} else {
				str.append(ecg.getCompoundComponentContent(component, "    "));
			}
		}
		
		Set<AcmeConnector> connectors = system.getConnectors();
		Iterator<AcmeConnector> connectorsIterator = connectors.iterator();
		while (connectorsIterator.hasNext()) {
			AcmeConnector connector = connectorsIterator.next();
			str.append(ecg.getConnectorContent(connector, "    "));
		}
		
		Set<AcmeAttachment> attachments = system.getAttachments();
		Iterator<AcmeAttachment> attachmentsIterator = attachments.iterator();
		while (attachmentsIterator.hasNext()) {
			AcmeAttachment attachment = attachmentsIterator.next();
			str.append(ecg.getAttachmentContent(attachment, "    "));
		}
		str.append("}");
		fw.write(str.toString());
		fw.close();
		return;
	}
}
