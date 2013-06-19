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
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelCommonNamesImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
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
		super(dbTableName, pluralString);
	}
	
	

	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT CommonNameId FROM emCommonName WHERE (1=1) ";
		if (StringUtils.isNotBlank(state.getConfig().getCommonNameFilter())){
			result += " AND " + state.getConfig().getCommonNameFilter();
		}
		
		return result;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String recordQuery = "";
		recordQuery = 
				" SELECT     cn.CommonNameId, cn.CommonName, PTaxon.RIdentifier AS taxonId, cn.PTNameFk, cn.RefFk AS refId, cn.Status, cn.RegionFks, cn.MisNameRefFk, " +
					       "               cn.NameInSourceFk, cn.Created_When, cn.Updated_When, cn.Created_Who, cn.Updated_Who, cn.Note AS Notes, languageCommonName.Language, " +
					       "               languageCommonName.LanguageOriginal, languageCommonName.ISO639_1, languageCommonName.ISO639_2,   " +
					       "               emLanguageReference.RefFk AS languageRefRefFk, emLanguageReference.ReferenceShort, emLanguageReference.ReferenceLong,  " +
					       "               emLanguageReference.LanguageFk, languageReferenceLanguage.Language AS refLanguage, languageReferenceLanguage.ISO639_2 AS refLanguageIso639_2,  "+ 
					       "               misappliedTaxon.RIdentifier AS misappliedTaxonId " +
					" FROM         PTaxon AS misappliedTaxon RIGHT OUTER JOIN " +
					    "                  emLanguage AS languageReferenceLanguage RIGHT OUTER JOIN " + 
					               "       emLanguageReference ON languageReferenceLanguage.LanguageId = emLanguageReference.LanguageFk RIGHT OUTER JOIN " +
					               "       emCommonName AS cn INNER JOIN " +
					               "       PTaxon ON cn.PTNameFk = PTaxon.PTNameFk AND cn.PTRefFk = PTaxon.PTRefFk ON  " +
					               "       emLanguageReference.ReferenceId = cn.LanguageRefFk LEFT OUTER JOIN " +
					                "      emLanguage AS languageCommonName ON cn.LanguageFk = languageCommonName.LanguageId ON misappliedTaxon.PTNameFk = cn.NameInSourceFk AND  " +
					                "      misappliedTaxon.PTRefFk = cn.MisNameRefFk " +
			" WHERE cn.CommonNameId IN (" + ID_LIST_TOKEN + ")";
		return recordQuery;
	}
	
	

	@Override
	protected void doInvoke(BerlinModelImportState state) {
		try {
			makeRegions(state);
		} catch (Exception e) {
			logger.error("Error when creating common name regions:" + e.getMessage());
			e.printStackTrace();
			state.setUnsuccessfull();
		}
		super.doInvoke(state);
		return;
	}
	
	/**
	 * @param state 
	 * 
	 */
	private void makeRegions(BerlinModelImportState state) {
		try {
			SortedSet<Integer> regionFks = new TreeSet<Integer>();
			Source source = state.getConfig().getSource();
			
			//fill set with all regionFk from emCommonName.regionFks
			getRegionFks(state, regionFks, source);
			//concat filter string
			String sqlWhere = getSqlWhere(regionFks);
			
			//get E+M - TDWG Mapping
			Map<String, String> emTdwgMap = getEmTdwgMap(source);
			//fill regionMap
			fillRegionMap(state, sqlWhere, emTdwgMap);
			
			return;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			state.setUnsuccessfull();
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			state.setUnsuccessfull();
			return;
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
				String regionFks  = rs.getString("RegionFks");
				String[] regionFkSplit = regionFks.split(",");
				
				//commonNameString
				if (CdmUtils.isBlank(commonNameString)){
					String message = "CommonName is empty or null. Do not import record for taxon " + taxonId;
					logger.warn(message);
					continue;
				}
				
				//taxon
				Taxon taxon = null;
				TaxonBase taxonBase  = taxonMap.get(String.valueOf(taxonId));
				if (taxonBase == null){
					logger.warn("Taxon (" + taxonId + ") could not be found. Common name " + commonNameString + "(" + commonNameId + ") not imported");
					continue;
				}else if (! taxonBase.isInstanceOf(Taxon.class)){
					logger.warn("taxon (" + taxonId + ") is not accepted. Can't import common name " +  commonNameId);
					continue;
				}else{
					taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				}
				
				//Language
				Language language = getAndHandleLanguage(iso6392Map, iso639_2, iso639_1, languageString, originalLanguageString, state);
				
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
							logger.warn("Area for " + regionFk + " not defined in regionMap.");
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
				
				Reference<?> reference = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(languageRefRefFk));
				String microCitation = null;
				String originalNameString = null;
				
				TaxonNameBase<?,?> nameUsedInSource = taxonNameMap.get(String.valueOf(nameInSourceFk));
				if (nameInSourceFk != null && nameUsedInSource == null){
					logger.warn("Name used in source (" + nameInSourceFk + ") was not found for common name " + commonNameId);
				}
				DescriptionElementSource source = DescriptionElementSource.NewPrimarySourceInstance(reference, microCitation, nameUsedInSource, originalNameString);
				for (CommonTaxonName commonTaxonName : commonTaxonNames){
					commonTaxonName.addSource(source);
				}
				
				
				//MisNameRef
				if (misNameRefFk != null){
					//Taxon misappliedName = getMisappliedName(biblioRefMap, nomRefMap, misNameRefFk, taxon);
					Taxon misappliedNameTaxon = null;
					if (misappliedTaxonId != null){
						TaxonBase misTaxonBase =  taxonMap.get(String.valueOf(misappliedTaxonId));
						if (misTaxonBase == null){
							logger.warn("MisappliedName not found for misappliedTaxonId " + misappliedTaxonId + "; commonNameId: " + commonNameId);
						}else if (misTaxonBase.isInstanceOf(Taxon.class)){
							misappliedNameTaxon = CdmBase.deproxy(misTaxonBase, Taxon.class);
						}else{
							logger.warn("Misapplied name taxon is not of type Taxon but " + misTaxonBase.getClass().getSimpleName());
						}
					}else{
						
						Reference<?> sec = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(misNameRefFk));
						if (nameUsedInSource == null || sec == null){
							logger.warn("Taxon name or misapplied name reference is null for common name " + commonNameId);
						}else{
							misappliedNameTaxon = Taxon.NewInstance(nameUsedInSource, sec);
							MarkerType misCommonNameMarker = getMarkerType(state, BerlinModelTransformer.uuidMisappliedCommonName,"Misapplied Common Name in Berlin Model", "Misapplied taxon was automatically created by Berlin Model import for a common name with a misapplied name reference", "MCN");
							Marker marker = Marker.NewInstance(misCommonNameMarker, true);
							misappliedNameTaxon.addMarker(marker);
							taxaToSave.add(misappliedNameTaxon);
							logger.warn("Misapplied name taxon could not be found in database but misapplied name reference exists for common name. " +
									"New misapplied name for misapplied reference common name was added. CommonNameId: " + commonNameId);
						}
					}
					if (misappliedNameTaxon != null){
						
						if (! taxon.getMisappliedNames().contains(misappliedNameTaxon)){
							taxon.addMisappliedName(misappliedNameTaxon,state.getTransactionalSourceReference(), null);
							logger.warn("Misapplied name for common name was not found related to the accepted taxon. Created new relationship. CommonNameId: " + commonNameId);
						}
						
						TaxonDescription misappliedNameDescription = getDescription(misappliedNameTaxon);
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
					logger.warn("Reference is null (" + languageRefRefFk + ") but refLanguage (" + CdmUtils.Nz(refLanguage) + ") or iso639_2 (" + CdmUtils.Nz(refLanguageIso639_2) + ") was not null for common name ("+ commonNameId +")");
				}
				
				//status
				if (CdmUtils.isNotEmpty(status)){
					AnnotationType statusAnnotationType = getAnnotationType( state, STATUS_ANNOTATION_UUID, "status","The status of this object","status", null);
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
	private Taxon getMisappliedName(Map<String, Reference> biblioRefMap, Map<String, Reference> nomRefMap, Object misNameRefFk, Taxon taxon) {
		Taxon misappliedTaxon = null;
		Reference<?> misNameRef = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, String.valueOf(misNameRefFk));
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
	 * @param state 
	 * @return
	 */
	private Language getAndHandleLanguage(Map<String, Language> iso639Map,	String iso639_2, String iso639_1, String languageString, String originalLanguageString, BerlinModelImportState state) {
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
		} else if ("Majorcan".equalsIgnoreCase(languageString)){
			language = getLanguage(state, BerlinModelTransformer.uuidLangMajorcan, "Majorcan", "Majorcan (original 'mallorqu\u00EDn')", null);
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
		if (CdmUtils.isBlank(originalLanguageString)){
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
	 * Fills the regionFks with all regionFks from emCommonName. Comma separated regionFks will be split.
	 * @param state
	 * @param regionFks
	 * @param source
	 * @return
	 * @throws SQLException
	 * 
	 */
	private void getRegionFks(BerlinModelImportState state, SortedSet<Integer> regionFks, Source source) throws SQLException {
		String sql = " SELECT DISTINCT RegionFks FROM emCommonName";
		if (state.getConfig().getCommonNameFilter() != null){
			sql += " WHERE " + state.getConfig().getCommonNameFilter(); 
		}
		
		ResultSet rs = source.getResultSet(sql);
		while (rs.next()){
			String strRegionFks = rs.getString("RegionFks"); 
			if (StringUtils.isBlank(strRegionFks)){
				continue;
			}
			
			String[] regionFkArray = strRegionFks.split(",");
			for (String regionFk: regionFkArray){
				regionFk = regionFk.trim();
				if (! StringUtils.isNumeric(regionFk) || "".equals(regionFk)  ){
					state.setUnsuccessfull();
					logger.warn("RegionFk is not numeric: " + regionFk +  " ( part of " + strRegionFks + ")");
				}else{
					regionFks.add(Integer.valueOf(regionFk));
				}
			}
		}
		return;
	}



	/**
	 * Fills the {@link #regionMap} by all emLanguageRegion regions defined in the sql filter.
	 * {@link #regionMap} maps emLanguageRegion.RegionId to named areas.
	 * @param state
	 * @param sqlWhere
	 * @param emTdwgMap
	 * @throws SQLException
	 */
	private void fillRegionMap(BerlinModelImportState state, String sqlWhere,
			Map<String, String> emTdwgMap) throws SQLException {
		Source source = state.getConfig().getSource();
		String sql;
		ResultSet rs;
		sql = " SELECT RegionId, Region FROM emLanguageRegion WHERE RegionId IN ("+ sqlWhere+ ") ";
		rs = source.getResultSet(sql);
		while (rs.next()){
			Object regionId = rs.getObject("RegionId");
			String region = rs.getString("Region");
			String[] splitRegion = region.split("-");
			if (splitRegion.length <= 1){
				NamedArea newArea = getNamedArea(state, null, region, "Language region '" + region + "'", null, null, null);
//				getTermService().save(newArea);
				regionMap.put(String.valueOf(regionId), newArea);
				logger.warn("Found new area: " +  region);
			}else if (splitRegion.length == 2){
				String emCode = splitRegion[1].trim();
				String tdwgCode = emTdwgMap.get(emCode);
				if (StringUtils.isNotBlank(tdwgCode) ){
					NamedArea tdwgArea = getNamedArea(state, tdwgCode);
					regionMap.put(String.valueOf(regionId), tdwgArea);
				}else {
					NamedArea area = getOtherAreas(state, emCode, tdwgCode);
					if (area != null){
						regionMap.put(String.valueOf(regionId), area);
					}else{
						logger.warn("emCode did not map to valid tdwgCode: " +  CdmUtils.Nz(emCode) + "->" + CdmUtils.Nz(tdwgCode));
					}
				}
			}
		}
	}


	/**
	 * Returns the are for a given TDWG code. See {@link #getEmTdwgMap(Source)} for exceptions from
	 * the TDWG code
	 * @param state 
	 * @param tdwgCode
	 */
	private NamedArea getNamedArea(BerlinModelImportState state, String tdwgCode) {
		NamedArea area;
		if (tdwgCode.equalsIgnoreCase("Ab")){
			area = getNamedArea(state, BerlinModelTransformer.uuidAzerbaijanNakhichevan, "Azerbaijan & Nakhichevan", "Azerbaijan (including Nakhichevan)",  "Ab", null, null);
			getTermService().saveOrUpdate(area);
		}else if (tdwgCode.equalsIgnoreCase("Uk")){
			area = getNamedArea(state, BerlinModelTransformer.uuidUkraineAndCrimea , "Ukraine & Crimea", "Ukraine (including Crimea)", "Uk", null, null);
			getTermService().saveOrUpdate(area);
		}else if (tdwgCode.equalsIgnoreCase("Rf")){
			area = WaterbodyOrCountry.RUSSIANFEDERATION();
		}else if (tdwgCode.equalsIgnoreCase("Gg")){
			area = WaterbodyOrCountry.GEORGIA();
		}else{
			area = TdwgAreaProvider.getAreaByTdwgAbbreviation(tdwgCode);
		}
		if (area == null){
			logger.warn("Area is null for " + tdwgCode);
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
	 * Returns a map which is filled by the emCode->TdwgCode mapping defined in emArea.
	 * Some exceptions are defined for emCode 'Ab','Rf','Uk' and some additional mapping is added 
	 * for 'Ab / Ab(A)', 'Ga / Ga(F)', 'It / It(I)', 'Ar / Ar(A)','Hs / Hs(S)'
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
				if (emCode.equalsIgnoreCase("Ab") || emCode.equalsIgnoreCase("Rf")|| 
						emCode.equalsIgnoreCase("Uk") || emCode.equalsIgnoreCase("Gg")){
					emTdwgMap.put(emCode, emCode);
				}else if (StringUtils.isNotBlank(TDWGCode)){
					emTdwgMap.put(emCode, TDWGCode.trim());
				}
			}
		}
		emTdwgMap.put("Ab / Ab(A)", "Ab");
		emTdwgMap.put("Ga / Ga(F)", "FRA-FR");
		emTdwgMap.put("It / It(I)", "ITA");
		emTdwgMap.put("Uk / Uk(U)", "Uk");
		emTdwgMap.put("Ar / Ar(A)", "TCS-AR");
		emTdwgMap.put("Hs / Hs(S)", "SPA-SP");
		
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
		Class<?> cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		int pos = -1;
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				pos = 0;
				handleForeignKey(rs, taxonIdSet, "taxonId");
				pos = 1;
				handleForeignKey(rs, taxonIdSet, "misappliedTaxonId");
				pos = 2;
				handleForeignKey(rs, referenceIdSet, "refId");
				pos = 3;
				handleForeignKey(rs, referenceIdSet, "languageRefRefFk");
				pos = 4;
				handleForeignKey(rs, nameIdSet, "NameInSourceFk");
				pos = 5;
				handleForeignKey(rs, nameIdSet, "PTNameFk");
				pos = 6;
				handleForeignKey(rs, referenceIdSet, "MisNameRefFk");
				pos = -2;
				
			}
			
			pos = 7;
			
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, TaxonNameBase<?,?>> nameMap = (Map<String, TaxonNameBase<?,?>>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nameMap);

			pos = 8;
			
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase<?>> taxonMap = (Map<String, TaxonBase<?>>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

			pos = 9;
			
			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> nomReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			pos = 10;
			
			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference<?>> biblioReferenceMap = (Map<String, Reference<?>>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);

			pos = 11;
			
		} catch (SQLException e) {
			throw new RuntimeException("pos: " +pos, e);
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoCommonNames();
	}

}
