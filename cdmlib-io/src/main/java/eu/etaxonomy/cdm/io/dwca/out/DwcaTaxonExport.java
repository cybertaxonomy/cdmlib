/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * @author a.mueller
 * @since 18.04.2011
 */
public class DwcaTaxonExport extends DwcaDataExportBase {
    private static final long serialVersionUID = -3770976064909193441L;

    private static final Logger logger = Logger.getLogger(DwcaTaxonExport.class);

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
	protected static final String fileName = "coreTax.txt";

	private DwcaMetaDataRecord metaRecord;

	/**
	 * Constructor
	 */
	public DwcaTaxonExport(DwcaTaxExportState state) {
        super();
        this.ioName = this.getClass().getSimpleName();
        metaRecord = new DwcaMetaDataRecord(IS_CORE, fileName, ROW_TYPE);
        state.addMetaRecord(metaRecord);
        file = DwcaTaxExportFile.TAXON;
	}

    @Override
    protected void doInvoke(DwcaTaxExportState state){}

    @Override
    protected void handleTaxonNode(DwcaTaxExportState state, TaxonNode node)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        try {
            Taxon taxon = CdmBase.deproxy(node.getTaxon());
            DwcaTaxExportConfigurator config = state.getConfig();
            DwcaTaxonRecord record = new DwcaTaxonRecord(metaRecord, config);

            TaxonName name = taxon.getName();
            Taxon parent = node.getParent() == null ? null : node.getParent().getTaxon();
            TaxonName basionym = name.getBasionym();
            Classification classification = node.getClassification();
            if (! state.recordExists(file, taxon)){
            	handleTaxonBase(state, record, taxon, name, taxon, parent, basionym, classification, null, false, false);
            	PrintWriter writer = createPrintWriter(state, file);
                record.write(state, writer);
            	state.addExistingRecord(file, taxon);
            }


            //synonyms
            if (state.getConfig().isDoSynonyms()){
                handleSynonyms(state, taxon, file, classification, metaRecord);
            }

            //misapplied names
            if (state.getConfig().isDoMisappliedNames()){
                handleMisapplication(state, taxon, file, classification, metaRecord);
            }

        } catch (Exception e) {
            String message = "Unexpected exception: " + e.getMessage();
            state.getResult().addException(e, message);
        }finally{
            flushWriter(state, file);

        }
    }



	private void handleSynonyms(DwcaTaxExportState state, Taxon taxon, DwcaTaxExportFile file,
	        Classification classification, DwcaMetaDataRecord metaRecord) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		for (Synonym synonym :taxon.getSynonyms() ){
           if (isUnpublished(state.getConfig(), synonym)){
               return;
           }

		    DwcaTaxonRecord record = new DwcaTaxonRecord(metaRecord, state.getConfig());
			SynonymType type = synonym.getType();
			boolean isProParte = synonym.isProParte();
			boolean isPartial = synonym.isPartial();
			if (type == null){ // should not happen
				type = SynonymType.SYNONYM_OF();
			}
			TaxonName name = synonym.getName();
			//????
			Taxon parent = null;
			TaxonName basionym = name.getBasionym();

			if (! state.recordExists(file, synonym)){
				handleTaxonBase(state, record, synonym, name, taxon, parent, basionym, classification, type, isProParte, isPartial);
				PrintWriter writer = createPrintWriter(state, file);
				record.write(state, writer);
				state.addExistingRecord(file, synonym);
			}
		}
	}

	private void handleMisapplication(DwcaTaxExportState state, Taxon taxon,
	        DwcaTaxExportFile file, Classification classification, DwcaMetaDataRecord metaRecord) throws FileNotFoundException, UnsupportedEncodingException, IOException {

	    Set<TaxonRelationship> misappliedNamesRels = taxon.getMisappliedNameRelations();
		for (TaxonRelationship misappliedNameRel : misappliedNamesRels ){
			DwcaTaxonRecord record = new DwcaTaxonRecord(metaRecord, state.getConfig());
			Taxon misappliedName = misappliedNameRel.getFromTaxon();
			TaxonName name = misappliedName.getName();
			//????
			Taxon parent = null;
			TaxonName basionym = name.getBasionym();

			if (! state.recordExists(file, misappliedName)){
				handleTaxonBase(state, record, misappliedName, name, taxon, parent, basionym, classification,
				        misappliedNameRel.getType(), false, false);
				PrintWriter writer = createPrintWriter(state, file);
                record.write(state, writer);
				state.addExistingRecord(file, misappliedName);
			}
		}
	}

	/**
	 * @param state
	 * @param record
	 * @param taxonBase
	 * @param name
	 * @param acceptedTaxon
	 * @param parent
	 * @param basionym
	 * @param isPartial
	 * @param isProParte
	 * @param config
	 * @param type
	 */
	private void handleTaxonBase(DwcaTaxExportState state, DwcaTaxonRecord record, TaxonBase<?> taxonBase, TaxonName name,
			Taxon acceptedTaxon, Taxon parent, TaxonName basionym, Classification classification,
			RelationshipTermBase<?> relType, boolean isProParte, boolean isPartial) {
		record.setId(taxonBase.getId());
		record.setUuid(taxonBase.getUuid());

		//maybe wrong as according to the DwC-A documentation only resolvable ids are allowed, this differs from DwC documentation
		record.setScientificNameId(name);
		record.setScientificName(name.getTitleCache());

		record.setAcceptedNameUsageId(acceptedTaxon.getUuid());
		record.setAcceptedNameUsage(acceptedTaxon.getName() == null? acceptedTaxon.getTitleCache() : acceptedTaxon.getName().getTitleCache());

		//parentNameUsage
		if (parent != null){
			record.setParentNameUsageId(parent.getUuid());
			record.setParentNameUsage(parent.getTitleCache());
		}

		//originalNameUsage
		// ??? - is not a name usage (concept)
		if (basionym != null){
			//FIXME needs to be a coreID otherwise use string only
//			record.setOriginalNameUsageId(basionym.getUuid());
			record.setOriginalNameUsageId(null);
			record.setOriginalNameUsage(basionym.getTitleCache());
		}

		//nameAccordingTo
		Reference sec = taxonBase.getSec();
		if (sec == null){
			String message = "There is a taxon without sec " + taxonBase.getTitleCache() + "(" + taxonBase.getId() + ")";
			state.getResult().addWarning(message);
		}else{
			record.setNameAccordingToId(taxonBase.getSec().getUuid());
			record.setNameAccordingTo(taxonBase.getSec().getTitleCache());
		}

		//namePublishedIn
		// ??? is not a nameUsage (concept)
		if (name.getNomenclaturalReference() != null){
			record.setNamePublishedInId(name.getNomenclaturalReference().getUuid());
			record.setNamePublishedIn(name.getNomenclaturalReference() == null ? null : name.getNomenclaturalReference().getTitleCache());
		}

		// what is the exact difference to id and acceptedNameUsageId
		record.setTaxonConceptId(taxonBase.getUuid());

		//Classification
		if (state.getConfig().isWithHigherClassification()){
			//all classification and rank specific fields are meant to represent the classification
		    handleHigherClassification(state, record, acceptedTaxon, classification);

			//... higher ranks
//			handleUninomialOrGenus(record, name);
//			if (name.getRank() != null &&  name.getRank().equals(Rank.SUBGENUS())){
//				record.setSubgenus(name.getNameCache());
//			}
			//record.setSubgenus(name.getInfraGenericEpithet());
		}
		if (name.getRank() != null &&  (name.getRank().isSupraGeneric() || name.getRank().isGenus())){
			record.setUninomial(name.getGenusOrUninomial());
		}else{
			record.setGenusPart(name.getGenusOrUninomial());
		}
		record.setInfraGenericEpithet(name.getInfraGenericEpithet());

		record.setSpecificEpithet(name.getSpecificEpithet());
		record.setInfraspecificEpithet(name.getInfraSpecificEpithet());

		record.setTaxonRank(name.getRank());
		if (name.getRank() != null){
			record.setVerbatimTaxonRank(name.getRank().getAbbreviation());
		}else{
			String message = "No rank available for " + name.getTitleCache() + "(" + name.getId() + ")";
			state.getResult().addWarning(message);
		}

		record.setScientificNameAuthorship(name.getAuthorshipCache());

		// ??? - use for TextData common names?
		record.setVernacularName(null);

		record.setNomenclaturalCode(name.getNameType());
		// ??? TODO Misapplied Names, inferred synonyms
		handleTaxonomicStatus(record, name, relType, isProParte, isPartial);
		handleNomStatus(record, taxonBase, name);

		// TODO we need to differentiate technical
		String taxonRemarks = "";
		for (Annotation annotation : taxonBase.getAnnotations()){
			if (AnnotationType.EDITORIAL().equals(annotation.getAnnotationType())){
				taxonRemarks += CdmUtils.Nz(annotation.getText());
			}
		}
		for (Annotation annotation : name.getAnnotations()){
			if (AnnotationType.EDITORIAL().equals(annotation.getAnnotationType())){
				taxonRemarks += CdmUtils.Nz(annotation.getText());
			}
		}
		if (StringUtils.isNotBlank(taxonRemarks)){
			record.setTaxonRemarks(taxonRemarks);
		}

		// TODO which date is needed here (taxon, name, sec, ... ?)
		record.setModified(taxonBase.getUpdated());

		// ???
		record.setLanguage(null);

		record.setRights(taxonBase.getRights());

		//TODO
		record.setRightsHolder(null);
		record.setAccessRights(null);

		//TODO currently only via default value
		record.setBibliographicCitation(null);
		record.setInformationWithheld(null);

		record.setDatasetId(classification);
		record.setDatasetName(classification.getTitleCache());

		//TODO
		record.setSource(null);

		return;
	}

	/**
     * @param state
     * @param record
     * @param taxonBase
     * @param classification
     */
    private void handleHigherClassification(DwcaTaxExportState state, DwcaTaxonRecord record, Taxon taxon,
            Classification classification) {
        TaxonNode node = taxon.getTaxonNode(classification);
        if (node == null){
            return;
        }
        record.setKingdom(nameOf(node.getAncestorOfRank(Rank.KINGDOM())));
        record.setPhylum(nameOf(node.getAncestorOfRank(Rank.PHYLUM())));
        record.setClazz(nameOf(node.getAncestorOfRank(Rank.CLASS())));
        record.setOrder(nameOf(node.getAncestorOfRank(Rank.ORDER())));
        record.setFamily(nameOf(node.getAncestorOfRank(Rank.FAMILY())));
        record.setGenus(nameOf(node.getAncestorOfRank(Rank.GENUS())));
        record.setSubgenus(nameOf(node.getAncestorOfRank(Rank.SUBGENUS())));
        List<TaxonNode> ancestors = node.getAncestorList();
        String higherClassification = higherClassificationString(ancestors);
        record.setHigherClassification(higherClassification);
    }

    /**
     * @param ancestors
     */
    private String higherClassificationString(List<TaxonNode> ancestors) {
        String result = "";
        for (TaxonNode node : ancestors){
            String nameOfNode = nameOf(node);
            if (StringUtils.isBlank(nameOfNode)){
                nameOfNode = "-";
            }
            result = CdmUtils.concat("|", result, nameOfNode);
        }
        return result;
    }

    /**
     * @param ancestorOfRank
     * @return
     */
    private String nameOf(TaxonNode node) {
        if (node != null && node.getTaxon()!= null){
            Taxon taxon = node.getTaxon();
            TaxonName name = taxon.getName();
            if (name!= null){
                if (isNotBlank(name.getNameCache())){
                    return name.getNameCache();
                }else{
                    return name.getTitleCache();
                }
            }else{
                return taxon.getTitleCache();
            }
        }
        return null;
    }

    /**
	 * @param record
	 * @param name
	 * @param type
	 * @param isPartial
	 * @param isProParte
	 */
	private void handleTaxonomicStatus(DwcaTaxonRecord record,
			INonViralName name, RelationshipTermBase<?> type,
			boolean isProParte, boolean isPartial) {
		if (type == null){
			record.setTaxonomicStatus(name.getNomenclaturalCode().acceptedTaxonStatusLabel());
		}else{
			String status = name.getNomenclaturalCode().synonymStatusLabel();
			if (type.equals(SynonymType.HETEROTYPIC_SYNONYM_OF())){
				status = "heterotypicSynonym";
			}else if(type.equals(SynonymType.HOMOTYPIC_SYNONYM_OF())){
				status = "homotypicSynonym";
			}else if(type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())){
				status = "misapplied";
			}else if(type.equals(TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR())){
                status = "proParteMisapplied";
            }
			if (isProParte){
				status = "proParteSynonym";
			}else if (isPartial){
				String message = "Partial synonym is not part of the gbif toxonomic status vocabulary";
				logger.warn(message);
				status = "partialSynonym";
			}

			record.setTaxonomicStatus(status);
		}
	}

//	/**
//	 * @param record
//	 * @param name
//	 */
//	private void handleUninomialOrGenus(DwcaTaxonRecord record, INonViralName name) {
//		//epitheta
//		String firstEpi = name.getGenusOrUninomial();
//		if (StringUtils.isNotBlank(firstEpi)){
//			Rank rank = name.getRank();
//			if (rank != null){
//				if (rank.isLower(Rank.GENUS())){
//					record.setGenus(firstEpi);
//				}else if (rank.equals(Rank.GENUS())){
//					record.setGenus(firstEpi);
//				}else if (rank.equals(Rank.KINGDOM())){
//					record.setKingdom(firstEpi);
//				}else if (rank.equals(Rank.PHYLUM())){
//					record.setPhylum(firstEpi);
//				}else if (rank.equals(Rank.CLASS())){
//					record.setClazz(firstEpi);
//				}else if (rank.equals(Rank.ORDER())){
//					record.setOrder(firstEpi);
//				}else if (rank.equals(Rank.FAMILY())){
//					record.setFamily(firstEpi);
//				}else{
//					// !!!
//					String message = "Rank not covered. Set uninomial as genus instead: " + rank.getLabel();
//					logger.warn(message);
////					record.setGenus(firstEpi);
//				}
//
//			}
//		}
//	}


	/**
	 * @param record
	 * @param taxon
	 * @param name
	 */
	private void handleNomStatus(DwcaTaxonRecord record, TaxonBase<?> taxon,
			INonViralName name) {
		int nStatus = name.getStatus().size();
		if (nStatus > 0){
			if (name.getStatus().size()> 1){
				String warning = "There is more than 1 nomenclatural status ( " + name.getStatus().size()+ "): " + taxon.getTitleCache();
				logger.warn(warning);
			}
			NomenclaturalStatusType status = name.getStatus().iterator().next().getType();
			record.setNomenclaturalStatus(status);
		}else{
			record.setNomenclaturalStatus(null);
		}
	}


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	public boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoTaxa();
	}

}
