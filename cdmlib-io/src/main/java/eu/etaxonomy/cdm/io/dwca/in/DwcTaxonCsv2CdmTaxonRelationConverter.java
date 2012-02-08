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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @date 23.11.2011
 *
 */
public class DwcTaxonCsv2CdmTaxonRelationConverter<STATE extends DwcaImportState> extends ConverterBase<DwcaImportState> implements IConverter<CsvStreamItem, IReader<CdmBase>>{
	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonRelationConverter.class);

	private static final String ID = "id";
	
	/**
	 * @param state
	 */
	public DwcTaxonCsv2CdmTaxonRelationConverter(DwcaImportState state) {
		super();
		this.state = state;
	}


	public IReader<CdmBase> map(CsvStreamItem item){
		List<CdmBase> resultList = new ArrayList<CdmBase>(); 
		
		Map<String, String> csvRecord = item.map;
		Reference<?> sourceReference = null;
		String sourceReferecenDetail = null;
		
		String id = csvRecord.get(ID);
		TaxonBase<?> taxonBase = getTaxonBase(csvRecord);
		csvRecord.remove(ID);
		if (taxonBase == null){

		}else{
			resultList.add(taxonBase);
			
			handleAcceptedNameUsage(item, state, taxonBase);
			
			handleParentNameUsage(item, state, taxonBase);
			
			handleKingdom(item, state);
			
			handlePhylum(item, state);
			
			handleClass(item, state);
			
			handleOrder(item, state);
			
			handleFamily(item, state);
			
			handleGenus(item, state);
			
			handleSubGenus(item, state);
			
		}
		
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

		return new ListReader<CdmBase>(resultList);
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


	private void handleParentNameUsage(CsvStreamItem csvRecord, DwcaImportState state, TaxonBase<?> taxonBase) {
		if (exists(TermUri.DWC_PARENT_NAME_USAGE_ID, csvRecord) || exists(TermUri.DWC_PARENT_NAME_USAGE, csvRecord)){
			if (taxonBase.isInstanceOf(Taxon.class)){
			
			}else{
				String message = "PARENT_NAME_USAGE given for Synonym";
				//TODO check "is this Taxon"
				fireWarningEvent(message, csvRecord, 4);
			}
		}

		
	}


	private void handleAcceptedNameUsage(CsvStreamItem item, DwcaImportState state, TaxonBase taxonBase) {
		if (exists(TermUri.DWC_ACCEPTED_NAME_USAGE_ID, item) || exists(TermUri.DWC_ACCEPTED_NAME_USAGE, item)){
			if (taxonBase.isInstanceOf(Synonym.class)){
				
			}else{
				String message = "ACCEPTED_NAME_USAGE given for non Synonym";
				//TODO check "is this Taxon"
				fireWarningEvent(message, item, 4);
			}
		}

		
	}


	private TaxonBase<?> getTaxonBase(Map<String, String> csvRecord) {
		// TODO Auto-generated method stub
		return null;
	}



	
	@Override
	public String toString(){
		return this.getClass().getName();
	}

}
