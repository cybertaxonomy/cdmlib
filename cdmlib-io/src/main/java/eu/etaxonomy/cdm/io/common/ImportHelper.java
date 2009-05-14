/**
 * 
 */
package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
/**
 * @author a.mueller
 *
 */
public class ImportHelper {
	private static final Logger logger = Logger.getLogger(ImportHelper.class);
	
	public static final boolean OVERWRITE = true;
	public static final boolean  NO_OVERWRITE = false;
	
	public static final boolean OBLIGATORY = true;
	public static final boolean  FACULTATIVE = false;
	
	

	public static boolean setOriginalSource(IdentifiableEntity idEntity, ReferenceBase sourceReference, long sourceId, String namespace){
		return setOriginalSource(idEntity, sourceReference, String.valueOf(sourceId), namespace);
	}
	
	/**
	 * Adds an original source object to the identifiable entity. 
	 * @param idEntity
	 * @param sourceReference
	 * @param sourceId
	 * @return
	 */
	public static boolean setOriginalSource(IdentifiableEntity idEntity, ReferenceBase sourceReference, String sourceId, String namespace){
		OriginalSource originalSource = new OriginalSource();
		originalSource.setIdInSource(sourceId);
		originalSource.setCitation(sourceReference);
		originalSource.setIdNamespace(namespace);
		idEntity.addSource(originalSource);
		return true;
	}
	
	
	public static boolean addStringValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, String.class, OVERWRITE);
	}
	
	public static boolean addStringValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName, boolean overwriteNull){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, String.class, overwriteNull);
	}
		
	public static boolean addBooleanValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, boolean.class, OVERWRITE);
	}

	public static boolean addValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName, Class clazz, boolean overwriteNull){
		Object strValue;
		try {
			strValue = rs.getObject(dbAttrName);
			return addValue(strValue, cdmBase, cdmAttrName, clazz, overwriteNull, OBLIGATORY);
		}catch (SQLException e) {
			logger.error("SQLException: " +  e);
			return false;
		}

	}
	
	
	public static boolean addXmlStringValue(Element root, CdmBase cdmBase, String xmlElementName, Namespace namespace, String cdmAttrName){
		return addXmlValue(root, cdmBase, xmlElementName, namespace, cdmAttrName, String.class, OVERWRITE);
	}
	
	public static boolean addXmlStringValue(Element root, CdmBase cdmBase, String xmlElementName, Namespace namespace, String cdmAttrName, boolean overwriteNull){
		return addXmlValue(root, cdmBase, xmlElementName, namespace, cdmAttrName, String.class, overwriteNull);
	}
		
	public static boolean addXmlBooleanValue(Element root, CdmBase cdmBase, String xmlElementName, Namespace namespace, String cdmAttrName){
		return addXmlValue(root, cdmBase, xmlElementName, namespace, cdmAttrName, boolean.class, OVERWRITE);
	}


	public static boolean addXmlValue(Element root, CdmBase cdmBase, String xmlElementName, Namespace namespace, String cdmAttrName, Class clazz, boolean overwriteNull){
		return addXmlValue(root, cdmBase, xmlElementName, namespace, cdmAttrName, clazz, overwriteNull, OBLIGATORY);
	}

	public static boolean addXmlValue(Element root, CdmBase cdmBase, String xmlElementName, Namespace namespace, String cdmAttrName, Class clazz, boolean overwriteNull, boolean obligat){
		Object strValue;
		strValue = getXmlInputValue(root, xmlElementName, namespace);
		return addValue(strValue, cdmBase, cdmAttrName, clazz, overwriteNull, obligat);
	}
	
	public static boolean addValue(Object sourceValue, CdmBase cdmBase, String cdmAttrName, Class clazz, boolean overwriteNull, boolean obligat){
		String methodName;
//		Object strValue;
		try {
			if (overwriteNull == NO_OVERWRITE && sourceValue == null ){
				if (logger.isDebugEnabled()) { logger.debug("no overwrite for NULL-value");}
				return true;
			}
			if (logger.isDebugEnabled()) { logger.debug("addValue: " + sourceValue);}
			if (clazz == boolean.class || clazz == Boolean.class){
				if (cdmAttrName == null || cdmAttrName.length() < 1 ){
					throw new IllegalArgumentException("boolean CdmAttributeName should have atleast 3 characters");
				}
				methodName = "set" + cdmAttrName.substring(2, 3).toUpperCase() + cdmAttrName.substring(3) ;
			}else if(clazz == String.class) {
				if (cdmAttrName == null || cdmAttrName.length() < 1 ){
					throw new IllegalArgumentException("CdmAttributeName should have atleast 1 character");
				}
				methodName = "set" + cdmAttrName.substring(0, 1).toUpperCase() + cdmAttrName.substring(1) ;
			}else{
				logger.error("Class not supported: " + clazz.toString());
				return false;
			}
			Method cdmMethod = cdmBase.getClass().getMethod(methodName, clazz);
			cdmMethod.invoke(cdmBase, sourceValue);
			return true;
		} catch (NullPointerException e) {
			logger.error("NullPointerException: " + e.getMessage());
			return false;
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException: " + e.getMessage());
			return false;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: " + e.getMessage());
			return false;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getMessage());
			return false;
		}catch (SecurityException e) {
			logger.error("SecurityException: " + e.getMessage());
			return false;
		} catch (NoSuchMethodException e) {
			if (obligat){
				logger.error("NoSuchMethod: " + e.getMessage());
				return false;
			}else{
				if (logger.isDebugEnabled()){ logger.debug("NoSuchMethod: " + e.getMessage());}
				return true;
			}
		}
		
	}

	private static boolean valuesAreNull(List<Object> values){
		for (Object sourceValue : values.toArray()){
			if (sourceValue != null){
				return false;
			}
		}
		return true;
	}
	
	public static boolean addMultipleValues(List<Object> sourceValues, CdmBase cdmBase, String cdmAttrName, List<Class> classes, boolean overwriteNull, boolean obligat){
		String methodName;
//		Object strValue;
		try {
			
			if (overwriteNull == NO_OVERWRITE && valuesAreNull(sourceValues)){
				if (logger.isDebugEnabled()) { logger.debug("no overwrite for NULL-value");}
				return true;
			}
			if (logger.isDebugEnabled()) { logger.debug("addValues: " + sourceValues.toString());}
			
			
			if (cdmAttrName == null || cdmAttrName.length() < 1 ){
				throw new IllegalArgumentException("CdmAttributeName should have atleast 1 character");
			}
			methodName = "add" + cdmAttrName.substring(0, 1).toUpperCase() + cdmAttrName.substring(1) ;
			
			Class[] classArray = classes.toArray(new Class[0]);
			Method cdmMethod = cdmBase.getClass().getMethod(methodName, classArray);
			cdmMethod.invoke(cdmBase, sourceValues.toArray());
			return true;
		} catch (NullPointerException e) {
			logger.error("NullPointerException: " + e.getMessage());
			return false;
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException: " + e.getMessage());
			return false;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: " + e.getMessage());
			return false;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getMessage());
			return false;
		}catch (SecurityException e) {
			logger.error("SecurityException: " + e.getMessage());
			return false;
		} catch (NoSuchMethodException e) {
			if (obligat){
				logger.error("NoSuchMethod: " + e.getMessage());
				return false;
			}else{
				if (logger.isDebugEnabled()){ logger.debug("NoSuchMethod: " + e.getMessage());}
				return true;
			}
		}
		
	}
	
	public static boolean addAnnotationFromResultSet(ResultSet rs, String attributeName, AnnotatableEntity cdmBase, Language language){
		try {
			String value = rs.getString(attributeName);
			if (CdmUtils.Nz(value).equals("")){
				String strAnnotation = attributeName + ": " + value;
				Annotation annoatation = Annotation.NewInstance(strAnnotation, language);
				cdmBase.addAnnotation(annoatation);

			}
			return true;
		} catch (SQLException e) {
			logger.warn(e);
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	public static Object getXmlInputValue(Element root, String xmlElementName, Namespace namespace){
		Object result = null; 
		Element child = root.getChild(xmlElementName, namespace);
		if (child != null){
			result = child.getText().trim();
		}
		return result;
	}
	
	
	public static TimePeriod getDatePublished(String refYear){
		//FIXME until now only quick and dirty and wrong
		if (refYear == null){
			return null;
		}
		String[] years = refYear.split("-");
		DateTime dtStart = null;
		DateTime dtEnd = null;
		
		if (years.length > 2 || years.length <= 0){
			logger.warn("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX getDatePublished");
		}else {
			dtStart = parseSingleDate(years[0]);
			
			if (years.length >= 2){
				dtEnd = parseSingleDate(years[1]);
			}
		}
		TimePeriod result = TimePeriod.NewInstance(dtStart, dtEnd);
		return result;
	}
	
	private static DateTime parseSingleDate(String singleDateString){
		//FIXME until now only quick and dirty and wrong
		DateTime dt = new DateTime(9999, 12, 30, 0,0,0,0);
		if (CdmUtils.isNumeric(singleDateString)){
			try {
				Integer year = Integer.valueOf(singleDateString.trim());
				if (year > 1750 && year < 2050){
					dt = dt.withYear(year);
				}
			} catch (NumberFormatException e) {
				logger.debug("Not a Integer format in getCalendar()");
			}
		}
		return dt;

	}

	//************** EXPORT *******************/
	
	
	public static<T extends Object> T getValue(CdmBase cdmBase, String cdmAttrName, boolean isBoolean, boolean obligatory){
		String methodName;
		T result;
		try {
			if (isBoolean){
				if (cdmAttrName == null || cdmAttrName.length() < 3 || !cdmAttrName.startsWith("is")){
					throw new IllegalArgumentException("boolean CdmAttributeName should have atleast 3 characters and start with 'is': " + cdmAttrName);
				}
				methodName = cdmAttrName ;
			}else {
				if (cdmAttrName == null || cdmAttrName.length() < 1 ){
					throw new IllegalArgumentException("CdmAttributeName should have atleast 1 character");
				}
				methodName = "get" + cdmAttrName.substring(0, 1).toUpperCase() + cdmAttrName.substring(1) ;
			}
//			else{
//				logger.error("Class not supported: " + clazz.toString());
//				return null;
//			}
			Method cdmMethod = cdmBase.getClass().getMethod(methodName);
			result = (T)cdmMethod.invoke(cdmBase);
			return result;
		} catch (NullPointerException e) {
			logger.error("NullPointerException: " + e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException: " + e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: " + e.getMessage());
			return null;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException: " + e.getMessage());
			return null;
		}catch (SecurityException e) {
			logger.error("SecurityException: " + e.getMessage());
			return null;
		} catch (NoSuchMethodException e) {
			if (obligatory){
				logger.error("NoSuchMethod: " + e.getMessage());
				return null;
			}else{
				if (logger.isDebugEnabled()){ logger.debug("NoSuchMethod: " + e.getMessage());}
				return null;
			}
		}
		
	}

	
	
	
	
	//******* old *****************
	
//	private static Calendar getCalendar(String strYear){
//		//FIXME until now only quick and dirty and wrong
//		Calendar cal = Calendar.getInstance();
//		cal.set(9999, Calendar.DECEMBER, 30, 0, 0, 0);
//		if (CdmUtils.isNumeric(strYear)){
//			try {
//				Integer year = Integer.valueOf(strYear.trim());
//				if (year > 1750 && year < 2030){
//					cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
//				}
//			} catch (NumberFormatException e) {
//				logger.debug("Not a Integer format in getCalendar()");
//			}
//		}
//		return cal;
//	}


}
