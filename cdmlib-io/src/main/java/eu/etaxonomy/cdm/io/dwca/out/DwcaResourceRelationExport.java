/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.common.ExportDataWrapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * Mapps taxon concept relationships, taxon name relationships and taxon interactions
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaResourceRelationExport extends DwcaExportBase {
    private static final long serialVersionUID = 33810773244068812L;

    private static final Logger logger = Logger.getLogger(DwcaResourceRelationExport.class);

	private static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/ResourceRelationship";
	private static final String fileName = "resourceRelationship.txt";

	/**
	 * Constructor
	 */
	public DwcaResourceRelationExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
		this.exportData = ExportDataWrapper.NewByteArrayInstance();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 *
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected void doInvoke(DwcaTaxExportState state){
		TransactionStatus txStatus = startTransaction(true);

		PrintWriter writer = null;
		try {

			writer = createPrintWriter(fileName, state);

			DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

            List<TaxonNode> allNodes = allNodes(state);

			for (TaxonNode node : allNodes){
			    makeSingleTaxonNode(state, node, metaRecord,writer);
                if (writer != null){
                    writer.flush();
                }
			}
		} catch (IOException e) {
	         String message = "Unexpected exception " + e.getMessage();
	         state.getResult().addException(e, message, "DwcaResourceRelationExport.doInvoke()");
		} finally{
			closeWriter(writer, state);
		}
		commitTransaction(txStatus);
		return;
	}

    private void makeSingleTaxonNode(DwcaTaxExportState state, TaxonNode node, DwcaMetaDataRecord metaRecord,
            PrintWriter writer){
        DwcaTaxExportConfigurator config = state.getConfig();

        Taxon taxon = CdmBase.deproxy(node.getTaxon());

        //taxon interactions
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        for (TaxonDescription description : descriptions){
            for (DescriptionElementBase el : description.getElements()){
                if (el.isInstanceOf(TaxonInteraction.class)){
                    DwcaResourceRelationRecord record = new DwcaResourceRelationRecord(metaRecord, config);
                    TaxonInteraction taxonInteraction = CdmBase.deproxy(el,TaxonInteraction.class);
                    if (! this.recordExistsUuid(taxonInteraction)){
                        handleInteraction(record, taxon, taxonInteraction);
                        record.write(state, writer);
                        this.addExistingRecordUuid(taxonInteraction);
                    }
                }
            }
        }

        //concept relationships
        for (TaxonRelationship rel : taxon.getTaxonRelations()){
            DwcaResourceRelationRecord record = new DwcaResourceRelationRecord(metaRecord, config);
            IdentifiableEntity<?> subject = rel.getFromTaxon();
            IdentifiableEntity<?> object = rel.getToTaxon();

            if (rel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR()) ){
                //misapplied names are handled in core (tax)
                continue;
            }
            if (! this.recordExistsUuid(rel)){
                handleRelationship(record, subject, object, rel, false);
                record.write(state, writer);
                this.addExistingRecordUuid(rel);
            }

        }

        //Name relationship
        //TODO
        INonViralName name = taxon.getName();
        if (name == null){
            String message = "There is a taxon node without name: " + node.getId();
            state.getResult().addError(message, "DwcaResourceRelationExport.makeSingleTaxonNode");
            return;
        }
        Set<NameRelationship> rels = name.getNameRelations();
        for (NameRelationship rel : rels){
            DwcaResourceRelationRecord record = new DwcaResourceRelationRecord(metaRecord, config);
            IdentifiableEntity<?> subject = CdmBase.deproxy(rel.getFromName());
            IdentifiableEntity<?> object = CdmBase.deproxy(rel.getToName());
            boolean isInverse = false;
            if(subject == name){
                subject = taxon;
            }else if(object == name){
                object= subject;
                subject = taxon;
                isInverse = true;
            }else{
                String message = "Both, subject and object, are not part of the relationship for " + name.getTitleCache();
                logger.warn(message);
                state.getResult().addWarning(message, "DwcaResourceRelationExport.makeSingleTaxonNode");
            }

            if (! this.recordExistsUuid(rel)){
                //????
                handleRelationship(record, subject, object, rel, isInverse);
                record.write(state, writer);
                this.addExistingRecordUuid(rel);
            }

        }
    }

	private void handleRelationship(DwcaResourceRelationRecord record, IdentifiableEntity<?> subject, IdentifiableEntity<?> object,
			RelationshipBase<?,?,?> rel, boolean isInverse) {
		RelationshipTermBase<?> type = rel.getType();

		record.setId(subject.getId());
		record.setUuid(subject.getUuid());


		record.setResourceRelationshipId(rel.getId());
		record.setResourceRelationshipId(rel.getUuid());
		//TODO id / uuid / names ??
		if (object.isInstanceOf(TaxonBase.class)){
			record.setRelatedResourceId(object.getUuid());
		}
		//TODO transform to controlled voc
		String relTypeLabel;
		if (isInverse){
			relTypeLabel = type.getInverseLabel();
		}else{
			relTypeLabel = type.getLabel();
		}
		record.setRelationshipOfResource(relTypeLabel);
		record.setRelationshipAccordingTo(rel.getCitation()== null? null : rel.getCitation().getTitleCache());
		//TODO missing
		record.setRelatioshipEstablishedDate(null);
		record.setRelationshipRemarks(rel.getAnnotations());
		if (object.isInstanceOf(TaxonName.class)){
			record.setScientificName(object.getTitleCache());
		}


	}



	private void handleInteraction(DwcaResourceRelationRecord record, IdentifiableEntity<?> subject, TaxonInteraction interaction) {
		Taxon object = interaction.getTaxon2();
		Map<Language, LanguageString> description = interaction.getDescription();

		record.setId(subject.getId());
		record.setUuid(subject.getUuid());

		record.setRelatedResourceId(object.getUuid());
		//TODO transform to controlled voc
		if (description != null && description.get(Language.DEFAULT()) != null){
			record.setRelationshipOfResource(description.get(Language.DEFAULT()).getText());
		}else{
			record.setRelationshipOfResource(interaction.getFeature().getLabel());
		}
		//TODO uuid
		record.setResourceRelationshipId(interaction.getId());
		record.setResourceRelationshipId(interaction.getUuid());

		//FIXME multiple sources
		record.setRelationshipAccordingTo(null);
		//TODO missing
		record.setRelatioshipEstablishedDate(null);
		record.setRelationshipRemarks(interaction.getAnnotations());
		//TODO does this need to be filled?
		record.setScientificName(null);

	}


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoResourceRelation();
	}

}
