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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * TODO synonyms are no relationships anymore, maybe we need to change something here
 * @author a.mueller
 * @created 02.03.2010
 * @param <CDM_BASE>
 * @param <STATE>
 */
public class DbImportSynonymMapper<STATE extends DbImportStateBase<?,?>> extends DbImportMultiAttributeMapperBase<CdmBase, STATE> {
	private static final Logger logger = Logger.getLogger(DbImportSynonymMapper.class);

//******************************** FACTORY METHOD ***************************************************/

	/**
	 * Creates a new instance of SynonymMapper.
	 * @param dbFromAttribute
	 * @param dbToAttribute
	 * @param relatedObjectNamespace
	 * @param relTypeAttribute
	 * @param taxonRelationshipType this relationship type is taken for accepted taxa being synonyms (may be the case if data are dirty)
	 * @return
	 */
	public static DbImportSynonymMapper<?> NewInstance(String dbFromAttribute, String dbToAttribute, String relatedObjectNamespace, String relTypeAttribute, TaxonRelationshipType taxonRelationshipType){
		return new DbImportSynonymMapper(dbFromAttribute, dbToAttribute, taxonRelationshipType, relatedObjectNamespace, relTypeAttribute);
	}

//******************************* ATTRIBUTES ***************************************/
	private String fromAttribute;
	private String toAttribute;
	private TaxonRelationshipType relType;
	private String relatedObjectNamespace;
	private String citationAttribute;
	private String microCitationAttribute;
	private String relationshipTypeAttribute;
	private boolean useTaxonRelationship;


//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param relatedObjectNamespace
	 * @param mappingImport
	 */
	protected DbImportSynonymMapper(String fromAttribute, String toAttribute, TaxonRelationshipType relType, String relatedObjectNamespace, String relTypeAttribute) {
		super();
		//TODO make it a single attribute mapper
		this.fromAttribute = fromAttribute;
		this.toAttribute = toAttribute;
		this.relType = relType;
		this.relatedObjectNamespace = relatedObjectNamespace;
		this.relationshipTypeAttribute = relTypeAttribute;
		if (relTypeAttribute != null){
			logger.warn("Synonymrelationship type not yet implemented");
		}
		this.useTaxonRelationship = (relType != null);
	}

//************************************ METHODS *******************************************/

	@Override
    public CdmBase invoke(ResultSet rs, CdmBase cdmBase) throws SQLException {
		STATE state = getState();
		ICdmIO<?> currentImport = state.getCurrentIO();
		if (currentImport instanceof ICheckIgnoreMapper){
			boolean ignoreRecord = ((ICheckIgnoreMapper)currentImport).checkIgnoreMapper(this, rs);
			if (ignoreRecord){
				return cdmBase;
			}
		}

		TaxonBase<?> fromObject = (TaxonBase<?>)getRelatedObject(rs, fromAttribute);
		TaxonBase<?> toObject = (TaxonBase<?>)getRelatedObject(rs, toAttribute);
		String fromId = rs.getObject(fromAttribute)== null ? null: String.valueOf(rs.getObject(fromAttribute));
		String toId = rs.getObject(toAttribute) == null? null : String.valueOf(rs.getObject(toAttribute));

		if (toId == null){
			return fromObject;
		}

		Reference citation = CdmBase.deproxy(getRelatedObject(rs, citationAttribute), Reference.class);
		String microCitation = null;
		if (citationAttribute != null){
			microCitation = rs.getString(microCitationAttribute);
		}


		if (fromObject == null){
			String warning  = "The synonym (" + fromId + ") could not be found. Synonym not added to accepted taxon";
			logger.warn(warning);
			return cdmBase;
		}
		checkSynonymType(fromObject, fromId);

		if (toObject == null){
			String warning  = "The accepted taxon (" + toId + ") could not be found. Synonym not added to accepted taxon";
			logger.warn(warning);
			return cdmBase;
		}
		Taxon taxon = checkTaxonType(toObject, "Accepted taxon", toId);


		if (fromObject.isInstanceOf(Synonym.class)){
			SynonymType relType = SynonymType.SYNONYM_OF();
			Synonym synonym = CdmBase.deproxy(fromObject, Synonym.class);
			taxon.addSynonym(synonym, relType); //citation and micro citation not in use anymore as we do not have synonym relationships anymore
		}else if (fromObject.isInstanceOf(Taxon.class)  && this.useTaxonRelationship){
			TaxonRelationshipType type = relType;
			Taxon synonymTaxon = CdmBase.deproxy(fromObject, Taxon.class);
			synonymTaxon.addTaxonRelation(taxon, type, citation, microCitation);

		}else{
			logger.warn("Taxon is not a synonym and accepted taxa are not allowed as synonyms: " +  fromObject.getTitleCache() + "; " + fromObject.getId());
		}
		return fromObject;
	}

	/**
	 *	//TODO copied from DbImportObjectMapper. Maybe these can be merged again in future
	 * @param rs
	 * @param dbAttribute
	 * @return
	 * @throws SQLException
	 */
	protected CdmBase getRelatedObject(ResultSet rs, String dbAttribute) throws SQLException {
		CdmBase result = null;
		if (dbAttribute != null){
			Object dbValue = rs.getObject(dbAttribute);
			String id = String.valueOf(dbValue);
			DbImportStateBase<?,?> state = importMapperHelper.getState();
			result = state.getRelatedObject(relatedObjectNamespace, id);
		}
		return result;
	}


	/**
	 * Checks if cdmBase is of type Taxon
	 * @param fromObject
	 */
	private Taxon checkTaxonType(TaxonBase<?> taxonBase, String typeString, String id) {
		if (! taxonBase.isInstanceOf(Taxon.class)){
			String warning = typeString + " (" + id + ") is not of type Taxon but of type " + taxonBase.getClass().getSimpleName();
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
		}
		return (CdmBase.deproxy(taxonBase, Taxon.class));
	}

	/**
	 * Checks if cdmBase is of type Synonym
	 * @param fromObject
	 */
	private TaxonBase<?> checkSynonymType(CdmBase cdmBase, String id) {
		if (! cdmBase.isInstanceOf(Synonym.class)){
			String warning = "Synonym (" + id + ") is not of type Synonym but of type " + cdmBase.getClass().getSimpleName();
			if (! this.useTaxonRelationship){
				logger.warn(warning);
				throw new IllegalArgumentException(warning);
			}else{
				logger.info(warning);
			}
		}
		return (CdmBase.deproxy(cdmBase, TaxonBase.class));
	}


}
