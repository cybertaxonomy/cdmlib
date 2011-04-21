/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
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
	private static final Logger logger = Logger.getLogger(DwcaResourceRelationExport.class);

	/**
	 * Constructor
	 */
	public DwcaResourceRelationExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
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
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		String dbname = config.getSource() != null ? config.getSource().getName() : "unknown";
    	String fileName = config.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		TransactionStatus txStatus = startTransaction(true);

		try {
			
			final String coreTaxFileName = "resourceRelationship.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			List<TaxonNode> allNodes =  getAllNodes(null);
			
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				
				//taxon interactions
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(TaxonInteraction.class)){
							DwcaResourceRelationRecord record = new DwcaResourceRelationRecord();
							TaxonInteraction taxonInteraction = CdmBase.deproxy(el,TaxonInteraction.class);
							handleInteraction(record, taxon, taxonInteraction);
							record.write(writer);
						}
					}
				}
				
				//concept relationships
				for (TaxonRelationship rel : taxon.getTaxonRelations()){
					DwcaResourceRelationRecord record = new DwcaResourceRelationRecord();
					IdentifiableEntity subject = rel.getFromTaxon();
					IdentifiableEntity object = rel.getToTaxon();
					
					if (rel.getType().equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR()) ){
						//misapplied names are handled in core (tax)
						continue;
					}
					handleRelationship(record, subject, object, rel);
					record.write(writer);

				}
				
				//Name relationship
				//TODO
				NonViralName<?> name = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				if (name == null){
					String message = "There is a taxon node without taxon: " + node.getId();
					logger.warn(message);
					continue;
				}
				Set<NameRelationship> rels = name.getNameRelations();
				for (NameRelationship rel : rels){
					DwcaResourceRelationRecord record = new DwcaResourceRelationRecord();
					IdentifiableEntity subject = rel.getFromName();
					IdentifiableEntity object = rel.getToName();
					
					//????
					handleRelationship(record, subject, object, rel);
					record.write(writer);

				}
				
				
				writer.flush();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		commitTransaction(txStatus);
		return true;
	}

	private void handleRelationship(DwcaResourceRelationRecord record, IdentifiableEntity subject, IdentifiableEntity object,
			RelationshipBase rel) {
		RelationshipTermBase type = rel.getType();
		record.setCoreid(subject.getId());
		record.setResourceRelationshipId(String.valueOf(rel.getId()));
		//TODO id / uuid / names ??
		if (subject.isInstanceOf(TaxonBase.class)){
			record.setRelatedResourceId(object.getUuid().toString());
		}
		//TODO transform to controlled voc
		record.setRelationshipOfResource(type.getLabel());
		record.setRelationshipAccordingTo(rel.getCitation()== null? null : rel.getCitation().getTitleCache());
		//TODO missing
		record.setRelatioshipEstablishedDate(null);
		record.setRelationshipRemarks(rel.getAnnotations());
		if (subject.isInstanceOf(TaxonNameBase.class)){
			record.setScientificName(subject.getTitleCache());
		}
		
		
	}

	private void handleInteraction(DwcaResourceRelationRecord record, IdentifiableEntity subject, TaxonInteraction interaction) {
		Taxon object = interaction.getTaxon2();
		Map<Language, LanguageString> description = interaction.getDescriptions();
		
		record.setCoreid(subject.getId());
		//TODO id / uuid
		record.setRelatedResourceId(object.getUuid().toString());
		//TODO transform to controlled voc
		if (description != null && description.get(Language.DEFAULT()) != null){
			record.setRelationshipOfResource(description.get(Language.DEFAULT()).getText());
		}else{
			record.setRelationshipOfResource(interaction.getFeature().getLabel());
		}
		//TODO uuid
		record.setResourceRelationshipId(String.valueOf(interaction.getId()));
		
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
