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
 * Configuration for filtering taxon nodes. This filter defines a set
 * of TaxonNodes. It can be used for searching and filtering data.
 *
 * @author a.mueller
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

    private boolean includeUnpublished = false;

    private ORDER orderBy = null;

    public enum ORDER{
        ID("tn.id"),
        TREEINDEX("tn.treeIndex"),
        TREEINDEX_DESC("tn.treeIndex DESC");
        String hql;
        private ORDER(String hql){
            this.hql = hql;
        }
        public String getHql(){
            return hql;
        }
    }

//************************ FACTORY ***************************/

    public static TaxonNodeFilter NewInstance(){
        return NewInstance(null, null, null, null, null, null, null);
    }

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
        return new TaxonNodeFilter().setRankMin(rankMin).setRankMax(rankMax);
    }

    public static TaxonNodeFilter NewRankInstance(UUID rankMinUuid, UUID rankMaxUuid){
        return new TaxonNodeFilter().setRankMin(rankMinUuid).setRankMax(rankMaxUuid);
    }

    public static TaxonNodeFilter NewAreaInstance(NamedArea area){
        return new TaxonNodeFilter().orArea(area);
    }
    public static TaxonNodeFilter NewAreaInstance(UUID areaUuid){
        return new TaxonNodeFilter().orArea(areaUuid);
    }

    public static TaxonNodeFilter NewInstance(Collection<UUID> classificationUuids,
            Collection<UUID> subtreeUuids, Collection<UUID> taxonNodeUuids,
            Collection<UUID> taxonUuids, Collection<UUID> areaUuids,
            UUID minRank, UUID maxRank){

        TaxonNodeFilter result = new TaxonNodeFilter().setRankMin(minRank).setRankMax(maxRank);
        classificationUuids = classificationUuids == null ? new ArrayList<>(): classificationUuids;
        subtreeUuids = subtreeUuids == null ? new ArrayList<>(): subtreeUuids;
        taxonNodeUuids = taxonNodeUuids == null ? new ArrayList<>(): taxonNodeUuids;
        taxonUuids = taxonUuids == null ? new ArrayList<>(): taxonUuids;
        areaUuids = areaUuids == null ? new ArrayList<>(): areaUuids;

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
        for (UUID uuid : areaUuids){
            result.orArea(uuid);
        }
        return result;
    }

// ************************ CONSTRUCTOR *******************/

    private TaxonNodeFilter(){
        reset();
    }

    public TaxonNodeFilter(NamedArea area){
        reset();
        LogicFilter<NamedArea> filter = new LogicFilter<>(area);
        areaFilter.add(filter);
    }

// ********************** reset *****************************/

    public void reset(){
        resetSubtrees();
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

    private void resetSubtrees() {
        subtrees = new ArrayList<>();
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
    public TaxonNodeFilter notSubtree(UUID taxonNodeUuid){
        subtrees.add( new LogicFilter<>(TaxonNode.class, taxonNodeUuid, Op.NOT));
        return this;
    }

    public TaxonNodeFilter orSubtree(UUID taxonNodeUuid){
        subtrees.add( new LogicFilter<>(TaxonNode.class, taxonNodeUuid, Op.OR));
        return this;
    }

    /**
     * Adds a single {@link TaxonNode} to the filter.<BR><BR>
     * NOTE: this adds only the node to the filter, not it's children!
     */
    public TaxonNodeFilter orTaxonNode(TaxonNode taxonNode){
        taxonNodes.add( new LogicFilter<>(taxonNode, Op.OR));
        return this;
    }
    /**
     * Adds a single {@link TaxonNode} to the filter.<BR><BR>
     * NOTE: this adds only the node to the filter, not it's children!
     */
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
    public TaxonNodeFilter orArea(NamedArea area){
        areaFilter.add( new LogicFilter<>(area, Op.OR));
        return this;
    }

//And filter on areas makes sense only in very specific cases.
//We remove it until it is really required
//    public TaxonNodeFilter andArea(UUID uuid){
//        areaFilter.add( new LogicFilter<>(NamedArea.class, uuid, Op.AND));
//        return this;
//    }
//    public TaxonNodeFilter andArea(NamedArea area){
//        areaFilter.add( new LogicFilter<>(area, Op.AND));
//        return this;
//    }


    public TaxonNodeFilter setRankMin(Rank rankMin){
        this.rankMin = rankMin == null? null : new LogicFilter<>(rankMin, Op.AND);
        return this;
    }
    public TaxonNodeFilter setRankMin(UUID rankMinUuid){
        this.rankMin = rankMinUuid == null? null : new LogicFilter<>(Rank.class, rankMinUuid, Op.AND);
        return this;
    }

    public TaxonNodeFilter setRankMax(Rank rankMax){
        this.rankMax = rankMax == null? null : new LogicFilter<>(rankMax, Op.AND);
        return this;
    }
    public TaxonNodeFilter setRankMax(UUID rankMaxUuid){
        this.rankMax = rankMaxUuid == null? null : new LogicFilter<>(Rank.class, rankMaxUuid, Op.AND);
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

    public LogicFilter<Rank> getRankMax() {
        return rankMax;
    }
    public LogicFilter<Rank> getRankMin() {
        return rankMin;
    }

    public boolean isIncludeUnpublished() {
        return includeUnpublished;
    }
    public void setIncludeUnpublished(boolean includeUnpublished) {
        this.includeUnpublished = includeUnpublished;
    }


    /**
     * If <code>true</code> the result will include the root node of
     * a classification.
     * Note: As a root node per se has no taxon all filters with filter
     * on taxon related data have no effect for the root node. If the
     * root node is returned only depends on the
     * {@link TaxonNodeFilter#isIncludeRootNodes() includeRootNodes}
     * parameter.
     */
    public boolean isIncludeRootNodes() {
        return includeRootNodes;
    }
    public TaxonNodeFilter setIncludeRootNodes(boolean includeRootNodes) {
        this.includeRootNodes = includeRootNodes;
        return this;
    }

    public boolean hasClassificationFilter(){
        return getClassificationFilter() != null  //just in case, but should never be null
                && !getClassificationFilter().isEmpty();
    }

    public boolean hasSubtreeFilter(){
        return getSubtreeFilter() != null  //just in case, but should never be null
                && !getSubtreeFilter().isEmpty();
    }

    public ORDER getOrderBy() {
        return orderBy;
    }
    public void setOrder(ORDER orderBy) {
        this.orderBy = orderBy;
    }

// ************************** toString *********************************/

    @Override
    public String toString() {
        return "TaxonNodeFilter [subtrees=" + subtrees + ", taxonNodes=" + taxonNodes + ", classifications="
                + classifications + ", taxa=" + taxa + ", rankMin=" + rankMin + ", rankMax=" + rankMax + ", areaFilter="
                + areaFilter + ", distributionStatusFilter=" + distributionStatusFilter + ", includeRootNodes="
                + includeRootNodes + ", includeUnpublished=" + includeUnpublished + ", orderBy=" + orderBy + "]";
    }
}