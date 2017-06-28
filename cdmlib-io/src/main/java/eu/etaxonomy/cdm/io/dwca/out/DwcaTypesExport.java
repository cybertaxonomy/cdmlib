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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
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

    private static final long serialVersionUID = 8879154738843628476L;

    private static final Logger logger = Logger.getLogger(DwcaTypesExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/TypesAndSpecimen";
	protected static final String fileName = "typesAndSpecimen.txt";

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
	protected void doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);

		DwcaTaxOutputFile file = DwcaTaxOutputFile.TYPES;

        try {

			DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

            List<TaxonNode> allNodes = state.getAllNodes();

			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon());

				//TODO use API methods to retrieve all related specimen

				//individual associations
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(IndividualsAssociation.class)){
							DwcaTypesRecord record = new DwcaTypesRecord(metaRecord, config);
							IndividualsAssociation individualAssociation = CdmBase.deproxy(el,IndividualsAssociation.class);
							if (! state.recordExistsUuid(individualAssociation)
							        && handleSpecimen(state, record, individualAssociation, null, taxon, config)){
							    PrintWriter writer = createPrintWriter(state, file);
					            record.write(state, writer);
								state.addExistingRecordUuid(individualAssociation);
							}
						}
					}
				}

				//type specimen
				INonViralName nvn = taxon.getName();
				handleTypeName(state, file, taxon, nvn, metaRecord);
				for (Synonym synonym : taxon.getSynonyms()){
					handleTypeName(state, file, synonym, nvn, metaRecord);
				}

				//FIXME
				//Determinations

				flushWriter(state, file);

			}
		} catch (Exception e) {
	          String message = "Unexpected exception " + e.getMessage();
	          state.getResult().addException(e, message, "DwcaTypesExport.doInvoke()");
		} finally{
			closeWriter(file, state);
		}
		commitTransaction(txStatus);
		return;
	}

	/**
	 * @param state
	 * @param writer
	 * @param taxon
	 * @param nvn
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	private Set<TypeDesignationBase> handleTypeName(DwcaTaxExportState state,
	        DwcaTaxOutputFile file, TaxonBase<?> taxonBase,
	        INonViralName nvn, DwcaMetaDataRecord metaRecord)
	        throws FileNotFoundException, UnsupportedEncodingException, IOException {
	    DwcaTaxExportConfigurator config = state.getConfig();
		Set<TypeDesignationBase> designations = nvn.getTypeDesignations();
		for (TypeDesignationBase<?> designation:designations){
			DwcaTypesRecord record = new DwcaTypesRecord(metaRecord, config);
			if (! state.recordExistsUuid(designation)
			        && handleSpecimen(state, record, null, designation, taxonBase, config)){
			    PrintWriter writer = createPrintWriter(state, file);
                record.write(state, writer);
				state.addExistingRecordUuid(designation);
			}
		}
		return designations;
	}


	private boolean handleSpecimen(DwcaTaxExportState state, DwcaTypesRecord record, IndividualsAssociation individualsAssociation, TypeDesignationBase<?> designation, TaxonBase<?> taxonBase, DwcaTaxExportConfigurator config) {
		TypeDesignationStatusBase<?> status = null;
		DerivedUnitFacade facade = null;
		if (individualsAssociation != null){
			facade = getFacadeFromAssociation(state, individualsAssociation);
		}else if (designation != null){
			facade = getFacadeFromDesignation(state, designation);
			status = designation.getTypeStatus();
		}
		if (facade == null){
			return false;
		}

		record.setId(taxonBase.getId());
		record.setUuid(taxonBase.getUuid());
		record.setBibliographicCitation(facade.getTitleCache());
		record.setTypeStatus(status);
		record.setTypeDesignatedBy( (designation == null || designation.getCitation()==null)? null: designation.getCitation().getTitleCache());

		TaxonName scientificName = getScientificName(facade);
		if (scientificName != null){
			record.setScientificName(scientificName.getTitleCache());
			record.setTaxonRank(scientificName.getRank());
		}

		record.setOccurrenceId(facade.innerDerivedUnit());
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

		String source2 = "";
		if (individualsAssociation!=null) {
            source2 = getSources2(individualsAssociation.getSources(), config);
        }

		record.setSource(getSources3(facade.innerDerivedUnit(), config));
		record.setDescriptionSource(source2);

		record.setEventDate(facade.getGatheringPeriod());
		//TODO missing
		record.setVerbatimLabel(null);
		if (facade.getExactLocation() != null){
			if (facade.getExactLocation().getLongitude() != null){
//				record.setVerbatimLongitude(facade.getExactLocation().getLongitudeSexagesimal().toString());
			    record.setVerbatimLongitude(facade.getExactLocation().getLongitude().toString());
			}
			if (facade.getExactLocation().getLatitude() != null){
//				record.setVerbatimLatitude(facade.getExactLocation().getLatitudeSexagesimal().toString());
			    record.setVerbatimLatitude(facade.getExactLocation().getLatitude().toString());
			}
			if(facade.getExactLocation().getErrorRadius() != null) {
                record.setCoordinatesPrecisionOrError(facade.getExactLocation().getErrorRadius().toString());
            }
			if(facade.getExactLocation().getReferenceSystem() != null) {
                record.setCoordinatesSystem(facade.getExactLocation().getReferenceSystem().toString());
            }
		}
		return true;
	}

	private TaxonName getScientificName(DerivedUnitFacade facade) {
		Set<DeterminationEvent> detEvents = facade.getDeterminations();
		for (DeterminationEvent detEvent : detEvents){
			if (detEvent.getPreferredFlag()== true || detEvents.size()==1){
				return detEvent.getTaxon().getName();
			}
		}
		return null;
	}

	private DerivedUnitFacade getFacadeFromDesignation(DwcaTaxExportState state, TypeDesignationBase<?> designation) {
		if (designation.isInstanceOf(SpecimenTypeDesignation.class)){
			SpecimenTypeDesignation specDesig = CdmBase.deproxy(designation, SpecimenTypeDesignation.class);
			try {
				DerivedUnit derivedUnit = specDesig.getTypeSpecimen();
				if (derivedUnit == null){
					return null;
				}else{
					DerivedUnitFacade facade = DerivedUnitFacade.NewInstance(derivedUnit);
					return facade;
				}
			} catch (DerivedUnitFacadeNotSupportedException e) {
				String message = "DerivedUnit is too complex to be handled by facade based darwin core archive export";
				state.getResult().addError(message, this, "getFacadeFromAssociation()");

				//TODO handle empty records
				return null;
			}
		}else{
			return null;
		}
	}

	private DerivedUnitFacade getFacadeFromAssociation(DwcaTaxExportState state, IndividualsAssociation individualsAssociation) {
		SpecimenOrObservationBase<?> specimen = individualsAssociation.getAssociatedSpecimenOrObservation();
		DerivedUnitFacade facade;
		if (! specimen.isInstanceOf(DerivedUnit.class)){
			String message = "Non DerivedUnit specimen can not yet be handled by this export";
			logger.warn(message);
			//TODO handle empty records
			return null;
		}else{
			DerivedUnit derivedUnit = CdmBase.deproxy(specimen, DerivedUnit.class);
			try {
				facade = DerivedUnitFacade.NewInstance(derivedUnit);
			} catch (DerivedUnitFacadeNotSupportedException e) {
				String message = "DerivedUnit is too complex to be handled by facade based darwin core archive export";
				state.getResult().addError(message, this, "getFacadeFromAssociation()");
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
