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

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMethodMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportTaxIncludedInMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.validation.CentralAfricaFernsTaxonImportValidator;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;


/**
 * @author a.mueller
 */

@Component
public class CentralAfricaFernsTaxonRelationImport  extends CentralAfricaFernsImportBase<TaxonBase> implements IMappingImport<TaxonBase, CentralAfricaFernsImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsTaxonRelationImport.class);
	
	private DbImportMapping mapping;
	
	
	private static final String pluralString = "taxon relations";
	private static final String dbTableName = "[African pteridophytes]";
	private static final Class cdmTargetClass = TaxonBase.class;

	private Map<String, UUID> nameCacheTaxonMap = new HashMap<String, UUID>();
	private Map<String, UUID> titleCacheTaxonMap = new HashMap<String, UUID>();

	private CentralAfricaFernsImportState state;

	
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
	 * @see eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportBase#getMapping()
	 */
	@Override
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "createObject", ResultSet.class, CentralAfricaFernsImportState.class));
			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "mapCommonName", ResultSet.class, CentralAfricaFernsImportState.class));
			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "mapDistribution", ResultSet.class, CentralAfricaFernsImportState.class ));
			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "mapEcology", ResultSet.class, CentralAfricaFernsImportState.class));

		}
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportBase#getRecordQuery(eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(CentralAfricaFernsImportConfigurator config) {
		String strSelect = " SELECT * ";
		String strFrom = " FROM [African pteridophytes] as ap";
		String strWhere = " WHERE ( ap.[taxon number] IN (" + ID_LIST_TOKEN + ") )";
		String strOrderBy = " ORDER BY [Taxon number]";
		String strRecordQuery = strSelect + strFrom + strWhere + strOrderBy ;
		return strRecordQuery;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportBase#doInvoke(eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportState)
	 */
	@Override
	protected boolean doInvoke(CentralAfricaFernsImportState state) {
		this.state = state;
		fillTaxonMap();
		boolean success = super.doInvoke(state);
		return success;
	}


	/**
	 * Fills the nameCache and the titleCache maps. The maps are used to find existing taxa
	 * by titleCache or nameCache matching.
	 * Matching may be implemented more sophisticated in future versions.
	 */
	private void fillTaxonMap() {
		List<String> propPath = Arrays.asList(new String []{"name"});
		
		List<Taxon> taxonList = (List)getTaxonService().list(Taxon.class, null, null, null, propPath );
		for (Taxon taxon : taxonList){
			NonViralName nvn = CdmBase.deproxy(taxon.getName(), NonViralName.class);
			UUID uuid = taxon.getUuid();
			String nameCache = nvn.getNameCache();
			String titleCache = nvn.getTitleCache();
			nameCacheTaxonMap.put(nameCache, uuid);
			titleCacheTaxonMap.put(titleCache, uuid);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
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

			//taxon map
			nameSpace = TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, taxonIdSet, nameSpace);
			result.put(nameSpace, taxonMap);
				
				
			//reference map
			this.sourceReference = getFernsSourceReference(state);
//			nameSpace = "Reference";
//			cdmClass = Reference.class;
//			Map<String, Person> referenceMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(Person.class, teamIdSet, nameSpace);
//			result.put(Reference.class, referenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	@Override
	public TaxonBase createObject(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		TaxonBase result = null;
		try {
			String status = rs.getString("Current/Synonym");
			String taxonNumber = rs.getString("Taxon number");
			state.setTaxonNumber(taxonNumber);
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

	
	/**
	 * Class to store all epithets of the database record. Maybe extended with business logic.
	 */
	private class Epithets{
		private String orderName;
		private String subOrderName;
		private String familyName;
		private String subFamilyName;
		private String tribusName;
		private String subTribusName;
		private String sectionName;
		private String subsectionName;
		private String genusName;
		private String subGenusName;
		private String seriesName;
		private String specificEpithet;
		private String subspeciesName;
		private String varietyName;
		private String subVariety;
		private String formaName;
		private String subFormaName;
	}

	
	/**
	 * Handles records with status synonym. A synonym relationship to the accepted taxon
	 * is created.
	 * @param rs
	 * @param state
	 * @return
	 * @throws SQLException
	 */
	private Synonym handleSynonym(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		String accTaxonId = rs.getString("Current");
		String nomRemarksString = rs.getString("Current/Synonym");
		
		String synonymId = state.getTaxonNumber();
		Synonym synonym = (Synonym)state.getRelatedObject(TAXON_NAMESPACE, synonymId);
		if (synonym == null){
			logger.warn ("Synonym ("+synonymId+")not found.");
			return null;
		}
		TaxonBase taxonBase = CdmBase.deproxy(state.getRelatedObject(TAXON_NAMESPACE, accTaxonId), TaxonBase.class);
			
		if (taxonBase != null){
			if (taxonBase.isInstanceOf(Taxon.class)){
				Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				SynonymRelationship rel = taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
				if ("p.p.".equalsIgnoreCase(nomRemarksString)){
					rel.setProParte(true);
				}
			}else{
				logger.warn("Accepted taxon (" + accTaxonId + ") for synonym (" + synonymId +") is not of type 'Current'");
			}		
		}else{
			logger.warn("Taxon (" + accTaxonId + ") not found for synonym (" + synonymId +")");
		}
		
		return synonym;
	}

	
	/**
	 * Handles all records with status 'current'. Creates parent-child relationships to the 
	 * higher taxa. Uses a complex algorithm to reuse existing higher taxa.
	 * @param rs
	 * @param state
	 * @return
	 * @throws SQLException
	 */
	private Taxon handleTaxon(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		String taxonNumber = rs.getString("Taxon number");
		Taxon child = (Taxon)state.getRelatedObject(TAXON_NAMESPACE, taxonNumber);
		if (child == null){
			logger.warn("Taxon does not exist: " + taxonNumber);
			return null;
		}
		Epithets epithets = new Epithets();
		epithets.orderName = rs.getString("Order name");
		epithets.subOrderName = rs.getString("Suborder name");
		epithets.familyName = rs.getString("Family name");
		epithets.subFamilyName = rs.getString("Subfamily name");
		epithets.tribusName = rs.getString("Tribus name");
		epithets.subTribusName = rs.getString("Subtribus name");
		epithets.sectionName = rs.getString("Section name");
		epithets.subsectionName = rs.getString("Subsection name");
		epithets.genusName = rs.getString("Genus name");
		epithets.subGenusName = rs.getString("Subgenus name");
		epithets.seriesName = rs.getString("Series name");
		epithets.specificEpithet = rs.getString("Specific epihet");
		epithets.subspeciesName = rs.getString("Subspecies name");
		epithets.varietyName = rs.getString("Variety name");
		epithets.subVariety = rs.getString("Subvariery");
		epithets.formaName = rs.getString("Forma name");
		epithets.subFormaName = rs.getString("Subforma");
		
		makeNextHigherTaxon(state, rs, child, epithets);
		return child;
	}


	/**
	 * Adds recursively this taxon to the next higher taxon. If the taxon exists already 
	 * the relationship is not added again.<BR>
	 * If the author is missing in the old taxon but not in the new taxon the 
	 * old taxon will get the new taxons author.(NOT VALID ANY MORE)<BR> 
	 * If authors differ a new taxon is created.<BR>
	 * If a higher taxon exists the method is called recursively on this taxon.
	 * @throws SQLException 
	 */
	private void makeNextHigherTaxon(CentralAfricaFernsImportState state, ResultSet rs, Taxon child, Epithets epithets) throws SQLException {

		Taxon constructedHigherTaxon = constructNextHigherTaxon(state, rs, child, epithets);
		Reference<?> citation = null;
		String microcitation = null;
		
		if (constructedHigherTaxon != null){
			handleHigherTaxonMustExist(state, rs, child, epithets, constructedHigherTaxon, citation, microcitation);
		}else{
			//add taxon to tree if not yet added
			if (child.getTaxonNodes().size() == 0){
				makeTaxonomicallyIncluded(state, null, child, null, citation, microcitation);
			}
		}
	}



	/**
	 * Handles the case when the database record has data for a taxon of a higher rank
	 * than the <code>child</code> taxon's rank.
	 * @param state
	 * @param rs
	 * @param child
	 * @param epithets
	 * @param higherTaxon
	 * @param citation
	 * @param microcitation
	 * @throws SQLException
	 */
	private void handleHigherTaxonMustExist(CentralAfricaFernsImportState state, ResultSet rs, Taxon child, Epithets epithets, Taxon constructedHigherTaxon, Reference<?> citation, String microCitation) throws SQLException {
		Taxon parentTaxon = getParent(child);
		if (parentTaxon == null){
			//if no parent taxon exists
			Taxon existingTaxon = findExistingNonParentTaxon(state, constructedHigherTaxon);
			if (existingTaxon != null){
				//a taxon with same title cache or same name cache exists
				parentTaxon = mergeExistingAndConstructedTaxon(state, existingTaxon, constructedHigherTaxon);
			}else{
				parentTaxon = constructedHigherTaxon;
			}
			makeTaxonomicallyIncluded(state, null, child, parentTaxon, citation, microCitation);
		}else{
			//parent taxon exists
			if (namesMatch(parentTaxon, constructedHigherTaxon)){
				//parents match
				//TODO what if the higher taxonomy does not match
				parentTaxon = mergeExistingAndConstructedTaxon(state, parentTaxon, constructedHigherTaxon);
			}else if (compareRanks(parentTaxon, constructedHigherTaxon) != 0){
				//ranks unequal
				parentTaxon = handleUnequalRanks(parentTaxon, constructedHigherTaxon);
			}else if (! nameCachesMatch(parentTaxon, constructedHigherTaxon)){
				//nameCache not equal
				parentTaxon = handleUnequalNameCaches(parentTaxon, constructedHigherTaxon);
			}else if (! authorsMatch(parentTaxon, constructedHigherTaxon)){
				//nameCache not equal
				parentTaxon = handleUnequalAuthors(parentTaxon, constructedHigherTaxon);
			}
		}
		//save the parent taxon, if it is new
		if (parentTaxon == constructedHigherTaxon){
			saveConstructedTaxon(state, constructedHigherTaxon);
		}
		makeNextHigherTaxon(state, rs, parentTaxon, epithets);
	}
	

	/**
	 * Merges author information of the constructed taxon into the existing taxon.
	 * Returns the existing taxon.
	 * @param state 
	 * @param parentTaxon
	 * @param constructedHigherTaxon
	 */
	private Taxon mergeExistingAndConstructedTaxon(CentralAfricaFernsImportState state, Taxon existingTaxon, Taxon constructedTaxon) {
		NonViralName constructedName = CdmBase.deproxy(constructedTaxon.getName(), NonViralName.class);
		NonViralName existingName = CdmBase.deproxy(existingTaxon.getName(), NonViralName.class);
		if (constructedName.hasAuthors()){
			if (! existingName.hasAuthors()){
				logger.warn(state.getTaxonNumber() + " - Constrcucted name ("+constructedName.getTitleCache()+") has authors but existing name ("+existingName.getTitleCache()+") has no authors");
			}else if (! authorsMatch(constructedName, existingName)){
				logger.warn(state.getTaxonNumber() + " - Constrcucted name ("+constructedName.getTitleCache()+") and existing name ("+existingName.getTitleCache()+") have different authors");
			}else {
				//authors match and are not null
			}
		}
		// more?
		return existingTaxon;
	}


	/**
	 * Strategy for the decision if an existing parent or a constructed higher taxon should 
	 * be taken as parent in case that the authors of the name differ somehow.
	 * Current strategy: use existing parent if constructed higher taxon has no authors 
	 * at all. Use constructed taxon otherwise.
	 * @param existingParentTaxon
	 * @param constructedHigherTaxon
	 * @return
	 */
	private Taxon handleUnequalAuthors(Taxon existingParentTaxon, Taxon constructedHigherTaxon) {
		Taxon result;
		BotanicalName existingName = CdmBase.deproxy(existingParentTaxon.getName(), BotanicalName.class);
		BotanicalName constructedName = (BotanicalName)constructedHigherTaxon.getName();
		//current strategy: if constructedName has no authors (and parentName has
		if (! constructedName.hasAuthors()){
			result = existingParentTaxon;
		}else if (! existingName.hasAuthors()){
			result = constructedHigherTaxon;
		}else{
			result = constructedHigherTaxon;
		}
		return result;
	}

	/**
	 * Strategy for the decision if an existing parent or a constructed higher taxon 
	 * should be taken as parent in case that the name caches differ somehow.
	 * Current strategy: Not implemented. Always use constructed higher taxon.
	 * @param existingParentTaxon
	 * @param constructedHigherTaxon
	 * @return
	 */
	private Taxon handleUnequalNameCaches(Taxon parentTaxon, Taxon constructedHigherTaxon) {
		BotanicalName parentName = CdmBase.deproxy(parentTaxon.getName(), BotanicalName.class);
		BotanicalName constructedName = (BotanicalName)constructedHigherTaxon.getName();
		logger.warn("handleUnequalNameCaches not yet implemented");
		return constructedHigherTaxon;
	}


	/**
	 * Handles the case that the existing parent taxon and the constructed parent taxon
	 * have a diffent rank. Returns the constructedHigherTaxon if no common grand parent exists. 
	 * @param parentTaxon
	 * @param constructedHigherTaxon
	 * @return
	 */
	private Taxon handleUnequalRanks(Taxon parentTaxon, Taxon constructedHigherTaxon) {
		BotanicalName parentName = CdmBase.deproxy(parentTaxon.getName(), BotanicalName.class);
		BotanicalName constructedName = (BotanicalName)constructedHigherTaxon.getName();
		int compare = compareRanks(parentName, constructedName);
		Taxon lowerTaxon = parentTaxon;
		Taxon grandParentTaxon = constructedHigherTaxon;
		if (compare < 0){
			lowerTaxon = constructedHigherTaxon;
			grandParentTaxon = parentTaxon;
		}	
		Taxon commonGrandParent = checkIsGrandParent(lowerTaxon, grandParentTaxon);
		if (commonGrandParent != null){
			if (lowerTaxon == constructedHigherTaxon){
				//TODO merge 
				logger.warn("Merge in between taxon not yet implemented");
			}
		}else{
			return constructedHigherTaxon;
		}
		return lowerTaxon;
	}

	/**
	 * Tries to find a taxon which matches the constructed taxon but is not a parent
	 * taxon of the constructed taxon's child (condition will not be checked).
	 * Returns null if no such taxon exists.
	 * @param constructedHigherTaxon
	 * @param state 
	 * @return
	 */
	private Taxon findExistingNonParentTaxon(CentralAfricaFernsImportState state, Taxon constructedHigherTaxon) {
		BotanicalName constructedName = CdmBase.deproxy(constructedHigherTaxon.getName(), BotanicalName.class);
		String titleCache = constructedName.getTitleCache();
		String nameCache = constructedName.getNameCache();
		UUID existingUuid = titleCacheTaxonMap.get(titleCache);
		if (existingUuid == null){
			existingUuid = nameCacheTaxonMap.get(nameCache);
		}
		Taxon relatedTaxon = null;
		if (existingUuid != null){
			relatedTaxon = state.getRelatedObject(HIGHER_TAXON_NAMESPACE, nameCache, Taxon.class);
			if (relatedTaxon == null){
				//TODO find for partition
				relatedTaxon = (Taxon)getTaxonService().find(existingUuid);
				if (relatedTaxon == null){
					logger.info(state.getTaxonNumber() +  " - Could not find existing name ("+nameCache+") in related objects map");
				}else{
					state.addRelatedObject(HIGHER_TAXON_NAMESPACE, nameCache, relatedTaxon);
				}
			}
		}
		return relatedTaxon;
	}

	/**
	 * Checks if a taxon is a grand parent of another taxon
	 * @param lowerTaxon
	 * @param higherTaxon
	 * @return
	 */
	private Taxon checkIsGrandParent(Taxon childTaxon, Taxon grandParentTaxon) {
		BotanicalName lowerName = CdmBase.deproxy(childTaxon.getName(), BotanicalName.class);
		BotanicalName higherName = CdmBase.deproxy(grandParentTaxon.getName(), BotanicalName.class);

		//TODO was wenn lowerTaxon constructed ist
		logger.warn("checkIsGrandParent not yet fully implemented");
		Taxon nextParent = getParent(childTaxon); 
		if (namesMatch(nextParent, grandParentTaxon)){
			//TODO which one to return? Merging ?
			logger.warn("checkIsGrandParent(matching) not yet fully implemented");
			return grandParentTaxon;
		}else{
			if (compareRanks(lowerName, higherName) >= 0){
				return null;
			}else{
				return checkIsGrandParent(childTaxon, grandParentTaxon);
			}
		}
	}


	/**
	 * Checks if the name caches match.
	 * @param name1
	 * @param name2
	 * @return
	 */
	private boolean nameCachesMatch(BotanicalName name1, BotanicalName name2) {
		return CdmUtils.nullSafeEqual(name1.getNameCache(), name2.getNameCache());
	}
	
	/**
	 * Checks if the name caches of the related names match.
	 *@param taxon1
	 * @param taxon2
	 * @return
	 */
	private boolean nameCachesMatch(Taxon taxon1, Taxon taxon2) {
		BotanicalName name1 = CdmBase.deproxy(taxon1.getName(), BotanicalName.class);
		BotanicalName name2 = CdmBase.deproxy(taxon2.getName(), BotanicalName.class);
		return nameCachesMatch(name1, name2);
	}


	/**
	 * Checks if all authors match
	 * @param name1
	 * @param name2
	 * @return
	 */
	private boolean authorsMatch(NonViralName<?> name1, NonViralName<?> name2) {
		String combinationAuthor1 = name1.computeCombinationAuthorNomenclaturalTitle();
		String combinationAuthor2 = name2.computeCombinationAuthorNomenclaturalTitle();
		String basionymAuthor1 = name1.computeBasionymAuthorNomenclaturalTitle();
		String basionymAuthor2 = name2.computeBasionymAuthorNomenclaturalTitle();
		String exCombinationAuthor1 = name1.computeExCombinationAuthorNomenclaturalTitle();
		String exCombinationAuthor2 = name2.computeExCombinationAuthorNomenclaturalTitle();
		String exBasionymAuthor1 = name1.computeExBasionymAuthorNomenclaturalTitle();
		String exBasionymAuthor2 = name2.computeExBasionymAuthorNomenclaturalTitle();
		boolean result = 
			CdmUtils.nullSafeEqual(combinationAuthor1, combinationAuthor2) &&
			CdmUtils.nullSafeEqual(basionymAuthor1, basionymAuthor2) &&
			CdmUtils.nullSafeEqual(exCombinationAuthor1, exCombinationAuthor2) &&
			CdmUtils.nullSafeEqual(exBasionymAuthor1, exBasionymAuthor2);
		return result;
	}
	
	/**
	 * Checks if all authors of the related names match.
	 * @param taxon1
	 * @param taxon2
	 * @return
	 */
	private boolean authorsMatch(Taxon taxon1, Taxon taxon2) {
		BotanicalName name1 = CdmBase.deproxy(taxon1.getName(), BotanicalName.class);
		BotanicalName name2 = CdmBase.deproxy(taxon2.getName(), BotanicalName.class);
		return authorsMatch(name1, name2);
	}

	/**
	 * Compares ranks of 2 names.
	 * @param parentName
	 * @param constructedName
	 * @return
	 */
	private int compareRanks(BotanicalName name1, BotanicalName name2) {
		return name1.getRank().compareTo(name2.getRank());
	}
	
	/**
	 * Compares the ranks of the according names.
	 * @param taxon1
	 * @param taxon2
	 * @return
	 */
	private int compareRanks(Taxon taxon1, Taxon taxon2) {
		BotanicalName name1 = CdmBase.deproxy(taxon1.getName(), BotanicalName.class);
		BotanicalName name2 = CdmBase.deproxy(taxon2.getName(), BotanicalName.class);
		return compareRanks(name1, name2);
	}
	
	

	/**
	 * Checks if 2 names match.
	 * Current strategy: true, if ranks are equal, nameCaches match and authors match 
	 * @param name1
	 * @param name2
	 * @return
	 */
	private boolean namesMatch(BotanicalName name1, BotanicalName name2) {
		return compareRanks(name1, name2)==0 && nameCachesMatch(name1, name2) && authorsMatch(name1, name2);
	}
	
	/**
	 * Checks if the according names match.
	 * @see #namesMatch(BotanicalName, BotanicalName)
	 * @param taxon1
	 * @param taxon2
	 * @return
	 */
	private boolean namesMatch(Taxon taxon1, Taxon taxon2) {
		BotanicalName name1 = CdmBase.deproxy(taxon1.getName(), BotanicalName.class);
		BotanicalName name2 = CdmBase.deproxy(taxon2.getName(), BotanicalName.class);
		return namesMatch(name1, name2);
	}

	
	/**
	 * Returns the only parent of the taxon. If not parent exists <code>null</code> is
	 * returned.
	 * TODO move to taxon class (with classification)
	 * @param child
	 * @return
	 * @throws IllegalStateException if taxon belongs to multiple states
	 */
	private Taxon getParent(Taxon child) {
		int countNodes = child.getTaxonNodes().size();
		if (countNodes < 1){
			return null;
		}else if (countNodes > 1){
			throw new IllegalStateException("Multiple nodes exist for child taxon. This is an invalid state for this import.");
		}else{
			TaxonNode childNode = child.getTaxonNodes().iterator().next();
			TaxonNode parentNode = childNode.getParent();
			if (parentNode != null){
				return parentNode.getTaxon();
			}else{
				return null;
			}
		}
	}
	

	/**
	 * Persists and saves the newly created taxon to the CDM store and to the look-up
	 * maps.
	 * @param state
	 * @param constructedHigherTaxon
	 * @return
	 */
	private Taxon saveConstructedTaxon(CentralAfricaFernsImportState state, Taxon constructedHigherTaxon) {
		BotanicalName constructedName = CdmBase.deproxy(constructedHigherTaxon.getName(), BotanicalName.class);
		String nameCache = constructedName.getNameCache();
		String titleCache = constructedName.getTitleCache();
		nameCacheTaxonMap.put(nameCache, constructedHigherTaxon.getUuid());
		titleCacheTaxonMap.put(titleCache, constructedHigherTaxon.getUuid());
		state.addRelatedObject(HIGHER_TAXON_NAMESPACE, nameCache, constructedHigherTaxon);
		
		//persist
//		Reference citation = state.getConfig().getSourceReference(); //throws nonUniqueObject exception
		Reference citation = null;  
		String id = state.getTaxonNumber() + "-" + constructedName.getRank().getTitleCache();
		addOriginalSource(constructedName, id, NAME_NAMESPACE, citation);
		addOriginalSource(constructedHigherTaxon, id, TAXON_NAMESPACE, citation);
		getTaxonService().save(constructedHigherTaxon);
		
		return constructedHigherTaxon;
	}


	//TODO use Mapper
	/**
	 * Adds the parent child relationship. Creates and saves the classification if needed.
	 * Adds parent and child to the classification.
	 * @param state
	 * @param treeRefFk
	 * @param child
	 * @param parent
	 * @param citation
	 * @param microCitation
	 * @return
	 */
	private boolean makeTaxonomicallyIncluded(CentralAfricaFernsImportState state, Integer treeRefFk, Taxon child, Taxon parent, Reference citation, String microCitation){
		String treeKey;
		UUID treeUuid;
		if (treeRefFk == null){
			treeKey = "1";  // there is only one tree and it gets the map key '1'
			treeUuid = state.getConfig().getClassificationUuid();
		}else{
			treeKey =String.valueOf(treeRefFk);
			treeUuid = state.getTreeUuidByTreeKey(treeKey);
		}
		Classification tree = (Classification)state.getRelatedObject(DbImportTaxIncludedInMapper.TAXONOMIC_TREE_NAMESPACE, treeKey);
		if (tree == null){
			IClassificationService service = state.getCurrentIO().getClassificationService();
			tree = service.getClassificationByUuid(treeUuid);
			if (tree == null){
				String treeName = state.getConfig().getClassificationName();
				tree = Classification.NewInstance(treeName);
				tree.setUuid(treeUuid);
				//FIXME tree reference
				tree.setReference(citation);
				service.save(tree);
			}
			state.addRelatedObject(DbImportTaxIncludedInMapper.TAXONOMIC_TREE_NAMESPACE, treeKey, tree);
		}
		
		TaxonNode childNode;
		if (parent != null){
			childNode = tree.addParentChild(parent, child, citation, microCitation);
		}else{
			childNode = tree.addChildTaxon(child, citation, microCitation, null);
		}
		return (childNode != null);
	}


	/**
	 * Reasons if a higher taxon should exist. If it should exist it returns it as a new taxon.
	 * Returns null otherwise.
	 * @return
	 * @throws SQLException
	 */
	private Taxon constructNextHigherTaxon(CentralAfricaFernsImportState state, ResultSet rs, Taxon childTaxon, Epithets epithets) throws SQLException {
		
		Taxon result = null;
		BotanicalName childName = CdmBase.deproxy(childTaxon.getName(), BotanicalName.class);
		Rank childRank = childName.getRank();
		BotanicalName higherName;
		higherName = handleInfraSpecific(childRank, epithets);
		if (higherName.getRank() == null){
			handleSpecies(childRank, higherName,  epithets);
		}
		if (higherName.getRank() == null){
			handleInfraGeneric(childRank, higherName, epithets);
		}
		if (higherName.getRank() == null){
			handleUninomial(childRank, higherName, epithets);
		}
		
		if (higherName.getRank() != null){
			result = Taxon.NewInstance(higherName, childTaxon.getSec());
			//TODO correct??
			setAuthor(higherName, rs, state.getTaxonNumber(), true);
//			UUID uuid = higherName.getUuid();
//			String name = higherName.getNameCache();
//			taxonMap.put(name, uuid);
//			state.addRelatedObject(HIGHER_TAXON_NAMESPACE, higherName.getNameCache(), result);
		}
		return result;
	}

	private BotanicalName handleInfraSpecific(Rank lowerTaxonRank, Epithets epithets) {

		BotanicalName taxonName = BotanicalName.NewInstance(null);
		Rank newRank = null;
		
		if (StringUtils.isNotBlank(epithets.subFormaName)   && lowerTaxonRank.isLower(Rank.SUBFORM())){
			taxonName.setInfraSpecificEpithet(epithets.subFormaName);
			newRank =  Rank.SUBFORM();
		}else if (StringUtils.isNotBlank(epithets.formaName)  && lowerTaxonRank.isLower(Rank.FORM())){
			taxonName.setInfraSpecificEpithet(epithets.formaName);
			newRank =  Rank.FORM();
		}else if (StringUtils.isNotBlank(epithets.subVariety)  && lowerTaxonRank.isLower(Rank.SUBVARIETY())){
			taxonName.setInfraSpecificEpithet(epithets.subVariety);
			newRank =  Rank.SUBVARIETY();
		}else if (StringUtils.isNotBlank(epithets.varietyName)  && lowerTaxonRank.isLower(Rank.VARIETY())){
			taxonName.setInfraSpecificEpithet(epithets.varietyName);
			newRank =  Rank.VARIETY();
		}else if (StringUtils.isNotBlank(epithets.subspeciesName)  && lowerTaxonRank.isLower(Rank.SUBSPECIES())){
			taxonName.setInfraSpecificEpithet(epithets.subspeciesName);
			newRank = Rank.SUBSPECIES();
		}
		
		if (newRank != null){
			taxonName.setSpecificEpithet(epithets.specificEpithet);
			taxonName.setGenusOrUninomial(epithets.genusName);
			taxonName.setRank(newRank);
		}
		
		return taxonName;
	}

	private BotanicalName handleSpecies(Rank lowerTaxonRank, BotanicalName taxonName, Epithets epithets) {
		Rank newRank = null;
		
		if (StringUtils.isNotBlank(epithets.specificEpithet)  && lowerTaxonRank.isLower(Rank.SPECIES())){
			taxonName.setSpecificEpithet(epithets.specificEpithet);
			newRank = Rank.SPECIES();
		}
		if (newRank != null){
			taxonName.setGenusOrUninomial(epithets.genusName);
			taxonName.setRank(newRank);
		}
		return taxonName;
	}

	private BotanicalName handleInfraGeneric(Rank lowerTaxonRank, BotanicalName taxonName, Epithets epithets) {
		Rank newRank = null;
		
		if (StringUtils.isNotBlank(epithets.seriesName)  && lowerTaxonRank.isLower(Rank.SERIES())){
			taxonName.setInfraGenericEpithet(epithets.seriesName);
			newRank = Rank.SERIES();
		}else if (StringUtils.isNotBlank(epithets.subsectionName)  && lowerTaxonRank.isLower(Rank.SUBSECTION_BOTANY())){
			taxonName.setInfraGenericEpithet(epithets.subsectionName);
			newRank =  Rank.SUBSECTION_BOTANY();
		}else if (StringUtils.isNotBlank(epithets.sectionName)  && lowerTaxonRank.isLower(Rank.SECTION_BOTANY())){
			taxonName.setInfraGenericEpithet(epithets.sectionName);
			newRank =  Rank.SECTION_BOTANY();
		}else if (StringUtils.isNotBlank(epithets.subGenusName) && lowerTaxonRank.isLower(Rank.SUBGENUS())){
			taxonName.setInfraGenericEpithet(epithets.subGenusName);
			newRank = Rank.SUBGENUS();
		}
		if (newRank != null){
			taxonName.setGenusOrUninomial(epithets.genusName);
			taxonName.setRank(newRank);
		}
		return taxonName;
	}



	private BotanicalName handleUninomial(Rank lowerTaxonRank, BotanicalName taxonName,  Epithets epithets) {
		
		Rank newRank = null;
		if (StringUtils.isNotBlank(epithets.genusName) && lowerTaxonRank.isLower(Rank.GENUS())){
			taxonName.setGenusOrUninomial(epithets.genusName);
			newRank =  Rank.GENUS();
		}else if (StringUtils.isNotBlank(epithets.subTribusName) && lowerTaxonRank.isLower(Rank.SUBTRIBE())){
			taxonName.setGenusOrUninomial(epithets.subTribusName);
			newRank =  Rank.SUBTRIBE();
		}else if (StringUtils.isNotBlank(epithets.tribusName) && lowerTaxonRank.isLower(Rank.TRIBE())){
			taxonName.setGenusOrUninomial(epithets.tribusName);
			newRank =  Rank.TRIBE();
		}else if (StringUtils.isNotBlank(epithets.subFamilyName) && lowerTaxonRank.isLower(Rank.SUBFAMILY())){
			taxonName.setGenusOrUninomial(epithets.subFamilyName);
			newRank =  Rank.SUBFAMILY();
		}else if (StringUtils.isNotBlank(epithets.familyName) && lowerTaxonRank.isLower(Rank.FAMILY())){
			taxonName.setGenusOrUninomial(epithets.familyName);
			newRank =  Rank.FAMILY();
		}else if (StringUtils.isNotBlank(epithets.subOrderName) && lowerTaxonRank.isLower(Rank.SUBORDER())){
			taxonName.setGenusOrUninomial(epithets.subOrderName);
			newRank =  Rank.SUBORDER();
		}else if (StringUtils.isNotBlank(epithets.orderName) && lowerTaxonRank.isLower(Rank.ORDER())){
			taxonName.setGenusOrUninomial(epithets.orderName);
			newRank =  Rank.ORDER();
		}
		taxonName.setRank(newRank);
		return taxonName;
	}
	

	/**
	 * for internal use only, used by MethodMapper
	 */
	private TaxonBase mapCommonName(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException{
		String taxonNumber = state.getTaxonNumber();
		String commonNames = rs.getString("Common names");
		TaxonBase<?> taxonBase = state.getRelatedObject(state.CURRENT_OBJECT_NAMESPACE, state.CURRENT_OBJECT_ID, TaxonBase.class);
		if (StringUtils.isNotBlank(commonNames)){
			Taxon taxon = getAcceptedTaxon(taxonBase);
			if ( taxon != null ){
				TaxonDescription description = getTaxonDescription(taxon, false, true);
				String[] split = commonNames.split(",");
				for (String commonNameString: split){
					CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString.trim(), Language.ENGLISH());
					description.addElement(commonName);				
				}
			}else{
				logger.warn(taxonNumber + " - Accepted taxon for synonym can't be defined for common name. Synonym " + taxonBase.getName().getTitleCache());
			}
		}
		return taxonBase;
	}
	

	/**
	 * for internal use only, used by MethodMapper
	 */
	private TaxonBase mapDistribution(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException{
		try {
			String taxonNumber = state.getTaxonNumber();
//			logger.info(taxonNumber);
			TaxonBase<?> taxonBase = state.getRelatedObject(state.CURRENT_OBJECT_NAMESPACE, state.CURRENT_OBJECT_ID, TaxonBase.class);
			String countriesString = rs.getString("Distribution - Country");
			String province = rs.getString("Distribution - Province");
			String distributionDetailed = rs.getString("Distribution - detailed");
			if (taxonBase != null){
				TaxonNameBase nameUsedInSource = taxonBase.getName();
				Taxon taxon = getAcceptedTaxon(taxonBase);
				if (taxon != null){
				
					if (StringUtils.isNotBlank(countriesString) ){
						makeCountries(state, taxonNumber, taxon, nameUsedInSource, countriesString, province, distributionDetailed);
					}
					makeProvince(taxon, province);
					makeDistributionDetailed(taxon, distributionDetailed);
				}else{
					logger.warn(taxonNumber + " - Accepted taxon for synonym can't be defined for distribution. Synonym " + taxonBase.getName().getTitleCache());
				}
			}else{
				logger.warn(" - " + taxonNumber + ": TaxonBase was null");
			}
			return taxonBase;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * for internal use only, used by MethodMapper
	 * @param commonNames 
	 */
	private TaxonBase mapEcology(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException{
		String taxonNumber = state.getTaxonNumber();
		String ecologyString = rs.getString("Ecology");
		TaxonBase<?> taxonBase = state.getRelatedObject(state.CURRENT_OBJECT_NAMESPACE, state.CURRENT_OBJECT_ID, TaxonBase.class);
		if (StringUtils.isNotBlank(ecologyString)){
			Taxon taxon = getAcceptedTaxon(taxonBase);
			
			if (taxon != null){
				TaxonDescription description = getTaxonDescription(taxon, false, true);
				TextData ecology = TextData.NewInstance(Feature.ECOLOGY());
				ecology.putText(Language.ENGLISH(), ecologyString.trim());
				description.addElement(ecology);				
			}else{
				logger.warn(taxonNumber + " - Accepted taxon for synonym can't be defined for ecology. Synonym " + taxonBase.getName().getTitleCache());
			}
		}
		return taxonBase;
	}




	private void makeDistributionDetailed(Taxon taxon, String distributionDetailed) {
		if (StringUtils.isNotBlank(distributionDetailed)){
			TaxonDescription description = getTaxonDescription(taxon, false, true);
			TextData distribution = TextData.NewInstance(Feature.DISTRIBUTION());
			description.addElement(distribution);
			distribution.putText(Language.ENGLISH(), distributionDetailed);
		}
	}

	
	private void makeProvince(Taxon taxon, String province) {
		if (StringUtils.isNotBlank(province)){
			TaxonDescription description = getTaxonDescription(taxon, false, true);
			TextData distribution = TextData.NewInstance(Feature.DISTRIBUTION());
			description.addElement(distribution);
			distribution.putText(Language.ENGLISH(), province);
		}
	}
	

	/**
	 * @param state
	 * @param taxonNumber
	 * @param taxonBase
	 * @param countriesString
	 */
	private void makeCountries(CentralAfricaFernsImportState state, String taxonNumber, Taxon taxon, TaxonNameBase nameUsedInSource, String countriesString, String province, String distributionDetailed) {
		countriesString = countriesString.replaceAll("\\*", "");  
		countriesString = countriesString.replace("  ", " ");
		countriesString = countriesString.replace(", endemic", " - endemic");
		countriesString = countriesString.replace("(endemic)", " - endemic");
		countriesString = countriesString.replace("(introduced)", " - introduced");
		countriesString = countriesString.replace("(naturalised)", " - naturalised");
		countriesString = countriesString.replace("Madagascar-", "Madagascar -");
		countriesString = countriesString.replace("Mahé", "Mahe");
		 
		String[] split = countriesString.split("[,;]");
		String remainingString = null;
		for (String countryString : split){
			countryString = CdmUtils.concat(", ", remainingString , countryString);
			if (countryString.matches(".*\\(.*") && ! countryString.matches(".*\\).*")){
				remainingString = countryString;
				continue;
			}
			remainingString = null;
			try {
				makeSingleCountry(state, taxonNumber, taxon, nameUsedInSource, countryString.trim());
			} catch (UndefinedTransformerMethodException e) {
				e.printStackTrace();
			}
		}
	}


	private void makeSingleCountry(CentralAfricaFernsImportState state, String taxonNumber, Taxon taxon, TaxonNameBase nameUsedInSource, String country) throws UndefinedTransformerMethodException {
		boolean areaDoubtful = false;
		Distribution distribution = Distribution.NewInstance(null, PresenceTerm.PRESENT());
		Reference sourceReference = this.sourceReference;
		distribution.addSource(taxonNumber, "Distribution_Country", sourceReference, null, nameUsedInSource, null);
		NamedArea area = null;
		//empty
		if (StringUtils.isBlank(country)){
			return;
		}
		country = country.trim();
		//doubtful
		if (country.startsWith("?")){
			areaDoubtful = true;
			country = country.substring(1).trim();
		}
		//status
		country = makeCountryStatus(state, country, distribution);
		
		//brackets
		country = makeCountryBrackets(state, taxonNumber, taxon, nameUsedInSource, country);
		String countryWithoutIslands = null;
		String countryWithoutDot = null;
		if (country.endsWith(" Isl.") || country.endsWith(" isl.") ){
			countryWithoutIslands = country.substring(0, country.length()-5);
		}
		if (country.endsWith(".")){
			countryWithoutDot = country.substring(0, country.length()-1);
		}
		if (country.endsWith("*")){
			country = country.substring(0, country.length()-1);
		}
		if (country.endsWith("Islands")){
			country = country.replace("Islands", "Is.");
		}
		
		
		//areas
		if (TdwgArea.isTdwgAreaLabel(country)){
			//tdwg
			area = TdwgArea.getAreaByTdwgLabel(country);
		}else if (TdwgArea.isTdwgAreaLabel(countryWithoutIslands)){
			//tdwg
			area = TdwgArea.getAreaByTdwgLabel(countryWithoutIslands);
		}else if (TdwgArea.isTdwgAreaLabel(countryWithoutDot)){
			//tdwg
			area = TdwgArea.getAreaByTdwgLabel(countryWithoutDot);
		}else if ( (area = state.getTransformer().getNamedAreaByKey(country)) != null) {
			//area already set
		}else if (WaterbodyOrCountry.isWaterbodyOrCountryLabel(country)){
			//iso
			area = WaterbodyOrCountry.getWaterbodyOrCountryByLabel(country);
		}else{
			//others
			NamedAreaLevel level = null;
			NamedAreaType areaType = null;
			
			UUID uuid = state.getTransformer().getNamedAreaUuid(country);
			if (uuid == null){
				logger.error(taxonNumber + " - Unknown country: " + country);
			}
			area = getNamedArea(state, uuid, country, country, country, areaType, level);
		}
		
		distribution.setArea(area);
		if (areaDoubtful == true){
			if (distribution.getStatus().equals(PresenceTerm.PRESENT())){
				distribution.setStatus(PresenceTerm.PRESENT_DOUBTFULLY());
			}
		}
		TaxonDescription description = getTaxonDescription(taxon, false, true);
		description.addElement(distribution);
	}



	/**
	 * @param state
	 * @return
	 */
	Reference sourceReference = null;
	private Reference getFernsSourceReference(CentralAfricaFernsImportState state) {
//		if (sourceReference == null || true){
			Reference tmpReference = state.getConfig().getSourceReference();
			sourceReference = getReferenceService().find(tmpReference.getUuid());
//		}
		return sourceReference;
	}


	private String makeCountryBrackets(CentralAfricaFernsImportState state, String taxonNumber, Taxon taxon, TaxonNameBase nameUsedInSource, String country) {
		String[] split = (country + " ").split("\\(.*\\)");
		if (split.length == 2){
			String bracket = country.substring(split[0].length()+1, country.indexOf(")"));
			country = split[0].trim();
			makeCountries(state, taxonNumber, taxon, nameUsedInSource, bracket, null, null);
		}else if (split.length ==1){
			//do nothing
		}else{
			logger.warn("Illegal length");
		}
		return country;
	}

	private String makeCountryStatus(CentralAfricaFernsImportState state, String country, Distribution distribution) throws UndefinedTransformerMethodException {
		PresenceTerm status = null;
		String[] split = country.split(" - ");
		
		if (split.length == 2){
			country = split[0].trim();
			String statusString = split[1];
			statusString = statusString.replace(".", "");
			status = state.getTransformer().getPresenceTermByKey(statusString);
			if (status == null){
				logger.warn("No status found: "+  statusString);
			}
//			UUID uuid = null;
//			status = getPresenceTerm(state, uuid, statusString, statusString, null);
		}else if (split.length == 1){
			//nothing to do
		}else{
			logger.warn("Invalid length: " + split.length);
		}
		if (status != null){
			distribution.setStatus(status);
		}
		return country;
	}


	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(CentralAfricaFernsImportState state){
		IOValidator<CentralAfricaFernsImportState> validator = new CentralAfricaFernsTaxonImportValidator();
		return validator.validate(state);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(CentralAfricaFernsImportState state){
		return ! state.getConfig().isDoRelTaxa();
	}

	
	
//************************ OLD **********************************************************

	/**
	 * Adds the higherTaxon authors to the existingHigherTaxon authors if the higherTaxon has authors and 
	 * the existingHigherTaxon has no authors.
	 * Returns false if both taxa have authors and the authors differ from each other.
	 * @param higherTaxon
	 * @param existingHigherTaxon
	 */
	private boolean mergeAuthors_old(Taxon higherTaxon, Taxon existingHigherTaxon) {
		NonViralName existingName = CdmBase.deproxy(higherTaxon.getName(), NonViralName.class);
		NonViralName newName = CdmBase.deproxy(existingHigherTaxon.getName(), NonViralName.class);
		if (existingName == newName){
			return true;
		}
		if (! newName.hasAuthors()){
			return true;
		}
		if (! existingName.hasAuthors()){
			existingName.setCombinationAuthorTeam(newName.getCombinationAuthorTeam());
			existingName.setExCombinationAuthorTeam(newName.getExCombinationAuthorTeam());
			existingName.setBasionymAuthorTeam(newName.getBasionymAuthorTeam());
			existingName.setExBasionymAuthorTeam(newName.getExBasionymAuthorTeam());
			return true;
		}
		boolean authorsAreSame = true;
		authorsAreSame &= getNomTitleNz(existingName.getCombinationAuthorTeam()).equals(getNomTitleNz(newName.getCombinationAuthorTeam()));
		authorsAreSame &= getNomTitleNz(existingName.getExCombinationAuthorTeam()).equals(getNomTitleNz(newName.getExCombinationAuthorTeam()));
		authorsAreSame &= getNomTitleNz(existingName.getBasionymAuthorTeam()).equals(getNomTitleNz(newName.getBasionymAuthorTeam()));
		authorsAreSame &= getNomTitleNz(existingName.getExBasionymAuthorTeam()).equals(getNomTitleNz(newName.getExBasionymAuthorTeam()));
		return authorsAreSame;
		
		
	}

	/**
	 * Returns the nomenclatural title of the author. Returns empty string if author is <code>null</code> or
	 * titleCache is <code>null</code>.
	 * @param author
	 * @return
	 */
	private String getNomTitleNz(INomenclaturalAuthor author) {
		if (author != null){
			return CdmUtils.Nz(author.getNomenclaturalTitle());
		}else{
			return "";
		}
	}

	private Taxon getExistingHigherTaxon_old(Taxon child, Taxon higherTaxon) {
		int countNodes = child.getTaxonNodes().size();
		if (countNodes < 1){
			return null;
		}else if (countNodes > 1){
			throw new IllegalStateException("Multiple nodes exist for child taxon. This is an invalid state.");
		}else{
			TaxonNode childNode = child.getTaxonNodes().iterator().next();
			TaxonNode parentNode = childNode.getParent();
			if (parentNode != null){
				String existingParentTitle = parentNode.getTaxon().getName().getTitleCache();
				String newParentTitle = higherTaxon.getName().getTitleCache();
				if (existingParentTitle.equals(newParentTitle)){
					return parentNode.getTaxon();
				}
			}
			return null;
		}
	}



	/**
	 * Tests if this the child taxon already is a child of the higher taxon.
	 * @param child
	 * @param higherTaxon
	 * @return
	 */
	private boolean includedRelationshipExists_Old(Taxon child, Taxon higherTaxon) {
		int countNodes = higherTaxon.getTaxonNodes().size();
		if (countNodes < 1){
			return false;
		}else if (countNodes > 1){
			throw new IllegalStateException("Multiple nodes exist for higher taxon. This is an invalid state.");
		}else{
			TaxonNode higherNode = higherTaxon.getTaxonNodes().iterator().next();
			return childExists_old(child, higherNode);
		}
	}



	private boolean childExists_old(Taxon child, TaxonNode higherNode) {
		for (TaxonNode childNode : higherNode.getChildNodes()){
			String existingChildTitle = childNode.getTaxon().getName().getTitleCache();
			String newChildTitle = child.getName().getTitleCache();
			if (existingChildTitle.equals(newChildTitle)){
				return true;
			}
		}
		return false;
	}


}
