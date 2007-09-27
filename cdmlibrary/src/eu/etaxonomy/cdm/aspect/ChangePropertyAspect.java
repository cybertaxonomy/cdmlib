/**
 * 
 */
package eu.etaxonomy.cdm.aspect;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author a.mueller
 *
 */
//@Aspect
public class ChangePropertyAspect {
	private static final Logger logger = Logger.getLogger(ChangePropertyAspect.class);
	
	//@AfterReturning("execution(* "eu.etaxonomy.cdm.model.name.TaxonName.getGenus(..))")
	public Object afterSet(JoinPoint jp){
		Object o = jp.getArgs();
		Object o2 = jp.getTarget();
		Object o3 = jp.getStaticPart();
		Object o4 = jp.getKind();
		Object o5 = jp.getThis();
		Object o6 = ((Signature)jp.getSignature()).getName();
		
		
		Object newValue = "";
		logger.warn("AFTER_SET* !!!");
		logger.warn("NewValue: " + newValue);
		return null;
	}
	
}
