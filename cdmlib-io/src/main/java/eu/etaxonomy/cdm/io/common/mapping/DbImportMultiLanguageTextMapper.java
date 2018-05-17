/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.mueller
 * @since 15.03.2010
 * @version 1.0
 */
public class DbImportMultiLanguageTextMapper<CDMBASE extends CdmBase> extends DbImportMultiAttributeMapperBase<CDMBASE, DbImportStateBase<?,?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportMultiLanguageTextMapper.class);

//****************************** FACTORY METHOD ********************************************/
	
	public static DbImportMultiLanguageTextMapper<CdmBase> NewInstance(String dbTextAttribute, String dbLanguageAttribute, String languageNamespace, String cdmMultiLanguageTextAttribute){
		return new DbImportMultiLanguageTextMapper<CdmBase>(dbTextAttribute, dbLanguageAttribute, languageNamespace, cdmMultiLanguageTextAttribute);
	}
	
	
//***************************** VARIABLES *************************************************/

	private String dbTextAttribute;
	private String dbLanguageAttribute;
	private String languageNamespace;
	private String cdmMultiLanguageTextAttribute;
	private Method destinationMethod;
	
//***************************** CONSTRUCTOR *************************************************/

	protected DbImportMultiLanguageTextMapper(String dbTextAttribute, String dbLanguageAttribute, String languageNamespace, String cdmMultiLanguageTextAttribute){
		super();
		this.dbTextAttribute = dbTextAttribute;
		this.dbLanguageAttribute = dbLanguageAttribute;
		this.languageNamespace = languageNamespace;
		this.cdmMultiLanguageTextAttribute = cdmMultiLanguageTextAttribute;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportMultiAttributeMapperBase#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		super.initialize(state, destinationClass);
		String methodName = "";
		try {
			Class<?> targetClass = getTargetClass(destinationClass);
			Class<?> parameterType = getTypeClass();
			methodName = ImportHelper.getPutterMethodName(targetClass, cdmMultiLanguageTextAttribute);
			destinationMethod = targetClass.getMethod(methodName, parameterType);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			String message = "NoSuchMethod in MultiLanguageTextMapper for attribute %s, destination method %s and destination class %s";
			message = String.format(message, this.dbTextAttribute,methodName, destinationClass);
			throw new RuntimeException(message, e);
		}
	}

	/**
	 * This method is used to be compliant with DbSingleAttributeImport
	 * @return
	 */
	protected Class<?> getTypeClass() {
		return LanguageString.class;
	}

	/**
	 * This method is used to be compliant with DbSingleAttributeImport
	 * @param destinationClass
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	protected Class<?> getTargetClass(Class<?> destinationClass) throws SecurityException, NoSuchMethodException{
		return destinationClass;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CDMBASE invoke(ResultSet rs, CDMBASE cdmBase) throws SQLException {
		//TODO make this a definedTermMapper
		Language language = (Language)getRelatedObject(rs, languageNamespace, dbLanguageAttribute);
		String text = getStringDbValue(rs, dbTextAttribute);
		
		LanguageString languageString = LanguageString.NewInstance(text, language);
		try {
			destinationMethod.invoke(cdmBase, languageString);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e);
		}
		return cdmBase;
	}
	

}
