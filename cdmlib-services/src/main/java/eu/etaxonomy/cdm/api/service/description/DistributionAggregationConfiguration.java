/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.description;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.LogicFilter;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author a.mueller
 * @since 05.11.2019
 */
public class DistributionAggregationConfiguration
        extends DescriptionAggregationConfigurationBase<DistributionAggregation> {

    private static final long serialVersionUID = 2542246141660930545L;

    private List<UUID> superAreasUuids;

    private TermCollection<PresenceAbsenceTerm, TermNode> statusOrder;

    private boolean ignoreAbsentStatusByArea = true;

    private boolean ignoreAbsentStatusByRank = true;

//    private boolean ignoreEndemicStatusByArea = true;
//
//    private boolean ignoreEndemicStatusByRank = true;


    private List<PresenceAbsenceTerm> byAreaIgnoreStatusList = null;

    private List<PresenceAbsenceTerm> byRankIgnoreStatusList = null;

// **************************************** FACTORY ************************************/

    public static DistributionAggregationConfiguration NewInstance(List<AggregationMode> aggregationModes, List<UUID> superAreas,
            TaxonNodeFilter filter, IProgressMonitor monitor){


        return new DistributionAggregationConfiguration(aggregationModes, superAreas, filter, monitor);
    }

    public static DistributionAggregationConfiguration NewInstance(List<AggregationMode> aggregationModes, List<UUID> superAreas,
            TaxonNodeFilter filter, TermTree<PresenceAbsenceTerm> statusOrder,  IProgressMonitor monitor){

        DistributionAggregationConfiguration result = new DistributionAggregationConfiguration(aggregationModes, superAreas, filter, monitor);
        result.setStatusOrder(statusOrder);
        return result;
    }



// ************************ CONSTRUCTOR *****************************/

    private DistributionAggregationConfiguration(List<AggregationMode> aggregationModes, List<UUID> superAreas,
            TaxonNodeFilter filter, IProgressMonitor monitor) {
        super(filter, monitor, aggregationModes);
        this.superAreasUuids = superAreas;
    }

// ******************** METHOD **************************************/

    @Override
    public DistributionAggregation getTaskInstance() {
        return new DistributionAggregation();
    }

// ******************* GETTER / SETTER ****************************/

    public List<UUID> getSuperAreas() {
        return superAreasUuids;
    }
    public void setSuperAreas(List<UUID> superAreas) {
        this.superAreasUuids = superAreas;
    }

    public UUID getLowerRank() {
        LogicFilter<Rank> rankMin = getTaxonNodeFilter().getRankMin();
        return rankMin == null ? null: rankMin.getUuid();
    }

    public UUID getUpperRank() {
        LogicFilter<Rank> rankMax = getTaxonNodeFilter().getRankMax();
        return rankMax == null ? null: rankMax.getUuid();
    }

    public TermCollection<PresenceAbsenceTerm, TermNode> getStatusOrder() {
        return statusOrder;
    }
    public void setStatusOrder(TermCollection<PresenceAbsenceTerm, TermNode> statusOrder) {
        this.statusOrder = statusOrder;
    }

    /**
     * byAreaIgnoreStatusList contains by default:
     *  <ul>
     *    <li>AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR()</li>
     *    <li>AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR()</li>
     *    <li>AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED()</li>
     *    <li>AbsenceTerm.NATIVE_REPORTED_IN_ERROR()</li>
     *    <li>AbsenceTerm.NATIVE_FORMERLY_NATIVE()</li>
     *  </ul>
     *
     * @return the byAreaIgnoreStatusList
     */
    public List<PresenceAbsenceTerm> getByAreaIgnoreStatusList() {
        if(byAreaIgnoreStatusList == null ){
            byAreaIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTerm[] {
                            PresenceAbsenceTerm.CULTIVATED_REPORTED_IN_ERROR(),
                            PresenceAbsenceTerm.INTRODUCED_REPORTED_IN_ERROR(),
                            PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR(),
                            PresenceAbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED(),
                            PresenceAbsenceTerm.NATIVE_FORMERLY_NATIVE()
                            // TODO what about PresenceAbsenceTerm.ABSENT() also ignore?
                    });
        }
        return byAreaIgnoreStatusList;
    }

    public void setByAreaIgnoreStatusList(List<PresenceAbsenceTerm> byAreaIgnoreStatusList) {
        this.byAreaIgnoreStatusList = byAreaIgnoreStatusList;
    }

    /**
     * Ranks to be ignored if aggregated to next higher rank.
     * byRankIgnoreStatusList contains by default
     *  <ul>
     *    <li>PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA()</li>
     *    <li>PresenceTerm.ENDEMIC_DOUBTFULLY_PRESENT()</li>
     *    <li>PresenceTerm.ENDEMIC_REPORTED_IN_ERROR()</li>
     *    <li>PresenceTerm.NOT_ENDEMIC_FOR_THE_RELEVANT_AREA()</li>
     *  </ul>
     *
     * @return the byRankIgnoreStatusList
     */
    public List<PresenceAbsenceTerm> getByRankIgnoreStatusList() {

        if (byRankIgnoreStatusList == null) {
            byRankIgnoreStatusList = Arrays.asList(
                    new PresenceAbsenceTerm[] {
                            PresenceAbsenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA(),
                            PresenceAbsenceTerm.ENDEMIC_DOUBTFULLY_PRESENT(),
                            PresenceAbsenceTerm.ENDEMIC_REPORTED_IN_ERROR(),
                            PresenceAbsenceTerm.NOT_ENDEMIC_FOR_THE_RELEVANT_AREA()
                    });
        }
        return byRankIgnoreStatusList;
    }

    public void setByRankIgnoreStatusList(List<PresenceAbsenceTerm> byRankIgnoreStatusList) {
        this.byRankIgnoreStatusList = byRankIgnoreStatusList;
    }

    public boolean isIgnoreAbsentStatusByArea() {
        return ignoreAbsentStatusByArea;
    }
    public void setIgnoreAbsentStatusByArea(boolean ignoreAbsentStatusByArea) {
        this.ignoreAbsentStatusByArea = ignoreAbsentStatusByArea;
    }

    public boolean isIgnoreAbsentStatusByRank() {
        return ignoreAbsentStatusByRank;
    }
    public void setIgnoreAbsentStatusByRank(boolean ignoreAbsentStatusByRank) {
        this.ignoreAbsentStatusByRank = ignoreAbsentStatusByRank;
    }
}
