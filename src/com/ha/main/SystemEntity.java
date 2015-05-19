package com.ha.main;

import java.util.Iterator;
import java.util.Set;

import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeSystem;
import org.acmestudio.basicmodel.model.AcmeModel;

import com.ha.context.Context;
import com.ha.input.ElementGetter;
import com.ha.input.ModelGetter;
import com.ha.input.ResourceGetter;
import com.ha.manager.ConfigInfoManager;
import com.ha.manager.DepConstraintManager;
import com.ha.output.OutputUtil;

/*
 * Main entity of this project which is used for software architecture high availability transformation.
 * 
 * @Anthony
 */
public class SystemEntity {
	public AcmeSystem transEntity (AcmeSystem system) throws Exception {
		Set <AcmeComponent> components = system.getComponents();
		Iterator <AcmeComponent> componentsIterator = components.iterator();
		while (componentsIterator.hasNext()) {
			AcmeComponent component = componentsIterator.next();
			Context context = new Context(component);
			context.ContextInterface(component, system);
		}
		return system;
	}
	
	public void run(String fileName) throws Exception{
		ResourceGetter rg = new ResourceGetter();
		IAcmeResource resource = rg.getIAcmeResource(fileName);
		ModelGetter mg = new ModelGetter();
		AcmeModel model = mg.getAcmeModel(resource);
		ElementGetter eg = new ElementGetter();
		AcmeSystem system = eg.getSystem(model);
		transEntity(system);
		OutputUtil ou = new OutputUtil();
		ou.writeSystem(system, "file/output.acme");
		ConfigInfoManager cim = new ConfigInfoManager();
		cim.genConfigFile(system, "file");
		DepConstraintManager dcm = new DepConstraintManager();
		dcm.genDepConstraint(system, "file");
	}
	
	public static void main(String []args) throws Exception{
		SystemEntity entity = new SystemEntity();
		entity.run("file/input.acme");
	}
}
