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
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

public class IdentificationKeyGenerator {
	
	static int level=-1; // global variable needed by the printTree function in order to store the level which is being printed
	private PolytomousKey polytomousKey; // the Identification Key
	private List<Feature> features; // the features used to generate the key
	private Set<TaxonDescription> taxa; // the base of taxa
	
	private String before="<";
	private String after=">";
	private String separator = ", ";
	
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
	 * Initializes the function buildBranches() with the starting parameters in order to build the key 
	 */
	private void loop(){
		polytomousKey = PolytomousKey.NewInstance();
		FeatureNode root = polytomousKey.getRoot();
		buildBranches(root,features,taxa);	
	}
	
	
	/**
	 * Creates the key and prints it
	 */
	public void makeandprint(){
		loop();
		List<FeatureNode> rootlist = new ArrayList<FeatureNode>();
		rootlist.add(polytomousKey.getRoot());
		String spaces = new String();
		printTree(rootlist,spaces);
	}
	

	/**
	 * Recursive function that builds the branches of the identification key (FeatureTree)
	 * 
	 * @param father the node considered
	 * @param featuresLeft List of features that can be used at this point
	 * @param taxaCovered the taxa left at this point (i.e. that verify the description corresponding to the path leading to this node)
	 */
	private void buildBranches(FeatureNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered){
		// this map stores the thresholds giving the best dichotomy of taxa for the corresponding feature supporting quantitative data
		Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<Feature,Float>();
		// the scores of the different features are calculated, the thresholds in the same time
		Map<Feature,Float> scoreMap = featureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
		// the feature with the best score becomes the one corresponding to the current node
		Feature winnerFeature = defaultWinner(taxaCovered.size(), scoreMap);
		// the feature is removed from the list of features available to build the next level of the tree
		featuresLeft.remove(winnerFeature);
		// this boolean indicates if the current father node has children or not (i.e. is a leaf or not) ; (a leaf has a "Question" element)
		boolean childrenExist = false;
		int i;
		
		/************** either the feature supports quantitative data... **************/
		// NB: in this version, "quantitative features" are dealt with in a dichotomous way
		if (winnerFeature.isSupportsQuantitativeData()) {
			// first, get the threshold
			float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
			String sign;
			
			// then determine which taxa are before and which are after this threshold (dichotomy) in order to create the children of the father node
			List<Set<TaxonDescription>> quantitativeStates = determineQuantitativeStates(threshold,winnerFeature,taxaCovered);
			for (i=0;i<2;i++) {
				Set<TaxonDescription> newTaxaCovered = quantitativeStates.get(i);
				if (i==0) sign = before; // the first element of the list corresponds to taxa before the threshold
				else sign = after; // the second to those after
				if (!(newTaxaCovered.size()==taxaCovered.size())&&newTaxaCovered.size()>0){ // if the taxa are discriminated compared to those of the father node, a child is created
					childrenExist = true;
					FeatureNode son = FeatureNode.NewInstance();
					son.setFeature(winnerFeature);
					Representation question = new Representation(null, sign + threshold,null, Language.DEFAULT()); // the question attribute is used to store the state of the feature
					son.addQuestion(question);
					father.addChild(son);
					buildBranches(son,featuresLeft, newTaxaCovered);
				}
			}
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
							FeatureNode son = FeatureNode.NewInstance();
							StringBuilder questionLabel = new StringBuilder();
							numberOfStates = listOfStates.size()-1;
							for (State st : listOfStates) {
								questionLabel.append(st.getLabel());
								if (listOfStates.lastIndexOf(st)!=numberOfStates) questionLabel.append(separator);
							}
							Representation question = new Representation(null, questionLabel.toString(),null, Language.DEFAULT());
							son.addQuestion(question);
							son.setFeature(winnerFeature);
							father.addChild(son);
							featuresLeft.remove(winnerFeature); // TODO was commented before, why ?
							buildBranches(son,featuresLeft, newTaxaCovered);
						}
					}
				}
			}
		}
		if (!childrenExist){
			Representation question = father.getQuestion(Language.DEFAULT());
			if (question!=null && taxaCovered!= null) question.setLabel(question.getLabel() + " --> " + taxaCovered.toString());
		}
		featuresLeft.add(winnerFeature);
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
		
		List<StateData> stateDatas = categoricalData.getStates();
		
		List<State> states = new ArrayList<State>(); // In this function states only are considered, modifiers are not
		for (StateData sd : stateDatas){
			states.add(sd.getState());
		}
		
		for (State featureState : states){
			if(!statesDone.contains(featureState)){
				statesDone.add(featureState);
				
				StateData sd = new StateData();
				sd.setState(featureState);
				//((CategoricalData)debsDone.get(0)).addState(sd);// A VOIR
				
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
	
	//change names
	private Feature defaultWinner(int nTaxons, Map<Feature,Float> scores){
		float meanScore = defaultMeanScore(nTaxons);
		float bestScore = nTaxons*nTaxons;
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
		if (!(feature.getLabel()==null)){
//			System.out.println(feature.getLabel() + bestScore);
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
				scoreMap.put(feature, featureScore(feature,coveredTaxa));
			}
			if (feature.isSupportsQuantitativeData()){
				scoreMap.put(feature, quantitativeFeatureScore(feature,coveredTaxa, quantitativeFeaturesThresholds));
			}
		}
		return scoreMap;
	}
	
	private List<Set<TaxonDescription>> determineQuantitativeStates (Float threshold, Feature feature, Set<TaxonDescription> taxa){
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
						Set<StatisticalMeasurementValue> values = qd.getStatisticalValues();
						for (StatisticalMeasurementValue smv : values){
							StatisticalMeasure type = smv.getType();
							// DONT FORGET sample size, MEAN etc
							if (type.equals(StatisticalMeasure.MAX()) || type.equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY())) {
								if (smv.getValue()>=threshold) taxaAfter.add(td);
							}
							if (type.equals(StatisticalMeasure.MIN()) || type.equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY())) {
								if (smv.getValue()<=threshold) taxaBefore.add(td);
							}
						}
					}
				}
			}
		}
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
				if (allValues.get(j*2)>=threshold) taxaAfter++;
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
		System.out.println(taxaBefore + ", " + taxaAfter + ", " +defaultQuantitativeScore);
		return (float)(defaultQuantitativeScore);
	}
	
	private float featureScore(Feature feature, Set<TaxonDescription> coveredTaxa){
		int i,j;
		float score =0;
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
				score = score + defaultPower(deb1,deb2);
			}
		}
		return score;
		}
	
	private float defaultPower(DescriptionElementBase deb1, DescriptionElementBase deb2){
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
		// one point each time two taxa have at least a state in common for a given feature
		if (bool) return 0;
		else return 1;
	}
	
	private void printTree(List<FeatureNode> fnodes, String spaces){
		if (!fnodes.isEmpty()){
			level++;
			int levelcopy = level;
			int j=1;
			String newspaces = spaces.concat("\t");
			for (FeatureNode fnode : fnodes){
				if (fnode.getFeature()!=null) {
					String state = null;
					if (fnode.getQuestion(Language.DEFAULT())!=null) state = fnode.getQuestion(Language.DEFAULT()).getLabel();
					System.out.println(newspaces + levelcopy + " : " + j + " " + fnode.getFeature().getLabel() + " = " + state);
					j++;
				}
				else { // TODO never read ?
					if (fnode.getQuestion(Language.DEFAULT())!=null) System.out.println(newspaces + "-> " + fnode.getQuestion(Language.DEFAULT()).getLabel());
				}
				printTree(fnode.getChildren(),newspaces);
			}
		}
	}

}

