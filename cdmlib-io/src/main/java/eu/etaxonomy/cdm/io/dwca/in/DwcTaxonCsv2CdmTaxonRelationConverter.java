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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
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
public class DwcTaxonCsv2CdmTaxonRelationConverter<STATE extends DwcaImportState> extends PartitionableConverterBase<DwcaImportState> 
						implements IPartitionableConverter<CsvStreamItem, INamespaceReader<CdmBase>, String>{
	private static final String SINGLE_CLASSIFICATION_ID = "1";

	private static final String SINGLE_CLASSIFICATION = "Single Classification";

	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonRelationConverter.class);

	private static final String ID = "id";
	
	/**
	 * @param state
	 */
	public DwcTaxonCsv2CdmTaxonRelationConverter(DwcaImportState state) {
		super(state);
	}


	public IReader<MappedCdmBase> map(CsvStreamItem item){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csvRecord = item.map;
		Reference<?> sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;
		
		String id = csvRecord.get(ID);
		TaxonBase<?> taxonBase = getTaxonBase(id, item, null, state);
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
				String parentId = item.get(TermUri.DWC_PARENT_NAME_USAGE_ID);
				Taxon parentTaxon = getTaxonBase(parentId, item, Taxon.class,state);
				if (parentTaxon == null){
					String message = "Can't find parent taxon with id '%s' and NON-ID parent Name Usage not yet implemented.";
					message = String.format(message, StringUtils.isBlank(parentId)?"-": parentId);
					fireWarningEvent(message, item, 4);
				}else{
					Classification classification = getClassification(item, resultList);
					Reference<?> citationForParentChild = null;
					if (classification == null){
						String warning = "Classification not found. Can't create parent-child relationship";
						fireWarningEvent(warning, item, 12);
					}
					classification.addParentChild(parentTaxon, taxon, citationForParentChild, null);
				}
			}else{
				String message = "PARENT_NAME_USAGE given for Synonym. This is not allowed in CDM.";
				//TODO check "is this Taxon"
				fireWarningEvent(message, item, 4);
			}
		}

		
	}


	private Classification getClassification(CsvStreamItem item, List<MappedCdmBase> resultList) {
		Set<Classification> resultSet = new HashSet<Classification>();
		//
		if (config.isDatasetsAsClassifications()){
			String datasetKey = item.get(TermUri.DWC_DATASET_ID);
			if (CdmUtils.areBlank(datasetKey,item.get(TermUri.DWC_DATASET_NAME))){
				datasetKey = DwcTaxonCsv2CdmTaxonConverter.NO_DATASET;
			}
			
			resultSet.addAll(state.get(TermUri.DWC_DATASET_ID.toString(), datasetKey, Classification.class));
			resultSet.addAll(state.get(TermUri.DWC_DATASET_NAME.toString(), item.get(TermUri.DWC_DATASET_NAME), Classification.class));
		//TODO accordingToAsClassification
		//single classification
		}else{
			resultSet.addAll(state.get(SINGLE_CLASSIFICATION, SINGLE_CLASSIFICATION_ID, Classification.class));
			
			//classification does not yet exist
			if (resultSet.isEmpty()){
				Classification newClassification = Classification.NewInstance("Darwin Core Classification");
				if (config.getClassificationUuid() != null){
					newClassification.setUuid(config.getClassificationUuid());
				}
				if (StringUtils.isNotBlank(config.getClassificationName())){
					newClassification.setName(LanguageString.NewInstance(config.getClassificationName(), Language.DEFAULT()));
				}
				resultList.add(new MappedCdmBase(SINGLE_CLASSIFICATION, SINGLE_CLASSIFICATION_ID, newClassification));
				resultSet.add(newClassification);
			}
		}
		if (resultSet.isEmpty()){
			return null;
		}else if (resultSet.size() > 1){
			fireWarningEvent("Dataset is ambigous. I take arbitrary one.", item, 8);
		}
		return resultSet.iterator().next();
	}


	private void handleAcceptedNameUsage(CsvStreamItem item, DwcaImportState state, TaxonBase taxonBase, String id) {
		if (exists(TermUri.DWC_ACCEPTED_NAME_USAGE_ID, item) || exists(TermUri.DWC_ACCEPTED_NAME_USAGE, item)){
			String accId = item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID);
			if (id.equals(accId)){
				return;   //mapping to itself needs no further handling
			}
			if (taxonBase.isInstanceOf(Synonym.class)){
				Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);
				Taxon accTaxon = getTaxonBase(accId, item, Taxon.class, state);
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



//**************************** PARTITIONABLE ************************************************

	@Override
	protected void makeForeignKeysForItem(CsvStreamItem item, Map<String, Set<String>> fkMap){
		String value;
		String key;
		if ( hasValue(value = item.get(ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if ( hasValue(value = item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID.toString()))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if ( hasValue(value = item.get(key = TermUri.DWC_PARENT_NAME_USAGE_ID.toString())) ){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		if ( hasValue(value = item.get(key = TermUri.DWC_NAME_ACCORDING_TO_ID.toString()))){
			//TODO
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		
		//classification
		if (config.isDatasetsAsClassifications()){
			boolean hasDefinedClassification = false;
			if ( hasValue(value = item.get(key = TermUri.DWC_DATASET_ID.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
				hasDefinedClassification = true;
			}
			if ( hasValue(value = item.get(key = TermUri.DWC_DATASET_NAME.toString()))){
				Set<String> keySet = getKeySet(key, fkMap);
				keySet.add(value);
				hasDefinedClassification = true;
			}
			if (! hasDefinedClassification){
				Set<String> keySet = getKeySet(TermUri.DWC_DATASET_ID.toString(), fkMap);
				value = DwcTaxonCsv2CdmTaxonConverter.NO_DATASET;
				keySet.add(value);
			}
		}else{
			key = SINGLE_CLASSIFICATION;
			value = SINGLE_CLASSIFICATION_ID;
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		
		//TODO cont.
	}
	
	@Override
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<String>();
 		
		result.add(TermUri.DWC_TAXON.toString());
		
		result.add(TermUri.DWC_ACCEPTED_NAME_USAGE_ID.toString());
 		result.add(TermUri.DWC_PARENT_NAME_USAGE_ID.toString());
 		
 		result.add(TermUri.DWC_NAME_ACCORDING_TO_ID.toString());
 		result.add(TermUri.DWC_NAME_ACCORDING_TO.toString());
 		if (config.isDatasetsAsClassifications()){
 			result.add(TermUri.DWC_DATASET_ID.toString());
 			result.add(TermUri.DWC_DATASET_NAME.toString());
 		}else{
 			result.add(SINGLE_CLASSIFICATION);
 		}
 		
 		return result;
	}
	

//************************************* TO STRING ********************************************
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
