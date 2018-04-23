/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 \* @since Apr 05, 2016
 *
 */
public class FloraCubaCondensedDistributionComposer extends CondensedDistributionComposerBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FloraCubaCondensedDistributionComposer.class);

//    private static Set<UUID> foreignStatusUuids;

    //preliminary for Cuba, needs to be parameterized
    private UUID uuidInternalArea = UUID.fromString("d0144a6e-0e17-4a1d-bce5-d464a2aa7229");  //Cuba

    private String internalAreaSeparator = UTF8.EN_DASH.toString() + " ";


//    // these status uuids are special for EuroPlusMed and might also be used
//    private final static UUID REPORTED_IN_ERROR_UUID =  UUID.fromString("38604788-cf05-4607-b155-86db456f7680");

    static {

        // ==================================================
        // Mapping as defined in ticket http://dev.e-taxonomy.eu/trac/ticket/5682
        // ==================================================

       statusSymbols = new HashMap<UUID, String> ();
       //no entries as we handle symbols now on model level

    }

// ***************************** GETTER/SETTER ***********************************/
    public String getInternalAreaSeparator() {
        return internalAreaSeparator;
    }

    public void setInternalAreaSeparator(String internalAreaSeparator) {
        this.internalAreaSeparator = internalAreaSeparator;
    }

// ***********************************************************************

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public CondensedDistribution createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> languages) {

        CondensedDistribution result = new CondensedDistribution();
//        Collection<NamedArea> allAreas = new HashSet<NamedArea>();
        //we expect every area only to have 1 status  (multiple status should have been filtered beforehand)
        Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap = new HashMap<>();


        //1. compute all areas and their status
        for(Distribution d : filteredDistributions) {
            PresenceAbsenceTerm status = d.getStatus();
            NamedArea area = d.getArea();

            //TODO needed? Do we only want to have areas with status?
            if(status == null || area == null) {
                continue;
            }

            areaToStatusMap.put(area, status);
        }


        //2. build the area hierarchy
        Map<NamedArea, AreaNode> areaNodeMap = new HashMap<>();

        for(NamedArea area : areaToStatusMap.keySet()) {
            AreaNode node;
            if(!areaNodeMap.containsKey(area)) {
                // putting area into hierarchy as node
                node = new AreaNode(area);
                areaNodeMap.put(area, node);
            } else {
                //  is parent of another and thus already has a node
                node = areaNodeMap.get(area);
            }

            NamedArea parent = findParentIn(area, areaToStatusMap.keySet());
            if(parent != null) {
                AreaNode parentNode;
                if(!areaNodeMap.containsKey(parent)) {
                    parentNode = new AreaNode(parent);
                    areaNodeMap.put(parent, parentNode);
                } else {
                    parentNode = areaNodeMap.get(parent);
                }
                parentNode.addSubArea(node);
            }
        }

        //3. find root nodes
        List<AreaNode>topLevelNodes = new ArrayList<AreaNode>();
        for(AreaNode node : areaNodeMap.values()) {
            if(!node.hasParent() && ! topLevelNodes.contains(node)) {
                topLevelNodes.add(node);
            }
        }

        //4. replace the area by the abbreviated representation and add symbols
        boolean isFirstAfterAreaOfScope = false;
        AreaNodeComparator areaNodeComparator = new AreaNodeComparator();

        Collections.sort(topLevelNodes, areaNodeComparator);

        for(AreaNode topLevelNode : topLevelNodes) {

            StringBuilder areaStatusString = new StringBuilder();

            NamedArea area = topLevelNode.area;
            if (area.getUuid().equals(uuidInternalArea)){
                isFirstAfterAreaOfScope = true;
            }else if(isFirstAfterAreaOfScope && !area.getUuid().equals(uuidInternalArea)){
                areaStatusString.append(internalAreaSeparator);
                isFirstAfterAreaOfScope = false;
            }


            PresenceAbsenceTerm status = areaToStatusMap.get(area);
            String statusSymbol = statusSymbol(areaToStatusMap.get(area));
            areaStatusString.append(statusSymbol);

            String areaLabel = makeAreaLabel(languages, area);
            areaStatusString.append(areaLabel);

            if(!topLevelNode.subAreas.isEmpty()) {
                areaStatusString.append('(');
                subAreaLabels(languages, topLevelNode.subAreas, areaStatusString, statusSymbol,
                        areaLabel, areaToStatusMap, areaNodeComparator);
                areaStatusString.append(')');
            }

//            if(isForeignStatus(status)) {
//                condensedDistribution.addForeignDistributionItem(status, areaStatusString.toString(), areaLabel);
//            } else {
                result.addIndigenousDistributionItem(status, areaStatusString.toString(), areaLabel);
//            }
        }

        return result;
    }

    /**
     * Recursive call to create sub area label strings
     * @param areaNodeComparator
     */
    private void subAreaLabels(List<Language> languages, Collection<AreaNode> nodes, StringBuilder totalStringBuilder,
            String parentStatusSymbol, String parentLabel,
            Map<NamedArea, PresenceAbsenceTerm> areaToStatusMap, AreaNodeComparator areaNodeComparator) {

        List<String> subAreaLabels = new ArrayList<String>();

        List<AreaNode> areaNodes = new ArrayList<>(nodes);
        Collections.sort(areaNodes, areaNodeComparator);

        for(AreaNode node : areaNodes) {

            StringBuilder subAreaString = new StringBuilder();

            NamedArea area = node.area;
            PresenceAbsenceTerm status = areaToStatusMap.get(area);
            String subAreaStatusSymbol = statusSymbol(status);
            if (subAreaStatusSymbol != null && !subAreaStatusSymbol.equals(parentStatusSymbol)){
                subAreaString.append(subAreaStatusSymbol);
            }

            String areaLabel = makeAreaLabel(languages, area);
            if(replaceCommonAreaLabelStart){
                String cleanSubAreaLabel = StringUtils.replaceEach(areaLabel, new String[] {parentLabel, "(", ")"}, new String[] {"", "", ""});
                subAreaString.append(cleanSubAreaLabel);
            }else{
                subAreaString.append(areaLabel);
            }

            if(!node.subAreas.isEmpty()) {
                subAreaString.append('(');
                subAreaLabels(languages, node.subAreas, subAreaString, subAreaStatusSymbol, areaLabel,
                        areaToStatusMap, areaNodeComparator);
                subAreaString.append(')');
            }

            subAreaLabels.add(subAreaString.toString());
        }
//        Collections.sort(subAreaLabels);
        totalStringBuilder.append(StringUtils.join(subAreaLabels, " "));
    }

    private class AreaNodeComparator implements Comparator<AreaNode>{

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

}
