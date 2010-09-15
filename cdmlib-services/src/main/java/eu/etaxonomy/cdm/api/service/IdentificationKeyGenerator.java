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
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;

public class IdentificationKeyGenerator {
	
	static int level=-1;
	private PolytomousKey polyto;
	private List<Feature> features;
	private Set<TaxonDescription> taxa;
	
	public void setFeatures(List<Feature> featuresList){
		this.features = featuresList;
	}
	
	public void setTaxa(Set<TaxonDescription> taxaSet){
		this.taxa = taxaSet;
	}
	
	
	public void makeandprint(){
		Boucle();
		List<FeatureNode> rootlist = new ArrayList<FeatureNode>();
		rootlist.add(polyto.getRoot());
		String spaces = new String();
		printTree2(rootlist,spaces);
	}
	
	
	private void Boucle(){
		polyto = PolytomousKey.NewInstance();
		FeatureNode root = polyto.getRoot();
		buildBranches(root,features,taxa);
		System.out.println();
		
	}
	

	private void buildBranches(FeatureNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered){
		//System.out.println(featuresLeft);
		//System.out.println("FL size : " + featuresLeft.size() + " and taxa size : " + taxaCovered.size());
		//System.out.println(taxaCovered);
		List<DescriptionElementBase> debsDone = new ArrayList<DescriptionElementBase>();
		List<State> statesDone = new ArrayList<State>(); // ATTENTION ONLY FOR CAT
		//Map<Set<TaxonDescription>,DescriptionElementBase> floor = new HashMap<Set<TaxonDescription>,DescriptionElementBase>(); // local variable never read
		
		Map<Feature,Float> scoreMap = FeatureScores(featuresLeft, taxaCovered);
		//System.out.println(scoreMap);
		Feature winnerFeature = DefaultWinner(taxaCovered.size(), scoreMap);
		//System.out.println(winnerFeature.getLabel());
		featuresLeft.remove(winnerFeature);
		boolean childrenExist = false;
		
		for (TaxonDescription td : taxaCovered){ // look for the different states
			DescriptionElementBase debConcerned = null;
				for (DescriptionElementBase deb : td.getElements()) {
					if (deb.getFeature().equals(winnerFeature)) debConcerned = deb;
				}
				//if deb!= null
				
				Map<Set<TaxonDescription>,List<State>> taxonStatesMap = runOverStates(statesDone,debConcerned,winnerFeature,taxaCovered); // /!\ ATTENTION not working yet for quantitative data
				if (taxonStatesMap!=null && !taxonStatesMap.isEmpty()) {
				for (Map.Entry<Set<TaxonDescription>,List<State>> e : taxonStatesMap.entrySet()){
					Set<TaxonDescription> newTaxaCovered = e.getKey();
					List<State> list = e.getValue(); // for the tree
					if (!(newTaxaCovered.size()==taxaCovered.size())){// if the remaining taxa are still discriminated, continue // >1 USEFUL ?
						//System.out.println(taxaCovered.size());
						childrenExist = true;
							FeatureNode son = FeatureNode.NewInstance();
						StringBuilder questionLabel = new StringBuilder();
						for (State st : list) questionLabel.append(st.getLabel());
						Representation question = new Representation(null, questionLabel.toString(),null, Language.DEFAULT());
						son.addQuestion(question);
						son.setFeature(winnerFeature);
							father.addChild(son);
						//List<Feature> newFeaturesLeft = new LinkedList<Feature>(featuresLeft); // replaced by featuresLeft.remove(winnerFeature)
						//newFeaturesLeft.remove(winnerFeature);
						buildBranches(son,featuresLeft, newTaxaCovered);
						}
						else {
//						FeatureNode son = FeatureNode.NewInstance();
//						Representation question = new Representation(null, taxaCovered.toString(),null, Language.DEFAULT());
//						son.addQuestion(question);
//						father.addChild(son);// a leaf is reached
						}
					}
			}
		}
		if (!childrenExist){
			Representation question = father.getQuestion(Language.DEFAULT());
			question.setLabel(question.getLabel() + " --> " + taxaCovered.toString());
	}
		featuresLeft.add(winnerFeature);
		//loop over the floor, if new = old taxa -> leaf ; else -> node + loop
	
	}
	
	private Map<Set<TaxonDescription>,List<State>> runOverStates(List<State> statesDone, DescriptionElementBase deb, Feature winnerFeature, Set<TaxonDescription> taxaCovered){
		if (deb==null){
			return null;
		}
		if (deb.isInstanceOf(CategoricalData.class)) {
			return rOSCategoricalDefault(statesDone, (CategoricalData)deb, winnerFeature, taxaCovered);
		}
		else return null;
	}
	
	private Map<Set<TaxonDescription>,List<State>> rOSCategoricalDefault(List<State> statesDone, CategoricalData categoricalData, Feature winnerFeature, Set<TaxonDescription> taxaCovered){
		Map<Set<TaxonDescription>,List<State>> childrenStatesMap = new HashMap<Set<TaxonDescription>,List<State>>();
		
		List<StateData> stateDatas = categoricalData.getStates();
		
		List<State> states = new ArrayList<State>(); // In this function STATES ONLY ARE CONSIDERED, MODIFIERS ARE NOT
		for (StateData sd : stateDatas){
			states.add(sd.getState());
		}
		for (StateData sd : stateDatas){
			states.add(sd.getState());
		}
		
		for (State featureState : states){
			if(!statesDone.contains(featureState)){
				statesDone.add(featureState);
				
				StateData sd = new StateData();
				sd.setState(featureState);
				//((CategoricalData)debsDone.get(0)).addState(sd);// A VOIR
				
				Set<TaxonDescription> newTaxaCovered = whichTaxa(winnerFeature,featureState,taxaCovered);
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
	private Feature DefaultWinner(int nTaxons, Map<Feature,Float> scores){
		float meanScore = DefaultMeanScore(nTaxons);
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
		return feature;
	}
	
	// rŽutiliser et vŽrif si rien de trop
	private float DefaultMeanScore(int nTaxons){
		int i;
		float score=0;
		for (i=1;i<nTaxons;i++){
			score = score + Math.round((float)(i+1/2));
		}
		return score;
	}
	
	private Map<Feature,Float> FeatureScores(List<Feature> featuresLeft, Set<TaxonDescription> coveredTaxa){
		Map<Feature,Float> scoreMap = new HashMap<Feature,Float>();
		for (Feature feature : featuresLeft){
				scoreMap.put(feature, FeatureScore(feature,coveredTaxa));
			}
		return scoreMap;
	}
	
	private float FeatureScore(Feature featureIndex, Set<TaxonDescription> coveredTaxa){
		int i,j;
		float score =0;
		TaxonDescription[] coveredTaxaArray = coveredTaxa.toArray(new TaxonDescription[coveredTaxa.size()]); // I did not figure a better way to do this
		for (i=0 ; i<coveredTaxaArray.length; i++){
			Set<DescriptionElementBase> elements1 = coveredTaxaArray[i].getElements();
			DescriptionElementBase deb1 = null;
			for (DescriptionElementBase deb : elements1){
				if (deb.getFeature().equals(featureIndex)) deb1 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
			}
			for (j=i+1 ; j< coveredTaxaArray.length ; j++){
				Set<DescriptionElementBase> elements2 = coveredTaxaArray[j].getElements();
				DescriptionElementBase deb2 = null;
				for (DescriptionElementBase deb : elements2){
					if (deb.getFeature().equals(featureIndex)) deb2 = deb; // finds the DescriptionElementBase corresponding to the concerned Feature
		}
				score = score + DefaultPower(deb1,deb2);
			}
		}
		return score;
		}
	
	private float DefaultPower(DescriptionElementBase deb1, DescriptionElementBase deb2){
		if (deb1==null || deb2==null) {
			return -1; //what if the two taxa don't have this feature in common ?
		}
		if ((deb1.isInstanceOf(CategoricalData.class))&&(deb2.isInstanceOf(CategoricalData.class))) {
			return DefaultCategoricalPower((CategoricalData)deb1, (CategoricalData)deb2);
		}
		else return 0;
	}
	
	private float DefaultCategoricalPower(CategoricalData deb1, CategoricalData deb2){
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
	
	private void printTree(List<FeatureNode> fnodes){
		Feature featureSpace = Feature.NewInstance(null, ";", null);
		FeatureNode featureNodeSpace = FeatureNode.NewInstance(featureSpace);
		List<FeatureNode> children = new ArrayList<FeatureNode>();
		for (FeatureNode fnode : fnodes){
			if (fnode.getFeature()!=null) {
			//System.out.print(fnode.getFeature().getLabel() + " ");
			}
			List<FeatureNode> childrenbis = fnode.getChildren();
			for (FeatureNode fnodebis : childrenbis){
				children.add(fnodebis);
			}
			if (children.size()>0) {children.add(featureNodeSpace);}
		}
		//System.out.println("\n-----");
		if (children.size()>0){
			printTree(children);
		}
	}
	
	private void printTree2(List<FeatureNode> fnodes, String spaces){
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
				else {
					if (fnode.getQuestion(Language.DEFAULT())!=null) System.out.println(newspaces + "-> " + fnode.getQuestion(Language.DEFAULT()).getLabel());
				}
				printTree2(fnode.getChildren(),newspaces);
			}
		}
	}

}
