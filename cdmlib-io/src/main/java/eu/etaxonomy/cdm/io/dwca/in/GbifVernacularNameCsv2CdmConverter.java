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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifVernacularNameCsv2CdmConverter extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>> 
					implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifVernacularNameCsv2CdmConverter.class);
	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public GbifVernacularNameCsv2CdmConverter(DwcaDataImportStateBase state) {
		super(state);
	}


	public IReader<MappedCdmBase> map(StreamItem item ){
		List<MappedCdmBase> resultList = new ArrayList<MappedCdmBase>(); 
		
		Map<String, String> csv = item.map;
		Reference<?> sourceReference = state.getTransactionalSourceReference();
		String sourceReferecenDetail = null;
		
		String id = csv.get(CORE_ID);
		Taxon taxon = getTaxonBase(id, item, Taxon.class, state);
		if (taxon != null){
			MappedCdmBase  mcb = new MappedCdmBase(item.term, csv.get(CORE_ID), taxon);
			String vernacular = item.get(TermUri.DWC_VERNACULAR_NAME);
			//TODO language, area,
			TaxonDescription desc = getTaxonDescription(taxon, false);
			
			//TODO
			Language language = null;
			CommonTaxonName commonName = CommonTaxonName.NewInstance(vernacular, language);
			desc.addElement(commonName);
			resultList.add(mcb);
		}else{
			String message = "Can't retrieve taxon from database for id '%s'";
			fireWarningEvent(String.format(message, id), item, 12);
		}
		
		//return
		return new ListReader<MappedCdmBase>(resultList);
		
	}
	
	
	@Override
	public String getSourceId(StreamItem item) {
		String id = item.get(CORE_ID);
		return id;
	}

//**************************** PARTITIONABLE ************************************************

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
	public final Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<String>();
 		result.add(TermUri.DWC_TAXON.toString());
 		return result;
	}	
	
//************************ STRING ************************************************/



	@Override
	public String toString(){
		return this.getClass().getName();
	}





}
