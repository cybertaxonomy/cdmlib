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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
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
public class DwcTaxonCsv2CdmTaxonConverter extends PartitionableConverterBase<DwcaImportState> implements IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String>{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonConverter.class);

	private static final String ID = "id";
	// key for for case that no dataset information is supplied, TODO use something better
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
		
		//TODO source reference
		Reference<?> sourceReference = null;
		String sourceReferenceDetail = null;
		
		//taxon
		TaxonBase<?> taxonBase = getTaxonBase(csvTaxonRecord);
		MappedCdmBase  mcb = new MappedCdmBase(csvTaxonRecord.term, csvTaxonRecord.get(ID), taxonBase);
		resultList.add(mcb);
		
		//original source
		String id = csvTaxonRecord.get(ID);
		IdentifiableSource source = taxonBase.addSource(id, "Taxon", sourceReference, sourceReferenceDetail);
		MappedCdmBase mappedSource = new MappedCdmBase(csvTaxonRecord.get(ID), source);
		resultList.add(mappedSource);
		csvTaxonRecord.remove(ID);
		
		//rank
		NomenclaturalCode nomCode = getNomCode(csvTaxonRecord);
		Rank rank = getRank(csvTaxonRecord, nomCode);

		//name
		TaxonNameBase<?,?> name = getScientificName(csvTaxonRecord, nomCode, rank, resultList);
		taxonBase.setName(name);
		
		//sec
		Reference<?> sec = getNameAccordingTo(csvTaxonRecord, resultList);
		taxonBase.setSec(sec);

		//classification
		handleDataset(csvTaxonRecord, taxonBase, resultList, sourceReference, sourceReferenceDetail);
		
		//NON core
	    //term="http://purl.org/dc/terms/identifier"
		//currently only LSIDs
		handleIdentifier(csvTaxonRecord, taxonBase); 

		
		
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
//		==> see scientific name
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


	
	//TODO handle non LSIDs
	//TODO handle LSIDs for names
	private void handleIdentifier(CsvStreamItem csvTaxonRecord, TaxonBase<?> taxonBase) {
		String identifier = csvTaxonRecord.get(TermUri.DC_IDENTIFIER);
		if (StringUtils.isNotBlank(identifier)){
			if (identifier.trim().startsWith("urn:lsid")){
				try {
					LSID lsid = new LSID(identifier);
					taxonBase.setLsid(lsid);
				} catch (MalformedLSIDException e) {
					String message = "LSID is malformed and can't be handled as LSID: %s";
					message = String.format(message, identifier);
					fireWarningEvent(message, csvTaxonRecord, 4);
				} 
			}else{
				String message = "Identifier type not supported: %s";
				message = String.format(message, identifier);
				fireWarningEvent(message, csvTaxonRecord, 4);
			}
		}
		
	}


	private void handleDataset(CsvStreamItem item, TaxonBase<?> taxonBase, List<MappedCdmBase> resultList, Reference<?> sourceReference, String sourceReferecenDetail) {
		if (state.getConfig().isDatasetsAsClassifications()){
			String datasetId = CdmUtils.Nz(item.get(TermUri.DWC_DATASET_ID)).trim();
			String datasetName = CdmUtils.Nz(item.get(TermUri.DWC_DATASET_NAME)).trim();
			if (CdmUtils.areBlank(datasetId, datasetName) ){
				datasetId = NO_DATASET;
			}
			
			//check id
			boolean classificationExists = state.exists(TermUri.DWC_DATASET_ID.toString() , datasetId, Classification.class);
			
			//check name
			if (!classificationExists){
				classificationExists = state.exists(TermUri.DWC_DATASET_NAME.toString() , datasetName, Classification.class);
			}
			
			//if not exists, create new
			if (! classificationExists){
				String classificationName = StringUtils.isBlank(datasetName)? datasetId : datasetName;
				if (classificationName.equals(NO_DATASET)){
					classificationName = "Classification (no name)";  //TODO define by config or zipfile or metadata
				}
				
				String classificationId = StringUtils.isBlank(datasetId)? datasetName : datasetId;
				Classification classification = Classification.NewInstance(classificationName);
				//source
				IdentifiableSource source = classification.addSource(classificationId, "Dataset", sourceReference, sourceReferecenDetail);
				//add to result
				resultList.add(new MappedCdmBase(TermUri.DWC_DATASET_ID, datasetId, classification));
				resultList.add(new MappedCdmBase(TermUri.DWC_DATASET_NAME, datasetName, classification));
				resultList.add(new MappedCdmBase(source));
				//TODO this is not so nice but currently necessary as classifications are requested in the same partition
				state.putMapping(TermUri.DWC_DATASET_ID.toString(), classificationId, classification);
				state.putMapping(TermUri.DWC_DATASET_NAME.toString(), classificationName, classification);
			}
		}else{
			//dataset as original source
			TermUri idTerm = TermUri.DWC_DATASET_ID;
			TermUri strTerm = TermUri.DWC_DATASET_NAME;
			Reference<?> citation = getReference(item, resultList, idTerm, strTerm);
			taxonBase.addSource(null, null, citation, null);
		}
		
		//remove to later check if all attributes were used
		item.remove(TermUri.DWC_DATASET_ID);
		item.remove(TermUri.DWC_DATASET_NAME);
		
	}

	
	@Override
	public String getSourceId(CsvStreamItem item) {
		String id = item.get(ID);
		return id;
	}

	private Reference<?> getNameAccordingTo(CsvStreamItem item, List<MappedCdmBase> resultList) {
		TermUri idTerm = TermUri.DWC_NAME_ACCORDING_TO_ID;
		TermUri strTerm = TermUri.DWC_NAME_ACCORDING_TO;
		Reference<?> secRef = getReference(item, resultList, idTerm, strTerm);
		return secRef;
		
	}

	private NomenclaturalCode getNomCode(CsvStreamItem item) {
		String strNomCode = getValue(item, TermUri.DWC_NOMENCLATURAL_CODE);
		NomenclaturalCode nomCode = null;
		// by Nomcenclatural Code
		if (strNomCode != null){
			nomCode = NomenclaturalCode.fromString(strNomCode);
			if (nomCode == null){
				String message = "NomCode '%s' not recognized";
				message = String.format(message, strNomCode);
				fireWarningEvent(message, item, 4);
			}else{
				return nomCode;
			}
		}
		// by Kingdom
		String strKingdom = getValue(item, TermUri.DWC_KINGDOM);
		if (strKingdom.equalsIgnoreCase("Plantae")){
			nomCode = NomenclaturalCode.ICBN;
		}else if (strKingdom.equalsIgnoreCase("Animalia")){
			nomCode = NomenclaturalCode.ICZN;
		}else if (strKingdom.equalsIgnoreCase("Fungi")){
			nomCode = NomenclaturalCode.ICBN;
		}
		//TODO further kingdoms
		if (nomCode == null){
			//TODO warning
		}
		return nomCode;
	}


	private TaxonNameBase<?,?> getScientificName(CsvStreamItem item, NomenclaturalCode nomCode, Rank rank, List<MappedCdmBase> resultList) {
		TaxonNameBase<?,?> name = null;
		String strScientificName = getValue(item, TermUri.DWC_SCIENTIFIC_NAME);
		//Name
		if (strScientificName != null){
			INonViralNameParser<?> parser = NonViralNameParserImpl.NewInstance();
			name = parser.parseFullName(strScientificName, nomCode, rank);
			if (rank != null && name != null && name.getRank() != null && 
					! rank.equals(name.getRank())){
				String message = "Parsed rank %s (%s) differs from rank %s given by fields 'taxonRank' or 'verbatimTaxonRank'";
				message = String.format(message, name.getRank().getTitleCache(), strScientificName, rank.getTitleCache());
				fireWarningEvent(message, item, 4);
			}
			checkAuthorship(name, item);
			resultList.add(new MappedCdmBase(TermUri.DWC_SCIENTIFIC_NAME, strScientificName, name));
		}
		//By ID
		String strScientificNameId = getValue(item, TermUri.DWC_SCIENTIFIC_NAME_ID);
		if (strScientificNameId != null){
			if (state.getConfig().isScientificNameIdAsOriginalSourceId()){
				if (name != null){
					Reference<?> sourceReference = null; //FIXME
					IdentifiableSource source = IdentifiableSource.NewInstance(strScientificNameId, TermUri.DWC_SCIENTIFIC_NAME_ID.toString(), sourceReference, null);
					name.addSource(source);
				}
			}else{
				String message = "ScientificNameId not yet implemented: '%s'";
				message = String.format(message, strScientificNameId);
				fireWarningEvent(message, item, 4);
			}
		}
		
		//namePublishedIn
		TermUri idTerm = TermUri.DWC_NAME_PUBLISHED_IN_ID;
		TermUri strTerm = TermUri.DWC_NAME_PUBLISHED_IN;
		Reference<?> nomRef = getReference(item, resultList, idTerm, strTerm);
		
		if (name != null){
			if (nomRef != null){
				name.setNomenclaturalReference(nomRef);  //check if name already has a nomRef, shouldn't be the case usually
			}
		}else{
			if (nomRef != null){
				String message = "NamePublishedIn information available but no name exists";
				fireWarningEvent(message, item, 4);
			}
		}
		return name;
	}


	private Reference<?> getReference(CsvStreamItem item, List<MappedCdmBase> resultList, TermUri idTerm, TermUri strTerm) {
		
		Reference result = null;
		if (exists(idTerm, item) || exists(strTerm, item)){
			String nomRefId = CdmUtils.Nz(item.get(idTerm)).trim();
			String nomRefStr = CdmUtils.Nz(item.get(strTerm)).trim();
			if (StringUtils.isNotBlank(nomRefId)){
				List<Reference> nomRefs = state.get(idTerm.toString(), nomRefId, Reference.class);
				if (nomRefs.size() == 0){
					//references should already exist in store if not linking to external links like URLs
					String message = "External namePublishedInIDs are not yet supported";
					fireWarningEvent(message, item, 4);
				}else{
					//TODO handle list.size > 1 , do we need a list here ?
					result = nomRefs.get(0);
				}
			}
			if (result == null){
				List<Reference> nomRefs = state.get(strTerm.toString(), nomRefStr, Reference.class);
				if (nomRefs.size() > 0){
					//TODO handle list.size > 1 , do we need a list here ?
					result = nomRefs.get(0);
				}else{
					// new Reference
					result = ReferenceFactory.newGeneric();  //TODO handle other types if possible
					result.setTitleCache(nomRefStr, true);
					//TODO distinguish available year, authorship, etc. if
					resultList.add(new MappedCdmBase(strTerm, nomRefStr, result));
				}
			}
		}
		return result;
	}


	//TODO we may configure in configuration that scientific name never includes Authorship
	private void checkAuthorship(TaxonNameBase nameBase, CsvStreamItem item) {
		if (!nameBase.isInstanceOf(NonViralName.class)){
			return;
		}
		NonViralName<?> nvName = CdmBase.deproxy(nameBase, NonViralName.class); 
		String strAuthors = getValue(item, TermUri.DWC_SCIENTIFIC_NAME_AUTHORS);
		
		if (! nvName.isProtectedTitleCache()){
			if (StringUtils.isBlank(nvName.getAuthorshipCache())){
				//TODO some more sophisticated stuff can be done here like parsing etc.
				nvName.setAuthorshipCache(strAuthors);
				//TODO warning (scientific name should always include authorship)
			}
		}
		
	}


	private Rank getRank(CsvStreamItem csvTaxonRecord, NomenclaturalCode nomCode) {
		boolean USE_UNKNOWN = true;
		Rank rank = null;
		String strRank = getValue(csvTaxonRecord,TermUri.DWC_TAXON_RANK);
		String strVerbatimRank = getValue(csvTaxonRecord,TermUri.DWC_VERBATIM_TAXON_RANK);
		if (strRank != null){
			try {
				rank = Rank.getRankByEnglishName(strRank, nomCode, USE_UNKNOWN);
				if (rank.equals(Rank.UNKNOWN_RANK())){
					rank = Rank.getRankByNameOrAbbreviation(strRank, USE_UNKNOWN);
					if (rank.equals(Rank.UNKNOWN_RANK())){
						String message = "Rank can not be defined for '%s'";
						message = String.format(message, strRank);
						fireWarningEvent(message, csvTaxonRecord, 4);
					}
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
		boolean isMissaplied = false;
		if (taxStatus != null){
			if (taxStatus.matches("accepted|valid")){
				status += "A";
			}else if (taxStatus.matches(".*synonym|invalid")){
				status += "S";
			}if (taxStatus.matches("misapplied")){
				status += "M";
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
		if (status.contains("A") || status.contains("M")){
			result = Taxon.NewInstance(name, sec);
			if (status.contains("S") && ! status.contains("M") ){
				String message = "Ambigous taxon status (%s)";
				message = String.format(message, status);
				fireWarningEvent(message, item, 6);
			}
		}else if (status.contains("S")){
			result = Synonym.NewInstance(name, sec);
		}else{
			result = Taxon.NewUnknownStatusInstance(name, sec);
		}
			
		return result;

	}
	
// ********************** PARTITIONABLE ****************************************/


	@Override
	protected void makeForeignKeysForItem(CsvStreamItem item, Map<String, Set<String>> fkMap) {
		String value;
		String key;
		
		//namePublishedIn
		if ( hasValue(value = item.get(key = TermUri.DWC_NAME_PUBLISHED_IN_ID.toString()))){
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if (state.getConfig().isDeduplicateNamePublishedIn()){
			if ( hasValue(value = item.get(key = TermUri.DWC_NAME_PUBLISHED_IN.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
			}
		}
		
		//nameAccordingTo
		if ( hasValue(value = item.get(key = TermUri.DWC_NAME_ACCORDING_TO_ID.toString()))){
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if ( hasValue(value = item.get(key = TermUri.DWC_NAME_ACCORDING_TO.toString()))){
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		
		
		//dataset
		if (! state.getConfig().isDatasetsAsClassifications()){
			//nameAccordingTo
			if ( hasValue(value = item.get(key = TermUri.DWC_DATASET_ID.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
			}
			if ( hasValue(value = item.get(key = TermUri.DWC_DATASET_NAME.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
			}
		}
		
	}
	
//** ***************************** TO STRING *********************************************/
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}


	
}
