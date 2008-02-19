/**
 * 
 */
package eu.etaxonomy.cdm.test.unit;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;

/**
 * @author a.mueller
 *
 */
public abstract class EntityTestBase {
	private static Logger logger = Logger.getLogger(EntityTestBase.class);

	/**
	 * 
	 */
	public EntityTestBase() {
		super();
	}
	

	protected Class clazzToTest = clazzToTest2();
	protected Class clazzToTest2(){
		String testClassName = this.getClass().getName();
		if (testClassName.endsWith("Test")){
			String className = testClassName.substring(0, testClassName.length() - "Test".length());
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				logger.warn(e.getMessage());
				return null;
			}
		}else{
			return null;
		}
	}
	
	@Test 
	public final void testPersistentGetterSetterPair() {
		//
		Annotation annotation = clazzToTest.getAnnotation(Entity.class);
		if (annotation != null){
			Method[] methods = clazzToTest.getDeclaredMethods();
			List<String> strMethods = new ArrayList<String>(); 
			for (Method method : methods){
				strMethods.add(method.getName());
			}		
			for (Method method : methods){
				String getMethodName = method.getName();
				String setMethodName = "s" + getMethodName.substring(1);
					try {
						if (getMethodName.startsWith("get") && method.getAnnotation(Transient.class) == null){
							Class params = method.getReturnType();
							Method setMethod = clazzToTest.getDeclaredMethod(setMethodName, params);
							if (setMethod == null){fail();}
						}else{
							//no setter - do nothing
						}
					} catch (SecurityException e) {
						logger.info(e.getMessage());
					} catch (Exception e) {
						String warning = "Missing setter for getter: " + getMethodName;
						logger.warn(warning);
						fail(warning);
					}

			}
		}
		
	}

}
