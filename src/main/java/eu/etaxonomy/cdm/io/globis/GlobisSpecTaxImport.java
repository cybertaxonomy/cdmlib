/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.globis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.globis.validation.GlobisSpecTaxaImportValidator;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 20.02.2010
 */
@Component
public class GlobisSpecTaxImport  extends GlobisImportBase<Reference<?>> implements IMappingImport<Reference<?>, GlobisImportState>{
	private static final Logger logger = Logger.getLogger(GlobisSpecTaxImport.class);
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private static final String dbTableName = "specTax";
	private static final Class<?> cdmTargetClass = Reference.class;
	
	private static UUID uuidCitedTypeLocality = UUID.fromString("ca431e0a-84ec-4828-935f-df4c8f5cf880");
	private static UUID uuidCitedTypeMaterial = UUID.fromString("8395021a-e596-4a55-9794-8c03aaad9e16");

	public GlobisSpecTaxImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}

	@Override
	protected String getIdQuery() {
		String strRecordQuery = 
			" SELECT specTaxId " + 
			" FROM " + dbTableName; 
		return strRecordQuery;	
	}

	@Override
	protected String getRecordQuery(GlobisImportConfigurator config) {
		String strRecordQuery = 
			" SELECT t.*, t.DateCreated as Created_When, t.CreatedBy as Created_Who," +
			"        t.ModifiedBy as Updated_who, t.DateModified as Updated_When, t.SpecRemarks as Notes " + 
			" FROM " + getTableName() + " t " +
			" WHERE ( t.specTaxId IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}
	

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, GlobisImportState state) {
		boolean success = true;
		
		Set<TaxonBase> objectsToSave = new HashSet<TaxonBase>();
		Set<TaxonNameBase> namesToSave = new HashSet<TaxonNameBase>();
		
		Map<String, Taxon> taxonMap = (Map<String, Taxon>) partitioner.getObjectMap(TAXON_NAMESPACE);
		Map<String, Reference> referenceMap = (Map<String, Reference>) partitioner.getObjectMap(REFERENCE_NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();
		
		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
        		Integer specTaxId = rs.getInt("SpecTaxId");
        		Integer acceptedTaxonId = nullSafeInt(rs, "SpecCurrspecID");
        		String specSystaxRank = rs.getString("SpecSystaxRank");
        		
        		//ignore: CountryDummy, currentSpecies, DepositoryDisplay, DepositoryDummy, ReferenceDisplay, 
        		//        SpecDescriptionImageFile, all *Valid*
        		
				try {
					
					//source ref
					Reference<?> sourceRef = state.getTransactionalSourceReference();
				
					Taxon acceptedTaxon =  taxonMap.get(String.valueOf(acceptedTaxonId));
					TaxonBase<?> thisTaxon = null;
					
					ZoologicalName name = null;
					if (isBlank(specSystaxRank) ){
						name = makeName(state, rs, specTaxId);
					}else if (specSystaxRank.equals("synonym")){
						Synonym synonym = getSynonym(state, rs, specTaxId);
						if (acceptedTaxon == null){
							if (acceptedTaxonId == null){
								logger.warn("Synonym has no accepted taxon defined. SpecTaxId: "+ specTaxId);
							}else{
								logger.warn("Accepted taxon (" + acceptedTaxonId + ") not found for synonym "+ specTaxId);
							}
						}else{
							acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
							thisTaxon = synonym;
						}
					}else if (specSystaxRank.equals("species")){
						validateAcceptedTaxon(acceptedTaxon, rs, specTaxId, acceptedTaxonId);
						thisTaxon = acceptedTaxon;
					}else{
						logger.warn(String.format("Unhandled specSystaxRank %s in specTaxId %d", specSystaxRank, specTaxId));
						name = makeName(state, rs, specTaxId);
					}
					
					if (thisTaxon != null){
						name = CdmBase.deproxy(thisTaxon.getName(), ZoologicalName.class);
					}else{
						if (name == null){
							name = makeName(state, rs, specTaxId);
						}
						
						thisTaxon = Taxon.NewInstance(name, sourceRef);
						objectsToSave.add(thisTaxon);
					}
					if (name == null){
						throw new RuntimeException("Name is still null");
					}
					
					handleNomRef(state, referenceMap, rs, name, specTaxId);
				
					handleTypeInformation(state,rs, name, specTaxId);
				
				
//					this.doIdCreatedUpdatedNotes(state, ref, rs, refId, REFERENCE_NAMESPACE);
				
					if (acceptedTaxon != null){
						objectsToSave.add(acceptedTaxon); 
					}
					
					//makeMarker1(state, rs, name);   //ignore!
					
					//make not available
					makeNotAvailable(state, rs, name, specTaxId);
					
					//maken invalid
					//TODO 
					
					//SpecCitedTypeLocality
					String citedTypeLocality = rs.getString("SpecCitedTypeLocality");
					if (isNotBlank(citedTypeLocality)){
//						ExtensionType exTypeCitedTypeLoc = getExtensionType(state, uuidCitedTypeLocality, "Type locality as cited in original description", "Type locality as cited in original description", null, ExtensionType.DOI().getVocabulary());
//						name.addExtension(citedTypeLocality, exTypeCitedTypeLoc);
						addNameDescription(state, name, uuidCitedTypeLocality, citedTypeLocality, "Type locality as cited in original description");
					}

					//SpecCitedTypeMaterial
					String citedTypeMaterial = rs.getString("SpecCitedTypeMaterial");
					if (isNotBlank(citedTypeMaterial)){
						ExtensionType exTypeCitedTypeLoc = getExtensionType(state, uuidCitedTypeMaterial, "Type material as cited in original description", "Type material as cited in original description", null, ExtensionType.DOI().getVocabulary());
						name.addExtension(citedTypeLocality, exTypeCitedTypeLoc);
					}

					name.addSource(OriginalSourceType.Import, String.valueOf(specTaxId), SPEC_TAX_NAMESPACE, state.getTransactionalSourceReference(), null);
					
					namesToSave.add(name);
					

				} catch (Exception e) {
					logger.warn("Exception in specTax: SpecTaxId " + specTaxId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
			logger.warn(pluralString + " to save: " + objectsToSave.size());
			getTaxonService().save(objectsToSave);	
			getNameService().save(namesToSave);
			
			return success;
		} catch (Exception e) {
			logger.error("Exception: " +  e);
			return false;
		}
	}


	private void makeNotAvailable(GlobisImportState state, ResultSet rs, ZoologicalName name, int id) throws SQLException {
		String notAvailableStr = rs.getString("SpecNotAvailable");
		String notAvailableReason = rs.getString("SpecNotAvailableReason");
		
		if (isNotBlank(notAvailableStr) && notAvailableStr.contains("not available")){
			if (isBlank(notAvailableReason)){
				logger.warn("Blank notAvailableReason has available: " + id);
			}
			NomenclaturalStatus nomStatus = getNomStatus(state, notAvailableReason, id);
			if (nomStatus != null){
				name.addStatus(nomStatus);
			}
		}else{
			if (isNotBlank(notAvailableReason)){
				logger.warn("Blank notAvailable has reason: " + id);
			}
			//Do nothing
		}


		//OLD
//				if (notAvailableStr.contains("not available") ){ 
//					UUID uuidNotAvailableMarkerType = state.getTransformer().getMarkerTypeUuid("not available");
//					
//					MarkerType markerType = getMarkerType(state, uuidNotAvailableMarkerType, "not available", "not available", null);
//					name.addMarker(Marker.NewInstance(markerType, true));
//				}
//		//Not available reason
//		String notAvailableReason = rs.getString("SpecNotAvailableReason");
//		if (isNotBlank(notAvailableReason)){
//			UUID uuidNotAvailableReason;
//			try {
//				uuidNotAvailableReason = state.getTransformer().getExtensionTypeUuid("not available reason");
//				ExtensionType notAvailableReasonExtType = getExtensionType(state, uuidNotAvailableReason, "Not available reason", "Not available reason", null, null);
//				name.addExtension(notAvailableReason, notAvailableReasonExtType);
//			} catch (UndefinedTransformerMethodException e) {
//				e.printStackTrace();
//			} 
//		}
		
	}




	
	private NomenclaturalStatus getNomStatus(GlobisImportState state, String notAvailableReason, int id) {
		NomenclaturalStatus status = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ZOO_NOT_AVAILABLE());
		status.setRuleConsidered(notAvailableReason);
		if (notAvailableReason.equalsIgnoreCase("hybrid")){
			logger.warn("Check hybrid for correctnes. Is there a better status?");
		}else if (notAvailableReason.equalsIgnoreCase("infrasubspecific name") || notAvailableReason.equalsIgnoreCase("infrasubspeciic name")){
			logger.warn("Check infrasubspecific name for correctnes. Is there a better status?");
		}else if (notAvailableReason.equalsIgnoreCase("by ruling of the Commission")){
			logger.warn("Check by ruling of the Commission for correctnes. Is there a better status?");
		}else{
			logger.warn("Unknown non available reason. ");
		}
		return status;
	}

	/**
	 * This method is not used anymore as according to Alexander Marker1 should be ignored.
	 * @param state
	 * @param rs
	 * @param name
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	private void makeMarker1(GlobisImportState state, ResultSet rs, ZoologicalName name) throws SQLException {
		String marker1Str = rs.getString("Marker1");
		try {
			if (isNotBlank(marker1Str)){
				marker1Str = marker1Str.trim();
				if (marker1Str.contains("checked") || marker1Str.contains("berpr") ){ //überprüft
					UUID uuidCheckedMarkerType;
						uuidCheckedMarkerType = state.getTransformer().getMarkerTypeUuid("checked");
					
					MarkerType markerType = getMarkerType(state, uuidCheckedMarkerType, "checked", "checked", null);
					name.addMarker(Marker.NewInstance(markerType, true));
				}
				if (marker1Str.contains("old record") || marker1Str.contains("alte Angabe") ){
					UUID uuidOldRecordMarkerType = state.getTransformer().getMarkerTypeUuid("old record");
					MarkerType markerType = getMarkerType(state, uuidOldRecordMarkerType, "checked", "checked", null);
					name.addMarker(Marker.NewInstance(markerType, true));
				}
			}
		} catch (UndefinedTransformerMethodException e) {
			e.printStackTrace();
		}
		
	}


	private void addNameDescription(GlobisImportState state, ZoologicalName name, UUID featureUuid,
			String citedTypeLocality, String featureLabel) {
		Feature feature = getFeature(state, featureUuid,featureLabel,featureLabel, null, null);
		getTaxonNameDescription(name, false, true);
		
	}


	private Pattern patternAll = Pattern.compile("(.+,\\s.+)(\\(.+\\))");
	

	private void handleTypeInformation(GlobisImportState state, ResultSet rs, ZoologicalName name, Integer specTaxId) throws SQLException {
		if (! hasTypeInformation(rs)){
			return;
		}
		
		FieldUnit fieldObservation = makeTypeFieldObservation(state, rs);
		
		//typeDepository
		String specTypeDepositoriesStr = rs.getString("SpecTypeDepository");
		String[] specTypeDepositories; 
		if (isNotBlank(specTypeDepositoriesStr) ){
			specTypeDepositories = specTypeDepositoriesStr.trim().split(";");
		}else{
			specTypeDepositories = new String[0];
		}
		
		//TODO several issues
		if (specTypeDepositories.length == 0){
			DerivedUnit specimen = makeSingleTypeSpecimen(fieldObservation);
			makeTypeDesignation(name, rs, specimen, specTaxId);
			makeTypeIdInSource(state, specimen, "null", specTaxId);
		}else{
			for (String specTypeDepositoryStr : specTypeDepositories){
				specTypeDepositoryStr = specTypeDepositoryStr.trim();
				
				//Specimen
				DerivedUnit specimen = makeSingleTypeSpecimen(fieldObservation);
	
				if (specTypeDepositoryStr.equals("??")){
					//unknown
					//TODO marker unknown ?
					specimen.setTitleCache("??", true);
					makeTypeIdInSource(state, specimen, "??", specTaxId);
				}else if (specTypeDepositoryStr.equals("[lost]")){
					//lost
					//TODO marker lost ?
					specimen.setTitleCache("lost", true);
					makeTypeIdInSource(state, specimen, "[lost]", specTaxId);
				}else{
					specTypeDepositoryStr = makeAdditionalSpecimenInformation(
							specTypeDepositoryStr, specimen, specTaxId);
					
					Collection collection = makeCollection(state, specTypeDepositoryStr, specimen, specTaxId);
					String collectionCode = collection.getCode();
					if (isBlank(collectionCode)){
						collectionCode = collection.getName();
					}
					if (isBlank(collectionCode)){
						logger.warn("Collection has empty representation: " + specTypeDepositoryStr + ", specTaxId" +  specTaxId);
					}
					makeTypeIdInSource(state, specimen, collectionCode , specTaxId);	
				}
				
				//type Designation
				makeTypeDesignation(name, rs, specimen, specTaxId);
			}
		}

		
	}


	private void makeTypeIdInSource(GlobisImportState state, DerivedUnit specimen, String collectionCode, Integer specTaxId) {
		String namespace = TYPE_NAMESPACE;
		String id = getTypeId(specTaxId, collectionCode);
		IdentifiableSource source = IdentifiableSource.NewInstance(OriginalSourceType.Import, id, namespace, state.getTransactionalSourceReference(), null);
		specimen.addSource(source);
	}




	public static String getTypeId(Integer specTaxId, String collectionCode) {
		String result = String.valueOf(specTaxId) + "@" + collectionCode;
		return result;
	}




	private boolean hasTypeInformation(ResultSet rs) throws SQLException {
		String specTypeDepositoriesStr = rs.getString("SpecTypeDepository");
		String countryString = rs.getString("SpecTypeCountry");
		String specType = rs.getString("SpecType");
		boolean result = false;
		result |= isNotBlank(specTypeDepositoriesStr) || isNotBlank(countryString)
			|| isNotBlank(specType);
		return result;
	}



	/**
	 * @param state 
	 * @param specTypeDepositoryStr
	 * @param specimen
	 * @param specTaxId 
	 */
	protected Collection makeCollection(GlobisImportState state, String specTypeDepositoryStr, DerivedUnit specimen, Integer specTaxId) {
		
		//Collection
		specTypeDepositoryStr = specTypeDepositoryStr.replace("Washington, D.C.", "Washington@ D.C.")
				.replace("St.-Raymond, Quebec", "St.-Raymond@ Quebec")
				.replace("St.Petersburg", "St. Petersburg");
		
		
		Collection collection = handleSpecialCase(specTypeDepositoryStr, state, specimen);
		if (collection == null){
			String[] split = specTypeDepositoryStr.split(",");
			if (split.length != 2){
				if (split.length == 1 && (split[0].startsWith("coll.")|| split[0].startsWith("Coll.") )){
					collection = state.getRelatedObject(COLLECTION_NAMESPACE, split[0], Collection.class);
					if (collection == null){
						collection = Collection.NewInstance();
						collection.setName(split[0]);
						state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
					}
					specimen.setCollection(collection);
				}else{
					logger.warn("Split size in SpecTypeDepository is not 2: " + specTypeDepositoryStr + " (specTaxID:" + specTaxId + ")");
					collection = Collection.NewInstance();
					collection.setCode("??");
					//TODO deduplicate ??
				}
				
			}else{
				String collectionStr = split[0];
				String location = split[1].replace("@", ",").trim();
				
				collection = state.getRelatedObject(COLLECTION_NAMESPACE, collectionStr, Collection.class);
				if (collection == null){
					collection = Collection.NewInstance();
					if (collectionStr != null && collectionStr.startsWith("coll.")){
						collection.setName(collectionStr);
					}else{
						collection.setCode(collectionStr);
					}
					collection.setTownOrLocation(location);
					state.addRelatedObject(COLLECTION_NAMESPACE, collection.getCode(), collection);
						
				}else if (! CdmUtils.nullSafeEqual(location, collection.getTownOrLocation())){
					if (! normalizeTownOrLocation(location, collection)){
						String message = "Location (%s) is not equal to location (%s) of existing collection, specTaxId: " + specTaxId;
						logger.warn(String.format(message, location, collection.getTownOrLocation(), collection.getCode()));
					}
					
				}
				
				specimen.setCollection(collection);
			}
		}
		return collection;
	}


	private boolean normalizeTownOrLocation(String location, Collection collection) {
		boolean result = false;
		if (location != null){
			if ("coll. C. G. Treadaway".equals(collection.getName())){
				if (! "Frankfurt am Main".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Frankfurt am Main");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			}	else if ("coll. K. Rose".equals(collection.getName())){
				if (! "Mainz-Bretzenheim".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Mainz-Bretzenheim");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("NMPN".equals(collection.getCode())){
				if (! "Prague".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Prague");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("USNM".equals(collection.getCode())){
				if (! "Washington, D.C.".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Washington, D.C.");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. S. Kocman".equals(collection.getName())){
				if (! "Ostrava-Zabreh".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Ostrava-Zabreh");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("ZSSM".equals(collection.getCode())){
				if (! "Munich".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Munich");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. R. H. Anken".equals(collection.getName())){
				if (! "Stuttgart".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Stuttgart");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. S.-I. Murayama".equals(collection.getName())){
				if (! "Aichi-Gakuin".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Aichi-Gakuin");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. Y. Sorimachi".equals(collection.getName())){
				if (! "Saitama".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Saitama");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. K. Yoshino".equals(collection.getName())){
				if (! "Saitama".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Saitama");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. U. Eitschberger".equals(collection.getName())){
				if (! "Marktleuthen".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Marktleuthen");
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. G. Sala".equals(collection.getName())){
				if (! "Sal\u00f2".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Sal\u00f2");  //LATIN SMALL LETTER O WITH GRAVE
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. Dantchenko".equals(collection.getName())){
				if (! "Moscow".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Moscow");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. S. Nakano".equals(collection.getName())){
				if (! "Tokyo".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Tokyo");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. A. Yagishita".equals(collection.getName())){
				if (! "Toride Ibaraki".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Toride Ibaraki");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. H. Sugiyama".equals(collection.getName())){
				if (! "Gifu".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Gifu");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("MDMO".equals(collection.getCode())){
				if (! "Moscow".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Moscow");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. W. Eckweiler".equals(collection.getName())){
				if (! "Frankfurt am Main".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Frankfurt am Main");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			} else if ("coll. T. Frankenbach".equals(collection.getName())){
				if (! "Wangen".equals(collection.getTownOrLocation())){
					collection.setTownOrLocation("Wangen");  
					getCollectionService().saveOrUpdate(collection);
				}
				return true;
			}
		}
		
		return result;
	}

	private Collection handleSpecialCase(String specTypeDepositoryStr, GlobisImportState state, DerivedUnit specimen) {
		Collection collection;
		if (specTypeDepositoryStr.equals("BMNH, London and/or MNHN, Paris")){
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, specTypeDepositoryStr, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(specTypeDepositoryStr);
				collection.setTownOrLocation("London or Paris");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.equals("ZISP, St. Petersburg, and/or BMNH, London ?")){
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, specTypeDepositoryStr, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(specTypeDepositoryStr);
				collection.setTownOrLocation("St. Petersburg or London");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.equals("MFNB, Berlin or SMTD, Dresden")){
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, specTypeDepositoryStr, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(specTypeDepositoryStr);
				collection.setTownOrLocation("Berlin or Dresden");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.equals("coll. S. Ianoka, Yamanashi, coll. A. Shinkai, Tokyo or coll. S. Morita")){
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, specTypeDepositoryStr, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(specTypeDepositoryStr);
				collection.setTownOrLocation("Yamanashi or Tokyo or ?");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.equals("coll. S. Inaoka, Yamanashi, coll. A. Shinkai, Tokyo, coll. H. Mikami, coll. S. Koiwaya, coll S. Morita or coll. Y. Nose")){
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, specTypeDepositoryStr, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(specTypeDepositoryStr);
				collection.setTownOrLocation("Yamanashi or Tokyo or ?");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.equals("S. Morita, Tokyo or coll. Y. Sorimachi, Saitama")){
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, specTypeDepositoryStr, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(specTypeDepositoryStr);
				collection.setTownOrLocation("Tokyo or Saitama");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
			
		}else if (specTypeDepositoryStr.equals("coll. L. V. Kaabak, A .V. Sotshivko & V. V. Titov, Moscow")){
			String colName = "coll. L. V. Kaabak, A .V. Sotshivko & V. V. Titov";
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, colName, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(colName);
				collection.setTownOrLocation("Moscow");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.matches("coll. R. E. Parrott?, Port Hope, Ontario")){
			String colName = "coll. R. E. Parrott";
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, colName, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(colName);
				collection.setTownOrLocation("Port Hope, Ontario");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.matches("coll. O. Slaby, Plzen, Tschechei")){
			String colName = "coll. O. Slaby";
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, colName, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(colName);
				collection.setTownOrLocation("Plzen, Tschechei");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else if (specTypeDepositoryStr.matches("coll. K. Okubo")){
			String colName = "coll. K. Okubo";
			collection = state.getRelatedObject(COLLECTION_NAMESPACE, colName, Collection.class);
			if (collection == null){
				collection = Collection.NewInstance();
				collection.setName(colName);
				collection.setTownOrLocation("Nichinomiya city, Hyogo");
				state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
			}
			specimen.setCollection(collection);
		}else{
			collection = null;
		}
		
		
		return collection;
	}

	/**
	 * @param specTypeDepositoriesStr
	 * @param specTypeDepositoryStr
	 * @param specimen
	 * @param specTaxId 
	 * @return
	 */
	protected String makeAdditionalSpecimenInformation( String specTypeDepositoryStr, DerivedUnit specimen, Integer specTaxId) {
		//doubful
		if (specTypeDepositoryStr.endsWith("?")){
			Marker.NewInstance(specimen, true, MarkerType.IS_DOUBTFUL());
			specTypeDepositoryStr = specTypeDepositoryStr.substring(0, specTypeDepositoryStr.length() -1).trim();
		}
		
		//brackets
		Matcher matcher = patternAll.matcher(specTypeDepositoryStr);
		if (matcher.find()){
			//has brackets
			String brackets = matcher.group(2);
			brackets = brackets.substring(1, brackets.length()-1);
			
			//TODO this is unwanted according to Alexander
			brackets = brackets.replace("[mm]", "\u2642\u2642");
			brackets = brackets.replace("[m]", "\u2642");
			brackets = brackets.replace("[ff]", "\u2640\u2640");
			brackets = brackets.replace("[f]", "\u2640");
			brackets = brackets.replace("[m/f]", "\u26a5");
			
			if (brackets.contains("[") || brackets.contains("]")){
				logger.warn ("There are still '[', ']' in the bracket part: " + brackets + "; specTaxId: " + specTaxId);
			}
			
			//TODO replace mm/ff by Unicode male 
			specimen.setTitleCache(brackets, true);
			specTypeDepositoryStr = matcher.group(1).trim();
		}
		return specTypeDepositoryStr;
	}




	/**
	 * @param fieldObservation
	 * @return
	 */
	protected DerivedUnit makeSingleTypeSpecimen(FieldUnit fieldObservation) {
		DerivationEvent derivEvent = DerivationEvent.NewInstance();
//			derivEvent.setType(DerivationEventType.ACCESSIONING());
		fieldObservation.addDerivationEvent(derivEvent);
		DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
		specimen.setDerivedFrom(derivEvent);
		return specimen;
	}




	/**
	 * @param state
	 * @return
	 * @throws SQLException
	 */
	protected FieldUnit makeTypeFieldObservation(GlobisImportState state, 
			ResultSet rs) throws SQLException {
		
		String countryString = rs.getString("SpecTypeCountry");
		
		SpecimenOrObservationType unitType = SpecimenOrObservationType.PreservedSpecimen;
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(unitType);
		
		NamedArea typeCountry = getCountry(state, countryString);
		facade.setCountry(typeCountry);
		FieldUnit fieldObservation = facade.innerFieldUnit();
		return fieldObservation;
	}




	/**
	 * @param name
	 * @param rs 
	 * @param status
	 * @param specimen
	 * @param specTaxId 
	 * @throws SQLException 
	 */
	protected void makeTypeDesignation(ZoologicalName name, ResultSet rs, DerivedUnit specimen, Integer specTaxId) throws SQLException {
		//type
		String specType = rs.getString("SpecType");
		SpecimenTypeDesignationStatus status = getTypeDesigType(specType, specTaxId);

		SpecimenTypeDesignation typeDesignation = SpecimenTypeDesignation.NewInstance();
		typeDesignation.setTypeStatus(status);
		typeDesignation.setTypeSpecimen(specimen);
		
		name.addTypeDesignation(typeDesignation, true);
	}




	private SpecimenTypeDesignationStatus getTypeDesigType(String specType, Integer specTaxId) {
		if (isBlank(specType) ){
			return null;
		}else if (specType.matches("Holotype(\r*\n*Holotypus)?")){
			return SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (specType.matches("Neotype")){
			return SpecimenTypeDesignationStatus.NEOTYPE();
		}else if (specType.matches("Syntype(\\(s\\))?") || specType.matches("Syntype.*Syntype\\(s\\)\\s*") ){
			return SpecimenTypeDesignationStatus.SYNTYPE();
		}else if (specType.matches("Syntype\r*\n*Syntype.*") ){
			return SpecimenTypeDesignationStatus.SYNTYPE();
		}else if (specType.matches("Lectotype")){
			return SpecimenTypeDesignationStatus.LECTOTYPE();
		}else{
			logger.warn("SpecimenTypeDesignationStatus does not match: " + specType + " in specTaxId "  + specTaxId);
			return null;
		}
	}




	/**
	 * @param state
	 * @param referenceMap
	 * @param rs
	 * @param name
	 * @param specTaxId 
	 * @return
	 * @throws SQLException
	 */
	private Reference<?> handleNomRef(GlobisImportState state, Map<String, Reference> referenceMap, ResultSet rs,
			ZoologicalName name, Integer specTaxId) throws SQLException {
		//ref
		Integer refId = nullSafeInt(rs, "fiSpecRefID");
		Reference<?> nomRef = null;
		if (refId != null){
			nomRef = referenceMap.get(String.valueOf(refId));
			if (nomRef == null && state.getConfig().getDoReferences().equals(state.getConfig().getDoReferences().ALL)){
				logger.warn("Reference " + refId + " could not be found. SpecTaxId: " + specTaxId);
			}else if (nomRef != null){
				name.setNomenclaturalReference(nomRef);
			}
		}
		
		//refDetail
		String refDetail = rs.getString("SpecPage");
		if (isNotBlank(refDetail)){
			name.setNomenclaturalMicroReference(refDetail);
		}
		return nomRef;
	}



	
	private void validateAcceptedTaxon(Taxon acceptedTaxon, ResultSet rs, Integer specTaxId, Integer acceptedTaxonId) throws SQLException {
		if (acceptedTaxon == null){
			logger.warn("Accepted taxon is null for taxon taxon to validate. SpecTaxId " + specTaxId + ", accTaxonId: " + acceptedTaxonId);
			return;
		}
		
		//TODO 
		ZoologicalName name = CdmBase.deproxy(acceptedTaxon.getName(), ZoologicalName.class);
		
		String specName = rs.getString("SpecName");
		if (! name.getSpecificEpithet().equals(specName)){
			logger.warn(String.format("Species epithet is not equal for accepted taxon: %s - %s. SpecTaxId: %d", name.getSpecificEpithet(), specName, specTaxId));
		}
		//TODO
	}




	private Synonym getSynonym(GlobisImportState state, ResultSet rs, Integer specTaxId) throws SQLException {
		ZoologicalName name = makeName(state, rs, specTaxId);
				
		Synonym synonym = Synonym.NewInstance(name, state.getTransactionalSourceReference());
		
		return synonym;
	}




	/**
	 * @param state
	 * @param rs
	 * @param specTaxId 
	 * @return
	 * @throws SQLException
	 */
	protected ZoologicalName makeName(GlobisImportState state, ResultSet rs, Integer specTaxId)
			throws SQLException {
		//rank
		String rankStr = rs.getString("SpecRank");
		Rank rank = null;
		if (isNotBlank(rankStr)){
			try {
				rank = Rank.getRankByNameOrIdInVoc(rankStr, NomenclaturalCode.ICZN, true);
			} catch (UnknownCdmTypeException e) {
				e.printStackTrace();
			}
		}
		
		//name
		ZoologicalName name = ZoologicalName.NewInstance(rank);
		makeNamePartsAndCache(state, rs, rankStr, name);
		

//		name.setGenusOrUninomial(genusOrUninomial);
		String authorStr = rs.getString("SpecAuthor");
		String yearStr = rs.getString("SpecYear");
		String authorAndYearStr = CdmUtils.concat(", ", authorStr, yearStr);
		handleAuthorAndYear(authorAndYearStr, name, specTaxId, state);
		
		return name;
	}




	private void makeNamePartsAndCache(GlobisImportState state, ResultSet rs, String rank, ZoologicalName name) throws SQLException {
		String citedFamily = rs.getString("SpecCitedFamily");
		String citedGenus = rs.getString("SpecCitedGenus");
		String citedSpecies = rs.getString("SpecCitedSpecies");
		String citedSubspecies = rs.getString("SpecCitedSubspecies");
		String lastEpithet = rs.getString("SpecName");
		
		
		String cache = CdmUtils.concat(" ", new String[]{citedFamily, citedGenus, citedSpecies, citedSubspecies, rank, lastEpithet});
		name.setGenusOrUninomial(citedGenus);
		//TODO separate authors
		if (isBlank(citedSpecies)){
			name.setSpecificEpithet(lastEpithet);
		}else{
			name.setSpecificEpithet(citedSpecies);
			if (isBlank(citedSubspecies)){
				name.setInfraSpecificEpithet(lastEpithet);
			}
		}
		
		//TODO check if cache needs protection
		name.setNameCache(cache, true);
	}

	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, GlobisImportState state) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		
		Set<AgentBase> agents = state.getAgents();
		getAgentService().saveOrUpdate(agents);
		
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "SpecCurrspecID");
				handleForeignKey(rs, referenceIdSet, "fiSpecRefID");
			}
			
			//taxon map
			nameSpace = TAXON_NAMESPACE;
			cdmClass = Taxon.class;
			idSet = taxonIdSet;
			Map<String, Taxon> objectMap = (Map<String, Taxon>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

			//reference map
			nameSpace = REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> referenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, referenceMap);
			
			//collection map
			nameSpace = COLLECTION_NAMESPACE;
			List<Collection> listCollection = getCollectionService().list(Collection.class, null, null, null, null);
			Map<String, Collection> collectionMap = new HashMap<String, Collection>();
			for (Collection collection : listCollection){
				if (isNotBlank(collection.getCode())){
					collectionMap.put(collection.getCode(), collection);
				}else if (isNotBlank(collection.getName())){
					collectionMap.put(collection.getName(), collection);
				}else{
					logger.warn("Collection code and name are blank: " + collection);
				}
			}
			result.put(nameSpace, collectionMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(GlobisImportState state){
		IOValidator<GlobisImportState> validator = new GlobisSpecTaxaImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(GlobisImportState state){
		return ! state.getConfig().isDoSpecTaxa();
	}




	@Override
	public Reference<?> createObject(ResultSet rs, GlobisImportState state)
			throws SQLException {
		// not needed
		return null;
	}

}
