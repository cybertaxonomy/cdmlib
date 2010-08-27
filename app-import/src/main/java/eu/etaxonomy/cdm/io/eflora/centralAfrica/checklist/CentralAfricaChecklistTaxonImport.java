/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMarkerMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportTaxIncludedInMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist.validation.CentralAfricaChecklistTaxonImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class CentralAfricaChecklistTaxonImport  extends CentralAfricaChecklistImportBase<TaxonBase> implements IMappingImport<TaxonBase, CentralAfricaChecklistImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaChecklistTaxonImport.class);
	
	private NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
	
	private Map<UUID, Taxon> higherTaxonMap;
	
	private Integer TREE_ID = null;
	
	private DbImportMapping mapping;
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private static final String dbTableName = "checklist";
	private static final Class cdmTargetClass = TaxonBase.class;
	private static final String strOrderBy = " ORDER BY family, genus, species ";

	public CentralAfricaChecklistTaxonImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT pk FROM " + dbTableName +
						strOrderBy;
		return strQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
 			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "pk", TAXON_NAMESPACE)); //id + tu_status
		
			UUID uuidKew = CentralAfricaChecklistTransformer.uuidAcceptedKew;
			mapping.addMapper(DbImportMarkerMapper.NewInstance("accepted kew", uuidKew, "Accepted Kew", "Accepted Kew", "Kew"));
			
			UUID uuidGeneva = CentralAfricaChecklistTransformer.uuidAcceptedGeneva;
			mapping.addMapper(DbImportMarkerMapper.NewInstance("accepted geneva", uuidGeneva, "Accepted Geneva", "Accepted Geneva", "Geneva"));

			UUID uuidItis = CentralAfricaChecklistTransformer.uuidAcceptedItis;
			mapping.addMapper(DbImportMarkerMapper.NewInstance("accepted itis", uuidItis, "Accepted ITIS", "Accepted ITIS", "ITIS"));
		}
		
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(CentralAfricaChecklistImportConfigurator config) {
		String strSelect = " SELECT * ";
		String strFrom = " FROM checklist";
		String strWhere = " WHERE ( pk IN (" + ID_LIST_TOKEN + ") )";
		String strRecordQuery = strSelect + strFrom + strWhere + strOrderBy;
		return strRecordQuery;
	}

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, CentralAfricaChecklistImportState state) {
		higherTaxonMap = new HashMap<UUID, Taxon>();
		boolean success = super.doPartition(partitioner, state);
		higherTaxonMap = new HashMap<UUID, Taxon>();
		return success;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Set<String> referenceIdSet = new HashSet<String>();
		
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
				while (rs.next()){
					handleForeignKey(rs, referenceIdSet, "source");
				}

			//reference map
			nameSpace = REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> referenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, referenceIdSet, nameSpace);
			result.put(REFERENCE_NAMESPACE, referenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public TaxonBase createObject(ResultSet rs, CentralAfricaChecklistImportState state) throws SQLException {
		BotanicalName speciesName = BotanicalName.NewInstance(Rank.SPECIES());
		
		ReferenceBase sec = state.getConfig().getSourceReference();
		getReferenceService().saveOrUpdate(sec);
		
		String familyString = rs.getString("family");
		String genusString = rs.getString("genus");
		String speciesString = rs.getString("species");
		String authorityString = rs.getString("authority");
		if (logger.isDebugEnabled()){
			System.out.println(familyString + " " + genusString + " " + speciesString);
		}
		
		Taxon speciesTaxon = Taxon.NewInstance(speciesName, sec);;
		speciesName.setGenusOrUninomial(genusString);
		speciesName.setSpecificEpithet(speciesString);
		parser.handleAuthors(speciesName, CdmUtils.concat(" ", new String[] {"", genusString, speciesString, authorityString}), authorityString);
		
		//family
		Taxon familyTaxon = null;
		if (StringUtils.isNotBlank(familyString)){
			familyTaxon = getHigherTaxon(state, familyString, null);
			if (familyTaxon == null){
				BotanicalName familyName = BotanicalName.NewInstance(Rank.FAMILY());
				familyName.setGenusOrUninomial(familyString);
				familyTaxon = Taxon.NewInstance(familyName, sec);
				saveHigherTaxon(state, familyTaxon, familyString, null);
			}
			getTaxonService().saveOrUpdate(familyTaxon);	
		}
		
		
		//genus
		Taxon genusTaxon = getHigherTaxon(state, familyString, genusString);
		if (genusTaxon == null){
			BotanicalName genusName = BotanicalName.NewInstance(Rank.GENUS());
			genusName.setGenusOrUninomial(genusString);
			genusTaxon = Taxon.NewInstance(genusName, sec);
			saveHigherTaxon(state, genusTaxon, familyString, genusString);
			if (familyTaxon != null){
				makeTaxonomicallyIncluded(state, TREE_ID, genusTaxon, familyTaxon, null, null);
			}
		}
		makeTaxonomicallyIncluded(state, TREE_ID, speciesTaxon, genusTaxon, null, null);
		getTaxonService().saveOrUpdate(genusTaxon);

		String sourceString = rs.getString("source");
		String sourceId = rs.getString("source_id");
		
		ReferenceBase sourceRef = state.getRelatedObject(REFERENCE_NAMESPACE, sourceString, ReferenceBase.class);
		speciesTaxon.addSource(sourceId, REFERENCE_NAMESPACE, sourceRef, null);
		
		//distribution
		handleDistribution(rs, speciesTaxon);
		
		return speciesTaxon;
	}
	
	private void handleDistribution(ResultSet rs, Taxon speciesTaxon) throws SQLException {
		TaxonDescription description = TaxonDescription.NewInstance(speciesTaxon);
		
		Boolean isCongo = rs.getBoolean("drc");
		Boolean isBurundi = rs.getBoolean("burundi");
		Boolean isRwanda = rs.getBoolean("rwanda");

		addDistribution(description, isCongo, "ZAI");
		addDistribution(description, isBurundi, "BUR");
		addDistribution(description, isRwanda, "RWA");

	}



	/**
	 * @param description
	 * @param isCongo
	 */
	private void addDistribution(TaxonDescription description, Boolean exists, String label) {
		if (exists == true){
			NamedArea namedArea = TdwgArea.getAreaByTdwgAbbreviation(label);
			Distribution distribution = Distribution.NewInstance(namedArea, PresenceTerm.PRESENT());
			description.addElement(distribution);
		}
	}



	private void saveHigherTaxon(CentralAfricaChecklistImportState state, Taxon higherTaxon, String family, String genus) {
		String higherName = normalizeHigherTaxonName(family, genus);
		UUID uuid = higherTaxon.getUuid();
		state.putHigherTaxon(higherName, uuid);
		higherTaxonMap.put(uuid, higherTaxon);
	}



	private Taxon getHigherTaxon(CentralAfricaChecklistImportState state, String family, String genus) {
		String higherName = normalizeHigherTaxonName(family, genus);
		UUID uuid = state.getHigherTaxon(higherName);
		
		Taxon taxon = null;
		if (uuid != null){
			taxon = higherTaxonMap.get(uuid);
			if (taxon == null){
				taxon = CdmBase.deproxy(getTaxonService().find(uuid), Taxon.class);
			}
		}
		return taxon;
	}



	/**
	 * @param family
	 * @param genus
	 */
	private String normalizeHigherTaxonName(String family, String genus) {
		return (CdmUtils.Nz(family) + "-" + CdmUtils.Nz(genus)).trim();
	}




//	private boolean makeTaxonomicallyIncluded(CentralAfricaChecklistImportState state, Taxon parent, Taxon child, ReferenceBase citation, String microCitation){
//		ReferenceBase sec = child.getSec();
//		UUID uuid = state.getTreeUuid(sec);
//		TaxonomicTree tree;
//		tree = state.getTree(sec);
//		
//		if (tree == null){
//			tree = makeTreeMemSave(state, sec);
//		}
//		TaxonNode childNode;
//		if (parent != null){
//			childNode = tree.addParentChild(parent, child, citation, microCitation);
//		}else{
//			childNode = tree.addChildTaxon(child, citation, microCitation, null);
//		}
//		return (childNode != null);
//	}
	
	//TODO use Mapper
	private boolean makeTaxonomicallyIncluded(CentralAfricaChecklistImportState state, Integer treeRefFk, Taxon child, Taxon parent, ReferenceBase citation, String microCitation){
		String treeKey;
		UUID treeUuid;
		if (treeRefFk == null){
			treeKey = "1";  // there is only one tree and it gets the map key '1'
			treeUuid = state.getConfig().getTaxonomicTreeUuid();
		}else{
			treeKey =String.valueOf(treeRefFk);
			treeUuid = state.getTreeUuidByTreeKey(treeKey);
		}
		TaxonomicTree tree = (TaxonomicTree)state.getRelatedObject(DbImportTaxIncludedInMapper.TAXONOMIC_TREE_NAMESPACE, treeKey);
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
			state.addRelatedObject(DbImportTaxIncludedInMapper.TAXONOMIC_TREE_NAMESPACE, treeKey, tree);
		}
		
		TaxonNode childNode = tree.addParentChild(parent, child, citation, microCitation);
		return (childNode != null);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(CentralAfricaChecklistImportState state){
		IOValidator<CentralAfricaChecklistImportState> validator = new CentralAfricaChecklistTaxonImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(CentralAfricaChecklistImportState state){
		return ! state.getConfig().isDoTaxa();
	}



}
