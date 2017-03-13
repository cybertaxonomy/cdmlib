package eu.etaxonomy.cdm.strategy.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

/**
 * @author m.venin
 *
 */
public class PolytomousKeyGenerator {

	static int level=-1; // global variable needed by the printTree function in order to store the level which is being printed
	private PolytomousKey polytomousKey; // the Identification Key
	private List<Feature> features; // the features used to generate the key
	private Set<TaxonDescription> taxa; // the base of taxa

	private boolean merge=true; // if this boolean is set to true, branches of the tree will be merged if the corresponding states can be used together without decreasing their score

	private FeatureTree dependenciesTree; // the tree containing the dependencies between states and features (InapplicableIf and OnlyApplicableIf)
	private Map<State,Set<Feature>> iIdependencies = new HashMap<State,Set<Feature>>(); // map of a set of Features (value) inapplicables if a State (key) is present
	private Map<State,Set<Feature>> oAIdependencies = new HashMap<State,Set<Feature>>(); // map of a set of Features (value) only applicables if a State (key) is present
	private Map<Feature,Set<Feature>> featureDependencies = new HashMap<Feature,Set<Feature>>(); // map of all the sets of features (values) which have dependencies with states of other features (keys)
	private boolean dependenciesON = true; // if this boolean is true, the dependencies are taken into account


	/**
	 * These strings are used for generating the statements of the key.
	 */
	private String before="<";
	private String after=">";
	private String separator = " or ";

	/**
	 * Sets the features used to generate the key.
	 *
	 * @param featuresList
	 */
	public void setFeatures(List<Feature> featuresList){
		this.features = featuresList;
	}

	/**
	 * Sets the base of taxa.
	 *
	 * @param featuresList
	 */
	public void setTaxa(Set<TaxonDescription> taxaSet){
		this.taxa = taxaSet;
	}

	/**
	 * Sets the tree containing the dependencies between states and features.
	 *
	 * @param tree
	 */
	public void setDependencies(FeatureTree tree){
		this.dependenciesTree = tree;
	}

	/**
	 * Allows the generator to use the dependencies given by the function "setDependencies".
	 */
	public void turnDependenciesON(){
		this.dependenciesON = true;
	}

	/**
	 * Prevent the generator from using dependencies.
	 */
	public void turnDependenciesOFF(){
		this.dependenciesON = false;
	}

	/**
	 * Allows the generator to merge branches if the corresponding states can be used together without diminishing their score.
	 */
	public void mergeModeON(){
		this.merge = true;
	}

	/**
	 * Prevent the generator from merging branches (apart from those leading to the same set of taxa).
	 */
	public void mergeModeOFF(){
		this.merge = false;
	}


	/**
	 * Initializes the function buildBranches() with the starting parameters in order to build the key
	 */
	private void loop(){
		polytomousKey = PolytomousKey.NewInstance();
		PolytomousKeyNode root = polytomousKey.getRoot();
		buildBranches(root,features,taxa,true);
	}


	/**
	 * Creates the key and prints it
	 */
	public PolytomousKey invoke(){
		if (dependenciesON && dependenciesTree!=null){
			checkDependencies(dependenciesTree.getRoot());
		}
		loop();
		List<PolytomousKeyNode> rootlist = new ArrayList<PolytomousKeyNode>();
		rootlist.add(polytomousKey.getRoot());
		//		String spaces = new String();
		//		printTree(rootlist,spaces);
		//		System.out.println(paths.toString());
		return polytomousKey;
	}


	/**
	 * Recursive function that builds the branches of the identification key (FeatureTree)
	 *
	 * @param father the node considered
	 * @param featuresLeft List of features that can be used at this point
	 * @param taxaCovered the taxa left at this point (i.e. that verify the description corresponding to the path leading to this node)
	 * @param taxaAreTheSame if in the previous level the taxa discriminated are the same, this boolean is set to true, thus if the taxa, again, are not discriminated at this level the function stops
	 */
	private void buildBranches(PolytomousKeyNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered, boolean taxaDiscriminated){
		boolean childrenExist = false; // boolean indicating if this node has children, if not, it means we reached a leaf and instead of adding a
		// question or statement to it, we must add the list of taxa discriminated at this point

		// These sets respectively store the features inapplicables and applicables at this point in the key
		Set<Feature> innapplicables = new HashSet<Feature>();
		Set<Feature> applicables = new HashSet<Feature>();

		if (taxaCovered.size()>1) {
			// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
			Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<Feature,Float>();
			// the scores of the different features are calculated, the thresholds in the same time
			Map<Feature,Float> scoreMap = featureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
			dependenciesScores(scoreMap,featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
			// the feature with the best score becomes the one corresponding to the current node
			Feature winnerFeature = lessStatesWinner(scoreMap, taxaCovered);
			// the feature is removed from the list of features available to build the next level of the tree
			featuresLeft.remove(winnerFeature);

			int i;
			/************** either the feature supports quantitative data... **************/
			// NB: in this version, "quantitative features" are dealt with in a dichotomous way
			if (winnerFeature!=null && winnerFeature.isSupportsQuantitativeData()) {
				// first, get the threshold
				float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
				String sign;
				StringBuilder unit= new StringBuilder("");
				// then determine which taxa are before and which are after this threshold (dichotomy) in order to create the children of the father node
				List<Set<TaxonDescription>> quantitativeStates = determineQuantitativeStates(threshold,winnerFeature,taxaCovered,unit);
				// thus the list contains two sets of TaxonDescription, the first corresponding to those before, the second to those after the threshold
				for (i=0;i<2;i++) {
					Set<TaxonDescription> newTaxaCovered = quantitativeStates.get(i);
					if (i==0){
						sign = before; // the first element of the list corresponds to taxa before the threshold
					} else {
						sign = after; // the second to those after
					}
					if (newTaxaCovered.size()>0 && !((newTaxaCovered.size()==taxaCovered.size()) && !taxaDiscriminated)){ // if the taxa are discriminated compared to those of the father node, a child is created
						childrenExist = true;
						PolytomousKeyNode son = PolytomousKeyNode.NewInstance();
						son.setFeature(winnerFeature);
						// old code used when PolytomousKey extended FeatureTree
						//						Representation question = new Representation(null, " " + sign + " " + threshold +unit,null, Language.DEFAULT()); // the question attribute is used to store the state of the feature
						//						son.addQuestion(question);
						KeyStatement statement = KeyStatement.NewInstance( " " + sign + " " + threshold +unit); // the question attribute is used to store the state of the feature
						son.setStatement(statement);
						father.addChild(son);
						boolean areTheTaxaDiscriminated;
						if (newTaxaCovered.size()==taxaCovered.size()) {
                            areTheTaxaDiscriminated = false;
                        } else {
                            areTheTaxaDiscriminated = true;
                        }
						buildBranches(son,featuresLeft, newTaxaCovered,areTheTaxaDiscriminated);
					}
				}
			}

			/************** ...or it supports categorical data. **************/
			// "categorical features" may present several different states, each one of these might correspond to one child
			List<State> statesDone = new ArrayList<State>(); // the list of states already used
			int numberOfStates;
			if (winnerFeature!=null && winnerFeature.isSupportsCategoricalData()) {
				for (TaxonDescription td : taxaCovered){
					DescriptionElementBase debConcerned = null;

					// first, get the right DescriptionElementBase
					for (DescriptionElementBase deb : td.getElements()) {
						if (deb.getFeature().equals(winnerFeature)) {
                            debConcerned = deb;
                        }
					}

					if (debConcerned!=null) {
						// a map is created, the key being the set of taxa that present the state(s) stored in the corresponding value
						Map<Set<TaxonDescription>,List<State>> taxonStatesMap = determineCategoricalStates(statesDone,(CategoricalData)debConcerned,winnerFeature,taxaCovered);
						// if the merge option is ON, branches with the same discriminative power will be merged (see Vignes & Lebbes, 1989)
						if (merge){
							// creates a map between the different states of the winnerFeature and the sets of states "incompatible" with them
							Map<State,Set<State>> exclusions = new HashMap<State,Set<State>>();
							featureScoreAndMerge(winnerFeature,taxaCovered,exclusions);

							while (!exclusions.isEmpty()){
								// looks for the largest clique, i.e. the state with less exclusions
								List<State> clique = returnBestClique(exclusions);
								// then merges the corresponding branches
								mergeBranches(clique,taxonStatesMap);
							}
						}
						if (taxonStatesMap!=null && !taxonStatesMap.isEmpty()) {
							for (Map.Entry<Set<TaxonDescription>,List<State>> entry : taxonStatesMap.entrySet()){
								Set<TaxonDescription> newTaxaCovered = entry.getKey();
								List<State> listOfStates = entry.getValue();
								if ((newTaxaCovered.size()>0) && !((newTaxaCovered.size()==taxaCovered.size()) && !taxaDiscriminated)){ // if the taxa are discriminated compared to those of the father node, a child is created
									childrenExist = true;
									PolytomousKeyNode son = PolytomousKeyNode.NewInstance();
									StringBuilder questionLabel = new StringBuilder();
									numberOfStates = listOfStates.size()-1;
									for (State state : listOfStates) {
										if (dependenciesON){
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
										questionLabel.append(state.getLabel());
										if (listOfStates.lastIndexOf(state)!=numberOfStates)
                                         {
                                            questionLabel.append(separator); // append a separator after each state except for the last one
                                        }
									}
									// old code used when PolytomousKey extended FeatureTree
									//									Representation question = new Representation(null, questionLabel.toString(),null, Language.DEFAULT());
									//									son.addQuestion(question);
									KeyStatement statement = KeyStatement.NewInstance(questionLabel.toString());
									son.setStatement(statement);
									son.setFeature(winnerFeature);
									father.addChild(son);
									featuresLeft.remove(winnerFeature); // unlike for quantitative features, once a categorical one has been used, it cannot be reused in the same branch
									boolean areTheTaxaDiscriminated;
									if (newTaxaCovered.size()==taxaCovered.size()) {
                                        areTheTaxaDiscriminated = true;
                                    } else {
                                        areTheTaxaDiscriminated = false;
                                    }
									buildBranches(son,featuresLeft, newTaxaCovered,areTheTaxaDiscriminated);
								}
							}
						}
					}
				}
			}
			// the features depending on other features are added/removed to/from the features left once the branch is done
			if (dependenciesON){
				for (Feature feature : innapplicables) {
                    featuresLeft.add(feature);
                }
				for (Feature feature : applicables) {
                    featuresLeft.remove(feature);
                }
			}
			// the winner features are put back to the features left once the branch is done
			featuresLeft.add(winnerFeature);
		}
		// if the node is a leaf, the statement contains the list of taxa to which the current leaf leads.
		if (!childrenExist){
			KeyStatement fatherStatement = father.getStatement();
			if (fatherStatement!=null){
				String statementString = fatherStatement.getLabelText(Language.DEFAULT());
				if (statementString !=null && taxaCovered != null){
					String label = statementString + " --> " + taxaCovered.toString();
					fatherStatement.putLabel(Language.DEFAULT(), label);
				}
			}
		}
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
	private void dependenciesScores(Map<Feature,Float> scoreMap, List<Feature> featuresLeft,Set<TaxonDescription> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		// first copies the list of features left
		List<Feature> pseudoFeaturesLeft = featuresLeft.subList(0, featuresLeft.size()-1);
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
	private void mergeBranches(List<State> clique, Map<Set<TaxonDescription>, List<State>> taxonStatesMap){
		boolean stateFound;
		Map.Entry<Set<TaxonDescription>,List<State>> firstBranch=null;
		List<Set<TaxonDescription>> tdToDelete = new ArrayList<Set<TaxonDescription>>();

		if (clique.size()>1){
			Iterator<Map.Entry<Set<TaxonDescription>, List<State>>> it1 = taxonStatesMap.entrySet().iterator();
			while (it1.hasNext()){
				Map.Entry<Set<TaxonDescription>,List<State>> branch = it1.next();
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
			for (Set<TaxonDescription> td : tdToDelete){
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
	 * @param categoricalData the element from which the states are extracted
	 * @param feature the feature corresponding to the CategoricalData
	 * @param taxaCovered the base of taxa considered
	 * @return
	 */
	private Map<Set<TaxonDescription>,List<State>> determineCategoricalStates(List<State> statesDone, CategoricalData categoricalData, Feature feature, Set<TaxonDescription> taxaCovered){
		Map<Set<TaxonDescription>,List<State>> childrenStatesMap = new HashMap<Set<TaxonDescription>,List<State>>();

		List<StateData> stateDatas = categoricalData.getStateData();

		List<State> states = new ArrayList<State>();
		for (StateData sd : stateDatas){
			states.add(sd.getState());
		}

		for (State featureState : states){ // for each state
			if(!statesDone.contains(featureState)){ // if the state hasn't already be considered
				statesDone.add(featureState);
				Set<TaxonDescription> newTaxaCovered = whichTaxa(feature,featureState,taxaCovered); //gets which taxa present this state
				List<State> newStates = childrenStatesMap.get(newTaxaCovered);
				if (newStates==null) { // if no states are associated to these taxa, create a new list
					newStates = new ArrayList<State>();
					childrenStatesMap.put(newTaxaCovered,newStates);
				}
				newStates.add(featureState); // then add the state to the list
			}
		}
		return childrenStatesMap;
	}


	/**
	 * returns the list of taxa from previously covered taxa, which have the state featureState for the given feature
	 *
	 * @param feature
	 * @param featureState
	 * @param taxaCovered
	 * @return
	 */
	private Set<TaxonDescription> whichTaxa(Feature feature, State featureState, Set<TaxonDescription> taxaCovered){
		Set<TaxonDescription> newCoveredTaxa = new HashSet<TaxonDescription>();
		for (TaxonDescription td : taxaCovered){
			Set<DescriptionElementBase> elements = td.getElements();
			for (DescriptionElementBase deb : elements){
				if (deb.isInstanceOf(CategoricalData.class)) {
					if (deb.getFeature().equals(feature)) {
						List<StateData> stateDatas = ((CategoricalData)deb).getStateData();
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
	private Feature lessStatesWinner(Map<Feature,Float> scores, Set<TaxonDescription> taxaCovered){
		int nTaxa = taxaCovered.size();
		if (nTaxa==1) {
            return null;
        }
		float meanScore = defaultMeanScore(nTaxa);
		float bestScore = nTaxa*nTaxa;
		List<Feature> bestFeatures = new ArrayList<Feature>(); // if ever different features have the best score, they are put in this list
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
					Set<State> differentStates = new HashSet<State>();
					for (TaxonDescription td : taxaCovered){
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
				if (lessStates==-1 || numberOfDifferentStates<lessStates){
					lessStates=numberOfDifferentStates;
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
	private Map<Feature,Float> featureScores(List<Feature> featuresLeft, Set<TaxonDescription> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		Map<Feature,Float> scoreMap = new HashMap<Feature,Float>();
		for (Feature feature : featuresLeft){
			if (feature.isSupportsCategoricalData()) {
				scoreMap.put(feature, categoricalFeatureScore(feature,coveredTaxa));
			}
			if (feature.isSupportsQuantitativeData()){
				scoreMap.put(feature, quantitativeFeatureScore(feature,coveredTaxa, quantitativeFeaturesThresholds));
			}
		}
		return scoreMap;
	}

	/**
	 * Since Quantitative features do not have states, unlike Categorical ones, this function determines which taxa,
	 * for a given quantitative feature, present either a lower or higher value than a given threshold.
	 * It returns two Sets of TaxonDescription, one with the taxa under this threshold (taxaBefore) and another one
	 * with the taxa over (taxaAfter).
	 *
	 * @param threshold
	 * @param feature
	 * @param taxa
	 * @param unit
	 * @return
	 */
	private List<Set<TaxonDescription>> determineQuantitativeStates (Float threshold, Feature feature, Set<TaxonDescription> taxa, StringBuilder unit){
		List<Set<TaxonDescription>> list = new ArrayList<Set<TaxonDescription>>();
		Set<TaxonDescription> taxaBefore = new HashSet<TaxonDescription>();
		Set<TaxonDescription> taxaAfter = new HashSet<TaxonDescription>();
		list.add(taxaBefore);
		list.add(taxaAfter);
		for (TaxonDescription td : taxa){
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
	private float quantitativeFeatureScore(Feature feature, Set<TaxonDescription> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		List<Float> allValues = new ArrayList<Float>();
		boolean lowerboundarypresent;
		boolean upperboundarypresent;
		float lowerboundary=0;
		float upperboundary=0;
		for (TaxonDescription td : coveredTaxa){
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
		int taxaBefore=0;
		int taxaAfter=0;
		for (i=0;i<allValues.size()/2;i++) {
			threshold = allValues.get(i*2+1);
			taxaBefore=0;
			taxaAfter=0;
			for (j=0;j<allValues.size()/2;j++) {
				if (allValues.get(j*2+1)<=threshold){
					taxaBefore++;
				}
				if (allValues.get(j*2)>threshold){
					taxaAfter++;
				}
			}
			difference = Math.abs(taxaBefore-taxaAfter);
			if (difference<differenceMin){
				differenceMin=difference;
				bestThreshold = threshold;
			}
		}
		quantitativeFeaturesThresholds.put(feature, bestThreshold);
		int defaultQuantitativeScore=0;
		for (i=0;i<taxaBefore;i++) {
			defaultQuantitativeScore += taxaAfter - i;
		}
		return (defaultQuantitativeScore);
	}



	/**
	 * This function returns the score of a categorical feature.
	 *
	 * @param feature
	 * @param coveredTaxa
	 * @return
	 */
	private float categoricalFeatureScore(Feature feature, Set<TaxonDescription> coveredTaxa){
		int i,j;
		float score =0;
		float power=0;
		TaxonDescription[] coveredTaxaArray = coveredTaxa.toArray(new TaxonDescription[coveredTaxa.size()]); // I did not figure a better way to do this
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
	private void checkDependencies(FeatureNode node){
		if (node.getOnlyApplicableIf()!=null){
			Set<State> addToOAI = node.getOnlyApplicableIf();
			for (State state : addToOAI){
				if (oAIdependencies.containsKey(state)) {
                    oAIdependencies.put(state, new HashSet<Feature>());
                }
				oAIdependencies.get(state).add(node.getFeature());
			}
		}
		if (node.getInapplicableIf()!=null){
			Set<State> addToiI = node.getInapplicableIf();
			for (State state : addToiI){
				if (iIdependencies.containsKey(state)) {
                    iIdependencies.put(state, new HashSet<Feature>());
                }
				iIdependencies.get(state).add(node.getFeature());
			}
		}
		if (node.getChildNodes()!=null) {
			for (FeatureNode fn : node.getChildNodes()){
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
	private float featureScoreAndMerge(Feature feature, Set<TaxonDescription> coveredTaxa, Map<State,Set<State>> exclusions){
		int i,j;
		float score =0;
		float power=0;
		TaxonDescription[] coveredTaxaArray = coveredTaxa.toArray(new TaxonDescription[coveredTaxa.size()]); // I did not figure a better way to do this
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
	private float defaultCategoricalPower(CategoricalData deb1, CategoricalData deb2){
		List<StateData> states1 = deb1.getStateData();
		List<StateData> states2 = deb2.getStateData();
		boolean bool = false;
		Iterator<StateData> stateData1Iterator = states1.iterator() ;
		//		while (!bool && stateData1Iterator.hasNext()) {
		//			Iterator<StateData> stateData2Iterator = states2.iterator() ;
		//			StateData stateData1 = stateData1Iterator.next();
		//			while (!bool && stateData2Iterator.hasNext()) {
		//				bool = stateData1.getState().equals(stateData2Iterator.next().getState()); // checks if the states are the same
		//			}
		//		}
		// one point each time two taxa can be discriminated for a given feature

		boolean checkFeature = false;

		if (!featureDependencies.containsKey(deb1.getFeature())){
			featureDependencies.put(deb1.getFeature(), new HashSet<Feature>());
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
				State state2 = stateData1.getState();
				bool = bool || state1.equals(state2); // checks if the states are the same
			}
		}


		if (bool) {
            return 0;
        } else {
            return 1;
        }
	}

	// old code used when PolytomousKey extended FeatureTree
	//	private void printTree(List<PolytomousKeyNode> fnodes, String spaces){
	//		if (!fnodes.isEmpty()){
	//			level++;
	//			int levelcopy = level;
	//			int j=1;
	//			String delimiter;
	//			String equals = " = ";
	//			String quantitative = "";
	//			String newspaces = spaces.concat("\t");
	//			for (PolytomousKeyNode fnode : fnodes){
	//				if (fnode.getFeature()!=null) {
	//					String state = null;
	//					if (fnode.getQuestion(Language.DEFAULT())!=null) state = fnode.getQuestion(Language.DEFAULT()).getLabel();
	//					if (fnode.getFeature().isSupportsQuantitativeData()) delimiter = quantitative;
	//					else delimiter = equals;
	//					System.out.println(newspaces + levelcopy + " : " + j + " " + fnode.getFeature().getLabel() + delimiter + state);
	//					j++;
	//				}
	//				else { // TODO never read ?
	//					if (fnode.getQuestion(Language.DEFAULT())!=null) System.out.println(newspaces + "-> " + fnode.getQuestion(Language.DEFAULT()).getLabel());
	//				}
	//				printTree(fnode.getChildren(),newspaces);
	//			}
	//		}
	//	}

}

