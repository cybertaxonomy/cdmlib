/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal.format;

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
import java.util.stream.Collectors;

import org.codehaus.plexus.util.StringUtils;

import eu.etaxonomy.cdm.api.dto.portal.DistributionDto;
import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistribution;
import eu.etaxonomy.cdm.api.dto.portal.config.CondensedDistributionConfiguration;
import eu.etaxonomy.cdm.api.dto.portal.config.SymbolUsage;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermNodeDto;
import eu.etaxonomy.cdm.api.service.portal.DistributionDtoLoader;
import eu.etaxonomy.cdm.api.service.portal.TermTreeDtoLoader;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.common.TripleResult;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.compare.common.OrderType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.TermTree;

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


    public String getSymbol(SymbolUsage su, TermDto term, List<Language> langs) {
        if (su == SymbolUsage.Map){
            //TODO not valid for areas yet
            return statusSymbols.get(term.getUuid());
        }else if (su == SymbolUsage.Symbol1){
            return term.getSymbol1();
        }else if (su == SymbolUsage.Symbol2){
            return term.getSymbol2();
        }else if (su == SymbolUsage.IdInVoc){
            return term.getIdInVocabulary();
        }else if (su == SymbolUsage.AbbrevLabel){
            return term.getAbbrevLabel();
        }
        throw new RuntimeException("Unhandled enum value: " +  this);
    }


    /**
     * Wrapper for {@link #createCondensedDistribution(Collection, SetMap, List, CondensedDistributionConfiguration)}
     * to make the method easiy avaiable for test.
     */
    public CondensedDistribution createCondensedDistribution(Set<Distribution> distributions,
            TermTree<NamedArea> areaTree, List<Language> languages,
            CondensedDistributionConfiguration config) {

        Set<DistributionDto> distributionDtos = distributions.stream()
                .map(d->DistributionDtoLoader.INSTANCE().fromEntity(d, null))
                .collect(Collectors.toSet());
        SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap = TermTreeDtoLoader.getTerm2NodeMap(TermTreeDtoLoader.INSTANCE().fromEntity(areaTree), NamedAreaDto.class);
        return createCondensedDistribution(distributionDtos, area2TermNodesMap, languages, config);
    }

    public CondensedDistribution createCondensedDistribution(
            Collection<DistributionDto> filteredDistributions,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            List<Language> languages,
            CondensedDistributionConfiguration config) {

        CondensedDistribution result = new CondensedDistribution();
        Map<NamedAreaDto,TermDto> areaToStatusMap = new HashMap<>();

        //1.-3. create area tree and status map
        DoubleResult<List<AreaNode>, List<AreaNode>> areaTreesAndStatusMap = createAreaTreesAndStatusMap(
                filteredDistributions, area2TermNodesMap, areaToStatusMap, config);

        List<AreaNode> topLevelNodes = areaTreesAndStatusMap.getFirstResult();
        List<AreaNode> introducedTopLevelNodes = areaTreesAndStatusMap.getSecondResult();

        handleAlternativeRootArea(topLevelNodes, areaToStatusMap, config);
        handleAlternativeRootArea(introducedTopLevelNodes, areaToStatusMap, config);

        //4. replace the area by the abbreviated representation and add symbols
        AreaNodeComparator areaNodeComparator = new AreaNodeComparator(config, languages);
        AreaNodeComparator topLevelAreaOfScopeComparator = config.orderType == OrderType.NATURAL
                ? areaNodeComparator : new AreaNodeComparator(config, languages, OrderType.NATURAL);

        //sort
        Collections.sort(topLevelNodes, topLevelAreaOfScopeComparator);

        final boolean NOT_BOLD = false;
        final boolean NOT_HANDLED_BY_PARENT = false;

        List<AreaNode> outOfScopeNodes = new ArrayList<>();
        if (!topLevelNodes.isEmpty()){
            AreaNode areaOfScopeNode = topLevelNodes.remove(0);
            outOfScopeNodes = topLevelNodes;

            //handle areaOfScope  (endemic area)
            TermDto areaOfScopeStatus = areaToStatusMap.get(areaOfScopeNode.area);
            DoubleResult<String, Boolean> areaOfScopeStatusSymbol = statusSymbol(areaOfScopeStatus, config, languages, NOT_HANDLED_BY_PARENT);
            String areaOfScopeLabel = config.showAreaOfScopeLabel? makeAreaLabel(languages, areaOfScopeNode.area, config, null):"";
            String statusStr = areaOfScopeStatusSymbol.getFirstResult();
            boolean isBold = areaOfScopeStatusSymbol.getSecondResult() || config.areasBold;

            result.addStatusAndAreaTaggedText(statusStr, areaOfScopeLabel,
                    isBold, config);

            //subareas
            handleSubAreas(result, areaOfScopeNode, config, areaNodeComparator, languages, areaToStatusMap,
                    areaOfScopeLabel, 0, NOT_BOLD, NOT_HANDLED_BY_PARENT, false);
        }

        //subareas with introduced status (if required by configuration)
        if (config.splitNativeAndIntroduced && !introducedTopLevelNodes.isEmpty()){
            String sep = (result.isEmpty()? "": " ") + config.introducedBracketStart;
            result.addSeparatorTaggedText(sep, NOT_BOLD);
            boolean isIntroduced = true;
            handleSubAreasLoop(result, introducedTopLevelNodes.get(0), config, areaNodeComparator, languages,
                    areaToStatusMap, "", 0, NOT_BOLD, NOT_HANDLED_BY_PARENT, isIntroduced);
            result.addSeparatorTaggedText(config.introducedBracketEnd, NOT_BOLD);
        }

        //outOfScope areas  (areas outside the endemic area)
        if (!outOfScopeNodes.isEmpty()){
            Collections.sort(topLevelNodes, areaNodeComparator);

            result.addPostSeparatorTaggedText(config.outOfScopeAreasSeperator);
            List<AreaNode> outOfScopeList = new ArrayList<>(outOfScopeNodes);
            Collections.sort(outOfScopeList, areaNodeComparator);
            boolean isFirst = true;
            for (AreaNode outOfScopeNode: outOfScopeList){
                String sep = isFirst ? "": " ";
                result.addSeparatorTaggedText(sep);
                handleSubAreaNode(languages, result, areaToStatusMap, areaNodeComparator,
                        outOfScopeNode, config, null, 0, NOT_BOLD, NOT_HANDLED_BY_PARENT, false);
                isFirst = false;
            }
        }

        return result;
    }

    private void handleAlternativeRootArea(List<AreaNode> topLevelNodes,
            Map<NamedAreaDto,TermDto> areaToStatusMap, CondensedDistributionConfiguration config) {

        //don't anything if no alternative area markers exist
        if (CdmUtils.isNullSafeEmpty(config.alternativeRootAreaMarkers)) {
            return;
        }

        List<AreaNode> removeSafeTopLevelNodes = new ArrayList<>(topLevelNodes);
        int index = -1;
        for (AreaNode topLevelNode : removeSafeTopLevelNodes) {
            index++;
            int nChildren = topLevelNode.getSubareas() == null ? 0 : topLevelNode.getSubareas().size();
            boolean switched = false;
            if (areaToStatusMap.get(topLevelNode.area) == null && nChildren == 1) {
                //real top level node has no data and 1 child => potential candidate to be replaced by alternative root
                AreaNode childNode = topLevelNode.subAreas.iterator().next();
                NamedAreaDto childArea = childNode.area;
                boolean childHasData = areaToStatusMap.get(childArea) != null;
                if (isMarkedAs(childArea, config.alternativeRootAreaMarkers)
                        && childHasData) {
                    //child is alternative root and has data => replace root by alternative root
                    topLevelNodes.remove(topLevelNode);
                    topLevelNodes.add(index, childNode);
                    switched = true;
                }
            }
            if (switched == false) {
                //if root has data or >1 children test if children are alternative roots with no data => remove
                Set<AreaNode> childNodes = new HashSet<>(topLevelNode.subAreas);
                for(AreaNode childNode : childNodes) {
                    NamedAreaDto childArea = childNode.area;
                    boolean childHasNoData = areaToStatusMap.get(childArea) == null;
                    if (isMarkedAs(childArea, config.alternativeRootAreaMarkers)
                            && childHasNoData) {
                        replaceInBetweenNode(topLevelNode,childNode);
                    }
                }
            }
        }
    }

    private void replaceInBetweenNode(AreaNode parent, AreaNode inBetweenNode) {
        for (AreaNode child : inBetweenNode.subAreas) {
            parent.addSubArea(child);
        }
        parent.subAreas.remove(inBetweenNode);
    }

    private boolean isMarkedAs(NamedAreaDto area, Set<UUID> alternativeRootAreaMarkers) {
        for (UUID uuid : alternativeRootAreaMarkers) {
            if (area.hasMarker(uuid)) {
                return true;
            }
        }
        return false;
    }

    private Map<NamedAreaDto, AreaNode>[] buildAreaHierarchie(
            Map<NamedAreaDto,TermDto> areaToStatusMap,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            CondensedDistributionConfiguration config) {

        Map<NamedAreaDto,AreaNode> nativeArea2NodeMap = new HashMap<>();
        Map<NamedAreaDto,AreaNode> introducedArea2NodeMap = new HashMap<>();
        Set<NamedAreaDto> additionalHiddenParents = new HashSet<>();

        //1. for each area decide which map (native or introduce), and merge
        for(NamedAreaDto area : areaToStatusMap.keySet()) {
            //decide which map
            Map<NamedAreaDto,AreaNode> map = nativeArea2NodeMap;
            if (config.splitNativeAndIntroduced && isIntroduced(areaToStatusMap.get(area))){
                map = introducedArea2NodeMap;
            }
            //merge into map
//            Set<TermNode<NamedArea>> termNodes = area2TermNodesMap.get(area);
//            for (TermNode<NamedArea> termNode : termNodes) {
//            }
            mergeIntoHierarchy(area, map, area2TermNodesMap, additionalHiddenParents, config);
        }

        //TODO move this filter further up where native and introduced is still combined,
        // this makes it less complicated
        //2. remove fallback areas
        removeFallbackAreasWithChildDistributions(nativeArea2NodeMap, introducedArea2NodeMap, additionalHiddenParents, config);

        @SuppressWarnings("unchecked")
        Map<NamedAreaDto, AreaNode>[] result = new Map[]{nativeArea2NodeMap, introducedArea2NodeMap};
        return result;
    }

    private void removeFallbackAreasWithChildDistributions(Map<NamedAreaDto,AreaNode> area2NodeMap,
            Map<NamedAreaDto,AreaNode> area2NodeMap2, Set<NamedAreaDto> additionalHiddenParents,
            CondensedDistributionConfiguration config) {

        //compute areas/areaNodes to be removed
        Set<NamedAreaDto> toBeDeletedAreas = new HashSet<>(additionalHiddenParents);
        Set<AreaNode> allNodes = new HashSet<>(area2NodeMap.values());
        allNodes.addAll(area2NodeMap2.values());
        for (AreaNode areaNode : allNodes){
            if (hasParentToBeRemoved(areaNode, config)){
                toBeDeletedAreas.add(areaNode.parent.area);
            }
        }
        //... remove them
        for (NamedAreaDto toBeDeletedArea : toBeDeletedAreas){
            removeFallbackArea(toBeDeletedArea, area2NodeMap);
            removeFallbackArea(toBeDeletedArea, area2NodeMap2);
        }
    }

    private void removeFallbackArea(NamedAreaDto toBeDeletedArea, Map<NamedAreaDto,AreaNode> areaNodeMap) {
        AreaNode toBeDeletedNode = areaNodeMap.get(toBeDeletedArea);
        if(toBeDeletedNode == null){
            return;
        }
        AreaNode parent = toBeDeletedNode.getParent();
        if (parent != null){
            parent.subAreas.remove(toBeDeletedNode);
        }
        for (AreaNode child : toBeDeletedNode.subAreas){
            if (parent != null){
                parent.addSubArea(child);
            }else{
                child.parent = null;
            }
        }
        areaNodeMap.remove(toBeDeletedNode.area);
    }

    private boolean hasParentToBeRemoved(AreaNode areaNode, CondensedDistributionConfiguration config) {
        if (areaNode.parent == null){
            return false;
        }
        //FIXME
        boolean parentIsHidden = isFallback(areaNode.parent, config);
        return parentIsHidden;
    }

    private boolean isIntroduced(TermDto status) {
        return PresenceAbsenceTerm.isAnyIntroduced(status.getUuid());
    }

    private void mergeIntoHierarchy(
            NamedAreaDto area,
            Map<NamedAreaDto,AreaNode> area2NodeMap,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            Set<NamedAreaDto> additionalHiddenParents,
            CondensedDistributionConfiguration config) {

        AreaNode node = area2NodeMap.get(area);
        if(node == null) {
            // putting area into list of areas as node
            node = new AreaNode(area, area2TermNodesMap);
            area2NodeMap.put(area, node);
        }

        NamedAreaDto parent = getNonFallbackParent(area, area2TermNodesMap, additionalHiddenParents, config);   // findParentIn(area, areas);

        if(parent != null) {
            AreaNode parentNode = area2NodeMap.get(parent);
            if(parentNode == null) {
                parentNode = new AreaNode(parent, area2TermNodesMap);
                area2NodeMap.put(parent, parentNode);
            }
            if (node.parent == null) {
                node.parent = parentNode;
            }
            parentNode.addSubArea(node);
            //recursive to top
            mergeIntoHierarchy(parentNode.area, area2NodeMap, area2TermNodesMap,
                    additionalHiddenParents, config);
        }
    }

    private NamedAreaDto getNonFallbackParent(NamedAreaDto area,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            Set<NamedAreaDto> additionalHiddenParents,
            CondensedDistributionConfiguration config) {

        Set<TermNodeDto> termNodes = area2TermNodesMap.get(area);  //TODO handle >1 parents
        Set<TermNodeDto> parentTermNodes = termNodes.stream()
                .map(a->a.getParent()).collect(Collectors.toSet());
        //        return parents;
        //NOTE: filtering out all fallback areas here leads to failing "fallback" test

        if (parentTermNodes.isEmpty()) {
            return null;
        }else if (parentTermNodes.size() == 1) {
            //TODO no check for fallback?
            TermNodeDto tn = parentTermNodes.iterator().next();
            return tn == null ? null : (NamedAreaDto)tn.getTerm();
        }else{
            Set<NamedAreaDto> nonFallbackParents = parentTermNodes.stream()
                    .filter(p->!isFallback(p, config))
                    .map(tn->(NamedAreaDto)tn.getTerm())
                    .collect(Collectors.toSet());
            Set<NamedAreaDto> fallbackParents = parentTermNodes.stream()
                    .filter(p->isFallback(p, config))
                    .map(tn->(NamedAreaDto)tn.getTerm())
                    .collect(Collectors.toSet());

            additionalHiddenParents.addAll(fallbackParents);
            if (!nonFallbackParents.isEmpty()) {
                //TODO at least use comparator here to have defined behavior
                //     or find other rules for decision
                return nonFallbackParents.iterator().next();
            }else {
                Set<NamedAreaDto> anyRecursiveNotFallback = fallbackParents.stream()
                    .map(fp->getNonFallbackParent(fp, area2TermNodesMap, additionalHiddenParents, config))
                    .collect(Collectors.toSet());
                return anyRecursiveNotFallback.isEmpty() ? null : anyRecursiveNotFallback.iterator().next();
            }
//            }else {
//                //TODO part-of , use parentAreaMap.get(p) but need to handle collection result
//                parents = nonFallbackParents.stream().map(p->p.getPartOf()).collect(Collectors.toSet());
//            }
//          for (NamedArea parent : parents) {
//            if (parent == null) {
//                return null;
//            }else
//            while(parent != null && isHiddenOrFallback(parent, config)){
//                parent = parent.getPartOf();
//            }
//
//        }
        }
    }

    private boolean isFallback(TermNodeDto termNode, CondensedDistributionConfiguration config) {
        if (config.fallbackAreaMarkers == null){
            return false;
        }
        for (UUID markerUuid : config.fallbackAreaMarkers){
            if (termNode.hasMarker(markerUuid)
                    || ((NamedAreaDto)termNode.getTerm()).hasMarker(markerUuid)){
                return true;
            }
        }
        return false;
    }

    private boolean isFallback(AreaNode areaNode, CondensedDistributionConfiguration config) {
        if (config.fallbackAreaMarkers == null){
            return false;
        }
        for (UUID markerUuid : config.fallbackAreaMarkers){
            if (areaNode.hasMarker(markerUuid) || areaNode.area.hasMarker(markerUuid)){
                return true;
            }
        }
        return false;
    }

    private String makeAreaLabel(List<Language> langs, NamedAreaDto area,
            CondensedDistributionConfiguration config, String parentAreaLabel) {

        //TODO config with symbols, not only idInVocabulary
        String label = getSymbol(config.areaSymbolField, area, langs);
        if (config.shortenSubAreaLabelsIfPossible && parentAreaLabel != null && !parentAreaLabel.isEmpty()){
            //TODO make brackets not hardcoded, but also allow [],- etc., but how?
            if (label.startsWith(parentAreaLabel+"(") && label.endsWith(")") ){
                label = label.substring(parentAreaLabel.length()+1, label.length()-1);
            }
        }

        return label;
    }

    private TripleResult<String, Boolean, Boolean> statusSymbolForArea(AreaNode areaNode, Map<NamedAreaDto,TermDto> areaToStatusMap,
            CondensedDistributionConfiguration config, List<Language> languages,boolean onlyIntroduced) {

        if (!config.showStatusOnParentAreaIfAllSame ){
            return statusSymbol(areaToStatusMap.get(areaNode.area), config, languages, false);
        }else{
            Set<TermDto> statusList = getStatusRecursive(areaNode, areaToStatusMap, new HashSet<>(), onlyIntroduced);
            if (statusList.isEmpty()){
                return statusSymbol(areaToStatusMap.get(areaNode.area), config, languages, false);
            }else if (statusList.size() == 1){
                return statusSymbol(statusList.iterator().next(), config, languages, true);
            }else{
                //subarea status is handled at subarea level, usually parent area status is empty as the parent area will not have a status
                if (areaToStatusMap.get(areaNode.area) == null && containsBoldAreas(statusList, config)){
                    //if parent area status is empty and at least one subarea has status native the parent area should also be bold (#8297#note-15)
                    return new TripleResult<>("", true, false);
                }else{
                    return statusSymbol(areaToStatusMap.get(areaNode.area), config, languages, false);
                }
            }
        }
    }

    private boolean containsBoldAreas(Set<TermDto> statusList, CondensedDistributionConfiguration config) {
        for (TermDto status : statusList){
            if (config.statusForBoldAreas.contains(status.getUuid())){
                return true;
            }
        }
        return false;
    }

    private Set<TermDto> getStatusRecursive(AreaNode areaNode,
            Map<NamedAreaDto,TermDto> areaToStatusMap, Set<TermDto> statusList,
            boolean onlyIntroduced) {

        TermDto status = areaToStatusMap.get(areaNode.area);
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
     * @param config
     *              configuration
     * @param statusHandledByParent
     *              indicates if the status is handled by the parent. Will be passed to the (third) result
     * @return
     *      a triple result with the first result being the symbol string, the second result
     *      being the isBold flag and the third result indicates if this symbol includes information
     *      for all sub-areas (which passes the input parameter) to the output here
     */
    private TripleResult<String, Boolean, Boolean> statusSymbol(TermDto status,
            CondensedDistributionConfiguration config, List<Language> languages,
            boolean statusHandledByParent) {

        List<SymbolUsage> symbolPreferences = Arrays.asList(config.statusSymbolField);
        if(status == null) {
            return new TripleResult<>("", false, statusHandledByParent);
        }

        //usually the symbols should all come from the same field, but in case they don't ...
        if (config.showAnyStatusSmbol){
            for (SymbolUsage usage : SymbolUsage.values()){
                if (!symbolPreferences.contains(usage)){
                    symbolPreferences.add(usage);
                }
            }
        }

        for (SymbolUsage usage: symbolPreferences){
            String symbol = getSymbol(usage, status, languages);
            if (symbol != null){
                return new TripleResult<>(symbol, isBoldStatus(status, config), statusHandledByParent);
            }
        }

        return new TripleResult<>("", isBoldStatus(status, config), statusHandledByParent);
    }

    private Boolean isBoldStatus(TermDto status, CondensedDistributionConfiguration config) {
        return config.statusForBoldAreas.contains(status.getUuid());
    }

    //TODO do we really need an explicit class here, maybe we can simply use TermNodeDto
    //     which has the params except for the missing parent (but we may want to add this anyway)
    private class AreaNode {

        protected final NamedAreaDto area;
        protected AreaNode parent = null;
        protected final Set<AreaNode> subAreas = new HashSet<>();
        protected final Set<UUID> markers = new HashSet<>();

        public AreaNode(NamedAreaDto area, SetMap<NamedAreaDto,TermNodeDto> map) {
            this.area = area;
            //areaNode marker
            markers.addAll(map.get(area).stream()
                .flatMap(termNode->termNode.getMarkers().stream())
                .collect(Collectors.toSet()));
            //area marker
            if (area.getMarkers() != null) {
                markers.addAll(area.getMarkers());
            }
        }

        public boolean hasMarker(UUID uuidMarkerType) {
            return markers.contains(uuidMarkerType);
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

        public Collection<NamedAreaDto> getSubareas() {
            Collection<NamedAreaDto> areas = new HashSet<>();
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

    private class AreaNodeComparator implements Comparator<AreaNode>{

        private CondensedDistributionConfiguration config;
        private OrderType orderType;
        private List<Language> languages;

        private AreaNodeComparator(CondensedDistributionConfiguration config, List<Language> languages){
            this(config, languages, null);
        }

        private AreaNodeComparator(CondensedDistributionConfiguration config, List<Language> languages, OrderType divergentOrderType){
            this.config = config;
            this.languages = languages;
            this.orderType = divergentOrderType != null? divergentOrderType : config.orderType;
        }

        @Override
        public int compare(AreaNode areaNode1, AreaNode areaNode2) {
            NamedAreaDto area1 = areaNode1.area;
            NamedAreaDto area2 = areaNode2.area;

            if (area1 == null && area2 == null){
                return 0;
            }else if (area1 == null){
                return -1;
            }else if (area2 == null){
                return 1;
            }else{
                if (orderType == OrderType.NATURAL) {
                    //- due to wrong ordering behavior in DefinedTerms
                    return - area1.compareTo(area2);
                }else{
                    String str1 = getSymbol(config.areaSymbolField, area1, languages);
                    String str2 = getSymbol(config.areaSymbolField, area2, languages);
                    return CdmUtils.nullSafeCompareTo(str1, str2);
                }
            }
        }
    }

    /**
     * Creates the root nodes for the native and the introduces area trees and fills
     * the area2StatusMap.
     */
    private DoubleResult<List<AreaNode>, List<AreaNode>> createAreaTreesAndStatusMap(
            Collection<DistributionDto> filteredDistributions,
            SetMap<NamedAreaDto,TermNodeDto> area2TermNodesMap,
            Map<NamedAreaDto,TermDto> area2StatusMap,
            CondensedDistributionConfiguration config){

        //we expect every area only to have 1 status  (multiple status should have been filtered beforehand)

        //1. compute all areas and their status
        for(DistributionDto distr : filteredDistributions) {
            TermDto status = distr.getStatus();
            NamedAreaDto area = distr.getArea();

            //TODO needed? Do we only want to have areas with status?
            if(status == null || area == null) {
                continue;
            }

            area2StatusMap.put(area, status);
        }

        //2. build the area hierarchy
        Map<NamedAreaDto,AreaNode>[] areaNodeMaps = buildAreaHierarchie(area2StatusMap, area2TermNodesMap, config);

        //3. find root nodes
        @SuppressWarnings("unchecked")
        List<AreaNode>[] topLevelNodes = new ArrayList[]{new ArrayList<>(), new ArrayList<>()};

        //   ... for both maps find the root nodes
        for (int i = 0; i < 2; i++){
            for(AreaNode node : areaNodeMaps[i].values()) {
                if(!node.hasParent() && !topLevelNodes[i].contains(node)) {
                    topLevelNodes[i].add(node);
                }
            }
        }

        return new DoubleResult<>(topLevelNodes[0], topLevelNodes[1]);
    }

    private void handleSubAreas(CondensedDistribution result, AreaNode areaNode, CondensedDistributionConfiguration config,
            AreaNodeComparator areaNodeComparator, List<Language> languages, Map<NamedAreaDto,TermDto> areaToStatusMap,
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
        }
    }

    private void handleSubAreasLoop(CondensedDistribution result, AreaNode areaNode,
            CondensedDistributionConfiguration config, AreaNodeComparator areaNodeComparator, List<Language> languages,
            Map<NamedAreaDto,TermDto> areaToStatusMap, String parentLabel, int level, boolean isBold,
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
            Map<NamedAreaDto,TermDto> areaToStatusMap, AreaNodeComparator areaNodeComparator,
            AreaNode areaNode, CondensedDistributionConfiguration config, String parentAreaLabel, int level,
            boolean parentIsBold, boolean statusHandledByParent, boolean isIntroduced) {

        level++;
        NamedAreaDto area = areaNode.area;

        TripleResult<String, Boolean, Boolean> statusSymbol = statusHandledByParent?
                new TripleResult<>("", parentIsBold, statusHandledByParent):
                statusSymbolForArea(areaNode, areaToStatusMap, config, languages, isIntroduced);

        String areaLabel = makeAreaLabel(languages, area, config, parentAreaLabel);
        result.addStatusAndAreaTaggedText(statusSymbol.getFirstResult(), areaLabel,
                (statusSymbol.getSecondResult() || config.areasBold), config);

        boolean isBold = statusSymbol.getSecondResult();
        boolean isHandledByParent = statusSymbol.getThirdResult();
        handleSubAreas(result, areaNode, config, areaNodeComparator, languages, areaToStatusMap, areaLabel, level,
                isBold, isHandledByParent, isIntroduced);
    }

}