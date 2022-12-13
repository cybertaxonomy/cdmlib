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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.database.update.DatabaseTypeNotSupportedException;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * This class maps a database attribute to CDM extension added to the target class
 * TODO maybe this class should not inherit from DbSingleAttributeImportMapperBase
 * as it does not map to a single attribute
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbImportMarkerMapper
        extends DbSingleAttributeImportMapperBase<DbImportStateBase<?,?>, AnnotatableEntity>
        implements IDbImportMapper<DbImportStateBase<?,?>,AnnotatableEntity>{

    private static final Logger logger = LogManager.getLogger();

//************************** FACTORY METHODS ***************************************************************/

	public static DbImportMarkerMapper NewInstance(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev, Boolean ignoreValue){
		return new DbImportMarkerMapper(dbAttributeString, uuid, label, text, labelAbbrev, ignoreValue);
	}

	public static DbImportMarkerMapper NewInstance(String dbAttributeString, MarkerType markerType, Boolean ignoreValue){
		return new DbImportMarkerMapper(dbAttributeString, markerType, ignoreValue);
	}

//***************** VARIABLES **********************************************************/

	private MarkerType markerType;
	private String label;
	private String text;
	private String labelAbbrev;
	private UUID uuid;
	private Boolean ignoreValue;

//******************************** CONSTRUCTOR *****************************************************************/

	private DbImportMarkerMapper(String dbAttributeString, UUID uuid, String label, String text, String labelAbbrev, Boolean ignoreValue) {
		super(dbAttributeString, dbAttributeString);
		this.uuid = uuid;
		this.label = label;
		this.text = text;
		this.labelAbbrev = labelAbbrev;
		this.ignoreValue = ignoreValue;
	}

	private DbImportMarkerMapper(String dbAttributeString, MarkerType markerType, Boolean ignoreValue) {
		super(dbAttributeString, dbAttributeString);
		this.markerType = markerType;
		this.ignoreValue = ignoreValue;
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
				if (this.markerType == null){
					this.markerType = getMarkerType(currentImport, uuid, label, text, labelAbbrev);
				}
			}else{
				ignore = true;
			}
		} catch (DatabaseTypeNotSupportedException e) {
			//do nothing  - checkDbColumnExists is not possible
		}
	}

	public boolean invoke(Map<String, Object> valueMap, CdmBase cdmBase){
		Object dbValueObject = valueMap.get(this.getSourceAttribute().toLowerCase());
		Boolean dbValue = (Boolean) (dbValueObject == null? null: dbValueObject);
		return invoke(dbValue, cdmBase);
	}

	private boolean invoke(Boolean dbValue, CdmBase cdmBase){
		if (ignore){
			return true;
		}
		if (cdmBase instanceof AnnotatableEntity){
			AnnotatableEntity annotatableEntity = (AnnotatableEntity) cdmBase;
			invoke(dbValue, annotatableEntity);
			return true;
		}else{
			throw new IllegalArgumentException("marked object must be of type annotatable entity.");
		}

	}

	@Override
    public AnnotatableEntity invoke(ResultSet rs, AnnotatableEntity annotatableEntity) throws SQLException {
		Boolean dbValue = (Boolean)rs.getObject(getSourceAttribute());
		return invoke(dbValue, annotatableEntity);
	}

	private AnnotatableEntity invoke(Boolean dbValue, AnnotatableEntity annotatableEntity){
		if (ignore){
			return annotatableEntity;
		}
		if (dbValue != null && ! dbValue.equals(this.ignoreValue)){
			Marker.NewInstance(annotatableEntity, dbValue, this.markerType);
			if (this.markerType == null){
				logger.warn("No marker type available for marker");
			}
		}
		return annotatableEntity;
	}

	protected MarkerType getMarkerType(CdmImportBase<?, ?> currentImport, UUID uuid, String label, String text, String labelAbbrev){
		ITermService termService = currentImport.getTermService();
		MarkerType markerType = (MarkerType)termService.find(uuid);
		if (markerType == null){
			//create object
			markerType = MarkerType.NewInstance(text, label, labelAbbrev);
			markerType.setUuid(uuid);
			//set vocabulary //TODO allow user defined vocabularies
			UUID uuidMarkerTypeVocabulary = UUID.fromString("19dffff7-e142-429c-a420-5d28e4ebe305");
			IVocabularyService vocService = currentImport.getVocabularyService();
			TransactionStatus tx = currentImport.startTransaction();
			TermVocabulary voc = vocService.find(uuidMarkerTypeVocabulary);
			if (voc != null){
				voc.addTerm(markerType);
			}else{
				logger.warn("Could not find default markerType vocabulary. Vocabulary not set for new marker type.");
			}
			//save
			termService.save(markerType);
			currentImport.commitTransaction(tx);
		}
		return markerType;
	}

	//not used
	@Override
    public Class<Boolean> getTypeClass(){
		return Boolean.class;
	}
}