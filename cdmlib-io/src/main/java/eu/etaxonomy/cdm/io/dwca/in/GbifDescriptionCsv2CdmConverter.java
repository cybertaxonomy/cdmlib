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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifDescriptionCsv2CdmConverter extends PartitionableConverterBase<DwcaImportState>  
						implements IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String>{
	
	private static final Logger logger = Logger.getLogger(GbifDescriptionCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";
	
	/**
	 * @param state
	 */
	public GbifDescriptionCsv2CdmConverter(DwcaImportState state) {
		super(state);
	}

	public IReader<MappedCdmBase> map(CsvStreamItem item ){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csv = item.map;
		Reference<?> sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;
		
		String id = getSourceId(item);
		Taxon taxon = getTaxonBase(id, item, Taxon.class, state);
		if (taxon != null){
			MappedCdmBase  mcb = new MappedCdmBase(item.term, csv.get(CORE_ID), taxon);
			resultList.add(mcb);
		}else{
			String message = "Taxon is null";
			fireWarningEvent(message, item, 12);
		}
		
		Feature feature = getFeatureByDcType(item);

		
		String message = "Not yet implemented"; 
		fireWarningEvent(message, item, 15);
		return new ListReader<MappedCdmBase>(resultList);
	}

	
	private Feature getFeatureByDcType(CsvStreamItem item) {
		String type = item.get(TermUri.DC_TYPE);

		Feature feature;
		try {
			feature = state.getTransformer().getFeatureByKey(type);
			if (feature != null){
				return feature;
			}
			String namespace = Feature.class.getSimpleName();
			List<Feature> features = state.get(namespace, type, Feature.class);
			if (features.size() > 1){
				String message = "There is more than 1 cdm entity matching given locationId '%s'. I take an arbitrary one.";
				fireWarningEvent(String.format(message, item), item, 4);
				return features.iterator().next();
			}	
			UUID featureUuid = state.getTransformer().getFeatureUuid(type);
			feature = state.getCurrentIO().getFeature(state, featureUuid, type, type, null, null);
			if (feature == null){
				feature = Feature.NewInstance(type, type, null);
				feature.setSupportsTextData(true);
				state.putMapping(namespace, type, feature);
			}
			return feature;
		} catch (UndefinedTransformerMethodException e) {
			String message = "GetFeature not yet supported by DwcA-Transformer. This should not have happend. Please contact your application developer.";
			fireWarningEvent(message, item, 8);
			return null;
		}
		
	}

	@Override
	public String getSourceId(CsvStreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}

	
//********************** PARTITIONABLE **************************************/

	@Override
	protected void makeForeignKeysForItem(CsvStreamItem item, Map<String, Set<String>> fkMap) {
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
 		return result;
	}
	
//******************* TO STRING ******************************************/
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
