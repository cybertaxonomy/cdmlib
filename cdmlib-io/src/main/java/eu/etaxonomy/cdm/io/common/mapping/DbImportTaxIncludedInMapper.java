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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.erms.ICheckIgnoreMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
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
public class DbImportTaxIncludedInMapper<STATE extends DbImportStateBase<ImportConfiguratorBase,?>> extends DbImportMultiAttributeMapperBase<CdmBase, STATE> {
	private static final Logger logger = Logger.getLogger(DbImportTaxIncludedInMapper.class);
	
//******************************** FACTORY METHOD ***************************************************/
	
	public static DbImportTaxIncludedInMapper<?> NewInstance(String dbChildAttribute, String dbChildNamespace, String dbParentAttribute, String parentNamespace, String dbAlternativeParentAttribute, String alternativeParentNamespace, String dbTreeAttribute){
		String citationNamespace = null;
		String citationAttribute = null;
		return new DbImportTaxIncludedInMapper(dbChildAttribute, dbChildNamespace, dbParentAttribute, parentNamespace, dbAlternativeParentAttribute, alternativeParentNamespace, dbTreeAttribute, citationAttribute, citationNamespace);
	}
	
//******************************* ATTRIBUTES ***************************************/
	private String fromAttribute;
	private String toAttribute;

	private String fromNamespace;
	private String toNamespace;
	
	private String citationAttribute;
	private String citationNamespace;
	
	private String microCitationAttribute;
	private String treeAttribute;
	private String alternativeAttribute;
	private String alternativeNamespace;
	
	
//********************************* CONSTRUCTOR ****************************************/
	/**
	 * @param relatedObjectNamespace 
	 * @param mappingImport
	 */
	protected DbImportTaxIncludedInMapper(String fromAttribute, String fromNamespace, String toAttribute, String toNamespace, String alternativeAttribute, String alternativeNamespace, String treeAttribute, String citationAttribute, String citationNamespace) {
		super();
		//TODO make it a single attribute mapper
		this.fromAttribute = fromAttribute;
		this.fromNamespace = fromNamespace;
		this.toAttribute = toAttribute;
		this.toNamespace = toNamespace;
		this.treeAttribute = treeAttribute;
		this.alternativeAttribute = alternativeAttribute;
		this.alternativeNamespace = alternativeNamespace;
		this.citationAttribute = citationAttribute;
		this.citationNamespace = citationNamespace;
	}

//************************************ METHODS *******************************************/

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CdmBase invoke(ResultSet rs, CdmBase cdmBase) throws SQLException {
		STATE state = getState();
		CdmImportBase currentImport = state.getCurrentIO();
		if (currentImport instanceof ICheckIgnoreMapper){
			boolean ignoreRecord = ((ICheckIgnoreMapper)currentImport).checkIgnoreMapper(this, rs);
			if (ignoreRecord){
				return cdmBase;
			}
		}
		
		TaxonBase fromObject = (TaxonBase)getRelatedObject(rs, fromAttribute, fromNamespace);
		TaxonBase toObject = (TaxonBase)getRelatedObject(rs, toAttribute, toNamespace);
		TaxonBase alternativeToObject = (TaxonBase)getRelatedObject(rs, alternativeAttribute, alternativeNamespace);
		
		String fromId = String.valueOf(rs.getObject(fromAttribute));
		String toId = String.valueOf(rs.getObject(toAttribute));
		String alternativeToId = String.valueOf(rs.getObject(alternativeAttribute));

		ReferenceBase citation = (ReferenceBase)getRelatedObject(rs, citationAttribute, citationNamespace);
		String microCitation = null;
		if (citationAttribute != null){
			microCitation = rs.getString(microCitationAttribute);
		}
		//TODO check int
		Integer treeFk = null;
		if (treeAttribute != null){
			treeFk = rs.getInt(treeAttribute);
		}
		
		if (fromObject == null){
			String warning  = "The child taxon could not be found. Child taxon not added to the tree";
			logger.warn(warning);
			return cdmBase;
		}
		Taxon fromTaxon;
		try {
			fromTaxon = checkTaxonType(fromObject, "Child", fromId);
		} catch (IllegalArgumentException e2) {
			//fromTaxon is null
			return cdmBase;
		}
		
		if (toObject == null){
			String warning  = "The parent taxon could not be found. Child taxon not added to the tree";
			logger.warn(warning);
			return cdmBase;
		}
		
		Taxon toTaxon;
		try {
			toTaxon = checkTaxonType(toObject, "Parent", toId);
		} catch (IllegalArgumentException e) {
			if (alternativeToObject != null){
				try {
					toTaxon = checkTaxonType(alternativeToObject, "Alternative parent", alternativeToId);
				} catch (IllegalArgumentException e1) {
					return cdmBase;
				}
			}else{
				
				return cdmBase;
			}
		}
		
		if (fromTaxon.equals(toTaxon)){
			String warning  = "A taxon may not be a child of itself. Taxon not added to the tree";
			logger.warn(warning);
			return cdmBase;
		}
		//maps the reference
		makeTaxonomicallyIncluded(state, treeFk, fromTaxon, toTaxon, citation, microCitation);
		return fromTaxon;
	}

	
	


	/**
	 * TODO copied from BM import. May be more generic
	 * @param state
	 * @param taxonTreeMap
	 * @param treeRefFk
	 * @param child
	 * @param parent
	 * @param citation
	 * @param microCitation
	 * @return
	 */
	
	public static final String TAXONOMIC_TREE_NAMESPACE = "TaxonomicTree";
	
	private boolean makeTaxonomicallyIncluded(STATE state, Integer treeRefFk, Taxon child, Taxon parent, ReferenceBase citation, String microCitation){
		String treeKey;
		UUID treeUuid;
		if (treeRefFk == null){
			treeKey = "1";  // there is only one tree and it gets the key '1'
			treeUuid = state.getConfig().getTaxonomicTreeUuid();
		}else{
			treeKey =String.valueOf(treeRefFk);
			treeUuid = state.getTreeUuidByTreeKey(treeKey);
		}
		TaxonomicTree tree = (TaxonomicTree)state.getRelatedObject(TAXONOMIC_TREE_NAMESPACE, treeKey);
		if (tree == null){
			ITaxonTreeService service = state.getCurrentIO().getTaxonTreeService();
			tree = service.getTaxonomicTreeByUuid(treeUuid);
			if (tree == null){
				String treeName = state.getConfig().getTaxonomicTreeName();
				tree = TaxonomicTree.NewInstance(treeName);
				tree.setUuid(treeUuid);
				//FIXME tree reference
				//tree.setReference(ref);
				service.save(tree);
			}
			state.addRelatedObject(TAXONOMIC_TREE_NAMESPACE, treeKey, tree);
		}
		
		TaxonNode childNode = tree.addParentChild(parent, child, citation, microCitation);
		return (childNode != null);
	}
	
//	
//	private boolean makeTaxonomicallyIncluded_OLD(STATE state, Integer treeRefFk, Taxon child, Taxon parent, ReferenceBase citation, String microCitation){
//		Map<Integer, TaxonomicTree> taxonTreeMap = state.getPartitionTaxonTreeMap();
//		
//		
//		TaxonomicTree tree = taxonTreeMap.get(treeRefFk);
//		if (tree == null){
//			UUID treeUuid = state.getTreeUuidByTreeKey(treeRefFk);
//			tree = state.getCurrentImport().getTaxonTreeService().getTaxonomicTreeByUuid(treeUuid);
//			if (tree == null){
//				//FIXME FIXME FIXME
//				String treeName = "TaxonTree - No Name";
//				tree = TaxonomicTree.NewInstance(treeName);
//				//tree.setReference(ref);
//				//throw new IllegalStateException("Tree for ToTaxon reference does not exist.");
//			}
//			taxonTreeMap.put(treeRefFk, tree);
//		}
//		return tree.addParentChild(parent, child, citation, microCitation);
//	}
	
	
	/**
	 *	//TODO copied from DbImportObjectMapper. Maybe these can be merged again in future
	 * @param rs
	 * @param dbAttribute
	 * @return
	 * @throws SQLException
	 */
	protected CdmBase getRelatedObject(ResultSet rs, String dbAttribute, String namespace) throws SQLException {
		CdmBase result = null;
		if (dbAttribute != null){
			Object dbValue = rs.getObject(dbAttribute);
			String id = String.valueOf(dbValue);
			DbImportStateBase state = importMapperHelper.getState();
			result = state.getRelatedObject(namespace, id);
		}
		return result;
	}
	

	/**
	 * Checks if cdmBase is of type Taxon 
	 * @param taxonBase
	 * @param typeString
	 * @param id
	 * @return
	 */
	private Taxon checkTaxonType(TaxonBase taxonBase, String typeString, String id) throws IllegalArgumentException{
		if (! taxonBase.isInstanceOf(Taxon.class)){
			String warning = typeString + " (" + id + ") is not of type Taxon but of type " + taxonBase.getClass().getSimpleName();
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
		}
		return (taxonBase.deproxy(taxonBase, Taxon.class));
	}


}
