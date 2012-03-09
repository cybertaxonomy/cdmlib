// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @date 23.11.2011
 *
 */
public class DwcTaxonCsv2CdmTaxonRelationConverter<STATE extends DwcaImportState> extends ConverterBase<DwcaImportState> 
						implements IConverter<CsvStreamItem, IReader<CdmBase>, String>{
	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonRelationConverter.class);

	private static final String ID = "id";
	
	/**
	 * @param state
	 */
	public DwcTaxonCsv2CdmTaxonRelationConverter(DwcaImportState state) {
		super();
		this.state = state;
	}


	public IReader<MappedCdmBase> map(CsvStreamItem item){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csvRecord = item.map;
		Reference<?> sourceReference = null;
		String sourceReferecenDetail = null;
		
		String id = csvRecord.get(ID);
		TaxonBase<?> taxonBase = getTaxonBase(id, item, null);
		if (taxonBase == null){
			String warning = "Taxon not available for id %s.";
			warning = String.format(warning, id);
			fireWarningEvent(warning, item, 8);
		}else{
			
			MappedCdmBase mcb = new MappedCdmBase(taxonBase);
			resultList.add(mcb);
			
			handleAcceptedNameUsage(item, state, taxonBase, id);
			
			handleParentNameUsage(item, state, taxonBase, resultList);
			
			handleKingdom(item, state);
			
			handlePhylum(item, state);
			
			handleClass(item, state);
			
			handleOrder(item, state);
			
			handleFamily(item, state);
			
			handleGenus(item, state);
			
			handleSubGenus(item, state);
			
		}
		csvRecord.remove(ID);
		
		
//		    <!-- Top level group; listed as kingdom but may be interpreted as domain or superkingdom
//		         The following eight groups are recognized: Animalia, Archaea, Bacteria, Chromista, 
//		         Fungi, Plantae, Protozoa, Viruses -->
//		    <field index='10' term='http://rs.tdwg.org/dwc/terms/kingdom'/>

//		    <!-- Specific epithet; for hybrids, the multiplication symbol is included in the epithet -->
//		    <field index='17' term='http://rs.tdwg.org/dwc/terms/specificEpithet'/>

//		    <!-- Infraspecific epithet -->
//		    <field index='18' term='http://rs.tdwg.org/dwc/terms/infraspecificEpithet'/>
	
//			<!-- Acceptance status published in -->
//		    <field index='20' term='http://purl.org/dc/terms/source'/>

//		    <!-- Reference in which the scientific name was first published -->
//		    <field index='21' term='http://rs.tdwg.org/dwc/terms/namePublishedIn'/>

//		    <!-- Scrutiny date -->
//		    <field index='23' term='http://purl.org/dc/terms/modified'/>
//		    <!-- Additional data for the taxon -->
		
//		    <field index='24' term='http://purl.org/dc/terms/description'/>
//		    </core>

		return new ListReader<MappedCdmBase>(resultList);
	}
	
	
	@Override
	public String getSourceId(CsvStreamItem item) {
		String id = item.get(ID);
		return id;
	}


	private void handleSubGenus(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handleGenus(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handleFamily(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handleOrder(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handleClass(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handlePhylum(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handleKingdom(CsvStreamItem item, DwcaImportState state) {
		// TODO Auto-generated method stub
		
	}


	private void handleParentNameUsage(CsvStreamItem item, DwcaImportState state, TaxonBase<?> taxonBase, List<MappedCdmBase> resultList) {
		if (exists(TermUri.DWC_PARENT_NAME_USAGE_ID, item) || exists(TermUri.DWC_PARENT_NAME_USAGE, item)){
			if (taxonBase.isInstanceOf(Taxon.class)){
				Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				String accId = item.get(TermUri.DWC_PARENT_NAME_USAGE_ID);
				Taxon parentTaxon = getTaxonBase(accId, item, Taxon.class);
				if (parentTaxon == null){
						fireWarningEvent("NON-ID parent Name Usage not yet implemented or parent name usage id not available", item, 4);
				}else{
					Classification classification = getClassification(item);
					Reference<?> citation = null;
					classification.addParentChild(parentTaxon, taxon, citation, null);
					resultList.add(new MappedCdmBase(classification));
				}
			}else{
				String message = "PARENT_NAME_USAGE given for Synonym. This is not allowed in CDM.";
				//TODO check "is this Taxon"
				fireWarningEvent(message, item, 4);
			}
		}

		
	}


	private Classification getClassification(CsvStreamItem item) {
		Set<Classification> result = new HashSet<Classification>();
		result.addAll(state.get(TermUri.DWC_DATASET_ID.toString(), item.get(TermUri.DWC_DATASET_ID), Classification.class));
		result.addAll(state.get(TermUri.DWC_DATASET_NAME.toString(), item.get(TermUri.DWC_DATASET_NAME), Classification.class));
		if (result.isEmpty()){
			return null;
		}else if (result.size() > 1){
			fireWarningEvent("Dataset is ambigous. I take arbitrary one.", item, 8);
		}
		return result.iterator().next();
	}


	private void handleAcceptedNameUsage(CsvStreamItem item, DwcaImportState state, TaxonBase taxonBase, String id) {
		if (exists(TermUri.DWC_ACCEPTED_NAME_USAGE_ID, item) || exists(TermUri.DWC_ACCEPTED_NAME_USAGE, item)){
			String accId = item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID);
			if (id.equals(accId)){
				return;   //mapping to itself needs no further handling
			}
			if (taxonBase.isInstanceOf(Synonym.class)){
				Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
				Taxon accTaxon = getTaxonBase(accId, item, Taxon.class);
				if (accTaxon == null){
						fireWarningEvent("NON-ID accepted Name Usage not yet implemented or taxon for name usage id not available", item, 4);
				}else{
					accTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(),null, null);
				}
			} else{
				String message = "Accepted name usage is not of type synonym. This is not allowed in CDM. Can't create realtionship";
				//TODO check "is this Taxon"
				fireWarningEvent(message, item, 4);
			}
		}else{
			if (logger.isDebugEnabled()){logger.debug("");}
		}
	}


	private <T extends TaxonBase> T getTaxonBase(String id, CsvStreamItem item, Class<T> clazz) {
		if (clazz == null){
			clazz = (Class)TaxonBase.class;
		}
		List<T> taxonList = state.get(TermUri.DWC_TAXON.toString(), id, clazz);
		if (taxonList.size() > 1){
			String message = "Undefined taxon mapping for id %s.";
			message = String.format(message, id);
			fireWarningEvent(message, item, 8);
			logger.warn(message);  //TODO remove when events are handled correctly
			return null;
		}else if (taxonList.isEmpty()){
			return null;
		}else{
			return taxonList.get(0);
		}
	}



	
	@Override
	public String toString(){
		return this.getClass().getName();
	}

}
