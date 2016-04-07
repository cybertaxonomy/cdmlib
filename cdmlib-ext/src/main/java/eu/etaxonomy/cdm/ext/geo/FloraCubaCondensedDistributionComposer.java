// $Id$
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @date Apr 05, 2016
 *
 */
public class FloraCubaCondensedDistributionComposer implements ICondensedDistributionComposer {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FloraCubaCondensedDistributionComposer.class);

    private static Map<UUID, String> statusSymbols;

    private static Set<UUID> foreignStatusUuids;

    // these status uuids are special for EuroPlusMed and might also be used
    private final static UUID REPORTED_IN_ERROR_UUID =  UUID.fromString("38604788-cf05-4607-b155-86db456f7680");

    static {

        // ==================================================
        // Mapping as defined in ticket http://dev.e-taxonomy.eu/trac/ticket/5682
        // ==================================================

        statusSymbols = new HashMap<UUID, String> ();
        // ● endemic (U+25CF BLACK CIRCLE)
        statusSymbols.put(PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA().getUuid(), "\u25CF");

        // Lu native (incl. archaeophytes) TODO status archaeophytes?
        statusSymbols.put(PresenceAbsenceTerm.NATIVE().getUuid(), "");
//        statusSymbols.put(PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE().getUuid(), "");

        // ?Lu doubtfully present (U+3F QUESTION MARK)
//        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE().getUuid(), "?");
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE().getUuid(), "?");
//        statusSymbols.put(PresenceAbsenceTerm.PRESENT_DOUBTFULLY().getUuid(), "?");

        // dLu doubtfully native
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE().getUuid(), "d");

        // -Lu absent but reported in error (U+2D HYPHEN-MINUS)
//        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR().getUuid(), "-");
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR().getUuid(), "-");
        statusSymbols.put(REPORTED_IN_ERROR_UUID, "-");

        // [aLu] casual alien = introduced: adventitious (casual)
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_ADVENTITIOUS().getUuid(), "a");

        // [cLu] cultivated
        statusSymbols.put(PresenceAbsenceTerm.CULTIVATED() .getUuid(), "c");
//        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_CULTIVATED().getUuid(), "c");

        // [nLu] naturalized
        statusSymbols.put(PresenceAbsenceTerm.NATURALISED().getUuid(), "n");
//        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_NATURALIZED().getUuid(), "n");

        statusSymbols.put(PresenceAbsenceTerm.CULTIVATED_PRESENCE_QUESTIONABLE().getUuid(), "?c");
        statusSymbols.put(PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR().getUuid(), "-c");


        //Cuba specific
        //occasionally cultivated
        statusSymbols.put(UUID.fromString("936c3f9a-6099-4322-9792-0a72c6c2ce25"), "(c)");
        //endemic, doubtfully present
        statusSymbols.put(UUID.fromString("5f954f08-267a-4928-b073-12328f74c187"), "?e");
        //non-native and doubtfully naturalised
        statusSymbols.put(UUID.fromString("a1e26234-831e-4190-9fe3-011aca09ddba"), "p");
        //rare casual
        statusSymbols.put(UUID.fromString("8914ce0d-7d31-4c88-8317-985f2b3a493b"), "(a)");
        //?non-native and doubtfully naturalised
        statusSymbols.put(UUID.fromString("9e0b413b-5a68-4e5b-91f2-227b4f832466"), "?p");
        //?adventive (casual) alien
        statusSymbols.put(UUID.fromString("c42ca644-1773-4230-a2ee-328a5d4a21ab"), "?a");
        //endemic, reported in error
        statusSymbols.put(UUID.fromString("679b215d-c231-4ee2-ae12-3ffc3dd528ad"), "-e");
        //naturalised, reported in error
        statusSymbols.put(UUID.fromString("8d918a37-3add-4e1c-a233-c37dbee209aa"), "-n");
        //non-native and doubtfully naturalised, reported in error
        statusSymbols.put(UUID.fromString("b9153d90-9e31-465a-a28c-79077a8ed4c2"), "-p");
        //adventive alien , reported in error
        statusSymbols.put(UUID.fromString("9b910b7b-43e3-4260-961c-6063b11cb7dc"), "-a");
        //doubtfully native: reported in error
        statusSymbols.put(UUID.fromString("71b72e24-c2b6-44a5-bdab-39f083bf0f06"), "-d");


    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public CondensedDistribution createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> langs) {
        CondensedDistribution condensedDistribution = new CondensedDistribution();

        //empty
        if (filteredDistributions == null || filteredDistributions.isEmpty()){
            return condensedDistribution;
        }

        OrderedTermVocabulary<NamedArea> areaVocabulary = CdmBase.deproxy(filteredDistributions.iterator().next().getArea().getVocabulary(), OrderedTermVocabulary.class);

        List<NamedArea> areaList = new ArrayList<NamedArea>(areaVocabulary.getOrderedTerms());
        Collections.reverse(areaList);

        for (NamedArea area : areaList){

            if (area.getPartOf() != null){
                continue;  //subarea are handled later
            }
            StringBuilder areaStatusString = new StringBuilder();
            Distribution distribution = getDistribution(area, filteredDistributions);
            if (distribution == null){
                continue;
            }

            PresenceAbsenceTerm status = distribution.getStatus();

            String statusSymbol = statusSymbol(status);
            areaStatusString.append(statusSymbol);

            String areaLabel = makeAreaLabel(langs, area);
            areaStatusString.append(areaLabel);

            if(!area.getIncludes().isEmpty()) {
//                areaStatusString.append('(');
                subAreaLabels(langs, area.getIncludes(), areaStatusString, statusSymbol, areaLabel, filteredDistributions);
//                areaStatusString.append(')');
            }

//            if(isForeignStatus(status)) {
//                condensedDistribution.addForeignDistributionItem(status, areaStatusString.toString(), areaLabel);
//            } else {
                condensedDistribution.addIndigenousDistributionItem(status, areaStatusString.toString(), areaLabel);
//            }

        }

//        }
//        //5. order the condensedDistributions alphabetically
//        // FIXME
//        condensedDistribution.sortForeign();
//        condensedDistribution.sortIndigenous();

        return condensedDistribution;
    }

    /**
     * @param area
     * @param filteredDistributions
     * @return
     */
    private Distribution getDistribution(NamedArea area, Collection<Distribution> filteredDistributions) {
        for (Distribution dist : filteredDistributions){
            if (dist.getArea() != null && dist.getArea().equals(area)){
                return dist;
            }
        }
        return null;
    }

    /**
     * @param status
     * @return
     */
    private String statusSymbol(PresenceAbsenceTerm status) {
        if(status == null) {
            return "";
        }
        String symbol = statusSymbols.get(status.getUuid());
        if(symbol == null) {
            symbol = "";
        }
        return symbol;
    }

//    private boolean isForeignStatus(PresenceAbsenceTerm status) {
//        return foreignStatusUuids.contains(status.getUuid());
//    }

    /**
     * @param langs
     * @param node
     * @param areaString
     * @param statusSymbol
     */
    private void subAreaLabels(List<Language> langs, Collection<NamedArea> subAreas, StringBuilder areaString,
            String statusSymbol, String parentLabel,
            Collection<Distribution> filteredDistributions) {
        //TODO very redundant with main method
        List<String> subAreaLabels = new ArrayList<String>();

        List<NamedArea> areaList = new ArrayList<NamedArea>(subAreas);
        Collections.reverse(areaList);

        for(NamedArea area : areaList) {

            StringBuilder subAreaString = new StringBuilder();
            Distribution distribution = getDistribution(area, filteredDistributions);
            if (distribution == null){
                continue;
            }


            PresenceAbsenceTerm status = distribution.getStatus();
            String subAreaStatusSymbol = statusSymbol(status);
            if (subAreaStatusSymbol != null && !subAreaStatusSymbol.equals(statusSymbol)){
                subAreaString.append(subAreaStatusSymbol);
            }

            String areaLabel = makeAreaLabel(langs, area);
            String cleanSubAreaLabel = StringUtils.replaceEach(areaLabel, new String[] {parentLabel, "(", ")"}, new String[] {"", "", ""});
            subAreaString.append(cleanSubAreaLabel);

            if(!area.getIncludes().isEmpty()) {
//                subAreaString.append('(');
                subAreaLabels(langs, area.getIncludes(), subAreaString, subAreaStatusSymbol, areaLabel, filteredDistributions);
//                subAreaString.append(')');
            }

            subAreaLabels.add(subAreaString.toString());
        }

//      Collections.sort(subAreaLabels);
        if (!subAreaLabels.isEmpty()){
            areaString.append("(" + StringUtils.join(subAreaLabels, " ") + ")");
        }

    }

    /**
     * @param langs
     * @param area
     * @return
     */
    private String makeAreaLabel(List<Language> langs, NamedArea area) {
        return area.getIdInVocabulary() != null ? area.getIdInVocabulary() :area.getPreferredRepresentation(langs).getAbbreviatedLabel();
    }

    /**
     * Searches for the parent are of the area given as parameter in
     * the Collection of areas.
     *
     * @parent area
     *      The area whose parent area is to be searched
     * @param collection
     *      The areas to search in.
     *
     * @return
     *      Either the parent if it has been found or null.
     */
    private NamedArea findParentIn(NamedArea area, Collection<NamedArea> areas) {
        NamedArea parent = area.getPartOf();
        if(parent != null && areas.contains(parent)){
            return parent;
        }
        return null;
    }

    class AreaNode {

        private final NamedArea area;
        private AreaNode parent = null;
        private final Set<AreaNode> subAreas = new HashSet<AreaNode>();

        /**
         * @param area
         */
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
            Collection<NamedArea> areas = new HashSet<NamedArea>();
            for(AreaNode node : subAreas) {
                areas.add(node.area);
            }
            return areas;
        }
    }

}
