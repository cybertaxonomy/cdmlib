/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
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



	public static boolean setOriginalSource(ISourceable sourceable, Reference sourceReference, long sourceId, String namespace){
		return setOriginalSource(sourceable, sourceReference, String.valueOf(sourceId), namespace);
	}

	/**
	 * Adds an original source object to the identifiable entity.
	 * @param idEntity
	 * @param sourceReference
	 * @param sourceId
	 * @return
	 */
	public static boolean setOriginalSource(ISourceable sourceable, Reference sourceReference, String sourceId, String namespace){
		IOriginalSource<?> originalSource;
		OriginalSourceType type = OriginalSourceType.Import;
		if (HibernateProxyHelper.isInstanceOf(sourceable, IdentifiableEntity.class)){
			originalSource = IdentifiableSource.NewInstance(type, sourceId, namespace, sourceReference, null);
		}else if (HibernateProxyHelper.isInstanceOf(sourceable, DescriptionElementBase.class)){
			originalSource = DescriptionElementSource.NewInstance(type, sourceId, namespace, sourceReference, null);
		}else{
			throw new ClassCastException("Unknown implementing class for ISourceable "+ sourceable.getClass() + " . Not supported bei ImportHelper.");
		}
		sourceable.addSource(originalSource);
		return true;
	}

	public static boolean addStringValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName, boolean blankToNull){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, String.class, OVERWRITE, blankToNull);
	}

//	public static boolean addStringValue(ResultSet rs, CdmBase cdmBase, String dbAttrName, String cdmAttrName, boolean overwriteNull){
//		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, String.class, overwriteNull);
//	}

	public static boolean addBooleanValue(ResultSet rs, ICdmBase cdmBase, String dbAttrName, String cdmAttrName){
		return addValue(rs, cdmBase, dbAttrName, cdmAttrName, boolean.class, OVERWRITE, false);
	}

	public static boolean addValue(ResultSet rs, ICdmBase cdmBase, String dbAttrName, String cdmAttrName, Class clazz, boolean overwriteNull, boolean blankToNull){
		Object strValue;
		try {
			strValue = rs.getObject(dbAttrName);
			if (strValue instanceof String){
				strValue = ((String)strValue).trim();
				if (blankToNull &&  strValue.equals("")){
					strValue = null;
				}
			}
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

	public static boolean addValue(Object sourceValue, ICdmBase cdmBase, String cdmAttrName, Class<?> clazz, boolean overwriteNull, boolean obligat){
		String methodName;
//		Object strValue;
		try {
			if (overwriteNull == NO_OVERWRITE && sourceValue == null ){
				if (logger.isDebugEnabled()) { logger.debug("no overwrite for NULL-value");}
				return true;
			}
			if (logger.isDebugEnabled()) { logger.debug("addValue: " + sourceValue);}
			methodName = getSetterMethodName(clazz, cdmAttrName);
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

	/**
	 * @param clazz either boolean or other class (for boolean the naming strategy is different !)
	 * @param cdmAttrName
	 * @return
//	 * @throws IllegalArgumentException if a clazz is not yet supported
	 */
	public static String getSetterMethodName(Class<?> clazz, String cdmAttrName) {
		return getSetterPutterMethodName(clazz, cdmAttrName, "set");
	}

	/**
	 * @param clazz either boolean or other class (for boolean the naming strategy is different !)
	 * @param cdmAttrName
	 * @return
//	 * @throws IllegalArgumentException if a clazz is not yet supported
	 */
	public static String getPutterMethodName(Class<?> clazz, String cdmAttrName) {
		return getSetterPutterMethodName(clazz, cdmAttrName, "put");
	}

	/**
	 * @param clazz either boolean or other class (for boolean the naming strategy is different !)
	 * @param cdmAttrName
	 * @return
//	 * @throws IllegalArgumentException if a clazz is not yet supported
	 */
	private static String getSetterPutterMethodName(Class<?> clazz, String cdmAttrName, String prefix) {
		String methodName;
		if (clazz == boolean.class || clazz == Boolean.class){
			if (cdmAttrName == null || cdmAttrName.length() < 1 ){
				throw new IllegalArgumentException("boolean CdmAttributeName should have atleast 3 characters");
			}
			methodName = prefix + cdmAttrName.substring(2, 3).toUpperCase() + cdmAttrName.substring(3) ;
		}else  {
			if (cdmAttrName == null || cdmAttrName.length() < 1 ){
				throw new IllegalArgumentException("CdmAttributeName should have atleast 1 character");
			}
			methodName = prefix + cdmAttrName.substring(0, 1).toUpperCase() + cdmAttrName.substring(1) ;
		}
		return methodName;
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

			Class<?>[] classArray = classes.toArray(new Class[0]);
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


	public static VerbatimTimePeriod getDatePublished(String refYear){
	    VerbatimTimePeriod resultNew;
		try {
			resultNew = TimePeriodParser.parseStringVerbatim(refYear);
		} catch (IllegalArgumentException e) {
			logger.warn("RefYear could not be parsed: " + refYear);
			resultNew = null;
		}
		return resultNew;
	}

	//************** EXPORT *******************/


	public static<T extends Object> T getValue(CdmBase cdmBase, String cdmAttrName, boolean isBoolean, boolean obligatory){
		String methodName;
		T result;
		try {
			methodName = getGetterMethodName(cdmAttrName, isBoolean);
			if (cdmBase.isInstanceOf(TaxonName.class)){
				cdmBase = CdmBase.deproxy(cdmBase);
			}
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

	/**
	 * @param cdmAttrName
	 * @param isBoolean
	 * @return
	 */
	public static String getGetterMethodName(String cdmAttrName, boolean isBoolean) {
		String methodName;
		if (isBoolean){
			if (cdmAttrName == null || cdmAttrName.length() < 3 ||  !( cdmAttrName.startsWith("is") || cdmAttrName.startsWith("use"))     ){
				throw new IllegalArgumentException("boolean CdmAttributeName should have atleast 3 characters and start with 'is' or 'use': " + cdmAttrName);
			}
			methodName = cdmAttrName ;
		}else {
			if (cdmAttrName == null || cdmAttrName.length() < 1 ){
				throw new IllegalArgumentException("CdmAttributeName should have atleast 1 character");
			}
			methodName = "get" + cdmAttrName.substring(0, 1).toUpperCase() + cdmAttrName.substring(1) ;
		}
		return methodName;
	}

}
