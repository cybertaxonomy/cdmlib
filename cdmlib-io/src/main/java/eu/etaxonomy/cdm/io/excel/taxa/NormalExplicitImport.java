 /**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.excel.taxa;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase.SourceDataHolder;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.babadshanjan
 * @created 08.01.2009
 */

@Component
public class NormalExplicitImport extends TaxonExcelImporterBase {
    private static final long serialVersionUID = 3642423349766191160L;

    private static final Logger logger = Logger.getLogger(NormalExplicitImport.class);

	public static Set<String> validMarkers = new HashSet<String>(Arrays.asList(new String[]{"", "valid", "accepted", "a", "v", "t", "!"}));
	public static Set<String> synonymMarkers = new HashSet<String>(Arrays.asList(new String[]{"**","invalid", "synonym", "s", "i"}));
	public static Set<String> nameStatusMarkers = new HashSet<String>(Arrays.asList(new String[]{"illegitimate", "nom. rej.", "nom. cons."}));
	public static final UUID uuidRefExtension = UUID.fromString("a46533df-7a78-448f-9b80-36d087fbdf2a");

    private static final Object NOM_ILLEG = "illegitimate";
    private static final Object NOM_REJ = "nom. rej.";
    private static final Object NOM_CONS = "nom. cons.";


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase#analyzeSingleValue(eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase.KeyValue, eu.etaxonomy.cdm.io.excel.common.ExcelImportState)
	 */
	@Override
	protected void analyzeSingleValue(KeyValue keyValue, TaxonExcelImportState state) {

		NormalExplicitRow normalExplicitRow = state.getCurrentRow();
		String key = keyValue.key;
		String value = keyValue.value;
		Integer index = keyValue.index;
		if (((NormalExplicitImportConfigurator)state.getConfig()).getParentUUID() != null){
            normalExplicitRow.setParentId(0);
		}

		key = key.replace(" ","");
		if (key.equalsIgnoreCase(ID_COLUMN)) {
			int ivalue = floatString2IntValue(value);
			normalExplicitRow.setId(ivalue);

		} else if(key.equalsIgnoreCase(PARENT_ID_COLUMN) ) {
			int ivalue = floatString2IntValue(value);
			normalExplicitRow.setParentId(ivalue);

		} else if(key.equalsIgnoreCase(RANK_COLUMN)) {
			normalExplicitRow.setRank(value);

		} else if(key.trim().equalsIgnoreCase(SCIENTIFIC_NAME_COLUMN)) {
			normalExplicitRow.setScientificName(value);

		} else if(key.equalsIgnoreCase(AUTHOR_COLUMN)) {
			normalExplicitRow.setAuthor(value);

		} else if(key.equalsIgnoreCase(REFERENCE_COLUMN)) {
			normalExplicitRow.setReference(value);

		} else if(key.equalsIgnoreCase(NAME_STATUS_COLUMN) ) {
			normalExplicitRow.setNameStatus(value);

		} else if(key.equalsIgnoreCase(VERNACULAR_NAME_COLUMN)) {
			normalExplicitRow.setCommonName(value);

		} else if(key.equalsIgnoreCase(LANGUAGE_COLUMN)) {
			normalExplicitRow.setLanguage(value);

		} else if(key.equalsIgnoreCase(TDWG_COLUMN)) {
			//TODO replace still necessary?
			value = value.replace(".0", "");
			normalExplicitRow.putDistribution(index, value);

		} else if(key.equalsIgnoreCase(PROTOLOGUE_COLUMN)) {
			normalExplicitRow.putProtologue(index, value);

		} else if(key.equalsIgnoreCase(IMAGE_COLUMN)) {
			normalExplicitRow.putImage(index, value);

		} else if(key.equalsIgnoreCase(DATE_COLUMN) || key.equalsIgnoreCase(YEAR_COLUMN)) {
            normalExplicitRow.setDate(value);

        } else if(key.equalsIgnoreCase(FAMILY_COLUMN)) {
            normalExplicitRow.setFamily(value);
        }else if(key.equalsIgnoreCase("!")) {
            //! = Legitimate, * = Illegitimate, ** = Invalid, *** = nom. rej., !! = nom. cons.
            if (value.equals("!")){
                normalExplicitRow.setNameStatus("accepted");
            } else if (value.equals("*")){
                normalExplicitRow.setNameStatus("illegitimate");
            } else if (value.equals("**")){
                normalExplicitRow.setNameStatus("invalid");
            } else if (value.equals("***")){
                normalExplicitRow.setNameStatus("nom. rej.");
            } else if (value.equals("!!")){
                normalExplicitRow.setNameStatus("nom. cons.");
            } else{
                normalExplicitRow.setNameStatus("accepted");
            }
        }else {
			if (analyzeFeatures(state, keyValue)){
				//ok
			}else{
				String message = "Unexpected column header " + key;
				fireWarningEvent(message, state, 10);
				state.setUnsuccessfull();
				logger.error(message);
			}
		}
		return;
	}


	/**
	 *  Create base taxa and add all information attached to it's name.
	 */
	@Override
    protected void firstPass(TaxonExcelImportState state) {

//		if (1==1){
//			return;
//		}
//		System.out.println("FP:" + state.getCurrentLine());
		Rank rank = null;
		NormalExplicitRow taxonDataHolder = state.getCurrentRow();

		String rankStr = taxonDataHolder.getRank();
		String taxonNameStr = taxonDataHolder.getScientificName();
		String authorStr = taxonDataHolder.getAuthor();
		String referenceStr = taxonDataHolder.getReference();
		String nameStatus = taxonDataHolder.getNameStatus();
		String familyNameStr = taxonDataHolder.getFamily();
		String dateStr = taxonDataHolder.getDate();
		Integer id = taxonDataHolder.getId();
		UUID cdmUuid = taxonDataHolder.getCdmUuid();

		TaxonBase<?> taxonBase = null;
		if (cdmUuid != null){
			taxonBase = getTaxonService().find(cdmUuid);
		}else{
			if (StringUtils.isNotBlank(taxonNameStr)) {

				// Rank
				try {
				    if (!StringUtils.isBlank(rankStr)) {
                        rank = Rank.getRankByNameOrIdInVoc(rankStr);
                        }
				} catch (UnknownCdmTypeException ex) {
					try {
						rank = Rank.getRankByEnglishName(rankStr, state.getConfig().getNomenclaturalCode(), false);
					} catch (UnknownCdmTypeException e) {
						//state.setUnsuccessfull();
						logger.error(rankStr + " is not a valid rank.");
					}
				}

	            //taxon
				taxonBase = createTaxon(state, rank, taxonNameStr, authorStr, referenceStr, dateStr, nameStatus);
			}else{
				return;
			}
		}
		if (taxonBase == null){
			String message = "Taxon is already in DB. Record will not be handled";
			fireWarningEvent(message, "Record: " + state.getCurrentLine(), 6);
			logger.warn(message);
			//state.setUnsuccessfull();
			return;
		}

		//protologue
		for (String protologue : taxonDataHolder.getProtologues()){
			TextData textData = TextData.NewInstance(Feature.PROTOLOGUE());
			this.getNameDescription(taxonBase.getName()).addElement(textData);
			URI uri;
			try {
				uri = new URI(protologue);
				textData.addMedia(Media.NewInstance(uri, null, null, null));
			} catch (URISyntaxException e) {
				String warning = "URISyntaxException when trying to convert to URI: " + protologue;
				logger.error(warning);
				state.setUnsuccessfull();
			}
		}

		state.putTaxon(id, taxonBase);
		getTaxonService().save(taxonBase);

		return;
    }



	/**
	 *  Stores parent-child, synonym and common name relationships.
	 *  Adds all taxon related descriptive information (this is not done in the first pass
	 *  because the information may also be attached to a synonym).
	 */
	@Override
    protected void secondPass(TaxonExcelImportState state) {
		if (logger.isDebugEnabled()){logger.debug(state.getCurrentLine());}
		try {
			NormalExplicitRow taxonDataHolder = state.getCurrentRow();
			String taxonNameStr = taxonDataHolder.getScientificName();
			String nameStatus = taxonDataHolder.getNameStatus();
			String commonNameStr = taxonDataHolder.getCommonName();
			Integer parentId = taxonDataHolder.getParentId();
			Integer childId = taxonDataHolder.getId();
			UUID cdmUuid = taxonDataHolder.getCdmUuid();
			Taxon acceptedTaxon = null;
			TaxonNameBase<?,?> nameUsedInSource = null;
			TaxonBase<?> taxonBase = null;

			if (cdmUuid != null){
				taxonBase = getTaxonService().find(cdmUuid);
				acceptedTaxon = getAcceptedTaxon(taxonBase);
				nameUsedInSource = taxonBase.getName();
			}else{
			    //TODO error handling for class cast
			    Taxon parentTaxon = null;
			    if (parentId == 0 && state.getParent() == null){
			        parentTaxon =(Taxon) getTaxonService().load(((NormalExplicitImportConfigurator)state.getConfig()).getParentUUID());
			        state.setParent(parentTaxon);
			    }else if (parentId != 0){
			       parentTaxon = CdmBase.deproxy(state.getTaxonBase(parentId), Taxon.class);
			    } else if (state.getParent() != null){
			        parentTaxon = state.getParent();
			    }
				if (StringUtils.isNotBlank(taxonNameStr)) {
					taxonBase = state.getTaxonBase(childId);
					if (taxonBase != null){

					nameUsedInSource = taxonBase.getName();
					nameStatus = CdmUtils.Nz(nameStatus).trim().toLowerCase();
					if (validMarkers.contains(nameStatus)){
						Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
						acceptedTaxon = taxon;
						// Add the parent relationship
						//if (state.getCurrentRow().getParentId() != 0) {
						MergeResult result = null;
							if (parentTaxon != null) {
								//Taxon taxon = (Taxon)state.getTaxonBase(childId);

								Reference sourceRef = state.getConfig().getSourceReference();
								String microCitation = null;
								Taxon childTaxon = taxon;
								makeParent(state, parentTaxon, childTaxon, sourceRef, microCitation);
								getTaxonService().saveOrUpdate(parentTaxon);
								state.putTaxon(parentId, parentTaxon);
							} else {
								String message = "Taxonomic parent not found for " + taxonNameStr;
								logger.warn(message);
								fireWarningEvent(message, state, 6);
								//state.setUnsuccessfull();
							}
//						}else{
//							//do nothing (parent == 0) no parent exists
//						}
					}else if (synonymMarkers.contains(nameStatus)){
						//add synonym relationship
						acceptedTaxon = parentTaxon;
						try {
							Synonym synonym = CdmBase.deproxy(taxonBase,Synonym.class);
							if (acceptedTaxon == null){
								String message = "Accepted/valid taxon could not be found. Please check referential integrity.";
								fireWarningEvent(message, state, 8);
							}else{
							    if (parentId != 0){
							        //if no relation was defined in file skip relationship creation
							        acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
							        getTaxonService().saveOrUpdate(acceptedTaxon);
							    }
							}
						} catch (Exception e) {
							String message = "Unhandled exception (%s) occurred during synonym import/update";
							message = String.format(message, e.getMessage());
							fireWarningEvent(message, state, 10);
							state.setUnsuccessfull();
						}
					}
					}else{
						acceptedTaxon = null;
						String message = "Unhandled name status (%s)";
						message = String.format(message, nameStatus);
						fireWarningEvent(message, state, 8);
					}
				}else{//taxonNameStr is empty
					//vernacular name case
					acceptedTaxon = parentTaxon;
					nameUsedInSource = null;
				}
			}

			if (acceptedTaxon == null && (StringUtils.isNotBlank(commonNameStr) ||taxonDataHolder.getFeatures().size() > 0 )){
				String message = "Accepted taxon could not be found. Can't add additional data (common names, descriptive data, ...) to taxon";
				fireWarningEvent(message, state, 6);
			}else{
				//common names
				if (StringUtils.isNotBlank(commonNameStr)){			// add common name to taxon
					handleCommonName(state, taxonNameStr, commonNameStr, acceptedTaxon);
				}


				//media
				for (String imageUrl : taxonDataHolder.getImages()){
					TaxonDescription td = acceptedTaxon.getImageGallery(true);
					DescriptionElementBase mediaHolder;
					if (td.getElements().size() != 0){
						mediaHolder = td.getElements().iterator().next();
					}else{
						mediaHolder = TextData.NewInstance(Feature.IMAGE());
						td.addElement(mediaHolder);
					}
					try {
						Media media = getImageMedia(imageUrl, READ_MEDIA_DATA);
						mediaHolder.addMedia(media);
					} catch (MalformedURLException e) {
						logger.warn("Can't add media: " + e.getMessage());
						state.setUnsuccessfull();
					}
				}

				//tdwg label
				for (String tdwg : taxonDataHolder.getDistributions()){
					TaxonDescription td = this.getTaxonDescription(acceptedTaxon, state.getConfig().getSourceReference() ,false, true);
					NamedArea area = TdwgAreaProvider.getAreaByTdwgAbbreviation(tdwg);
					if (area == null){
						area = TdwgAreaProvider.getAreaByTdwgLabel(tdwg);
					}
					if (area != null){
						Distribution distribution = Distribution.NewInstance(area, PresenceAbsenceTerm.PRESENT());
						td.addElement(distribution);
					}else{
						String message = "TDWG area could not be recognized: " + tdwg;
						logger.warn(message);
						state.setUnsuccessfull();
					}
				}

				//features
				handleFeatures(state, taxonDataHolder, acceptedTaxon, nameUsedInSource);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}


	/**
	 * @param state
	 * @param taxonDataHolder
	 * @param acceptedTaxon
	 */
	private void handleFeatures(TaxonExcelImportState state, NormalExplicitRow taxonDataHolder, Taxon acceptedTaxon, TaxonNameBase nameUsedInSource) {
		//feature
		for (UUID featureUuid : taxonDataHolder.getFeatures()){
			Feature feature = getFeature(state, featureUuid);
			List<String> textList = taxonDataHolder.getFeatureTexts(featureUuid);
			List<String> languageList = taxonDataHolder.getFeatureLanguages(featureUuid);

			for (int i = 0; i < textList.size(); i++){
				String featureText = textList.get(i);
				String featureLanguage = languageList == null ? null :languageList.get(i);
				Language language = getFeatureLanguage(featureLanguage, state);
				//TODO
				TaxonDescription td = this.getTaxonDescription(acceptedTaxon, state.getConfig().getSourceReference() ,false, true);
				TextData textData = TextData.NewInstance(feature);
				textData.putText(language, featureText);
				td.addElement(textData);

				SourceDataHolder sourceDataHolder = taxonDataHolder.getFeatureTextReferences(featureUuid, i);
				List<Map<SourceType, String>> sourceList = sourceDataHolder.getSources();
				for (Map<SourceType, String> sourceMap : sourceList){

					//ref
					Reference ref = ReferenceFactory.newGeneric();
					boolean refExists = false; //in case none of the ref fields exists, the ref should not be added
					for (SourceType type : sourceMap.keySet()){
						String value = sourceMap.get(type);
						if (type.equals(SourceType.Author)){
							TeamOrPersonBase<?> author = getAuthorAccordingToConfig(value, state);
							ref.setAuthorship(author);
						}else if (type.equals(SourceType.Title)) {
							ref.setTitle(value);
						}else if (type.equals(SourceType.Year)) {
							ref.setDatePublished(TimePeriodParser.parseString(value));
						}else if (type.equals(SourceType.RefExtension)) {
							ExtensionType extensionType = getExtensionType(state, uuidRefExtension, "RefExtension", "Reference Extension", "RefExt.");
							Extension extension = Extension.NewInstance(ref, value, extensionType);
						}
						refExists = true;
					}
					DescriptionElementSource source = DescriptionElementSource.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
					if (refExists){
						ref = getReferenceAccordingToConfig(ref, state);
						source.setCitation(ref);
						source.setNameUsedInSource(nameUsedInSource);
					}
					textData.addSource(source);
				}
			}
		}
	}

	private final Map<String, UUID> referenceMapping = new HashMap<String, UUID>();
	private final Map<UUID, Reference> referenceStore = new HashMap<UUID, Reference>();

	private Reference getReferenceAccordingToConfig(Reference value, TaxonExcelImportState state) {
		Reference result = null;
		String titleCache = value.getTitleCache();
		UUID referenceUuid = referenceMapping.get(titleCache);
		if (referenceUuid != null){
			result = referenceStore.get(referenceUuid);
		}
		if (result == null){
			result = value;
			referenceStore.put(result.getUuid(), result);
		}
		if (referenceUuid == null){
			referenceMapping.put(titleCache, result.getUuid());
		}
		return result;
	}


	private final Map<String, UUID> authorMapping = new HashMap<String, UUID>();
	private final Map<UUID, TeamOrPersonBase> authorStore = new HashMap<UUID, TeamOrPersonBase>();

	private TeamOrPersonBase<?> getAuthorAccordingToConfig(String value, TaxonExcelImportState state) {
		TeamOrPersonBase<?> result = null;
		UUID authorUuid = authorMapping.get(value);
		if (authorUuid != null){
			result = authorStore.get(authorUuid);
		}
		if (result == null){
			//TODO parsing
			TeamOrPersonBase<?> author = Team.NewInstance();
			author.setTitleCache(value, true);
			result = author;
			authorStore.put(result.getUuid(), result);
		}
		if (authorUuid == null){
			authorMapping.put(value, result.getUuid());
		}
		return result;
	}


	private final Map<String, UUID> languageMapping = new HashMap<String, UUID>();

	private Language getFeatureLanguage(String featureLanguage, TaxonExcelImportState state) {
		if (StringUtils.isBlank(featureLanguage)){
			return null;
		}
		UUID languageUuid = languageMapping.get(featureLanguage);
		if (languageUuid == null){
			Language result = getTermService().getLanguageByIso(featureLanguage);
			languageUuid = result.getUuid();
			languageMapping.put(featureLanguage, languageUuid);
		}
		Language result = getLanguage(state, languageUuid, null, null, null);
		return result;
	}


	/**
	 * @param state
	 * @param taxonNameStr
	 * @param commonNameStr
	 * @param parentId
	 */
	private void handleCommonName(TaxonExcelImportState state,
			String taxonNameStr, String commonNameStr, Taxon acceptedTaxon) {
		Language language = getTermService().getLanguageByIso(state.getCurrentRow().getLanguage());
		if (language == null && CdmUtils.isNotEmpty(state.getCurrentRow().getLanguage())  ){
			String error ="Language is null but shouldn't";
			logger.error(error);
			throw new IllegalArgumentException(error);
		}
		CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonNameStr, language);
		try {
			TaxonDescription taxonDescription = getTaxonDescription(acceptedTaxon, false, true);
			taxonDescription.addElement(commonTaxonName);
			logger.info("Common name " + commonNameStr + " added to " + acceptedTaxon.getTitleCache());
		} catch (ClassCastException ex) {
			logger.error(taxonNameStr + " is not a taxon instance.");
		}
	}


	/**
	 * @param state
	 * @param rank
	 * @param taxonNameStr
	 * @param authorStr
	 * @param nameStatus
	 * @param nameStatus2
	 * @return
	 */
	private TaxonBase createTaxon(TaxonExcelImportState state, Rank rank,
			String taxonNameStr, String authorStr, String reference, String date, String nameStatus) {
		// Create the taxon name object depending on the setting of the nomenclatural code
		// in the configurator (botanical code, zoological code, etc.)
		if (StringUtils.isBlank(taxonNameStr)){
			return null;
		}
		NomenclaturalCode nc = getConfigurator().getNomenclaturalCode();

		TaxonBase taxonBase;

		String titleCache = CdmUtils.concat(" ", taxonNameStr, authorStr);
		if (! synonymMarkers.contains(nameStatus)  && state.getConfig().isReuseExistingTaxaWhenPossible()){
			titleCache = CdmUtils.concat(" ", taxonNameStr, authorStr);
			taxonBase = getTaxonService().findBestMatchingTaxon(titleCache);
		}else{
			taxonBase = getTaxonService().findBestMatchingSynonym(titleCache);
			if (taxonBase != null){
				logger.info("Matching taxon/synonym found for " + titleCache);
			}
		}
		if (taxonBase != null){
			logger.info("Matching taxon/synonym found for " + titleCache);
			return null;
		}else {
			taxonBase = createTaxon(state, rank, taxonNameStr, authorStr, reference, date, nameStatus, nc);
		}
		return taxonBase;
	}




	/**
	 * @param state
	 * @param rank
	 * @param taxonNameStr
	 * @param authorStr
	 * @param nameStatus
	 * @param nameStatus2
	 * @param nc
	 * @return
	 */
	private TaxonBase<?> createTaxon(TaxonExcelImportState state, Rank rank, String taxonNameStr,
			String authorStr, String reference, String date, String nameStatus, NomenclaturalCode nc) {
		TaxonBase<?> taxonBase;
		NonViralName<?> taxonNameBase = null;
		if (nc == NomenclaturalCode.ICVCN){
			logger.warn("ICVCN not yet supported");

		}else{
			taxonNameBase =(NonViralName<?>) nc.getNewTaxonNameInstance(rank);
			//NonViralName nonViralName = (NonViralName)taxonNameBase;
			NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
			taxonNameBase = parser.parseFullName(taxonNameStr, nc, rank);

			if (! taxonNameBase.getNameCache().equals(taxonNameStr)){
				taxonNameBase.setNameCache(taxonNameStr, true);
			}

			// Create the author
			if (StringUtils.isNotBlank(authorStr)) {
				try {
					parser.parseAuthors(taxonNameBase, authorStr);
				} catch (StringNotParsableException e) {
					taxonNameBase.setAuthorshipCache(authorStr);
 				}
			}
			if (StringUtils.isNotBlank(reference)) {

			    INomenclaturalReference ref = parser.parseReferenceTitle(reference, date, true);
			    if (ref.getAuthorship() == null){
			        ref.setAuthorship(taxonNameBase.getCombinationAuthorship());
			    }
			    if (ref.getAbbrevTitle() == null){
			        ref.setAbbrevTitle(reference);
			    }
			    ref.setProtectedAbbrevTitleCache(false);
			    ref.setProtectedTitleCache(false);

			    taxonNameBase.setNomenclaturalReference(ref);
			}
		}

		//Create the taxon
		Reference sec = state.getConfig().getSourceReference();
		// Create the status
		nameStatus = CdmUtils.Nz(nameStatus).trim().toLowerCase();
		if (validMarkers.contains(nameStatus)){
			taxonBase = Taxon.NewInstance(taxonNameBase, sec);
		}else if (synonymMarkers.contains(nameStatus)){
			taxonBase = Synonym.NewInstance(taxonNameBase, sec);
		}else {
			Taxon taxon = Taxon.NewInstance(taxonNameBase, sec);
			if (nameStatusMarkers.contains(nameStatus)){
			    if (nameStatus.equals(NOM_ILLEG)){
			        taxonNameBase.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
			    } else if (nameStatus.equals(NOM_REJ)){
			        taxonNameBase.addStatus(NomenclaturalStatusType.REJECTED(), null, null);
			    } else if (nameStatus.equals(NOM_CONS)){
                    taxonNameBase.addStatus(NomenclaturalStatusType.CONSERVED(), null, null);
                }
			}else{
			    taxon.setTaxonStatusUnknown(true);
			}
			taxonBase = taxon;
		}
		return taxonBase;
	}

	/**
	 * @param taxon
	 * @return
	 */
	//TODO implementation must be improved when matching of taxon names with existing names is implemented
	//=> the assumption that the only description is the description added by this import
	//is wrong then
	private TaxonNameDescription getNameDescription(TaxonNameBase<?,?> name) {
		Set<TaxonNameDescription> descriptions = name.getDescriptions();
		if (descriptions.size()>1){
			throw new IllegalStateException("Implementation does not yet support names with multiple descriptions");
		}else if (descriptions.size()==1){
			return descriptions.iterator().next();
		}else{
			return TaxonNameDescription.NewInstance(name);
		}
	}

	private void makeParent(TaxonExcelImportState state, Taxon parentTaxon, Taxon childTaxon, Reference citation, String microCitation){
		Reference sec = state.getConfig().getSourceReference();

//		Reference sec = parentTaxon.getSec();
		Classification tree = state.getClassification();
		if (tree == null){
			//tree = makeTree(state, sec);
		    if (state.getConfig().getClassificationUuid() != null){
		        tree = getClassificationService().load(state.getConfig().getClassificationUuid());
		        state.setClassification(tree);
		    }
		    if (tree == null){
		        tree = makeTree(state, sec);
		        getClassificationService().save(tree);
		        state.setClassification(tree);
		    }
		}
		if (sec.equals(childTaxon.getSec())){
		    boolean success =  (null !=  tree.addParentChild(parentTaxon, childTaxon, citation, microCitation));
			if (success == false){
				state.setUnsuccessfull();
			}
		}else{
			logger.warn("No relationship added for child " + childTaxon.getTitleCache());
		}
		return;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelTaxonOrSpecimenImportBase#createDataHolderRow()
	 */
	@Override
	protected NormalExplicitRow createDataHolderRow() {
		return new NormalExplicitRow();
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(TaxonExcelImportState state) {
		logger.warn("DoCheck not yet implemented for NormalExplicitImport");
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(TaxonExcelImportState state) {
		return false;
	}



}
