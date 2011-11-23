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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUris;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class DwcTaxonCsv2CdmTaxonConverter implements IConverter<CsvStreamItem, IReader<CdmBase>, DwcaImportState>{
	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonConverter.class);

	private static final String ID = "id";

	public IReader<CdmBase> map(CsvStreamItem item, DwcaImportState state){
		List<CdmBase> resultList = new ArrayList<CdmBase>(); 
		
		Map<String, String> csvTaxonRecord = item.map;
		Reference<?> sourceReference = null;
		String sourceReferecenDetail = null;
		
		TaxonBase<?> taxonBase = getTaxonBase(csvTaxonRecord);
		resultList.add(taxonBase);
		
		String id = csvTaxonRecord.get(ID);
		IdentifiableSource source = taxonBase.addSource(id, "Taxon", sourceReference, sourceReferecenDetail);
		resultList.add(source);
		csvTaxonRecord.remove(ID);
		
		Rank rank = getRank(csvTaxonRecord);

		NomenclaturalCode nomCode = getNomCode(csvTaxonRecord);
		TaxonNameBase<?,?> name = getScientificName(csvTaxonRecord, nomCode, rank);
		taxonBase.setName(name);
		
		Reference<?> sec = getSec(csvTaxonRecord);
		taxonBase.setSec(sec);
		
		


		

//	    <!-- Taxonomic rank -->
//	    <field index='7' term='http://rs.tdwg.org/dwc/terms/taxonRank'/>

//	    <!-- Infraspecific marker if displayed in complete scientific name -->
//	    <field index='8' term='http://rs.tdwg.org/dwc/terms/verbatimTaxonRank'/>

		
//		    <field index="0" term="http://rs.tdwg.org/dwc/terms/taxonID"/>
		
//		    <!-- LSID -->
//		    <field index="1" term="http://purl.org/dc/terms/identifier"/>

//		    <!-- CoL source database id -->
//		    <field index="2" term="http://rs.tdwg.org/dwc/terms/datasetID"/>
		
//		    <!-- Short name of source database plus CoL credits -->
//		    <field index="3" term="http://rs.tdwg.org/dwc/terms/datasetName"/>

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

		return new ListReader<CdmBase>(resultList);
	}


	private Reference<?> getSec(Map<String, String> csvTaxonRecord) {
		String strSec = csvTaxonRecord.get(TermUris.DWC_NAME_ACCORDING_TO);
		if (strSec != null){
			Reference<?> sec = ReferenceFactory.newGeneric();
			sec.setTitleCache(strSec, true);
			return sec;
		}
		return null;
	}


	private NomenclaturalCode getNomCode(Map<String, String> csvTaxonRecord) {
		String strNomCode = csvTaxonRecord.get(TermUris.DWC_NOMENCLATURAL_CODE);
		if (strNomCode != null){
			NomenclaturalCode nomCode = NomenclaturalCode.fromString(strNomCode);
			if (nomCode == null){
				logger.warn("NomCode not recognized");
			}
			return nomCode;
		}
		return null;
	}


	private TaxonNameBase<?,?> getScientificName(Map<String, String> csvTaxonRecord, NomenclaturalCode nomCode, Rank rank) {
		String strScientificName = csvTaxonRecord.get(TermUris.DWC_SCIENTIFIC_NAME);
		if (strScientificName != null){
			INonViralNameParser<?> parser = NonViralNameParserImpl.NewInstance();
			TaxonNameBase<?,?> name = parser.parseFullName(strScientificName, nomCode, rank);
			return name;
		}else{
			logger.warn("Scientific name not given");
		}
		return null;
	}


	private Rank getRank(Map<String, String> csvTaxonRecord) {
		boolean USE_UNKNOWN = true;
		String strRank = csvTaxonRecord.get("http://rs.tdwg.org/dwc/terms/taxonRank");
		if (strRank != null){
			try {
				Rank rank = Rank.getRankByNameOrAbbreviation(strRank, USE_UNKNOWN);
				if (rank.equals(Rank.UNKNOWN_RANK())){
					//TODO
					logger.warn("Unknown rank: " + strRank);
				}
				return rank;
			} catch (UnknownCdmTypeException e) {
				//should not happen as USE_UNKNOWN is used
			}
		}
		return null;
	}


	private TaxonBase getTaxonBase(Map<String, String> csvTaxonRecord) {
		TaxonNameBase<?,?> name = null;
		Reference<?> sec = null;
		TaxonBase<?> result;
		String status = csvTaxonRecord.get(TermUris.DWC_TAXONOMIC_STATUS);
		if (status != null){
			if (status.matches("accepted|valid|misapplied")){
				result = Taxon.NewInstance(name, sec);
			}else if (status.matches(".*synonym|invalid")){
				result = Synonym.NewInstance(name, sec);
			}else{
				result = Taxon.NewUnknownStatusInstance(name, sec);
			}
			csvTaxonRecord.remove(TermUris.DWC_TAXONOMIC_STATUS);
		}else{
			result = Taxon.NewUnknownStatusInstance(name, sec);
		}
		//TODO handle acceptedNameUsage(ID), 
		return result;
	}
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}

}
