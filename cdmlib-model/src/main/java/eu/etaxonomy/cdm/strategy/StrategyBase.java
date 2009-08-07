package eu.etaxonomy.cdm.strategy;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.TimePeriod;


public abstract class StrategyBase implements IStrategy, Serializable {
	private static final long serialVersionUID = -274791080847215663L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StrategyBase.class);
	
	final static UUID uuid = UUID.fromString("2ff2b1d6-17a6-4807-a55f-f6b45bf429b7");

	abstract protected UUID getUuid();
	
	protected StrategyBase(){
	}


	/**
	 * Computes all fields recursively
	 * @param clazz
	 * @return
	 */
	protected static Map<String, Field> getAllNonStaticNonTransientFields(Class clazz, boolean includeStatic, boolean includeTransient, boolean makeAccessible) {
		Map<String, Field> result = new HashMap<String, Field>();
		//exclude static
		for (Field field: clazz.getDeclaredFields()){
			if (includeStatic || ! Modifier.isStatic(field.getModifiers())){
				if (includeTransient || ! isTransient(field)){
					field.setAccessible(makeAccessible);
					result.put(field.getName(), field);
				}
			}
		}
		
		//include superclass fields
		Class superclass = clazz.getSuperclass();
		if (CdmBase.class.isAssignableFrom(superclass)){
			result.putAll(getAllNonStaticNonTransientFields(superclass, includeStatic, includeTransient, makeAccessible));
		}
		return result;
	}
	

	/**
	 * Returns true, if field has an annotation of type javax.persistence.Annotation
	 * @param field
	 * @return
	 */
	protected static boolean isTransient(Field field) {
		for (Annotation annotation : field.getAnnotations()){
			//if (Transient.class.isAssignableFrom(annotation.annotationType())){
			if (annotation.annotationType() == Transient.class){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param fieldType
	 * @return
	 */
	protected static boolean isCollection(Class<?> fieldType) {
		if (Collection.class.isAssignableFrom(fieldType) ){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @param fieldType
	 * @return
	 */
	protected boolean isPrimitive(Class<?> fieldType) {
		if (fieldType.isPrimitive()){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @param fieldType
	 * @return
	 */
	protected boolean isSingleCdmBaseObject(Class<?> fieldType) {
		if (CdmBase.class.isAssignableFrom(fieldType)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @param fieldType
	 * @return
	 */
	protected boolean isUserType(Class<?> fieldType) {
		if (	fieldType == TimePeriod.class ||
				fieldType == DateTime.class ||
				fieldType == LSID.class ||
				fieldType == Contact.class
			){
				return true;
		}else{
			return false;
		}
	}
}
