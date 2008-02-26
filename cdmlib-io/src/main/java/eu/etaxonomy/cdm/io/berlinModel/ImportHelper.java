/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
/**
 * @author a.mueller
 *
 */
public class ImportHelper {
	private static final Logger logger = Logger.getLogger(ImportHelper.class);
	
	
	public static boolean addStringValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, String.class);
	}
	
	
	public static boolean addBooleanValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, boolean.class);
	}

	public static boolean addValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName, Class clazz){
		try {
			String methodName;
			Object strValue = rs.getObject(dbAttrName);
			if (logger.isDebugEnabled()) { logger.debug("addValue: " + strValue);}
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
			cdmMethod.invoke(cdmBase, strValue);
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
		}catch (SQLException e) {
			logger.error("SQLException: " +  e);
			return false;
		}

	}	

}
