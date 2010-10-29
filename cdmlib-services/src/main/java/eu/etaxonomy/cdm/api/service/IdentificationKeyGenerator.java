package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
		
	}
	

	private void buildBranches(FeatureNode father, List<Feature> featuresLeft, Set<TaxonDescription> taxaCovered){
		List<DescriptionElementBase> debsDone = new ArrayList<DescriptionElementBase>();
		List<State> statesDone = new ArrayList<State>(); // ATTENTION ONLY FOR CAT
		List<QuantitativeData> quantitativeStatesDone = new ArrayList<QuantitativeData>();
		
		//Map<Set<TaxonDescription>,DescriptionElementBase> floor = new HashMap<Set<TaxonDescription>,DescriptionElementBase>(); // local variable never read
		
		Map<Feature,Float> quantitativeFeaturesThresholds = new HashMap<Feature,Float>();
		Map<Feature,Float> scoreMap = FeatureScores(featuresLeft, taxaCovered, quantitativeFeaturesThresholds);
		Feature winnerFeature = DefaultWinner(taxaCovered.size(), scoreMap);
		featuresLeft.remove(winnerFeature);
		boolean childrenExist = false;
		int i;
		String sign;
		String before="<";
		String after=">";
		
		if (winnerFeature.isSupportsQuantitativeData()) {
			float threshold = quantitativeFeaturesThresholds.get(winnerFeature);
			List<Set<TaxonDescription>> taxonQuantitativeStates = runOverQuantitativeStates(threshold,winnerFeature,taxaCovered);
			for (i=0;i<2;i++) {
				Set<TaxonDescription> set = taxonQuantitativeStates.get(i);
				if (i==1) sign = before;
				else sign = after;
				if (!(set.size()==taxaCovered.size())&&set.size()>0){
					FeatureNode son = FeatureNode.NewInstance();
					Representation question = new Representation(null, sign + threshold,null, Language.DEFAULT());
					son.addQuestion(question);
					son.setFeature(winnerFeature);
					father.addChild(son);
					//List<Feature> newFeaturesLeft = new LinkedList<Feature>(featuresLeft); // replaced by featuresLeft.remove(winnerFeature)
					//newFeaturesLeft.remove(winnerFeature);
					buildBranches(son,featuresLeft, set);
				}
			}
			// On a déjà les états normalements
			// on passe les états on regarde les taxons concernés
			// on fait un fils
		}
		
		
		if (winnerFeature.isSupportsCategoricalData()) {
		for (TaxonDescription td : taxaCovered){ // look for the different states
			DescriptionElementBase debConcerned = null;
			for (DescriptionElementBase deb : td.getElements()) {
				if (deb.getFeature().equals(winnerFeature)) debConcerned = deb;
			}
				Map<Set<TaxonDescription>,List<State>> taxonStatesMap = runOverStates(statesDone,debConcerned,winnerFeature,taxaCovered); // /!\ ATTENTION not working yet for quantitative data
				if (taxonStatesMap!=null && !taxonStatesMap.isEmpty()) {
					for (Map.Entry<Set<TaxonDescription>,List<State>> e : taxonStatesMap.entrySet()){
						Set<TaxonDescription> newTaxaCovered = e.getKey();
						List<State> list = e.getValue(); // for the tree
						if (!(newTaxaCovered.size()==taxaCovered.size())){// if the remaining taxa are still discriminated, continue
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
		}
		if (!childrenExist){
			Representation question = father.getQuestion(Language.DEFAULT());
			if (question!=null && taxaCovered!= null) question.setLabel(question.getLabel() + " --> " + taxaCovered.toString());
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
//		for (StateData sd : stateDatas){
//			states.add(sd.getState());
//		}
		
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
		if (!(feature.getLabel()==null)){
//			System.out.println(feature.getLabel() + bestScore);
		}
		return feature;
	}
	
	// rutiliser et vrif si rien de trop <- FIXME please do not comment in french or at least use proper file encoding
	private float DefaultMeanScore(int nTaxons){
		int i;
		float score=0;
		for (i=1;i<nTaxons;i++){
			score = score + Math.round((float)(i+1/2));
		}
		return score;
	}
	
	private Map<Feature,Float> FeatureScores(List<Feature> featuresLeft, Set<TaxonDescription> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
		Map<Feature,Float> scoreMap = new HashMap<Feature,Float>();
		for (Feature feature : featuresLeft){
			if (feature.isSupportsCategoricalData()) {
				scoreMap.put(feature, FeatureScore(feature,coveredTaxa));
			}
			if (feature.isSupportsQuantitativeData()){
				scoreMap.put(feature, QuantitativeFeatureScore(feature,coveredTaxa, quantitativeFeaturesThresholds));
			}
		}
		return scoreMap;
	}
	
	private List<Set<TaxonDescription>> runOverQuantitativeStates (Float threshold, Feature feature, Set<TaxonDescription> taxa){
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
	
	private float QuantitativeFeatureScore(Feature feature, Set<TaxonDescription> coveredTaxa, Map<Feature,Float> quantitativeFeaturesThresholds){
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
				if (allValues.get(j*2)<=threshold) taxaBefore++;
				if (allValues.get(j*2+1)>=threshold) taxaAfter++;
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
	
	private float FeatureScore(Feature feature, Set<TaxonDescription> coveredTaxa){
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

