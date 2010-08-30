/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonTreeService;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMethodMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportTaxIncludedInMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist.CentralAfricaChecklistImportState;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.validation.CentralAfricaFernsTaxonImportValidator;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class CentralAfricaFernsTaxonRelationImport  extends CentralAfricaFernsImportBase<TaxonBase> implements IMappingImport<TaxonBase, CentralAfricaFernsImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsTaxonRelationImport.class);
	
	private DbImportMapping mapping;
	
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private static final String dbTableName = "[African pteridophytes]";
	private static final Class cdmTargetClass = TaxonBase.class;

	private Map<String, UUID> taxonMap = new HashMap<String, UUID>();

	
	public CentralAfricaFernsTaxonRelationImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT [Taxon number] FROM " + dbTableName;;
		return strQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "createObject", ResultSet.class, CentralAfricaFernsImportState.class));
//					NewInstance(this, "Taxon number", TAXON_NAMESPACE)); //id + tu_status

//funktioniert nicht wegen doppeltem Abfragen von Attributen
//			mapping.addMapper(DbImportSynonymMapper.NewInstance("Taxon number", "Current", TAXON_NAMESPACE, null)); 			
//			mapping.addMapper(DbImportNameTypeDesignationMapper.NewInstance("id", "tu_typetaxon", NAME_NAMESPACE, "tu_typedesignationstatus"));

		}
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(CentralAfricaFernsImportConfigurator config) {
		String strSelect = " SELECT * ";
		String strFrom = " FROM [African pteridophytes] as ap";
		String strWhere = " WHERE ( ap.[taxon number] IN (" + ID_LIST_TOKEN + ") )";
		String strRecordQuery = strSelect + strFrom + strWhere;
		return strRecordQuery;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#doInvoke(eu.etaxonomy.cdm.io.erms.ErmsImportState)
	 */
	@Override
	protected boolean doInvoke(CentralAfricaFernsImportState state) {
		//first path
		fillTaxonMap();
		boolean success = super.doInvoke(state);
		
		return success;

	}



	private void fillTaxonMap() {
		List<String> propPath = Arrays.asList(new String []{"name"});
		
		List<Taxon> taxonList = (List)getTaxonService().list(Taxon.class, null, null, null, propPath );
		for (Taxon taxon : taxonList){
			NonViralName nvn = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			UUID uuid = taxon.getName().getUuid();
			String name = nvn.getNameCache();
			taxonMap.put(name, uuid);
			
		}
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
//				Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "Current");
				handleForeignKey(rs, taxonIdSet, "Taxon number");

//				handleForeignKey(rs, referenceIdSet, "PTRefFk");
			}

			//reference map
			nameSpace = TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, taxonIdSet, nameSpace);
			result.put(nameSpace, taxonMap);
				
				
			//reference map
//			nameSpace = "Reference";
//			cdmClass = ReferenceBase.class;
//			Map<String, Person> referenceMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(Person.class, teamIdSet, nameSpace);
//			result.put(ReferenceBase.class, referenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public TaxonBase createObject(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		TaxonBase result = null;
		try {
			String status = rs.getString("Current/Synonym");
			
			if ("s".equalsIgnoreCase(status)){
				//synonym
				result = handleSynonym(rs, state);
			}else{
				//accepted Taxon
				result = handleTaxon(rs, state);
			}
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		}

	}


	private Synonym handleSynonym(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		String accTaxonId = rs.getString("Current");
		String synonymId = rs.getString("Taxon number");
		Synonym synonym = (Synonym)state.getRelatedObject(TAXON_NAMESPACE, synonymId);
		if (synonym == null){
			logger.warn ("Synonym ("+synonymId+")not found.");
			return null;
		}
		TaxonBase taxonBase = CdmBase.deproxy(state.getRelatedObject(TAXON_NAMESPACE, accTaxonId), TaxonBase.class);
			
		if (taxonBase != null){
			if (taxonBase.isInstanceOf(Taxon.class)){
				Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
			}else{
				logger.warn("Accepted taxon (" + accTaxonId + ") for synonym (" + synonymId +") is not of type 'Current'");
			}		
		}else{
			logger.warn("Taxon (" + accTaxonId + ") not found for synonym (" + synonymId +")");
		}
		
		return synonym;
	}

	private Taxon handleTaxon(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		String taxonNumber = rs.getString("Taxon number");
		Taxon child = (Taxon)state.getRelatedObject(TAXON_NAMESPACE, taxonNumber);
		if (child == null){
			logger.warn("Taxon does not exist: " + taxonNumber);
			return null;
		}
		
		String orderName = rs.getString("Order name");
		String subOrderName = rs.getString("Suborder name");
		String familyName = rs.getString("Family name");
		String subFamilyName = rs.getString("Subfamily name");
		String tribusName = rs.getString("Tribus name");
		String subTribusName = rs.getString("Subtribus name");
		String sectionName = rs.getString("Section name");
		String subsectionName = rs.getString("Subsection name");
		String genusName = rs.getString("Genus name");
		String subGenusName = rs.getString("Subgenus name");
		String seriesName = rs.getString("Series name");
		String specificEpihet = rs.getString("Specific epihet");
		String subspeciesName = rs.getString("Subspecies name");
		String varietyName = rs.getString("Variety name");
		String subVariety = rs.getString("Subvariery");
		String formaName = rs.getString("Forma name");
		String subFormaName = rs.getString("Subforma");
		
		makeNextHigherTaxon(state, child, orderName, subOrderName, familyName, subFamilyName, tribusName, subTribusName, sectionName,
				subsectionName, genusName, subGenusName, seriesName, specificEpihet, subspeciesName, varietyName, subVariety, formaName, subFormaName);
		return child;
	}



	/**
	 * @param state
	 * @param child
	 * @param orderName
	 * @param subOrderName
	 * @param familyName
	 * @param subFamilyName
	 * @param tribusName
	 * @param subTribusName
	 * @param sectionName
	 * @param subsectionName
	 * @param genusName
	 * @param subGenusName
	 * @param seriesName
	 * @param specificEpihet
	 * @param subspeciesName
	 * @param varietyName
	 * @param subVariety
	 * @param formaName
	 * @param subFormaName
	 */
	private void makeNextHigherTaxon(CentralAfricaFernsImportState state, Taxon child, String orderName, String subOrderName,
			String familyName, String subFamilyName, String tribusName, String subTribusName, String sectionName, String subsectionName,
			String genusName, String subGenusName, String seriesName, String specificEpihet, String subspeciesName, String varietyName,
			String subVariety, String formaName, String subFormaName) {

		Taxon higherTaxon = getNextHigherTaxon(state, child, orderName, subOrderName, familyName, subFamilyName, tribusName, subTribusName, sectionName, subsectionName, genusName, subGenusName, seriesName, specificEpihet, subspeciesName, varietyName, subVariety, formaName, subFormaName);
		
		ReferenceBase citation = null;
		if (higherTaxon != null){
			if (! includedRelationshipExists(child, higherTaxon)){
				makeTaxonomicallyIncluded(state, null, child, higherTaxon, citation, null);
			}else{
				logger.info("Included exists");
			}
			makeNextHigherTaxon(state, higherTaxon, orderName, subOrderName, familyName, subFamilyName, tribusName, subTribusName, sectionName, subsectionName, genusName, subGenusName, seriesName, specificEpihet, subspeciesName, varietyName, subVariety, formaName, subFormaName);
		}
	}

	/**
	 * Tests if this the child taxon already is a child of the higher taxon.
	 * @param child
	 * @param higherTaxon
	 * @return
	 */
	private boolean includedRelationshipExists(Taxon child, Taxon higherTaxon) {
		int countNodes = higherTaxon.getTaxonNodes().size();
		if (countNodes < 1){
			return false;
		}else if (countNodes > 1){
			throw new IllegalStateException("Multiple nodes exist for higher taxon. This is an invalid state.");
		}else{
			TaxonNode higherNode = higherTaxon.getTaxonNodes().iterator().next();
			return childExists(child, higherNode);
		}
	}



	private boolean childExists(Taxon child, TaxonNode higherNode) {
		for (TaxonNode childNode : higherNode.getChildNodes()){
			String existingChildTitle = childNode.getTaxon().getName().getTitleCache();
			String newChildTitle = child.getName().getTitleCache();
			if (existingChildTitle.equals(newChildTitle)){
				return true;
			}
		}
		return false;
	}



//	private boolean makeTaxonomicallyIncluded(CentralAfricaFernsImportState state, Taxon parent, Taxon child, ReferenceBase citation, String microCitation){
//		ReferenceBase sec = child.getSec();
//		TaxonomicTree tree = state.getTree(sec);
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
	private boolean makeTaxonomicallyIncluded(CentralAfricaFernsImportState state, Integer treeRefFk, Taxon child, Taxon parent, ReferenceBase citation, String microCitation){
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
				tree.setReference(citation);
				service.save(tree);
			}
			state.addRelatedObject(DbImportTaxIncludedInMapper.TAXONOMIC_TREE_NAMESPACE, treeKey, tree);
		}
		
		TaxonNode childNode = tree.addParentChild(parent, child, citation, microCitation);
		return (childNode != null);
	}



	private Taxon getNextHigherTaxon(CentralAfricaFernsImportState state, Taxon childTaxon, String orderName, String subOrderName, String familyName, String subFamilyName,
			String tribusName, String subTribusName, String sectionName, String subsectionName, String genusName, String subGenusName, String seriesName, String speciesName, String subspeciesName, String varietyName, String subVariety, String formaName, String subFormaName) {
		
		Taxon result = null;
		BotanicalName childName = CdmBase.deproxy(childTaxon.getName(), BotanicalName.class);
		Rank childRank = childName.getRank();
		BotanicalName higherName; 
		higherName = handleInfraSpecific(childRank, genusName, speciesName, subspeciesName, varietyName, subVariety, formaName, subFormaName);
		if (higherName.getRank() == null){
			handleSpecies(childRank, higherName, genusName, speciesName);
		}
		if (higherName.getRank() == null){
			handleInfraGeneric(childRank, higherName, genusName, subGenusName, seriesName);
		}
		if (higherName.getRank() == null){
			handleUninomial(childRank, higherName, orderName, subOrderName, familyName, subFamilyName, tribusName, subTribusName, sectionName, subsectionName, genusName);
		}
		//if higher taxon must exist, create it if it was not yet created
		if (higherName.getRank() != null && getExistingTaxon(higherName, state) == null ){
			result = Taxon.NewInstance(higherName, childTaxon.getSec());
			UUID uuid = higherName.getUuid();
			String name = higherName.getNameCache();
			taxonMap.put(name, uuid);
			state.addRelatedObject(HIGHER_TAXON_NAMESPACE, higherName.getNameCache(), result);
		}
		return result;
	}



	private Taxon getExistingTaxon(BotanicalName higherName, CentralAfricaFernsImportState state) {
		String nameCache = higherName.getNameCache();
		UUID uuid = taxonMap.get(nameCache);
		
		Taxon taxon = null;
		if (uuid != null){
			taxon = CdmBase.deproxy(getTaxonService().find(uuid), Taxon.class);
			Taxon taxon2 = state.getRelatedObject(HIGHER_TAXON_NAMESPACE, nameCache, Taxon.class);
			if (taxon != taxon2){
				logger.warn("Difference in related taxa: " + nameCache);
			}
			
		}
		return taxon;
	}



	private BotanicalName handleInfraSpecific(Rank lowerTaxonRank, String genusName, String specificEpithet, String subspeciesName, String varietyName, String subVariety, String formaName, String subFormaName) {

		BotanicalName taxonName = BotanicalName.NewInstance(null);
		Rank newRank = null;
		
		if (StringUtils.isNotBlank(subFormaName)   && lowerTaxonRank.isLower(Rank.SUBFORM())){
			taxonName.setInfraSpecificEpithet(subFormaName);
			newRank =  Rank.SUBFORM();
		}else if (StringUtils.isNotBlank(formaName)  && lowerTaxonRank.isLower(Rank.FORM())){
			taxonName.setInfraSpecificEpithet(formaName);
			newRank =  Rank.FORM();
		}else if (StringUtils.isNotBlank(subVariety)  && lowerTaxonRank.isLower(Rank.SUBVARIETY())){
			taxonName.setInfraSpecificEpithet(subVariety);
			newRank =  Rank.SUBVARIETY();
		}else if (StringUtils.isNotBlank(varietyName)  && lowerTaxonRank.isLower(Rank.VARIETY())){
			taxonName.setInfraSpecificEpithet(varietyName);
			newRank =  Rank.VARIETY();
		}else if (StringUtils.isNotBlank(subspeciesName)  && lowerTaxonRank.isLower(Rank.SUBSPECIES())){
			taxonName.setInfraSpecificEpithet(subspeciesName);
			newRank = Rank.SUBSPECIES();
		}
		
		if (newRank != null){
			taxonName.setSpecificEpithet(specificEpithet);
			taxonName.setGenusOrUninomial(genusName);
			taxonName.setRank(newRank);
		}
		
		return taxonName;
	}

	private BotanicalName handleSpecies(Rank lowerTaxonRank, BotanicalName taxonName, String genusName, String speciesEpithet) {
		Rank newRank = null;
		
		if (StringUtils.isNotBlank(speciesEpithet)  && lowerTaxonRank.isLower(Rank.SPECIES())){
			taxonName.setSpecificEpithet(speciesEpithet);
			newRank = Rank.SPECIES();
		}
		if (newRank != null){
			taxonName.setGenusOrUninomial(genusName);
			taxonName.setRank(newRank);
		}
		return taxonName;
	}

	private BotanicalName handleInfraGeneric(Rank lowerTaxonRank, BotanicalName taxonName, String genusName, String subGenusName, String seriesName) {
		Rank newRank = null;
		
		if (StringUtils.isNotBlank(seriesName)  && lowerTaxonRank.isLower(Rank.SERIES())){
			taxonName.setInfraGenericEpithet(seriesName);
			newRank = Rank.SERIES();
		}else if (StringUtils.isNotBlank(subGenusName) && lowerTaxonRank.isLower(Rank.SUBGENUS())){
			taxonName.setInfraGenericEpithet(subGenusName);
			newRank = Rank.SUBGENUS();
		}
		if (newRank != null){
			taxonName.setGenusOrUninomial(genusName);
			taxonName.setRank(newRank);
		}
		return taxonName;
	}



	private BotanicalName handleUninomial(Rank lowerTaxonRank, BotanicalName taxonName,  String orderName, String subOrderName, String familyName, String subFamilyName,
				String tribusName, String subTribusName, String sectionName, String subsectionName, String genusName) {
		
		Rank newRank = null;
		if (StringUtils.isNotBlank(genusName) && lowerTaxonRank.isLower(Rank.GENUS())){
			taxonName.setGenusOrUninomial(genusName);
			newRank =  Rank.GENUS();
		}else if (StringUtils.isNotBlank(subsectionName)  && lowerTaxonRank.isLower(Rank.SUBSECTION_BOTANY())){
			taxonName.setGenusOrUninomial(subsectionName);
			newRank =  Rank.SUBSECTION_BOTANY();
		}else if (StringUtils.isNotBlank(sectionName)  && lowerTaxonRank.isLower(Rank.SECTION_BOTANY())){
			taxonName.setGenusOrUninomial(sectionName);
			newRank =  Rank.SECTION_BOTANY();
		}else if (StringUtils.isNotBlank(subTribusName) && lowerTaxonRank.isLower(Rank.SUBTRIBE())){
			taxonName.setGenusOrUninomial(subTribusName);
			newRank =  Rank.SUBTRIBE();
		}else if (StringUtils.isNotBlank(tribusName) && lowerTaxonRank.isLower(Rank.TRIBE())){
			taxonName.setGenusOrUninomial(tribusName);
			newRank =  Rank.TRIBE();
		}else if (StringUtils.isNotBlank(subFamilyName) && lowerTaxonRank.isLower(Rank.SUBFAMILY())){
			taxonName.setGenusOrUninomial(subFamilyName);
			newRank =  Rank.SUBFAMILY();
		}else if (StringUtils.isNotBlank(familyName) && lowerTaxonRank.isLower(Rank.FAMILY())){
			taxonName.setGenusOrUninomial(familyName);
			newRank =  Rank.FAMILY();
		}else if (StringUtils.isNotBlank(subOrderName) && lowerTaxonRank.isLower(Rank.SUBORDER())){
			taxonName.setGenusOrUninomial(subOrderName);
			newRank =  Rank.SUBORDER();
		}else if (StringUtils.isNotBlank(orderName) && lowerTaxonRank.isLower(Rank.ORDER())){
			taxonName.setGenusOrUninomial(orderName);
			newRank =  Rank.ORDER();
		}
		taxonName.setRank(newRank);
		return taxonName;
	}




	private void setAuthor(BotanicalName taxonName, ResultSet rs) throws SQLException {
		String orderAuthor = rs.getString("Order name author");
		String subOrderAuthor = rs.getString("Suborder name author");
		String familyAuthor = rs.getString("Family name author");
		String subFamilyAuthor = rs.getString("Subfamily name author");
		String tribusAuthor = rs.getString("Tribus author");
		String subTribusAuthor = rs.getString("Subtribus author");
		String sectionAuthor = rs.getString("Section name author");
		String subsectionAuthor = rs.getString("Subsection author");
		String genusAuthor = rs.getString("Genus name author");
		String subGenusAuthor = rs.getString("Subgenus name author");
		String seriesAuthor = rs.getString("Series name author");
		String specificEpihetAuthor = rs.getString("Specific epithet author");
		String subspeciesAuthor = rs.getString("Subspecies author");
		String varietyAuthor = rs.getString("Variety name author");
		String subVarietyAuthor = rs.getString("Subvariety author");
		String formaAuthor = rs.getString("Forma name author");
		String subFormaAuthor = rs.getString("Subforma author");
		
		String authorsFull = rs.getString("Author/s - full");
		String authorsAbbrev = rs.getString("Author/s - abbreviated");
		

		Rank rank = taxonName.getRank();
		String authorString;
		if (rank != null){
			if (rank.equals(Rank.ORDER())){
				authorString = orderAuthor;
			}else if (rank.equals(Rank.SUBORDER())){
				authorString = subOrderAuthor;
			}else if (rank.equals(Rank.FAMILY())){
				authorString = familyAuthor;
			}else if (rank.equals(Rank.SUBFAMILY())){
				authorString = subFamilyAuthor;
			}else if (rank.equals(Rank.TRIBE())){
				authorString = tribusAuthor;
			}else if (rank.equals(Rank.SUBTRIBE())){
				authorString = subTribusAuthor;
			}else if (rank.equals(Rank.SECTION_BOTANY())){
				authorString = sectionAuthor;
			}else if (rank.equals(Rank.SUBSECTION_BOTANY())){
				authorString = subsectionAuthor;
			}else if (rank.equals(Rank.GENUS())){
				authorString = genusAuthor;
			}else if (rank.equals(Rank.SUBGENUS())){
				authorString = subGenusAuthor;
			}else if (rank.equals(Rank.SERIES())){
				authorString = seriesAuthor;
			}else if (rank.equals(Rank.SPECIES())){
				authorString = specificEpihetAuthor;
			}else if (rank.equals(Rank.SUBSPECIES())){
				authorString = subspeciesAuthor;
			}else if (rank.equals(Rank.VARIETY())){
				authorString = varietyAuthor;
			}else if (rank.equals(Rank.SUBVARIETY())){
				authorString = subVarietyAuthor;
			}else if (rank.equals(Rank.FORM())){
				authorString = formaAuthor;
			}else if (rank.equals(Rank.SUBFORM())){
				authorString = subFormaAuthor;
			}else{
				logger.warn("Author string could not be defined");
				authorString = authorsAbbrev;
				if (StringUtils.isBlank(authorString)){
					logger.warn("Authors abbrev string could not be defined");
					authorString = authorsFull;	
				}
			}
		}else{
			logger.warn("Rank is null");
			authorString = authorsAbbrev;
			if (StringUtils.isBlank(authorString)){
				logger.warn("Authors abbrev string could not be defined");
				authorString = authorsFull;	
			}
		}
		if (StringUtils.isNotBlank(authorsAbbrev) && ! authorsAbbrev.equalsIgnoreCase(authorString)){
			logger.warn("Rank author and abbrev author are not equal");
		}
		if (StringUtils.isNotBlank(authorsFull) && ! authorsFull.equalsIgnoreCase(authorString)){
			logger.warn("Rank author and full author are not equal");
		}
	
		Team team = Team.NewTitledInstance(authorString, authorString);
		taxonName.setCombinationAuthorTeam(team);
	
	}	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(CentralAfricaFernsImportState state){
		IOValidator<CentralAfricaFernsImportState> validator = new CentralAfricaFernsTaxonImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(CentralAfricaFernsImportState state){
		return ! state.getConfig().isDoTaxa();
	}





}
