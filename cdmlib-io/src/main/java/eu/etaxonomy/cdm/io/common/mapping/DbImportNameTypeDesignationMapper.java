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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 12.05.2009
 */
public class DbImportNameTypeDesignationMapper<STATE extends DbImportStateBase<?,?>, T extends IDbImportTransformed> extends DbImportMultiAttributeMapperBase<CdmBase, STATE> {

    private static final Logger logger = LogManager.getLogger();

//******************************** FACTORY METHOD ***************************************************/

	public static DbImportNameTypeDesignationMapper<?,?> NewInstance(String dbFromAttribute, String dbToAttribute, String relatedObjectNamespace, String desigStatusAttribute){
		return new DbImportNameTypeDesignationMapper(dbFromAttribute, dbToAttribute, null, relatedObjectNamespace, desigStatusAttribute);
	}

//******************************* ATTRIBUTES ***************************************/
	private String fromAttribute;
	private String toAttribute;
	private NameTypeDesignationStatus designationStatus;
	private String relatedObjectNamespace;
	private String citationAttribute;
	private String microCitationAttribute;
	private String designationStatusAttribute;


//********************************* CONSTRUCTOR ****************************************/

	protected DbImportNameTypeDesignationMapper(String fromAttribute, String toAttribute, NameTypeDesignationStatus designationStatus, String relatedObjectNamespace, String desigStatusAttribute) {
		super();
		//TODO make it a single attribute mapper
		this.fromAttribute = fromAttribute;
		this.toAttribute = toAttribute;
		this.relatedObjectNamespace = relatedObjectNamespace;
		this.designationStatusAttribute = desigStatusAttribute;
		this.designationStatus = designationStatus;
	}

//************************************ METHODS *******************************************/

	@Override
    public CdmBase invoke(ResultSet rs, CdmBase cdmBase) throws SQLException {
		STATE state = importMapperHelper.getState();
		CdmImportBase currentImport = state.getCurrentIO();
		if (currentImport instanceof ICheckIgnoreMapper){
			boolean ignoreRecord = ((ICheckIgnoreMapper)currentImport).checkIgnoreMapper(this, rs);
			if (ignoreRecord){
				return cdmBase;
			}
		}

		CdmBase fromObject = getRelatedObject(rs, fromAttribute);
		CdmBase toObject = getRelatedObject(rs, toAttribute);
		//TODO cast
		Reference citation = (Reference)getRelatedObject(rs, citationAttribute);
		String microCitation = null;
		if (citationAttribute != null){
			microCitation = rs.getString(microCitationAttribute);
		}

		Object designationStatusValue = null;
		if (citationAttribute != null){
			designationStatusValue = rs.getObject(designationStatusAttribute);
		}

		if (fromObject == null){
			String warning  = "Higher rank name could not be found. Name type not added to higher rank name";
			logger.warn(warning);
			return cdmBase;
		}
		TaxonName typifiedName = checkTaxonNameType(fromObject);

		if (toObject == null){
			String warning  = "Species name could not be found. Name type not added to higher rank name";
			logger.warn(warning);
			return cdmBase;
		}
		TaxonName typeName = checkTaxonNameType(toObject);

		boolean addToAllHomotypicNames = false; //TODO check if this is correct
		String originalInfo = null; //TODO what is this

		NameTypeDesignationStatus status = this.designationStatus;
		if (designationStatusValue != null){
			//FIXME this needs work in generics to remove casts. Or find an other solution
			if (currentImport instanceof IDbImportTransformed){
				IDbImportTransformer transformer = ((IDbImportTransformed)currentImport).getTransformer();
				status = transformer.transformNameTypeDesignationStatus(designationStatusValue);
			}
		}
		typifiedName.addNameTypeDesignation(typeName, citation, microCitation, originalInfo, status, addToAllHomotypicNames);

		return typifiedName;
	}

	/**
	 *	//TODO copied from DbImportObjectMapper. Maybe these can be merged again in future
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
	 */
	private TaxonName checkTaxonNameType(CdmBase cdmBase) {
		if (! cdmBase.isInstanceOf(TaxonName.class)){
			String warning = "Type name or typifier name is not of type TaxonName but " + cdmBase.getClass().getName();
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
		}
		return (cdmBase.deproxy(cdmBase, TaxonName.class));
	}
}