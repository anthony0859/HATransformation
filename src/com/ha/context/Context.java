package com.ha.context;

import java.util.HashMap;

import org.acmestudio.basicmodel.element.AcmeComponent;
import org.acmestudio.basicmodel.element.AcmeSystem;

import com.ha.input.ElementGetter;
import com.ha.solution.NoneSolution;
import com.ha.solution.Solution;
import com.ha.solution.StatefulAA_MySQL_Solution;
import com.ha.solution.StatefulAA_RabbitMQ_Solution;
import com.ha.solution.StatefulAA_WebServer_Solution;
import com.ha.solution.StatefulAP_DRBD_Solution;
import com.ha.solution.StatefulAP_NFS_Solution;
import com.ha.solution.StatelessAASolution;
import com.ha.solution.StatelessAPSolution;

/**
 * Context definition of Strategy Pattern of High Availability Model Transformation.
 * The appropriate strategy will be chosen based on the HAParams defined by the user in the ACME description of the application service. Otherwise, we will provide 
 * default strategy. 
 * In addition to the service component itself, three key high availability parameters are: ServiceCategory, Mode and Size.
 * 
 * @author Anthony
 * 
 */
public class Context {
	private Solution solution;
	
	public Context(AcmeComponent component) {
		ElementGetter elementGetter = new ElementGetter();
		HashMap<String, String> HAParamsMap = elementGetter.getHAParams(component);
		
		if (HAParamsMap.size()==0) {
			solution = new NoneSolution();
		} else if (HAParamsMap.get("ServiceCategory").equalsIgnoreCase("stateless") && HAParamsMap.get("Mode").equalsIgnoreCase("active/passive") && HAParamsMap.get("Size").equals("2")) {
			solution = new StatelessAPSolution();
		} else if (HAParamsMap.get("ServiceCategory").equalsIgnoreCase("stateless") && HAParamsMap.get("Mode").equalsIgnoreCase("active/active") && HAParamsMap.get("Size").equals("2")) {
			solution = new StatelessAASolution();
		} else if (HAParamsMap.get("ServiceCategory").equalsIgnoreCase("stateful") && HAParamsMap.get("Mode").equalsIgnoreCase("active/passive") && HAParamsMap.get("Size").equals("2")) {
			if (component.getName().equals("MySQL")) {
				solution = new StatefulAP_DRBD_Solution();
			} else if (component.getName().equals("WebServer")) {
				solution = new StatefulAP_NFS_Solution();
			}
		} else if (HAParamsMap.get("ServiceCategory").equalsIgnoreCase("stateful") && HAParamsMap.get("Mode").equalsIgnoreCase("active/active") && HAParamsMap.get("Size").equals("2")) {
			if (component.getName().equals("MySQL")) {
				solution = new StatefulAA_MySQL_Solution();
			} else if (component.getName().equals("WebServer")) {
				solution = new StatefulAA_WebServer_Solution();
			} else if (component.getName().equals("RabbitMQ")) {
				solution = new StatefulAA_RabbitMQ_Solution();
			}
		}
	}
	
	public Solution getSolution() {
		return solution;
	}
	
	public void ContextInterface(AcmeComponent component, AcmeSystem system) throws Exception{
		solution.haTrans(component, system);
	}
}
