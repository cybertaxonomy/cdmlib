// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.mail.MethodNotSupportedException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * This class maps a database attribute to CDM extension added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportExtensionMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, IdentifiableEntity> implements IDbImportMapper<DbImportStateBase<?,?>,IdentifiableEntity>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportExtensionMapper.class);
	
//************************** FACTORY METHODS ***************************************************************/
	
	/**
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	public static DbImportExtensionMapper NewInstance(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev){
		return new DbImportExtensionMapper(dbAttributeString, uuid, label, text, labelAbbrev);
	}
	
	public static DbImportExtensionMapper NewInstance(String dbAttributeString, ExtensionType extensionType){
		return new DbImportExtensionMapper(dbAttributeString, extensionType);
	}
	
//***************** VARIABLES **********************************************************/
	
	private ExtensionType extensionType;
	private String label;
	private String text;
	private String labelAbbrev;
	private UUID uuid;

//******************************** CONSTRUCTOR *****************************************************************/
	/**
	 * @param dbAttributeString
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 */
	private DbImportExtensionMapper(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev) {
		super(dbAttributeString, dbAttributeString);
		this.uuid = uuid;
		this.label = label;
		this.text = text;
		this.labelAbbrev = labelAbbrev;
	}
	
	/**
	 * @param dbAttributeString
	 * @param extensionType
	 */
	private DbImportExtensionMapper(String dbAttributeString, ExtensionType extensionType) {
		super(dbAttributeString, dbAttributeString);
		this.extensionType  = extensionType;
	}
		
//****************************** METHODS ***************************************************/
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		CdmImportBase<?, ?> currentImport = state.getCurrentIO();
		if (currentImport == null){
			throw new IllegalStateException("Current import is not available. Please make sure the the state knows about the current import (state.setCurrentImport())) !"); 
	}
		ITermService service = currentImport.getTermService();
		
		try {
			if (  checkDbColumnExists()){
				if (this.extensionType == null){
					this.extensionType = getExtensionType(service, uuid, label, text, labelAbbrev);
				}
			}else{
				ignore = true;
			}
		} catch (MethodNotSupportedException e) {
			//do nothing  - checkDbColumnExists is not possible
		}
	}
	

	/**
	 * @param valueMap
	 * @param cdmBase
	 * @return
	 */
	public boolean invoke(Map<String, Object> valueMap, CdmBase cdmBase){
		Object dbValueObject = valueMap.get(this.getSourceAttribute().toLowerCase());
		String dbValue = dbValueObject == null? null: dbValueObject.toString();
		return invoke(dbValue, cdmBase);
	}
	
	/**
	 * @param dbValue
	 * @param cdmBase
	 * @return
	 */
	private boolean invoke(String dbValue, CdmBase cdmBase){
		if (ignore){
			return true;
		}
		if (cdmBase instanceof IdentifiableEntity){
			IdentifiableEntity identifiableEntity = (IdentifiableEntity) cdmBase;
			invoke(dbValue, identifiableEntity);
			return true;
		}else{
			throw new IllegalArgumentException("extended object must be of type identifiable entity.");
		}
		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbSingleAttributeImportMapperBase#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public IdentifiableEntity invoke(ResultSet rs, IdentifiableEntity identifiableEntity) throws SQLException {
		String dbValue = rs.getString(getSourceAttribute());
		return invoke(dbValue, identifiableEntity);
	}
	
	/**
	 * @param dbValue
	 * @param identifiableEntity
	 * @return
	 */
	private IdentifiableEntity invoke(String dbValue, IdentifiableEntity identifiableEntity){
		if (ignore){
		return identifiableEntity;
	}
		if (CdmUtils.isNotEmpty(dbValue)){
			Extension.NewInstance(identifiableEntity, dbValue, extensionType);
		}
		return identifiableEntity;
	}
	
	
	/**
	 * @param service
	 * @param uuid
	 * @param label
	 * @param text
	 * @param labelAbbrev
	 * @return
	 */
	protected ExtensionType getExtensionType(ITermService service, UUID uuid, String label, String text, String labelAbbrev){
		ExtensionType extensionType = (ExtensionType)service.find(uuid);
		if (extensionType == null){
			extensionType = ExtensionType.NewInstance(text, label, labelAbbrev);
			extensionType.setUuid(uuid);
			service.save(extensionType);
		}
		return extensionType;
	}


	//not used
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	public Class<String> getTypeClass(){
		return String.class;
	}

	

}
