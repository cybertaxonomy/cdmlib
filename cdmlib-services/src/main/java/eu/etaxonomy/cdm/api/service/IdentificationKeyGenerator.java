package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
<<<<<<< .mine
import eu.etaxonomy.cdm.model.description.FeatureTree;
=======
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.KeyStatement;
>>>>>>> .r10927
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

public class IdentificationKeyGenerator {
	
	static int level=-1; // global variable needed by the printTree function in order to store the level which is being printed
	private FeatureTree polytomousKey_old; // the Identification Key
	private PolytomousKey polytomousKey; // the Identification Key
	private List<Feature> features; // the features used to generate the key
	private Set<TaxonDescription> taxa; // the base of taxa
	private FeatureTree dependenciesTree;
	private Map<TaxonDescription,List<Integer>> paths = new HashMap<TaxonDescription,List<Integer>>(); // for statistics only
	private boolean merge=true;
	
	private Map<State,Set<Feature>> iIdependencies = new HashMap<State,Set<Feature>>();
	private Map<State,Set<Feature>> oAIdependencies = new HashMap<State,Set<Feature>>();
	private boolean dependenciesON = true;
	
	private String before="<";
	private String after=">";
	private String separator = " or ";
	
	/**
	 * Sets the features used to generate the key
	 * 
	 * @param featuresList
	 */
	public void setFeatures(List<Feature> featuresList){
		this.features = featuresList;
	}
	
	/**
	 * Sets the base of taxa
	 * 
	 * @param featuresList
	 */
	public void setTaxa(Set<TaxonDescription> taxaSet){
		this.taxa = taxaSet;
	}
	
	/**
	 * Sets the tree containing the dependencies between states and features
	 * 
	 * @param tree
	 */
	public void setDependencies(FeatureTree tree){
		this.dependenciesTree = tree;
	}
	
	
	/**
	 * Initializes the function buildBranches() with the starting parameters in order to build the key 
	 */
	private void loop_old(){
		polytomousKey_old = FeatureTree.NewInstance();
		FeatureNode root = polytomousKey_old.getRoot();
		buildBranches_old(root,features,taxa);	
	}
	
	/**
	 * Initializes the function buildBranches() with the starting parameters in order to build the key 
	 */
	private void loop(){
		polytomousKey = polytomousKey.NewInstance();
		PolytomousKeyNode root = polytomousKey.getRoot();
		buildBranches(root,features,taxa,false,-1);	
	}
	
	
	/**
	 * Creates the key and prints it
	 */
	public void makeandprint(){
<<<<<<< .mine
		if (dependenciesON && dependenciesTree!=null) checkDependencies(dependenciesTree.getRoot());
		Loop();
		List<FeatureNode> rootlist = new ArrayList<FeatureNode>();
=======
		loop();
		List<PolytomousKeyNode> rootlist = new ArrayList<PolytomousKeyNode>();
>>>>>>> .r10927
		rootlist.add(polytomousKey.getRoot());
		String spaces = new String();
		printTree(rootlist,spaces);
		System.out.println(paths.toString());
	}
	

	/**
	 * Recursive function that builds the branches of the identification key (FeatureTree)
	 * 
	 * @param father the node considered
	 * @param featuresLeft List of features that can be used at this point
	 * @param taxaCovered the taxa left at this point (i.e. that verify the description corresponding to the path leading to this node)
	 * @param mybool
	 */
<<<<<<< .mine
	private void buildBranches(FeatureNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered, boolean mybool, int levelbis){
		int levelhere=levelbis+1;
=======
	private void buildBranches_old(FeatureNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered){
//		// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
//		Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<Feature,Float>();
//		// the scores of the different features are calculated, the thresholds in the same time
//		Map<Feature,Float> scoreMap = featureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
//		// the feature with the best score becomes the one corresponding to the current node
//		Feature winnerFeature = defaultWinner(taxaCovered.size(), scoreMap);
//		// the feature is removed from the list of features available to build the next level of the tree
//		featuresLeft.remove(winnerFeature);
//		// this boolean indicates if the current father node has children or not (i.e. is a leaf or not) ; (a leaf has a "Question" element)
//		boolean childrenExist = false;
//		int i;
//		
//		/************** either the feature supports quantitative data... **************/
//		// NB: in this version, "quantitative features" are dealt with in a dichotomous way
//		if (winnerFeature.isSupportsQuantitativeData()) {
//			// first, get the threshold
//			float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
//			String sign;
//			
//			// then determine which taxa are before and which are after this threshold (dichotomy) in order to create the children of the father node
//			List<Set<TaxonDescription>> quantitativeStates = determineQuantitativeStates(threshold,winnerFeature,taxaCovered);
//			for (i=0;i<2;i++) {
//				Set<TaxonDescription> newTaxaCovered = quantitativeStates.get(i);
//				if (i==0) sign = before; // the first element of the list corresponds to taxa before the threshold
//				else sign = after; // the second to those after
//				if (!(newTaxaCovered.size()==taxaCovered.size())&&newTaxaCovered.size()>0){ // if the taxa are discriminated compared to those of the father node, a child is created
//					childrenExist = true;
//					FeatureNode son = FeatureNode.NewInstance();
//					son.setFeature(winnerFeature);
//					Representation question = new Representation(null, sign + threshold,null, Language.DEFAULT()); // the question attribute is used to store the state of the feature
//					son.addQuestion(question);
//					father.addChild(son);
//					buildBranches(son,featuresLeft, newTaxaCovered);
//				}
//			}
//		}
//		
//		/************** ...or it supports categorical data. **************/
//		// "categorical features" may present several different states, each one of these might correspond to one child
//		List<State> statesDone = new ArrayList<State>();
//		int numberOfStates;
//		if (winnerFeature.isSupportsCategoricalData()) {
//		for (TaxonDescription td : taxaCovered){
//			// go through all the states possible for one feature for the taxa considered
//			DescriptionElementBase debConcerned = null;
//			for (DescriptionElementBase deb : td.getElements()) {
//				if (deb.getFeature().equals(winnerFeature)) debConcerned = deb;
//			}
//			// a map is created, the key being the set of taxa that present the state(s) stored in the corresponding value
//				Map<Set<TaxonDescription>,List<State>> taxonStatesMap = determineCategoricalStates(statesDone,(CategoricalData)debConcerned,winnerFeature,taxaCovered);
//				if (taxonStatesMap!=null && !taxonStatesMap.isEmpty()) { 
//					for (Map.Entry<Set<TaxonDescription>,List<State>> e : taxonStatesMap.entrySet()){
//						Set<TaxonDescription> newTaxaCovered = e.getKey();
//						List<State> listOfStates = e.getValue();
//						if (!(newTaxaCovered.size()==taxaCovered.size())){ // if the taxa are discriminated compared to those of the father node, a child is created
//							childrenExist = true;
//							FeatureNode son = FeatureNode.NewInstance();
//							StringBuilder questionLabel = new StringBuilder();
//							numberOfStates = listOfStates.size()-1;
//							for (State st : listOfStates) {
//								questionLabel.append(st.getLabel());
//								if (listOfStates.lastIndexOf(st)!=numberOfStates) questionLabel.append(separator);
//							}
//							Representation question = new Representation(null, questionLabel.toString(),null, Language.DEFAULT());
//							son.addQuestion(question);
//							son.setFeature(winnerFeature);
//							father.addChild(son);
//							featuresLeft.remove(winnerFeature); // TODO was commented before, why ?
//							buildBranches(son,featuresLeft, newTaxaCovered);
//						}
//					}
//				}
//			}
//		}
//		if (!childrenExist){
//			Representation question = father.getQuestion(Language.DEFAULT());
//			if (question!=null && taxaCovered!= null) question.setLabel(question.getLabel() + " --> " + taxaCovered.toString());
//		}
//		featuresLeft.add(winnerFeature);
	}

	/**
	 * Recursive function that builds the branches of the identification key (FeatureTree)
	 * 
	 * @param father the node considered
	 * @param featuresLeft List of features that can be used at this point
	 * @param taxaCovered the taxa left at this point (i.e. that verify the description corresponding to the path leading to this node)
	 */
	private void buildBranches(PolytomousKeyNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered){
		// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
		Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<Feature,Float>();
		// the scores of the different features are calculated, the thresholds in the same time
		Map<Feature,Float> scoreMap = featureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
		// the feature with the best score becomes the one corresponding to the current node
		Feature winnerFeature = defaultWinner(taxaCovered.size(), scoreMap);
		// the feature is removed from the list of features available to build the next level of the tree
		featuresLeft.remove(winnerFeature);
		// this boolean indicates if the current father node has children or not (i.e. is a leaf or not) ; (a leaf has a "Question" element)
>>>>>>> .r10927
		boolean childrenExist = false;
		
<<<<<<< .mine
		Set<Feature> innapplicables = new HashSet<Feature>();
		Set<Feature> applicables = new HashSet<Feature>();
		
		if (taxaCovered.size()>1) {
			// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
			Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<Feature,Float>();
			// the scores of the different features are calculated, the thresholds in the same time
			Map<Feature,Float> scoreMap = FeatureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
			// the feature with the best score becomes the one corresponding to the current node
//			Feature winnerFeature = DefaultWinner(taxaCovered.size(), scoreMap);
			Feature winnerFeature = LessStatesWinner(taxaCovered.size(), scoreMap, taxaCovered);
			// the feature is removed from the list of features available to build the next level of the tree
			featuresLeft.remove(winnerFeature);
			// this boolean indicates if the current father node has children or not (i.e. is a leaf or not) ; (a leaf has a "Question" element)
			int i;
			/************** either the feature supports quantitative data... **************/
			// NB: in this version, "quantitative features" are dealt with in a dichotomous way
			if (winnerFeature.isSupportsQuantitativeData()) {
				// first, get the threshold
				float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
				String sign;
				StringBuilder unit= new StringBuilder("");
				// then determine which taxa are before and which are after this threshold (dichotomy) in order to create the children of the father node
				List<Set<TaxonDescription>> quantitativeStates = determineQuantitativeStates(threshold,winnerFeature,taxaCovered,unit);
				for (i=0;i<2;i++) {
					Set<TaxonDescription> newTaxaCovered = quantitativeStates.get(i);
					if (i==0) sign = before; // the first element of the list corresponds to taxa before the threshold
					else sign = after; // the second to those after
					if (newTaxaCovered.size()>0 && !((newTaxaCovered.size()==taxaCovered.size()) && mybool)){ // if the taxa are discriminated compared to those of the father node, a child is created
						childrenExist = true;
						FeatureNode son = FeatureNode.NewInstance();
						son.setFeature(winnerFeature);
						Representation question = new Representation(null, " " + sign + " " + threshold +unit,null, Language.DEFAULT()); // the question attribute is used to store the state of the feature
						son.addQuestion(question);
						father.addChild(son);
						boolean newbool;
						if (newTaxaCovered.size()==taxaCovered.size()) newbool = true;
						else newbool = false;
						buildBranches(son,featuresLeft, newTaxaCovered,newbool,levelhere);
					}
=======
		/************** either the feature supports quantitative data... **************/
		// NB: in this version, "quantitative features" are dealt with in a dichotomous way
		if (winnerFeature.isSupportsQuantitativeData()) {
			// first, get the threshold
			float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
			String sign;
			
			// then determine which taxa are before and which are after this threshold (dichotomy) in order to create the children of the father node
			List<Set<TaxonDescription>> quantitativeStates = determineQuantitativeStates(threshold,winnerFeature,taxaCovered);
			for (i=0 ; i<2 ; i++) {
				Set<TaxonDescription> newTaxaCovered = quantitativeStates.get(i);
				if (i==0){
					sign = before; // the first element of the list corresponds to taxa before the threshold
				} else{
					sign = after; // the second to those after
				}
				if ( (newTaxaCovered.size()!=taxaCovered.size()) && newTaxaCovered.size() > 0){ // if the taxa are discriminated compared to those of the father node, a child is created
					childrenExist = true;
					PolytomousKeyNode son = PolytomousKeyNode.NewInstance();
					son.setFeature(winnerFeature);
					KeyStatement statement = KeyStatement.NewInstance(sign + threshold); // the question attribute is used to store the state of the feature
					son.setStatement(statement);
					father.addChild(son);
					buildBranches(son,featuresLeft, newTaxaCovered);
>>>>>>> .r10927
				}
			}
<<<<<<< .mine

			/************** ...or it supports categorical data. **************/
			// "categorical features" may present several different states, each one of these might correspond to one child
			List<State> statesDone = new ArrayList<State>();
			int numberOfStates;
			if (winnerFeature.isSupportsCategoricalData()) {
				for (TaxonDescription td : taxaCovered){
					// go through all the states possible for one feature for the taxa considered
					DescriptionElementBase debConcerned = null;
					for (DescriptionElementBase deb : td.getElements()) {
						if (deb.getFeature().equals(winnerFeature)) debConcerned = deb;
					}
					// a map is created, the key being the set of taxa that present the state(s) stored in the corresponding value
					if (debConcerned!=null) {
						Map<Set<TaxonDescription>,List<State>> taxonStatesMap = determineCategoricalStates(statesDone,(CategoricalData)debConcerned,winnerFeature,taxaCovered);
						// if the merge option is ON, branches with the same discriminative power will be merged (see Vignes & Lebbes, 1989)
						if (merge){
							// see below
							Map<State,Set<State>> exclusions = new HashMap<State,Set<State>>();
							// maps the different states of the winnerFeature to the list of states "incompatible" with it
							FeatureScoreAndMerge(winnerFeature,taxaCovered,exclusions);

							Integer best=null;
							int length;

							// looks for the largest clique, i.e. the state with less exclusions
							while (!exclusions.isEmpty()){
								List<State> clique = returnBestClique(exclusions);
								mergeBranches(clique,taxonStatesMap);
=======
		}
		
		/************** ...or it supports categorical data. **************/
		// "categorical features" may present several different states, each one of these might correspond to one child
		List<State> statesDone = new ArrayList<State>();
		int numberOfStates;
		if (winnerFeature.isSupportsCategoricalData()) {
		for (TaxonDescription td : taxaCovered){
			// go through all the states possible for one feature for the taxa considered
			DescriptionElementBase debConcerned = null;
			for (DescriptionElementBase deb : td.getElements()) {
				if (deb.getFeature().equals(winnerFeature)) debConcerned = deb;
			}
			// a map is created, the key being the set of taxa that present the state(s) stored in the corresponding value
				Map<Set<TaxonDescription>,List<State>> taxonStatesMap = determineCategoricalStates(statesDone,(CategoricalData)debConcerned,winnerFeature,taxaCovered);
				if (taxonStatesMap!=null && !taxonStatesMap.isEmpty()) { 
					for (Map.Entry<Set<TaxonDescription>,List<State>> e : taxonStatesMap.entrySet()){
						Set<TaxonDescription> newTaxaCovered = e.getKey();
						List<State> listOfStates = e.getValue();
						if (!(newTaxaCovered.size()==taxaCovered.size())){ // if the taxa are discriminated compared to those of the father node, a child is created
							childrenExist = true;
							PolytomousKeyNode son = PolytomousKeyNode.NewInstance();
							StringBuilder questionLabel = new StringBuilder();
							numberOfStates = listOfStates.size()-1;
							for (State st : listOfStates) {
								questionLabel.append(st.getLabel());
								if (listOfStates.lastIndexOf(st)!=numberOfStates) questionLabel.append(separator);
>>>>>>> .r10927
							}
<<<<<<< .mine
=======
							KeyStatement statement = KeyStatement.NewInstance(questionLabel.toString());
							son.setStatement(statement);
							son.setFeature(winnerFeature);
							father.addChild(son);
							featuresLeft.remove(winnerFeature); // TODO was commented before, why ?
							buildBranches(son,featuresLeft, newTaxaCovered);
>>>>>>> .r10927
						}
						if (taxonStatesMap!=null && !taxonStatesMap.isEmpty()) { 
							for (Map.Entry<Set<TaxonDescription>,List<State>> e : taxonStatesMap.entrySet()){
								Set<TaxonDescription> newTaxaCovered = e.getKey();
								List<State> listOfStates = e.getValue();
								if ((newTaxaCovered.size()>0) && !((newTaxaCovered.size()==taxaCovered.size()) && mybool)){ // if the taxa are discriminated compared to those of the father node, a child is created
									childrenExist = true;
									FeatureNode son = FeatureNode.NewInstance();
									StringBuilder questionLabel = new StringBuilder();
									numberOfStates = listOfStates.size()-1;
									for (State st : listOfStates) {
										if (dependenciesON){
											if (iIdependencies.get(st)!= null) innapplicables.addAll(iIdependencies.get(st));
											if (oAIdependencies.get(st)!= null) applicables.addAll(oAIdependencies.get(st));
											for (Feature feature : innapplicables) featuresLeft.remove(feature);
											for (Feature feature : applicables) featuresLeft.add(feature);
										}
										questionLabel.append(st.getLabel());
										if (listOfStates.lastIndexOf(st)!=numberOfStates) questionLabel.append(separator);
									}
									Representation question = new Representation(null, questionLabel.toString(),null, Language.DEFAULT());
									son.addQuestion(question);
									son.setFeature(winnerFeature);
									father.addChild(son);
									featuresLeft.remove(winnerFeature); // TODO was commented before, why ?
									boolean newbool;
									if (newTaxaCovered.size()==taxaCovered.size()) newbool = true;
									else newbool = false;
									buildBranches(son,featuresLeft, newTaxaCovered,newbool,levelhere);
								}
							}
						}
					}
				}
			}
			if (dependenciesON){
				for (Feature feature : innapplicables) featuresLeft.add(feature);
				for (Feature feature : applicables) featuresLeft.remove(feature);
			}
			featuresLeft.add(winnerFeature);
		}
		if (! childrenExist){
			KeyStatement fatherStatement = father.getStatement();
			String statementString = fatherStatement.getLabelText(Language.DEFAULT());
			if (statementString !=null && taxaCovered != null){
				String label = statementString + " --> " + taxaCovered.toString();
				fatherStatement.putLabel(label, Language.DEFAULT());
			}
			//			for (TaxonDescription td : taxaCovered){
			//				if (paths.containsKey(td)) paths.get(td).add(levelhere);
			//				else {
			//					List<Integer> pathLength = new ArrayList<Integer>();
			//					pathLength.add(levelhere);
			//					paths.put(td, pathLength);
			//				}
			//			}
		}
	}

	private void mergeBranches(List<State> clique, Map<Set<TaxonDescription>,List<State>> taxonStatesMap){
		int i = 1;
		boolean stateFound;
		Map.Entry<Set<TaxonDescription>,List<State>> firstPair=null;
		List<Set<TaxonDescription>> tdToDelete = new ArrayList<Set<TaxonDescription>>();
		if (clique.size()>1){
			Iterator it1 = taxonStatesMap.entrySet().iterator();
			while (it1.hasNext()){
				Map.Entry<Set<TaxonDescription>,List<State>> pair = (Map.Entry)it1.next();
				Iterator<State> stateIterator = clique.iterator();
				stateFound=false;
				while(stateIterator.hasNext() && stateFound!=true) {
					State state = stateIterator.next();
					if (pair.getValue().contains(state)) {
						stateFound=true;
					}
				}
				if (stateFound==true){
					if (firstPair==null){
						firstPair=pair;
					}
					else {
						firstPair.getKey().addAll(pair.getKey());
						firstPair.getValue().addAll(pair.getValue());
						tdToDelete.add(pair.getKey());
//						taxonStatesMap.remove(pair.getKey()); //remove(pair);
					}
				}
			}
			for (Set<TaxonDescription> td : tdToDelete){
				taxonStatesMap.remove(td);
			}
		}
	}
	
<<<<<<< .mine
	private List<State> returnBestClique (Map<State,Set<State>> exclusions){
		int best=-1;;
		int length;
		List<State> clique = new ArrayList<State>();
		// looks for the largest clique, i.e. the state with less exclusions
		
		State bestState=null;
		for (Iterator it1 = exclusions.entrySet().iterator() ; it1.hasNext();){
			Map.Entry<State,Set<State>> pair = (Map.Entry)it1.next();
			length = pair.getValue().size();
			if ((best==-1) || length<best) {
				best=length;
				bestState = pair.getKey();
			}
		}
		clique.add(bestState);
		exclusions.remove(bestState);
		boolean bool;
		for (Iterator<Map.Entry<State,Set<State>>> it0 = exclusions.entrySet().iterator() ; it0.hasNext();){
			Map.Entry<State,Set<State>> pair = (Map.Entry)it0.next();
			bool = true;
			for (State state : clique) {
				if (pair.getValue().contains(state)) bool = false;
			}
			if (bool){
				clique.add(pair.getKey());
				//exclusions.remove(pair.getKey());
			}
		}
		for (State state : clique) {
			exclusions.remove(state);
		}
		return clique;
	}
	
	
=======
	
>>>>>>> .r10927
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
		
		List<StateData> stateDatas = categoricalData.getStates();
		
		List<State> states = new ArrayList<State>(); // In this function states only are considered, modifiers are not
		for (StateData sd : stateDatas){
			states.add(sd.getState());
		}
		
		for (State featureState : states){
			if(!statesDone.contains(featureState)){
				statesDone.add(featureState);
				
				Set<TaxonDescription> newTaxaCovered = whichTaxa(feature,featureState,taxaCovered);
				List<State> newStates = childrenStatesMap.get(newTaxaCovered);
				if (newStates==null) {
					newStates = new ArrayList<State>();
					
					childrenStatesMap.put(newTaxaCovered,newStates);
				}
				newStates.add(featureState);
			}
	}
		return childrenStatesMap;
	}
	
	// returns the list of taxa from previously covered taxa, which have the state featureState for the feature feature 
	private Set<TaxonDescription> whichTaxa(Feature feature, State featureState, Set<TaxonDescription> taxaCovered){
		Set<TaxonDescription> newCoveredTaxa = new HashSet<TaxonDescription>();
		for (TaxonDescription td : taxaCovered){
			Set<DescriptionElementBase> elements = td.getElements();
			for (DescriptionElementBase deb : elements){
				if (deb.isInstanceOf(CategoricalData.class)) {
					if (deb.getFeature().equals(feature)) {
						List<StateData> stateDatas = ((CategoricalData)deb).getStates();
						for (StateData sd : stateDatas) {
							if (sd.getState().equals(featureState))
								newCoveredTaxa.add(td);
						}
					}
				} 
			}
		}
		return newCoveredTaxa;
	}
	
<<<<<<< .mine
	//change names ; merge with Default (Default takes the first one of the list)
	private Feature LessStatesWinner(int nTaxa, Map<Feature,Float> scores, Set<TaxonDescription> taxaCovered){
		if (nTaxa==1) return null;
		float meanScore = DefaultMeanScore(nTaxa);
		float bestScore = nTaxa*nTaxa;
		List<Feature> bestFeatures = new ArrayList<Feature>();
		Feature bestFeature = null;
		Iterator it = scores.entrySet().iterator();
		float newScore;
		while (it.hasNext()){
			Map.Entry<Feature,Float> pair = (Map.Entry)it.next();
			if (pair.getValue()!=null){
				newScore = Math.abs((Float)pair.getValue()-meanScore);
				if (newScore < bestScore){
					bestFeatures.clear();
					bestFeatures.add((Feature)pair.getKey());
					bestScore = newScore;
				}
				else if (newScore==bestScore){
					bestFeatures.add((Feature)pair.getKey());
				}
			}
		}
		if (bestFeatures.size()==1) {
			return bestFeatures.get(0);
		}
		else {
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
									List<StateData> stateDatas = catdat.getStates();
									for (StateData sd : stateDatas) {
										differentStates.add(sd.getState());
									}
								}
							}
						} 
					}
				numberOfDifferentStates=differentStates.size();
				}
				else if (feature.isSupportsQuantitativeData()){
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
	
	private Feature DefaultWinner(int nTaxa, Map<Feature,Float> scores){
		if (nTaxa==1) return null;
		float meanScore = DefaultMeanScore(nTaxa);
		float bestScore = nTaxa*nTaxa;
=======
	//change names
	private Feature defaultWinner(int nTaxons, Map<Feature,Float> scores){
		float meanScore = defaultMeanScore(nTaxons);
		float bestScore = nTaxons*nTaxons;
>>>>>>> .r10927
		Feature feature = null;
		Iterator it = scores.entrySet().iterator();
		float newScore;
		while (it.hasNext()){
			Map.Entry<Feature,Float> pair = (Map.Entry)it.next();
			if (pair.getValue()!=null){
				newScore = Math.abs((Float)pair.getValue()-meanScore);
				if (newScore < bestScore){
					feature = (Feature)pair.getKey();
					bestScore = newScore;
				}
			}
		}
		return feature;
	}
	
	// rutiliser et vrif si rien de trop <- FIXME please do not comment in french or at least use proper file encoding
	private float defaultMeanScore(int nTaxons){
		int i;
		float score=0;
		for (i=1;i<nTaxons;i++){
			score = score + Math.round((float)(i+1/2));
		}
		return score;
	}
	
	private Map<Feature,Float> featureScores(List<Feature> featuresLeft, Set<TaxonDescription> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		Map<Feature,Float> scoreMap = new HashMap<Feature,Float>();
		for (Feature feature : featuresLeft){
			if (feature.isSupportsCategoricalData()) {
<<<<<<< .mine
				scoreMap.put(feature, categoricalFeatureScore(feature,coveredTaxa));
=======
				scoreMap.put(feature, featureScore(feature,coveredTaxa));
>>>>>>> .r10927
			}
			if (feature.isSupportsQuantitativeData()){
				scoreMap.put(feature, quantitativeFeatureScore(feature,coveredTaxa, quantitativeFeaturesThresholds));
			}
		}
		return scoreMap;
	}
	
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
							// DONT FORGET sample size, MEAN etc
							if (type.equals(StatisticalMeasure.MAX()) || type.equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY()) || type.equals(StatisticalMeasure.AVERAGE())) {
								if (smv.getValue()>threshold) taxaAfter.add(td);
							}
							if (type.equals(StatisticalMeasure.MIN()) || type.equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY()) || type.equals(StatisticalMeasure.AVERAGE())) {
								if (smv.getValue()<=threshold) taxaBefore.add(td);
							}
						}
					}
				}
			}
		}
//		if (unit==null) unit=new String("");
		return list;
	}
	
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
							// TODO improve
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
				if (allValues.get(j*2+1)<=threshold) taxaBefore++;
				if (allValues.get(j*2)>threshold) taxaAfter++;
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
		return (float)(defaultQuantitativeScore);
	}
	
<<<<<<< .mine
	private float categoricalFeatureScore(Feature feature, Set<TaxonDescription> coveredTaxa){
=======
	private float featureScore(Feature feature, Set<TaxonDescription> coveredTaxa){
>>>>>>> .r10927
		int i,j;
		float score =0;
		float power=0;
		TaxonDescription[] coveredTaxaArray = coveredTaxa.toArray(new TaxonDescription[coveredTaxa.size()]); // I did not figure a better way to do this
		for (i=0 ; i<coveredTaxaArray.length; i++){
			Set<DescriptionElementBase> elements1 = coveredTaxaArray[i].getElements();
			DescriptionElementBase deb1 = null;
			for (DescriptionElementBase deb : elements1){
				if (deb.getFeature().equals(feature)) deb1 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
			}
			for (j=i+1 ; j< coveredTaxaArray.length ; j++){
				Set<DescriptionElementBase> elements2 = coveredTaxaArray[j].getElements();
				DescriptionElementBase deb2 = null;
				for (DescriptionElementBase deb : elements2){
					if (deb.getFeature().equals(feature)) deb2 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
				}
<<<<<<< .mine
				power = DefaultPower(deb1,deb2);
				score = score + power;
=======
				score = score + defaultPower(deb1,deb2);
>>>>>>> .r10927
			}
		}
		return score;
		}
	
<<<<<<< .mine
	private void checkDependencies(FeatureNode node){
		if (node.getOnlyApplicableIf()!=null){
			Set<State> addToOAI = node.getOnlyApplicableIf();
			for (State state : addToOAI){
				if (oAIdependencies.containsKey(state)) oAIdependencies.put(state, new HashSet<Feature>());
				oAIdependencies.get(state).add(node.getFeature());
			}
		}
		if (node.getInapplicableIf()!=null){
			Set<State> addToiI = node.getInapplicableIf();
			for (State state : addToiI){
				if (iIdependencies.containsKey(state)) iIdependencies.put(state, new HashSet<Feature>());
				iIdependencies.get(state).add(node.getFeature());
			}
		}
		if (node.getChildren()!=null) {
			for (FeatureNode fn : node.getChildren()){
				checkDependencies(fn);
			}
		}
	}
	
	private float FeatureScoreAndMerge(Feature feature, Set<TaxonDescription> coveredTaxa, Map<State,Set<State>> exclusions){
		int i,j;
		float score =0;
		float power=0;
		TaxonDescription[] coveredTaxaArray = coveredTaxa.toArray(new TaxonDescription[coveredTaxa.size()]); // I did not figure a better way to do this
		for (i=0 ; i<coveredTaxaArray.length; i++){
			Set<DescriptionElementBase> elements1 = coveredTaxaArray[i].getElements();
			DescriptionElementBase deb1 = null;
			for (DescriptionElementBase deb : elements1){
				if (deb.getFeature().equals(feature)) deb1 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
			}
			for (j=i+1 ; j< coveredTaxaArray.length ; j++){
				Set<DescriptionElementBase> elements2 = coveredTaxaArray[j].getElements();
				DescriptionElementBase deb2 = null;
				for (DescriptionElementBase deb : elements2){
					if (deb.getFeature().equals(feature)) deb2 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
				}
				power = DefaultPower(deb1,deb2);
				score = score + power;
				if (power>0) // if there is no state in common between deb1 and deb2
				{
					CategoricalData cat1 = (CategoricalData)deb1;
					CategoricalData cat2 = (CategoricalData)deb2;
					for (StateData statedata1 : cat1.getStates()){
						State state1 = statedata1.getState();
						if (!exclusions.containsKey(state1)) exclusions.put(state1, new HashSet<State>());
						for (StateData statedata2 : cat2.getStates()){
							State state2 = statedata2.getState();
							if (!exclusions.containsKey(state2)) exclusions.put(state2, new HashSet<State>());
							exclusions.get(state1).add(state2);
							exclusions.get(state2).add(state1);
						}
					}
				}
			}
		}
		return score;
		}
	
	private float DefaultPower(DescriptionElementBase deb1, DescriptionElementBase deb2){
=======
	private float defaultPower(DescriptionElementBase deb1, DescriptionElementBase deb2){
>>>>>>> .r10927
		if (deb1==null || deb2==null) {
			return -1; //what if the two taxa don't have this feature in common ?
		}
		if ((deb1.isInstanceOf(CategoricalData.class))&&(deb2.isInstanceOf(CategoricalData.class))) {
			return defaultCategoricalPower((CategoricalData)deb1, (CategoricalData)deb2);
		}
		else return 0;
	}
	
	private float defaultCategoricalPower(CategoricalData deb1, CategoricalData deb2){
		List<StateData> states1 = deb1.getStates();
		List<StateData> states2 = deb2.getStates();
		boolean bool = false;
		Iterator<StateData> stateData1Iterator = states1.iterator() ;
		while (!bool && stateData1Iterator.hasNext()) {
			Iterator<StateData> stateData2Iterator = states2.iterator() ;
			StateData stateData1 = stateData1Iterator.next();
			//bool = states2.contains(strIterator.next());
			while (!bool && stateData2Iterator.hasNext()) {
				bool = stateData1.getState().equals(stateData2Iterator.next().getState()); // checks if the states are the same
			}
			// modifiers not taken into account for this default power
		}
		// one point each time two taxa can be discriminated for a given feature
		if (bool) return 0;
		else return 1;
	}
	
	private void printTree(List<PolytomousKeyNode> polytomousKeyNodes, String spaces){
		if (! polytomousKeyNodes.isEmpty()){
			level++;
			int levelcopy = level;
			int j=1;
			String delimiter;
			String equals = " = ";
			String quantitative = "";
			String newspaces = spaces.concat("\t");
			for (PolytomousKeyNode polytomousKeyNode : polytomousKeyNodes){
				if (polytomousKeyNode.getQuestion() != null) {
					String state = null;
<<<<<<< .mine
					if (fnode.getQuestion(Language.DEFAULT())!=null) state = fnode.getQuestion(Language.DEFAULT()).getLabel();
					if (fnode.getFeature().isSupportsQuantitativeData()) delimiter = quantitative;
					else delimiter = equals;
					System.out.println(newspaces + levelcopy + " : " + j + " " + fnode.getFeature().getLabel() + delimiter + state);
=======
					if (polytomousKeyNode.getStatement().getLabel(Language.DEFAULT() ) != null){
						state = polytomousKeyNode.getStatement().getLabelText(Language.DEFAULT());
					}
					System.out.println(newspaces + levelcopy + " : " + j + " " + polytomousKeyNode.getQuestion().getLabelText(Language.DEFAULT()) + " = " + state);
>>>>>>> .r10927
					j++;
				}
				else { // TODO never read ?
					if (polytomousKeyNode.getStatement().getLabel(Language.DEFAULT() ) != null){
						System.out.println(newspaces + "-> " + polytomousKeyNode.getStatement().getLabelText(Language.DEFAULT()));
					}
				}
				printTree(polytomousKeyNode.getChildren(),newspaces);
			}
		}
	}

}

