/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Base class for Distribution Composers
 * @author a.mueller
 \* @since 02.06.2016
 *
 */
public abstract class CondensedDistributionComposerBase implements ICondensedDistributionComposer{

    protected static Map<UUID, String> statusSymbols;



    protected String areaPreTag = "<b>";

    protected String areaPostTag = "</b>";


    protected boolean replaceCommonAreaLabelStart;

    //for future use in combined DistributionComposer
    private boolean sortByStatus;


    /**
     * @param langs
     * @param area
     * @return
     */
    protected String makeAreaLabel(List<Language> langs, NamedArea area) {
        String result = area.getIdInVocabulary() != null ? area.getIdInVocabulary() :area.getPreferredRepresentation(langs).getAbbreviatedLabel();
        return areaPreTag + result + areaPostTag;
    }


    /**
     * @param status
     * @return
     */
    protected String statusSymbol(PresenceAbsenceTerm status) {
        if(status == null) {
            return "";
        }
        String symbol = statusSymbols.get(status.getUuid());
        if(symbol != null) {
            return symbol;
        }else if (status.getSymbol() != null){
            return status.getSymbol();
        }else if (status.getIdInVocabulary() != null){
            return status.getIdInVocabulary();
        }else {
            Representation r = status.getPreferredRepresentation((Language)null);
            if (r != null){
                String abbrevLabel = r.getAbbreviatedLabel();
                if (abbrevLabel != null){
                    return abbrevLabel;
                }
            }
        }

        return "n.a.";
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
        protected final Set<AreaNode> subAreas = new HashSet<AreaNode>();

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


        @Override
        public String toString() {
            return "AreaNode [area=" + area + "]";
        }
    }


    /**
     * @param status
     * @return
     */
    //Keep here only in case new version does creates problems in E+M
    //can be deleted if no problem occurs
    private String statusSymbolEuroMedOld(PresenceAbsenceTerm status) {
        if(status == null) {
            return "";
        }
        String symbol = statusSymbols.get(status.getUuid());
        if(symbol == null) {
            symbol = "";
        }
        return symbol;
    }


    public String getAreaPreTag() {
        return areaPreTag;
    }

    public void setAreaPreTag(String areaPreTag) {
        this.areaPreTag = areaPreTag;
    }

    public String getAreaPostTag() {
        return areaPostTag;
    }

    public void setAreaPostTag(String areaPostTag) {
        this.areaPostTag = areaPostTag;
    }



}
