/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ItemFilter;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.MappedCdmBase;
import eu.etaxonomy.cdm.io.stream.PartitionableConverterBase;
import eu.etaxonomy.cdm.io.stream.StreamImportBase;
import eu.etaxonomy.cdm.io.stream.StreamImportStateBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class  DwcTaxonStreamItem2CdmTaxonConverter<CONFIG extends DwcaDataImportConfiguratorBase, STATE extends StreamImportStateBase<CONFIG, StreamImportBase>>
        extends PartitionableConverterBase<CONFIG, STATE>
        implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>, ItemFilter<StreamItem> {

    private static final Logger logger = Logger.getLogger(DwcTaxonStreamItem2CdmTaxonConverter.class);

    //if this converter is used as filter we may not want to delete item parts during evaluation
    boolean isFilterOnly = false;

    private static final String ID = "id";
	// temporary key for the case that no dataset information is supplied, TODO use something better
	public static final String NO_DATASET = "no_dataset_jli773oebhjklw";

	private final NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();

	/**
	 * @param state
	 */
	public DwcTaxonStreamItem2CdmTaxonConverter(STATE state) {
		super(state);
	}

    public DwcTaxonStreamItem2CdmTaxonConverter(STATE state, boolean isFilter) {
        super(state);
        this.isFilterOnly = isFilter;
    }

    @Override
    public boolean toBeRemovedFromStream(StreamItem item) {
        if (!config.isDoSplitRelationshipImport()){
            return false;
        }else{
            if (isSynonym(item)){
                return ! this.config.isDoSynonymRelationships();
            }else{
                NomenclaturalCode nomCode = getNomCode(item);
                Rank rank = getRank(item, nomCode);
                boolean isHigherRank = rank == null || rank.isHigher(Rank.SPECIES());
                if (isHigherRank){
                    return ! config.isDoHigherRankRelationships();
                }else{
                    return ! config.isDoLowerRankRelationships();
                }
            }
        }
    }

    private boolean isSynonym(StreamItem item) {
        TaxonBase<?> taxonBase = getTaxonBase(item);
        return taxonBase instanceof Synonym;
    }

	@Override
    public IReader<MappedCdmBase<? extends CdmBase>> map(StreamItem csvTaxonRecord){
		List<MappedCdmBase<? extends CdmBase>> resultList = new ArrayList<>();

		//TODO what if not transactional?
		Reference sourceReference = state.getTransactionalSourceReference();
		String sourceReferenceDetail = null;

		//taxon
		TaxonBase<?> taxonBase = getTaxonBase(csvTaxonRecord);
		MappedCdmBase<TaxonBase<?>>  mcb = new MappedCdmBase<>(csvTaxonRecord.term, csvTaxonRecord.get(ID), taxonBase);
		resultList.add(mcb);

		//original source
		String id = csvTaxonRecord.get(ID);
		IdentifiableSource source = taxonBase.addSource(OriginalSourceType.Import, id, "Taxon", sourceReference, sourceReferenceDetail);
		MappedCdmBase<IdentifiableSource> mappedSource = new MappedCdmBase<>(csvTaxonRecord.get(ID), source);
		resultList.add(mappedSource);
		csvTaxonRecord.remove(ID);

		//rank
		NomenclaturalCode nomCode = getNomCode(csvTaxonRecord);
		Rank rank = getRank(csvTaxonRecord, nomCode);

		//name && name published in
		TaxonName name = getScientificName(csvTaxonRecord, nomCode, rank, resultList, sourceReference);
		taxonBase.setName(name);

		//nameAccordingTo
		MappedCdmBase<Reference> sec = getNameAccordingTo(csvTaxonRecord, resultList);

		if (sec == null && state.getConfig().isUseSourceReferenceAsSec()){
			sec = new MappedCdmBase<>(state.getTransactionalSourceReference());
		}
		if (sec != null){
			taxonBase.setSec(sec.getCdmBase());
		}

		//classification
		handleDataset(csvTaxonRecord, taxonBase, resultList, sourceReference, sourceReferenceDetail);

		//NON core
	    //term="http://purl.org/dc/terms/identifier"
		//currently only LSIDs or generic
		handleIdentifier(csvTaxonRecord, taxonBase);

		//TaxonRemarks
		handleTaxonRemarks(csvTaxonRecord, taxonBase);

		//TDWG_1
		handleTdwgArea(csvTaxonRecord, taxonBase);

		//VernecularName
		handleCommonNames(csvTaxonRecord, taxonBase);

		//External Sources, ID's and References
		handleIdentifiableObjects(csvTaxonRecord, taxonBase);


		//		    <!-- Top level group; listed as kingdom but may be interpreted as domain or superkingdom
//		         The following eight groups are recognized: Animalia, Archaea, Bacteria, Chromista,
//		         Fungi, Plantae, Protozoa, Viruses -->
//		    <field index='10' term='http://rs.tdwg.org/dwc/terms/kingdom'/>

//		    <!-- Phylum in which the taxon has been classified -->
//		    <field index='11' term='http://rs.tdwg.org/dwc/terms/phylum'/>

		//		    <!-- Class in which the taxon has been classified -->
//		    <field index='12' term='http://rs.tdwg.org/dwc/terms/class'/>

		//		    <!-- Order in which the taxon has been classified -->
//		    <field index='13' term='http://rs.tdwg.org/dwc/terms/order'/>

		//		    <!-- Family in which the taxon has been classified -->
//		    <field index='14' term='http://rs.tdwg.org/dwc/terms/family'/>

		//		    <!-- Genus in which the taxon has been classified -->
//		    <field index='15' term='http://rs.tdwg.org/dwc/terms/genus'/>

		//		    <!-- Subgenus in which the taxon has been classified -->
//		    <field index='16' term='http://rs.tdwg.org/dwc/terms/subgenus'/>
//		    <!-- Specific epithet; for hybrids, the multiplication symbol is included in the epithet -->

//		    <field index='17' term='http://rs.tdwg.org/dwc/terms/specificEpithet'/>
//		    <!-- Infraspecific epithet -->

//		    <field index='18' term='http://rs.tdwg.org/dwc/terms/infraspecificEpithet'/>
//		    <!-- Authorship -->

//		    <field index='19' term='http://rs.tdwg.org/dwc/terms/scientificNameAuthorship'/>
//		==> see scientific name
//
//		<!-- Acceptance status published in -->
//		    <field index='20' term='http://purl.org/dc/terms/source'/>
//		    <!-- Reference in which the scientific name was first published -->
//		    <field index='21' term='http://rs.tdwg.org/dwc/terms/namePublishedIn'/>
//		    <!-- Taxon scrutinized by -->
//		    <field index='22' term='http://rs.tdwg.org/dwc/terms/nameAccordingTo'/> 
//		    <!-- Scrutiny date -->
//		    <field index='23' term='http://purl.org/dc/terms/modified'/>
//		    <!-- Additional data for the taxon -->
//		    <field index='24' term='http://purl.org/dc/terms/description'/>
//		    </core>

		handleModified(csvTaxonRecord, taxonBase);

		handleIsExtinct(csvTaxonRecord, taxonBase);



		return new ListReader<>(resultList);
	}



    /**
     * @param csvTaxonRecord
     * @param taxonBase
     */
    private void handleIsExtinct(StreamItem item, TaxonBase<?> taxonBase) {
        String isExtinctStr = item.get(TermUri.GBIF_IS_EXTINCT);
        if (isBlank(isExtinctStr)){
            return;
        }
        Boolean isExtinct = getBoolean(isExtinctStr, item);
        if (isExtinct != null){
            try {
                UUID isExtinctUuid = state.getTransformer().getMarkerTypeUuid("isExtinct");
                MarkerType markerType = state.getCurrentIO().getMarkerType(state, isExtinctUuid, "extinct", "extinct", "extinct");
                Marker.NewInstance(taxonBase, isExtinct, markerType);

            } catch (UndefinedTransformerMethodException e) {
                String message = "GetMarkerType not available for import. This should not happen. Please conntact developer";
                fireWarningEvent(message, item.getLocation(), 8);
            }
        }

    }

    /**
     * @param item
     * @param isExtinctStr
     * @return
     */
    private Boolean getBoolean(String booleanStr, StreamItem item) {
        try {
            return Boolean.valueOf(booleanStr);
        } catch (Exception e) {
            String message = "Boolean value could not be parsed";
            fireWarningEvent(message, item, 4);
            return null;
        }
    }



    /**
     * @param csvTaxonRecord
     * @param taxonBase
     */
    private void handleModified(StreamItem item, TaxonBase<?> taxonBase) {
        String modifiedStr = item.get(TermUri.DC_MODIFIED);
        if (isBlank(modifiedStr)){
            return;
        }

        try {
            UUID modifiedUuid = state.getTransformer().getExtensionTypeUuid("modified");
            ExtensionType extensionType = state.getCurrentIO().getExtensionType(state, modifiedUuid, "modified", "modified", "modified");
            Extension.NewInstance(taxonBase, modifiedStr, extensionType);

        } catch (UndefinedTransformerMethodException e) {
            String message = "GetMarkerType not available for import. This should not happen. Please conntact developer";
            fireWarningEvent(message, item.getLocation(), 8);
        }


    }

    /**
	 * @param item
	 * @param taxonBase
	 */
	private void handleIdentifiableObjects(StreamItem item,TaxonBase<?> taxonBase) {

		String references = item.get(TermUri.DC_REFERENCES);

		if (references == null || references == "") {
			references = item.get(TermUri.DWC_NAME_PUBLISHED_IN_ID);//lorna temporary until Scratchpads move the reference to the correct place.
		}

		if (StringUtils.isNotBlank(references)){
			URI uri = makeUriIfIs(references);
			if (uri != null){
				Extension.NewInstance(taxonBase, references, ExtensionType.URL());
			}else{
				String message = "Non-URI Dublin Core References not yet handled for taxa. References is: %s";
				fireWarningEvent(String.format(message, references), item, 6);
			}
		}


		//TODO: Finish properly
		String id = item.get(TermUri.CDM_SOURCE_IDINSOURCE);
		String idNamespace = item.get(TermUri.CDM_SOURCE_IDNAMESPACE);
		String reference = item.get(TermUri.CDM_SOURCE_REFERENCE);
		if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(idNamespace) && StringUtils.isNotBlank(reference)){
			Reference ref = ReferenceFactory.newGeneric();
			ref.setTitle(reference);
			Taxon taxon = (Taxon) taxonBase;
			taxon.addSource(OriginalSourceType.Import, id, idNamespace, ref, null);
		}

	}


	/**
	 * If str is an uri it returns is as an {@link URI}. If not it returns <code>null</code>.
	 * @param str
	 * @return the URI.
	 */
	private URI makeUriIfIs(String str) {
		if (! str.startsWith("http:")){
			return null;
		}else{
			try {
				URI uri = URI.create(str);
				return uri;
			} catch (Exception e) {
				return null;
			}
		}

	}


	/**
	 * @param item
	 * @param taxonBase
	 */
	private void handleCommonNames(StreamItem item,TaxonBase<?> taxonBase) {
		//TODO: handle comma separated values
		String commonName = item.get(TermUri.DWC_VERNACULAR_NAME);
		if (StringUtils.isNotBlank(commonName)){

			Language language = getLanguage(item);
			CommonTaxonName commonTaxonName = CommonTaxonName.NewInstance(commonName, language);
			if(taxonBase instanceof Taxon){
				Taxon taxon = (Taxon) taxonBase;
				TaxonDescription taxonDescription = getTaxonDescription(taxon, false);
				taxonDescription.addElement(commonTaxonName);
				logger.info("Common name " + commonName + " added to " + taxon.getTitleCache());
			}
		}
	}



	/**
	 * @param csvTaxonRecord
	 * @param taxonBase
	 */
	private void handleTdwgArea(StreamItem item, TaxonBase<?> taxonBase) {
		String tdwg_area = item.get(TermUri.DWC_COUNTRY_CODE);
		if (tdwg_area != null){
    		if(taxonBase instanceof Synonym){
    			Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
    			Taxon acceptedTaxon = synonym.getAcceptedTaxon();
    			if (acceptedTaxon != null){
    			    TaxonDescription td = getTaxonDescription(acceptedTaxon, false);
    			    NamedArea area = NamedArea.getAreaByTdwgAbbreviation(tdwg_area);

    			    if (area == null){
    			        area = NamedArea.getAreaByTdwgLabel(tdwg_area);
    			    }
    			    if (area != null){
    			        Distribution distribution = Distribution.NewInstance(area, PresenceAbsenceTerm.PRESENT());
    			        td.addElement(distribution);
    			    }
    			}
    		}
    		if(!(taxonBase instanceof Synonym)){
    			Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
    			TaxonDescription td = getTaxonDescription(taxon, false);
    			NamedArea area = NamedArea.getAreaByTdwgAbbreviation(tdwg_area);

    			if (area == null){
    				area = NamedArea.getAreaByTdwgLabel(tdwg_area);
    			}
    			if (area != null){
    				Distribution distribution = Distribution.NewInstance(area, PresenceAbsenceTerm.PRESENT());
    				td.addElement(distribution);
    			}
    		}
    	}
	}


	/**
	 * @param item
	 * @param taxonBase
	 */
	private void handleTaxonRemarks(StreamItem item,TaxonBase<?> taxonBase) {
		String comment = item.get(TermUri.DWC_TAXON_REMARKS);
		Language language = getLanguage(item);
		if(StringUtils.isNotBlank(comment)){
				Annotation annotation = Annotation.NewInstance(comment, language);
				taxonBase.addAnnotation(annotation);
		}else{
//			String message = "Comment is empty or some error appeared while saving: %s";
////			message = String.format(message);
//			fireWarningEvent(message, item, 1);
		}
	}


	//TODO handle non LSIDs
	//TODO handle LSIDs for names
	private void handleIdentifier(StreamItem csvTaxonRecord, TaxonBase<?> taxonBase) {
		String identifier = csvTaxonRecord.get(TermUri.DC_IDENTIFIER);
		if (StringUtils.isNotBlank(identifier)){
			if (identifier.trim().startsWith("urn:lsid")){
				try {
					LSID lsid = new LSID(identifier);
					taxonBase.setLsid(lsid);
				} catch (MalformedLSIDException e) {
					String message = "LSID is malformed and can't be handled as LSID: %s";
					message = String.format(message, identifier);
					fireWarningEvent(message, csvTaxonRecord, 4);
					Identifier.NewInstance(taxonBase, identifier, DefinedTerm.getTermByClassAndUUID(DefinedTerm.class, DefinedTerm.uuidLsid));
				}
			}else{
				Identifier.NewInstance(taxonBase, identifier, null);
			    String message = "Identifier type not recognized. Create generic identifier: %s";
				message = String.format(message, identifier);
				fireWarningEvent(message, csvTaxonRecord, 1);
			}
		}

	}


	private void handleDataset(StreamItem item, TaxonBase<?> taxonBase,
	        List<MappedCdmBase<? extends CdmBase>> resultList,
	        Reference sourceReference,
	        String sourceReferecenDetail) {

		TermUri idTerm = TermUri.DWC_DATASET_ID;
		TermUri strTerm = TermUri.DWC_DATASET_NAME;

		if (config.isDatasetsAsClassifications()){
			String datasetId = CdmUtils.Nz(item.get(idTerm)).trim();
			String datasetName = CdmUtils.Nz(item.get(strTerm)).trim();
				if (CdmUtils.areBlank(datasetId, datasetName) ){
				datasetId = NO_DATASET;
			}

			//check id
			boolean classificationExists = state.exists(idTerm.toString() , datasetId, Classification.class);

			//check name
			if (!classificationExists){
				classificationExists = state.exists(strTerm.toString() , datasetName, Classification.class);
			}

			//if not exists, create new
			if (! classificationExists){
				String classificationName = StringUtils.isBlank(datasetName)? datasetId : datasetName;
				if (classificationName.equals(NO_DATASET)){
					classificationName = config.getClassificationName();
					//classificationName = "Classification (no name)";  //TODO define by config or zipfile or metadata
				}

				String classificationId = StringUtils.isBlank(datasetId)? datasetName : datasetId;
				Classification classification = Classification.NewInstance(classificationName);
				//source
				IdentifiableSource source = classification.addSource(OriginalSourceType.Import, classificationId, "Dataset", sourceReference, sourceReferecenDetail);
				//add to result
				resultList.add(new MappedCdmBase<>(idTerm, datasetId, classification));
				resultList.add(new MappedCdmBase<>(strTerm, datasetName, classification));
				resultList.add(new MappedCdmBase<>(source));
				//TODO this is not so nice but currently necessary as classifications are requested in the same partition
				state.putMapping(idTerm.toString(), classificationId, classification);
				state.putMapping(strTerm.toString(), classificationName, classification);
			}
		}else if (config.isDatasetsAsSecundumReference() || config.isDatasetsAsOriginalSource()){
			MappedCdmBase<Reference> mappedCitation = getReference(item, resultList, idTerm, strTerm, true);
			if (mappedCitation != null){
				Reference ref = mappedCitation.getCdmBase();
				if (config.isDatasetsAsSecundumReference()){
					//dataset as secundum reference
					taxonBase.setSec(ref);
				}else{
					//dataset as original source
					taxonBase.addSource(OriginalSourceType.Import, null, null, ref, null);
				}
			}
		}else{
			String message = "DatasetUse type not yet implemented. Can't import dataset information.";
			fireWarningEvent(message, item, 4);
		}

		//remove to later check if all attributes were used
		removeItemInfo(item, idTerm);
		removeItemInfo(item, strTerm);
	}


	@Override
	public String getSourceId(StreamItem item) {
		String id = item.get(ID);
		return id;
	}

	private MappedCdmBase<Reference> getNameAccordingTo(StreamItem item, List<MappedCdmBase<? extends CdmBase>> resultList) {
		if (config.isDatasetsAsSecundumReference()){
			//TODO store nameAccordingTo info some where else or let the user define where to store it.
			return null;
		}else{
			TermUri idTerm = TermUri.DWC_NAME_ACCORDING_TO_ID;
			TermUri strTerm = TermUri.DWC_NAME_ACCORDING_TO;
			MappedCdmBase<Reference> secRef = getReference(item, resultList, idTerm, strTerm, false);
			return secRef;
		}
	}

	private NomenclaturalCode getNomCode(StreamItem item) {
		String strNomCode = getValue(item, TermUri.DWC_NOMENCLATURAL_CODE);
		NomenclaturalCode nomCode = null;
		// by Nomcenclatural Code
		if (strNomCode != null){
			nomCode = NomenclaturalCode.fromString(strNomCode);
			if (nomCode == null){
				String message = "NomCode '%s' not recognized";
				message = String.format(message, strNomCode);
				fireWarningEvent(message, item, 4);
			}else{
				return nomCode;
			}
		}
		// by Kingdom
		String strKingdom = getValue(item, TermUri.DWC_KINGDOM);
		if (strKingdom != null){
			if (strKingdom.equalsIgnoreCase("Plantae")){
				nomCode = NomenclaturalCode.ICNAFP;
			}else if (strKingdom.equalsIgnoreCase("Fungi")){
				nomCode = NomenclaturalCode.ICNAFP;
			}else if (strKingdom.equalsIgnoreCase("Animalia")){
				nomCode = NomenclaturalCode.ICZN;
			}else if (strKingdom.equalsIgnoreCase("Protozoa")){
				nomCode = NomenclaturalCode.ICZN;
			}
		}

		//TODO further kingdoms
		if (nomCode == null){
			//TODO warning
			if (config.getNomenclaturalCode() != null){
				nomCode = config.getNomenclaturalCode();
			}
		}
		return nomCode;
	}


	private TaxonName getScientificName(StreamItem item, NomenclaturalCode nomCode, Rank rank, List<MappedCdmBase<? extends CdmBase>> resultList, Reference sourceReference) {
		TaxonName name = null;
		String strScientificName = getValue(item, TermUri.DWC_SCIENTIFIC_NAME);
		//Name
		if (strScientificName != null){
			name = (TaxonName)parser.parseFullName(strScientificName, nomCode, rank);
			if ( rank != null && name != null && name.getRank() != null &&  ! rank.equals(name.getRank())){
				if (config.isValidateRankConsistency()){
					String message = "Parsed rank %s (%s) differs from rank %s given by fields 'taxonRank' or 'verbatimTaxonRank'";
					message = String.format(message, name.getRank().getTitleCache(), strScientificName, rank.getTitleCache());
					fireWarningEvent(message, item, 4);
				}
			}
			checkAuthorship(name, item);
			resultList.add(new MappedCdmBase(TermUri.DWC_SCIENTIFIC_NAME, strScientificName, name));
		}
		//By ID
		String strScientificNameId = getValue(item, TermUri.DWC_SCIENTIFIC_NAME_ID);
		if (strScientificNameId != null){
			if (config.isScientificNameIdAsOriginalSourceId()){
				if (name != null){
					IdentifiableSource source = IdentifiableSource.NewInstance(OriginalSourceType.Import, strScientificNameId, TermUri.DWC_SCIENTIFIC_NAME_ID.toString(), sourceReference, null);
					name.addSource(source);
				}
			}else{
				String message = "ScientificNameId not yet implemented: '%s'";
				message = String.format(message, strScientificNameId);
				fireWarningEvent(message, item, 4);
			}
		}

		//namePublishedIn
		TermUri idTerm = TermUri.DWC_NAME_PUBLISHED_IN_ID;
		TermUri strTerm = TermUri.DWC_NAME_PUBLISHED_IN;
		MappedCdmBase<Reference> nomRef = getReference(item, resultList, idTerm, strTerm, false);

		if (name != null){
			if (nomRef != null){
				name.setNomenclaturalReference(nomRef.getCdmBase());  //check if name already has a nomRef, shouldn't be the case usually
			}
		}else{
			if (nomRef != null){
				String message = "NamePublishedIn information available but no name exists";
				fireWarningEvent(message, item, 4);
			}
		}
		return name;
	}


	/**
	 * General method to handle references used for multiple attributes.
	 * @param item
	 * @param resultList
	 * @param idTerm
	 * @param strTerm
	 * @param idIsInternal
	 * @return
	 */
	private MappedCdmBase<Reference> getReference(StreamItem item,
	        List<MappedCdmBase<? extends CdmBase>> resultList, TermUri idTerm,
	        TermUri strTerm, boolean idIsInternal) {
		Reference newRef = null;
		Reference sourceCitation = null;

		MappedCdmBase<Reference> result = null;
		if (exists(idTerm, item) || exists(strTerm, item)){
			String refId = CdmUtils.Nz(item.get(idTerm)).trim();
			String refStr = CdmUtils.Nz(item.get(strTerm)).trim();
			if (StringUtils.isNotBlank(refId)){
				List<Reference> references = state.get(idTerm.toString(), refId, Reference.class);
				if (references.size() == 0){
					if (! idIsInternal){
						//references should already exist in store if not linking to external links like URLs
						String message = "External namePublishedInIDs are not yet supported";
						fireWarningEvent(message, item, 4);//set to DEBUG
					}else{
						newRef = ReferenceFactory.newGeneric();  //TODO handle other types if possible
						newRef.addSource(OriginalSourceType.Import, refId, idTerm.toString(), sourceCitation, null);
						MappedCdmBase<Reference> idResult = new MappedCdmBase<>(idTerm, refId, newRef);
						resultList.add(idResult);
					}
				}else{
					//TODO handle list.size > 1 , do we need a list here ?
					result = new MappedCdmBase<Reference>(idTerm, refId , references.get(0));
				}
			}
			if (result == null){
				List<Reference> nomRefs = state.get(strTerm.toString(), refStr, Reference.class);
				if (nomRefs.size() > 0){
					//TODO handle list.size > 1 , do we need a list here ?
					result = new MappedCdmBase<>(strTerm, refStr , nomRefs.get(0));
				}else{
					// new Reference
					if (newRef == null){
						newRef = ReferenceFactory.newGeneric();  //TODO handle other types if possible
					}
					newRef.setTitleCache(refStr, true);
					//TODO distinguish available year, authorship, etc. if
					result = new MappedCdmBase<>(strTerm, refStr, newRef);
					resultList.add(result);
				}
			}
		}
		return result;
	}


	//TODO we may configure in configuration that scientific name never includes Authorship
	private void checkAuthorship(TaxonName nameBase, StreamItem item) {
		if (nameBase.isViral()){
			return;
		}
		String strAuthors = getValue(item, TermUri.DWC_SCIENTIFIC_NAME_AUTHORS);

		if (! nameBase.isProtectedTitleCache()){
			if (isBlank(nameBase.getAuthorshipCache())){
				if (nameBase.isBotanical() || nameBase.isZoological()){
					//TODO can't we also parse NonViralNames correctly ?
					try {
						parser.parseAuthors(nameBase, strAuthors);
					} catch (StringNotParsableException e) {
					    nameBase.setAuthorshipCache(strAuthors);
					}
				}else{
				    nameBase.setAuthorshipCache(strAuthors);
				}
				//TODO throw warning (scientific name should always include authorship) by DwC definition
			}
		}

	}


	private Rank getRank(StreamItem csvTaxonRecord, NomenclaturalCode nomCode) {
		boolean USE_UNKNOWN = true;
		Rank rank = null;
		String strRank = getValue(csvTaxonRecord,TermUri.DWC_TAXON_RANK);
		String strVerbatimRank = getValue(csvTaxonRecord,TermUri.DWC_VERBATIM_TAXON_RANK);
		if (strRank != null){
			try {
				rank = Rank.getRankByEnglishName(strRank, nomCode, USE_UNKNOWN);
				if (rank.equals(Rank.UNKNOWN_RANK())){
					rank = Rank.getRankByNameOrIdInVoc(strRank, USE_UNKNOWN);
					if (rank.equals(Rank.UNKNOWN_RANK())){
						String message = "Rank can not be defined for '%s'";
						message = String.format(message, strRank);
						fireWarningEvent(message, csvTaxonRecord, 4);
					}
				}
			} catch (UnknownCdmTypeException e) {
				//should not happen as USE_UNKNOWN is used
				rank = Rank.UNKNOWN_RANK();
			}
		}
		if ( (rank == null || rank.equals(Rank.UNKNOWN_RANK())) && strVerbatimRank != null){
			try {
				rank = Rank.getRankByNameOrIdInVoc(strVerbatimRank, USE_UNKNOWN);
				if (rank.equals(Rank.UNKNOWN_RANK())){
					String message = "Rank can not be defined for '%s'";
					message = String.format(message, strVerbatimRank);
					fireWarningEvent(message, csvTaxonRecord, 4);
				}
			} catch (UnknownCdmTypeException e) {
				//should not happen as USE_UNKNOWN is used
				rank = Rank.UNKNOWN_RANK();
			}
		}
		return rank;
	}


	/**
	 * Creates an empty taxon object with a given status.
	 * <i>Empty</i> taxon means, without a defined name or sec.
	 * @param item
	 * @return
	 */
	private TaxonBase<?> getTaxonBase(StreamItem item) {
		TaxonName name = null;
		Reference sec = null;
		TaxonBase<?> result;
		String taxStatus = item.get(TermUri.DWC_TAXONOMIC_STATUS);
		String status = "";

		if (taxStatus != null){
			if (taxStatus.matches("accepted.*|valid")){
				status += "A";
			} else if (taxStatus.matches(".*synonym|invalid|not accepted")){   //not accepted comes from scratchpads
				status += "S";
			} else if (taxStatus.matches("misapplied.*")){
				status += "M";
			} else{
				status += "?";
			}
			removeItemInfo(item, TermUri.DWC_TAXONOMIC_STATUS);
		}
		if (! CdmUtils.isBlank(item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID))){
			// acceptedNameUsageId = id
			if (getSourceId(item).equals(item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID))){
				status += "A";
			}else{
				status += "S";
			}
		}
		if (status.contains("A") || status.contains("M")){
			result = Taxon.NewInstance(name, sec);
			if (status.contains("S") && ! status.contains("M") ){
				String message = "Ambigous taxon status (%s)";
				message = String.format(message, status);
				fireWarningEvent(message, item, 6);
			}
		} else if (status.contains("S")){
			result = Synonym.NewInstance(name, sec);
		} else{
			result = Taxon.NewUnknownStatusInstance(name, sec);
		}

		return result;

	}



    /**
	 * @param item
	 * @return
	 */
	private Language getLanguage(StreamItem item) {
		String langItem = item.get(TermUri.DC_LANGUAGE);
		Language language = null;

		if(StringUtils.equalsIgnoreCase(langItem, "de")){
			language = Language.GERMAN();
		}else if(StringUtils.equalsIgnoreCase(langItem, "en")){
			language = Language.ENGLISH();
		}else{
			language = Language.DEFAULT();
		}
		return language;
	}

// ********************** PARTITIONABLE ****************************************/


	@Override
	protected void makeForeignKeysForItem(StreamItem item, Map<String, Set<String>> fkMap) {
		String value;
		String key;

		//namePublishedIn
		if ( hasValue(value = item.get(key = TermUri.DWC_NAME_PUBLISHED_IN_ID.toString()))){
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if (config.isDeduplicateNamePublishedIn()){
			if ( hasValue(value = item.get(key = TermUri.DWC_NAME_PUBLISHED_IN.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
			}
		}

		//nameAccordingTo
		if (! config.isDatasetsAsSecundumReference()){
			if ( hasValue(value = item.get(key = TermUri.DWC_NAME_ACCORDING_TO_ID.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
			}
			if ( hasValue(value = item.get(key = TermUri.DWC_NAME_ACCORDING_TO.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
			}
		}

		//dataset
		if ( hasValue(value = item.get(key = TermUri.DWC_DATASET_ID.toString()))){
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if ( hasValue(value = item.get(key = TermUri.DWC_DATASET_NAME.toString()))){
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}

	}


	@Override
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<>();
 		result.add(TermUri.DWC_NAME_PUBLISHED_IN_ID.toString());
 		result.add(TermUri.DWC_NAME_PUBLISHED_IN.toString());
 		if (!config.isDatasetsAsSecundumReference()){
	 		result.add(TermUri.DWC_NAME_ACCORDING_TO_ID.toString());
	 		result.add(TermUri.DWC_NAME_ACCORDING_TO.toString());
 		}
	 	result.add(TermUri.DWC_DATASET_ID.toString());
	 	result.add(TermUri.DWC_DATASET_NAME.toString());
	 	return result;
	}


    /**
     * @param item
     * @param dwcTaxonomicStatus
     */
    private void removeItemInfo(StreamItem item, TermUri dwcTaxonomicStatus) {
        if (!isFilterOnly){
            item.remove(dwcTaxonomicStatus);
        }
    }


//** ***************************** TO STRING *********************************************/

	@Override
	public String toString(){
		return this.getClass().getName();
	}
}
