package eu.etaxonomy.cdm.io.sdd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.DateTime;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class SDDDescriptionIO  extends SDDIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(SDDDescriptionIO.class);

	private static int modCount = 10;

	public SDDDescriptionIO(){
		super();
	}

	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("No check implemented for SDD");
		return result;
	}

	@Override
	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){

		String value;
		logger.info("start Datasets ...");
		SDDImportConfigurator sddConfig = (SDDImportConfigurator)config;

		// <Datasets xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://rs.tdwg.org/UBIF/2006/" xsi:schemaLocation="http://rs.tdwg.org/UBIF/2006/ ../SDD.xsd">
		Element root = sddConfig.getSourceRoot();
		boolean success =true;

		Namespace sddNamespace = sddConfig.getSddNamespace();
		Namespace xmlNamespace = Namespace.getNamespace("xml","http://www.w3.org/XML/1998/namespace");

		// <TechnicalMetadata created="2006-04-20T10:00:00">
		Element elTechnicalMetadata = root.getChild("TechnicalMetadata", sddNamespace);
		String nameCreated = elTechnicalMetadata.getAttributeValue("created");
		int year = Integer.parseInt(nameCreated.substring(0,4));
		int monthOfYear = Integer.parseInt(nameCreated.substring(5,7));
		int dayOfMonth = Integer.parseInt(nameCreated.substring(8,10));
		int hourOfDay = Integer.parseInt(nameCreated.substring(11,13));
		int minuteOfHour = Integer.parseInt(nameCreated.substring(14,16));
		int secondOfMinute = Integer.parseInt(nameCreated.substring(17,19));
		DateTime created = new DateTime(year,monthOfYear,dayOfMonth,hourOfDay,minuteOfHour,secondOfMinute,0);

		GregorianCalendar updated = null; 

		// <Generator name="n/a, handcrafted instance document" version="n/a"/>
		Element elGenerator = elTechnicalMetadata.getChild("Generator", sddNamespace);
		String generatorName = elGenerator.getAttributeValue("name");
		String generatorVersion = elGenerator.getAttributeValue("version");

		List<Element> elDatasets = root.getChildren("Dataset",sddNamespace);

		Map<String,TaxonDescription> taxonDescriptions = new HashMap<String,TaxonDescription>();
		Map<String,State> states = new HashMap<String,State>();
		Map<String,MeasurementUnit> units = new HashMap<String,MeasurementUnit>();
		Map<String,String> defaultUnitPrefixes = new HashMap<String,String>();
		Map<String,Feature> features = new HashMap<String,Feature>();
		Map<String,ReferenceBase> publications = new HashMap<String,ReferenceBase>();

		Set<StatisticalMeasure> statisticalMeasures = new HashSet<StatisticalMeasure>();
		Set<VersionableEntity> featureData = new HashSet<VersionableEntity>();

		NonViralName taxonName = null;
		Taxon taxon = Taxon.NewInstance(taxonName, null);
		Rights copyright = null;

		int i = 0;
		//for each Dataset
		for (Element elDataset : elDatasets){

			if ((++i % modCount) == 0){ logger.info("Datasets handled: " + (i-1));}

			// <Dataset xml:lang="en-us">
			String nameLang = elDataset.getAttributeValue("lang",xmlNamespace);
			Language datasetLanguage = Language.NewInstance();
			if (!nameLang.equals("")) {
				if(nameLang.equals("en-us")) {
					datasetLanguage = Language.ENGLISH();
				}
				if(nameLang.equals("en-au")) {
					datasetLanguage = Language.ENGLISH();
				}
			}

			/* <Representation>
      			<Label>The Genus Viola</Label>
      			<Detail>This is an example for a very simple SDD file, representing a single description with categorical, quantitative, and text character. Compare also the "Fragment*" examples, which contain more complex examples in the form of document fragments. Intended for version="SDD 1.1".</Detail>
    		   </Representation>
			 */
			Element elRepresentation = elDataset.getChild("Representation",sddNamespace);
			String label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
			String detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);

			OriginalSource originalSource = OriginalSource.NewInstance(" ", generatorName + " - " + generatorVersion + " - " + label);
//			originalSource.setCitation(citation)
			Annotation annotation = Annotation.NewInstance(detail, datasetLanguage);

			// <RevisionData>
			Element elRevisionData = elDataset.getChild("RevisionData",sddNamespace);

			// <Creators>
			Element elCreators = elRevisionData.getChild("Creators",sddNamespace);

			// <Agent role="aut" ref="a1"/>
			List<Element> listAgents = elCreators.getChildren("Agent", sddNamespace);

			int j = 0;
			//for each Agent
			Map<String,Person> authors = new HashMap<String,Person>();
			Map<String,Person> editors = new HashMap<String,Person>();
			for (Element elAgent : listAgents){
				if ((++j % modCount) == 0){ logger.info("Agents handled: " + (j-1));}
				String role = elAgent.getAttributeValue("role");
				String ref = elAgent.getAttributeValue("ref");
				if (role.equals("aut")) {
					if(!ref.equals("")) {
						authors.put(ref, null);
					}
				}
				if (role.equals("edt")) {
					if(!ref.equals("")) {
						editors.put(ref, null);
					}
				}
			}

			// <DateModified>2006-04-08T00:00:00</DateModified>
			String stringDateModified = (String)ImportHelper.getXmlInputValue(elRevisionData, "DateModified",sddNamespace);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
			Date d = null;
			try {
				d = sdf.parse(stringDateModified);
			} catch(Exception e) {
				System.err.println("Exception :");
				e.printStackTrace();
			}

			updated = new java.util.GregorianCalendar(); 
			updated.setTime(d);

			// <IPRStatements>
			Element elIPRStatements = elDataset.getChild("IPRStatements",sddNamespace);
			// <IPRStatement role="Copyright">
			List<Element> listIPRStatements = elIPRStatements.getChildren("IPRStatement", sddNamespace);
			j = 0;
			//for each IPRStatement
			for (Element elIPRStatement : listIPRStatements){
				if ((++j % modCount) == 0){ logger.info("IPRStatements handled: " + (j-1));}
				String role = elIPRStatement.getAttributeValue("role");
				// <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
				Element elLabel = elIPRStatement.getChild("Label",sddNamespace);
				String lang = elLabel.getAttributeValue("lang",xmlNamespace);
				label = (String)ImportHelper.getXmlInputValue(elIPRStatement, "Label",sddNamespace);
				if (role.equals("Copyright")) {
					Language iprLanguage = Language.NewInstance();
					if (!lang.equals("")) {
						if(lang.equals("en-us")) {
							iprLanguage = Language.ENGLISH();
						}
						if(lang.equals("en-au")) {
							iprLanguage = Language.ENGLISH();
						}
					}
					copyright = Rights.NewInstance(label, iprLanguage);
				}
			}

			// <TaxonNames>
			Element elTaxonNames = elDataset.getChild("TaxonNames",sddNamespace);
			// <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
			List<Element> listTaxonNames = elTaxonNames.getChildren("TaxonName", sddNamespace);
			j = 0;
			Map<String,NonViralName> taxonNameBases = new HashMap<String,NonViralName>();
			//for each TaxonName
			for (Element elTaxonName : listTaxonNames){
				if ((++j % modCount) == 0){ logger.info("TaxonNames handled: " + (j-1));}
				String id = elTaxonName.getAttributeValue("id");
				String uri = elTaxonName.getAttributeValue("uri");
				// <Representation>
				elRepresentation = elTaxonName.getChild("Representation",sddNamespace);
				// <Label xml:lang="la">Viola hederacea Labill.</Label>
				Element elLabel = elRepresentation.getChild("Label",sddNamespace);
				String lang = elLabel.getAttributeValue("lang",xmlNamespace);
				label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
				NonViralName tnb = NonViralName.NewInstance(null);
				tnb.setCreated(created);
				tnb.setUpdated(updated);
				if ((lang.equals("la")) || (lang.equals(""))) {
					tnb.setTitleCache(label);
				}
				if (!id.equals("")) {
					taxonNameBases.put(id,tnb);
				}

			}

			// <Characters>
			Element elCharacters = elDataset.getChild("Characters", sddNamespace);

			// <CategoricalCharacter id="c1">
			List<Element> elCategoricalCharacters = elCharacters.getChildren("CategoricalCharacter", sddNamespace);
			j = 0;
			//for each CategoricalCharacter
			for (Element elCategoricalCharacter : elCategoricalCharacters){

				if ((++j % modCount) == 0){ logger.info("CategoricalCharacters handled: " + (j-1));}

				try {

					String idCC = elCategoricalCharacter.getAttributeValue("id");

					// <Representation>
					//  <Label> Leaf complexity</Label>
					// </Representation>
					elRepresentation = elCategoricalCharacter.getChild("Representation",sddNamespace);
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);

					Feature categoricalCharacter = null;
					if (label != null){
						categoricalCharacter = Feature.NewInstance(label, label, label);
					}
					categoricalCharacter.setSupportsQuantitativeData(false);
					categoricalCharacter.setSupportsTextData(true);

					// <States>
					Element elStates = elCategoricalCharacter.getChild("States",sddNamespace);

					// <StateDefinition id="s1">
					List<Element> elStateDefinitions = elStates.getChildren("StateDefinition",sddNamespace);
					TermVocabulary<State> termVocabularyState = new TermVocabulary<State>();

					int k = 0;
					//for each StateDefinition
					for (Element elStateDefinition : elStateDefinitions){

						if ((++k % modCount) == 0){ logger.info("StateDefinitions handled: " + (k-1));}

						String idSD = elStateDefinition.getAttributeValue("id");
						// <Representation>
						//  <Label>Simple</Label>
						// </Representation>
						elRepresentation = elStateDefinition.getChild("Representation",sddNamespace);
						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
						State state = new State(label,label,label);
						states.put(idSD, state);
						termVocabularyState.addTerm(state);
					}

					categoricalCharacter.addSupportedCategoricalEnumeration(termVocabularyState);
					features.put(idCC, categoricalCharacter);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of CategoricalCharacter " + j + " failed.");
					success = false; 
				}

			}

			// <QuantitativeCharacter id="c2">
			List<Element> elQuantitativeCharacters = elCharacters.getChildren("QuantitativeCharacter", sddNamespace);
			j = 0;
			//for each QuantitativeCharacter
			for (Element elQuantitativeCharacter : elQuantitativeCharacters){

				if ((++j % modCount) == 0){ logger.info("QuantitativeCharacters handled: " + (j-1));}

				try {

					String idQC = elQuantitativeCharacter.getAttributeValue("id");

					// <Representation>
					//  <Label>Leaf length</Label>
					// </Representation>
					elRepresentation = elQuantitativeCharacter.getChild("Representation",sddNamespace);
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);

					Feature quantitativeCharacter = Feature.NewInstance();

					if (!label.equals("")){
						quantitativeCharacter = Feature.NewInstance(label, label, label);
					}
					quantitativeCharacter.setSupportsQuantitativeData(true);
					quantitativeCharacter.setSupportsTextData(false);

					// <MeasurementUnit>
					//  <Label role="Abbrev">m</Label>
					// </MeasurementUnit>
					Element elMeasurementUnit = elQuantitativeCharacter.getChild("MeasurementUnit",sddNamespace);
					Element elLabel = elMeasurementUnit.getChild("Label",sddNamespace);
					String role = elLabel.getAttributeValue("role");
					label = (String)ImportHelper.getXmlInputValue(elMeasurementUnit, "Label",sddNamespace);

					MeasurementUnit unit = null;
					if (!label.equals("")){
						if (role.equals("Abbrev")){
							unit = MeasurementUnit.NewInstance(label,label,label);
						} else {
							unit = MeasurementUnit.NewInstance(label,label,label);
						}
					}

					units.put(idQC, unit);

					//<Default>
					//  <MeasurementUnitPrefix>milli</MeasurementUnitPrefix>
					//</Default>
					Element elDefault = elQuantitativeCharacter.getChild("Default",sddNamespace);
					String measurementUnitPrefix = (String)ImportHelper.getXmlInputValue(elDefault, "MeasurementUnitPrefix",sddNamespace);
					if (!measurementUnitPrefix.equals("")){
						defaultUnitPrefixes.put(idQC, measurementUnitPrefix);
					}

					features.put(idQC, quantitativeCharacter);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of QuantitativeCharacter " + j + " failed.");
					success = false; 
				}

			}

			// <TextCharacter id="c3">
			List<Element> elTextCharacters = elCharacters.getChildren("TextCharacter", sddNamespace);
			j = 0;
			//for each TextCharacter
			for (Element elTextCharacter : elTextCharacters){

				if ((++j % modCount) == 0){ logger.info("TextCharacters handled: " + (j-1));}

				try {

					String idTC = elTextCharacter.getAttributeValue("id");

					// <Representation>
					//  <Label xml:lang="en">Leaf features not covered by other characters</Label>
					// </Representation>
					elRepresentation = elTextCharacter.getChild("Representation",sddNamespace);
					Element elLabel = elRepresentation.getChild("Label",sddNamespace);
					nameLang = elLabel.getAttributeValue("lang",xmlNamespace);
					Language language = Language.NewInstance();
					if (!nameLang.equals("")) {
						if(nameLang.equals("en")) {
							language = Language.ENGLISH();
						}
					}
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);

					Feature textCharacter = Feature.NewInstance();

					if (!idTC.equals("")){
						textCharacter = Feature.NewInstance(label, label, label);
					}
					textCharacter.setSupportsQuantitativeData(false);
					textCharacter.setSupportsTextData(true);
					textCharacter.setLabel(label, language);

					Map<String,String> textCharacters = new HashMap<String,String>();
					Map<String,Language> textCharactersLang = new HashMap<String,Language>();			
					if ((!idTC.equals("")) && (!label.equals(""))){
						textCharacters.put(idTC, label);
					}
					if (!idTC.equals("")){
						textCharactersLang.put(idTC, language);
					}

					features.put(idTC, textCharacter);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of TextCharacter " + j + " failed.");
					success = false; 
				}

			}

			// <CodedDescriptions>
			Element elCodedDescriptions = elDataset.getChild("CodedDescriptions",sddNamespace);

			// <CodedDescription id="D101">
			List<Element> listCodedDescriptions = elCodedDescriptions.getChildren("CodedDescription", sddNamespace);
			j = 0;
			//for each CodedDescription
			Map<String,String> citations = new HashMap<String,String>();
			Map<String,String> locations = new HashMap<String,String>();
			for (Element elCodedDescription : listCodedDescriptions){

				if ((++j % modCount) == 0){ logger.info("CodedDescriptions handled: " + (j-1));}

				try {

					String idCD = elCodedDescription.getAttributeValue("id");

					// <Representation>
					//  <Label>&lt;i&gt;Viola hederacea&lt;/i&gt; Labill. as revised by R. Morris April 8, 2006</Label>
					// </Representation>
					elRepresentation = elCodedDescription.getChild("Representation",sddNamespace);
					Element elLabel = elRepresentation.getChild("Label",sddNamespace);
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);

					TaxonDescription taxonDescription = TaxonDescription.NewInstance();
					taxonDescription.setTitleCache(label);

					// <Scope>
					//  <TaxonName ref="t1"/>
					//  <Citation ref="p1" location="p. 30"/>
					// </Scope>
					Element elScope = elCodedDescription.getChild("Scope",sddNamespace);
					Element elTaxonName = elScope.getChild("TaxonName",sddNamespace);
					String ref = elTaxonName.getAttributeValue("ref");

					NonViralName taxonNameBase = taxonNameBases.get(ref);
					taxon = Taxon.NewInstance(taxonNameBase, null);
					taxon.setCreated(created);
					taxon.setUpdated(updated);

					Element elCitation = elScope.getChild("Citation",sddNamespace);
					String refCitation = elCitation.getAttributeValue("ref");
					String location = elCitation.getAttributeValue("location");

					// <SummaryData>
					Element elSummaryData = elCodedDescription.getChild("SummaryData",sddNamespace);

					// <Categorical ref="c4">
					List<Element> elCategoricals = elSummaryData.getChildren("Categorical", sddNamespace);
					int k = 0;
					//for each Categorical
					for (Element elCategorical : elCategoricals){
						if ((++k % modCount) == 0){ logger.info("Categorical handled: " + (k-1));}
						ref = elCategorical.getAttributeValue("ref");
						Feature feature = features.get(ref);
						CategoricalData categoricalData = CategoricalData.NewInstance();
						categoricalData.setFeature(feature);

						// <State ref="s3"/>
						List<Element> elStates = elCategorical.getChildren("State", sddNamespace);
						int l = 0;
						//for each State
						for (Element elState : elStates){
							if ((++l % modCount) == 0){ logger.info("States handled: " + (l-1));}
							ref = elState.getAttributeValue("ref");
							State state = states.get(ref);
							categoricalData.addState(state);
						}
						taxonDescription.addElement(categoricalData);
					}

					// <Quantitative ref="c2">
					List<Element> elQuantitatives = elSummaryData.getChildren("Quantitative", sddNamespace);
					k = 0;
					//for each Quantitative
					for (Element elQuantitative : elQuantitatives){
						if ((++k % modCount) == 0){ logger.info("Quantitative handled: " + (k-1));}
						ref = elQuantitative.getAttributeValue("ref");
						Feature feature = features.get(ref);
						QuantitativeData quantitativeData = QuantitativeData.NewInstance();
						quantitativeData.setFeature(feature);

						MeasurementUnit unit = units.get(ref);
						String prefix = defaultUnitPrefixes.get(ref);
						String u = unit.getLabel();
						u = prefix + u;
						unit.setLabel(u);
						quantitativeData.setUnit(unit);

						// <Measure type="Min" value="2.3"/>
						List<Element> elMeasures = elQuantitative.getChildren("Measure", sddNamespace);
						int l = 0;
						//for each State
						for (Element elMeasure : elMeasures){
							if ((++l % modCount) == 0){ logger.info("States handled: " + (l-1));}
							String type = elMeasure.getAttributeValue("type");
							value = elMeasure.getAttributeValue("value");
							float v = Float.parseFloat(value);
							StatisticalMeasure t = null;
							if (type.equals("Min")) {
								t = StatisticalMeasure.MIN();
							} else if (type.equals("Mean")) {
								t = StatisticalMeasure.AVERAGE();
							} else if (type.equals("Max")) {
								t = StatisticalMeasure.MAX();
							} else if (type.equals("SD")) {
								// Create a new StatisticalMeasure for standard deviation
								t = StatisticalMeasure.STANDARD_DEVIATION();
							} else if (type.equals("N")) {
								t = StatisticalMeasure.SAMPLE_SIZE();
							} else {
								t = StatisticalMeasure.NewInstance(type,type,type);
								statisticalMeasures.add(t);
							}

							StatisticalMeasurementValue statisticalValue = StatisticalMeasurementValue.NewInstance();
							statisticalValue.setValue(v);
							statisticalValue.setType(t);
							quantitativeData.addStatisticalValue(statisticalValue);
							featureData.add(statisticalValue);
						}
						taxonDescription.addElement(quantitativeData);
					}

					// <TextChar ref="c3">
					List<Element> elTextChars = elSummaryData.getChildren("TextChar", sddNamespace);
					k = 0;
					//for each TextChar
					for (Element elTextChar : elTextChars){
						if ((++k % modCount) == 0){ logger.info("TextChar handled: " + (k-1));}
						ref = elTextChar.getAttributeValue("ref");
						Feature feature = features.get(ref);
						TextData textData = TextData.NewInstance();
						textData.setFeature(feature);

						// <Content>Free form text</Content>
						String content = (String)ImportHelper.getXmlInputValue(elTextChar, "Content",sddNamespace);
						textData.putText(content, datasetLanguage);
						taxonDescription.addElement(textData);
					}

					taxonDescription.addSource(originalSource);
					taxon.addDescription(taxonDescription);
					
					if (!ref.equals("")){
						citations.put(idCD,refCitation);
					}

					if (!location.equals("")){
						locations.put(idCD, location);
					}

					taxonDescriptions.put(idCD, taxonDescription);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of CodedDescription " + j + " failed.");
					success = false; 
				}

			}

			// <Agents>
			Element elAgents = elDataset.getChild("Agents",sddNamespace);

			// <Agent id="a1">
			listAgents = elAgents.getChildren("Agent", sddNamespace);
			j = 0;
			//for each Agent
			for (Element elAgent : listAgents){

				if ((++j % modCount) == 0){ logger.info("elAgent handled: " + (j-1));}

				try {

					String idA = elAgent.getAttributeValue("id");

					//  <Representation>
					//   <Label>Kevin Thiele</Label>
					//   <Detail role="Description">Ali Baba is also known as r.a.m.</Detail>
					//  </Representation>
					elRepresentation = elAgent.getChild("Representation",sddNamespace);
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
					Element elDetail = elRepresentation.getChild("Detail",sddNamespace);

					Person person = Person.NewTitledInstance(label);
					
					// PROBLEME
					
					String role = elDetail.getAttributeValue("role");
					detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
					annotation = Annotation.NewInstance(role + " - " + detail, datasetLanguage);
					person.addAnnotation(annotation);
					
					person.setCreated(created);
					person.setUpdated(updated);
					

					// <Links>
					Element elLinks = elAgent.getChild("Links",sddNamespace);

					//  <Link rel="Alternate" href="http://www.diversitycampus.net/people/hagedorn"/>
					List<Element> listLinks = elLinks.getChildren("Link", sddNamespace);
					int k = 0;
					//for each Link
					for (Element elLink : listLinks){

						if ((++k % modCount) == 0){ logger.info("elLink handled: " + (k-1));}

						try {

							String rel = elLink.getAttributeValue("rel");
							String href = elLink.getAttributeValue("href");
							
							if (k==1) {
								OriginalSource source = OriginalSource.NewInstance(rel, href);
								person.addSource(source);
							}
							
						} catch (Exception e) {
							//FIXME
							logger.warn("Import of Link " + k + " failed.");
							success = false; 
						}


					}
					
					if (authors.containsKey(idA)) {
						authors.put(idA,person);
					}

					if (editors.containsKey(idA)) {
						editors.put(idA, person);
					}

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of Agent " + j + " failed.");
					success = false; 
				}


			}

			// <Publications>
			Element elPublications = elDataset.getChild("Publications",sddNamespace);

			// <Publication id="p1">
			List<Element> listPublications = elPublications.getChildren("Publication", sddNamespace);
			j = 0;
			//for each Publication
			for (Element elPublication : listPublications){

				if ((++j % modCount) == 0){ logger.info("elAgent handled: " + (j-1));}

				try {

					String idP = elPublication.getAttributeValue("id");

					//  <Representation>
					//   <Label>Sample Citation</Label>
					//  </Representation>
					elRepresentation = elPublication.getChild("Representation",sddNamespace);
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
					Article publication = Article.NewInstance();
					publication.setTitle(label);

					publications.put(idP,publication);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of Agent " + j + " failed.");
					success = false; 
				}

			}

			for (Iterator<TaxonDescription> taxonDescription = taxonDescriptions.values().iterator() ; taxonDescription.hasNext() ;){
				TaxonDescription td = taxonDescription.next();
				/*
				if (authors.size()>1) {
					for (Iterator<Person> author = authors.values().iterator() ; author.hasNext() ;){
						td.setCreatedBy(author.next());
					}
				} else {
					Team team = Team.NewInstance();
					for (Iterator<Person> author = authors.values().iterator() ; author.hasNext() ;){
						team.addTeamMember(author.next());
					}
					td.setCreatedBy(team);
				}
				 */

				Iterator<Person> author = authors.values().iterator();
				if (author.hasNext()){
					td.setCreatedBy(author.next());
				}

				Iterator<Person> editor = editors.values().iterator();
				if (editor.hasNext()){
					td.setUpdatedBy(editor.next());
				}
				
				if (copyright != null) {
					td.addRights(copyright);
				}
				
			}

			for (Iterator<String> refCD = taxonDescriptions.keySet().iterator() ; refCD.hasNext() ;){
				String ref = refCD.next();
				TaxonDescription td = taxonDescriptions.get(ref);
				if (citations.containsKey(ref)) {
					Article publication = (Article) publications.get(citations.get(ref));
					if (locations.containsKey(ref)) {
						publication.addAnnotation(Annotation.NewInstance(locations.get(ref), datasetLanguage));
					}
					td.addDescriptionSource(publication);
				}
			}

		}
		logger.info(i + " Datasets handled");

		ITermService termService = config.getCdmAppController().getTermService();
		TransactionStatus ts = config.getCdmAppController().startTransaction();
		for (Iterator<State> k = states.values().iterator() ; k.hasNext() ;){
			State state = k.next();
			state.setCreated(created);
			state.setUpdated(updated);
			termService.saveTerm(state); 
		}
		for (Iterator<Feature> k = features.values().iterator() ; k.hasNext() ;){
			Feature feature = k.next();
			feature.setCreated(created);
			feature.setUpdated(updated);
			termService.saveTerm(feature); 
		}
		for (Iterator<MeasurementUnit> k = units.values().iterator() ; k.hasNext() ;){
			MeasurementUnit unit = k.next();
			unit.setCreated(created);
			unit.setUpdated(updated);
			termService.saveTerm(unit); 
		}
		for (Iterator<StatisticalMeasure> k = statisticalMeasures.iterator() ; k.hasNext() ;) {
			StatisticalMeasure sm = k.next();
			sm.setCreated(created);
			sm.setUpdated(updated);
			termService.saveTerm(sm); 
		}

		config.getCdmAppController().commitTransaction(ts);
		
		IReferenceService referenceService = config.getCdmAppController().getReferenceService();
		
		for (Iterator<ReferenceBase> k = publications.values().iterator() ; k.hasNext() ;){
			Article publication = (Article) k.next();
			publication.setCreated(created);
			publication.setUpdated(updated);
			referenceService.saveReference(publication); 
		}
		
		// Returns a CdmApplicationController created by the values of this configuration.
		IDescriptionService descriptionService = config.getCdmAppController().getDescriptionService();

		for (Iterator<TaxonDescription> k = taxonDescriptions.values().iterator() ; k.hasNext() ;){
			TaxonDescription taxonDescription = k.next();
			taxonDescription.setCreated(created);
			taxonDescription.setUpdated(updated);
			// Persists a Description
			descriptionService.saveDescription(taxonDescription); 
		}

		//		makeNameSpecificData(nameMap);
		logger.info("end makeTaxonDescriptions ...");
		return success;

	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return false;
	}

}
