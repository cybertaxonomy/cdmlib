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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.erms.ICheckIgnoreMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
/**
 * @author a.mueller
 * @created 02.03.2010
 * @version 1.0
 * @param <CDM_BASE>
 * @param <STATE>
 */
public class DbImportSynonymMapper<STATE extends DbImportStateBase> extends DbImportMultiAttributeMapperBase<CdmBase, STATE> {
	private static final Logger logger = Logger.getLogger(DbImportSynonymMapper.class);
	
//******************************** FACTORY METHOD ***************************************************/
	
	public static DbImportSynonymMapper<?> NewInstance(String dbFromAttribute, String dbToAttribute, String relatedObjectNamespace, String relTypeAttribute){
		return new DbImportSynonymMapper(dbFromAttribute, dbToAttribute, null, relatedObjectNamespace, relTypeAttribute);
	}
	
//******************************* ATTRIBUTES ***************************************/
	private String fromAttribute;
	private String toAttribute;
//	private TaxonRelationshipType relType;
	private String relatedObjectNamespace;
	private String citationAttribute;
	private String microCitationAttribute;
	private String relationshipTypeAttribute;
	
	
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
//		this.relType = relType;
		this.relatedObjectNamespace = relatedObjectNamespace;
		this.relationshipTypeAttribute = relTypeAttribute;
		if (relTypeAttribute != null){
			logger.warn("Synonymrelationship type not yet implemented");
		}
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CdmBase invoke(ResultSet rs, CdmBase cdmBase) throws SQLException {
		STATE state = getState();
		ICdmIO currentImport = state.getCurrentIO();
		if (currentImport instanceof ICheckIgnoreMapper){
			boolean ignoreRecord = ((ICheckIgnoreMapper)currentImport).checkIgnoreMapper(this, rs);
			if (ignoreRecord){
				return cdmBase;
			}
		}
		
		TaxonBase fromObject = (TaxonBase)getRelatedObject(rs, fromAttribute);
		TaxonBase toObject = (TaxonBase)getRelatedObject(rs, toAttribute);
		String fromId = String.valueOf(rs.getObject(fromAttribute));
		String toId = String.valueOf(rs.getObject(toAttribute));
		
		//TODO cast
		ReferenceBase citation = (ReferenceBase)getRelatedObject(rs, citationAttribute);
		String microCitation = null;
		if (citationAttribute != null){
			microCitation = rs.getString(microCitationAttribute);
		}

		
		if (fromObject == null){
			String warning  = "The synonym (" + fromId + ") could not be found. Synonym not added to accepted taxon";
			logger.warn(warning);
			return cdmBase;
		}
		Synonym synonym = checkSynonymType(fromObject, fromId);
		
		if (toObject == null){
			String warning  = "The accepted taxon (" + toId + ") could not be found. Synonym not added to accepted taxon";
			logger.warn(warning);
			return cdmBase;
		}
		Taxon taxon = checkTaxonType(toObject, "Accepted taxon", toId);
		
		SynonymRelationshipType relType = SynonymRelationshipType.SYNONYM_OF();
		
		taxon.addSynonym(synonym, relType, citation, microCitation);
		return synonym;
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
			DbImportStateBase state = importMapperHelper.getState();
			result = state.getRelatedObject(relatedObjectNamespace, id);
		}
		return result;
	}
	

	/**
	 * Checks if cdmBase is of type Taxon 
	 * @param fromObject
	 */
	private Taxon checkTaxonType(TaxonBase taxonBase, String typeString, String id) {
		if (! taxonBase.isInstanceOf(Taxon.class)){
			String warning = typeString + " (" + id + ") is not of type Taxon but of type " + taxonBase.getClass().getSimpleName();
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
		}
		return (taxonBase.deproxy(taxonBase, Taxon.class));
	}
	
	/**
	 * Checks if cdmBase is of type Synonym 
	 * @param fromObject
	 */
	private Synonym checkSynonymType(CdmBase cdmBase, String id) {
		if (! cdmBase.isInstanceOf(Synonym.class)){
			String warning = "Synonym (" + id + ") is not of type Synonym but of type " + cdmBase.getClass().getSimpleName();
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
		}
		return (cdmBase.deproxy(cdmBase, Synonym.class));
	}


}
