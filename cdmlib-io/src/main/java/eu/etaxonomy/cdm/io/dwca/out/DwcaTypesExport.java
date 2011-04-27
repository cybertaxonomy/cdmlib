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
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
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

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaTypesExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaTypesExport.class);
	private static final String fileName = "typesAndSpecimen.txt";
	
	/**
	 * Constructor
	 */
	public DwcaTypesExport() {
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
		TransactionStatus txStatus = startTransaction(true);

		try {
			
			PrintWriter writer = createPrintWriter(fileName, config);
			List<TaxonNode> allNodes =  getAllNodes(null);
			
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				
				//TODO use API methods to retrieve all related specimen
				
				//individual associations
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(IndividualsAssociation.class)){
							DwcaTypesRecord record = new DwcaTypesRecord();
							IndividualsAssociation individualAssociation = CdmBase.deproxy(el,IndividualsAssociation.class);
							if (! this.recordExistsUuid(individualAssociation) && handleSpecimen(record, individualAssociation, null, taxon)){
								record.write(writer);
								this.addExistingRecordUuid(individualAssociation);
							}
						}
					}
				}
				
				//type specimen 
				NonViralName<?> nvn = CdmBase.deproxy(taxon.getName(), NonViralName.class);
				handleTypeName(writer, taxon, nvn);
				for (Synonym synonym : taxon.getSynonyms()){
					handleTypeName(writer, synonym, nvn);
				}
				
				//FIXME
				//Determinations
				
				
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

	/**
	 * @param writer
	 * @param taxon
	 * @param nvn
	 * @return
	 */
	private Set<TypeDesignationBase<?>> handleTypeName(PrintWriter writer, TaxonBase taxonBase, NonViralName<?> nvn) {
		Set<TypeDesignationBase<?>> designations = nvn.getTypeDesignations();
		for (TypeDesignationBase designation:designations){
			DwcaTypesRecord record = new DwcaTypesRecord();
			if (! this.recordExistsUuid(designation) && handleSpecimen(record, null, designation, taxonBase)){
				record.write(writer);
				addExistingRecordUuid(designation);
			}
		}
		return designations;
	}
	

	private boolean handleSpecimen(DwcaTypesRecord record, IndividualsAssociation individualsAssociation, TypeDesignationBase designation, TaxonBase taxonBase) {
		TypeDesignationStatusBase status = null;
		DerivedUnitFacade facade = null;
		if (individualsAssociation != null){
			facade = getFacadeFromAssociation(individualsAssociation);
		}else if (designation != null){
			facade = getFacadeFromDesignation(designation);
			status = designation.getTypeStatus();
		}
		if (facade == null){
			return false;
		}
		
		record.setCoreid(taxonBase.getId());
		record.setBibliographicCitation(facade.getTitleCache());
		record.setTypeStatus(status);
		record.setTypeDesignatedBy( (designation == null || designation.getCitation()==null)? null: designation.getCitation().getTitleCache());
		
		TaxonNameBase scientificName = getScientificName(facade);
		if (scientificName != null){
			record.setScientificName(scientificName.getTitleCache());
			record.setTaxonRank(scientificName.getRank());
		}
		
		record.setOccurrenceId(facade.innerDerivedUnit().getUuid().toString());
		Collection collection = facade.getCollection();
		if (collection != null){
			record.setCollectionCode(collection.getCode());
			if (collection.getInstitute() != null){
				record.setInstitutionCode(collection.getInstitute().getCode());
			}
		}
		record.setCatalogNumber(facade.getCatalogNumber());
		record.setLocality(facade.getLocalityText());
		record.setSex(facade.getSex());
		record.setRecordedBy(facade.getCollector());
		//TODO ???
		record.setSource(null);
		record.setEventDate(facade.getGatheringPeriod());
		//TODO missing
		record.setVerbatimLabel(null);
		if (facade.getExactLocation() != null){
			if (facade.getExactLocation().getLongitude() != null){
				record.setVerbatimLongitude(facade.getExactLocation().getLongitudeSexagesimal().toString());
			}
			if (facade.getExactLocation().getLatitude() != null){
				record.setVerbatimLatitude(facade.getExactLocation().getLatitudeSexagesimal().toString());
			}
		}
		return true;
	}
	
	private TaxonNameBase getScientificName(DerivedUnitFacade facade) {
		Set<DeterminationEvent> detEvents = facade.getDeterminations();
		for (DeterminationEvent detEvent : detEvents){
			if (detEvent.getPreferredFlag()== true){
				return detEvent.getTaxon().getName();
			}
		}
		return null;
	}

	private DerivedUnitFacade getFacadeFromDesignation(TypeDesignationBase designation) {
		if (designation.isInstanceOf(SpecimenTypeDesignation.class)){
			SpecimenTypeDesignation specDesig = CdmBase.deproxy(designation, SpecimenTypeDesignation.class);
			try {
				DerivedUnitBase derivedUnit = specDesig.getTypeSpecimen();
				if (derivedUnit == null){
					return null;
				}else{
					DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(derivedUnit);
					return facade;
				}
			} catch (DerivedUnitFacadeNotSupportedException e) {
				String message = "DerivedUnit is too complex to be handled by facade based darwin core archive export";
				logger.warn(message);
				//TODO handle empty records
				return null; 
			}
		}else{
			return null;
		}
	}

	private DerivedUnitFacade getFacadeFromAssociation(IndividualsAssociation individualsAssociation) {
		SpecimenOrObservationBase specimen = individualsAssociation.getAssociatedSpecimenOrObservation();
		DerivedUnitFacade facade;
		if (! specimen.isInstanceOf(DerivedUnitBase.class)){
			String message = "Non DerivedUnit specimen can not yet be handled by this export";
			logger.warn(message);
			//TODO handle empty records
			return null; 
		}else{
			DerivedUnitBase<?> derivedUnit = CdmBase.deproxy(specimen, DerivedUnitBase.class);
			try {
				facade = DerivedUnitFacade.NewInstance(derivedUnit);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				String message = "DerivedUnit is too complex to be handled by facade based darwin core archive export";
				logger.warn(message);
				//TODO handle empty records
				return null; 
			}
			
		}
		return facade;
	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoTypesAndSpecimen();
	}
	
}
