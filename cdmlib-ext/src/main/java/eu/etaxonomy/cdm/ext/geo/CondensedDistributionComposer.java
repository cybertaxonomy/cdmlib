/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.codehaus.plexus.util.StringUtils;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.TripleResult;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * Base class for condensed distribution composers
 *
 * @author a.mueller
 * @since 02.06.2016
 */
public class CondensedDistributionComposer {

    //for old handling of status symbols
    protected static Map<UUID, String> statusSymbols;

    static {

        // ==================================================
        // Mapping as defined in ticket https://dev.e-taxonomy.eu/redmine/issues/3907
        // ==================================================

        statusSymbols = new HashMap<> ();
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR().getUuid(), UTF8.EN_DASH.toString());
    }


    public enum StatusSymbolUsage{
        Map,
        Symbol1,
        Symbol2,
        IdInVoc,
        AbbrelLabel;

        public String getSymbol(PresenceAbsenceTerm status) {
            if (this == Map){
                return statusSymbols.get(status.getUuid());
            }else if (this == Symbol1){
                return status.getSymbol();
            }else if (this == Symbol2){
                return status.getSymbol2();
            }else if (this == IdInVoc){
                return status.getIdInVocabulary();
            }else if (this == AbbrelLabel){
                Representation r = status.getPreferredRepresentation((Language)null);
                if (r != null){
                    String abbrevLabel = r.getAbbreviatedLabel();
                    if (abbrevLabel != null){
                        return abbrevLabel;
                    }
                }
            }
            throw new RuntimeException("Unhandled enum value: " +  this);
        }
    }

    public CondensedDistribution createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> languages, CondensedDistributionConfiguration config) {

        CondensedDistribution result = new CondensedDistribution();

        Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap = new HashMap<>();

        DoubleResult<List<AreaNode>, List<AreaNode>> step1_3 = createAreaTreesAndStatusMap(filteredDistributions, areaToStatusMap, config);
        List<AreaNode> topLevelNodes = step1_3.getFirstResult();
        List<AreaNode> introducedTopLevelNodes = step1_3.getSecondResult();

        //4. replace the area by the abbreviated representation and add symbols
        AreaNodeComparator areaNodeComparator = new AreaNodeComparator();
        Collections.sort(topLevelNodes, areaNodeComparator);

        final boolean NOT_BOLED = false;
        final boolean NOT_HANDLED_BY_PARENT = false;

        List<AreaNode> outOfScopeNodes = new ArrayList<>();
        if (!topLevelNodes.isEmpty()){
            AreaNode areaOfScopeNode = topLevelNodes.remove(0);
            outOfScopeNodes = topLevelNodes;

            //handle areaOfScope  (endemic area)
            PresenceAbsenceTerm areaOfScopeStatus = areaToStatusMap.get(areaOfScopeNode.area);
            DoubleResult<String, Boolean> areaOfScopeStatusSymbol = statusSymbol(areaOfScopeStatus, config, NOT_HANDLED_BY_PARENT);
            String areaOfScopeLabel = config.showAreaOfScopeLabel? makeAreaLabel(languages, areaOfScopeNode.area, config, null):"";
            result.addStatusAndAreaTaggedText(areaOfScopeStatusSymbol.getFirstResult(),
                    areaOfScopeLabel, areaOfScopeStatusSymbol.getSecondResult() || config.areasBold);

            //subareas
            handleSubAreas(result, areaOfScopeNode, config, areaNodeComparator, languages, areaToStatusMap,
                    areaOfScopeLabel, 0, NOT_BOLED, NOT_HANDLED_BY_PARENT, false);
        }

        //subareas with introduced status (if required by configuration)
        if (config.splitNativeAndIntroduced && !introducedTopLevelNodes.isEmpty()){
            String sep = (result.isEmpty()? "": " ") + config.introducedBracketStart;
            result.addSeparatorTaggedText(sep, NOT_BOLED);
            boolean isIntroduced = true;
            handleSubAreasLoop(result, introducedTopLevelNodes.get(0), config, areaNodeComparator, languages,
                    areaToStatusMap, "", 0, NOT_BOLED, NOT_HANDLED_BY_PARENT, isIntroduced);
            result.addSeparatorTaggedText(config.introducedBracketEnd, NOT_BOLED);
        }

        //outOfScope areas  (areas outside the endemic area)
        if (!outOfScopeNodes.isEmpty()){
            result.addPostSeparatorTaggedText(config.outOfScopeAreasSeperator);
            List<AreaNode> outOfScopeList = new ArrayList<>(outOfScopeNodes);
            Collections.sort(outOfScopeList, areaNodeComparator);
            boolean isFirst = true;
            for (AreaNode outOfScopeNode: outOfScopeList){
                String sep = isFirst ? "": " ";
                result.addSeparatorTaggedText(sep);
                handleSubAreaNode(languages, result, areaToStatusMap, areaNodeComparator,
                        outOfScopeNode, config, null, 0, NOT_BOLED, NOT_HANDLED_BY_PARENT, false);
                isFirst = false;
            }
        }

        return result;
    }

    protected Map<NamedArea, AreaNode>[] buildAreaHierarchie(Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap,
            CondensedDistributionConfiguration config) {

        Map<NamedArea, AreaNode> areaNodeMap = new HashMap<>();
        Map<NamedArea, AreaNode> introducedAreaNodeMap = new HashMap<>();

        for(NamedArea area : areaToStatusMap.keySet()) {
            Map<NamedArea, AreaNode> map = areaNodeMap;
            if (config.splitNativeAndIntroduced && isIntroduced(areaToStatusMap.get(area))){
                map = introducedAreaNodeMap;
            }
            mergeIntoHierarchy(areaToStatusMap.keySet(), map, area, config);
        }
        @SuppressWarnings("unchecked")
        Map<NamedArea, AreaNode>[] result = new Map[]{areaNodeMap,introducedAreaNodeMap};
        return result;
    }

    private boolean isIntroduced(PresenceAbsenceTerm status) {
        return status.isAnyIntroduced();
    }

    private void mergeIntoHierarchy(Collection<NamedArea> areas,  //areas not really needed anymore if we don't use findParentIn
            Map<NamedArea, AreaNode> areaNodeMap, NamedArea area, CondensedDistributionConfiguration config) {

        AreaNode node = areaNodeMap.get(area);
        if(node == null) {
            // putting area into list of areas as node
            node = new AreaNode(area);
            areaNodeMap.put(area, node);
        }

        NamedArea parent = getNonFallbackParent(area, config);   // findParentIn(area, areas);

        if(parent != null) {
            AreaNode parentNode = areaNodeMap.get(parent);
            if(parentNode == null) {
                parentNode = new AreaNode(parent);
                areaNodeMap.put(parent, parentNode);
            }
            parentNode.addSubArea(node);
            mergeIntoHierarchy(areas, areaNodeMap, parentNode.area, config);  //recursive to top
        }
    }

    private NamedArea getNonFallbackParent(NamedArea area, CondensedDistributionConfiguration config) {
        NamedArea parent = area.getPartOf();
        while(parent != null && parent.hasMarker(config.fallbackAreaMarker, true)){
            parent = parent.getPartOf();
        }
        return parent;
    }

    protected String makeAreaLabel(List<Language> langs, NamedArea area,
            CondensedDistributionConfiguration config, String parentAreaLabel) {
        //TODO config with symbols, not only idInVocabulary
        String label = area.getIdInVocabulary() != null ? area.getIdInVocabulary() :area.getPreferredRepresentation(langs).getAbbreviatedLabel();
        if (config.shortenSubAreaLabelsIfPossible && parentAreaLabel != null && !parentAreaLabel.isEmpty()){
            //TODO make brackets not hardcoded, but also allow [],- etc., but how?
            if (label.startsWith(parentAreaLabel+"(") && label.endsWith(")") ){
                label = label.substring(parentAreaLabel.length()+1, label.length()-1);
            }
        }

        return label;
    }

    protected TripleResult<String, Boolean, Boolean> statusSymbolForArea(AreaNode areaNode, Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap,
            CondensedDistributionConfiguration config, boolean onlyIntroduced) {

        if (!config.showStatusOnParentAreaIfAllSame ){
            return statusSymbol(areaToStatusMap.get(areaNode.area), config, false);
        }else{
            Set<PresenceAbsenceTerm> statusList = getStatusRecursive(areaNode, areaToStatusMap, new HashSet<>(), onlyIntroduced);
            if (statusList.isEmpty()){
                return statusSymbol(areaToStatusMap.get(areaNode.area), config, false);
            }else if (statusList.size() == 1){
                return statusSymbol(statusList.iterator().next(), config, true);
            }else{
                //subarea status is handled at subarea level, usually parent area status is empty as the parent area will not have a status
                return statusSymbol(areaToStatusMap.get(areaNode.area), config, false);
            }
        }
    }

    private Set<PresenceAbsenceTerm> getStatusRecursive(AreaNode areaNode,
            Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap, Set<PresenceAbsenceTerm> statusList,
            boolean onlyIntroduced) {

        PresenceAbsenceTerm status = areaToStatusMap.get(areaNode.area);
        if (status != null && (!onlyIntroduced || isIntroduced(status))){
            statusList.add(status);
        }

        for (AreaNode subNode : areaNode.subAreas){
            statusList.addAll(getStatusRecursive(subNode, areaToStatusMap, statusList, onlyIntroduced));
        }
        return statusList;
    }

    /**
     * @param status
     * @param config configuration
     * @param statusHandledByParent indicates if the status is handled by the parent. Will be passed to the (third) result
     * @return a triple result with the first result being the symbol string, the second result being the isBold flag
     *     and the third result indicates if this symbol includes information for all sub-areas (which passes the input parameter)
     *     to the output here
     */
    protected TripleResult<String, Boolean, Boolean> statusSymbol(PresenceAbsenceTerm status,
            CondensedDistributionConfiguration config, boolean statusHandledByParent) {

        List<StatusSymbolUsage> symbolPreferences = Arrays.asList(config.statusSymbolField);
        if(status == null) {
            return new TripleResult<>("", false, statusHandledByParent);
        }

        //usually the symbols should all come from the same field, but in case they don't ...
        if (config.showAnyStatusSmbol){
            for (StatusSymbolUsage usage : StatusSymbolUsage.values()){
                if (!symbolPreferences.contains(usage)){
                    symbolPreferences.add(usage);
                }
            }
        }

        for (StatusSymbolUsage usage: symbolPreferences){
            String symbol = usage.getSymbol(status);
            if (symbol != null){
                return new TripleResult<>(symbol, isBoldStatus(status, config), statusHandledByParent);
            }
        }

        return new TripleResult<>("", isBoldStatus(status, config), statusHandledByParent);
    }

    private Boolean isBoldStatus(PresenceAbsenceTerm status, CondensedDistributionConfiguration config) {
        return config.statusForBoldAreas.contains(status.getUuid());
    }

    /**
     * Searches for the parent area of the area given as parameter in
     * the collection of areas.
     *
     * @parent area
     *      The area which parent area is to be searched
     * @param collection
     *      The areas to search in.
     *
     * @return
     *      Either the parent if it has been found or null.
     */
    protected NamedArea findParentIn(NamedArea area, Collection<NamedArea> areas) {
        NamedArea parent = area.getPartOf();
        if(parent != null && areas.contains(parent)){
            return parent;
        }
        return null;
    }

    protected class AreaNode {

        protected final NamedArea area;
        protected AreaNode parent = null;
        protected final Set<AreaNode> subAreas = new HashSet<>();

        public AreaNode(NamedArea area) {
            this.area = area;
        }

        public void addSubArea(AreaNode subArea) {
            subAreas.add(subArea);
            subArea.parent = this;
        }

        public AreaNode getParent() {
            return parent;
        }

        public boolean hasParent() {
            return getParent() != null;
        }

        public Collection<NamedArea> getSubareas() {
            Collection<NamedArea> areas = new HashSet<>();
            for(AreaNode node : subAreas) {
                areas.add(node.area);
            }
            return areas;
        }

        @Override
        public String toString() {
            return "AreaNode [area=" + area + "]";
        }
    }

    protected class AreaNodeComparator implements Comparator<AreaNode>{

        @Override
        public int compare(AreaNode areaNode1, AreaNode areaNode2) {
            NamedArea area1 = areaNode1.area;
            NamedArea area2 = areaNode2.area;

            if (area1 == null && area2 == null){
                return 0;
            }else if (area1 == null){
                return -1;
            }else if (area2 == null){
                return 1;
            }else{
                //- due to wrong ordering behavior in DefinedTerms
                return - area1.compareTo(area2);
            }
        }
    }

    protected DoubleResult<List<AreaNode>, List<AreaNode>> createAreaTreesAndStatusMap(Collection<Distribution> filteredDistributions,
            Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap, CondensedDistributionConfiguration config){

        //we expect every area only to have 1 status  (multiple status should have been filtered beforehand)

        //1. compute all areas and their status
        for(Distribution distr : filteredDistributions) {
            PresenceAbsenceTerm status = distr.getStatus();
            NamedArea area = distr.getArea();

            //TODO needed? Do we only want to have areas with status?
            if(status == null || area == null) {
                continue;
            }

            areaToStatusMap.put(area, status);
        }

        //2. build the area hierarchy
        Map<NamedArea, AreaNode>[] areaNodeMaps = buildAreaHierarchie(areaToStatusMap, config);

        //3. find root nodes
        @SuppressWarnings("unchecked")
        List<AreaNode>[] topLevelNodes = new ArrayList[]{new ArrayList<>(), new ArrayList<>()};

        for (int i = 0; i < 2; i++){
            for(AreaNode node : areaNodeMaps[i].values()) {
                if(!node.hasParent() && ! topLevelNodes[i].contains(node)) {
                    topLevelNodes[i].add(node);
                }
            }
        }

        DoubleResult<List<AreaNode>, List<AreaNode>> result = new DoubleResult<>(topLevelNodes[0], topLevelNodes[1]);

        return result;
    }

    private void handleSubAreas(CondensedDistribution result, AreaNode areaNode, CondensedDistributionConfiguration config,
            AreaNodeComparator areaNodeComparator, List<Language> languages, Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap,
            String parentAreaLabel, int level, boolean isBold, boolean statusHandledByParent, boolean isIntroduced) {

        if (!areaNode.subAreas.isEmpty()) {
            String sepStart = getListValue(config.areaOfScopeSubAreaBracketStart, level, "(");
            if (StringUtils.isNotBlank(sepStart)){
                result.addSeparatorTaggedText(sepStart, isBold);
            }
            handleSubAreasLoop(result, areaNode, config, areaNodeComparator, languages, areaToStatusMap, parentAreaLabel,
                    level, isBold, statusHandledByParent, isIntroduced);
            String sepEnd = getListValue(config.areaOfScopeSubAreaBracketEnd, level, ")");
            if (StringUtils.isNotBlank(sepEnd)){
                result.addSeparatorTaggedText(sepEnd, isBold);
            }
            if (isBold){
                result.addSeparatorTaggedText("", true);  //previous separator is otherwise not included in bold html tag by TaggedCacheHelper
            }
        }
    }

    private void handleSubAreasLoop(CondensedDistribution result, AreaNode areaNode,
            CondensedDistributionConfiguration config, AreaNodeComparator areaNodeComparator, List<Language> languages,
            Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap, String parentLabel, int level, boolean isBold,
            boolean statusHandledByParent, boolean isIntroduced) {

        List<AreaNode> subAreas = new ArrayList<>(areaNode.subAreas);
        Collections.sort(subAreas, areaNodeComparator);
        boolean isFirst = true;
        for (AreaNode subAreaNode: subAreas){
            if (!isFirst){
                result.addSeparatorTaggedText(" ", isBold);
            }
            handleSubAreaNode(languages, result, areaToStatusMap, areaNodeComparator,
                    subAreaNode, config, parentLabel, level, isBold, statusHandledByParent, isIntroduced);
            isFirst = false;
        }
    }

    private String getListValue(List<String> list, int i, String defaultStr) {
        if (i < list.size()){
            return list.get(i);
        }
        return defaultStr;
    }

    private void handleSubAreaNode(List<Language> languages, CondensedDistribution result,
            Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap, AreaNodeComparator areaNodeComparator,
            AreaNode areaNode, CondensedDistributionConfiguration config, String parentAreaLabel, int level,
            boolean parentIsBold, boolean statusHandledByParent, boolean isIntroduced) {

        level++;
        NamedArea area = areaNode.area;

        TripleResult<String, Boolean, Boolean> statusSymbol = statusHandledByParent?
                new TripleResult<>("", parentIsBold, statusHandledByParent):
                statusSymbolForArea(areaNode, areaToStatusMap, config, isIntroduced);

        String areaLabel = makeAreaLabel(languages, area, config, parentAreaLabel);
        result.addStatusAndAreaTaggedText(statusSymbol.getFirstResult(), areaLabel, statusSymbol.getSecondResult() || config.areasBold);

        boolean isBold = statusSymbol.getSecondResult();
        boolean isHandledByParent = statusSymbol.getThirdResult();
        handleSubAreas(result, areaNode, config, areaNodeComparator, languages, areaToStatusMap, areaLabel, level,
                isBold, isHandledByParent, isIntroduced);
    }
}
