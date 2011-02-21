package eu.etaxonomy.xper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import xper2.fr_jussieu_snv_lis.Xper;
import xper2.fr_jussieu_snv_lis.base.Individual;
import xper2.fr_jussieu_snv_lis.base.Mode;
import xper2.fr_jussieu_snv_lis.base.Variable;
import xper2.fr_jussieu_snv_lis.utils.Utils;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class AdaptaterCdmXper {
	private static final Logger logger = Logger.getLogger(AdaptaterCdmXper.class);
	
	public AdaptaterCdmXper() {
		
	}

	// Load the featureTree with the UUID 7027f0aa-ac00-4e78-a15e-1b3f8314884e
	public void loadFeatures() {
		UUID featureTreeUUID = UUID.fromString("2e6d3fe1-59db-490e-9e1c-2d58ddbbff78");
		List<String> featureTreeInit = Arrays.asList(new String[]{"root.children.feature.representations"});
		
		TransactionStatus tx = Xper.getCdmApplicationController().startTransaction();
		FeatureTree featureTree = Xper.getCdmApplicationController().getFeatureTreeService().load(featureTreeUUID, featureTreeInit);
		if (featureTree != null) {
			loadFeatureNode(featureTree.getRoot(), -1);
		}else{
			logger.warn("Feature tree " + featureTreeUUID.toString() + " not found");
		}
		Xper.getCdmApplicationController().commitTransaction(tx);
	}
	
	// Recursive methode to load FeatureNode and all its children
	public void loadFeatureNode(FeatureNode featureNode, int indiceParent){
		if(featureNode.getChildCount() > 0){
			List<FeatureNode> featureList = featureNode.getChildren();
			for(FeatureNode child : featureList){
				boolean alreadyExist = false;
				Variable variable = new Variable(child.getFeature().getLabel());
				variable.setUuid(child.getFeature().getUuid());
				List<Variable> vars = Utils.currentBase.getVariables();
				for(Variable var : vars){
					if(var.getName().equals(variable.getName()))
						alreadyExist = true;
				}
				
				if(!alreadyExist && child.getFeature().isSupportsCategoricalData()){
					Utils.currentBase.addVariable(variable);
					// Add states to the character
					Set<TermVocabulary<State>> termVocabularySet = child.getFeature().getSupportedCategoricalEnumerations();
					for(TermVocabulary<State> termVocabulary : termVocabularySet){
						for(State sate : termVocabulary.getTerms()){
							Mode mode = new Mode(sate.getLabel());
							mode.setUuid(sate.getUuid());
							variable.addMode(mode);
						}
					}
					
					if(indiceParent != -1 && Utils.currentBase.getVariableAt(indiceParent) != null){
						variable.addMother(((Variable)Utils.currentBase.getVariableAt(indiceParent -1)));
					}
					
					loadFeatureNode(child, variable.getIndexInt());
				}else{
					loadFeatureNode(child, indiceParent);
				}
			}
		}
	}
	
	// Load all the taxa and 1 description
	public void loadTaxaAndDescription() {
		TransactionStatus tx = Xper.getCdmApplicationController().startTransaction();
		List<TaxonBase> taxonList = Xper.getCdmApplicationController().getTaxonService().list(Taxon.class , null, null, null, null);
		for(TaxonBase taxonBase : taxonList){
			if (Utils.currentBase != null) {
				Individual individual = new Individual(taxonBase.getName().toString());
				individual.setUuid(taxonBase.getUuid());
				// Add an empty description
				List<Variable> vars = Utils.currentBase.getVariables();
				for(Variable var : vars){
					individual.addMatrix(var, new ArrayList<Mode>());
				}
				loadDescription(individual, taxonBase);
				Utils.currentBase.addIndividual(individual);
			}
		}
		Xper.getCdmApplicationController().commitTransaction(tx);
	}

	// Load the first taxonDescription
	public void loadDescription(Individual individual, TaxonBase taxonBase) {
		
		Pager<TaxonDescription> taxonDescriptionPager = Xper.getCdmApplicationController().getDescriptionService().getTaxonDescriptions((Taxon)taxonBase, null, null, null, 0, Arrays.asList(new String[]{"elements.states", "elements.feature"} ));
		List<TaxonDescription> taxonDescriptionList = taxonDescriptionPager.getRecords();
		TaxonDescription taxonDescription = taxonDescriptionList.get(0);
		Set<DescriptionElementBase> DescriptionElementBaseList = taxonDescription.getElements();
		for(DescriptionElementBase descriptionElementBase : DescriptionElementBaseList){
			if(descriptionElementBase instanceof CategoricalData){
				// find the xper variable corresponding
				Variable variable = null;
				List<Variable> vars = Utils.currentBase.getVariables();
				for(Variable var : vars){
					if(var.getUuid().equals(((CategoricalData)descriptionElementBase).getFeature().getUuid())){
						variable = var;
					}
				}
				if(variable != null){
					// create a list of xper Mode corresponding
					List<Mode> modesList = variable.getModes();
					List<StateData> stateList = ((CategoricalData)descriptionElementBase).getStates();
					for(StateData state : stateList){
						for(Mode mode : modesList){
							if(state.getState().getUuid().equals(mode.getUuid())){
								// Add state to the Description
								individual.addModeMatrix(variable, mode);
							}
						}
					}
				}
			}
		}
	}
	
	// Create a workingSet if not exist
	public void createWorkingSet(){
		
		if(Xper.getCdmApplicationController().getWorkingSetService().list(WorkingSet.class, null, null, null, null).size() <= 0){
			WorkingSet ws = WorkingSet.NewInstance();
			
			UUID featureTreeUUID = UUID.fromString("7027f0aa-ac00-4e78-a15e-1b3f8314884e");
			List<String> featureTreeInit = Arrays.asList(new String[]{"root.children.feature.representations"});
			
			FeatureTree featureTree = Xper.getCdmApplicationController().getFeatureTreeService().load(featureTreeUUID, featureTreeInit);
			ws.setDescriptiveSystem(featureTree);
			
			List<TaxonBase> taxonList = Xper.getCdmApplicationController().getTaxonService().list(Taxon.class , null, null, null, null);
			for(TaxonBase taxonBase : taxonList){
				Pager<TaxonDescription> taxonDescriptionPager = Xper.getCdmApplicationController().getDescriptionService().getTaxonDescriptions((Taxon)taxonBase, null, null, null, 0, Arrays.asList(new String[]{"elements.states", "elements.feature"} ));
				List<TaxonDescription> taxonDescriptionList = taxonDescriptionPager.getRecords();
				TaxonDescription taxonDescription = taxonDescriptionList.get(0);
				ws.addDescription(taxonDescription);
				System.out.println(taxonDescription.getUuid());
			}
			
			Xper.getCdmApplicationController().getWorkingSetService().save(ws);
		}
	}
}
