/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelCommonNamesImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * 
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelCommonNamesImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelCommonNamesImport.class);

	public static final UUID REFERENCE_LANGUAGE_ISO639_2_UUID = UUID.fromString("40c4f8dd-3d9c-44a4-b77a-76e137a89a5f");
	public static final UUID REFERENCE_LANGUAGE_STRING_UUID = UUID.fromString("2a1b678f-c27d-48c1-b43e-98fd0d426305");
	public static final UUID STATUS_ANNOTATION_UUID = UUID.fromString("e3f7b80a-1286-458d-812c-5e818f731968");
	
	public static final String NAMESPACE = "common name";
	
	
	private static final String pluralString = "common names";
	private static final String dbTableName = "emCommonName";


	//map that stores the regions (named areas) and makes them accessible via the regionFk
	private Map<String, NamedArea> regionMap = new HashMap<String, NamedArea>();

	

	public BerlinModelCommonNamesImport(){
		super();
	}
	
	

	@Override
	protected String getIdQuery() {
		String result = " SELECT CommonNameId FROM emCommonName ";
		return result;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String recordQuery = "";
		recordQuery = 
			" SELECT emCommonName.CommonNameId, emCommonName.CommonName, PTaxon.RIdentifier AS taxonId, emCommonName.PTNameFk, emCommonName.RefFk AS refId, emCommonName.Status, " + 
				" emCommonName.RegionFks, emCommonName.MisNameRefFk, emCommonName.NameInSourceFk , emCommonName.Created_When, emCommonName.Updated_When, emCommonName.Created_Who, emCommonName.Updated_Who, emCommonName.Note as Notes," + 
        		" regionLanguage.Language AS regionLanguage, languageCommonName.Language, languageCommonName.LanguageOriginal, languageCommonName.ISO639_1, languageCommonName.ISO639_2, " + 
        		" emLanguageRegion.Region, emLanguageReference.RefFk as languageRefRefFk, emLanguageReference.ReferenceShort, " + 
        		" emLanguageReference.ReferenceLong, emLanguageReference.LanguageFk, languageReferenceLanguage.Language AS refLanguage, " +
        		" languageReferenceLanguage.ISO639_2 AS refLanguageIso639_2, regionLanguage.ISO639_2 AS regionLanguageIso, " +
        		" misappliedTaxon.RIdentifier AS misappliedTaxonId " + 
        	" FROM emLanguage as regionLanguage RIGHT OUTER JOIN " + 
        		" emLanguageRegion ON regionLanguage.LanguageId = emLanguageRegion.LanguageFk RIGHT OUTER JOIN " +
        		" emLanguage AS languageReferenceLanguage RIGHT OUTER JOIN " +
        		" emLanguageReference ON languageReferenceLanguage.LanguageId = emLanguageReference.LanguageFk RIGHT OUTER JOIN " +
        		" emCommonName INNER JOIN " +
        		" PTaxon ON emCommonName.PTNameFk = PTaxon.PTNameFk AND emCommonName.PTRefFk = PTaxon.PTRefFk ON " + 
        		" emLanguageReference.ReferenceId = emCommonName.LanguageRefFk LEFT OUTER JOIN " +
        		" emLanguage AS languageCommonName ON emCommonName.LanguageFk = languageCommonName.LanguageId ON " + 
        		" emLanguageRegion.RegionId = emCommonName.RegionFks LEFT OUTER JOIN " +
        		" PTaxon as misappliedTaxon ON emCommonName.PTNameFk = misappliedTaxon.PTNameFk AND emCommonName.MisNameRefFk = misappliedTaxon.PTRefFk " + 
			" WHERE emCommonName.CommonNameId IN (" + ID_LIST_TOKEN + ")";
		return recordQuery;
	}
	
	

	@Override
	protected boolean doInvoke(BerlinModelImportState state) {
		boolean result = true;
		try {
			result &= makeRegions(state);
		} catch (Exception e) {
			logger.error("Error when creating common name regions:" + e.getMessage());
			result = false;
		}
		result &= super.doInvoke(state);
		return result;
	}
	
	/**
	 * @param state 
	 * 
	 */
	private boolean makeRegions(BerlinModelImportState state) {
		boolean result = true;
		try {
			SortedSet<Integer> regionFks = new TreeSet<Integer>();
			Source source = state.getConfig().getSource();
			
			result = getRegionFks(result, regionFks, source);
			//concat filter string
			String sqlWhere = getSqlWhere(regionFks);
			
			//get E+M - TDWG Mapping
			Map<String, String> emTdwgMap = getEmTdwgMap(source);
			//fill regionMap
			fillRegionMap(source, sqlWhere, emTdwgMap);
			
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state)  {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		Map<String, Taxon> taxonMap = (Map<String, Taxon>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
		Map<String, TaxonNameBase> taxonNameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);
		
		Map<String, Reference> biblioRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);
		
		Map<String, Language> iso6392Map = new HashMap<String, Language>();
		
	//	logger.warn("MisappliedNameRefFk  not yet implemented for Common Names");
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){

				//create TaxonName element
				Object commonNameId = rs.getObject("CommonNameId");
				int taxonId = rs.getInt("taxonId");
				Object refId = rs.getObject("refId");
				Object ptNameFk = rs.getObject("PTNameFk");
				String commonNameString = rs.getString("CommonName");
				String iso639_2 = rs.getString("ISO639_2");
				String iso639_1 = rs.getString("ISO639_1");
				String languageString = rs.getString("Language");
				String originalLanguageString = rs.getString("LanguageOriginal");
				Object misNameRefFk = rs.getObject("MisNameRefFk");
				Object languageRefRefFk = rs.getObject("languageRefRefFk");
				String refLanguage = rs.getString("refLanguage");
				String refLanguageIso639_2 = rs.getString("refLanguageIso639_2");
				String status = rs.getString("Status");
				Object nameInSourceFk = rs.getObject("NameInSourceFk");
				Object misappliedTaxonId = rs.getObject("misappliedTaxonId");
				
				//regions
				String region = rs.getString("Region");
				String regionFks  = rs.getString("RegionFks");
				String[] regionFkSplit = regionFks.split(",");
				
				//commonNameString
				if (CdmUtils.isEmpty(commonNameString)){
					String message = "CommonName is empty or null. Do not import record for taxon " + taxonId;
					logger.warn(message);
					continue;
				}
				
				//taxon
				Taxon taxon = null;
				TaxonBase taxonBase = null;
				taxonBase  = taxonMap.get(String.valueOf(taxonId));
				if (taxonBase == null){
					logger.warn("Taxon (" + taxonId + ") could not be found. Common name " + commonNameString + " not imported");
					continue;
				}else if (! taxonBase.isInstanceOf(Taxon.class)){
					logger.warn("taxon (" + taxonId + ") is not accepted. Can't import common name " +  commonNameId);
					continue;
				}else{
					taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				}
				
				//Language
				Language language = getAndHandleLanguage(iso6392Map, iso639_2, iso639_1, languageString, originalLanguageString);
				
				//CommonTaxonName
				List<CommonTaxonName> commonTaxonNames = new ArrayList<CommonTaxonName>();
				for (String regionFk : regionFkSplit){ //
					CommonTaxonName commonTaxonName;
					if (commonTaxonNames.size() == 0){
						commonTaxonName = CommonTaxonName.NewInstance(commonNameString, language);
					}else{
						commonTaxonName = (CommonTaxonName)commonTaxonNames.get(0).clone();
					}
					commonTaxonNames.add(commonTaxonName);
					regionFk = regionFk.trim();
					NamedArea area = regionMap.get(regionFk);
					if (area == null){
						if (regionFkSplit.length > 1 && StringUtils.isNotBlank(regionFk)){
							logger.warn("Area for " + regionFk + " not defined.");
						}else{
							//no region is defined
						}
					}else{
						commonTaxonName.setArea(area);
						TaxonDescription description = getDescription(taxon);
						description.addElement(commonTaxonName);
					}
				}
					
				//Reference/Source
				String strRefId = String.valueOf(refId);
				String languageRefFk = String.valueOf(languageRefRefFk);
				if (! CdmUtils.nullSafeEqual(strRefId, languageRefFk)){
					//use strRefId if languageRefFk is null
					if (languageRefRefFk == null){
						languageRefFk = strRefId;
					}else{
						logger.warn("CommonName.RefFk (" + CdmUtils.Nz(strRefId) + ") and LanguageReference.RefFk " + CdmUtils.Nz(languageRefFk) + " are not equal. I will import only languageReference.RefFk");
					}
				}
				
						
				Reference reference = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(languageRefRefFk));
				String microCitation = null;
				String originalNameString = null;
				
				TaxonNameBase nameUsedInSource = taxonNameMap.get(String.valueOf(nameInSourceFk));
				if (nameInSourceFk != null && nameUsedInSource == null){
					logger.warn("Name used in source (" + nameInSourceFk + ") was not found");
				}
				DescriptionElementSource source = DescriptionElementSource.NewInstance(reference, microCitation, nameUsedInSource, originalNameString);
				for (CommonTaxonName commonTaxonName : commonTaxonNames){
					commonTaxonName.addSource(source);
				}
				
				//MisNameRef
				if (misNameRefFk != null){
					//Taxon misappliedName = getMisappliedName(biblioRefMap, nomRefMap, misNameRefFk, taxon);
					Taxon misappliedName = null;
					if (misappliedTaxonId != null){
						misappliedName = taxonMap.get(String.valueOf(misappliedTaxonId));
					}else{
						TaxonNameBase taxonName = taxonNameMap.get(String.valueOf(ptNameFk));
						Reference sec = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(misNameRefFk));
						if (taxonName == null || sec == null){
							logger.info("Taxon name or misapplied name reference is null for common name " + commonNameId);
						}else{
							misappliedName = Taxon.NewInstance(taxonName, sec);
							taxaToSave.add(misappliedName);
						}
					}
					if (misappliedName != null){
						taxon.addMisappliedName(misappliedName, config.getSourceReference(), null);
						TaxonDescription misappliedNameDescription = getDescription(misappliedName);
						for (CommonTaxonName commonTaxonName : commonTaxonNames){
							CommonTaxonName commonNameClone = (CommonTaxonName)commonTaxonName.clone();
							misappliedNameDescription.addElement(commonNameClone);
						}	
					}else{
						logger.warn("Misapplied name is null for common name " + commonNameId);
					}
					
				}
				
				
				//reference extensions
				if (reference != null){
					if (CdmUtils.isNotEmpty(refLanguage)){
						ExtensionType refLanguageExtensionType = getExtensionType( state, REFERENCE_LANGUAGE_STRING_UUID, "reference language","The language of the reference","ref. lang.");
						Extension.NewInstance(reference, refLanguage, refLanguageExtensionType);
					}
					
					if (CdmUtils.isNotEmpty(refLanguageIso639_2)){
						ExtensionType refLanguageIsoExtensionType = getExtensionType( state, REFERENCE_LANGUAGE_ISO639_2_UUID, "reference language iso 639-2","The iso 639-2 code of the references language","ref. lang. 639-2");
						Extension.NewInstance(reference, refLanguageIso639_2, refLanguageIsoExtensionType);
					}
				}else if (CdmUtils.isNotEmpty(refLanguage) || CdmUtils.isNotEmpty(refLanguageIso639_2)){
					logger.warn("Reference is null (" + languageRefRefFk + ") but refLanguage (" + CdmUtils.Nz(refLanguage) + ") or iso639_2 (" + CdmUtils.Nz(refLanguageIso639_2) + ") was not null");
				}
				
				//status
				if (CdmUtils.isNotEmpty(status)){
					AnnotationType statusAnnotationType = getAnnotationType( state, STATUS_ANNOTATION_UUID, "status","The status of this object","status");
					Annotation annotation = Annotation.NewInstance(status, statusAnnotationType, Language.DEFAULT());
					for (CommonTaxonName commonTaxonName : commonTaxonNames){
						commonTaxonName.addAnnotation(annotation);
					}
					
				}
				
				//Notes
				for (CommonTaxonName commonTaxonName : commonTaxonNames){
					doIdCreatedUpdatedNotes(state, commonTaxonName, rs, String.valueOf(commonNameId), NAMESPACE);
				}
				partitioner.startDoSave();
				taxaToSave.add(taxon);

			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		} catch (ClassCastException e) {
			e.printStackTrace();
		} 	
			
		//	logger.info( i + " names handled");
		getTaxonService().save(taxaToSave);
		return success;

	}



	/**
	 * Not used anymore. Use MisappliedName RIdentifier instead
	 * @param biblioRefMap
	 * @param nomRefMap
	 * @param misNameRefFk
	 * @param taxon
	 */
	private boolean isFirstMisappliedName = true;
	private Taxon getMisappliedName(Map<String, Reference> biblioRefMap, Map<String, Reference> nomRefMap, 
			Object misNameRefFk, Taxon taxon) {
		Taxon misappliedTaxon = null;
		Reference misNameRef = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(misNameRefFk));
		misappliedTaxon = Taxon.NewInstance(taxon.getName(), misNameRef);
		Set<String> includeProperty = new HashSet<String>();
		try {
//			//IMatchStrategy matchStrategy = DefaultMatchStrategy.NewInstance(TaxonBase.class);
//			//List<TaxonBase> misappliedList1 = getCommonService().findMatching(misappliedTaxon, matchStrategy);
			List<TaxonBase> misappliedList = getTaxonService().list(misappliedTaxon, includeProperty, null, null, null, null);
			if (misappliedList.size() > 0){
				misappliedTaxon = CdmBase.deproxy(misappliedList.get(0), Taxon.class);
			}
		} catch (ClassCastException e) {
			logger.error(e.getMessage());
			if (isFirstMisappliedName){
				e.printStackTrace();
				isFirstMisappliedName = false;
			}
		}
		return misappliedTaxon;
	}



	/**
	 * @param iso6392Map
	 * @param iso639_2
	 * @param languageString
	 * @param originalLanguageString
	 * @return
	 */
	private Language getAndHandleLanguage(Map<String, Language> iso639Map,	String iso639_2, String iso639_1, String languageString, String originalLanguageString) {
		Language language;
		if (CdmUtils.isNotEmpty(iso639_2)|| CdmUtils.isNotEmpty(iso639_1)  ){
			//TODO test performance, implement in state
			language = getLanguageFromIsoMap(iso639Map, iso639_2, iso639_1);
			
			if (language == null){
				language = getTermService().getLanguageByIso(iso639_2);
				iso639Map.put(iso639_2, language);
				if (language == null){
					language = getTermService().getLanguageByIso(iso639_1);
					iso639Map.put(iso639_1, language);
				}
				if (language == null){
					logger.warn("Language for code ISO693-2 '" + iso639_2 + "' and ISO693-1 '" + iso639_1 + "' was not found");
				}
			}
		} else if ("unknown".equals(languageString)){
			language = Language.UNKNOWN_LANGUAGE();
		}else{
			logger.warn("language ISO 639_1 and ISO 639_2 were empty for " + languageString);
			language = null;
		}
		addOriginalLanguage(language, originalLanguageString);
		return language;
	}


	/**
	 * @param iso639Map
	 * @param iso639_2
	 * @param iso639_1
	 * @return
	 */
	private Language getLanguageFromIsoMap(Map<String, Language> iso639Map,	String iso639_2, String iso639_1) {
		Language language;
		language = iso639Map.get(iso639_2);
		if (language == null){
			language = iso639Map.get(iso639_1);
		}
		return language;
	}

	/**
	 * @param language
	 * @param originalLanguageString
	 */
	private void addOriginalLanguage(Language language,	String originalLanguageString) {
		if (CdmUtils.isEmpty(originalLanguageString)){
			return;
		}else if (language == null){
			logger.warn("Language could not be defined, but originalLanguageString exists: " + originalLanguageString);
		}else {
			Representation representation = language.getRepresentation(language);
			if (representation == null){
				language.addRepresentation(Representation.NewInstance(originalLanguageString, originalLanguageString, originalLanguageString, language));
				getTermService().saveOrUpdate(language);
			}
		}
		
	}
	


	/**
	 * @param result
	 * @param regionFks
	 * @param source
	 * @return
	 * @throws SQLException
	 */
	private boolean getRegionFks(boolean result, SortedSet<Integer> regionFks,
			Source source) throws SQLException {
		String sql = " SELECT DISTINCT RegionFks FROM emCommonName";
		ResultSet rs = source.getResultSet(sql);
		while (rs.next()){
			String strRegionFks = rs.getString("RegionFks"); 
			String[] regionFkArray = strRegionFks.split(",");
			for (String regionFk: regionFkArray){
				regionFk = regionFk.trim();
				if (! StringUtils.isNumeric(regionFk) || "".equals(regionFk)  ){
					result = false;
					logger.warn("RegionFk is not numeric: " + regionFk);
				}else{
					regionFks.add(Integer.valueOf(regionFk));
				}
			}
		}
		return result;
	}



	/**
	 * @param source
	 * @param sqlWhere
	 * @param emTdwgMap
	 * @throws SQLException
	 */
	private void fillRegionMap(Source source, String sqlWhere,
			Map<String, String> emTdwgMap) throws SQLException {
		String sql;
		ResultSet rs;
		sql = " SELECT RegionId, Region FROM emLanguageRegion WHERE RegionId IN ("+ sqlWhere+ ") ";
		rs = source.getResultSet(sql);
		while (rs.next()){
			Object regionId = rs.getObject("RegionId");
			String region = rs.getString("Region");
			String[] splitRegion = region.split("-");
			if (splitRegion.length <= 1){
				NamedArea newArea = NamedArea.NewInstance(region, region, null);
				getTermService().save(newArea);
				regionMap.put(String.valueOf(regionId), newArea);
				logger.warn("Found new area: " +  region);
			}else if (splitRegion.length == 2){
				String emCode = splitRegion[1].trim();
				String tdwgCode = emTdwgMap.get(emCode);
				if (StringUtils.isNotBlank(tdwgCode) ){
					NamedArea tdwgArea = getNamedArea(tdwgCode);
					regionMap.put(String.valueOf(regionId), tdwgArea);
				}else{
					logger.warn("emCode did not map to valid tdwgCode: " +  CdmUtils.Nz(emCode) + "->" + CdmUtils.Nz(tdwgCode));
				}
			}
		}
	}


	/**
	 * @param tdwgCode
	 */
	private NamedArea getNamedArea(String tdwgCode) {
		NamedArea area;
		if (tdwgCode.equalsIgnoreCase("Ab")){
			area = NamedArea.NewInstance("Azerbaijan (including Nakhichevan)", "Azerbaijan & Nakhichevan", "Ab");
			getTermService().save(area);
		}else if (tdwgCode.equalsIgnoreCase("Rf")){
			area = NamedArea.NewInstance("The Russian Federation", "The Russian Federation", "Rf");
			getTermService().save(area);
		}else if (tdwgCode.equalsIgnoreCase("Uk")){
			area = NamedArea.NewInstance("Ukraine (including Crimea)", "Ukraine & Crimea", "Uk");
			getTermService().save(area);
		}else{
			area = TdwgArea.getAreaByTdwgAbbreviation(tdwgCode);
		}
		return area;
	}



	/**
	 * @param regionFks
	 * @return
	 */
	private String getSqlWhere(SortedSet<Integer> regionFks) {
		String sqlWhere = "";
		for (Integer regionFk : regionFks){
			sqlWhere += regionFk + ","; 
		}
		sqlWhere = sqlWhere.substring(0, sqlWhere.length()-1);
		return sqlWhere;
	}



	/**
	 * @param source
	 * @throws SQLException
	 */
	private Map<String, String> getEmTdwgMap(Source source) throws SQLException {
		String sql;
		ResultSet rs;
		Map<String, String> emTdwgMap = new HashMap<String, String>();
		sql = " SELECT EmCode, TDWGCode FROM emArea ";
		rs = source.getResultSet(sql);
		while (rs.next()){
			String emCode = rs.getString("EMCode");
			String TDWGCode = rs.getString("TDWGCode");
			if (StringUtils.isNotBlank(emCode) ){
				emCode = emCode.trim();
				if (emCode.equalsIgnoreCase("Ab") || emCode.equalsIgnoreCase("Rf")|| emCode.equalsIgnoreCase("Uk") ){
					emTdwgMap.put(emCode, emCode);
				}else if (StringUtils.isNotBlank(TDWGCode)){
					emTdwgMap.put(emCode, TDWGCode.trim());
				}
			}
		}
		return emTdwgMap;
	}




	/**
	 * Returns the first non-image gallery description. Creates a new one if no description exists.
	 * @param taxon
	 * @return
	 */
	private TaxonDescription getDescription(Taxon taxon) {
		TaxonDescription result = null;
		for (TaxonDescription taxonDescription : taxon.getDescriptions()){
			if (! taxonDescription.isImageGallery()){
				result = taxonDescription;
			}
		}
		if (result == null){
			result = TaxonDescription.NewInstance(taxon);
		}
		return result;
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
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxonId");
				handleForeignKey(rs, taxonIdSet, "misappliedTaxonId");
				handleForeignKey(rs, referenceIdSet, "refId");
				handleForeignKey(rs, referenceIdSet, "languageRefRefFk");
				handleForeignKey(rs, nameIdSet, "NameInSourceFk");
				handleForeignKey(rs, nameIdSet, "PTNameFk");
				handleForeignKey(rs, referenceIdSet, "MisNameRefFk");
			}
			
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nameMap);

			//name map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = Taxon.class;
			idSet = taxonIdSet;
			Map<String, TaxonNameBase> taxonMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> nomReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> biblioReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException nep){
			logger.error("NullPointerException in getRelatedObjectsForPartition()");
		}
		return result;
	}
		


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelCommonNamesImportValidator();
		return validator.validate(state);
	}
						
						
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}
							
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoCommonNames();
	}

}
