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
import eu.etaxonomy.cdm.io.stream.StreamItem;
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
public class DwcTaxonCsv2CdmTaxonRelationConverter
        extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>
        implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>, ItemFilter<StreamItem> {

    private static final String SINGLE_CLASSIFICATION_ID = "1";

	private static final String SINGLE_CLASSIFICATION = "Single Classification";

	private static Logger logger = Logger.getLogger(DwcTaxonCsv2CdmTaxonRelationConverter.class);

	private static final String ID = "id";

	/**
	 * @param state
	 */
	public DwcTaxonCsv2CdmTaxonRelationConverter(DwcaDataImportStateBase state) {
		super(state);
	}


    @Override
    public boolean toBeRemovedFromStream(StreamItem item) {
        // TODO Auto-generated method stub
        return false;
    }


	@Override
    public IReader<MappedCdmBase> map(StreamItem item){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>();

		Map<String, String> csvRecord = item.map;
		Reference<?> sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;

		String id = csvRecord.get(ID);
		TaxonBase<?> taxonBase = getTaxonBase(id, item, null, state);
		if (taxonBase == null){
			String warning = "Taxon not available for id '%s'.";
			warning = String.format(warning, id);
			fireWarningEvent(warning, item, 8);
		}else{

			MappedCdmBase mcb = new MappedCdmBase(taxonBase);
			resultList.add(mcb);

			handleAcceptedNameUsage(item, state, taxonBase, id);

			handleParentNameUsage(item, state, taxonBase, id, resultList);

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
	public String getSourceId(StreamItem item) {
		String id = item.get(ID);
		return id;
	}


	private void handleSubGenus(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handleGenus(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handleFamily(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handleOrder(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handleClass(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handlePhylum(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handleKingdom(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state) {
		// TODO Auto-generated method stub

	}


	private void handleParentNameUsage(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state, TaxonBase<?> taxonBase, String id, List<MappedCdmBase> resultList) {
		if (exists(TermUri.DWC_PARENT_NAME_USAGE_ID, item) || exists(TermUri.DWC_PARENT_NAME_USAGE, item)){
			String parentId = item.get(TermUri.DWC_PARENT_NAME_USAGE_ID);
			if (id.equals(parentId)){
				//taxon can't be it's own child
				//TODO log
				return;
			}else if (taxonBase.isInstanceOf(Taxon.class)){
				Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
				Taxon parentTaxon = getTaxonBase(parentId, item, Taxon.class, state);
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
					try {
						classification.addParentChild(parentTaxon, taxon, citationForParentChild, null);
					} catch (IllegalStateException e) {
						String message = "Exception occurred when trying to add a child to a parent in a classification: %s";
						message = String.format(message, e.getMessage());
						fireWarningEvent(message, item, 12);
					}
				}
			}else if (taxonBase.isInstanceOf(Synonym.class)){
				if (! acceptedNameUsageExists(item) && state.getConfig().isUseParentAsAcceptedIfAcceptedNotExists()){
					handleAcceptedNameUsageParam(item, state, taxonBase, id, parentId);
				}else{
					String message = "PARENT_NAME_USAGE given for Synonym and ACCEPTED_NAME_USAGE also exists or configuration does not allow" +
							"to use ACCEPTED_NAME_USAGE as parent. This is not allowed in CDM.";
					//TODO check "is this Taxon"
					fireWarningEvent(message, item, 4);
				}
			}else{
				String message = "Unhandled case";
				fireWarningEvent(message, item, 12);
			}
		}


	}


	private Classification getClassification(StreamItem item, List<MappedCdmBase> resultList) {
		Set<Classification> resultSet = new HashSet<Classification>();
		//
		if (config.isDatasetsAsClassifications()){
			String datasetKey = item.get(TermUri.DWC_DATASET_ID);
			if (CdmUtils.areBlank(datasetKey,item.get(TermUri.DWC_DATASET_NAME))){
				datasetKey = DwcTaxonStreamItem2CdmTaxonConverter.NO_DATASET;
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


	private void handleAcceptedNameUsage(StreamItem item, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase> state, TaxonBase taxonBase, String id) {
		if (acceptedNameUsageExists(item)){
			String accId = item.get(TermUri.DWC_ACCEPTED_NAME_USAGE_ID);
			handleAcceptedNameUsageParam(item, state, taxonBase, id, accId);
		}else{
			if (logger.isDebugEnabled()){logger.debug("No accepted name usage");}
		}
	}


	/**
	 * @param item
	 * @param state
	 * @param taxonBase
	 * @param id
	 * @param accId
	 * @param taxStatus
	 */
	private void handleAcceptedNameUsageParam(StreamItem item,
			DwcaDataImportStateBase state, TaxonBase<?> taxonBase, String id, String accId) {
		if (id.equals(accId)){
			//mapping to itself needs no further handling
		}else{
			String taxStatus = item.get(TermUri.DWC_TAXONOMIC_STATUS);

			Taxon accTaxon = getTaxonBase(accId, item, Taxon.class, state);
			if (taxonBase.isInstanceOf(Synonym.class)){
				Synonym synonym = CdmBase.deproxy(taxonBase, Synonym.class);

				if (accTaxon == null){
						fireWarningEvent("NON-ID accepted Name Usage not yet implemented or taxon for name usage id not available", item, 4);
				} else{
					accTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(),null, null);
				}
				// FIXME : no information regarding misapplied name available at this point,
				//         hence a regexp check for 'misapplied' is done to add them as a relationship
			} else if(taxonBase.isInstanceOf(Taxon.class) && taxStatus.matches("misapplied.*")) {
				if (accTaxon == null){
					fireWarningEvent("NON-ID accepted (misapplied) Name Usage not yet implemented or taxon for name usage id not available", item, 4);
				} else{
					Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
					accTaxon.addMisappliedName(taxon,null,null);
				}
			} else {
				String message = "Accepted name usage is not of type synonym. This is not allowed in CDM. Can't create realtionship";
				//TODO check "is this Taxon"
				fireWarningEvent(message, item, 4);
			}
		}
	}


	/**
	 * @param item
	 * @return
	 */
	private boolean acceptedNameUsageExists(StreamItem item) {
		return exists(TermUri.DWC_ACCEPTED_NAME_USAGE_ID, item) || exists(TermUri.DWC_ACCEPTED_NAME_USAGE, item);
	}



//**************************** PARTITIONABLE ************************************************

	@Override
	protected void makeForeignKeysForItem(StreamItem item, Map<String, Set<String>> fkMap){
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
				value = DwcTaxonStreamItem2CdmTaxonConverter.NO_DATASET;
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
