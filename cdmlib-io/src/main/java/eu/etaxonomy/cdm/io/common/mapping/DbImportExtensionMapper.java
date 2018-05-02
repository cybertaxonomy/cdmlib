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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.database.update.DatabaseTypeNotSupportedException;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * This class maps a database attribute to CDM extension added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbImportExtensionMapper extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, IdentifiableEntity>
        implements IDbImportMapper<DbImportStateBase<?,?>,IdentifiableEntity>{
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
	@Deprecated
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
		this.extensionType = extensionType;
	}

//****************************** METHODS ***************************************************/

	@Override
	public void initialize(DbImportStateBase<?,?> state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		CdmImportBase<?, ?> currentImport = state.getCurrentIO();
		if (currentImport == null){
			throw new IllegalStateException("Current import is not available. Please make sure the the state knows about the current import (state.setCurrentImport())) !");
		}

		try {
			if (  checkDbColumnExists()){
				if (this.extensionType == null){
					this.extensionType = getExtensionType(currentImport, uuid, label, text, labelAbbrev);
				}
			}else{
				ignore = true;
			}
		} catch (DatabaseTypeNotSupportedException e) {
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

	@Override
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
		if (StringUtils.isNotBlank(dbValue)){
			Extension.NewInstance(identifiableEntity, dbValue, extensionType);
			if (extensionType == null){
				logger.warn("No extension type available for extension");
			}
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
	protected ExtensionType getExtensionType(CdmImportBase<?, ?> currentImport, UUID uuid, String label, String text, String labelAbbrev){
		ITermService termService = currentImport.getTermService();
		ExtensionType extensionType = (ExtensionType)termService.find(uuid);
		if (extensionType == null){
			extensionType = ExtensionType.NewInstance(text, label, labelAbbrev);
			extensionType.setUuid(uuid);
			//set vocabulary //TODO allow user defined vocabularies
			UUID uuidExtensionTypeVocabulary = UUID.fromString("117cc307-5bd4-4b10-9b2f-2e14051b3b20");
			IVocabularyService vocService = currentImport.getVocabularyService();
			TransactionStatus tx = currentImport.startTransaction();
			TermVocabulary<ExtensionType> voc = vocService.find(uuidExtensionTypeVocabulary);
			currentImport.getVocabularyService().saveOrUpdate(voc);
			if (voc != null){
				voc.addTerm(extensionType);
			}else{
				logger.warn("Could not find default extensionType vocabulary. Vocabulary not set for new extension type.");
			}
			//save
			termService.save(extensionType);
			currentImport.commitTransaction(tx);
		}
		return extensionType;
	}


	//not used
	@Override
    public Class<String> getTypeClass(){
		return String.class;
	}

}
