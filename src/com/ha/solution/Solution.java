package com.ha.solution;

import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeSystem;

//Base class for high availability solution classes.
public abstract class Solution {
	public abstract void haTrans(AcmeComponent component, AcmeSystem system) throws Exception;
}
