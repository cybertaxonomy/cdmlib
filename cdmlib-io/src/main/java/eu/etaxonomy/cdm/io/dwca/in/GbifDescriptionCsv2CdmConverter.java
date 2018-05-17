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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.MappedCdmBase;
import eu.etaxonomy.cdm.io.stream.PartitionableConverterBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 22.11.2011
 *
 */
public class GbifDescriptionCsv2CdmConverter extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>
						implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifDescriptionCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public GbifDescriptionCsv2CdmConverter(DwcaDataImportStateBase state) {
		super(state);
	}

	@Override
    public IReader<MappedCdmBase<? extends CdmBase>> map(StreamItem item ){
		List<MappedCdmBase<? extends CdmBase>> resultList = new ArrayList<>();

		Map<String, String> csv = item.map;
		Reference sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;

		String id = getSourceId(item);
		Taxon taxon = getTaxonBase(id, item, Taxon.class, state);
		if (taxon != null){

			String description = item.get(TermUri.DC_DESCRIPTION);
			//String license = item.get(TermUri.DC_LICENSE);//lorna check - often empty in SP dwca

			Language language = getDcLanguage(item, resultList);
			if(language == null){
			    language = Language.UNKNOWN_LANGUAGE();
			}

			if (isNotBlank(description)){
				Feature feature = getFeatureByDcType(item, resultList);

				TaxonDescription taxonDescription = getTaxonDescription(taxon, false);

				//deduplicate - if this taxonDescription already got these Rights attached
				boolean addRights = true;
				String rights = item.get(TermUri.DC_RIGHTS);
				Set<Rights> allRights = taxonDescription.getRights();
				for (Rights r : allRights) {
					if (r.getText() == rights) {
						addRights = false;
					}
				}

				if (addRights && (isNotBlank(rights))) {
					Rights copyright = Rights.NewInstance(rights, language);
					taxonDescription.addRights(copyright);
				}

				TextData descElement = TextData.NewInstance(feature);

				//Language language = getLanguage(item);  //TODO
				descElement.putText(language,description);
				taxonDescription.addElement(descElement);
			}else{
				String message = "Description is empty. Description item will not be imported.";
				fireWarningEvent(message, item, 4);
			}

			MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(item.term, csv.get(CORE_ID), taxon);
			resultList.add(mcb);
		}else{
			String message = "Taxon is not available for id '%s'";
			message = String.format(message, id);
			fireWarningEvent(message, item, 12);
		}

		return new ListReader<>(resultList);
	}


	/**
	 * Determines the feature by the dc:type attribute. Tries to reuse existing
	 * features.
	 * @param item
	 * @param resultList
	 * @return
	 */
	private Feature getFeatureByDcType(StreamItem item, List<MappedCdmBase<? extends CdmBase>> resultList) {
		String descriptionType = item.get(TermUri.DC_TYPE);
		item.remove(TermUri.DC_TYPE);

		try {
			Feature feature = state.getTransformer().getFeatureByKey(descriptionType);
			if (feature != null){
				return feature;
			}
			String namespace = Feature.class.getCanonicalName();
			List<Feature> features = state.get(namespace, descriptionType, Feature.class);
			if (features.size() > 1){
				String message = "There is more than 1 cdm entity matching given locationId '%s'. I take an arbitrary one.";
				fireWarningEvent(String.format(message, item), item, 4);
				return features.iterator().next();
			}
			UUID featureUuid = state.getTransformer().getFeatureUuid(descriptionType);
			feature = state.getCurrentIO().getFeature(state, featureUuid, descriptionType, descriptionType, null, null);
			if (feature == null){
				feature = Feature.NewInstance(descriptionType, descriptionType, null);
				feature.setSupportsTextData(true);
//				state.putMapping(namespace, type, feature);
				state.getCurrentIO().saveNewTerm(feature);
				MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(namespace, descriptionType, feature);
				resultList.add(mcb);
			}
			return feature;
		} catch (UndefinedTransformerMethodException e) {
			String message = "GetFeature not yet supported by DwcA-Transformer. This should not have happend. Please contact your application developer.";
			fireWarningEvent(message, item, 8);
			return null;
		}

	}

	@Override
	public String getSourceId(StreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}


//********************** PARTITIONABLE **************************************/

	@Override
	protected void makeForeignKeysForItem(StreamItem item, Map<String, Set<String>> fkMap) {
		String value;
		String key;
		if ( hasValue(value = item.get(CORE_ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
	}


	@Override
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<String>();
 		result.add(TermUri.DWC_TAXON.toString());
 		result.add(TermUri.DC_LANGUAGE.toString());
        return result;
	}

//******************* TO STRING ******************************************/

	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
