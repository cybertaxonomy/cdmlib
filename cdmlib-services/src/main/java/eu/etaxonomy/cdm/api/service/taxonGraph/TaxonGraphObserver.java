/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeEvent;
import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeEvent.EventType;
import eu.etaxonomy.cdm.persistence.hibernate.CdmDataChangeMap;
import eu.etaxonomy.cdm.persistence.hibernate.ICdmPostDataChangeObserver;

/**
 * @author a.kohlbecker
 * @since Sep 27, 2018
 *
 */
@Component
public class TaxonGraphObserver implements ICdmPostDataChangeObserver {

    @Autowired
    private ITaxonGraphService taxonGraphService;

    private String[] NAMEPARTS_OR_RANK_PROPS = new String[]{"genusOrUninomial", "specificEpithet", "rank"};
    private String[] NOMREF_PROP = new String[]{"nomenclaturalReference"};

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(CdmDataChangeMap changeEvents) {

        try {
            for(CdmDataChangeEvent event : changeEvents.getEvents(EventType.UPDATE)){
                if(event.getEntity() instanceof TaxonName){
                    if(checkStateChange(event, NAMEPARTS_OR_RANK_PROPS) > -1){
                        taxonGraphService.onNameOrRankChange((TaxonName) event.getEntity());
                    }
                    int changedNomRefIndex = checkStateChange(event, NOMREF_PROP);
                    if(changedNomRefIndex > -1){
                        taxonGraphService.onNomReferenceChange((TaxonName) event.getEntity(), (Reference)event.getOldState()[changedNomRefIndex]);
                    }
                }
            }
            for(CdmDataChangeEvent event : changeEvents.getEvents(EventType.INSERT)){
                if(event.getEntity() instanceof TaxonName){
                    taxonGraphService.onNewTaxonName((TaxonName) event.getEntity());
                }
            }
        } catch (TaxonGraphException e) {
            Logger.getLogger(this.getClass()).error(e);
        }

    }


    private int checkStateChange(CdmDataChangeEvent event, String[] propertyNamesToCheck){

        String[] propertyNames = event.getPersister().getPropertyNames();
        Object[] oldState = event.getOldState();
        Object[] state = event.getState();

        int propsCheckedCnt = 0;
        for(int i = 0; i < propertyNames.length; i++){
            if(ArrayUtils.contains(propertyNamesToCheck, propertyNames[i])){
                propsCheckedCnt++;
                if(!oldState[i].equals(state[i])){
                    return i;
                }
                if(propsCheckedCnt == propertyNamesToCheck.length){
                    return -1;
                }
            }
        }
        // this execption should be raised during the unit tests already and thus will never occur in production
        throw new RuntimeException("TaxonName class misses at least one property of: " + ArrayUtils.toString(propertyNamesToCheck));
    }

}
