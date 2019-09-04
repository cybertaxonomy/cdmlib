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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author a.mueller
 * @since 24.02.2010
 */
public class DbImportTruncatedStringMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, CdmBase>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportTruncatedStringMapper.class);


    private String longTextAttribute;
    private int truncatedLength = 255;
    private Method longTextMethod;
    private ExtensionType truncatedExtentionType;
    private boolean isProtectedTitleCache;

	public static DbImportTruncatedStringMapper NewInstance (String dbAttributeString, String cdmAttributeString, String longTextAttribute, Integer truncatedLength) {
		boolean obligatory = false;
		Object defaultValue = null;
		return new DbImportTruncatedStringMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory, longTextAttribute, null, truncatedLength, false);
	}

    /**
     * Creates an instance using a "truncated extension type" extension to store the complete
     * text. If isProtectedTitleCache is <code>true</code> and the object is an identifiable entity
     * the value is stored in the titleCache field with protected flag = <code>true</code>
     */
    public static DbImportTruncatedStringMapper NewInstance (String dbAttributeString, String cdmAttributeString, ExtensionType truncatedExtentionType, Integer truncatedLength, boolean isProtectedTitleCache) {
        boolean obligatory = false;
        Object defaultValue = null;
        return new DbImportTruncatedStringMapper(dbAttributeString, cdmAttributeString, defaultValue, obligatory, null, truncatedExtentionType, truncatedLength, isProtectedTitleCache);
    }

// ************************************* CONSTRUCTOR *******************************/

	protected DbImportTruncatedStringMapper(String dbAttributeString, String cdmAttributeString,
	        Object defaultValue, boolean obligatory, String longTextAttribute,
	        ExtensionType extensionType, Integer truncatedLength, boolean isProtectedTitleCache) {
		super(dbAttributeString, cdmAttributeString, defaultValue, obligatory);
		this.longTextAttribute = longTextAttribute;
		this.truncatedExtentionType = extensionType;
		if (truncatedLength != null){
		    this.truncatedLength = truncatedLength;
		}
		this.isProtectedTitleCache = isProtectedTitleCache;
	}

// ******************************* METHODS ******************************/
	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		super.initialize(state, destinationClass);
		if (longTextAttribute != null){
		    Class<?> parameterType = getTypeClass();
		    String methodName = ImportHelper.getSetterMethodName(parameterType, longTextAttribute);
		    try {
		        longTextMethod = targetClass.getMethod(methodName, parameterType);
		    } catch (SecurityException | NoSuchMethodException e) {
		        throw new RuntimeException(e);
		    }
		}
	}

	@Override
	protected CdmBase doInvoke(CdmBase cdmBase, Object value) {
		String strValue = (String)value;
		strValue = StringUtils.isBlank(strValue)? null:strValue.trim();
        if (strValue != null && strValue.length() > truncatedLength){
			return invokeTruncatedValue(cdmBase, strValue);
		}else{
			return doNotTruncate(cdmBase, strValue);

		}
	}

    private CdmBase doNotTruncate(CdmBase cdmBase, String value) {
        if (isProtectedTitleCache && cdmBase.isInstanceOf(IdentifiableEntity.class)){
            (CdmBase.deproxy(cdmBase, IdentifiableEntity.class)).setTitleCache(value, true);
            return cdmBase;
        }else{
            return super.doInvoke(cdmBase, value);
        }
    }

    private CdmBase invokeTruncatedValue(CdmBase cdmBase, Object value) {
		CdmBase result;
		String truncatedValue = ((String)value).substring(0, truncatedLength - 3) + "...";
		result = doNotTruncate(cdmBase, truncatedValue);
		if (longTextMethod != null){
		    try {
		        longTextMethod.invoke(cdmBase, value);
		        return result;
		    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
		        e.printStackTrace();
		    }
		}else if (truncatedExtentionType != null && cdmBase.isInstanceOf(IdentifiableEntity.class)){
		    (CdmBase.deproxy(cdmBase, IdentifiableEntity.class)).addExtension((String)value, truncatedExtentionType);
		    return cdmBase;
		}
		throw new RuntimeException("Problems when invoking long text method");
	}

	@Override
	public Class getTypeClass() {
		return String.class;
	}
}
