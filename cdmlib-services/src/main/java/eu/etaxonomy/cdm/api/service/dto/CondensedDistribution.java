/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * @author a.kohlbecker
 * @since Jun 25, 2015
 *
 */
public class CondensedDistribution {

    private List<DistributionItem> indigenous = new ArrayList<DistributionItem>();
    private List<DistributionItem> foreign = new ArrayList<DistributionItem>();

    /**
     * @return the foreign
     */
    public List<DistributionItem> getForeign() {
        return foreign;
    }

    /**
     * @param foreign the foreign to set
     */
    public void setForeign(List<DistributionItem> foreign) {
        this.foreign = foreign;
    }

    /**
     * @param indigenous the indigenous to set
     */
    public void setIndigenous(List<DistributionItem> indigenous) {
        this.indigenous = indigenous;
    }

    /**
     * @return the indigenous
     */
    public List<DistributionItem> getIndigenous() {
        return indigenous;
    }

    public void addForeignDistributionItem(PresenceAbsenceTerm status, String areaStatusLabel, String sortString) {
        foreign.add(new DistributionItem(status, areaStatusLabel, sortString));
    }

    public void addIndigenousDistributionItem(PresenceAbsenceTerm status, String areaStatusLabel, String sortString) {
        indigenous.add(new DistributionItem(status, areaStatusLabel, sortString));
    }


    public void sortForeign() {
        Collections.sort(foreign, new CondensedDistributionComparator());
    }

    public void sortIndigenous() {
        Collections.sort(indigenous, new CondensedDistributionComparator());
    }

    @Override
    public String toString() {

        StringBuilder out = new StringBuilder();

        boolean isFirst = true;
        for(DistributionItem item : indigenous) {
            if(!isFirst) {
                out.append(" ");
            }
            out.append(item.areaStatusLabel);
            isFirst = false;
        }

        if(!isFirst) {
            out.append(" ");
        }
        isFirst = true;
        if(!foreign.isEmpty()) {
            out.append("[");
            for(DistributionItem item : foreign) {
                if(!isFirst) {
                    out.append(" ");
                }
                out.append(item.areaStatusLabel);
                isFirst = false;
            }
            out.append("]");
        }
        return out.toString();
    }

    public class DistributionItem {


        private PresenceAbsenceTerm status;
        private String areaStatusLabel;
        private final String sortString;

        /**
         * @param status
         * @param areaStatusLabel
         */
        public DistributionItem(PresenceAbsenceTerm status, String areaStatusLabel, String sortString) {
            this.status = status;
            this.areaStatusLabel = areaStatusLabel;
            this.sortString = sortString;
        }
        /**
         * @return the status
         */
        public PresenceAbsenceTerm getStatus() {
            return status;
        }

        /**
         * @return the areaStatusLabel
         */
        public String getAreaStatusLabel() {
            return areaStatusLabel;
        }

        /**
         * @param status the status to set
         */
        public void setStatus(PresenceAbsenceTerm status) {
            this.status = status;
        }
        /**
         * @param areaStatusLabel the areaStatusLabel to set
         */
        public void setAreaStatusLabel(String areaStatusLabel) {
            this.areaStatusLabel = areaStatusLabel;
        }
    }

    class CondensedDistributionComparator implements Comparator<DistributionItem>{

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(DistributionItem o1, DistributionItem o2) {
            return o1.sortString.compareToIgnoreCase(o2.sortString);
        }



    }
}
