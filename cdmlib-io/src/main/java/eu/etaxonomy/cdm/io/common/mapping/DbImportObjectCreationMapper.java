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
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportObjectCreationMapper<CDM_BASE extends CdmBase, STATE extends DbImportStateBase> extends MultipleAttributeMapperBase implements IDbImportMapper<STATE, CDM_BASE> {
	private static final Logger logger = Logger.getLogger(DbImportObjectCreationMapper.class);
	
//******************************** FACTORY METHOD ***************************************************/
	
	public static DbImportObjectCreationMapper<?,?> NewInstance(IMappingImport mappingImport, String dbIdAttribute, String namespace){
		return new DbImportObjectCreationMapper(mappingImport, dbIdAttribute, namespace);
	}
	
//******************************* ATTRIBUTES ***************************************/
	protected DbImportMapperBase<STATE> importMapperHelper = new DbImportMapperBase<STATE>();
	private IMappingImport<CDM_BASE, STATE> mappingImport;
	private String dbIdAttribute;
	//TODO get standard namespace from mappingImport
	private String namespace;
	
	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param mappingImport
	 */
	protected DbImportObjectCreationMapper(IMappingImport<CDM_BASE, STATE> mappingImport, String dbIdAttribute, String namespace) {
		super();
		this.mappingImport = mappingImport;
		//TODO make it a single attribute mapper
		this.dbIdAttribute = dbIdAttribute;
		this.namespace = namespace;
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#initialize(eu.etaxonomy.cdm.io.common.DbImportStateBase, java.lang.Class)
	 */
	public void initialize(STATE state, Class<? extends CdmBase> destinationClass) {
		importMapperHelper.initialize(state, destinationClass);
		logger.warn("DbImportObjectCreationMapper still needs 'citation' implemented for OriginalSource"); //see addOriginalSource()
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CDM_BASE invoke(ResultSet rs, CDM_BASE cdmBase) throws SQLException {
		cdmBase = mappingImport.createObject(rs, importMapperHelper.getState());
		addOriginalSource(rs, cdmBase);
		return cdmBase;
	}

	/**
	 * TODO also implemented in CdmImportBase (reduce redundance)
	 * @throws SQLException 
	 */
	public void addOriginalSource(ResultSet rs, CDM_BASE cdmBase) throws SQLException {
		if (cdmBase instanceof ISourceable ){
			IOriginalSource source;
			ISourceable sourceable = (ISourceable)cdmBase;
			Object id = rs.getObject(dbIdAttribute);
			String strId = String.valueOf(id);
			String idNamespace = namespace;
			//FIXME
			ReferenceBase citation = null;
			//importMapperHelper.getState().getConfig()xxx;
			String microCitation = null;
			if (cdmBase instanceof IdentifiableEntity){
				source = IdentifiableSource.NewInstance(strId, idNamespace, citation, microCitation);
			}else if (cdmBase instanceof DescriptionElementBase){
				source = DescriptionElementSource.NewInstance(strId, idNamespace, citation, microCitation);
			}else{
				logger.warn("ISourceable not beeing identifiable entities or description element base are not yet supported. CdmBase is of type " + cdmBase.getClass().getName() + ". Original source not added.");
				return;
			}
			sourceable.addSource(source);
		}
	}
	
}
