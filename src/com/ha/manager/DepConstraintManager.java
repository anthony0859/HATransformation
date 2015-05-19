package com.ha.manager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.element.representation.AcmeRepresentation;

import com.ha.input.ElementGetter;
import com.ha.util.StringOperation;

//Get the deploy constraint for HASA.
public class DepConstraintManager {
	public void genDepConstraint (AcmeSystem system, String path) throws IOException {
		FileWriter fw = new FileWriter(path + "/DeploymentConstraint");		
		StringBuilder sb = new StringBuilder();	
		ElementGetter eg = new ElementGetter();
		Set<AcmeComponent> components = eg.getComponents(system);	
		Iterator<AcmeComponent> componentIt = components.iterator();
		while (componentIt.hasNext()) {
			AcmeComponent component = componentIt.next();
			sb.append(component.getName() +" : " );
			Set <? extends AcmeRepresentation> representations = component.getRepresentations();
			if (representations.size() > 0) {
				Iterator<? extends AcmeRepresentation> representationsIterator = representations.iterator();
				while (representationsIterator.hasNext()) {
					AcmeRepresentation representation = representationsIterator.next();
					AcmeSystem innerSystem = representation.getSystem();
					Set <AcmeComponent> innerComponents = innerSystem.getComponents();
					Iterator <AcmeComponent> innerComponentsIterator = innerComponents.iterator();			
					while (innerComponentsIterator.hasNext()) {
						AcmeComponent innerComponent = innerComponentsIterator.next();
						sb.append(innerComponent.getName()+" ");			
					}
					sb.append("\n\n");
				}
			} else {
				StringOperation so = new StringOperation();
				sb.append(so.rmLastNum(component.getName()) +"\n\n");
			}	
		}
		fw.write(sb.toString());
		fw.close();
		return;
	}
}
