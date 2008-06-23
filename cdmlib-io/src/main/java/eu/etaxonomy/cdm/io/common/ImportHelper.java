/**
 * 
 */
package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
/**
 * @author a.mueller
 *
 */
public class ImportHelper {
	private static final Logger logger = Logger.getLogger(ImportHelper.class);
	
	public static final boolean OVERWRITE = true;
	public static final boolean  NO_OVERWRITE = false;
	
	
	public static boolean setOriginalSource(IdentifiableEntity idEntity, ReferenceBase sourceReference, int sourceId){
		OriginalSource originalSource = new OriginalSource();
		originalSource.setIdInSource(String.valueOf(sourceId));
		originalSource.setCitation(sourceReference);
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
			return addValue(strValue, cdmBase, cdmAttrName, clazz, overwriteNull);
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
		Object strValue;
		strValue = getXmlInputValue(root, xmlElementName, namespace);
		return addValue(strValue, cdmBase, cdmAttrName, clazz, overwriteNull);
	}
	
	public static boolean addValue(Object sourceValue, CdmBase cdmBase, String cdmAttrName, Class clazz, boolean overwriteNull){
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
			logger.error("NoSuchMethod: " + e.getMessage());
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


}
