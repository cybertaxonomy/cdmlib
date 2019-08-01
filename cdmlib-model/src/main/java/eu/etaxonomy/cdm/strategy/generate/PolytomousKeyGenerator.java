package eu.etaxonomy.cdm.strategy.generate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author m.venin
 *
 */
public class PolytomousKeyGenerator {

    private PolytomousKeyGeneratorConfigurator config;
	//TODO include in configurator
    private TermTree dependenciesTree; // the tree containing the dependencies between states and features (InapplicableIf and OnlyApplicableIf)

	private Map<State,Set<Feature>> iIdependencies = new HashMap<>(); // map of a set of Features (value) inapplicables if a State (key) is present
	private Map<State,Set<Feature>> oAIdependencies = new HashMap<>(); // map of a set of Features (value) only applicables if a State (key) is present
	private Map<Feature,Set<Feature>> featureDependencies = new HashMap<>(); // map of all the sets of features (values) which have dependencies with states of other features (keys)


	/**
	 * These strings are used for generating the statements of the key.
	 */
	private static final String before="<";
	private static final String after=">";
	private static final String separator = " or ";

	/**
	 * Sets the tree containing the dependencies between states and features.
	 *
	 * @param tree
	 */
	public void setDependencies(TermTree tree){
		this.dependenciesTree = tree;
	}

    /**
     * Creates the key and prints it
     */

    public PolytomousKey invoke(PolytomousKeyGeneratorConfigurator config){
        if (config == null){
            throw new NullPointerException("PolytomousKeyGeneratorConfigurator must not be null");
        }
        this.config = config;
        if (this.config.isUseDependencies() && dependenciesTree!=null){
            checkDependencies(dependenciesTree.getRoot());
        }
        PolytomousKey polytomousKey = PolytomousKey.NewInstance();
        PolytomousKeyNode root = polytomousKey.getRoot();
        buildBranches(root, config.getFeatures(), (Set)config.getTaxonDescriptions(), true);
        return polytomousKey;
    }


	/**
	 * Recursive function that builds the branches of the identification key (FeatureTree)
	 *
	 * @param father the node considered
	 * @param featuresLeft List of features that can be used at this point
	 * @param taxaCovered the taxa left at this point (i.e. that verify the description corresponding to the path leading to this node)
	 * @param taxaDiscriminatedInPreviousStep if in the previous level the taxa discriminated are the same, this boolean is set to true,
	 *           thus if the taxa, again, are not discriminated at this level the function stops
	 */
	private void buildBranches(PolytomousKeyNode father, List<Feature> featuresLeft, Set<DescriptionBase<?>> taxaCovered, boolean taxaDiscriminatedInPreviousStep){
	    // boolean indicating if this node has children, if not,
        // it means we reached a leaf and instead of adding a
        // question or statement to it, we must add the list of taxa discriminated at this point
	    boolean childrenExist = false;

		// These sets respectively store the features inapplicables and applicables at this point in the key
		Set<Feature> innapplicables = new HashSet<>();
		Set<Feature> applicables = new HashSet<>();

		if (taxaCovered.size()<=1){
		    //do nothing
		}else {
			// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
			Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<>();
			// the scores of the different features are calculated, the thresholds in the same time
			System.out.println("Feature left: " + featuresLeft);
			Feature winnerFeature = computeScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);

			/************** either the feature supports quantitative data... **************/
			// NB: in this version, "quantitative features" are dealt with in a dichotomous way
			if (winnerFeature != null && winnerFeature.isSupportsQuantitativeData()) {
				childrenExist = handleQuantitativeData(father, featuresLeft, taxaCovered, taxaDiscriminatedInPreviousStep,
                        childrenExist, quantitativeFeaturesThresholds, winnerFeature);
			}

			/************** ...or it supports categorical data. **************/
			if (winnerFeature!=null && winnerFeature.isSupportsCategoricalData()) {
			    childrenExist = handleCategorialFeature(father, featuresLeft, taxaCovered,
                        taxaDiscriminatedInPreviousStep, childrenExist, innapplicables, applicables, winnerFeature);
			}
			// the features depending on other features are added/removed to/from the features left once the branch is done
			if (config.isUseDependencies()){
				for (Feature feature : innapplicables) {
                    featuresLeft.add(feature);
                }
				for (Feature feature : applicables) {
                    featuresLeft.remove(feature);
                }
			}
			// the winner features are put back to the features left once the branch is done
			if (winnerFeature != null){
			    featuresLeft.add(winnerFeature);
			}
		}
		// if the node is a leaf, the statement contains the list of taxa to which the current leaf leads.
		if (!childrenExist){
			handleLeaf(father, taxaCovered);
		}
	}

    /**
     * @param father
     * @param taxaCovered
     *
     */
    private void handleLeaf(PolytomousKeyNode father, Set<DescriptionBase<?>> taxaCovered) {
        KeyStatement fatherStatement = father.getStatement();
        for(DescriptionBase<?> description: taxaCovered){
            description = CdmBase.deproxy(description);
            if  (description instanceof TaxonDescription){
                father.setOrAddTaxon(((TaxonDescription)description).getTaxon());
            }else{
                //FIXME handle other descriptions better
                if (fatherStatement!=null){
                    String statementString = fatherStatement.getLabelText(Language.DEFAULT());
                    if (statementString !=null && description != null){
                        String label = statementString + " --> " + description.getTitleCache();
                        fatherStatement.putLabel(Language.DEFAULT(), label);
                    }
                }
            }
        }

    }

    /**
     * @param father
     * @param featuresLeft
     * @param taxaCovered
     * @param taxaDiscriminatedInPreviousStep
     * @param childrenExist
     * @param innapplicables
     * @param applicables
     * @param winnerFeature
     * @param statesDone
     * @return
     */
    private boolean handleCategorialFeature(PolytomousKeyNode father, List<Feature> featuresLeft,
            Set<DescriptionBase<?>> taxaCovered, boolean taxaDiscriminatedInPreviousStep, boolean childrenExist,
            Set<Feature> innapplicables, Set<Feature> applicables, Feature winnerFeature) {

        // "categorical features" may present several different states, each one of these might correspond to one child
        List<State> statesDone = new ArrayList<>(); // the list of states already used

        int numberOfStates;
        Set<State> states = getAllStates(winnerFeature, taxaCovered);
		// a map is created, the key being the set of taxa that present the state(s) stored in the corresponding value
		Map<Set<DescriptionBase<?>>,List<State>> taxonStatesMap = determineCategoricalStates(
		            statesDone, states, winnerFeature, taxaCovered);
		// if the merge option is ON, branches with the same discriminative power will be merged (see Vignes & Lebbes, 1989)
		if (config.isMerge()){
			// creates a map between the different states of the winnerFeature and the sets of states "incompatible" with them
			Map<State,Set<State>> exclusions = new HashMap<>();
			featureScoreAndMerge(winnerFeature, taxaCovered, exclusions);

			while (!exclusions.isEmpty()){
				// looks for the largest clique, i.e. the state with less exclusions
				List<State> clique = returnBestClique(exclusions);
				// then merges the corresponding branches
				mergeBranches(clique, taxonStatesMap);
			}
		}

		List<Set<DescriptionBase<?>>> sortedKeys = sortKeys(taxonStatesMap);
		for (Set<DescriptionBase<?>> newTaxaCovered : sortedKeys){
			List<State> listOfStates = taxonStatesMap.get(newTaxaCovered);
			if ((newTaxaCovered.size()>0) && !((newTaxaCovered.size()==taxaCovered.size()) && !taxaDiscriminatedInPreviousStep)){ // if the taxa are discriminated compared to those of the father node, a child is created
				childrenExist = true;
				PolytomousKeyNode pkNode = PolytomousKeyNode.NewInstance();
				StringBuilder statementLabel = new StringBuilder();
				numberOfStates = listOfStates.size()-1;
				for (State state : listOfStates) {
					if (config.isUseDependencies()){
						// if the dependencies are considered, removes and adds the right features from/to the list of features left
						// these features are stored in order to be put back again when the current branch is finished
						if (iIdependencies.get(state)!= null) {
                            innapplicables.addAll(iIdependencies.get(state));
                        }
						if (oAIdependencies.get(state)!= null) {
                            applicables.addAll(oAIdependencies.get(state));
                        }
						for (Feature feature : innapplicables) {
                            featuresLeft.remove(feature);
                        }
						for (Feature feature : applicables) {
                            featuresLeft.add(feature);
                        }
					}
					statementLabel.append(state.getLabel());
					if (listOfStates.lastIndexOf(state)!=numberOfStates){
                        statementLabel.append(separator); // append a separator after each state except for the last one
                    }
				}
				// old code used when PolytomousKey extended FeatureTree
				//									Representation question = new Representation(null, questionLabel.toString(),null, Language.DEFAULT());
				//									son.addQuestion(question);
				KeyStatement statement = KeyStatement.NewInstance(statementLabel.toString());
				pkNode.setStatement(statement);
				father.setFeature(winnerFeature);
				father.addChild(pkNode);
				featuresLeft.remove(winnerFeature); // unlike for quantitative features, once a categorical one has been used, it cannot be reused in the same branch
				boolean areTheTaxaDiscriminated = !(newTaxaCovered.size() == taxaCovered.size());
				buildBranches(pkNode, featuresLeft, newTaxaCovered, areTheTaxaDiscriminated);
			}
		}

        return childrenExist;
    }

    /**
     * @param taxonStatesMap
     * @return
     */
    private List<Set<DescriptionBase<?>>> sortKeys(Map<Set<DescriptionBase<?>>, List<State>> taxonStatesMap) {
        //for now this is a dummy sorting
        List<Map.Entry<Set<DescriptionBase<?>>, List<State>>> entries = new ArrayList<>();
        entries.addAll(taxonStatesMap.entrySet());
        Comparator<? super Entry<Set<DescriptionBase<?>>, List<State>>> c = (a,b)-> {
            if (a.getKey().size()!=b.getKey().size()){
                return a.getKey().size() - b.getKey().size();
            }else if (a.getValue().size()!= b.getValue().size()){
                return a.getValue().size() - b.getValue().size();
            }else{
                for (int i = 0; i < a.getValue().size(); i++){
                    State stateA = a.getValue().get(i);
                    State stateB = b.getValue().get(i);
                    //TODO use real Term Comparator
                    if (!stateA.getTitleCache().equals(stateB.getTitleCache())){
                        return stateA.getTitleCache().compareTo(stateB.getTitleCache());
                    }
                    if (stateA.getUuid()!= stateB.getUuid()){
                        return stateA.getUuid().compareTo(stateB.getUuid());
                    }
                }
                //TODO compare keys (sets of descriptionBase)
//                for (int i = 0; i < a.getKey().size(); i++){
//                    Object stateA = a.getKey().getUuid;
//                    State stateB = a.getKey().get(i);
//                    //TODO use real Term Comparator
//                    if (stateA.getUuid()!= stateB.getUuid()){
//                        return stateA.getUuid().compareTo(stateB.getUuid());
//                    }
//                }
                throw new RuntimeException("Compare for same state lists with different unit descriptions not yet implemented");

            }
        };
        entries.sort(c );
        List<Set<DescriptionBase<?>>> result = new ArrayList<>();
        for (Map.Entry<Set<DescriptionBase<?>>, List<State>> entry : entries){
            result.add(entry.getKey());
        }
        return result;
    }

//    private static Comparator<? super Entry<Set<DescriptionBase<?>>, List<State>>> taxonStatesComparator


    /**
     * @param winnerFeature
     * @param taxaCovered
     */
    private Set<State> getAllStates(Feature winnerFeature, Set<DescriptionBase<?>> taxaCovered) {
        Set<State> states = new HashSet<>();
        for (DescriptionBase<?> td : taxaCovered){
            for (DescriptionElementBase deb : td.getElements()) {
                if (deb.getFeature().equals(winnerFeature)) {
                    List<StateData> stateDatas = CdmBase.deproxy(deb, CategoricalData.class).getStateData();

                    for (StateData sd : stateDatas){
                        states.add(sd.getState());
                    }
                }
            }
        }
        return states;
    }

    /**
     * @param winnerFeature
     * @param td
     * @param debConcerned
     * @return
     */
    private DescriptionElementBase getDescriptionElementForFeature(Feature winnerFeature, DescriptionBase<?> td) {
        DescriptionElementBase result = null;
        for (DescriptionElementBase deb : td.getElements()) {
        	if (deb.getFeature().equals(winnerFeature)) {
        	    result = deb;
            }
        }
        return result;
    }

    /**
     * @param father
     * @param featuresLeft
     * @param taxaCovered
     * @param taxaDiscriminatedInPreviousStep
     * @param childrenExist
     * @param quantitativeFeaturesThresholds
     * @param winnerFeature
     * @return
     */
    private boolean handleQuantitativeData(PolytomousKeyNode father, List<Feature> featuresLeft,
            Set<DescriptionBase<?>> taxaCovered, boolean taxaDiscriminatedInPreviousStep, boolean childrenExist,
            Map<Feature, Float> quantitativeFeaturesThresholds, Feature winnerFeature) {

        // first, get the threshold
        float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
        String sign;
        StringBuilder unit = new StringBuilder("");
        // then determine which taxa are before and which are after this threshold (dichotomy)
        //in order to create the children of the father node
        List<Set<DescriptionBase<?>>> quantitativeStates = determineQuantitativeStates(threshold, winnerFeature, taxaCovered, unit);
        // thus the list contains two sets of DescriptionBase, the first corresponding to
        //those before, the second to those after the threshold
        for (int i=0; i<2; i++) {
        	Set<DescriptionBase<?>> newTaxaCovered = quantitativeStates.get(i);
        	if (i==0){
        		sign = before; // the first element of the list corresponds to taxa before the threshold
        	} else {
        		sign = after; // the second to those after
        	}
        	if (newTaxaCovered.size()>0 && !((newTaxaCovered.size()==taxaCovered.size()) && !taxaDiscriminatedInPreviousStep)){ // if the taxa are discriminated compared to those of the father node, a child is created
        		childrenExist = true;
        		PolytomousKeyNode pkNode = PolytomousKeyNode.NewInstance();
        		father.setFeature(winnerFeature);
        		KeyStatement statement = KeyStatement.NewInstance( " " + sign + " " + threshold + unit);
        		pkNode.setStatement(statement);
        		father.addChild(pkNode);
        		boolean areTheTaxaDiscriminated;
        		if (newTaxaCovered.size()==taxaCovered.size()) {
                    areTheTaxaDiscriminated = false;
                } else {
                    areTheTaxaDiscriminated = true;
                }
        		buildBranches(pkNode, featuresLeft, newTaxaCovered, areTheTaxaDiscriminated);
        	}
        }
        return childrenExist;
    }

    /**
     * @param featuresLeft
     * @param taxaCovered
     * @param quantitativeFeaturesThresholds
     * @return
     */
    private Feature computeScores(List<Feature> featuresLeft, Set<DescriptionBase<?>> taxaCovered,
            Map<Feature, Float> quantitativeFeaturesThresholds) {
        Map<Feature,Float> scoreMap = featureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
        dependenciesScores(scoreMap, featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
        // the feature with the best score becomes the one corresponding to the current node
        Feature winnerFeature = lessStatesWinner(scoreMap, taxaCovered);
        // the feature is removed from the list of features available to build the next level of the tree
        featuresLeft.remove(winnerFeature);
        System.out.println("   ScoreMap: " + scoreMap);
        System.out.println("   quantitativeThreshold: " + quantitativeFeaturesThresholds);
        return winnerFeature;
    }



	/**
	 * Dependencies can be seen as a hierarchy.
	 * If the dependencies are on, this function calculates the score of all children features and give the highest score
	 * of these to their father (of course, if its score is lower). This way, if a feature has a good score but is
	 * "onlyApplicableIf" or "InapplicableIf", the feature it depends can be chosen in order to build a better key.
	 *
	 * @param scoreMap
	 * @param featuresLeft
	 * @param coveredTaxa
	 * @param quantitativeFeaturesThresholds
	 */
	private void dependenciesScores(Map<Feature,Float> scoreMap, List<Feature> featuresLeft,
	        Set<DescriptionBase<?>> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		// first copies the list of features left
		List<Feature> pseudoFeaturesLeft = featuresLeft.subList(0, Math.max(0, featuresLeft.size()-1));
		// then adds all the features which depend on features contained in the list
		for (Feature feature : pseudoFeaturesLeft){
			if (featureDependencies.containsKey(feature)){
				for (Feature feature2 : featureDependencies.get(feature)){
					if (!pseudoFeaturesLeft.contains(feature2)){
						pseudoFeaturesLeft.add(feature2);
					}
				}
			}
		}
		// then calculates the scores of all features that have been added
		Map<Feature,Float> newScoreMap = featureScores(pseudoFeaturesLeft, coveredTaxa, quantitativeFeaturesThresholds);
		for (Feature feature : featureDependencies.keySet()){
			if (newScoreMap.containsKey(feature)){
				for (Feature feature2 : featureDependencies.get(feature)){
					if (newScoreMap.containsKey(feature2)) {
						// if a feature has a better than the "father" feature it depends on
						if (newScoreMap.get(feature2)>newScoreMap.get(feature)){
							// the "father" feature gets its score
							scoreMap.put(feature, newScoreMap.get(feature2));
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
	 */
	private void mergeBranches(List<State> clique, Map<Set<DescriptionBase<?>>, List<State>> taxonStatesMap){
		boolean stateFound;
		Map.Entry<Set<DescriptionBase<?>>,List<State>> firstBranch=null;
		List<Set<DescriptionBase<?>>> tdToDelete = new ArrayList<>();

		if (clique.size()>1){
			Iterator<Map.Entry<Set<DescriptionBase<?>>, List<State>>> it1 = taxonStatesMap.entrySet().iterator();
			while (it1.hasNext()){
				Map.Entry<Set<DescriptionBase<?>>,List<State>> branch = it1.next();
				Iterator<State> stateIterator = clique.iterator();
				stateFound=false;
				// looks for one state of the clique in this branch
				while(stateIterator.hasNext() && stateFound!=true) {
					State state = stateIterator.next();
					if (branch.getValue().contains(state)) {
						stateFound=true;
					}
				}
				// if one state is found...
				if (stateFound==true){
					// ...for the first time, the branch becomes the one to which the others will be merged
					if (firstBranch==null){
						firstBranch=branch;
					}
					// ... else the branch is merged to the first one.
					else {
						firstBranch.getKey().addAll(branch.getKey());
						firstBranch.getValue().addAll(branch.getValue());
						tdToDelete.add(branch.getKey());
					}
				}
			}
			// once this is done, the branches merged to the first one are deleted
			for (Set<DescriptionBase<?>> td : tdToDelete){
				taxonStatesMap.remove(td);
			}
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
	private List<State> returnBestClique (Map<State,Set<State>> exclusions){
		int bestNumberOfExclusions=-1;;
		int numberOfExclusions;
		List<State> clique = new ArrayList<State>();

		// looks for the largest clique, i.e. the state with less exclusions
		State bestState=null;
		for (Iterator<Map.Entry<State,Set<State>>> it1 = exclusions.entrySet().iterator() ; it1.hasNext();){
			Map.Entry<State,Set<State>> pair = it1.next();
			numberOfExclusions = pair.getValue().size();
			if ((bestNumberOfExclusions==-1) || numberOfExclusions<bestNumberOfExclusions) {
				bestNumberOfExclusions=numberOfExclusions;
				bestState = pair.getKey();
			}
		}

		clique.add(bestState);
		exclusions.remove(bestState);

		boolean isNotExcluded;
		// then starts building the clique by adding the states which do not exclude each other
		for (Iterator<Map.Entry<State,Set<State>>> iterator = exclusions.entrySet().iterator() ; iterator.hasNext();){
			Map.Entry<State,Set<State>> pair = iterator.next();
			isNotExcluded = true;
			for (State state : clique) {
				if (pair.getValue().contains(state)) {
                    isNotExcluded = false;
                }
			}
			if (isNotExcluded){
				clique.add(pair.getKey());
			}
		}
		for (State state : clique) {
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
	 * @return
	 */
	private Map<Set<DescriptionBase<?>>,List<State>> determineCategoricalStates(List<State> statesDone,
	        Set<State> states, Feature feature, Set<DescriptionBase<?>> taxaCovered){

	    Map<Set<DescriptionBase<?>>, List<State>> childrenStatesMap = new HashMap<>();

		for (State featureState : states){ // for each state
			if(!statesDone.contains(featureState)){ // if the state hasn't already be considered
				statesDone.add(featureState);
				Set<DescriptionBase<?>> newTaxaCovered = whichTaxa(feature, featureState, taxaCovered); //gets which taxa present this state
				List<State> newStates = childrenStatesMap.get(newTaxaCovered);
				if (newStates==null) { // if no states are associated to these taxa, create a new list
					newStates = new ArrayList<>();
					childrenStatesMap.put(newTaxaCovered, newStates);
				}
				newStates.add(featureState); // then add the state to the list
			}
		}
		return childrenStatesMap;
	}


	/**
	 * Returns the list of taxa from previously covered taxa, which have the state featureState for the given feature
	 *
	 * @param feature
	 * @param featureState
	 * @param taxaCovered
	 * @return
	 */
	private Set<DescriptionBase<?>> whichTaxa(Feature feature, State featureState, Set<DescriptionBase<?>> taxaCovered){
		Set<DescriptionBase<?>> newCoveredTaxa = new HashSet<>();
		for (DescriptionBase<?> td : taxaCovered){
			Set<DescriptionElementBase> elements = td.getElements();
			for (DescriptionElementBase deb : elements){
				if (deb.isInstanceOf(CategoricalData.class)) {
					if (deb.getFeature().equals(feature)) {
						List<StateData> stateDatas = CdmBase.deproxy(deb, CategoricalData.class).getStateData();
						for (StateData sd : stateDatas) {
							if (sd.getState().equals(featureState)){
								newCoveredTaxa.add(td);
							}
						}
					}
				}
			}
		}
		return newCoveredTaxa;
	}


	/**
	 * This function returns the feature with the highest score. However, if several features have the same score
	 * the one wich leads to less options is chosen (this way, the key is easier to read).
	 *
	 * @param nTaxa
	 * @param scores
	 * @param taxaCovered
	 * @return
	 */
	private Feature lessStatesWinner(Map<Feature,Float> scores, Set<DescriptionBase<?>> taxaCovered){
		int nTaxa = taxaCovered.size();
		if (nTaxa==1) {
            return null;
        }
		float meanScore = defaultMeanScore(nTaxa);
		float bestScore = nTaxa*nTaxa;
		List<Feature> bestFeatures = new ArrayList<>(); // if ever different features have the best score, they are put in this list
		Feature bestFeature = null;
		Iterator<Map.Entry<Feature,Float>> it = scores.entrySet().iterator();
		float newScore;
		while (it.hasNext()){
			Map.Entry<Feature,Float> pair = it.next();
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
			int lessStates=-1;
			int numberOfDifferentStates=-1;
			for (Feature feature : bestFeatures){
				if (feature.isSupportsCategoricalData()){
					Set<State> differentStates = new HashSet<>();
					for (DescriptionBase<?> td : taxaCovered){
						Set<DescriptionElementBase> elements = td.getElements();
						for (DescriptionElementBase deb : elements){
							if (deb.isInstanceOf(CategoricalData.class)) {
								CategoricalData catdat = (CategoricalData)deb;
								if (catdat.getFeature().equals(feature)) {
									List<StateData> stateDatas = catdat.getStateData();
									for (StateData sd : stateDatas) {
										differentStates.add(sd.getState());
									}
								}
							}
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
	 *
	 * @param nTaxa
	 * @return
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
	 *
	 * @param featuresLeft
	 * @param coveredTaxa
	 * @param quantitativeFeaturesThresholds
	 * @return
	 */
	private Map<Feature,Float> featureScores(List<Feature> featuresLeft, Set<DescriptionBase<?>> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		Map<Feature,Float> scoreMap = new HashMap<>();
		for (Feature feature : featuresLeft){
			if (feature.isSupportsCategoricalData()) {
				scoreMap.put(feature, categoricalFeatureScore(feature, coveredTaxa));
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
	 * It returns two Sets of DescriptionBase, one with the taxa under this threshold (taxaBefore) and another one
	 * with the taxa over (taxaAfter).
	 *
	 * @param threshold
	 * @param feature
	 * @param taxa
	 * @param unit
	 * @return
	 */
	private List<Set<DescriptionBase<?>>> determineQuantitativeStates (Float threshold, Feature feature,
	        Set<DescriptionBase<?>> taxa, StringBuilder unit){
		List<Set<DescriptionBase<?>>> list = new ArrayList<>();
		Set<DescriptionBase<?>> taxaBefore = new HashSet<>();
		Set<DescriptionBase<?>> taxaAfter = new HashSet<>();
		list.add(taxaBefore);
		list.add(taxaAfter);
		for (DescriptionBase<?> td : taxa){
			Set<DescriptionElementBase> elements = td.getElements();
			for (DescriptionElementBase deb : elements){
				if (deb.getFeature().equals(feature)) {
					if (deb.isInstanceOf(QuantitativeData.class)) {
						QuantitativeData qd = (QuantitativeData)deb;
						if (unit.toString().equals("") && qd.getUnit()!=null && qd.getUnit().getLabel()!=null){
							unit.append(" " + qd.getUnit().getLabel());
						}
						Set<StatisticalMeasurementValue> values = qd.getStatisticalValues();
						for (StatisticalMeasurementValue smv : values){
							StatisticalMeasure type = smv.getType();
							//TODO DONT FORGET sample size, MEAN etc
							if (type.equals(StatisticalMeasure.MAX()) || type.equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY()) || type.equals(StatisticalMeasure.AVERAGE())) {
								if (smv.getValue()>threshold){
									taxaAfter.add(td);
								}
							}
							if (type.equals(StatisticalMeasure.MIN()) || type.equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY()) || type.equals(StatisticalMeasure.AVERAGE())) {
								if (smv.getValue()<=threshold){
									taxaBefore.add(td);
								}
							}
						}
					}
				}
			}
		}
		return list;
	}



	/**
	 * This function returns the score of a quantitative feature.
	 *
	 * @param feature
	 * @param coveredTaxa
	 * @param quantitativeFeaturesThresholds
	 * @return
	 */
	private float quantitativeFeatureScore(Feature feature, Set<DescriptionBase<?>> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		List<Float> allValues = new ArrayList<>();
		boolean lowerboundarypresent;
		boolean upperboundarypresent;
		float lowerboundary=0;
		float upperboundary=0;
		for (DescriptionBase<?> td : coveredTaxa){
			Set<DescriptionElementBase> elements = td.getElements();
			for (DescriptionElementBase deb : elements){
				if (deb.getFeature().equals(feature)) {
					if (deb.isInstanceOf(QuantitativeData.class)) {
						QuantitativeData qd = (QuantitativeData)deb;
						Set<StatisticalMeasurementValue> values = qd.getStatisticalValues();
						lowerboundarypresent = false;
						upperboundarypresent = false;
						for (StatisticalMeasurementValue smv : values){
							StatisticalMeasure type = smv.getType();
							// DONT FORGET sample size, MEAN etc
							if (type.equals(StatisticalMeasure.MAX())) {
								upperboundary = smv.getValue();
								upperboundarypresent=true;
							}
							if (type.equals(StatisticalMeasure.MIN())) {
								lowerboundary = smv.getValue();
								lowerboundarypresent=true;
							}
							if (type.equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY()) && upperboundarypresent==false) {
								upperboundary = smv.getValue();
								upperboundarypresent=true;
							}
							if (type.equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY()) && lowerboundarypresent==false) {
								lowerboundary = smv.getValue();
								lowerboundarypresent=true;
							}
							if (type.equals(StatisticalMeasure.AVERAGE()) && upperboundarypresent==false && lowerboundarypresent==false) {
								lowerboundary = smv.getValue();
								upperboundary = lowerboundary;
								lowerboundarypresent=true;
								upperboundarypresent=true;
							}
						}
						if (lowerboundarypresent && upperboundarypresent) {
							allValues.add(lowerboundary);
							allValues.add(upperboundary);
						}
					}
				}
			}
		}
		int i,j;
		float threshold=0;
		float bestThreshold=0;
		int difference=allValues.size();
		int differenceMin = difference;
		int bestTaxaBefore=0;
		int bestTaxaAfter=0;
		for (i=0;i<allValues.size()/2;i++) {
			threshold = allValues.get(i*2+1);
			int taxaBefore=0;
			int taxaAfter=0;
			for (j=0;j<allValues.size()/2;j++) {
				if (allValues.get(j*2+1)<=threshold){
					taxaBefore++;
				}
				if (allValues.get(j*2)>threshold){
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



	/**
	 * This function returns the score of a categorical feature.
	 *
	 * @param feature
	 * @param coveredTaxa
	 * @return
	 */
	private float categoricalFeatureScore(Feature feature, Set<DescriptionBase<?>> coveredTaxa){
		int i,j;
		float score =0;
		float power=0;
		DescriptionBase<?>[] coveredTaxaArray = coveredTaxa.toArray(new DescriptionBase[coveredTaxa.size()]); // I did not figure a better way to do this
		for (i=0 ; i<coveredTaxaArray.length; i++){
			Set<DescriptionElementBase> elements1 = coveredTaxaArray[i].getElements();
			DescriptionElementBase deb1 = null;
			for (DescriptionElementBase deb : elements1){
				if (deb.getFeature().equals(feature)){
					deb1 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
				}
			}
			for (j=i+1 ; j< coveredTaxaArray.length ; j++){
				Set<DescriptionElementBase> elements2 = coveredTaxaArray[j].getElements();
				DescriptionElementBase deb2 = null;
				for (DescriptionElementBase deb : elements2){
					if (deb.getFeature().equals(feature)){
						deb2 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
					}
				}
				power = defaultPower(deb1,deb2);
				score = score + power;
			}
		}
		return score;
	}


	/**
	 * This recursive function fills the maps of dependencies by reading the tree containing the dependencies.
	 *
	 * @param node
	 */
	private void checkDependencies(TermNode<Feature> node){
		if (node.getOnlyApplicableIf()!=null){
			Set<State> addToOAI = node.getOnlyApplicableIf();
			for (State state : addToOAI){
				if (oAIdependencies.containsKey(state)) {
                    oAIdependencies.put(state, new HashSet<Feature>());
                }
				oAIdependencies.get(state).add(node.getTerm());
			}
		}
		if (node.getInapplicableIf()!=null){
			Set<State> addToiI = node.getInapplicableIf();
			for (State state : addToiI){
				if (iIdependencies.containsKey(state)) {
                    iIdependencies.put(state, new HashSet<Feature>());
                }
				iIdependencies.get(state).add(node.getTerm());
			}
		}
		if (node.getChildNodes()!=null) {
			for (TermNode fn : node.getChildNodes()){
				checkDependencies(fn);
			}
		}
	}



	/**
	 * This function fills the exclusions map.
	 *
	 * @param feature
	 * @param coveredTaxa
	 * @param exclusions
	 * @return
	 */
	private float featureScoreAndMerge(Feature feature, Set<DescriptionBase<?>> coveredTaxa, Map<State,Set<State>> exclusions){
		int i,j;
		float score =0;
		float power=0;
		DescriptionBase<?>[] coveredTaxaArray = coveredTaxa.toArray(new DescriptionBase[coveredTaxa.size()]); // I did not figure a better way to do this
		for (i=0 ; i<coveredTaxaArray.length; i++){
			Set<DescriptionElementBase> elements1 = coveredTaxaArray[i].getElements();
			DescriptionElementBase deb1 = null;
			for (DescriptionElementBase deb : elements1){
				if (deb.getFeature().equals(feature)){
					deb1 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
				}
			}
			for (j=i+1 ; j< coveredTaxaArray.length ; j++){
				Set<DescriptionElementBase> elements2 = coveredTaxaArray[j].getElements();
				DescriptionElementBase deb2 = null;
				for (DescriptionElementBase deb : elements2){
					if (deb.getFeature().equals(feature)){
						deb2 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
					}
				}
				power = defaultPower(deb1,deb2);
				score = score + power;
				if (power>0) // if there is no state in common between deb1 and deb2
				{
					CategoricalData cat1 = (CategoricalData)deb1;
					CategoricalData cat2 = (CategoricalData)deb2;
					for (StateData statedata1 : cat1.getStateData()){
						State state1 = statedata1.getState();
						if (!exclusions.containsKey(state1)){
							exclusions.put(state1, new HashSet<State>());
						}
						for (StateData statedata2 : cat2.getStateData()){
							State state2 = statedata2.getState();
							if (!exclusions.containsKey(state2)){
								exclusions.put(state2, new HashSet<State>());
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



	/**
	 * Returns the score between two DescriptionElementBase. If one of them is null, returns -1.
	 * If they are not of the same type (Categorical) returns 0.
	 *
	 * @param deb1
	 * @param deb2
	 * @return
	 */
	private float defaultPower(DescriptionElementBase deb1, DescriptionElementBase deb2){
		if (deb1==null || deb2==null) {
			return -1; //what if the two taxa don't have this feature in common ?
		}
		if ((deb1.isInstanceOf(CategoricalData.class))&&(deb2.isInstanceOf(CategoricalData.class))) {
			return defaultCategoricalPower((CategoricalData)deb1, (CategoricalData)deb2);
		} else {
            return 0;
        }
	}

	/**
	 * Returns the score of a categorical feature.
	 *
	 * @param deb1
	 * @param deb2
	 * @return
	 */
	private float defaultCategoricalPower(CategoricalData cd1, CategoricalData cd2){
	    //FIXME see defaultCategoricalPower_old for additional code on dependencies
	    //which has been removed here for now but might be important

	    //get all states of both categorical data
        Set<State> states = getStates(cd1, cd2);
        if (states.size() == 0){
            return 0;
        }

	    int nDiscriminative = 0;
	    for (State state : states){
	        boolean hasState1 = hasState(state, cd1);
	        boolean hasState2 = hasState(state, cd2);
	        //if only 1 has the state than the state is discriminative
	        if (! (hasState1&&hasState2)) {
	            nDiscriminative++;
            }
	    }
	    return nDiscriminative/states.size();

	}

	   /**
     * @param cd
     * @return
     */
    private boolean hasState(State state, CategoricalData cd) {
        boolean result = false;
        for (StateData stateData:cd.getStateData()){
            result |= state.equals(stateData.getState());
        }
        return result;
    }

    /**
     * @param cd1
     * @param cd2
     * @return
     */
    private Set<State> getStates(CategoricalData cd1, CategoricalData cd2) {
        Set<State> result = new HashSet<>();
        List<StateData> states1 = cd1.getStateData();
        List<StateData> states2 = cd2.getStateData();
        for (StateData state:states1){
            result.add(state.getState());
        }
        for (StateData state:states2){
            result.add(state.getState());
        }
        return result;
    }


    //keep as long as the handling of featureDependencies is not yet checked or handled in
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
	            State state1 = stateData1.getState();
	            if (checkFeature){
	                if (iIdependencies.get(state1)!=null) {
	                    featureDependencies.get(deb1.getFeature()).addAll(iIdependencies.get(state1));
	                }
	                if (oAIdependencies.get(state1)!=null) {
	                    featureDependencies.get(deb1.getFeature()).addAll(oAIdependencies.get(state1));
	                }
	            }
	            while (stateData2Iterator.hasNext()) {
	                StateData stateData2 = stateData2Iterator.next();
	                State state2 = stateData2.getState();
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

