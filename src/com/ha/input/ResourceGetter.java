package com.ha.input;


import org.acmestudio.acme.core.resource.IAcmeResource;
import org.acmestudio.standalone.environment.StandaloneEnvironment;
import org.acmestudio.standalone.environment.StandaloneEnvironment.TypeCheckerType;
import org.acmestudio.standalone.resource.StandaloneResource;
import org.acmestudio.standalone.resource.StandaloneResourceProvider;


/*
 * Get AcmeResource from object file.
 */
public class ResourceGetter {
	public IAcmeResource getIAcmeResource(String fileName) throws Exception {
		StandaloneEnvironment.instance().useTypeChecker(TypeCheckerType.SYNCHRONOUS);
		StandaloneResource resource = StandaloneResourceProvider.instance().acmeResourceForString(fileName);

		StandaloneResourceProvider.instance().releaseResource(resource);
		return resource;
	}
}
