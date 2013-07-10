package eu.etaxonomy.cdm.io.xper;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IoStateBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.WorkingSet;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import fr_jussieu_snv_lis.XPApp;
import fr_jussieu_snv_lis.Xper;
import fr_jussieu_snv_lis.IO.IExternalAdapter;
import fr_jussieu_snv_lis.base.BaseObjectResource;
import fr_jussieu_snv_lis.base.Individual;
import fr_jussieu_snv_lis.base.IndividualNode;
import fr_jussieu_snv_lis.base.IndividualTree;
import fr_jussieu_snv_lis.base.Mode;
import fr_jussieu_snv_lis.base.Variable;
import fr_jussieu_snv_lis.base.XPResource;
import fr_jussieu_snv_lis.utils.Utils;

@Component
public class CdmXperAdapter extends CdmIoBase<IoStateBase> implements IExternalAdapter{
	private static final Logger logger = Logger.getLogger(CdmXperAdapter.class);
	
	private CdmXperAdapter  adapter = this;

//	private CdmApplicationController cdmApplicationController;
	private CdmXperBaseControler baseController;
	private UUID uuidWorkingSet;
//	private WorkingSet workingSet; 
	
	//TODO preliminary
	//CONFIGURATION
	private boolean isLazyModes = false;
	private boolean isLazyIndMatrix = true;
	private boolean useSecInTaxonName = false;
	private StatisticalMeasure unknownDataType;
	
	public CdmXperAdapter(){
		BaseCdm base = new BaseCdm();
		CdmXperBaseControler baseController = new CdmXperBaseControler(base, this);
		setBaseController(baseController);
	}
	
	public boolean startXper(UUID uuidWorkingSet){
		this.uuidWorkingSet = uuidWorkingSet;
		Thread t = new Thread() {
			public void run() {
				new Xper(adapter);
			}
			
		};
		System.out.println("xper2 start");
		t.start();
//		while(!XPApp.xperReady){
//			//TODO
//		}
//		System.out.println("xper2 started :::");
		return true;
	}

	
//************************* GETTER /SETTER **********************/	

	public void setBaseController(CdmXperBaseControler baseController) {
		this.baseController = baseController;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.IO.IExternalAdapter#getBaseController()
	 */
	public CdmXperBaseControler getBaseController() {
		return baseController;
	}

	public WorkingSet getWorkingSet() {
//		if (this.workingSet == null){
			WorkingSet workingSet = getWorkingSetService().find(uuidWorkingSet);
//		}
		return workingSet;
	}
	
	public WorkingSet getLanguage() {
		
//		if (this.workingSet == null){
			WorkingSet workingSet = getWorkingSetService().find(uuidWorkingSet);
//		}
		return workingSet;
	}
	
	
	
//*********************** METHODS **********************/	
	
	public void load(){
		loadFeatures();
		loadTaxa();
	}

	// Load the featureTree with the UUID
	public void loadFeatures() {
		logger.warn("load features start");
		TransactionStatus tx = startTransaction();
		
		FeatureTree featureTree = getFeatureTree();
		getFeatureTreeService().saveOrUpdate(featureTree);
		
		if (featureTree != null) {
			loadFeatureNode(featureTree.getRoot(), -1);
		}else{
			logger.warn("No feature tree available");
		}
		commitTransaction(tx);
		logger.warn("load features end :::");
	}

	/**
	 * @return
	 */
	private FeatureTree getFeatureTree() {
		return getWorkingSet().getDescriptiveSystem();
	}


	
	/**
	 * Recursive methode to load FeatureNode and all its children
	 * 
	 * @param featureNode
	 * @param indiceParent
	 */
	public void loadFeatureNode(FeatureNode featureNode, int indiceParent){
		List<FeatureNode> featureList = featureNode.getChildren();
		
		adaptFeatureListToVariableList(indiceParent, featureList);
	}

	/**
	 * @param indiceParent
	 * @param featureList
	 */
	public void adaptFeatureListToVariableList(int indiceParent, List<FeatureNode> featureList) {
//		List<Variable> result = new ArrayList<Variable>(featureList.size()); 
		for(FeatureNode child : featureList){
			boolean alreadyExist = false;
			Variable variable = adaptFeatureNodeToVariable(child);
			
			//?? TODO
			List<Variable> vars = XPApp.getCurrentBase().getVariables();
			
			for(Variable var : vars){
				if(var.getName().equals(variable.getName()))
					alreadyExist = true;
			}
			
			if(!alreadyExist && (child.getFeature().isSupportsCategoricalData() || child.getFeature().isSupportsQuantitativeData())){
				
				XPApp.getCurrentBase().addVariable(variable);
//				result.add(variable);
				
				if(child.getFeature().isSupportsCategoricalData()){
					// Add states to the feature
					if (! isLazyModes){
						addModesToVariable(child, variable);
					}
				}
				
				if(indiceParent != -1 && XPApp.getCurrentBase().getVariableAt(indiceParent) != null){
//				if(indiceParent != -1 && result.get(indiceParent) != null){
					variable.addMother((XPApp.getCurrentBase().getVariableAt(indiceParent -1)));
//					variable.addMother(result.get(indiceParent -1 ));
				}
				
				adaptFeatureListToVariableList(variable.getIndexInt(), child.getChildren());
			}else{
				adaptFeatureListToVariableList(indiceParent, child.getChildren());
			}
		}
		return;
	}

	/**
	 * @param child
	 * @param variable
	 */
	private void addModesToVariable(FeatureNode child, Variable variable) {
		Set<TermVocabulary<State>> termVocabularySet = child.getFeature().getSupportedCategoricalEnumerations();
		for(TermVocabulary<State> termVocabulary : termVocabularySet){
			for(State state : termVocabulary.getTerms()){
				Mode mode = adaptStateToMode(state);
				variable.addMode(mode);
			}
		}
	}

	/**
	 * @param child
	 * @return
	 */
	private Variable adaptFeatureNodeToVariable(FeatureNode child) {
		Variable variable = new Variable(child.getFeature().getLabel());
		variable.setUuid(child.getFeature().getUuid());
		if (child.getFeature().isSupportsQuantitativeData()){
			// Specify the character type (numerical)
			variable.setType(Utils.numType);
		}else if (child.getFeature().isSupportsCategoricalData()){
			variable.setType(Utils.catType);
		}
		return variable;
	}
	
// ******************** STATE - MODE ***********************************/
	private Mode adaptStateToMode(State state) {
		Mode result =  new Mode(state.getLabel());
		result.setUuid(state.getUuid());
		return result;	
	}
	

	public State adaptModeToState(Mode mode) {
		State state = State.NewInstance(mode.getDescription(), mode.getName(), null);
		if (mode.getUuid() == null){
			mode.setUuid(state.getUuid());
		}else{
			state.setUuid(mode.getUuid());
		}
		return state;
	}
	
// ******************** TAXON - INDIVIDUAL ***********************************/

	/**
	 * @param taxonBase
	 * @return
	 */
	public Individual adaptTaxonToIndividual(TaxonBase taxonBase) {
		String name = useSecInTaxonName? taxonBase.getTitleCache() : taxonBase.getName().getTitleCache();
		Individual individual = new Individual(name);
		individual.setUuid(taxonBase.getUuid());
		return individual;
	}
	
// ******************************** ******************************************/	
//    // OLD
//	// Load all the taxa and 1 description
//	public void loadTaxaAndDescription() {
//		logger.warn("load taxa start");
//		//fill all variables with empty lists
//
//		
//		TransactionStatus tx = startTransaction();
//		
//		getWorkingSet().getDescriptions();
//		
//		logger.warn("load taxa from CDM");
//		List<TaxonBase> taxonList = getTaxonService().list(Taxon.class , null, null, null, null);
//		
//		for(TaxonBase taxonBase : taxonList){
//			if (XPApp.getCurrentBase() != null) {
//				
//				// Add a image to the taxon
//				BaseObjectResource bor = new BaseObjectResource(new XPResource("http://www.cheloniophilie.com/Images/Photos/Chelonia-mydas/tortue-marine.JPG"));
//                individual.addResource(bor); 
//                
//				// Add an empty description
//                
//                loadDescription(individual, taxonBase);
//                
//			}
//		}
//		commitTransaction(tx);
//		logger.warn("load taxa end :::");
//	}


	/**
	 * Load taxa and convert to individuals.
	 * Loads the 
	 *  - uuid
	 *  - name
	 *  - varModMatrix, 
	 *  - varNumValuesMatrix
	 *  - varComment (TODO)
	 *  - varUnknown (TODO)
	 */
	public void loadTaxa(){
		//categorical data
		logger.warn("load categorical data");
		TransactionStatus tx = startTransaction();
		Map<UuidAndTitleCache, Map<UUID, Set<CategoricalData>>> categoricalData = retrieveCategoricalData();
		handleCategoricalData(categoricalData);
		commitTransaction(tx);
		logger.warn("load categorical data :::");
		
		//quantitative data
		logger.warn("load quantitative data");
		tx = startTransaction();
		Map<UuidAndTitleCache, Map<UUID, Set<QuantitativeData>>> quantitativeData = retrieveQuantitativeData();
		handleQuantitativeData(quantitativeData);
		commitTransaction(tx);
		logger.warn("load quantitative data :::");
		
		//classification
		//TODO classification not yet in WorkingSet. We create a preliminary classification here
		logger.warn("Classification implementation is preliminary");
		handleClassification();
		
		
		//TODO
		//descriptions with no data
		logger.warn("Descriptions with no data not yet implemented");
		
		//TODO varComment
		logger.warn("varComment not yet implemented");
		
		//TODO varUnknown
		logger.warn("varUnknown not yet implemented");
		
		//TODO Resources
		logger.warn("resources not yet implemented");
		
//		TODO private String index;
//		TODO private String description;

		
		
	}

	/**
	 * Handles the classification.
	 * Currently this is a very preliminary implementation as classifications are not 
	 * yet part of WorkingSet. We may implement better implementations later.
	 * Current implementation uses IndividualTree and IndividualNode from Xper but later we
	 * may use Classification and TaxonNode itself. However, we will need to adapt 
	 * BaseCdm then.
	 */
	private void handleClassification() {
		IndividualTree indTree = new IndividualTree();
		SortedMap<String, Individual> indMap = new TreeMap<String, Individual>();
		List<Individual> individuals = this.getBaseController().getBase().getIndividuals();
		
		for (Individual ind : individuals){
			String name = ind.getName();
			name = getNameCache(name);
			indMap.put(name, ind);
		}
		
		IndividualNode lastNode = null;
		IndividualNode newNode;
		for (String str : indMap.keySet()){
			Individual ind = indMap.get(str);
			String description = null;
			IndividualNode mother = getLastCommonAncestor(str, lastNode);
			newNode = new IndividualNode(str, description, mother, ind);
			if (mother == null){
				indTree.addIndividualNode(newNode);
			}
			lastNode = newNode;
		}
		
		//save
		getBaseController().getBase().setIndividualTree(indTree);
		
	}

	private String getNameCache(String name) {
		name = name.split("sec\\.")[0];  //remove secundum information
		//remove authorship
		String[] split = name.split(" ");
		if (split.length > 1){
			name = split[0];
			for (int i = 1; i < split.length; i++){
				String epithet = split[i];
				char firstChar = epithet.charAt(0);
				if (! Character.isUpperCase(firstChar) && ! "(".equals(String.valueOf(firstChar)) ){
					name += " " + epithet;
				}else{
					break;  //stop with first capital epiteht or open bracket "(". May be incorrect for subgenuses.
				}
			}
		}
		return name;
		
	}

	private IndividualNode getLastCommonAncestor(String str, IndividualNode lastNode) {
		if (lastNode == null || StringUtils.isBlank(str)){
			return null;
		}else if (str.startsWith(lastNode.getName())){
			return lastNode;
		}else{
			return getLastCommonAncestor(str, lastNode.getMother());
		}
			
		
	}

	/**
	 * @return
	 */
	private Map<UuidAndTitleCache, Map<UUID, Set<CategoricalData>>> retrieveCategoricalData() {
		Map<UuidAndTitleCache, Map<UUID, Set<CategoricalData>>> data = 
			getWorkingSetService().getTaxonFeatureDescriptionElementMap(CategoricalData.class, uuidWorkingSet, null);
		return data;
	}

	private Map<UuidAndTitleCache, Map<UUID, Set<QuantitativeData>>> retrieveQuantitativeData() {
		Map<UuidAndTitleCache, Map<UUID, Set<QuantitativeData>>> data = 
			getWorkingSetService().getTaxonFeatureDescriptionElementMap(QuantitativeData.class, uuidWorkingSet, null);
		return data;
	}



	private void handleCategoricalData(Map<UuidAndTitleCache, Map<UUID, Set<CategoricalData>>> categoricalData) {
		for (UuidAndTitleCache<Taxon> taxon : categoricalData.keySet()){
			Map<UUID, Set<CategoricalData>> variableMap = categoricalData.get(taxon);
			Individual individual = findOrCreateIndividualByUuidAndTitleCache(taxon);
			handleResources(individual, taxon);
			handleCategoricalData(variableMap, individual);
		}
	}
	
	private void handleResources(Individual individual, UuidAndTitleCache<Taxon> taxon) {
		boolean limitToGalleries = false;
		Set<MarkerType> markerTypes = null;
		List<String> propertyPaths = null;
		boolean isTooSlow = true;
		if (isTooSlow){
			return;
		}
		List<Media> mediaList = getDescriptionService().listTaxonDescriptionMedia(taxon.getUuid(), limitToGalleries, markerTypes, null, null, propertyPaths);
		for(Media media : mediaList){
			BaseObjectResource xpResource = adaptMediaToXpResource(media);
			individual.addResource(xpResource);
		}
	}

	private BaseObjectResource adaptMediaToXpResource(Media media) {
		BaseObjectResource result = new BaseObjectResource();
		Map<Language, LanguageString> descriptions = media.getAllDescriptions();
		
		//BaseObjectResource
		//TODO handle language correctly
		String description = descriptions.get(Language.DEFAULT()).getText();
		result.setDescription(description);
		//TODO getTitle(Language) ??
		result.setName(media.getTitleCache());
		
		//XpResource
		MediaRepresentation rep = media.getRepresentations().iterator().next();
		XPResource xpResource = null;
		if (rep != null){
			MediaRepresentationPart part = rep.getParts().iterator().next();
			if (part != null){
				URI uri = part.getUri();
				xpResource = new XPResource(uri.toString());
				//TODO needed here or will it be loaded by Xper anyway?
				xpResource.loadProperties();
			}
		}
		if (xpResource == null){
			logger.warn("No uri available to create XpResource");
		}else{
			result.setResource(xpResource);
		}
		
		return result;
		
		
	}

	private void handleQuantitativeData(Map<UuidAndTitleCache, Map<UUID, Set<QuantitativeData>>> quantitativeData) {
		for (UuidAndTitleCache taxon : quantitativeData.keySet()){
			Map<UUID, Set<QuantitativeData>> variableMap = quantitativeData.get(taxon);
			Individual individual = findOrCreateIndividualByUuidAndTitleCache(taxon);
			handleResources(individual, taxon);
			handleQuantitativeData(variableMap, individual);
		}
	}


	private void handleCategoricalData(Map<UUID, Set<CategoricalData>> variableMap, Individual individual) {
		for (UUID featureUuid : variableMap.keySet()){
			Variable variable = baseController.findVariableByUuid(featureUuid);
			if (variable != null){
				for (CategoricalData categorical : variableMap.get(featureUuid)) {
					// create a list of xper Mode corresponding
					List<Mode> modesList = variable.getModes();
					List<StateData> stateDataList = categorical.getStates();
					for (StateData stateData : stateDataList) {
						for (Mode mode : modesList) {
							if (stateData.getState().getUuid().equals(mode.getUuid())) {
								// Add state to the Description
								individual.addModeMatrix(variable, mode);
							}
						}
					}
					handleAnnotations(categorical, individual, variable);
				}
			}else{
				logger.warn("Variable not found for uuid " +  featureUuid.toString());
			}
		}
	}
	
	private void handleQuantitativeData(Map<UUID, Set<QuantitativeData>> variableMap, Individual individual) {
		for (UUID featureUuid : variableMap.keySet()){
			Variable variable = baseController.findVariableByUuid(featureUuid);
			if (variable != null){
				for (QuantitativeData qdCDM : variableMap.get(featureUuid)){
					fr_jussieu_snv_lis.base.QuantitativeData qdXper = adaptQdCdm2QdXper(qdCDM);
					individual.addNumMatrix(variable, qdXper);
					handleAnnotations(qdCDM, individual, variable);
				}
			}else{
				logger.warn("Variable not found for uuid " +  featureUuid.toString());
			}
		}
	}
	
	
	/**
	 * FIXME Do they need to be loaded at all? This is against the common rules at 
	 * http://dev.e-taxonomy.eu/trac/wiki/XperCdmAdapter
	 * At least Individual.varComment we do want to handle on the fly!!
	 * @param descriptionElement
	 * @param individual
	 * @param variable
	 */
	private void handleAnnotations(DescriptionElementBase descriptionElement, Individual individual, Variable variable) {
		if (descriptionElement.getAnnotations().size()>0){
			Set<Annotation> annotations = descriptionElement.getAnnotations();
			if (annotations.size()> 1 ){
				String message = "There is more than one note for Taxon-Character note %s - %s";
				message = String.format(message, individual.getName(), variable.getName());
				logger.warn(message);
				getBaseController().getBase().putIndividualVarComment(individual, variable, "There is more than one note available. Can't handle this in Xper");
			}else{
				getBaseController().getBase().putIndividualVarComment(individual, variable, annotations.iterator().next().getText());
			}
		}
	}

	/**
	 * @param qdCDM
	 * @return
	 */
	private fr_jussieu_snv_lis.base.QuantitativeData adaptQdCdm2QdXper(QuantitativeData qdCDM) {
		fr_jussieu_snv_lis.base.QuantitativeData qdXper = new fr_jussieu_snv_lis.base.QuantitativeData();
		
		if (qdCDM.getMax() != null){
			qdXper.setMax(new Double(qdCDM.getMax()));
		}
		if (qdCDM.getMin() != null){
			qdXper.setMin(new Double(qdCDM.getMin()));
		}
		if (qdCDM.getTypicalLowerBoundary() != null){
			qdXper.setUmethLower(new Double(qdCDM
					.getTypicalLowerBoundary()));
		}
		if (qdCDM.getTypicalUpperBoundary() != null){
			qdXper.setUmethUpper(new Double(qdCDM
					.getTypicalUpperBoundary()));
		}
		if (qdCDM.getAverage() != null){
			qdXper.setMean(new Double(qdCDM.getAverage()));
		}
		if (qdCDM.getStandardDeviation() != null){
			qdXper.setSd(new Double(qdCDM.getStandardDeviation()));
		}
		if (qdCDM.getSampleSize() != null){
			qdXper.setNSample(new Integer(Math.round(qdCDM.getSampleSize())));
		}
		return qdXper;
	}
	
	
	private Individual findOrCreateIndividualByUuidAndTitleCache(UuidAndTitleCache simpleTaxon) {
		Individual result = this.getBaseController().findIndividualByName(simpleTaxon.getTitleCache());
		
		if (result == null){
			result= new Individual(simpleTaxon.getTitleCache());
			result.setUuid(simpleTaxon.getUuid());
			this.getBaseController().addIndividual(result);
		}
		return result;
	}


//	// Create a workingSet if not exist
//	public void createWorkingSet(){
//		
//		if(getWorkingSetService().list(WorkingSet.class, null, null, null, null).size() <= 0){
//			WorkingSet ws = WorkingSet.NewInstance();
//			
//			UUID featureTreeUUID = UUID.fromString("47eda782-89c7-4c69-9295-e4052ebe16c6");
//			List<String> featureTreeInit = Arrays.asList(new String[]{"root.children.feature.representations"});
//			
//			FeatureTree featureTree = getFeatureTreeService().load(featureTreeUUID, featureTreeInit);
//			ws.setDescriptiveSystem(featureTree);
//			
//			List<TaxonBase> taxonList = getTaxonService().list(Taxon.class , null, null, null, null);
//			for(TaxonBase taxonBase : taxonList){
//				Pager<TaxonDescription> taxonDescriptionPager = getDescriptionService().getTaxonDescriptions((Taxon)taxonBase, null, null, null, 0, Arrays.asList(new String[]{"elements.states", "elements.feature"} ));
//				List<TaxonDescription> taxonDescriptionList = taxonDescriptionPager.getRecords();
//				TaxonDescription taxonDescription = taxonDescriptionList.get(0);
//				ws.addDescription(taxonDescription);
//				System.out.println(taxonDescription.getUuid());
//			}
//			
//			getWorkingSetService().save(ws);
//		}
//	}

// ************************************ SAVE *************************************/	
	
	@Override
	public void save() {
		List<Variable> vars = XPApp.getCurrentBase().getVariables();
		saveFeatureTree(vars);
		saveFeatures(vars);
	}


	private void saveFeatureTree(List<Variable> vars) {
		logger.warn("Save feature tree  not yet implemented");
	}

	/**
	 * @param vars
	 */
	private void saveFeatures(List<Variable> vars) {
		TransactionStatus tx = startTransaction();
		for (Variable variable : vars){
			Feature feature = getFeature(variable);
			if (variable.isNumType()){
				saveNumericalFeature(variable, feature);
			}else if (Utils.catType.equals(variable.getType())){
				saveCategoricalFeature(variable, feature);
			}else{
				logger.warn("variable type undefined");
			}
		}
		commitTransaction(tx);
	}


	/**
	 * @param variable
	 * @return
	 */
	public Feature getFeature(Variable variable) {
		UUID uuid = variable.getUuid();
		ITermService termService = getTermService();
		DefinedTermBase<?> term = termService.find(uuid);
		Feature feature = CdmBase.deproxy(term, Feature.class);
		return feature;
	}

	private void saveCategoricalFeature(Variable variable, Feature feature) {
//		ITermService termService = getTermService();
//		IVocabularyService vocService = getVocabularyService();
		if (feature == null){
			saveNewFeature(variable);
		}else{
			if (isChanged(feature, variable)){
				feature.setLabel(variable.getName());
				getTermService().save(feature);
			}else{
				logger.info("No change for variable: " + variable.getName());
			}
			
			HashMap<UUID, State> allStates = getAllSupportedStates(feature);
			for (Mode mode : variable.getModes()){
				State state = allStates.get(mode.getUuid());
				if (state == null){
					saveNewState(mode, feature);
				}else{
					allStates.remove(state.getUuid());
					if (modeHasChanged(mode, state)){
						String stateDescription = null;
						String stateLabel = mode.getName();
						String stateAbbrev = null;
						Language lang = Language.DEFAULT();
						Representation rep = state.getRepresentation(lang);
						rep.setLabel(stateLabel);
						getTermService().saveOrUpdate(state);
//						State state = State.NewInstance(stateDescription, stateLabel, stateAbbrev);
//						termService.save(state);
//						voc.addTerm(state);
//						vocService.save(voc);
					}
				}
			}
			for (State state : allStates.values()){
				logger.warn("There is a state to delete: " + feature.getLabel() + "-" + state.getLabel());
				for (TermVocabulary<State> voc :feature.getSupportedCategoricalEnumerations()){
					voc.removeTerm(state);
				}
			}
		}
	}


	private boolean modeHasChanged(Mode mode, State state) {
		if (CdmUtils.nullSafeEqual(mode.getName(), state.getLabel())){
			return false;
		}else{
			return true;
		}
	}


	public void saveNewState(Mode mode, Feature feature) {
		TransactionStatus ta = startTransaction();
		ITermService termService = getTermService();
		IVocabularyService vocService = getVocabularyService();
		
		termService.saveOrUpdate(feature);
		int numberOfVocs = feature.getSupportedCategoricalEnumerations().size();
		
		TermVocabulary<State> voc;
		if (numberOfVocs <= 0){
			//new voc
			String vocLabel = "Vocabulary for feature " + feature.getLabel();
			String vocDescription = vocLabel + ". Automatically created by Xper.";
			String vocAbbrev = null;
			URI termSourceUri = null;
			voc = TermVocabulary.NewInstance(vocDescription, vocLabel, vocAbbrev, termSourceUri);
		}else if (numberOfVocs == 1){
			voc = feature.getSupportedCategoricalEnumerations().iterator().next();
		}else{
			//numberOfVocs > 1
			//FIXME preliminary
			logger.warn("Multiple supported vocabularies not yet correctly implemented");
			voc = feature.getSupportedCategoricalEnumerations().iterator().next();
		}
		saveNewModeToVoc(termService, voc, mode);
		commitTransaction(ta);
	}
	

	/**
	 * @param variable
	 * @param termService
	 * @param vocService
	 */
	private void saveNewFeature(Variable variable) {
		Feature feature;
		//new feature
		String description = null;
		String label = variable.getName();
		String labelAbbrev = null;
		feature = Feature.NewInstance(description, label, labelAbbrev);
		variable.setUuid(feature.getUuid());
		getTermService().save(feature);
		//new voc
		String vocDescription = null;
		String vocLabel = "Vocabulary for feature " + label;
		String vocAbbrev = null;
		URI termSourceUri = null;
		TermVocabulary<State> voc = TermVocabulary.NewInstance(vocDescription, vocLabel, vocAbbrev, termSourceUri);
		for (Mode mode:variable.getModes()){
			saveNewModeToVoc(getTermService(), voc, mode);
		}
		feature.addSupportedCategoricalEnumeration(voc);
		getTermService().saveOrUpdate(feature);
	}


	/**
	 * @param termService
	 * @param vocService
	 * @param voc
	 * @param mode
	 */
	private void saveNewModeToVoc(ITermService termService, TermVocabulary<State> voc, Mode mode) {
		State state = adaptModeToState(mode);
		termService.save(state);
		voc.addTerm(state);
		getVocabularyService().saveOrUpdate(voc);
	}



	private HashMap<UUID, State> getAllSupportedStates(Feature feature) {
		HashMap<UUID, State> result = new HashMap<UUID,State>();
		Set<TermVocabulary<State>> vocs = feature.getSupportedCategoricalEnumerations();
		for (TermVocabulary<State> voc : vocs){
			for (State state : voc.getTerms()){
				result.put(state.getUuid(), state);
			}
		}
		return result;
	}


	private boolean isChanged(Feature feature, Variable variable) {
		//preliminary
		return ! variable.getName().equals(feature.getLabel());
	}



	private void saveNumericalFeature(Variable variable, Feature feature) {
//		IVocabularyService vocService = getVocabularyService();
		String variableUnit = variable.getUnit();
		Set<MeasurementUnit> units = feature.getRecommendedMeasurementUnits();
		//preliminary
		if (StringUtils.isBlank(variableUnit) ){
			//unit is empty
			if (!units.isEmpty()){
				feature.getRecommendedMeasurementUnits().clear();
			}
		}else{
			// unit is not empty
			boolean unitExists = false;
			for (MeasurementUnit measurementUnit: units){
				//TODO ??
				String labelOfUnit = measurementUnit.getLabel();
				if (variableUnit.equals(labelOfUnit)){
					unitExists = true;
					break;
				}
			}
			if (! unitExists){
				units.clear();
				MeasurementUnit existingUnit = findExistingUnit(variableUnit);
				if (existingUnit == null){
					String unitDescription = null;
					String unitLabel = variableUnit;
					String labelAbbrev = null;
					MeasurementUnit newUnit = MeasurementUnit.NewInstance(unitDescription, unitLabel, labelAbbrev);
					getTermService().save(newUnit);
					UUID defaultMeasurmentUnitVocabularyUuid = UUID.fromString("3b82c375-66bb-4636-be74-dc9cd087292a");
					TermVocabulary voc = getVocabularyService().find(defaultMeasurmentUnitVocabularyUuid);
					if (voc == null){
						logger.warn("Could not find MeasurementService vocabulary");
					}else{
						voc.addTerm(newUnit);
						getVocabularyService().saveOrUpdate(voc);
					}
					existingUnit = newUnit;
				}
				feature.addRecommendedMeasurementUnit(existingUnit);
			}
		}
	}


	private MeasurementUnit findExistingUnit(String variableUnit) {
		Pager<MeasurementUnit> existingUnits = getTermService().findByRepresentationText(variableUnit, MeasurementUnit.class, null, null);
		for (MeasurementUnit exUnit : existingUnits.getRecords()){
			if (variableUnit.equals(exUnit.getLabel())){
				return exUnit;
			}
		}
		return null;
	}


// ************************** Override ********************************/	
	
	@Override
	protected void doInvoke(IoStateBase state) {
		//not needed
		return;
	}

	@Override
	protected boolean doCheck(IoStateBase state) {
		//not needed
		return false;
	}

	@Override
	protected boolean isIgnore(IoStateBase state) {
		//not needed
		return false;
	}

	@Override
	public String toString() {
		String ws = uuidWorkingSet == null ?"-" : uuidWorkingSet.toString();
		return "CdmXperAdapter (" + ws + ")";
	}
	
	/**
	 * Control addition or removal of StateData from a taxon description
	 * @param selected true for addition, false for removal
	 * @param var the Xper variable
	 * @param ind the Xper Individual
	 * @param m the Xper mode
	 */
	public void controlModeIndVar(boolean selected, Variable var, Individual ind, Mode m) {
//		(boolean selected, Variable v, Individual i, Mode m);
		TransactionStatus txStatus = startTransaction();
		Taxon taxon = (Taxon)getTaxonService().find(ind.getUuid());
		Feature feature = (Feature)getTermService().find(var.getUuid());
		Set<Feature> features = new HashSet<Feature>();
		features.add(feature);
		List<CategoricalData> catData = getDescriptionService().getDescriptionElementsForTaxon(taxon, features, CategoricalData.class, null, null, null);
		if (catData.size()>1){
			logger.warn("There is more than one categorical data for the same taxon and the same feature");
		}
		//add new state
		if (selected && catData.size() == 0 ){
			CategoricalData data = CategoricalData.NewInstance();
			data.setFeature(feature);
			TaxonDescription desc = taxon.getDescriptions().iterator().next();
			desc.addElement(data);
			addModeToCategoricalData(m, data);
			getDescriptionService().saveDescriptionElement(data);
		}else{
			//update existing data
			for (CategoricalData data: catData){
				State tmpState = adaptModeToState(m);
				StateData existingState = null;
				//test data exists
				for (StateData sd : data.getStates()){
					if (tmpState.equals(sd.getState())){
						existingState = sd;
						break;
					}
				}
				//update data
				if (selected && existingState == null){
					//selected
					addModeToCategoricalData(m, data);
				}else if (!selected && existingState != null){
					//unselected
					data.getStates().remove(existingState);
				}
				getDescriptionService().saveDescriptionElement(data);
			}
		}
		commitTransaction(txStatus);
		
	}

	/**
	 * @param m
	 * @param data
	 */
	private void addModeToCategoricalData(Mode m, CategoricalData data) {
		StateData sd =  StateData.NewInstance();
		State state = (State)getTermService().find(m.getUuid());
		if (state == null){
			logger.warn("State not found: " + m.getName() + "; " + m.getUuid());
		}
		sd.setState(state);
		data.getStates().add(sd);
	}

	/**
	 * @param var
	 * @param ind
	 * @param value
	 * @param withDaughters
	 */
	public void checkUnknown(Variable var, Individual ind, boolean value, boolean withDaughters) {
		TransactionStatus txStatus = startTransaction();
		
		List<QuantitativeData> quantData = retriveQuantitativeDataListForTaxonFeature(ind, var);
		
		for (QuantitativeData data: quantData){
			//TODO handle existing data better ??
			setDataUnknown(data);

			//test data exists ??
			getDescriptionService().saveDescriptionElement(data);
		}
		
		commitTransaction(txStatus);
	}
	
	public void controlModeIndVar(Individual ind, Variable var, Double min,
			Double max, Double mean, Double sd, Double umethLower,
			Double umethUpper, Integer nSample) {

		TransactionStatus txStatus = startTransaction();
		List<QuantitativeData> quantData = retriveQuantitativeDataListForTaxonFeature(ind, var);
		
//		fr_jussieu_snv_lis.base.QuantitativeData qdXper = ind.getNumMatrix().get(var);
//		if (qdXper == null ){
//			//TODO needed?
//		}
		
		for (QuantitativeData data: quantData){
			//TODO handle existing data better
			setQdValues(data, min, max, mean, sd, umethLower, umethUpper, nSample);

			//test data exists
//				for (StateData sd : data.getStates()){
//					if (tmpState.equals(sd.getState())){
//						existingState = sd;
//						break;
//					}
//				}
			getDescriptionService().saveDescriptionElement(data);
		}
		
		commitTransaction(txStatus);
		
	}

	/**
	 * @param ind
	 * @param var
	 * @return
	 */
	private List<QuantitativeData> retriveQuantitativeDataListForTaxonFeature(
			Individual ind, Variable var) {
		Taxon taxon = (Taxon)getTaxonService().find(ind.getUuid());
		Feature feature = (Feature)getTermService().find(var.getUuid());
		Set<Feature> features = new HashSet<Feature>();
		features.add(feature);
		List<QuantitativeData> quantData = getDescriptionService().getDescriptionElementsForTaxon(taxon, features, QuantitativeData.class, null, null, null);
		if (quantData.size()>1){
			logger.warn("There are more than one quantitative data for the same taxon and the same feature");
		}
		
		if (quantData.size() == 0 ){
			QuantitativeData data = QuantitativeData.NewInstance();
			data.setFeature(feature);
			TaxonDescription desc = taxon.getDescriptions().iterator().next();
			desc.addElement(data);
//			addValuesToQuantitativeData(qdXper, data, min, max, 
//					mean, sd, umethLower, umethUpper, nSample);
			
			quantData.add(data);
		}
		return quantData;
	}

	/**
	 * Removes all existing values and puts a new value of type unknown data.
	 * The value itself is arbitrary.
	 * @param data
	 */
	private void setDataUnknown(QuantitativeData data) {
		//remove all other values
		Iterator<StatisticalMeasurementValue> it = data.getStatisticalValues().iterator();
		while (it.hasNext()){
			it.remove();
		}
		
		//add unknown data value
		StatisticalMeasure dataUnknownType = getUnknownData();
		data.setSpecificStatisticalValue(Float.valueOf(0), null, dataUnknownType);
		
	}

	/**
	 * @param qdXper Xper quantitative data attached to the individual. Needed here?
	 * @param data
	 * @param nSample 
	 * @param umethUpper 
	 * @param umethLower 
	 * @param sd 
	 * @param mean 
	 * @param max 
	 * @param min 
	 */
	private void setQdValues(QuantitativeData data, Double min, Double max, 
			Double mean, Double sd, Double umethLower, Double umethUpper, Integer nSample) {
		
		data.setMinimum(getFloat(min), null);
		data.setMaximum(getFloat(max), null);
		data.setAverage(getFloat(mean), null);
		data.setStandardDeviation(getFloat(sd), null);
		data.setTypicalLowerBoundary(getFloat(umethLower), null);
		data.setTypicalUpperBoundary(getFloat(umethUpper), null);
		data.setSampleSize(getFloat(nSample), null);
	}

	private Float getFloat(Number myNumber) {
		Float result = (myNumber == null) ? null : myNumber.floatValue();
		return result;
	}
	
	/**
	 * Retrieve the unknownData {@link StatisticalMeasure}. If necessary create it anew and save in database.
	 * @return
	 */
	private StatisticalMeasure getUnknownData(){
		if (this.unknownDataType != null){
			return this.unknownDataType;
		}
		UUID uuid = StatisticalMeasure.uuidStatisticalMeasureUnknownData;
		
		unknownDataType = (StatisticalMeasure)getTermService().find(uuid);
		if (unknownDataType == null){
			unknownDataType = StatisticalMeasure.NewInstance("Unknown data", "Placeholder for unknown statistical data", "unknown");
			unknownDataType.setUuid(uuid);
			TermVocabulary<StatisticalMeasure> voc = StatisticalMeasure.AVERAGE().getVocabulary();
			getVocabularyService().saveOrUpdate(voc);
			voc.addTerm(unknownDataType);
			getTermService().save(unknownDataType);
		}
	
		return unknownDataType;
	}

	/**
	 * Creates and saves a new feature with label = name, adds it to the end of the feature tree,
	 * and adds the supported states (currently none)
	 * 
	 * @param newVariable
	 */
	public void createNewVariable(Variable newVariable) {
		TransactionStatus txStatus = startTransaction();
		
		//create feature
		String description = newVariable.getDescription();
		String label = newVariable.getName();
		String abbrev = null;
		Feature feature = Feature.NewInstance(description, label, abbrev);
		feature.setUuid(newVariable.getUuid());
		
		//feature type
		if (newVariable.getType().equals(Utils.catType)){
			feature.setSupportsCategoricalData(true);
			//add supported states
			String vocDescription = "Vocabulary for feature " + label;
			String vocLabel = label +  " states";
			String vocAbbrev = null;
			URI termSourceUri = null;
			TermVocabulary<State> supportedCategoricalEnumeration = TermVocabulary.NewInstance(vocDescription, vocLabel, vocAbbrev, termSourceUri); 
			feature.addSupportedCategoricalEnumeration(supportedCategoricalEnumeration);
			getVocabularyService().save(supportedCategoricalEnumeration);
				
		}else if (newVariable.getType().equals(Utils.numType)){
			feature.setSupportsQuantitativeData(true);
			
			//measurement unit
			String strUnit = newVariable.getUnit();
			if (strUnit != null){
				List unitList = getTermService().listByTitle(MeasurementUnit.class, strUnit, MatchMode.EXACT, null, 1, 1, null, null);
				if (unitList.isEmpty()){
					MeasurementUnit newUnit = MeasurementUnit.NewInstance(strUnit, strUnit, strUnit);
					unitList.add(newUnit);
					getTermService().save(newUnit);
				}
				MeasurementUnit unit = (MeasurementUnit)unitList.get(0);
				feature.addRecommendedMeasurementUnit(unit);
			}
			//add Xper statistical measures
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.SAMPLE_SIZE());
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.STANDARD_DEVIATION());
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.AVERAGE());
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.MIN());
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.MAX());
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY());
			feature.addRecommendedStatisticalMeasure(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY());
		}
		
		getTermService().save(feature); 
		
		
		//add to feature tree 
		FeatureNode node = FeatureNode.NewInstance(feature);
	    
		FeatureNode root = getFeatureTree().getRoot();
		//get feature vocabulary
		TermVocabulary<Feature> featureVoc = null;
		if (root.getChildCount()>0 && featureVoc == null){
			featureVoc = getFirstVocabulary(root);
		}
		if (featureVoc == null){
			//TODO labels  ; add WorkingSet information
			String featureVocDescription = "Feature vocabulary for descriptive data";
			String featureVocLabel = "Feature vocabulary for descriptive data";
			String featureVocAbbrev = null;
			URI featureVocTermSourceUri = null;
			featureVoc = TermVocabulary.NewInstance(featureVocDescription, featureVocLabel, featureVocAbbrev, featureVocTermSourceUri);
			getVocabularyService().save(featureVoc);
		}
		featureVoc.addTerm(feature);
		root.addChild(node);
		getFeatureNodeService().save(node);
		commitTransaction(txStatus);
	}
	
	/**
	 * Returns the first vocabulary found in an child or grandchild.
	 * @param node
	 * @return
	 */
	private TermVocabulary<Feature> getFirstVocabulary(FeatureNode node){
		TermVocabulary<Feature> result = null;
		for (FeatureNode child : node.getChildren()){
			result = child.getFeature().getVocabulary();
			if (result != null){
				return result;
			}
			result = getFirstVocabulary(child);
			if (result != null){
				return result;
			}
		}
		return null;
	}

	public void moveFeature(Variable current, Variable newMother) {
		TransactionStatus txStatus = startTransaction();
		FeatureNode root = getFeatureTree().getRoot();
		
		//current node
		FeatureNode currentNode = getFirstFeature(root, current.getUuid());
		if (currentNode == null){
			throw new RuntimeException("Feature node for descriptor to move could not be found");
		}
		
		//parent node
		FeatureNode newParentNode = null;
		if (newMother != null){
			newParentNode = getFirstFeature(root, newMother.getUuid());
		}
		if (newParentNode == null){
			newParentNode = root;
		}
		
		//move
		newParentNode.addChild(currentNode);
		
		//save
		getFeatureNodeService().saveOrUpdate(newParentNode);
		commitTransaction(txStatus);
		
	}
	
	/**
	 * Returns the first feature node found that has a feature with the given uuid.
	 * @param parentNode
	 * @param uuid
	 * @return
	 */
	//preliminary implementation to find the 
	private FeatureNode getFirstFeature(FeatureNode parentNode, UUID uuid){
		FeatureNode result = null;
		for (FeatureNode child : parentNode.getChildren()){
			if (child.getFeature().getUuid().equals(uuid)){
				return child;
			}
			result = getFirstFeature(child, uuid);
			if (result != null){
				return result;
			}
		}
		return null;
	}

}
