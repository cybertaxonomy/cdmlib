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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
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
public class DwcTaxonCsv2CdmTaxonConverter extends ConverterBase<DwcaImportState> implements IConverter<CsvStreamItem, IReader<CdmBase>, String>{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonConverter.class);

	private static final String ID = "id";
	// key for for case that no dataset information is supplied
	public static final String NO_DATASET = "no_dataset_jli773oebhjklw";

	
	/**
	 * @param state
	 */
	public DwcTaxonCsv2CdmTaxonConverter(DwcaImportState state) {
		super();
		this.state = state;
	}


	public IReader<MappedCdmBase> map(CsvStreamItem csvTaxonRecord){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Reference<?> sourceReference = null;
		String sourceReferenceDetail = null;
		
		//taxon
		TaxonBase<?> taxonBase = getTaxonBase(csvTaxonRecord);
		MappedCdmBase  mcb = new MappedCdmBase(csvTaxonRecord.term, csvTaxonRecord.get(ID), taxonBase);
		resultList.add(mcb);
		
		//source
		String id = csvTaxonRecord.get(ID);
		IdentifiableSource source = taxonBase.addSource(id, "Taxon", sourceReference, sourceReferenceDetail);
		MappedCdmBase mappedSource = new MappedCdmBase(csvTaxonRecord.get(ID), source);
		resultList.add(mappedSource);
		csvTaxonRecord.remove(ID);
		
		//rank
		Rank rank = getRank(csvTaxonRecord);

		//name
		NomenclaturalCode nomCode = getNomCode(csvTaxonRecord);
		TaxonNameBase<?,?> name = getScientificName(csvTaxonRecord, nomCode, rank);
		taxonBase.setName(name);
		
		//sec
		Reference<?> sec = getNameAccordingTo(csvTaxonRecord);
		taxonBase.setSec(sec);

		//classification
		handleDataset(csvTaxonRecord, resultList, sourceReference, sourceReferenceDetail);
		
		
//		    <field index="0" term="http://rs.tdwg.org/dwc/terms/taxonID"/>
		
//		    <!-- LSID -->
//		    <field index="1" term="http://purl.org/dc/terms/identifier"/>


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

		return new ListReader<MappedCdmBase>(resultList);
	}


	private void handleDataset(CsvStreamItem csvTaxonRecord, List<MappedCdmBase> resultList, Reference<?> sourceReference, String sourceReferecenDetail) {
		String datasetId = CdmUtils.Nz(csvTaxonRecord.get(TermUri.DWC_DATASET_ID)).trim();
		String datasetName = CdmUtils.Nz(csvTaxonRecord.get(TermUri.DWC_DATASET_NAME)).trim();
		if (CdmUtils.areBlank(datasetId, datasetName) ){
			datasetId = NO_DATASET;
		}
		
		//check id
		boolean classificationExists = state.exists(TermUri.DWC_DATASET_ID.toString() , datasetId, Classification.class);
		
		//check name
		if (!classificationExists){
			classificationExists = state.exists(TermUri.DWC_DATASET_ID.toString() , datasetName, Classification.class);
		}
		
		//if not exists, create new
		if (! classificationExists){
			String classificationName = StringUtils.isBlank(datasetName)? datasetId : datasetName;
			String classificationId = StringUtils.isBlank(datasetId)? datasetName : datasetId;
			Classification classification = Classification.NewInstance(classificationName);
			//source
			IdentifiableSource source = classification.addSource(classificationId, "Dataset", sourceReference, sourceReferecenDetail);
			//add to result
			resultList.add(new MappedCdmBase(TermUri.DWC_DATASET_ID, datasetId, classification));
			resultList.add(new MappedCdmBase(TermUri.DWC_DATASET_NAME, datasetName, classification));
			resultList.add(new MappedCdmBase(source));
		}
		
		//remove to later check if all attributes were used
		csvTaxonRecord.remove(TermUri.DWC_DATASET_ID);
		csvTaxonRecord.remove(TermUri.DWC_DATASET_NAME);
		
	}

	
	@Override
	public String getSourceId(CsvStreamItem item) {
		String id = item.get(ID);
		return id;
	}

	private Reference<?> getNameAccordingTo(CsvStreamItem csvTaxonRecord) {
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


	private TaxonBase<?> getTaxonBase(CsvStreamItem item) {
		TaxonNameBase<?,?> name = null;
		Reference<?> sec = null;
		TaxonBase<?> result;
		String taxStatus = item.get(TermUri.DWC_TAXONOMIC_STATUS);
		String status = "";
		if (taxStatus != null){
			if (taxStatus.matches("accepted|valid|misapplied")){
				status += "A";
			}else if (taxStatus.matches(".*synonym|invalid")){
				status += "S";
			}else{
				status += "?";
			}
			item.remove(TermUri.DWC_TAXONOMIC_STATUS);
		}
		if (! CdmUtils.isBlank(item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID))){
			// acceptedNameUsageId = id
			if (getSourceId(item).equals(item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID))){
				status += "A";
			}else{
				status += "S";
			}
		}
		if (status.contains("A")){
			result = Taxon.NewInstance(name, sec);
			if (status.contains("S")){
				String message = "Ambigous taxon status.";
				fireWarningEvent(message, item, 6);
			}
		}else if (status.contains("S")){
			result = Synonym.NewInstance(name, sec);
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
