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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * This mapper creates an description element source that is added to the according description element.
 * It adds the reference and the microReference.
 * @author a.mueller
 * @created 11.03.2010
 * @version 1.0
 */
public class DbImportDescriptionElementSourceCreationMapper extends DbImportObjectCreationMapperBase<DescriptionElementSource, DbImportStateBase<?,?>> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportDescriptionElementSourceCreationMapper.class);

//**************************** FACTORY METHOD ***********************************************/

	/**
	 * Returns an description element source creation mapper.
	 * @param dbDescriptionElementFkAttribute
	 * @param descriptionElementNamespace
	 * @param dbReferenceFkAttribute
	 * @param referenceNamespace
	 * @return
	 */
	public static DbImportDescriptionElementSourceCreationMapper NewInstance(String dbDescriptionElementFkAttribute, String descriptionElementNamespace, String dbReferenceFkAttribute, String referenceNamespace){
		String dbMicroReferenceAttribute = null;
		return new DbImportDescriptionElementSourceCreationMapper(dbDescriptionElementFkAttribute, descriptionElementNamespace, dbReferenceFkAttribute, referenceNamespace, dbMicroReferenceAttribute);
	}


	/**
	 * Returns an description element source creation mapper.
	 * @param dbDescriptionElementFkAttribute
	 * @param descriptionElementNamespace
	 * @param dbReferenceFkAttribute
	 * @param referenceNamespace
	 * @param dbMicroReferenceAttribute
	 * @return
	 */
	public static DbImportDescriptionElementSourceCreationMapper NewInstance(String dbDescriptionElementFkAttribute, String descriptionElementNamespace, String dbReferenceFkAttribute, String referenceNamespace, String dbMicroReferenceAttribute){
		return new DbImportDescriptionElementSourceCreationMapper(dbDescriptionElementFkAttribute, descriptionElementNamespace, dbReferenceFkAttribute, referenceNamespace, dbMicroReferenceAttribute);
	}


//******************************* VARIABLES ***********************************************/

	protected String descriptionElementNamespace;
	protected String dbDescriptionElementFkAttribute;
	protected String referenceNamespace;
	protected String dbReferenceFkAttribute;
	protected String dbMicroReferenceAttribute;


//******************************* CONSTRUCTOR ***********************************************/

	/**
	 * @param dbIdAttribute
	 * @param objectToCreateNamespace
	 */
	protected DbImportDescriptionElementSourceCreationMapper(String dbDescriptionElementFkAttribute, String descriptionElementNamespace, String dbReferenceFkAttribute, String referenceNamespace, String dbMicroReferenceAttribute) {
		super(null, null);  // idAttribute and objectToCreateNamespace not needed
		this.descriptionElementNamespace = descriptionElementNamespace;
		this.dbDescriptionElementFkAttribute = dbDescriptionElementFkAttribute;
		this.dbReferenceFkAttribute = dbReferenceFkAttribute;
		this.referenceNamespace = referenceNamespace;
		this.dbMicroReferenceAttribute = dbMicroReferenceAttribute;
	}

//******************************* METHODS ***********************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected DescriptionElementSource createObject(ResultSet rs)throws SQLException {
		DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
		return source;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#doInvoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.VersionableEntity)
	 */
	@Override
	protected DescriptionElementSource doInvoke(ResultSet rs, DescriptionElementSource source) throws SQLException {
		addCitation(rs, source);
		addMicroCitation(rs, source);
		setDescriptionElement(rs, source);
		return source;
	}

	/**
	 * @param rs
	 * @param source
	 * @throws SQLException
	 */
	private void setDescriptionElement(ResultSet rs,
			DescriptionElementSource source) throws SQLException {
		DescriptionElementBase descriptionElement = (DescriptionElementBase)getRelatedObject(rs, descriptionElementNamespace, dbDescriptionElementFkAttribute);
		descriptionElement.addSource(source);
	}

	/**
	 * @param rs
	 * @param source
	 * @throws SQLException
	 */
	private void addMicroCitation(ResultSet rs, DescriptionElementSource source) throws SQLException {
		String microReference = null;
		if (StringUtils.isNotBlank(dbMicroReferenceAttribute)){
			microReference = rs.getString(dbMicroReferenceAttribute);
		}
		source.setCitationMicroReference(microReference);
	}

	/**
	 * @param rs
	 * @param source
	 * @throws SQLException
	 */
	private void addCitation(ResultSet rs, DescriptionElementSource source) throws SQLException {
		Reference citation = (Reference)getRelatedObject(rs, referenceNamespace, dbReferenceFkAttribute);
		source.setCitation(citation);
	}

}
