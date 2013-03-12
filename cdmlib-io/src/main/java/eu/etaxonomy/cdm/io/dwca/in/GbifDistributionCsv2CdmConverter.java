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
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifDistributionCsv2CdmConverter extends PartitionableConverterBase<DwcaImportState>  
						implements IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String>{
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifDistributionCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";
	
	/**
	 * @param state
	 */
	public GbifDistributionCsv2CdmConverter(DwcaImportState state) {
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
			
			String locationId = item.get(TermUri.DWC_LOCATION_ID);
			NamedArea area = getAreaByLocationId(item, locationId);
			if (area != null){
				MappedCdmBase  mcb = new MappedCdmBase(item.term, csv.get(TermUri.DWC_LOCATION_ID), area);
				resultList.add(mcb);
			}else if (! config.isExcludeLocality()){
				String locality = item.get(TermUri.DWC_LOCALITY);
				area = getAreaByLocality(item, locality);
				MappedCdmBase  mcb = new MappedCdmBase(item.term, csv.get(TermUri.DWC_LOCALITY), area);
				resultList.add(mcb);
			}
			
			if (area != null){
				
				//TODO language, area,
				TaxonDescription desc = getTaxonDescription(taxon, false);
				
				//TODO
				PresenceAbsenceTermBase<?> status = null;
				Distribution distribution = Distribution.NewInstance(area, status);
				desc.addElement(distribution);
				
				//save taxon
				MappedCdmBase  mcb = new MappedCdmBase(item.term, csv.get(CORE_ID), taxon);
				resultList.add(mcb);
			}

		}else{
			String message = "Can't retrieve taxon from database for id '%s'";
			fireWarningEvent(String.format(message, id), item, 12);
		}
		
		//return
		return new ListReader<MappedCdmBase>(resultList);
	}


	
	private NamedArea getAreaByLocality(CsvStreamItem item, String locality) {
		String namespace = TermUri.DWC_LOCALITY.toString();
		List<NamedArea> result = state.get(namespace, locality, NamedArea.class);
		if (result.isEmpty()){
			NamedArea newArea = NamedArea.NewInstance(locality, locality, locality);
			newArea.setTitleCache(locality, true);
			return newArea;
		}
		if (result.size() > 1){
			String message = "There is more than 1 cdm entity matching given locality '%s'. I take an arbitrary one.";
			fireWarningEvent(String.format(message, locality), item, 4);
		}
		return (NamedArea)result.iterator().next();
	}

	private NamedArea getAreaByLocationId(CsvStreamItem item, String locationId) {
		String namespace = TermUri.DWC_LOCATION_ID.toString();
		List<NamedArea> result = state.get(namespace, locationId, NamedArea.class);
		if (result.isEmpty()){
			//try to find in cdm
			NamedArea newArea = TdwgArea.getAreaByTdwgAbbreviation(locationId);
			if (newArea == null){
//				state.getCurrentIO().getTermService().findByAreaCode
			}
			if (newArea == null){
				newArea = NamedArea.NewInstance(locationId, locationId, locationId);
			}
			
			return newArea;
		}
		if (result.size() > 1){
			String message = "There is more than 1 cdm entity matching given locationId '%s'. I take an arbitrary one.";
			fireWarningEvent(String.format(message, locationId), item, 4);
		}
		return (NamedArea)result.iterator().next();
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
		//taxon
		if ( hasValue(value = item.get(CORE_ID))){
			key = TermUri.DWC_TAXON.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		
		//areaId
		
		String locationId = item.get(TermUri.DWC_LOCATION_ID);
		if ( hasValue(value = locationId)){
			key = TermUri.DWC_LOCATION_ID.toString();
			Set<String> keySet = getKeySet(key, fkMap);
			keySet.add(value);
		}
		
	}
	
	
	@Override
	public Set<String> requiredSourceNamespaces() {
		Set<String> result = new HashSet<String>();
 		result.add(TermUri.DWC_TAXON.toString());
 		result.add(TermUri.DWC_LOCATION_ID.toString());
 		return result;
	}
	
//******************* TO STRING ******************************************/
	
	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
