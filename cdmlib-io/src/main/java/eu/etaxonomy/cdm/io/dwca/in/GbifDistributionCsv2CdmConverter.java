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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.stream.IPartitionableConverter;
import eu.etaxonomy.cdm.io.stream.IReader;
import eu.etaxonomy.cdm.io.stream.ListReader;
import eu.etaxonomy.cdm.io.stream.MappedCdmBase;
import eu.etaxonomy.cdm.io.stream.PartitionableConverterBase;
import eu.etaxonomy.cdm.io.stream.StreamItem;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @date 22.11.2011
 *
 */
public class GbifDistributionCsv2CdmConverter extends PartitionableConverterBase<DwcaDataImportConfiguratorBase, DwcaDataImportStateBase<DwcaDataImportConfiguratorBase>>
						implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String>{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(GbifDistributionCsv2CdmConverter.class);

	private static final String CORE_ID = "coreId";

	/**
	 * @param state
	 */
	public GbifDistributionCsv2CdmConverter(DwcaDataImportStateBase state) {
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

		    //area
		    String locality = item.get(TermUri.DWC_LOCALITY);
			String locationId = item.get(TermUri.DWC_LOCATION_ID);
			NamedArea area = getAreaByLocationId(item, locationId, locality, resultList);
			if (area != null){
				MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(TermUri.DWC_LOCATION_ID, csv.get(TermUri.DWC_LOCATION_ID.toString()), area);
				resultList.add(mcb);
			}else if (! config.isExcludeLocality()){
				area = getAreaByLocality(item, locality);
				MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(TermUri.DWC_LOCALITY, csv.get(TermUri.DWC_LOCALITY.toString()), area);
				resultList.add(mcb);
			}

			//status
			String establishmentMeans = item.get(TermUri.DWC_ESTABLISHMENT_MEANS);
	        String occurrenceStatus = item.get(TermUri.DWC_OCCURRENCE_STATUS);
			PresenceAbsenceTerm status = getPresenceAbsenceStatus(item, establishmentMeans, occurrenceStatus, resultList);

			if (area != null){

				TaxonDescription desc = getTaxonDescription(taxon, false);

				Distribution distribution = Distribution.NewInstance(area, status);
				desc.addElement(distribution);

				//save taxon
				MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(item.term, csv.get(CORE_ID.toString()), taxon);
				resultList.add(mcb);
			}

		}else{
			String message = "Can't retrieve taxon from database for id '%s'";
			fireWarningEvent(String.format(message, id), item, 12);
		}

		//return
		return new ListReader<>(resultList);
	}



	/**
     * @param item
	 * @param occurrenceStatus
	 * @param establishmentMeans
     * @param resultList
     * @return
     */
    private PresenceAbsenceTerm getPresenceAbsenceStatus(StreamItem item,
            String establishmentMeans, String occurrenceStatus, List<MappedCdmBase<? extends CdmBase>> resultList) {

        PresenceAbsenceTerm status = null;
        if (isBlank(establishmentMeans) && isBlank(occurrenceStatus)){
            status = PresenceAbsenceTerm.PRESENT();
        }else{
            String statusStr = CdmUtils.concat(" - ", occurrenceStatus, establishmentMeans);
            String namespace = PresenceAbsenceTerm.class.getCanonicalName();
            List<PresenceAbsenceTerm> result = state.get(namespace, statusStr, PresenceAbsenceTerm.class);
            try{
                if (result.isEmpty()){
                    PresenceAbsenceTerm newStatus = state.getTransformer().getPresenceTermByKey(statusStr);
                    if (newStatus != null){
                        return newStatus;
                    }
                    //try to find in cdm
                    newStatus = getExistingPresenceAbsenceTerm(statusStr);
                    if (newStatus != null){
                        return newStatus;
                    }

                    UUID statusUuid = state.getTransformer().getPresenceTermUuid(statusStr);
                    newStatus = state.getCurrentIO().getPresenceTerm(state, statusUuid, statusStr, statusStr, null, false);

                    //should not happen
                    if (newStatus == null){
                        newStatus = PresenceAbsenceTerm.NewPresenceInstance(statusStr, statusStr, statusStr);
                        state.getCurrentIO().saveNewTerm(newStatus);
                        MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(namespace, statusStr, newStatus);
                        resultList.add(mcb);
                    }

                    state.putMapping(namespace, statusStr, newStatus);
                    return newStatus;
                }
                if (result.size() > 1){
                    String message = "There is more than 1 cdm entity matching given occurrence status/establishment means '%s'."
                            + " I take an arbitrary one.";
                    fireWarningEvent(String.format(message, statusStr), item, 4);
                }
                return result.iterator().next();
            } catch (UndefinedTransformerMethodException e) {
                String message = "GetNamedArea not yet supported by DwcA-Transformer. This should not have happend. Please contact your application developer.";
                fireWarningEvent(message, item, 8);
                return null;
            }
        }
        return status;
    }

    /**
     * @param statusStr
     * @return
     */
    private PresenceAbsenceTerm getExistingPresenceAbsenceTerm(String statusStr) {
        TermVocabulary<PresenceAbsenceTerm> voc = PresenceAbsenceTerm.PRESENT().getVocabulary();
        for (PresenceAbsenceTerm status: voc.getTerms()){
            if (statusStr.equalsIgnoreCase(status.getLabel())){
                return status;
            }
        }
        return null;
    }

    private NamedArea getAreaByLocality(StreamItem item, String locality) {
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
		return result.iterator().next();
	}

	private NamedArea getAreaByLocationId(StreamItem item, String locationId, String newLabel, List<MappedCdmBase<? extends CdmBase>> resultList) {
		String namespace = TermUri.DWC_LOCATION_ID.toString();
		if (isBlank(locationId)){
		    return null;
		}
		List<NamedArea> result = state.get(namespace, locationId, NamedArea.class);
		try{
    		if (result.isEmpty()){
    		    NamedArea newArea = state.getTransformer().getNamedAreaByKey(locationId);
    		    if (newArea != null){
                    return newArea;
                }
    		  //try to find in cdm
                newArea = getTdwgArea(locationId);
                if (newArea != null){
                    return newArea;
                }

    		    String label = isNotBlank(newLabel)? newLabel : locationId;
    		    UUID namedAreaUuid = state.getTransformer().getNamedAreaUuid(locationId);
    		    newArea = state.getCurrentIO().getNamedArea(state, namedAreaUuid, label, label, locationId, null);

    		    //should not happen
    		    if (newArea == null){
    		        newArea = NamedArea.NewInstance(label, label, locationId);
    //            state.putMapping(namespace, type, newArea);
                    state.getCurrentIO().saveNewTerm(newArea);
                    MappedCdmBase<? extends CdmBase>  mcb = new MappedCdmBase<>(namespace, locationId, newArea);
                    resultList.add(mcb);
                }


    			state.putMapping(namespace, locationId, newArea);
    			return newArea;
    		}
    		if (result.size() > 1){
    			String message = "There is more than 1 cdm entity matching given locationId '%s'. I take an arbitrary one.";
    			fireWarningEvent(String.format(message, locationId), item, 4);
    		}
    		return result.iterator().next();
        } catch (UndefinedTransformerMethodException e) {
            String message = "GetNamedArea not yet supported by DwcA-Transformer. This should not have happend. Please contact your application developer.";
            fireWarningEvent(message, item, 8);
            return null;
        }
	}

    /**
     * @param locationId
     * @return
     */
    protected NamedArea getTdwgArea(String locationId) {
        if (locationId == null){
            return null;
        }else if (locationId.startsWith("TDWG:")){
            locationId = locationId.substring("TDWG:".length()); //CoL case
        }
        return TdwgAreaProvider.getAreaByTdwgAbbreviation(locationId);
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
		Set<String> result = new HashSet<>();
 		result.add(TermUri.DWC_TAXON.toString());
 		result.add(TermUri.DWC_LOCATION_ID.toString());
 		result.add(TermUri.DWC_LOCALITY.toString());
 		return result;
	}

//******************* TO STRING ******************************************/

	@Override
	public String toString(){
		return this.getClass().getName();
	}


}
