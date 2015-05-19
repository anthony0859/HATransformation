package com.ha.input;


import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.acme.model.IAcmeModel;
import org.acmestudio.basicmodel.model.AcmeModel;


/*
 * Get AcmeModel from object AcmeResource.
 */
public class ModelGetter {
	public IAcmeModel getIAcmeModel(IAcmeResource resource) throws Exception {
		IAcmeModel model = resource.getModel();
		return model;
	}
	
	public AcmeModel getAcmeModel(IAcmeResource resource) throws Exception {
		AcmeModel model = (AcmeModel)resource.getModel();
		return model;
	}
}
