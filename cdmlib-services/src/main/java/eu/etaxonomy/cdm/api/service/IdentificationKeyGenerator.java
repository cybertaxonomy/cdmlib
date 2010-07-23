package eu.etaxonomy.cdm.api.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.SAXException;

import org.apache.xerces.impl.xpath.regex.ParseException;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.PolytomousKey;

public class IdentificationKeyGenerator {
	
	static int level=-1;
	private PolytomousKey polyto;
	private List<Integer> features;
	private List<Integer> taxa;
	
	public void setFeatures(List<Integer> featuresList){
		this.features = featuresList;
	}
	
	public void setTaxa(List<Integer> taxaList){
		this.taxa = taxaList;
	}
	
	
	private int nFeatures = 3;
	
	public void makeandprint(List<List<List<String>>> testMatrix){
		nFeatures = testMatrix.get(0).size();
		Boucle(testMatrix);
//		Map<Integer,Float> scoreMap = FeatureScores(testMatrix);
//		System.out.println(scoreMap.toString());
//		Float flo = FeatureScore(0,testMatrix);
//		System.out.println(DefaultWinner(3,scoreMap));
//		System.out.println(flo);
		List<FeatureNode> rootlist = new ArrayList<FeatureNode>();
		rootlist.add(polyto.getRoot());
		String spaces = new String();
		printTree2(rootlist,spaces);
		//printTree(rootlist);
	}
	
	
	private void Boucle(List<List<List<String>>> taxonFeatureMatrix){
		polyto = PolytomousKey.NewInstance();
		FeatureNode root = polyto.getRoot();
//		features.add(0);
//		features.add(1);
//		features.add(2);
//		taxa.add(0);
//		taxa.add(1);
//		taxa.add(2);
//		taxa.add(3);
//		taxa.add(4);
//		taxa.add(5);
		buildBranches(root,features,taxa, taxonFeatureMatrix);
		System.out.println();
		
	}
	

	private void buildBranches(FeatureNode father, List<Integer> featuresLeft, List<Integer> taxaCovered, List<List<List<String>>> taxonFeatureMatrix){
		List<String> statesDone = new ArrayList<String>();
		int i;
		
		Map<Integer,Float> scoreMap = FeatureScores(featuresLeft, taxaCovered, taxonFeatureMatrix);
		//System.out.println("-----");
		Integer winnerFeature = DefaultWinner(taxaCovered.size(), scoreMap);
		
		for (i=0;i<taxaCovered.size();i++){
				List<String> featureStates = taxonFeatureMatrix.get(taxaCovered.get(i)).get(winnerFeature);
				for (String featureState : featureStates){
					if(!statesDone.contains(featureState)){ // if a state of the winning feature has not yet been scanned a new node is created
						statesDone.add(featureState);
						List<Integer> newTaxaCovered = whichTaxa(winnerFeature,featureState,taxaCovered,taxonFeatureMatrix);
						if (!(newTaxaCovered.size()==taxaCovered.size())){ // if the remaining taxa are still discriminated, continue
							FeatureNode son = FeatureNode.NewInstance();
							Feature feature = Feature.NewInstance(null, featureState, null);
							son.setFeature(feature);
							father.addChild(son);
							//Map<Integer,Float> scoreMap = FeatureScores(featuresLeft, newTaxaCovered, taxonFeatureMatrix);
							//int winnerFeature = DefaultWinner(newTaxaCovered.size(), scoreMap);
							List<Integer> newFeaturesLeft = new LinkedList<Integer>(featuresLeft);
							//Collections.copy(featuresLeft, newFeaturesLeft);
							newFeaturesLeft.remove((Integer)winnerFeature);
							//System.out.println("zip");
							buildBranches(son, newFeaturesLeft, newTaxaCovered,taxonFeatureMatrix);
						}
						else {
							// a leaf is reached
						}
					}
			}
		}
	}
	
	
	// returns the list of taxa from previously covered taxa, which have the state featureState for the feature feature 
	private List<Integer> whichTaxa(Integer feature, String featureState, List<Integer> coveredTaxa, List<List<List<String>>> taxonFeatureMatrix){
		List<Integer> newCoveredTaxa = new ArrayList<Integer>();
		int i;
		for (i=0;i<taxonFeatureMatrix.size();i++){
			if (taxonFeatureMatrix.get(i).get(feature).contains(featureState) && (coveredTaxa.contains((Integer)i))){
				newCoveredTaxa.add(i);
			}
		}
		return newCoveredTaxa;
	}
	
	//change names
	private Integer DefaultWinner(int nTaxons, Map<Integer,Float> scores){
		float meanScore = DefaultMeanScore(nTaxons);
		float bestScore = nTaxons*nTaxons;
		Integer Feature = 0;
		Iterator it = scores.entrySet().iterator();
		float newScore;
		while (it.hasNext()){
			Map.Entry<Integer,Float> pair = (Map.Entry)it.next();
			if (pair.getValue()!=null){
				newScore = Math.abs((Float)pair.getValue()-meanScore);
				if (newScore < bestScore){
					Feature = (Integer)pair.getKey();
					bestScore = newScore;
				}
			}
		}
		return Feature;
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
	
	private Map<Integer,Float> FeatureScores(List<Integer> featuresLeft, List<Integer> coveredTaxa, List<List<List<String>>> taxonFeatureMatrix){
		Map<Integer,Float> scoreMap = new HashMap<Integer,Float>();
		int i;
		for (i=0;i<nFeatures;i++){
			if (featuresLeft.contains(i)){
				scoreMap.put(i, FeatureScore(i,coveredTaxa,taxonFeatureMatrix));
				//System.out.println(i + " : " + FeatureScore(i,coveredTaxa,taxonFeatureMatrix));
			}
			else {
				scoreMap.put(i, null);
			}
		}
		return scoreMap;
	}
	
	private float FeatureScore(int featureIndex, List<Integer> coveredTaxa, List<List<List<String>>> taxonFeatureMatrix){
		int i,j;
		float score =0;
		for (i=0 ; i<coveredTaxa.size(); i++){
			for (j=i+1 ; j< coveredTaxa.size() ; j++){
				score = score + DefaultPower(taxonFeatureMatrix.get(coveredTaxa.get(i)).get(featureIndex),taxonFeatureMatrix.get(coveredTaxa.get(j)).get(featureIndex));
			}
		}
		return score;
		}
	
	private float DefaultPower(List<String> states1, List<String> states2){
		boolean bool = false;
		Iterator<String> strIterator = states1.iterator() ;
		while (!bool && strIterator.hasNext()) {
			bool = states2.contains(strIterator.next());
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
					System.out.println(newspaces + levelcopy + " : " + j + " " + fnode.getFeature().getLabel());
					j++;
				}
				printTree2(fnode.getChildren(),newspaces);
			}
		}
	}

}
