/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.distribution.excelupdate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.io.excel.common.ExcelImporterBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This Import class updates existing distributions with the new state
 * described in the Excel file. It requires that the data was exported
 * before in the defined format.
 *
 * TODO where is the export to be found?
 *
 * This class is initiated by #6524
 *
 * @author a.mueller
 * @date 04.04.2017
 *
 */
@Component
public class ExcelDistributionUpdate
            extends ExcelImporterBase<ExcelDistributionUpdateState>{

    private static final long serialVersionUID = 621338661492857764L;
    private static final Logger logger = Logger.getLogger(ExcelDistributionUpdate.class);

    private static final String AREA_MAP = "AreaMap";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void analyzeRecord(HashMap<String, String> record, ExcelDistributionUpdateState state) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void firstPass(ExcelDistributionUpdateState state) {
        HashMap<String, String> record = state.getOriginalRecord();
        String line = state.getCurrentLine() + ": ";
        String taxonUuid = getValue(record, "taxon_uuid");
        String taxonName = getValue(record, "Taxonname");

        if ("taxon_uuid".equals(taxonUuid)){
            return;
        }
        UUID uuidTaxon = UUID.fromString(taxonUuid);
        Taxon taxon = (Taxon)getTaxonService().find(uuidTaxon);
        if (taxon == null){
            String message = line + "Taxon for uuid not found: " +  uuidTaxon;
            state.getResult().addError(message);
        }else{
            try {
                handleAreasForTaxon(state, taxon, record, line);
            } catch (Exception e) {
                String message = line + "An unexpected error occurred when handling %s (uuid: %s)";
                message = String.format(message, taxonName, taxonUuid);
                state.getResult().addError(message);
                state.getResult().addException(e);
            }
        }
    }

    /**
     * @param state
     * @param taxon
     * @param record
     * @param line
     */
    private void handleAreasForTaxon(ExcelDistributionUpdateState state, Taxon taxon, HashMap<String, String> record,
            String line) {
        ImportResult result = state.getResult();
        Map<NamedArea, Set<Distribution>> existingDistributions = getExistingDistributions(state, taxon, line);
        Map<NamedArea, Distribution> newDistributions = getNewDistributions(state, record, line);
        TaxonDescription newDescription = TaxonDescription.NewInstance();
        newDescription.addImportSource(null, null, state.getConfig().getSourceReference(), "row " + state.getCurrentLine());
        newDescription.setTitleCache("Updated distributions for " + getTaxonLabel(taxon), true);
        Set<TaxonDescription> oldReducedDescriptions = new HashSet<>();
        for (NamedArea area : newDistributions.keySet()){
            Set<Distribution> existingDistrForArea = existingDistributions.get(area);
            boolean hasChange = false;
            Distribution newDistribution = newDistributions.get(area);
            if (existingDistrForArea == null || existingDistrForArea.isEmpty()){
                if (newDistribution != null){
                    //new distribution exists, old distribution did not exist
                    hasChange = true;
                }
            }else{
                for (Distribution existingDistr : existingDistrForArea){
                    if (!isEqualDistribution(existingDistr, newDistribution)){
                        //distribution changed or deleted
                        if (state.getConfig().isCreateNewDistribution() || newDistribution == null ){
                            DescriptionBase<?> inDescription = existingDistr.getInDescription();
                            inDescription.removeElement(existingDistr);
                            result.addDeletedRecord(existingDistr);
                            hasChange = true;
                            oldReducedDescriptions.add(CdmBase.deproxy(inDescription, TaxonDescription.class));
                        }else{
                            existingDistr.setStatus(newDistribution.getStatus());
                            result.addUpdatedRecord(existingDistr);
                            existingDistr.addImportSource(null, null, state.getConfig().getSourceReference(), "row "+state.getCurrentLine());
                        }
                    }else{
    //                    addSource? => not if nothing changed
                    }
                }
            }
            if (hasChange && newDistribution != null){
                newDescription.addElement(newDistribution);
                result.addNewRecord(newDistribution);
            }
        }
        //add new description to taxon if any new element exists
        if (!newDescription.getElements().isEmpty()){
            taxon.addDescription(newDescription);
            result.addNewRecord(newDescription);
        }
        //remove old empty descriptions (oldReducedDescriptions) if really empty
        for (TaxonDescription desc : oldReducedDescriptions){
            if (desc.getElements().isEmpty()){
                desc.getTaxon().removeDescription(desc);
                result.addDeletedRecord(desc);
            }
        }
    }

    /**
     * @param taxon
     * @return
     */
    private String getTaxonLabel(Taxon taxon) {
        return taxon.getName() == null ? taxon.getTitleCache() : taxon.getName().getTitleCache();
    }

    private Map<NamedArea, Distribution> getNewDistributions(ExcelDistributionUpdateState state,
            HashMap<String, String> record, String line) {

        Map<NamedArea, Distribution> result = new HashMap<>();

        Set<String> keys = record.keySet();
        keys = removeNonAreaKeys(keys);
        for (String key : keys){
            NamedArea area = getAreaByIdInVoc(state, key, line);
            if (area != null){
                String statusStr = record.get(key);
                PresenceAbsenceTerm status = getStatusByStatusStr(state, statusStr, line);
                if (status != null){
                    Distribution distribution = Distribution.NewInstance(area, status);
                    distribution.addImportSource(null, null, state.getConfig().getSourceReference(), "row " + state.getCurrentLine());
                    Distribution previousDistribution = result.put(area, distribution);
                    if (previousDistribution != null){
                        String message = line + "Multiple distributions exist for same area (" + area.getTitleCache() +  ") in input source";
                        logger.warn(message);
                        state.getResult().addWarning(message);
                    }
                }else{
                    result.put(area, null);
                }
            }else{
                //??
            }
        }
        return result;
    }

    /**
     * @param statusStr
     * @return
     */
    private PresenceAbsenceTerm getStatusByStatusStr(ExcelDistributionUpdateState state, String statusStr, String line) {
//        FIXME replace hardcoded;
        if ("A".equals(statusStr)) {
            return PresenceAbsenceTerm.ABSENT();
        }else if ("P".equals(statusStr)) {
            return PresenceAbsenceTerm.PRESENT();
        }else if ("P?".equals(statusStr)) {
            return PresenceAbsenceTerm.PRESENT_DOUBTFULLY();
        }else if (isBlank(statusStr)){
            return null;
        }else{
            String message = line + "Status string not recognized: " +  statusStr +". Status not imported.";
            logger.warn(message);
            state.getResult().addWarning(message);
        }

        return null;
    }

    /**
     * @param state
     * @param key
     * @param line
     * @return
     */
    private NamedArea getAreaByIdInVoc(ExcelDistributionUpdateState state, String id, String line) {
        //TODO remember in state
        Map<String, NamedArea> areaMap = (Map<String, NamedArea>)state.getStatusItem(AREA_MAP);
        if (areaMap == null){
            areaMap = createAreaMap(state);
            state.putStatusItem(AREA_MAP, areaMap);
        }
        NamedArea area = areaMap.get(id);
        return area;
    }

    /**
     * @param state
     * @return
     */
    private Map<String, NamedArea> createAreaMap(ExcelDistributionUpdateState state) {
        Map<String, NamedArea> result = new HashMap<>();
        TermVocabulary<?> voc = getVocabularyService().find(state.getConfig().getAreaVocabularyUuid());
        //TODO handle null
        for (DefinedTermBase<?> obj : voc.getTerms()){
            //TODO handle exception
            NamedArea area = CdmBase.deproxy(obj, NamedArea.class);
            String key = area.getIdInVocabulary();
            result.put(key, area);
        }
        return result;
    }

    /**
     * @param keys
     * @return
     */
    private Set<String> removeNonAreaKeys(Set<String> keys) {
        Iterator<String> it = keys.iterator();
        while (it.hasNext()){
            if (it.next().matches("(Family|Taxonname|taxon_uuid)")){
                it.remove();
            }
        }
        return keys;
    }

    private boolean isEqualDistribution(Distribution existingDistribution, Distribution newDistribution) {
        if (existingDistribution == null || newDistribution == null){
            return existingDistribution == newDistribution;
        }
        if (existingDistribution.getArea().equals(newDistribution.getArea())){
            if (CdmUtils.nullSafeEqual(existingDistribution.getStatus(), newDistribution.getStatus())){
                return true;
            }
        }
        return false;
    }

    /**
     * @param state
     * @param taxon
     * @param line
     * @return
     */
    private Map<NamedArea, Set<Distribution>> getExistingDistributions(
            ExcelDistributionUpdateState state, Taxon taxon,
            String line) {
        Map<NamedArea, Set<Distribution>> result = new HashMap<>();
        //TODO better use service layer call to return only distributions, this might be necessary if the list of description elements is large
        for (TaxonDescription desc : taxon.getDescriptions()){
            for (DescriptionElementBase descElem : desc.getElements()){
                if (descElem.isInstanceOf(Distribution.class)){
                    Distribution distribution =  CdmBase.deproxy(descElem, Distribution.class);
                    NamedArea area = distribution.getArea();
                    Set<Distribution> set = result.get(area);
                    if (set == null){
                        set = new HashSet<>();
                        result.put(area, set);
                    }
                    set.add(distribution);
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void secondPass(ExcelDistributionUpdateState state) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(ExcelDistributionUpdateState state) {
        return false;
    }


}
