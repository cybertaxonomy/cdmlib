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

import eu.etaxonomy.cdm.io.dwca.TermUri;
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
public class DwcTaxonCsv2CdmTaxonConverter extends ConverterBase<DwcaImportState> implements IConverter<CsvStreamItem, IReader<CdmBase>>{
	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonConverter.class);

	private static final String ID = "id";
	
	
	/**
	 * @param state
	 */
	public DwcTaxonCsv2CdmTaxonConverter(DwcaImportState state) {
		super();
		this.state = state;
	}


	public IReader<CdmBase> map(CsvStreamItem item){
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
		
		Rank rank = getRank(item);

		NomenclaturalCode nomCode = getNomCode(item);
		TaxonNameBase<?,?> name = getScientificName(item, nomCode, rank);
		taxonBase.setName(name);
		
		Reference<?> sec = getNameAccordingTo(csvTaxonRecord);
		taxonBase.setSec(sec);
		
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


	private Reference<?> getNameAccordingTo(Map<String, String> csvTaxonRecord) {
		String strSec = csvTaxonRecord.get(TermUri.DWC_NAME_ACCORDING_TO);
		if (strSec != null){
			Reference<?> sec = ReferenceFactory.newGeneric();
			sec.setTitleCache(strSec, true);
			return sec;
		}
		return null;
	}


	private NomenclaturalCode getNomCode(CsvStreamItem item) {
		String strNomCode = getValue(item, TermUri.DWC_NOMENCLATURAL_CODE);
		if (strNomCode != null){
			NomenclaturalCode nomCode = NomenclaturalCode.fromString(strNomCode);
			if (nomCode == null){
				String message = "NomCode '%s' not recognized";
				message = String.format(message, strNomCode);
				fireWarningEvent(message, item, 4);
			}
			return nomCode;
		}
		return null;
	}


	private TaxonNameBase<?,?> getScientificName(CsvStreamItem item, NomenclaturalCode nomCode, Rank rank) {
		String strScientificName = getValue(item, TermUri.DWC_SCIENTIFIC_NAME);
		if (strScientificName != null){
			INonViralNameParser<?> parser = NonViralNameParserImpl.NewInstance();
			TaxonNameBase<?,?> name = parser.parseFullName(strScientificName, nomCode, rank);
			if (rank != null && name != null && name.getRank() != null && 
					! rank.equals(name.getRank())){
				String message = "Parsed rank %s differs from given rank %s";
				message = String.format(message, name.getRank().getTitleCache(), rank.getTitleCache());
				fireWarningEvent(message, item, 4);
			}
			return name;
		}
		String strScientificNameId = getValue(item, TermUri.DWC_SCIENTIFIC_NAME_ID);
		if (strScientificNameId != null){
			String message = "ScientificNameId not yet implemented: '%s'";
			message = String.format(message, strScientificNameId);
			fireWarningEvent(message, item, 4);
		}
		return null;
	}


	private Rank getRank(CsvStreamItem csvTaxonRecord) {
		boolean USE_UNKNOWN = true;
		Rank rank = null;
		String strRank = getValue(csvTaxonRecord,TermUri.DWC_TAXON_RANK);
		String strVerbatimRank = getValue(csvTaxonRecord,TermUri.DWC_VERBATIM_TAXON_RANK);
		if (strRank != null){
			try {
				rank = Rank.getRankByNameOrAbbreviation(strRank, USE_UNKNOWN);
				if (rank.equals(Rank.UNKNOWN_RANK())){
					String message = "Rank can not be defined for '%s'";
					message = String.format(message, strRank);
					fireWarningEvent(message, csvTaxonRecord, 4);
				}
			} catch (UnknownCdmTypeException e) {
				//should not happen as USE_UNKNOWN is used
				rank = Rank.UNKNOWN_RANK();
			}
		}
		if ( (rank == null || rank.equals(Rank.UNKNOWN_RANK())) && strVerbatimRank != null){
			try {
				rank = Rank.getRankByNameOrAbbreviation(strVerbatimRank, USE_UNKNOWN);
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


	private TaxonBase getTaxonBase(Map<String, String> csvTaxonRecord) {
		TaxonNameBase<?,?> name = null;
		Reference<?> sec = null;
		TaxonBase<?> result;
		String status = csvTaxonRecord.get(TermUri.DWC_TAXONOMIC_STATUS);
		if (status != null){
			if (status.matches("accepted|valid|misapplied")){
				result = Taxon.NewInstance(name, sec);
			}else if (status.matches(".*synonym|invalid")){
				result = Synonym.NewInstance(name, sec);
			}else{
				result = Taxon.NewUnknownStatusInstance(name, sec);
			}
			csvTaxonRecord.remove(TermUri.DWC_TAXONOMIC_STATUS);
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
