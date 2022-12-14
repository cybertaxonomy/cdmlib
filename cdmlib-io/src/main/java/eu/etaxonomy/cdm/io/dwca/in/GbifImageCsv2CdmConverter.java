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

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.MappedCdmBase;
import eu.etaxonomy.cdm.io.stream.PartitionableConverterBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 22.11.2011
 *
 */
public class GbifImageCsv2CdmConverter extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>
						implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>{

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(GbifImageCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public GbifImageCsv2CdmConverter(DwcaDataImportStateBase state) {
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

//			xxx;

		}else{
			String message = "Can't retrieve taxon from database for id '%s'";
			fireWarningEvent(String.format(message, id), item, 12);
		}

		//return
		return new ListReader<>(resultList);
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
		//taxon
		if ( hasValue(value = item.get(CORE_ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}

	}


	@Override
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<>();
 		result.add(TermUri.DWC_TAXON.toString());
 		return result;
	}

//******************* TO STRING ******************************************/

	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
