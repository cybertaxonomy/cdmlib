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
import eu.etaxonomy.cdm.model.common.CdmBase;
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
		
		String message = "Not yet implemented"; 
		fireWarningEvent(message, item, 15);
		return new ListReader<MappedCdmBase>(resultList);
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
