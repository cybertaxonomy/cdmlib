package eu.etaxonomy.cdm.strategy.generate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.BigDecimalUtil;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;

/**
 * @author m.venin
 * @author a.mueller
 */
public class PolytomousKeyGenerator {

    private static final Logger logger = LogManager.getLogger(PolytomousKeyGenerator.class);

    /**
     * Strings used for generating the statements of the key.
     */
    private static final String before="<";
    private static final String after=">";
    private static final String separator = " or ";


    private PolytomousKeyGeneratorConfigurator config;

	private Map<FeatureState,Set<Feature>> iAifDependencies = new HashMap<>(); // map of a set of Features (value) inapplicables if a State (key) is present
	private Map<FeatureState,Set<Feature>> oAifDependencies = new HashMap<>(); // map of a set of Features (value) only applicables if a State (key) is present
	private Map<Feature,Set<Feature>> featureDependencies = new HashMap<>(); // map of all the sets of features (values) which have dependencies with states of other features (keys)

	private class KeyTaxon{
	    private UUID uuid;
	    private Taxon taxon;
	    private SpecimenOrObservationBase<?> specimen;
	    private Map<Feature,Set<CategoricalData>> categoricalData = new HashMap<>();
        private Map<Feature,Set<QuantitativeData>> quantitativeData = new HashMap<>();
        private Set<KeyTaxon> children = new HashSet<>();

        private Set<CategoricalData> getCategoricalData(Feature feature){
            return categoricalData.get(feature) == null? new HashSet<>():categoricalData.get(feature);
        }
        private Set<QuantitativeData> getQuantitativeData(Feature feature){
            return quantitativeData.get(feature) == null? new HashSet<>():quantitativeData.get(feature);
        }

        private void addDescription(DescriptionBase<?> db) {
            for (DescriptionElementBase deb : db.getElements()){
                Feature feature = deb.getFeature();
                if (deb.isCharacterData()){
                    if (deb.isInstanceOf(CategoricalData.class)){
                        CategoricalData cd = CdmBase.deproxy(deb, CategoricalData.class);
                        if (categoricalData.get(feature)== null){
                            categoricalData.put(feature, new HashSet<>());
                        }
                        categoricalData.get(feature).add(cd);
                    }else if (deb.isInstanceOf(QuantitativeData.class)){
                        QuantitativeData qd = CdmBase.deproxy(deb, QuantitativeData.class);
                        if (quantitativeData.get(feature)== null){
                            quantitativeData.put(feature, new HashSet<>());
                        }
                        quantitativeData.get(feature).add(qd);
                    }
                }
            }
        }

        @Override
        public String toString() {
            return taxon.getTitleCache(); // + "KeyTaxon [uuid=" + uuid + (taxon != null? ", " + taxon.getTitleCache():"") + "]";  // uuid=" + uuid +
        }
	}

// *************************** METHODS ***************************************/

    /**
     * Creates the key
     */
    public PolytomousKey invoke(PolytomousKeyGeneratorConfigurator config){
        if (config == null){
            throw new NullPointerException("PolytomousKeyGeneratorConfigurator must not be null");
        }
        this.config = config;
        if (this.config.isUseDependencies()){
            createDependencies(config.getDependenciesTree().getRoot());
        }
        PolytomousKey polytomousKey = PolytomousKey.NewInstance();
        PolytomousKeyNode root = polytomousKey.getRoot();
        @SuppressWarnings("unchecked")
        Set<KeyTaxon> taxaCovered = makeKeyTaxa((Set)config.getTaxonDescriptions());
        taxaCovered = replaceSingleRoot(taxaCovered);

        //filter if a feature is available only for certain states in this branche
        Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter = new HashMap<>();
        //TODO what if size(taxaCovered) <= 1, is this covered by algo? Write test to check
        buildBranches(root, config.getFeatures(), taxaCovered, featureStatesFilter);
        return polytomousKey;
    }

    /**
     * If the root taxon is a single taxon but has children
     * it will be replaced by it's children.
     */
    private Set<KeyTaxon> replaceSingleRoot(Set<KeyTaxon> taxaCovered) {
        if (this.config.isUseTaxonHierarchy() && taxaCovered.size()==1
                && !taxaCovered.iterator().next().children.isEmpty()){
            return replaceSingleRoot(taxaCovered.iterator().next().children);
        }else{
            return taxaCovered;
        }
    }

    private Set<KeyTaxon> makeKeyTaxa(Set<DescriptionBase<?>> descriptions) {

        Map<UUID,KeyTaxon> taxonMap = new HashMap<>();
        for (DescriptionBase<?> db : descriptions){
            KeyTaxon taxon = new KeyTaxon();
            if (db.isInstanceOf(TaxonDescription.class)){
                TaxonDescription td = CdmBase.deproxy(db, TaxonDescription.class);
                taxon.uuid = td.getTaxon().getUuid();
                taxon.taxon = td.getTaxon();
            }else if (db.isInstanceOf(SpecimenDescription.class)){
                SpecimenDescription sd = CdmBase.deproxy(db, SpecimenDescription.class);
                taxon.uuid = sd.getDescribedSpecimenOrObservation().getUuid();
                taxon.specimen = sd.getDescribedSpecimenOrObservation();
            }else{
                throw new RuntimeException("Unhandled entity type " + db.getClass().getName());
            }
            if (taxonMap.get(taxon.uuid)!= null){
                taxon = taxonMap.get(taxon.uuid);
            }else{
                taxonMap.put(taxon.uuid, taxon);
            }
            taxon.addDescription(db);
        }

        createTaxonHierarchy(taxonMap);

        return new HashSet<>(taxonMap.values());
    }

    private void createTaxonHierarchy(Map<UUID, KeyTaxon> taxonMap) {
        if(config.isUseTaxonHierarchy()==false){
            return;
        }
        Set<KeyTaxon> taxaToTest = new HashSet<>(taxonMap.values());
        for(KeyTaxon taxon:taxaToTest){
            KeyTaxon parent = getBestTaxonParent(taxon, taxaToTest);
            if (parent != null){
                parent.children.add(taxon);
                taxonMap.remove(taxon.uuid);
            }
        }
    }

    private KeyTaxon getBestTaxonParent(KeyTaxon taxon, Collection<KeyTaxon> values) {
        KeyTaxon parent = null;
        String parentIndex = "";
        String myTreeIndex = getTaxonTreeIndex(taxon);
        if (myTreeIndex != null) {
            for (KeyTaxon candidate:values){
                String candidateIndex = getTaxonTreeIndex(candidate);
                if (candidateIndex == null || myTreeIndex.equals(candidateIndex)){
                    continue;
                }
                if (myTreeIndex.startsWith(candidateIndex)){
                    if (candidateIndex.length()> parentIndex.length()){
                        parent = candidate;
                        parentIndex = candidateIndex;
                    }
                }
            }
        }
        return parent;
    }

    private String getTaxonTreeIndex(KeyTaxon taxon) {
        if (taxon.taxon.getTaxonNodes().isEmpty()){
            return null;
        }
        //TODOO size>1  or classification check
        TaxonNode node = taxon.taxon.getTaxonNodes().iterator().next();
        String treeIndex = node.treeIndex();
        if (treeIndex == null){
            //unpersisted, this should only happen during test, create provisional treeindex
            treeIndex = getParentTreeIndex(node) + node.getUuid().toString() + "#" ;
        }
        return treeIndex;
    }

    private String getParentTreeIndex(TaxonNode node) {
        TaxonNode parent = node.getParent();
        if (parent == null ){
            return "#";
        }else{
            return getParentTreeIndex(parent) + parent.getUuid().toString() + "#" ;
        }
    }

    /**
	 * Recursive function that builds the branches of the identification key
	 *
	 * @param parent the node considered
	 * @param featuresLeft List of features that can be used at this point
	 * @param taxaCovered the taxa left at this point (i.e. that verify the description corresponding to the path leading to this node)
     * @param featureStatesFilter
	 */
	private void buildBranches(PolytomousKeyNode parent, List<Feature> featuresLeft, Set<KeyTaxon> taxaCovered,
	        Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter){

	    //handle all branches taxa =>
        Set<KeyTaxon> allBranchesTaxa = getAllBranchesTaxa(featuresLeft, taxaCovered, featureStatesFilter);
        if (allBranchesTaxa.size()>0){
            if (allBranchesTaxa.size()>1){
            //TODO test if this case in handled and displayed correctly
                logger.warn(">1 final taxa in inner node");
            }
            taxaCovered.removeAll(allBranchesTaxa);
            if(taxaCovered.size() != 1){
                handleLeaf(parent, allBranchesTaxa);
            }else{
                //if only 1 is left it is better to handle all remaining in sub-branch to make difference clearer
                taxaCovered.addAll(allBranchesTaxa);
            }
        }

        //start real branching
	    if (taxaCovered.size()<=1){
		    //do nothing
	        logger.warn("Only 1 or no taxon covered. This should currently only be possible on top level and is not yet handled. ");
		}else {
			// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
			Map<Feature,BigDecimal> quantitativeFeaturesThresholds = new HashMap<>();
			// the scores of the different features are calculated, the thresholds in the same time
			if (config.isDebug()){
			    System.out.println("Feature left: " + featuresLeft + ", taxa: " + taxaCovered);
			}
			Feature winnerFeature = computeScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds, featureStatesFilter);

			if(winnerFeature != null){
			    /************** either the feature supports quantitative data... **************/
			    // NB: in this version, "quantitative features" are dealt with in a dichotomous way
			    if (winnerFeature.isSupportsQuantitativeData()) {
			        featuresLeft.add(winnerFeature);  //as quantitative data are currently only split in 2 parts there might be further splits possible and needed
			        handleQuantitativeData(parent, featuresLeft, taxaCovered,
			                quantitativeFeaturesThresholds, winnerFeature, featureStatesFilter);
			    }
			    /************** ...or it supports categorical data. **************/
			    else  if (winnerFeature.isSupportsCategoricalData()) {
			        handleCategorialFeature(parent, featuresLeft, taxaCovered,
			                winnerFeature, featureStatesFilter);
			    }else{
	                throw new RuntimeException("Winner feature does not support character data.");
			    }
			    // the winner features are put back to the features left once the branch is done
			    if (!featuresLeft.contains(winnerFeature)){  //TODO why is a list and not a set?
			        featuresLeft.add(winnerFeature);
			    }
			}else if (featuresLeft.isEmpty()){
			    handleLeaf(parent, taxaCovered);
			}else{
			    throw new RuntimeException("No winner feature but features left to handle should not happen.");
			}
		}
        return;
	}

    private Set<KeyTaxon> getAllBranchesTaxa(List<Feature> featuresLeft, Set<KeyTaxon> taxaCovered,
            Map<Feature, Set<eu.etaxonomy.cdm.model.term.DefinedTermBase<?>>> featureStatesFilter) {

        Set<KeyTaxon> candidates = new HashSet<>(taxaCovered);
        List<Feature> dependendFeatures = new ArrayList<>();
        for (Feature feature : featuresLeft){
            if(feature.isSupportsCategoricalData()){
                Set<DefinedTermBase<?>> allStates = getAllStates(feature, taxaCovered, featureStatesFilter.get(feature));
                Iterator<KeyTaxon> it = candidates.iterator();
                while (it.hasNext()){
                    Set<KeyTaxon> taxonSet = new HashSet<>(Arrays.asList(it.next()));
                    Set<DefinedTermBase<?>> taxonStates = getAllStates(feature, taxonSet, featureStatesFilter.get(feature));
                    if(allStates.size() > taxonStates.size()){
                        it.remove();
                    }
                }
                if(candidates.isEmpty()){
                    break;
                }else{
                    addDependentFeatures(dependendFeatures, feature, new HashSet<>(), new ArrayList<>(allStates));
                }
            }else if (feature.isSupportsQuantitativeData()){
                Iterator<KeyTaxon> it = candidates.iterator();
                while (it.hasNext()){
                    BigDecimal min = BigDecimalUtil.MAX_BIGDECIMAL;
                    BigDecimal max = BigDecimalUtil.MIN_BIGDECIMAL;
                    Set<QuantitativeData> qds = it.next().quantitativeData.get(feature);
                    qds = qds == null? new HashSet<>(): qds;
                    for (QuantitativeData qd : qds){
                        BigDecimal qdMin = qd.getOverallMin();
                        if(qdMin != null){
                            min = min.min(qdMin);
                        }
                        BigDecimal qdMax = qd.getOverallMax();
                        if(qdMax != null){
                            max = max.max(qdMax);
                        }
                    }
                    boolean staysCandidate = true;
                    for(KeyTaxon taxon : taxaCovered){
                        Set<QuantitativeData> tqds = taxon.quantitativeData.get(feature);
                        tqds = tqds == null? new HashSet<>(): tqds;
                        for (QuantitativeData qd : tqds){
                            staysCandidate &= qd.getOverallMin() == null || qd.getOverallMin().compareTo(min) >= 0;
                            staysCandidate &= qd.getOverallMax() == null || qd.getOverallMax().compareTo(max) <= 0;
                        }
                        if (!staysCandidate){
                            break;
                        }
                    }
                    if (!staysCandidate){
                        it.remove();
                    }
                }
            }
        }
        if(config.isUseDependencies() && !dependendFeatures.isEmpty() && !candidates.isEmpty()){
            Set<KeyTaxon> dependetCandidates = getAllBranchesTaxa(dependendFeatures, taxaCovered, featureStatesFilter);
            candidates.retainAll(dependetCandidates);
        }
        return candidates;
    }

    /**
     * Creates a leaf. It adds the taxa the parent taxon as linked taxa. Handles a
     * list of multiple taxa and handles "specimen taxa" (not yet fully implemented)
     * @return taxa which exist in ALL sub-branches and therefore can be linked on higher level
     */
	private Set<KeyTaxon> handleLeaf(PolytomousKeyNode parent, Set<KeyTaxon> taxaCovered) {
        KeyStatement parentStatement = parent.getStatement();
        for(KeyTaxon taxon: taxaCovered){
            if  (taxon.taxon != null){
                parent.setOrAddTaxon(taxon.taxon);
            }else{
                //FIXME handle other descriptions like specimen descriptions better
                if (parentStatement!=null){
                    String statementString = parentStatement.getLabelText(Language.DEFAULT());
                    if (statementString !=null && taxon.specimen != null){
                        String label = statementString + " --> " + taxon.specimen.getTitleCache();
                        parentStatement.putLabel(Language.DEFAULT(), label);
                    }
                }
            }
        }
        return taxaCovered;
    }

    /**
     * "categorical features" may present several different states/branches,
     * each one of these might correspond to one child.
     */
    private void handleCategorialFeature(PolytomousKeyNode parent, List<Feature> featuresLeft,
            Set<KeyTaxon> taxaCovered,
            Feature winnerFeature,
            Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter) {

        Map<Set<KeyTaxon>,Boolean> reuseWinner = new HashMap<>();

        Set<DefinedTermBase<?>> allStates = getAllStates(winnerFeature, taxaCovered, featureStatesFilter.get(winnerFeature));
		// a map is created, the key being the set of taxa that present the state(s) stored in the corresponding value
        // this key represents a single branch in the decision tree
		Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> taxonStatesMap
		        = determineCategoricalStates(allStates, winnerFeature, taxaCovered, featureStatesFilter.get(winnerFeature));

		if (taxonStatesMap.size()<=1){
		    if (notEmpty(featureDependencies.get(winnerFeature))){
		        //TODO is empty list correctly handled here? Seems to happen if incorrect data (e.g. only Max values) for quantdata exist
		        List<DefinedTermBase<?>> stateList = taxonStatesMap.isEmpty()? new ArrayList<>(): taxonStatesMap.values().iterator().next();
		        Set<Feature> featuresAdded = new HashSet<>();
		        addDependentFeatures(featuresLeft, winnerFeature, featuresAdded, stateList);
		        featuresLeft.remove(winnerFeature);
		        buildBranches(parent, featuresLeft, taxaCovered, featureStatesFilter);
		        removeAddedDependendFeatures(featuresLeft, featuresAdded);
		    }else{
		        //if only 1 branch is left we can handle this as a leaf, no matter how many taxa are left
		        handleLeaf(parent, taxaCovered);
		    }
		}else {
		    // if the merge option is ON, branches with the same discriminative power will be merged (see Vignes & Lebbes, 1989)
		    if (config.isMerge()){
		        taxonStatesMap = handleMerge(taxaCovered, winnerFeature, reuseWinner,
		                taxonStatesMap, allStates, featureStatesFilter.get(winnerFeature));
		    }
		    List<Set<KeyTaxon>> sortedKeys = sortKeys(taxonStatesMap);
            for (Set<KeyTaxon> newTaxaCovered : sortedKeys){
		        //handle each branch
                handleCategoricalBranch(parent, featuresLeft,
                        taxaCovered.size(), winnerFeature, reuseWinner, taxonStatesMap, newTaxaCovered, featureStatesFilter);
            }
		}
		return;
    }

    private Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> handleMerge(Set<KeyTaxon> taxaCovered,
            Feature winnerFeature, Map<Set<KeyTaxon>, Boolean> reuseWinner,
            Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> taxonStatesMap, Set<DefinedTermBase<?>> allStates, Set<DefinedTermBase<?>> filter) {

        // creates a map between the different states of the winnerFeature and the sets of states "incompatible" with them
        Map<DefinedTermBase<?>,Set<DefinedTermBase<?>>> exclusions = new HashMap<>();
        computeExclusions(winnerFeature, taxaCovered, exclusions, filter);

        while (!exclusions.isEmpty()){
        	// looks for the largest clique, i.e. the state with less exclusions
        	List<DefinedTermBase<?>> clique = returnBestClique(exclusions);
        	if(clique.containsAll(allStates)){
        	    continue;
        	}
        	// then merges the corresponding branches
        	mergeBranches(clique, taxonStatesMap, reuseWinner, filter);
        }
        //during merge the keySet (set of taxa) may change, therefore they change their hashcode
        //and can not be used as keys in the map anymore.
        //Therefore we refill the map.
        taxonStatesMap = renewTaxonStatesMap(taxonStatesMap);
        return taxonStatesMap;
    }

    private void handleCategoricalBranch(PolytomousKeyNode parent, List<Feature> featuresLeft,
            int taxaCoveredSize,
            Feature winnerFeature, Map<Set<KeyTaxon>, Boolean> reuseWinner,
            Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> taxonStatesMap,
            Set<KeyTaxon> newTaxaCovered,
            Map<Feature,Set<DefinedTermBase<?>>> featureStatesFilter) {

        //to restore old state
        Set<DefinedTermBase<?>> oldFilterSet = featureStatesFilter.get(winnerFeature);
        Set<Feature> featuresAdded = new HashSet<>();

        boolean areTheTaxaDiscriminated = false;
        PolytomousKeyNode childNode = PolytomousKeyNode.NewInstance();
        parent.addChild(childNode);

        List<DefinedTermBase<?>> listOfStates = taxonStatesMap.get(newTaxaCovered);
        if ((newTaxaCovered.size() > 0)){ //old: if the taxa are discriminated compared to those of the parent node, a child is created
        	areTheTaxaDiscriminated = (newTaxaCovered.size() != taxaCoveredSize);

        	int numberOfStates = listOfStates.size()-1;
        	listOfStates.sort(stateComparator);

        	if (config.isUseDependencies()){
        	    // old: if the dependencies are considered, removes and adds the right features from/to the list of features left
                // these features are stored in order to be put back again when the current branch is finished

        	    addDependentFeatures(featuresLeft, winnerFeature, featuresAdded, listOfStates);
        	}

        	String statementLabel = createStatement(listOfStates, numberOfStates);
            KeyStatement statement = KeyStatement.NewInstance(statementLabel);
        	childNode.setStatement(statement);
        	parent.setFeature(winnerFeature);

        	if (reuseWinner.get(newTaxaCovered)== Boolean.TRUE){
        	    featuresLeft.add(winnerFeature);
        	    setStatesFilter(featureStatesFilter, winnerFeature, listOfStates);
        	}else{
        	    featuresLeft.remove(winnerFeature);
        	}
        }

        boolean hasChildren = areTheTaxaDiscriminated && (newTaxaCovered.size() > 1);
        if (hasChildren){
            buildBranches(childNode, featuresLeft, newTaxaCovered, featureStatesFilter);
        }else{
            handleLeaf(childNode, newTaxaCovered);
            Set<KeyTaxon> taxonChildren = getTaxonChildren(newTaxaCovered);
            if(!taxonChildren.isEmpty()){
                //TODO FIXME featuresLeft probably needs to include all features, similar for featureStatesFilter
                buildBranches(childNode, featuresLeft, taxonChildren, featureStatesFilter);
            }
        }

        //restore old state before returning to parent node
        removeAddedDependendFeatures(featuresLeft, featuresAdded);
        featureStatesFilter.put(winnerFeature, oldFilterSet);

        return;
    }

    private Set<KeyTaxon> getTaxonChildren(Set<KeyTaxon> newTaxaCovered) {
        Set<KeyTaxon> result = new HashSet<>();
        for (KeyTaxon taxon:newTaxaCovered){
            result.addAll(taxon.children);
        }
        return result;
    }

    private void setStatesFilter(Map<Feature, Set<DefinedTermBase<?>>> filter, Feature feature,
            List<DefinedTermBase<?>> listOfStates) {
        if (filter.get(feature)==null){
            filter.put(feature, new HashSet<>(listOfStates));
        }else{
            Set<DefinedTermBase<?>> set = filter.get(feature);
            set.retainAll(listOfStates);
        }
    }

    private void removeAddedDependendFeatures(List<Feature> featuresLeft, Set<Feature> featuresAdded) {
        for (Feature addedFeature : featuresAdded) {
            featuresLeft.remove(addedFeature);
        }
    }

    private void addDependentFeatures(List<Feature> featuresLeft, Feature baseFeature,
            Set<Feature> featuresAdded, List<DefinedTermBase<?>> listOfStates) {

        if(notEmpty(featureDependencies.get(baseFeature))){
            Set<Feature> newFeatureCandidates = new HashSet<>(featureDependencies.get(baseFeature));
            newFeatureCandidates.remove(null);
            for (DefinedTermBase<?> state : listOfStates) {
                //in-applicable
                List<Feature> inapplicableFeatures = getApplicableFeatures(baseFeature, state, iAifDependencies);
                newFeatureCandidates.removeAll(inapplicableFeatures);
                //only-applicable
                List<Feature> onlyApplicableFeatures = getApplicableFeatures(baseFeature, state, oAifDependencies);
                if (!onlyApplicableFeatures.isEmpty()){
                    Iterator<Feature> it = newFeatureCandidates.iterator();
                    while (it.hasNext()){
                        Feature featureCandidate = it.next();
                        if (!onlyApplicableFeatures.contains(featureCandidate)){
                            it.remove();
                        }
                    }
                }
            }
            featuresLeft.addAll(newFeatureCandidates);
            featuresAdded.addAll(newFeatureCandidates);
        }
    }

    private List<Feature> getApplicableFeatures(Feature feature, DefinedTermBase<?> state,
            Map<FeatureState, Set<Feature>> applicabilityDependencies) {
        List<Feature> result = new ArrayList<>();
        for (FeatureState featureState : applicabilityDependencies.keySet()){
            if(featureState.getFeature().equals(feature) && featureState.getState().equals(state)){
                result.addAll(applicabilityDependencies.get(featureState));
            }
        }
        return result;
    }

    private boolean notEmpty(Set<?> set) {
        return (set != null) && !set.isEmpty();
    }

    private String createStatement(List<DefinedTermBase<?>> listOfStates, int numberOfStates) {
        StringBuilder statementLabel = new StringBuilder();
        for (DefinedTermBase<?> state : listOfStates) {
            statementLabel.append(state.getLabel());
            if (listOfStates.lastIndexOf(state)!=numberOfStates){
                statementLabel.append(separator); // append a separator after each state except for the last one
            }
        }
        return statementLabel.toString();
    }

    private Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> renewTaxonStatesMap(Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> taxonStatesMap) {
        Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> result = new HashMap<>();
        for (Map.Entry<Set<KeyTaxon>, List<DefinedTermBase<?>>> entry : taxonStatesMap.entrySet()){
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private List<Set<KeyTaxon>> sortKeys(Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> taxonStatesMap) {
        //for now this is a dummy sorting
        List<Map.Entry<Set<KeyTaxon>, List<DefinedTermBase<?>>>> sortedEntries = new ArrayList<>();
        sortedEntries.addAll(taxonStatesMap.entrySet());

        sortedEntries.sort(entryComparator);
        List<Set<KeyTaxon>> result = new ArrayList<>();
        for (Map.Entry<Set<KeyTaxon>, List<DefinedTermBase<?>>> entry : sortedEntries){
            result.add(entry.getKey());
        }
        return result;
    }

    //TODO use a general term comparator here
    private static final Comparator<DefinedTermBase<?>> stateComparator = (a,b)-> {

        //TODO use real Term Comparator
        if (!a.getTitleCache().equals(b.getTitleCache())){
            return a.getTitleCache().compareTo(b.getTitleCache());
        }
        if (a.getUuid()!= b.getUuid()){
            return a.getUuid().compareTo(b.getUuid());
        }
        return 0;
    };

    private static final Comparator<? super Entry<Set<KeyTaxon>, List<DefinedTermBase<?>>>> entryComparator =  (a,b)-> {
        if (a.getKey().size()!=b.getKey().size()){
            //order by number of taxa covered
            return b.getKey().size() - a.getKey().size();
        }else if (a.getValue().size()!= b.getValue().size()){
            //order by number of states covered
            return b.getValue().size() - a.getValue().size();
        }else{
            //order states alphabetically or by uuid
            for (int i = 0; i < a.getValue().size(); i++){
                DefinedTermBase<?> stateA = a.getValue().get(i);
                DefinedTermBase<?> stateB = b.getValue().get(i);
                int result = stateComparator.compare(stateA, stateB);
                if (result != 0){
                    return result;
                }
            }
            //TODO compare keys (sets of KeyTaxon)
//            for (int i = 0; i < a.getKey().size(); i++){
//                Object stateA = a.getKey().getUuid;
//                State stateB = a.getKey().get(i);
//                //TODO use real Term Comparator
//                if (stateA.getUuid()!= stateB.getUuid()){
//                    return stateA.getUuid().compareTo(stateB.getUuid());
//                }
//            }
            throw new RuntimeException("Compare for same state lists with different unit descriptions not yet implemented");
        }
    };

    private Set<DefinedTermBase<?>> getAllStates(Feature feature, Set<KeyTaxon> taxaCovered, Set<DefinedTermBase<?>> filter) {
        //TODO handle modifier
        Set<DefinedTermBase<?>> states = new HashSet<>();
        for (KeyTaxon taxon : taxaCovered){
            Set<CategoricalData> cdSet = taxon.getCategoricalData(feature);
            for (CategoricalData cd : cdSet){
                List<StateData> stateDatas = cd.getStateData();
                for (StateData sd : stateDatas){
                    DefinedTermBase<?> state = sd.getState();
                    if (filter != null && !filter.contains(state)) {
                        continue;
                    }
                    states.add(state);
                }
            }
        }
        return states;
    }

    private void handleQuantitativeData(PolytomousKeyNode parent, List<Feature> featuresLeft,
            Set<KeyTaxon> taxaCovered, Map<Feature, BigDecimal> quantitativeFeaturesThresholds,
            Feature winnerFeature, Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter) {

        // first, get the threshold
        BigDecimal threshold = quantitativeFeaturesThresholds.get(winnerFeature);
        //TODO unit not seems to be used yet
        StringBuilder unit = new StringBuilder();
        // then determine which taxa are before and which are after this threshold (dichotomy)
        //in order to create the children of the parent node
        List<Set<KeyTaxon>> quantitativeStates = determineQuantitativeStates(threshold, winnerFeature, taxaCovered, unit);
        // thus the list contains two sets of KeyTaxon, the first corresponding to
        // those before, the second to those after the threshold
        for (int i=0; i<2; i++) {
            handleQuantitativeBranch(parent, featuresLeft, taxaCovered.size(), winnerFeature, threshold, unit,
                    quantitativeStates, featureStatesFilter, i);
        }

        return;
    }

    /**
     * Creates the branch for a quantitative feature.
     * TODO if the quantitative feature has dependent features they are not yet handled
     */
    private void handleQuantitativeBranch(PolytomousKeyNode parent, List<Feature> featuresLeft,
            int parentTaxaCoveredSize, Feature winnerFeature, BigDecimal threshold, StringBuilder unit,
            List<Set<KeyTaxon>> quantitativeStates, Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter,
            int brunchNum) {

        String sign;
        Set<KeyTaxon> newTaxaCovered = quantitativeStates.get(brunchNum);
        if (brunchNum==0){
        	sign = before; // the first element of the list corresponds to taxa before the threshold
        } else {
        	sign = after; // the second to those after
        }
        if (newTaxaCovered.size()>0){
        	PolytomousKeyNode childNode = PolytomousKeyNode.NewInstance();
        	parent.setFeature(winnerFeature);
        	KeyStatement statement = KeyStatement.NewInstance( " " + sign + " " + threshold + unit);
        	childNode.setStatement(statement);
        	parent.addChild(childNode);
        	//TODO don't we need to check dependent features, they are not very likely for quantitative features, but still might exist as exception ...
        	boolean taxaAreDiscriminatedInThisStep = newTaxaCovered.size() < parentTaxaCoveredSize;
        	boolean childrenExist = taxaAreDiscriminatedInThisStep && (newTaxaCovered.size() > 1);
        	if (childrenExist){
        	    buildBranches(childNode, featuresLeft, newTaxaCovered, featureStatesFilter);
        	}else{
        	    handleLeaf(childNode, newTaxaCovered);
        	}
        }else{
            //TODO do we need to check the 0 case, can this happen at all, shouldn't we throw a warning instead?
            throw new RuntimeException("No taxa left on branch. This should probably not happen.");
        }
        return;
    }

    private Feature computeScores(List<Feature> featuresLeft, Set<KeyTaxon> taxaCovered,
            Map<Feature,BigDecimal> quantitativeFeaturesThresholds, Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter) {

        Map<Feature,Float> scoreMap = featureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds, featureStatesFilter);
        dependenciesScores(scoreMap, featuresLeft, taxaCovered, quantitativeFeaturesThresholds, featureStatesFilter);
        // the feature with the best score becomes the one corresponding to the current node
        Feature winnerFeature = lessStatesWinner(scoreMap, taxaCovered);
        // the feature is removed from the list of features available to build the next level of the tree
        featuresLeft.remove(winnerFeature);
        if (config.isDebug()){System.out.println("   ScoreMap: " + scoreMap);}
        if (config.isDebug()){System.out.println("   quantitativeThreshold: " + quantitativeFeaturesThresholds);}
        if (config.isDebug()){System.out.println("   winner: " + winnerFeature);}
        return winnerFeature;
    }

	/**
	 * Dependencies can be seen as a hierarchy.
	 * If the dependencies are on, this function calculates the score of all children features and give the highest score
	 * of these to their parent (of course, if its score is lower). This way, if a feature has a good score but is
	 * "onlyApplicableIf" or "InapplicableIf", the feature it depends can be chosen in order to build a better key.
	 */
	private void dependenciesScores(Map<Feature,Float> scoreMap, List<Feature> featuresLeft,
	        Set<KeyTaxon> coveredTaxa, Map<Feature,BigDecimal> quantitativeFeaturesThresholds, Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter){

	    //TODO maybe we need to do this recursive?

	    //old: but I don't understand why we should include the existing featureList, this is only necessary
	    //if the single cores depend on the set of features
	    //List<Feature> pseudoFeaturesLeft = new ArrayList<>(featuresLeft);  //copies the list of features

	    // then adds all the features which depend on features contained in the list
		List<Feature> pseudoFeatures = new ArrayList<>();
		for (Feature feature : featuresLeft){
			if (featureDependencies.containsKey(feature)){
				for (Feature dependendFeature : featureDependencies.get(feature)){
				    if (!pseudoFeatures.contains(dependendFeature)){
				        pseudoFeatures.add(dependendFeature);
				    }
				}
			}
		}

		if (!pseudoFeatures.isEmpty()){
    		// then calculates the scores of all features that have been added
    		Map<Feature,Float> newScoreMap = featureScores(pseudoFeatures, coveredTaxa, quantitativeFeaturesThresholds, featureStatesFilter);
    		for (Feature parentFeature : featureDependencies.keySet()){
    			if (scoreMap.containsKey(parentFeature)){
    				for (Feature dependendFeature : featureDependencies.get(parentFeature)){
    					if (newScoreMap.containsKey(dependendFeature)) {
    						// if a feature has a better than the "parent" feature it depends on
    						if (newScoreMap.get(dependendFeature) > scoreMap.get(parentFeature)){
    							// the "parent" feature gets its score in the original score map
    							scoreMap.put(parentFeature, newScoreMap.get(dependendFeature));
    						}
    					}
    				}
    			}
    		}
		}
	}


	/**
	 * This function merges branches of the key belonging to the same clique
	 * (http://en.wikipedia.org/wiki/Clique_%28graph_theory%29)
	 *
	 * @param clique the list of States linked together (i.e. if merged have the same score)
	 * @param taxonStatesMap the map between the taxa (keys) and the states (keys) leading to them
	 * @param reuseWinner
	 * @param filter
	 * @return <code>true</code>, if all taxa covered by the new branch include all states of the clique.
	 * <code>false</code> otherwise.
	 */
	private void mergeBranches(List<DefinedTermBase<?>> clique, Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> taxonStatesMap,
	        Map<Set<KeyTaxon>, Boolean> reuseWinner, Set<DefinedTermBase<?>> filter){

	    boolean isExact = true;
	    if (clique.size()<=1){
	        return;
	    }
	    Map.Entry<Set<KeyTaxon>,List<DefinedTermBase<?>>> firstBranch = null;
	    List<Set<KeyTaxon>> tdToDelete = new ArrayList<>();
	    //TODO is the stateFilter needed here somehow?
	    for (Map.Entry<Set<KeyTaxon>, List<DefinedTermBase<?>>> branch : taxonStatesMap.entrySet()){
		    boolean stateFound = false;
			// looks for one state of the clique in this branch
			for(DefinedTermBase<?> state : clique){
			    if (branch.getValue().contains(state)) {
                    stateFound = true;
                    break;
                }
			}
			// if one state is found...
			if (stateFound == true){
				// ...for the first time, the branch becomes the one to which the others will be merged
				if (firstBranch == null){
					firstBranch = branch;
				}
				// ... else the branch is merged to the first one.
				else {
					int oldSize = firstBranch.getKey().size();
				    firstBranch.getKey().addAll(branch.getKey());
				    int newSize = firstBranch.getKey().size();
                    if (oldSize != newSize || newSize != branch.getKey().size()){
                        isExact = false;
                    }
				    firstBranch.getValue().addAll(branch.getValue());
					tdToDelete.add(branch.getKey());
				}
			}
		}
		// once this is done, the branches merged to the first one are deleted
		for (Set<KeyTaxon> td : tdToDelete){
			taxonStatesMap.remove(td);
		}
		if (!isExact && firstBranch != null){
		    reuseWinner.put(firstBranch.getKey(), Boolean.TRUE);
		}
	}

	/**
	 * This function looks for the largest clique of States
	 * (http://en.wikipedia.org/wiki/Clique_%28graph_theory%29)
	 * from the map of exclusions.
	 *
	 * @param exclusions map a state (key) to the set of states (value) which can not be merged with it without decreasing its score.
	 * @return
	 */
	private List<DefinedTermBase<?>> returnBestClique (Map<DefinedTermBase<?>,Set<DefinedTermBase<?>>> exclusions){
		int bestNumberOfExclusions=-1;
		int numberOfExclusions;
		List<DefinedTermBase<?>> clique = new ArrayList<>();

		// looks for the largest clique, i.e. the state with less exclusions
		DefinedTermBase<?> bestState=null;
		for (Map.Entry<DefinedTermBase<?>,Set<DefinedTermBase<?>>> pair :  exclusions.entrySet()){
			numberOfExclusions = pair.getValue().size();
			if ((bestNumberOfExclusions == -1) || numberOfExclusions < bestNumberOfExclusions) {
				bestNumberOfExclusions = numberOfExclusions;
				bestState = pair.getKey();
			}
		}

		clique.add(bestState);
		exclusions.remove(bestState);

		boolean isNotExcluded;
		// then starts building the clique by adding the states which do not exclude each other
		for (Map.Entry<DefinedTermBase<?>,Set<DefinedTermBase<?>>> pair : exclusions.entrySet()){
			isNotExcluded = true;
			for (DefinedTermBase<?> state : clique) {
				if (pair.getValue().contains(state)) {
                    isNotExcluded = false;
                }
			}
			if (isNotExcluded){
				clique.add(pair.getKey());
			}
		}
		for (DefinedTermBase<?> state : clique) {
			exclusions.remove(state);
		}
		return clique;
	}


	/**
	 * fills a map of the sets of taxa (key) presenting the different states (value) for the given feature.
	 *
	 * @param statesDone the list of states already done for this feature
	 * @param states2 the element from which the states are extracted
	 * @param feature the feature corresponding to the CategoricalData
	 * @param taxaCovered the base of taxa considered
	 * @param featureStatesFilter
	 * @return
	 */
	private Map<Set<KeyTaxon>,List<DefinedTermBase<?>>> determineCategoricalStates(
	        Set<DefinedTermBase<?>> states, Feature feature, Set<KeyTaxon> taxaCovered, Set<DefinedTermBase<?>> filter){

	    Map<Set<KeyTaxon>, List<DefinedTermBase<?>>> childrenStatesMap = new HashMap<>();
	    //TODO needed
	    List<DefinedTermBase<?>> statesDone = new ArrayList<>(); // the list of states already used

		for (DefinedTermBase<?> state : states){ // for each state
			if (filter != null && !filter.contains(state)){
			    continue;
			}
		    statesDone.add(state);
			Set<KeyTaxon> newTaxaCovered = taxaByFeatureState(feature, state, taxaCovered); //gets which taxa present this state
			List<DefinedTermBase<?>> statesOfTaxa = childrenStatesMap.get(newTaxaCovered);
			if (statesOfTaxa == null) { // if no states are associated to these taxa, create a new list
				statesOfTaxa = new ArrayList<>();
				childrenStatesMap.put(newTaxaCovered, statesOfTaxa);
			}
			statesOfTaxa.add(state); // then add the state to the list
		}
		return childrenStatesMap;
	}


	/**
	 * Returns the list of taxa from previously covered taxa, which have the state featureState for the given feature
	 */
	private Set<KeyTaxon> taxaByFeatureState(Feature feature, DefinedTermBase<?> featureState, Set<KeyTaxon> taxaCovered){
		Set<KeyTaxon> newCoveredTaxa = new HashSet<>();
		for (KeyTaxon td : taxaCovered){
			for (CategoricalData cd : td.getCategoricalData(feature)){
			    List<StateData> stateDatas = cd.getStateData();
                for (StateData sd : stateDatas) {
                    if (sd.getState().equals(featureState)){
                        newCoveredTaxa.add(td);
                    }
                }
			}
		}
		return newCoveredTaxa;
	}

	/**
	 * This function returns the feature with the highest score. However, if several features have the same score
	 * the one which leads to less options is chosen (this way, the key is easier to read).
	 */
	private Feature lessStatesWinner(Map<Feature,Float> scores, Set<KeyTaxon> taxaCovered){
		int nTaxa = taxaCovered.size();
		if (nTaxa==1) {
            return null;
        }
		float meanScore = defaultMeanScore(nTaxa);
		float bestScore = nTaxa*nTaxa;
		List<Feature> bestFeatures = new ArrayList<>(); // if ever different features have the best score, they are put in this list
		Feature bestFeature = null;
		float newScore;
		for (Map.Entry<Feature,Float> pair : scores.entrySet()){
			if (pair.getValue()!=null){
				newScore = Math.abs(pair.getValue()-meanScore);// the best score is the closest to the score (meanScore here)
				// a feature would have if it divided the taxa in two equal parts
				if (newScore < bestScore){
					bestFeatures.clear();
					bestFeatures.add(pair.getKey());
					bestScore = newScore;
				}else if (newScore==bestScore){
					bestFeatures.add(pair.getKey());
				}
			}
		}
		if (bestFeatures.size()==1) { // if there is only one feature with the best score, pick it
			return bestFeatures.get(0);
		}
		else { // else choose the one with less states
			int lessStates = -1;
			int numberOfDifferentStates=-1;
			for (Feature feature : bestFeatures){
				if (feature.isSupportsCategoricalData()){
					Set<DefinedTermBase<?>> differentStates = new HashSet<>();
					for (KeyTaxon taxon : taxaCovered){
						Set<CategoricalData> cds = taxon.getCategoricalData(feature);
						Set<StateData> allStateData = getStateData(cds);
						for (StateData sd : allStateData) {
							differentStates.add(sd.getState());
						}
					}
					numberOfDifferentStates=differentStates.size();
				}else if (feature.isSupportsQuantitativeData()){
					numberOfDifferentStates=2;
				}
				if (lessStates == -1 || numberOfDifferentStates < lessStates){
					lessStates = numberOfDifferentStates;
					bestFeature = feature;
				}
			}
			return bestFeature;
		}
	}

	/**
	 * This function calculates the mean score, i.e. the score a feature dividing the taxa in two equal parts
	 * would have.
	 */
	private float defaultMeanScore(int nTaxa){
		int i;
		float score=0;
		for (i=1;i<nTaxa;i++){
			score = score + Math.round(i+1/2);
		}
		return score;
	}

	/**
	 * This function fills the map of features (keys) with their respecting scores (values)
	 */
	private Map<Feature,Float> featureScores(List<Feature> featuresLeft, Set<KeyTaxon> coveredTaxa,
	        Map<Feature,BigDecimal> quantitativeFeaturesThresholds, Map<Feature, Set<DefinedTermBase<?>>> featureStatesFilter){

		Map<Feature,Float> scoreMap = new HashMap<>();
		for (Feature feature : featuresLeft){
			if (feature.isSupportsCategoricalData()) {
				scoreMap.put(feature, categoricalFeatureScore(feature, coveredTaxa, featureStatesFilter.get(feature)));
			}
			if (feature.isSupportsQuantitativeData()){
				scoreMap.put(feature, quantitativeFeatureScore(feature, coveredTaxa, quantitativeFeaturesThresholds));
			}
		}
		return scoreMap;
	}

	/**
	 * Since Quantitative features do not have states, unlike Categorical ones, this function determines which taxa,
	 * for a given quantitative feature, present either a lower or higher value than a given threshold.
	 * It returns two Sets of {@link KeyTaxon}, one with the taxa under this threshold (taxaBefore) and another one
	 * with the taxa over (taxaAfter).
	 */
	private List<Set<KeyTaxon>> determineQuantitativeStates (BigDecimal threshold, Feature feature,
	        Set<KeyTaxon> taxa, StringBuilder unit){

	    List<Set<KeyTaxon>> list = new ArrayList<>();
		Set<KeyTaxon> taxaBefore = new HashSet<>();
		Set<KeyTaxon> taxaAfter = new HashSet<>();
		list.add(taxaBefore);
		list.add(taxaAfter);
		for (KeyTaxon td : taxa){
		    for (QuantitativeData qd : td.getQuantitativeData(feature)){
		        if (unit.toString().equals("") && qd.getUnit()!=null && qd.getUnit().getLabel()!=null){
                    unit.append(" " + qd.getUnit().getLabel());
                }
                Set<StatisticalMeasurementValue> values = qd.getStatisticalValues();
                for (StatisticalMeasurementValue smv : values){
                    StatisticalMeasure type = smv.getType();
                    //TODO DONT FORGET sample size, MEAN etc
                    if (type.isMax() || type.isTypicalUpperBoundary() || type.isAverage() || type.isExactValue()) {
                        if (smv.getValue().compareTo(threshold) > 0){
                            taxaAfter.add(td);
                        }
                    }
                    if (type.isMin() || type.isTypicalLowerBoundary() || type.isAverage() || type.isExactValue()) {
                        if (smv.getValue().compareTo(threshold) <= 0){
                            taxaBefore.add(td);
                        }
                    }
                }
		    }
		}
		return list;
	}

	/**
	 * This function returns the score of a quantitative feature.
	 */
	private float quantitativeFeatureScore(Feature feature, Set<KeyTaxon> coveredTaxa,
	        Map<Feature,BigDecimal> quantitativeFeaturesThresholds){

	    List<BigDecimal> allValues = new ArrayList<>();
		for (KeyTaxon td : coveredTaxa){
		    for (QuantitativeData qd : td.getQuantitativeData(feature)){
		        computeAllValues(allValues, qd);
		    }
		}
		int i,j;
		BigDecimal threshold = BigDecimal.ZERO;
		BigDecimal bestThreshold = BigDecimal.ZERO;
		int difference = allValues.size();
		int differenceMin = difference;
		int bestTaxaBefore = 0;
		int bestTaxaAfter = 0;
		for (i=0; i<allValues.size()/2; i++) {
			threshold = allValues.get(i*2+1);
			int taxaBefore=0;
			int taxaAfter=0;
			for (j=0;j<allValues.size()/2;j++) {
				if (allValues.get(j*2+1).compareTo(threshold) <= 0){
					taxaBefore++;
				}
				if (allValues.get(j*2).compareTo(threshold) > 0){
					taxaAfter++;
				}
			}
			difference = Math.abs(taxaBefore-taxaAfter);
			if (difference < differenceMin){
				differenceMin = difference;
				bestThreshold = threshold;
				bestTaxaBefore = taxaBefore;
				bestTaxaAfter = taxaAfter;
			}
		}
		quantitativeFeaturesThresholds.put(feature, bestThreshold);
		int defaultQuantitativeScore=0;
		for (i=0; i<bestTaxaBefore; i++) {
			defaultQuantitativeScore += bestTaxaAfter;
		}
		return defaultQuantitativeScore;
	}

    private void computeAllValues(List<BigDecimal> allValues, QuantitativeData qd) {
        Set<StatisticalMeasurementValue> values = qd.getStatisticalValues();
        BigDecimal lowerboundary = null;
        BigDecimal upperboundary = null;
        for (StatisticalMeasurementValue smv : values){
        	StatisticalMeasure type = smv.getType();
        	BigDecimal value = smv.getValue();
        	// DONT FORGET sample size, MEAN etc
        	if(type.isMin() || type.isTypicalLowerBoundary() || type.isAverage() || type.isExactValue()){
        	    if (lowerboundary == null){
        	        lowerboundary = value;
        	    }else{
        	        lowerboundary = lowerboundary.min(value);
        	    }
        	}
        	if(type.isMax() || type.isTypicalUpperBoundary() || type.isAverage() || type.isExactValue()){
                if (upperboundary == null){
                    upperboundary = value;
                }else{
                    upperboundary = upperboundary.max(value);
                }
            }

//        	if (type.isMax()) {
//        		upperboundary = smv.getValue();
//        		upperboundarypresent=true;
//        	}else if (type.equals(StatisticalMeasure.MIN())) {
//        		lowerboundary = smv.getValue();
//        		lowerboundarypresent=true;
//        	}else if (type.equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY()) && upperboundarypresent==false) {
//        		upperboundary = smv.getValue();
//        		upperboundarypresent=true;
//        	}else if (type.equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY()) && lowerboundarypresent==false) {
//        		lowerboundary = smv.getValue();
//        		lowerboundarypresent=true;
//        	}else if (type.equals(StatisticalMeasure.AVERAGE()) && upperboundarypresent==false && lowerboundarypresent==false) {
//        		lowerboundary = smv.getValue();
//        		upperboundary = lowerboundary;
//        		lowerboundarypresent=true;
//        		upperboundarypresent=true;
//        	}
        }
        if (lowerboundary != null && upperboundary != null) {
        	allValues.add(lowerboundary);
        	allValues.add(upperboundary);
        }else if(lowerboundary != null || upperboundary != null){
            logger.warn("Only one of upper or lower boundary is defined. Statistical measurement value not used.");
        }
    }

	/**
	 * This function returns the score of a categorical feature
	 * by comparing each taxon with each other. If the feature
	 * discriminates a single pair of taxa the score is increased.
	 */
	private float categoricalFeatureScore(Feature feature, Set<KeyTaxon> coveredTaxa, Set<DefinedTermBase<?>> filter){
		int i,j;
		float score =0;
		float power=0;
		KeyTaxon[] coveredTaxaArray = coveredTaxa.toArray(new KeyTaxon[coveredTaxa.size()]); // I did not figure a better way to do this
		for (i=0 ; i<coveredTaxaArray.length; i++){
			Set<CategoricalData> cd1 = coveredTaxaArray[i].getCategoricalData(feature);
			for (j=i+1 ; j< coveredTaxaArray.length ; j++){
			    Set<CategoricalData> cd2 = coveredTaxaArray[j].getCategoricalData(feature);
				power = defaultCategoricalPower(cd1, cd2, filter);
				score = score + power;
			}
		}
		return score;
	}

	/**
	 * This recursive function fills the maps of dependencies by reading the tree containing the dependencies.
	 */
	private void createDependencies(TermNode<Feature> node){

	    //the featureDependencies handling was originally in defaultCategoricalPower(cat, cat)
	    //needs to be checked if it is OK to handle them here
        if (node.isDependent()){
            Feature parentFeature = node.getParent().getTerm();
            if (!featureDependencies.containsKey(parentFeature)){
                featureDependencies.put(parentFeature, new HashSet<>());
            }
            for (FeatureState featureState : node.getOnlyApplicableIf()){
                if (!oAifDependencies.containsKey(featureState)) {
                    oAifDependencies.put(featureState, new HashSet<>());
                }
                oAifDependencies.get(featureState).add(node.getTerm());
                //TODO: we only guess that it is the state of the parent feature here
                //needs to be improved
                featureDependencies.get(node.getParent().getTerm()).add(node.getTerm());
            }
            for (FeatureState featureState : node.getInapplicableIf()){
                if (!iAifDependencies.containsKey(featureState)) {
                    iAifDependencies.put(featureState, new HashSet<>());
                }
                iAifDependencies.get(featureState).add(node.getTerm());
                //TODO: we only guess that it is the state of the parent feature here
                //needs to be improved
                featureDependencies.get(node.getParent().getTerm()).add(node.getTerm());
            }
        }

		for (TermNode<Feature> fn : node.getChildNodes()){
			createDependencies(fn);
		}
//		System.out.println(featureDependencies);
	}

	/**
	 * This function fills the exclusions map.
	 */
	private float computeExclusions(Feature feature, Set<KeyTaxon> coveredTaxa, Map<DefinedTermBase<?>,Set<DefinedTermBase<?>>> exclusions, Set<DefinedTermBase<?>> filter){
		//unclear what the score is fore here
		float score =0;
		float power=0;
		KeyTaxon[] fixedOrderTaxa = coveredTaxa.toArray(new KeyTaxon[coveredTaxa.size()]); // I did not figure a better way to do this
		for (int i=0 ; i<fixedOrderTaxa.length; i++){
		    Set<CategoricalData> cd1 = fixedOrderTaxa[i].getCategoricalData(feature);

			for (int j=i+1 ; j< fixedOrderTaxa.length ; j++){
				Set<CategoricalData> cd2 = fixedOrderTaxa[j].getCategoricalData(feature);

//				System.out.println(deb1 + "; " +deb2);
				power = defaultCategoricalPower(cd1, cd2, filter);
				score = score + power;
				if (power >= 1.0){ // if there is no state in common between deb1 and deb2

					for (StateData statedata1 : getStateData(cd1)){
					    DefinedTermBase<?> state1 = statedata1.getState();
						if (!exclusions.containsKey(state1)){
							exclusions.put(state1, new HashSet<>());
						}
						for (StateData statedata2 : getStateData(cd2)){
						    DefinedTermBase<?> state2 = statedata2.getState();
							if (!exclusions.containsKey(state2)){
								exclusions.put(state2, new HashSet<>());
							}
							exclusions.get(state1).add(state2);
							exclusions.get(state2).add(state1);
						}
					}
				}
			}
		}
		return score;
	}

    private Set<StateData> getStateData(Set<CategoricalData> cds) {
        Set<StateData> result = new HashSet<>();
        for (CategoricalData cd : cds){
            result.addAll(cd.getStateData());
        }
        return result;
    }

    /**
	 * Returns the score of a categorical feature.
	 */
	private float defaultCategoricalPower(Set<CategoricalData> cd1, Set<CategoricalData> cd2, Set<DefinedTermBase<?>> filter){
	    if (cd1 == null || cd2 == null ||cd1.isEmpty() || cd2.isEmpty()){
	        return 0;
	    }

	    //FIXME see defaultCategoricalPower_old for additional code on dependencies
	    //which has been removed here for now but might be important
        //Now I moved it to #createDependencies. Therefore the below is maybe not needed
	    //anymore but superfluent.
	    //But the implementation at createDependencies is not fully correct yet
	    //so I keep it here for now.

	    for (CategoricalData cd : cd1){
	        if (!featureDependencies.containsKey(cd.getFeature())){
	            featureDependencies.put(cd.getFeature(), new HashSet<>());
	        }
	        for (DefinedTermBase<?> state : getStates(cd, filter)){
	            if (iAifDependencies.get(state)!=null) {
	                featureDependencies.get(cd.getFeature()).addAll(iAifDependencies.get(state));
	            }
	            if (oAifDependencies.get(state)!=null) {
	                featureDependencies.get(cd.getFeature()).addAll(oAifDependencies.get(state));
	            }
	        }
	    }

	    //get all states of both categorical data
        Set<DefinedTermBase<?>> states = getStates(cd1, cd2, filter);
        if (states.size() == 0){
            return 0;
        }

	    int nDiscriminative = 0;
	    for (DefinedTermBase<?> state : states){
	        boolean hasState1 = hasState(state, cd1);
	        boolean hasState2 = hasState(state, cd2);
	        //if only 1 has the state than the state is discriminative
	        if (! (hasState1&&hasState2)) {
	            nDiscriminative++;
            }
	    }
	    float result = (float)nDiscriminative/states.size();
	    return result;
	}

    private boolean hasState(DefinedTermBase<?> state, Set<CategoricalData> cds) {
        boolean result = false;
        for (CategoricalData cd : cds){
            for (StateData stateData:cd.getStateData()){
                result |= state.equals(stateData.getState());
            }
        }
        return result;
    }

    private Set<DefinedTermBase<?>> getStates(Set<CategoricalData> cdset1, Set<CategoricalData> cdset2, Set<DefinedTermBase<?>> filter) {
        Set<DefinedTermBase<?>> result = new HashSet<>();
        result.addAll(getStates(cdset1, filter));
        result.addAll(getStates(cdset2, filter));
        return result;
    }

    private Set<DefinedTermBase<?>> getStates(Set<CategoricalData> cdset, Set<DefinedTermBase<?>> filter) {
        Set<DefinedTermBase<?>> result = new HashSet<>();
        for (CategoricalData cd : cdset){
            result.addAll(getStates(cd, filter));
        }
        return result;
    }

    private Set<DefinedTermBase<?>> getStates(CategoricalData cd, Set<DefinedTermBase<?>> filter) {
        //TODO handle modifier
        Set<DefinedTermBase<?>> result = new HashSet<>();
        List<StateData> states = cd.getStateData();
        for (StateData stateData:states){
            DefinedTermBase<?> state = stateData.getState();
            if (filter != null && !filter.contains(state)){
                continue;
            }
            result.add(stateData.getState());
        }
        return result;
    }

    //TODO keep as long as the handling of featureDependencies is not yet checked or handled in
    //the new method defaultCategoricalPower()
    private float defaultCategoricalPower_old(CategoricalData deb1, CategoricalData deb2){
	        List<StateData> states1 = deb1.getStateData();
	        List<StateData> states2 = deb2.getStateData();
	        boolean bool = false;
	        Iterator<StateData> stateData1Iterator = states1.iterator() ;
	        //      while (!bool && stateData1Iterator.hasNext()) {
	        //          Iterator<StateData> stateData2Iterator = states2.iterator() ;
	        //          StateData stateData1 = stateData1Iterator.next();
	        //          while (!bool && stateData2Iterator.hasNext()) {
	        //              bool = stateData1.getState().equals(stateData2Iterator.next().getState()); // checks if the states are the same
	        //          }
	        //      }
	        // one point each time two taxa can be discriminated for a given feature

	        boolean checkFeature = false;

	        if (!featureDependencies.containsKey(deb1.getFeature())){
	            featureDependencies.put(deb1.getFeature(), new HashSet<>());
	            checkFeature = true;
	        }

	        while (stateData1Iterator.hasNext()) {
	            Iterator<StateData> stateData2Iterator = states2.iterator() ;
	            StateData stateData1 = stateData1Iterator.next();
	            DefinedTermBase<?> state1 = stateData1.getState();
	            if (checkFeature){
	                if (iAifDependencies.get(state1)!=null) {
	                    featureDependencies.get(deb1.getFeature()).addAll(iAifDependencies.get(state1));
	                }
	                if (oAifDependencies.get(state1)!=null) {
	                    featureDependencies.get(deb1.getFeature()).addAll(oAifDependencies.get(state1));
	                }
	            }
	            while (stateData2Iterator.hasNext()) {
	                StateData stateData2 = stateData2Iterator.next();
	                DefinedTermBase<?> state2 = stateData2.getState();
	                bool = bool || state1.equals(state2); // checks if the states are the same
	            }
	        }

	        if (bool) {
	            return 0;
	        } else {
	            return 1;
	        }
	    }
}