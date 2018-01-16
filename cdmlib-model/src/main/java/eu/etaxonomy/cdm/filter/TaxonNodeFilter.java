/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.filter.LogicFilter.Op;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 *
 * @author a.mueller
 *
 */
public class TaxonNodeFilter implements Serializable{

    private static final long serialVersionUID = 2292886683987183999L;

    private List<LogicFilter<TaxonNode>> subtrees = new ArrayList<>();
    private List<LogicFilter<TaxonNode>> taxonNodes = new ArrayList<>();
    private List<LogicFilter<Classification>> classifications = new ArrayList<>();
    private List<LogicFilter<Taxon>> taxa = new ArrayList<>();
    private LogicFilter<Rank> rankMin = null;
    private LogicFilter<Rank> rankMax = null;

    private List<LogicFilter<NamedArea>> areaFilter = new ArrayList<>();

    private List<LogicFilter<PresenceAbsenceTerm>> distributionStatusFilter = new ArrayList<>();

    private boolean includeRootNodes = false;

    //********************** FACTORY ***************************/

    public static TaxonNodeFilter NewTaxonNodeInstance(UUID taxonNodeUuid){
        return new TaxonNodeFilter().orTaxonNode(taxonNodeUuid);
    }
    public static TaxonNodeFilter NewTaxonNodeInstance(TaxonNode taxonNode){
        return new TaxonNodeFilter().orTaxonNode(taxonNode);
    }

    public static TaxonNodeFilter NewClassificationInstance(UUID classificationUuid){
        return new TaxonNodeFilter().orClassification(classificationUuid);
    }
    public static TaxonNodeFilter NewClassificationInstance(Classification classification){
        return new TaxonNodeFilter().orClassification(classification);
    }


    public static TaxonNodeFilter NewSubtreeInstance(UUID subtreeUuid){
        return new TaxonNodeFilter().orSubtree(subtreeUuid);
    }
    public static TaxonNodeFilter NewSubtreeInstance(TaxonNode subtree){
        return new TaxonNodeFilter().orSubtree(subtree);
    }

    public static TaxonNodeFilter NewTaxonInstance(UUID taxonUuid){
        return new TaxonNodeFilter().orTaxon(taxonUuid);
    }

    public static TaxonNodeFilter NewTaxonInstance(Taxon taxon){
        return new TaxonNodeFilter().orTaxon(taxon);
    }

    public static TaxonNodeFilter NewRankInstance(Rank rankMin, Rank rankMax){
        return new TaxonNodeFilter().orRank(rankMin, rankMax);
    }

    public static TaxonNodeFilter NewInstance(Collection<UUID> classificationUuids,
            Collection<UUID> subtreeUuids, Collection<UUID> taxonNodeUuids,
            Collection<UUID> taxonUuids){

        TaxonNodeFilter result = new TaxonNodeFilter();
        classificationUuids = classificationUuids == null ? new ArrayList<>(): classificationUuids;
        subtreeUuids = subtreeUuids == null ? new ArrayList<>(): subtreeUuids;
        taxonNodeUuids = taxonNodeUuids == null ? new ArrayList<>(): taxonNodeUuids;
        taxonUuids = taxonUuids == null ? new ArrayList<>(): taxonUuids;

        for (UUID uuid : classificationUuids){
            result.orClassification(uuid);
        }
        for (UUID uuid : subtreeUuids){
            result.orSubtree(uuid);
        }
        for (UUID uuid : taxonNodeUuids){
            result.orTaxonNode(uuid);
        }
        for (UUID uuid : taxonUuids){
            result.orTaxon(uuid);
        }
        return result;

    }

// ************************ CONSTRUCTOR *******************/

    public TaxonNodeFilter(){
        reset();
    }

    public TaxonNodeFilter(TaxonNode node){
        reset();
        LogicFilter<TaxonNode> filter = new LogicFilter<>(node);
        subtrees.add(filter);
    }

    public TaxonNodeFilter(Rank rankMin, Rank rankMax){
        reset();
        if(rankMin!=null){
            this.rankMin = new LogicFilter<Rank>(rankMin);
        }
        if(rankMax!=null){
            this.rankMax = new LogicFilter<Rank>(rankMax);
        }
    }

    public TaxonNodeFilter(Classification classification){
        reset();
        LogicFilter<Classification> filter = new LogicFilter<>(classification);
        classifications.add(filter);
    }

    public TaxonNodeFilter(NamedArea area){
        reset();
        LogicFilter<NamedArea> filter = new LogicFilter<>(area);
        areaFilter.add(filter);
    }

    public TaxonNodeFilter(Taxon taxon){
        reset();
        LogicFilter<Taxon> filter = new LogicFilter<>(taxon);
        taxa.add(filter);
    }

//    public <T extends CdmBase> TaxonNodeFilter(Class<T> clazz, UUID uuid){
//        reset();
//        LogicFilter<T> filter = new LogicFilter<T>(clazz, uuid);
//        classifications.add(filter);
//    }

// ********************** reset *****************************/

    public void reset(){
        subtrees = new ArrayList<>();
        resetAreas();
        resetRanks();
        resetDistributionStatus();
        resetTaxonNodes();
        resetClassifications();
        resetTaxa();
    }

    private void resetDistributionStatus() {
        distributionStatusFilter = new ArrayList<>();
    }

    private void resetTaxonNodes() {
        taxonNodes = new ArrayList<>();
    }

    private void resetClassifications() {
        classifications = new ArrayList<>();
    }

    private void resetTaxa() {
        taxa = new ArrayList<>();
    }

    private void resetAreas() {
        areaFilter = new ArrayList<>();
    }

    private void resetRanks() {
        rankMin = null;
        rankMax = null;
    }

//*************************************

    public List<LogicFilter<TaxonNode>>getSubtreeFilter(){
        return Collections.unmodifiableList(subtrees);
    }

    public List<LogicFilter<TaxonNode>>getTaxonNodesFilter(){
        return Collections.unmodifiableList(taxonNodes);
    }


    public List<LogicFilter<Classification>>getClassificationFilter(){
        return Collections.unmodifiableList(classifications);
    }

    public List<LogicFilter<Taxon>>getTaxonFilter(){
        return Collections.unmodifiableList(taxa);
    }

    public List<LogicFilter<NamedArea>>getAreaFilter(){
        return Collections.unmodifiableList(areaFilter);
    }

    public List<LogicFilter<PresenceAbsenceTerm>>getDistributionStatusFilter(){
        return Collections.unmodifiableList(distributionStatusFilter);
    }

    public TaxonNodeFilter orSubtree(TaxonNode taxonNode){
        subtrees.add( new LogicFilter<>(taxonNode, Op.OR));
        return this;
    }

    public TaxonNodeFilter notSubtree(TaxonNode taxonNode){
        subtrees.add( new LogicFilter<>(taxonNode, Op.NOT));
        return this;
    }

    public TaxonNodeFilter orSubtree(UUID taxonNodeUuid){
        subtrees.add( new LogicFilter<>(TaxonNode.class, taxonNodeUuid, Op.OR));
        return this;
    }

    public TaxonNodeFilter orTaxonNode(TaxonNode taxonNode){
        taxonNodes.add( new LogicFilter<>(taxonNode, Op.OR));
        return this;
    }
    public TaxonNodeFilter orTaxonNode(UUID uuid){
        taxonNodes.add( new LogicFilter<>(TaxonNode.class, uuid, Op.OR));
        return this;
    }

    public TaxonNodeFilter notTaxonNode(TaxonNode taxonNode){
        taxonNodes.add( new LogicFilter<>(taxonNode, Op.NOT));
        return this;
    }

    public TaxonNodeFilter andTaxonNode(TaxonNode taxonNode){
        taxonNodes.add(new LogicFilter<>(taxonNode, Op.AND));
        return this;
    }

    public TaxonNodeFilter orTaxon(Taxon taxon){
        taxa.add( new LogicFilter<>(taxon, Op.OR));
        return this;
    }

    public TaxonNodeFilter orTaxon(UUID uuid){
        taxa.add( new LogicFilter<>(Taxon.class, uuid, Op.OR));
        return this;
    }

    public TaxonNodeFilter orArea(UUID uuid){
        areaFilter.add( new LogicFilter<>(NamedArea.class, uuid, Op.OR));
        return this;
    }

    public TaxonNodeFilter andArea(UUID uuid){
        areaFilter.add( new LogicFilter<>(NamedArea.class, uuid, Op.AND));
        return this;
    }

    public TaxonNodeFilter orRank(Rank rankMin, Rank rankMax){
        if(rankMin!=null){
            this.rankMin = new LogicFilter<Rank>(rankMin);
        }
        if(rankMax!=null){
            this.rankMax = new LogicFilter<>(rankMax);
        }
        return this;
    }

    public TaxonNodeFilter andRank(Rank rankMin, Rank rankMax){
        if(rankMin!=null){
            this.rankMin = new LogicFilter<Rank>(rankMin, Op.AND);
        }
        if(rankMax!=null){
            this.rankMax = new LogicFilter<>(rankMax, Op.AND);
        }
        return this;
    }

    public TaxonNodeFilter orClassification(Classification classification){
        classifications.add( new LogicFilter<>(classification, Op.OR));
        return this;
    }
    public TaxonNodeFilter orClassification(UUID uuid){
        classifications.add( new LogicFilter<>(Classification.class, uuid, Op.OR));
        return this;
    }

    public TaxonNodeFilter notTaxon(Taxon taxon){
        taxa.add( new LogicFilter<>(taxon, Op.NOT));
        return this;
    }

    public boolean isIncludeRootNodes() {
        return includeRootNodes;
    }
    public TaxonNodeFilter setIncludeRootNodes(boolean includeRootNodes) {
        this.includeRootNodes = includeRootNodes;
        return this;
    }

    public LogicFilter<Rank> getRankMax() {
        return rankMax;
    }

    public LogicFilter<Rank> getRankMin() {
        return rankMin;
    }

}
