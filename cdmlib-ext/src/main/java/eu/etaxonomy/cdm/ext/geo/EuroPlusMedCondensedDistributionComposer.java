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
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.kohlbecker
 * @date Jun 24, 2015
 *
 */
public class EuroPlusMedCondensedDistributionComposer extends CondensedDistributionComposerBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(EuroPlusMedCondensedDistributionComposer.class);

    private final CondensedDistribution condensedDistribution;

    private static Set<UUID> foreignStatusUuids;

    // these status uuids are special for EuroPlusMed and might also be used
    private final static UUID REPORTED_IN_ERROR_UUID =  UUID.fromString("38604788-cf05-4607-b155-86db456f7680");

    static {

        // ==================================================
        // Mapping as defined in ticket http://dev.e-taxonomy.eu/trac/ticket/3907
        // ==================================================

        statusSymbols = new HashMap<UUID, String> ();
        // ● endemic (U+25CF BLACK CIRCLE)
        statusSymbols.put(PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA().getUuid(), "\u25CF");

        // Lu native (incl. archaeophytes) TODO status archaeophytes?
        statusSymbols.put(PresenceAbsenceTerm.NATIVE().getUuid(), "");
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE().getUuid(), "");

        // ?Lu doubtfully present (U+3F QUESTION MARK)
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE().getUuid(), "?");
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_PRESENCE_QUESTIONABLE().getUuid(), "?");
        statusSymbols.put(PresenceAbsenceTerm.PRESENT_DOUBTFULLY().getUuid(), "?");

        // dLu doubtfully native
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_DOUBTFULLY_NATIVE().getUuid(), "d");

        // -Lu absent but reported in error (U+2D HYPHEN-MINUS)
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR().getUuid(), "-");
        statusSymbols.put(PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR().getUuid(), "-");
        statusSymbols.put(REPORTED_IN_ERROR_UUID, "-");

        // †Lu (presumably) extinct (U+2020 DAGGER)
        // no such status in database!!!

        // [Lu] introduced (casual or naturalized) =  introduced, introduced: naturalized
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED().getUuid(), "");

        // [aLu] casual alien = introduced: adventitious (casual)
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_ADVENTITIOUS().getUuid(), "a");

        // [cLu] cultivated
        statusSymbols.put(PresenceAbsenceTerm.CULTIVATED() .getUuid(), "c");
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_CULTIVATED().getUuid(), "c");

        // [nLu] naturalized
        statusSymbols.put(PresenceAbsenceTerm.NATURALISED().getUuid(), "n");
        statusSymbols.put(PresenceAbsenceTerm.INTRODUCED_NATURALIZED().getUuid(), "n");

        foreignStatusUuids = new HashSet<UUID>();
        foreignStatusUuids.add(PresenceAbsenceTerm.INTRODUCED().getUuid());
        foreignStatusUuids.add(PresenceAbsenceTerm.INTRODUCED_NATURALIZED().getUuid());
        foreignStatusUuids.add(PresenceAbsenceTerm.INTRODUCED_ADVENTITIOUS().getUuid());
        foreignStatusUuids.add(PresenceAbsenceTerm.INTRODUCED_CULTIVATED().getUuid());
        foreignStatusUuids.add(PresenceAbsenceTerm.NATURALISED().getUuid());
        foreignStatusUuids.add(PresenceAbsenceTerm.CULTIVATED().getUuid());

    }

    public EuroPlusMedCondensedDistributionComposer() {
        condensedDistribution = new CondensedDistribution();
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public CondensedDistribution createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> langs) {

        //1. order by PresenceAbsenceTerms
        Map<PresenceAbsenceTerm, Collection<NamedArea>> byStatus = new HashMap<PresenceAbsenceTerm, Collection<NamedArea>>();
        for(Distribution d : filteredDistributions) {
            PresenceAbsenceTerm status = d.getStatus();
            if(status == null) {
                continue;
            }
            if(!byStatus.containsKey(status)) {
                byStatus.put(status, new HashSet<NamedArea>());
            }
            byStatus.get(status).add(d.getArea());
        }

        //2. build the area hierarchy
        for(PresenceAbsenceTerm status : byStatus.keySet()) {

            Map<NamedArea, AreaNode> areaNodeMap = new HashMap<NamedArea, AreaNode>();

            for(NamedArea area : byStatus.get(status)) {
                AreaNode node;
                if(!areaNodeMap.containsKey(area)) {
                    // putting area into hierarchy as node
                    node = new AreaNode(area);
                    areaNodeMap.put(area, node);
                } else {
                    //  is parent of another and thus already has a node
                    node = areaNodeMap.get(area);
                }

                NamedArea parent = findParentIn(area, byStatus.get(status));
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
            Set<AreaNode>hierarchy = new HashSet<AreaNode>();
            for(AreaNode node : areaNodeMap.values()) {
                if(!node.hasParent()) {
                    hierarchy.add(node);
                }
            }

            //4. replace the area by the abbreviated representation and add symbols
            for(AreaNode node : hierarchy) {

                StringBuilder areaStatusString = new StringBuilder();

                String statusSymbol = statusSymbol(status);
                areaStatusString.append(statusSymbol);

                String areaLabel = node.area.getPreferredRepresentation(langs).getAbbreviatedLabel();
                areaStatusString.append(areaLabel);

                if(!node.subAreas.isEmpty()) {
                    areaStatusString.append('(');
                    subAreaLabels(langs, node.subAreas, areaStatusString, statusSymbol, areaLabel);
                    areaStatusString.append(')');
                }

                if(isForeignStatus(status)) {
                    condensedDistribution.addForeignDistributionItem(status, areaStatusString.toString(), areaLabel);
                } else {
                    condensedDistribution.addIndigenousDistributionItem(status, areaStatusString.toString(), areaLabel);
                }

            }

        }
        //5. order the condensedDistributions alphabetically
        condensedDistribution.sortForeign();
        condensedDistribution.sortIndigenous();

        return condensedDistribution;
    }


    private boolean isForeignStatus(PresenceAbsenceTerm status) {
        return foreignStatusUuids.contains(status.getUuid());
    }

    /**
     * @param langs
     * @param node
     * @param areaString
     * @param statusSymbol
     */
    private void subAreaLabels(List<Language> langs, Collection<AreaNode> nodes, StringBuilder areaString, String statusSymbol, String parentLabel) {

        List<String> subAreaLabels = new ArrayList<String>();

        for(AreaNode node : nodes) {

            StringBuilder subAreaString = new StringBuilder();

            subAreaString.append(statusSymbol);

            String areaLabel = node.area.getPreferredRepresentation(langs).getAbbreviatedLabel();
            String cleanSubAreaLabel = StringUtils.replaceEach(areaLabel, new String[] {parentLabel, "(", ")"}, new String[] {"", "", ""});
            subAreaString.append(cleanSubAreaLabel);

            if(!node.subAreas.isEmpty()) {
                subAreaString.append('(');
                subAreaLabels(langs, node.subAreas, subAreaString, statusSymbol, areaLabel);
                subAreaString.append(')');
            }

            subAreaLabels.add(subAreaString.toString());
        }
        Collections.sort(subAreaLabels);
        areaString.append(StringUtils.join(subAreaLabels, " "));

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
