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
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.globis.validation.GlobisSpecTaxaImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class GlobisSpecTaxImport  extends GlobisImportBase<Reference> implements IMappingImport<Reference, GlobisImportState>{
	private static final Logger logger = Logger.getLogger(GlobisSpecTaxImport.class);
	
	private int modCount = 10000;
	private static final String pluralString = "taxa";
	private static final String dbTableName = "specTax";
	private static final Class cdmTargetClass = Reference.class;
	
	private static UUID uuidCitedTypeLocality = UUID.fromString("ca431e0a-84ec-4828-935f-df4c8f5cf880");
	private static UUID uuidCitedTypeMaterial = UUID.fromString("8395021a-e596-4a55-9794-8c03aaad9e16");

	public GlobisSpecTaxImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}


	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.globis.GlobisImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strRecordQuery = 
			" SELECT specTaxId " + 
			" FROM " + dbTableName; 
		return strRecordQuery;	
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(GlobisImportConfigurator config) {
		String strRecordQuery = 
			" SELECT t.*, t.DateCreated as Created_When, t.CreatedBy as Created_Who," +
			"        t.ModifiedBy as Updated_who, t.DateModified as Updated_When, t.SpecRemarks as Notes " + 
			" FROM " + getTableName() + " t " +
			" WHERE ( t.specTaxId IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.globis.GlobisImportBase#doPartition(eu.etaxonomy.cdm.io.common.ResultSetPartitioner, eu.etaxonomy.cdm.io.globis.GlobisImportState)
	 */
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
        		
        		//ignore: CountryDummy, currentSpecies, DepositoryDisplay, DepositoryDummy, ReferenceDisplay, SpecDescriptionImageFile, all *Valid*
        		
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
					
					handleNomRef(state, referenceMap, rs, name);
				
					handleTypeInformation(state,rs, name, specTaxId);
				
				
//						this.doIdCreatedUpdatedNotes(state, ref, rs, refId, REFERENCE_NAMESPACE);
				
					if (acceptedTaxon != null){
						objectsToSave.add(acceptedTaxon); 
					}
					
					//makeMarker1(state, rs, name);   //ignore!
					
					makeNotAvailable(state, rs, name);
					
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

					name.addSource(String.valueOf(specTaxId), SPEC_TAX_NAMESPACE, state.getTransactionalSourceReference(), null);
					
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


	private void makeNotAvailable(GlobisImportState state, ResultSet rs, ZoologicalName name) throws SQLException {
		String notAvailableStr = rs.getString("SpecNotAvailable");
		try {
			if (isNotBlank(notAvailableStr)){
				if (notAvailableStr.contains("not available") ){ 
					UUID uuidNotAvailableMarkerType = state.getTransformer().getMarkerTypeUuid("not available");
					
					MarkerType markerType = getMarkerType(state, uuidNotAvailableMarkerType, "not available", "not available", null);
					name.addMarker(Marker.NewInstance(markerType, true));
				}
			}
		} catch (UndefinedTransformerMethodException e) {
			e.printStackTrace();
		}
		//Not available reason
		//TODO make it a vocabulary
		String notAvailableReason = rs.getString("SpecNotAvailableReason");
		if (isNotBlank(notAvailableReason)){
			UUID uuidNotAvailableReason;
			try {
				uuidNotAvailableReason = state.getTransformer().getExtensionTypeUuid("not available reason");
				ExtensionType notAvailableReasonExtType = getExtensionType(state, uuidNotAvailableReason, "Not available reason", "Not available reason", null, null);
				name.addExtension(notAvailableReason, notAvailableReasonExtType);
			} catch (UndefinedTransformerMethodException e) {
				e.printStackTrace();
			} 
		}
		
	}




	
	/**
	 * This method is not used anymore as according to Alexander Marker1 should be ignored.
	 * @param state
	 * @param rs
	 * @param name
	 * @throws SQLException
	 */
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
		
		FieldObservation fieldObservation = makeTypeFieldObservation(state, rs);
		
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
			Specimen specimen = makeSingleTypeSpecimen(fieldObservation);
			makeTypeDesignation(name, rs, specimen, specTaxId);
			makeTypeIdInSource(state, specimen, "null", specTaxId);
		}
		for (String specTypeDepositoryStr : specTypeDepositories){
			specTypeDepositoryStr = specTypeDepositoryStr.trim();
			
			//Specimen
			Specimen specimen = makeSingleTypeSpecimen(fieldObservation);

			if (specTypeDepositoryStr.equals("??")){
				//unknown
				specimen.setTitleCache("??", true);
				makeTypeIdInSource(state, specimen, "??", specTaxId);
				
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


	private void makeTypeIdInSource(GlobisImportState state, Specimen specimen, String collectionCode, Integer specTaxId) {
		String namespace = TYPE_NAMESPACE;
		String id = getTypeId(specTaxId, collectionCode);
		IdentifiableSource source = IdentifiableSource.NewInstance(id, namespace, state.getTransactionalSourceReference(), null);
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
	protected Collection makeCollection(GlobisImportState state, String specTypeDepositoryStr, Specimen specimen, Integer specTaxId) {
		
		//Collection
		specTypeDepositoryStr = specTypeDepositoryStr.replace("Washington, D.C.", "Washington@ D.C.");
		
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
		}else{
			
			String[] split = specTypeDepositoryStr.split(",");
			if (split.length != 2){
				if (split.length == 1 && split[0].startsWith("coll.")){
					collection = state.getRelatedObject(COLLECTION_NAMESPACE, split[0], Collection.class);
					if (collection == null){
						collection = Collection.NewInstance();
						collection.setName(split[0]);
						state.addRelatedObject(COLLECTION_NAMESPACE, collection.getName(), collection);
					}
					specimen.setCollection(collection);
				}else{
					logger.warn("Split size is not 2: " + specTypeDepositoryStr + " (specTaxID:" + specTaxId + ")");
					collection = Collection.NewInstance();
					collection.setCode("??");
					//TODO deduplicate ??
				}
				
			}else{
				String collectionStr = split[0];
				String location = split[1].replace("Washington@ D.C.", "Washington, D.C.");
				
				collection = state.getRelatedObject(COLLECTION_NAMESPACE, collectionStr, Collection.class);
				if (collection == null){
					collection = Collection.NewInstance();
					collection.setCode(collectionStr);
					collection.setTownOrLocation(split[1]);
					state.addRelatedObject(COLLECTION_NAMESPACE, collection.getCode(), collection);
						
				}else if (! CdmUtils.nullSafeEqual(location, collection.getTownOrLocation())){
					String message = "Location (%s) is not equal to location (%s) of existing collection";
					logger.warn(String.format(message, location, collection.getTownOrLocation(), collection.getCode()));
				}
				
				specimen.setCollection(collection);
			}
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
	protected String makeAdditionalSpecimenInformation( String specTypeDepositoryStr, Specimen specimen, Integer specTaxId) {
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
	protected Specimen makeSingleTypeSpecimen(FieldObservation fieldObservation) {
		DerivationEvent derivEvent = DerivationEvent.NewInstance();
//			derivEvent.setType(DerivationEventType.ACCESSIONING());
		fieldObservation.addDerivationEvent(derivEvent);
		Specimen specimen = Specimen.NewInstance();
		specimen.setDerivedFrom(derivEvent);
		return specimen;
	}




	/**
	 * @param state
	 * @return
	 * @throws SQLException
	 */
	protected FieldObservation makeTypeFieldObservation(GlobisImportState state, 
			ResultSet rs) throws SQLException {
		
		String countryString = rs.getString("SpecTypeCountry");
		
		DerivedUnitType unitType = DerivedUnitType.Specimen;
		DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(unitType);
		
		NamedArea typeCountry = getCountry(state, countryString);
		facade.setCountry(typeCountry);
		FieldObservation fieldObservation = facade.innerFieldObservation();
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
	protected void makeTypeDesignation(ZoologicalName name, ResultSet rs, Specimen specimen, Integer specTaxId) throws SQLException {
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
		}else if (specType.matches("Holotype(.*Holotypus)?")){
			return SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (specType.matches("Neotype")){
			return SpecimenTypeDesignationStatus.NEOTYPE();
		}else if (specType.matches("Syntype(\\(s\\))?") || specType.matches("Syntype.*Syntype\\(s\\)\\s*") ){
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
	 * @return
	 * @throws SQLException
	 */
	private Reference<?> handleNomRef(GlobisImportState state, Map<String, Reference> referenceMap, ResultSet rs,
			ZoologicalName name) throws SQLException {
		//ref
		Integer refId = nullSafeInt(rs, "fiSpecRefID");
		Reference<?> nomRef = null;
		if (refId != null){
			nomRef = referenceMap.get(String.valueOf(refId));
			if (nomRef == null && state.getConfig().getDoReferences().equals(state.getConfig().getDoReferences().ALL)){
				logger.warn("Reference " + refId + " could not be found.");
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
			logger.warn("Accepted taxon is null for taxon taxon to validate: ");
			return;
		}
		
		//TODO 
		ZoologicalName name = CdmBase.deproxy(acceptedTaxon.getName(), ZoologicalName.class);
		
		String specName = rs.getString("SpecName");
		if (! name.getSpecificEpithet().equals(specName)){
			logger.warn(String.format("Species epithet is not equal for accepted taxon: %s - %s", name.getSpecificEpithet(), specName));
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
				rank = Rank.getRankByNameOrAbbreviation(rankStr, NomenclaturalCode.ICZN, true);
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
		handleAuthorAndYear(authorAndYearStr, name, specTaxId);
		
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
		//TODO sperate authors
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
				collectionMap.put(collection.getCode(), collection);
				if (isBlank(collection.getCode())){
					logger.warn("Collection code is blank: " + collection);
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
