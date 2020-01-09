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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * A mapper that can be used to create synonym relationships for real synonyms with a {@link SynonymType},
 * synonyms relationships that are handled as concept relationships if the synonym needs to be handled
 * as accepted taxon (misapplied names, pro parte synonyms, synonyms with attached factual data, ...)
 * and even synonym relationships which in reality are only name relationships.
 * The mapping is defined in the transformer.
 *
 * @author a.mueller
 * @since 02.03.2010
 *
 * @see DbImportTaxIncludedInMapper
 */
public class DbImportSynonymMapper<STATE extends DbImportStateBase<?,?>>
        extends DbImportMultiAttributeMapperBase<CdmBase, STATE> {

    private static final Logger logger = Logger.getLogger(DbImportSynonymMapper.class);

//******************************* ATTRIBUTES ***************************************/

    private String fromAttribute;
    private String toAttribute;
    private TaxonRelationshipType taxonRelType;
    private SynonymType synType;
    private String relatedObjectNamespace;
    private String citationAttribute;
    private String microCitationAttribute;
    private String relationshipTypeAttribute;
    boolean forceTaxonLevelRelation = true;
    boolean saveSourceValueAsAnnotation;

//******************************** FACTORY METHOD ***************************************************/

	/**
	 * Creates a new instance of SynonymMapper.
	 * @param dbFromAttribute
	 * @param dbToAttribute
	 * @param relatedObjectNamespace
	 * @param relTypeAttribute the attribute in the source DB to compute the relationship type(s) from
	 * @param defaultTaxonRelationshipType this relationship type is taken for accepted taxa
	 *        being synonyms (may be the case if data are dirty)
	 * @return
	 */
	public static DbImportSynonymMapper<?> NewInstance(String dbFromAttribute, String dbToAttribute,
	        String relatedObjectTaxonNamespace, String relTypeAttribute, SynonymType defaultSynonymType,
	        TaxonRelationshipType defaultTaxonRelationshipType, boolean saveSourceValueAsAnnotation){
		return new DbImportSynonymMapper<>(dbFromAttribute, dbToAttribute, defaultSynonymType, defaultTaxonRelationshipType, relatedObjectTaxonNamespace, relTypeAttribute, saveSourceValueAsAnnotation);
	}

//********************************* CONSTRUCTOR ****************************************/

	protected DbImportSynonymMapper(String fromAttribute, String toAttribute, SynonymType synType,
	        TaxonRelationshipType relType, String relatedObjectNamespace, String relTypeAttribute
	        , boolean saveSourceValueAsAnnotation) {
		//TODO make it a single attribute mapper (?)
		this.fromAttribute = fromAttribute;
		this.toAttribute = toAttribute;
		this.taxonRelType = relType;
		this.synType = synType;
        this.relatedObjectNamespace = relatedObjectNamespace;
		this.relationshipTypeAttribute = relTypeAttribute;
		this.saveSourceValueAsAnnotation = saveSourceValueAsAnnotation;
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
        SynonymType synType = this.synType;
        NameRelationshipType nameType = null;
        HybridRelationshipType hybridType = null;
        TaxonRelationshipType taxonRelType = this.taxonRelType;

        String relTypeAttrValue = null;
		if (relationshipTypeAttribute != null){
		    relTypeAttrValue = rs.getString(relationshipTypeAttribute);
		    RelationshipTermBase<?>[] relTypes = this.getState().getTransformer().getSynonymRelationTypesByKey(relTypeAttrValue, state);
		    if (relTypes[0]!= null){
		        synType = (SynonymType)relTypes[0];
		    }
		    if (relTypes[1]!=null){
		        taxonRelType = (TaxonRelationshipType)relTypes[1];
		    }
            nameType = (NameRelationshipType)relTypes[2];
            hybridType = (HybridRelationshipType)relTypes[3];
		}

		if (fromObject == null){
			String warning  = "The synonym (" + fromId + ") could not be found. Synonym not added to accepted taxon";
			logger.warn(warning);
			return cdmBase;
		}
		checkSynonymType(fromObject, fromId, taxonRelType);

		if (toObject == null){
			String warning  = "The accepted taxon (" + toId + ") could not be found. Synonym not added to accepted taxon";
			logger.warn(warning);
			return cdmBase;
		}
		Taxon taxon = checkTaxonType(toObject, "Accepted taxon", toId);

		if(forceTaxonLevelRelation && synType == null){
		    synType = SynonymType.SYNONYM_OF();
		}

		AnnotatableEntity[] result = new AnnotatableEntity[4];
		if (fromObject.isInstanceOf(Synonym.class) && synType != null){
			Synonym synonym = CdmBase.deproxy(fromObject, Synonym.class);
			taxon.addSynonym(synonym, synType); //citation and micro citation not in use anymore as we do not have synonym relationships anymore
			result[0] = synonym;
		}else if (fromObject.isInstanceOf(Taxon.class) && taxonRelType != null){
			TaxonRelationshipType type = taxonRelType;
			Taxon synonymTaxon = CdmBase.deproxy(fromObject, Taxon.class);
			result[1] = synonymTaxon.addTaxonRelation(taxon, type, citation, microCitation);
		}else{
			if (forceTaxonLevelRelation){
			    String message = "Taxon is not a synonym and accepted taxa are not allowed as synonyms: " +  fromObject.getTitleCache() + "; " + fromObject.getId();
			    logger.warn(message);
			}
		}
		if(nameType != null){
		    result[2] = fromObject.getName().addRelationshipToName(toObject.getName(), nameType, citation, microCitation, null, null);
		}
        if(hybridType != null){
            result[3] = fromObject.getName().addHybridParent(toObject.getName(), hybridType, citation, microCitation, null, null);
        }
        if (saveSourceValueAsAnnotation && StringUtils.isNotBlank(relTypeAttrValue)){
            for (AnnotatableEntity a : result){
                if (a != null){
                    a.addAnnotation(Annotation.NewInstance(relTypeAttrValue, Language.UNKNOWN_LANGUAGE()));
                }
            }
        }

		return fromObject;
	}

	/**
	 *	//TODO copied from DbImportObjectMapper. Maybe these can be merged again in future
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
	 * @param taxonRelType
	 */
	private TaxonBase<?> checkSynonymType(CdmBase cdmBase, String id, TaxonRelationshipType taxonRelType) {
		if (! cdmBase.isInstanceOf(Synonym.class)){
			String warning = "Synonym (" + id + ") is not of type Synonym but of type " + cdmBase.getClass().getSimpleName();
			if (taxonRelType == null){
				logger.warn(warning);
				throw new IllegalArgumentException(warning);
			}else{
				logger.info(warning);
			}
		}
		return (CdmBase.deproxy(cdmBase, TaxonBase.class));
	}
}
