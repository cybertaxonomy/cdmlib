package eu.etaxonomy.cdm.io.sdd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class SDDDescriptionIO  extends SDDIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(SDDDescriptionIO.class);

	private static int modCount = 1000;

	private Map<String,List<CdmBase>> mediaObject_ListCdmBase = new HashMap<String,List<CdmBase>>();
	private Map<String,String> mediaObject_Role = new HashMap<String,String>();

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

		logger.info("start TechnicalMetadata ...");
		// <TechnicalMetadata created="2006-04-20T10:00:00">
		Element elTechnicalMetadata = root.getChild("TechnicalMetadata", sddNamespace);
		String nameCreated = elTechnicalMetadata.getAttributeValue("created");
		ReferenceBase sourceReference = config.getSourceReference();
		ReferenceBase sec = Database.NewInstance();
		if (nameCreated != null) {
			if (!nameCreated.equals("")) {
				int year = Integer.parseInt(nameCreated.substring(0,4));
				int monthOfYear = Integer.parseInt(nameCreated.substring(5,7));
				int dayOfMonth = Integer.parseInt(nameCreated.substring(8,10));
				int hourOfDay = Integer.parseInt(nameCreated.substring(11,13));
				int minuteOfHour = Integer.parseInt(nameCreated.substring(14,16));
				int secondOfMinute = Integer.parseInt(nameCreated.substring(17,19));
				DateTime created = new DateTime(year,monthOfYear,dayOfMonth,hourOfDay,minuteOfHour,secondOfMinute,0);
				sourceReference.setCreated(created);
				sec.setCreated(created);
			}
		}

		// <Generator name="n/a, handcrafted instance document" version="n/a"/>
		Element elGenerator = elTechnicalMetadata.getChild("Generator", sddNamespace);
		String generatorName = elGenerator.getAttributeValue("name");
		String generatorVersion = elGenerator.getAttributeValue("version");

		List<Element> elDatasets = root.getChildren("Dataset",sddNamespace);

		Map<String,TaxonDescription> taxonDescriptions = new HashMap<String,TaxonDescription>();
		Map<String,StateData> stateDatas = new HashMap<String,StateData>();
		Map<String,MeasurementUnit> units = new HashMap<String,MeasurementUnit>();
		Map<String,String> defaultUnitPrefixes = new HashMap<String,String>();
		Map<String,Feature> features = new HashMap<String,Feature>();
		Map<String,ReferenceBase> publications = new HashMap<String,ReferenceBase>();

		Set<StatisticalMeasure> statisticalMeasures = new HashSet<StatisticalMeasure>();
		Set<VersionableEntity> featureData = new HashSet<VersionableEntity>();

		TransactionStatus ts = config.getCdmAppController().startTransaction();

		int i = 0;
		//for each Dataset
		logger.info("start Dataset ...");
		for (Element elDataset : elDatasets){

			// <Dataset xml:lang="en-us">
			String nameLang = elDataset.getAttributeValue("lang",xmlNamespace);
			Language datasetLanguage = null;
			if (!nameLang.equals("")) {
				String iso = nameLang.substring(0, 2);
				datasetLanguage = config.getCdmAppController().getTermService().getLanguageByIso(iso);
				//datasetLanguage = Language.ENGLISH();
			} else {
				datasetLanguage = Language.ENGLISH();
			}
			if (datasetLanguage == null) {
				datasetLanguage = Language.ENGLISH();
			}

			/* <Representation>
      			<Label>The Genus Viola</Label>
      			<Detail>This is an example for a very simple SDD file, representing a single description with categorical, quantitative, and text character. Compare also the "Fragment*" examples, which contain more complex examples in the form of document fragments. Intended for version="SDD 1.1".</Detail>
    		   </Representation>
			 */
			logger.info("start Representation ...");
			Element elRepresentation = elDataset.getChild("Representation",sddNamespace);
			String label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
			String detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);

			sourceReference.setTitleCache(generatorName + " - " + generatorVersion + " - " + label);
			sec.setTitleCache(generatorName + " - " + generatorVersion + " - " + label);

			if (detail != null) {
				Annotation annotation = Annotation.NewInstance(detail, datasetLanguage);
				sec.addAnnotation(annotation);
			}
			
			List<Element> listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

			for (Element elMediaObject : listMediaObjects) {
				String ref = null;
				String role = null;
				if (elMediaObject != null) {
					ref = elMediaObject.getAttributeValue("ref");
					role = elMediaObject.getAttributeValue("role");
				}
				if (ref != null) {
					if (!ref.equals("")) {
						this.addCdmBaseWithImage(ref, sourceReference);
						this.addCdmBaseWithImage(ref,sec);
						mediaObject_Role.put(ref,role);
					}
				}
			}

			// <RevisionData>
			logger.info("start RevisionData ...");
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

				if ((++j % modCount) == 0){ logger.info("Agents handled: " + j);}

			}

			// <DateModified>2006-04-08T00:00:00</DateModified>
			String stringDateModified = (String)ImportHelper.getXmlInputValue(elRevisionData, "DateModified",sddNamespace);

			if (stringDateModified != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				Date d = null;
				try {
					d = sdf.parse(stringDateModified);
				} catch(Exception e) {
					System.err.println("Exception :");
					e.printStackTrace();
				}

				GregorianCalendar updated = null;
				if (d != null) {
					updated = new java.util.GregorianCalendar();
					updated.setTime(d);
					sourceReference.setUpdated(updated);
					sec.setUpdated(updated);
				}
			}

			// <IPRStatements>
			logger.info("start IPRStatements ...");
			Element elIPRStatements = elDataset.getChild("IPRStatements",sddNamespace);
			// <IPRStatement role="Copyright">
			Rights copyright = null;
			if (elIPRStatements != null) {
				List<Element> listIPRStatements = elIPRStatements.getChildren("IPRStatement", sddNamespace);
				j = 0;
				//for each IPRStatement

				for (Element elIPRStatement : listIPRStatements){

					String role = elIPRStatement.getAttributeValue("role");
					// <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
					Element elLabel = elIPRStatement.getChild("Label",sddNamespace);
					String lang = "";
					if (elLabel != null) {
						lang = elLabel.getAttributeValue("lang",xmlNamespace);
					}
					label = (String)ImportHelper.getXmlInputValue(elIPRStatement, "Label",sddNamespace);

					if (role.equals("Copyright")) {
						Language iprLanguage = null;
						if (lang != null) {
							if (!lang.equals("")) {
								iprLanguage = config.getCdmAppController().getTermService().getLanguageByIso(lang.substring(0, 2));
								//iprLanguage = datasetLanguage;
							} else {
								iprLanguage = datasetLanguage;
							}
						}
						if (iprLanguage == null) {
							iprLanguage = datasetLanguage;
						}
						copyright = Rights.NewInstance(label, iprLanguage);
					}

					if (copyright != null) {
						sourceReference.addRights(copyright);
						sec.addRights(copyright);
					}

					if ((++j % modCount) == 0){ logger.info("IPRStatements handled: " + j);}

				}
			}

			// <TaxonNames>
			logger.info("start TaxonNames ...");
			Element elTaxonNames = elDataset.getChild("TaxonNames",sddNamespace);
			// <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
			Map<String,NonViralName> taxonNameBases = new HashMap<String,NonViralName>();
			if (elTaxonNames != null) {
				List<Element> listTaxonNames = elTaxonNames.getChildren("TaxonName", sddNamespace);
				j = 0;
				//for each TaxonName
				for (Element elTaxonName : listTaxonNames){

					String id = elTaxonName.getAttributeValue("id");
					String uri = elTaxonName.getAttributeValue("uri");
					// <Representation>
					elRepresentation = elTaxonName.getChild("Representation",sddNamespace);
					// <Label xml:lang="la">Viola hederacea Labill.</Label>
					Element elLabel = elRepresentation.getChild("Label",sddNamespace);
					String lang = elLabel.getAttributeValue("lang",xmlNamespace);
					label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
					detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
					
					NonViralName tnb = NonViralName.NewInstance(null);
					if ((lang != null) && (!lang.equals("la")))  {
						logger.info("TaxonName " + j + " is not specified as a latin name.");
					}
					tnb.setTitleCache(label);
					if (detail != null) {
						Annotation annotation = Annotation.NewInstance(detail, datasetLanguage);
						tnb.addAnnotation(annotation);
					}
					OriginalSource source = OriginalSource.NewInstance(id, "TaxonName");
					tnb.addSource(source);
					if (!id.equals("")) {
						taxonNameBases.put(id,tnb);
					}

					listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

					for (Element elMediaObject : listMediaObjects) {
						String ref = null;
						String role = null;
						if (elMediaObject != null) {
							ref = elMediaObject.getAttributeValue("ref");
							role = elMediaObject.getAttributeValue("role");
						}
						if (ref != null) {
							if (!ref.equals("")) {
								this.addCdmBaseWithImage(ref,tnb);
							}
						}
					}

					if ((++j % modCount) == 0){ logger.info("TaxonNames handled: " + j);}

				}
			}

			// <Characters>
			logger.info("start Characters ...");
			Element elCharacters = elDataset.getChild("Characters", sddNamespace);

			// <CategoricalCharacter id="c1">
			if (elCharacters != null) {
				List<Element> elCategoricalCharacters = elCharacters.getChildren("CategoricalCharacter", sddNamespace);
				j = 0;
				//for each CategoricalCharacter
				for (Element elCategoricalCharacter : elCategoricalCharacters){

					try {

						String idCC = elCategoricalCharacter.getAttributeValue("id");

						// <Representation>
						//  <Label> Leaf complexity</Label>
						// </Representation>
						elRepresentation = elCategoricalCharacter.getChild("Representation",sddNamespace);
						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
						detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
						
						Feature categoricalCharacter = null;
						if (label != null){
							if (detail != null) {
								categoricalCharacter = Feature.NewInstance(detail, label, label);
							} else {
								categoricalCharacter = Feature.NewInstance(label, label, label);
							}
						}
						categoricalCharacter.setSupportsQuantitativeData(false);
						categoricalCharacter.setSupportsTextData(true);

						listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

						for (Element elMediaObject : listMediaObjects) {
							String ref = null;
							String role = null;
							if (elMediaObject != null) {
								ref = elMediaObject.getAttributeValue("ref");
								role = elMediaObject.getAttributeValue("role");
							}
							if (ref != null) {
								if (!ref.equals("")) {
									this.addCdmBaseWithImage(ref,categoricalCharacter);
								}
							}
						}

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
							//  <MediaObject ref="ib" role="Primary"/>
							// </Representation>
							elRepresentation = elStateDefinition.getChild("Representation",sddNamespace);
							label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
							detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
							
							State state = null;
							if (label != null){
								if (detail != null) {
									state = new State(detail,label,label);
								} else {
									state = new State(label,label,label);
								}
							}

							listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

							for (Element elMediaObject : listMediaObjects) {
								String ref = null;
								String role = null;
								if (elMediaObject != null) {
									ref = elMediaObject.getAttributeValue("ref");
									role = elMediaObject.getAttributeValue("role");
								}

								if (ref != null) {
									if (!ref.equals("")) {
										this.addCdmBaseWithImage(ref,state);
									}
								}
							}

							StateData stateData = StateData.NewInstance();
							stateData.setState(state);
							termVocabularyState.addTerm(state);
							stateDatas.put(idSD,stateData);
						}

						categoricalCharacter.addSupportedCategoricalEnumeration(termVocabularyState);
						features.put(idCC, categoricalCharacter);

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of CategoricalCharacter " + j + " failed.");
						success = false; 
					}

					if ((++j % modCount) == 0){ logger.info("CategoricalCharacters handled: " + j);}

				}

				// <QuantitativeCharacter id="c2">
				List<Element> elQuantitativeCharacters = elCharacters.getChildren("QuantitativeCharacter", sddNamespace);
				j = 0;
				//for each QuantitativeCharacter
				for (Element elQuantitativeCharacter : elQuantitativeCharacters){

					try {

						String idQC = elQuantitativeCharacter.getAttributeValue("id");

						// <Representation>
						//  <Label>Leaf length</Label>
						// </Representation>
						elRepresentation = elQuantitativeCharacter.getChild("Representation",sddNamespace);
						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);

						detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
						
						Feature quantitativeCharacter = null;
						if (label != null){
							if (detail != null) {
								quantitativeCharacter = Feature.NewInstance(detail,label,label);
							} else {
								quantitativeCharacter = Feature.NewInstance(label,label,label);
							}
						}

						if (!label.equals("")){
							quantitativeCharacter = Feature.NewInstance(label, label, label);
						}
						quantitativeCharacter.setSupportsQuantitativeData(true);
						quantitativeCharacter.setSupportsTextData(false);

						listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

						for (Element elMediaObject : listMediaObjects) {
							String ref = null;
							String role = null;
							if (elMediaObject != null) {
								ref = elMediaObject.getAttributeValue("ref");
								role = elMediaObject.getAttributeValue("role");
							}
							if (ref != null) {
								if (!ref.equals("")) {
									this.addCdmBaseWithImage(ref,quantitativeCharacter);
								}
							}
						}

						// <MeasurementUnit>
						//  <Label role="Abbrev">m</Label>
						// </MeasurementUnit>
						Element elMeasurementUnit = elQuantitativeCharacter.getChild("MeasurementUnit",sddNamespace);
						label = "";
						String role = "";
						if (elMeasurementUnit != null) {
							Element elLabel = elMeasurementUnit.getChild("Label",sddNamespace);
							role = elLabel.getAttributeValue("role");
							label = (String)ImportHelper.getXmlInputValue(elMeasurementUnit, "Label",sddNamespace);
						}

						MeasurementUnit unit = null;
						if (!label.equals("")){
							if (role != null) {
								if (role.equals("Abbrev")){
									unit = MeasurementUnit.NewInstance(label,label,label);
								}
							} else {
								unit = MeasurementUnit.NewInstance(label,label,label);
							}
						}

						if (unit != null) {
							units.put(idQC, unit);
						}

						//<Default>
						//  <MeasurementUnitPrefix>milli</MeasurementUnitPrefix>
						//</Default>
						Element elDefault = elQuantitativeCharacter.getChild("Default",sddNamespace);
						if (elDefault != null) {
							String measurementUnitPrefix = (String)ImportHelper.getXmlInputValue(elDefault, "MeasurementUnitPrefix",sddNamespace);
							if (!measurementUnitPrefix.equals("")){
								defaultUnitPrefixes.put(idQC, measurementUnitPrefix);
							}
						}

						features.put(idQC, quantitativeCharacter);

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of QuantitativeCharacter " + j + " failed.");
						success = false; 
					}

					if ((++j % modCount) == 0){ logger.info("QuantitativeCharacters handled: " + j);}

				}

				// <TextCharacter id="c3">
				List<Element> elTextCharacters = elCharacters.getChildren("TextCharacter", sddNamespace);
				j = 0;
				//for each TextCharacter
				for (Element elTextCharacter : elTextCharacters){

					try {

						String idTC = elTextCharacter.getAttributeValue("id");

						// <Representation>
						//  <Label xml:lang="en">Leaf features not covered by other characters</Label>
						// </Representation>
						elRepresentation = elTextCharacter.getChild("Representation",sddNamespace);
						Element elLabel = elRepresentation.getChild("Label",sddNamespace);
						nameLang = elLabel.getAttributeValue("lang",xmlNamespace);
						Language language = null;
						if (nameLang != null) {
							if (!nameLang.equals("")) {
								language = config.getCdmAppController().getTermService().getLanguageByIso(nameLang.substring(0, 2));
								//language = datasetLanguage;
							} else {
								language = datasetLanguage;
							}
						} else {
							language = datasetLanguage;
						}

						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
						detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
						
						Feature textCharacter = null;
						if (label != null){
							if (detail != null) {
								textCharacter = Feature.NewInstance(detail,label,label);
							} else {
								textCharacter = Feature.NewInstance(label,label,label);
							}
						}

						if (label != null) {
							if (!label.equals("")){
								textCharacter = Feature.NewInstance(label, label, label);
								textCharacter.setLabel(label, language);
							}
						}

						textCharacter.setSupportsQuantitativeData(false);
						textCharacter.setSupportsTextData(true);

						features.put(idTC, textCharacter);

						listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

						for (Element elMediaObject : listMediaObjects) {
							String ref = null;
							String role = null;
							if (elMediaObject != null) {
								ref = elMediaObject.getAttributeValue("ref");
								role = elMediaObject.getAttributeValue("role");
							}
							if (ref != null) {
								if (!ref.equals("")) {
									this.addCdmBaseWithImage(ref,textCharacter);
								}
							}
						}

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of TextCharacter " + j + " failed.");
						success = false; 
					}

					if ((++j % modCount) == 0){ logger.info("TextCharacters handled: " + j);}

				}

			}

			// <CodedDescriptions>
			logger.info("start CodedDescriptions ...");
			Element elCodedDescriptions = elDataset.getChild("CodedDescriptions",sddNamespace);

			// <CodedDescription id="D101">

			Map<String,String> citations = new HashMap<String,String>();
			Map<String,String> locations = new HashMap<String,String>();

			if (elCodedDescriptions != null) {
				List<Element> listCodedDescriptions = elCodedDescriptions.getChildren("CodedDescription", sddNamespace);
				j = 0;
				//for each CodedDescription

				for (Element elCodedDescription : listCodedDescriptions){

					try {

						String idCD = elCodedDescription.getAttributeValue("id");

						// <Representation>
						//  <Label>&lt;i&gt;Viola hederacea&lt;/i&gt; Labill. as revised by R. Morris April 8, 2006</Label>
						// </Representation>
						elRepresentation = elCodedDescription.getChild("Representation",sddNamespace);
						Element elLabel = elRepresentation.getChild("Label",sddNamespace);
						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
						detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);

						TaxonDescription taxonDescription = TaxonDescription.NewInstance();
						
						if (detail != null) {
							Annotation annotation = Annotation.NewInstance(detail, datasetLanguage);
							taxonDescription.addAnnotation(annotation);
						}
						
						taxonDescription.setTitleCache(label);
						OriginalSource source = OriginalSource.NewInstance(idCD, "CodedDescription",sec,"");
						taxonDescription.addSource(source);

						// <Scope>
						//  <TaxonName ref="t1"/>
						//  <Citation ref="p1" location="p. 30"/>
						// </Scope>
						Element elScope = elCodedDescription.getChild("Scope",sddNamespace);
						String ref = "";
						Taxon taxon = null;
						if (elScope != null) {
							Element elTaxonName = elScope.getChild("TaxonName",sddNamespace);
							ref = elTaxonName.getAttributeValue("ref");

							NonViralName taxonNameBase = taxonNameBases.get(ref);
							taxon = Taxon.NewInstance(taxonNameBase, sec);
						}

						String refCitation = "";
						String location = "";

						if (elScope != null) {
							Element elCitation = elScope.getChild("Citation",sddNamespace);
							if (elCitation != null) {
								refCitation = elCitation.getAttributeValue("ref");
								location = elCitation.getAttributeValue("location");
							}
						}

						// <SummaryData>
						Element elSummaryData = elCodedDescription.getChild("SummaryData",sddNamespace);

						if (elSummaryData != null) {

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
									StateData stateData = stateDatas.get(ref);
									categoricalData.addState(stateData);
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
								if (unit != null) {
									String u = unit.getLabel();
									if (prefix != null) {
										u = prefix + u;
									}
									unit.setLabel(u);
									quantitativeData.setUnit(unit);
								}

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

						}

						if (taxon != null) {
							taxon.addDescription(taxonDescription);
						}

						if (!refCitation.equals("")){
							citations.put(idCD,refCitation);
						}

						if (!location.equals("")){
							locations.put(idCD, location);
						}

						taxonDescriptions.put(idCD, taxonDescription);

						listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

						for (Element elMediaObject : listMediaObjects) {
							ref = null;
							String role = null;
							if (elMediaObject != null) {
								ref = elMediaObject.getAttributeValue("ref");
								role = elMediaObject.getAttributeValue("role");
							}
							if (ref != null) {
								if (!ref.equals("")) {
									if (taxonDescription.getDescriptionSources().toArray().length > 0) {
										this.addCdmBaseWithImage(ref,(ReferenceBase) taxonDescription.getDescriptionSources().toArray()[0]);
									} else {
										ReferenceBase descriptionSource = Generic.NewInstance();
										taxonDescription.addDescriptionSource(descriptionSource);
										this.addCdmBaseWithImage(ref,descriptionSource);
									}

								}
							}
						}

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of CodedDescription " + j + " failed.");
						success = false; 
					}

					if ((++j % modCount) == 0){ logger.info("CodedDescriptions handled: " + j);}

				}

			}

			// <Agents>
			logger.info("start Agents ...");
			Element elAgents = elDataset.getChild("Agents",sddNamespace);

			// <Agent id="a1">
			listAgents = elAgents.getChildren("Agent", sddNamespace);
			j = 0;
			//for each Agent
			for (Element elAgent : listAgents){

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
					OriginalSource source = OriginalSource.NewInstance(idA, "Agent");
					person.addSource(source);

					if (elDetail != null) {
						String role = elDetail.getAttributeValue("role");
						detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);
						Annotation annotation = Annotation.NewInstance(role + " - " + detail, datasetLanguage);
						person.addAnnotation(annotation);
					}

					// <Links>
					Element elLinks = elAgent.getChild("Links",sddNamespace);

					if (elLinks != null) {

						//  <Link rel="Alternate" href="http://www.diversitycampus.net/people/hagedorn"/>
						List<Element> listLinks = elLinks.getChildren("Link", sddNamespace);
						int k = 0;
						//for each Link
						for (Element elLink : listLinks){

							try {

								String rel = elLink.getAttributeValue("rel");
								String href = elLink.getAttributeValue("href");

								if (k==1) {
									source = OriginalSource.NewInstance(rel, href);
									person.addSource(source);
								}

							} catch (Exception e) {
								//FIXME
								logger.warn("Import of Link " + k + " failed.");
								success = false; 
							}

							if ((++k % modCount) == 0){ logger.info("Links handled: " + k);}

						}
					}
					if (authors.containsKey(idA)) {
						authors.put(idA,person);
					}

					if (editors.containsKey(idA)) {
						editors.put(idA, person);
					}

					listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

					for (Element elMediaObject : listMediaObjects) {
						String ref = null;
						String role = null;
						if (elMediaObject != null) {
							ref = elMediaObject.getAttributeValue("ref");
							role = elMediaObject.getAttributeValue("role");
						}
						if (ref != null) {
							if (!ref.equals("")) {
								this.addCdmBaseWithImage(ref,person);
							}
						}
					}

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of Agent " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("Agents handled: " + j);}

			}

			// <Publications>
			logger.info("start Publications ...");
			Element elPublications = elDataset.getChild("Publications",sddNamespace);

			if (elPublications != null) {
				// <Publication id="p1">
				List<Element> listPublications = elPublications.getChildren("Publication", sddNamespace);
				j = 0;
				//for each Publication
				for (Element elPublication : listPublications){

					try {

						String idP = elPublication.getAttributeValue("id");

						//  <Representation>
						//   <Label>Sample Citation</Label>
						//  </Representation>
						elRepresentation = elPublication.getChild("Representation",sddNamespace);
						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
						Article publication = Article.NewInstance();
						publication.setTitle(label);
						OriginalSource source = OriginalSource.NewInstance(idP, "Publication");
						publication.addSource(source);

						publications.put(idP,publication);

						listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

						for (Element elMediaObject : listMediaObjects) {
							String ref = null;
							String role = null;
							if (elMediaObject != null) {
								ref = elMediaObject.getAttributeValue("ref");
								role = elMediaObject.getAttributeValue("role");
							}
							if (ref != null) {
								if (!ref.equals("")) {
									this.addCdmBaseWithImage(ref,publication);
								}
							}
						}

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of Publication " + j + " failed.");
						success = false; 
					}

					if ((++j % modCount) == 0){ logger.info("Publications handled: " + j);}

				}
			}

			// <MediaObjects>
			logger.info("start MediaObjects ...");
			Element elMediaObjects = elDataset.getChild("MediaObjects",sddNamespace);

			if (elMediaObjects != null) {
				// <MediaObject id="m1">
				listMediaObjects = elMediaObjects.getChildren("MediaObject", sddNamespace);
				j = 0;
				//for each Publication
				for (Element elMO : listMediaObjects){

					try {

						String idMO = elMO.getAttributeValue("id");

						//  <Representation>
						//   <Label>Image description, e.g. to be used for alt-attribute in html.</Label>
						//  </Representation>
						elRepresentation = elMO.getChild("Representation",sddNamespace);
						label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
						MultilanguageText m = MultilanguageText.NewInstance(LanguageString.NewInstance(label, datasetLanguage));
						// mediaObjects.get(idMO).setTitle(m);
						OriginalSource originalSource = OriginalSource.NewInstance(idMO, "MediaObject");
						// NO MEDIA SOURCE AVAILABLE
						// media.addSource();

						// <Type>Image</Type>
						// <Source href="http://test.edu/test.jpg"/>
						String type = (String)ImportHelper.getXmlInputValue(elMO,"Type",sddNamespace);
						Element elSource = elMO.getChild("Source",sddNamespace);
						String href = elSource.getAttributeValue("href");

						ImageMetaData imageMetaData = new ImageMetaData();
						ImageFile image = null;

						if (href.substring(0,7).equals("http://")) {
							try{
								URL url = new URL(href);
								imageMetaData.readFrom(url);
								image = ImageFile.NewInstance(url.toString(), null, imageMetaData);
							} catch (MalformedURLException e) {
								logger.error("Malformed URL", e);
							}
						} else {
							String sns = config.getSourceNameString();
							File f = new File(sns);
							File parent = f.getParentFile();
							String fi = parent.toString() + File.separator + href;
							File file = new File(fi);
							imageMetaData.readFrom(file);
							image = ImageFile.NewInstance(file.toString(), null, imageMetaData);
						}

						MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
						representation.addRepresentationPart(image);

						Media media = Media.NewInstance();
						media.addRepresentation(representation);
						// media.setTitle(m);

						ArrayList<CdmBase> lcb = (ArrayList<CdmBase>) mediaObject_ListCdmBase.get(idMO);
						if (lcb != null) {
							// for (int k = 0; k < lcb.size(); k++) {
							//	if (lcb.get(k) instanceof DefinedTermBase) {
							//		DefinedTermBase dtb = (DefinedTermBase) lcb.get(k);
							if (lcb.get(0) instanceof DefinedTermBase) {
								DefinedTermBase dtb = (DefinedTermBase) lcb.get(0);
								if (dtb!=null) {
									dtb.addMedia(media);
								}
								//} else if (lcb.get(k) instanceof ReferenceBase) {
								//	ReferenceBase rb = (ReferenceBase) lcb.get(k);
							} else if (lcb.get(0) instanceof ReferenceBase) {
								ReferenceBase rb = (ReferenceBase) lcb.get(0);
								// rb.setTitleCache(label);
								if (rb!=null) {
									rb.addMedia(media);
								}
							}
						}

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of MediaObject " + j + " failed.");
						success = false; 
					}

					if ((++j % modCount) == 0){ logger.info("MediaObjects handled: " + j);}

				}
			}

			if (authors != null) {
					Team team = Team.NewInstance();
					for (Iterator<Person> author = authors.values().iterator() ; author.hasNext() ;){
						team.addTeamMember(author.next());
					}
					sourceReference.setAuthorTeam(team);
				}
			
			if (editors != null) {
				Person ed = Person.NewInstance();
				for (Iterator<Person> editor = editors.values().iterator() ; editor.hasNext() ;){
					ed = editor.next();
				}
				sourceReference.setUpdatedBy(ed);
			}

			if (copyright != null) {
				sourceReference.addRights(copyright);
				sec.addRights(copyright);
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

			if ((++i % modCount) == 0){ logger.info("Datasets handled: " + i);}

			config.setSourceReference(sourceReference);

		}
		logger.info(i + " Datasets handled");

		ITermService termService = config.getCdmAppController().getTermService();
		for (Iterator<StateData> k = stateDatas.values().iterator() ; k.hasNext() ;){
			StateData sd = k.next();
			termService.saveTerm(sd.getState()); 
		}
		for (Iterator<Feature> k = features.values().iterator() ; k.hasNext() ;){
			Feature feature = k.next();
			termService.saveTerm(feature); 
		}
		if (units != null) {
			for (Iterator<MeasurementUnit> k = units.values().iterator() ; k.hasNext() ;){
				MeasurementUnit unit = k.next();
				if (unit != null) {
					termService.saveTerm(unit); 
				}
			}
		}
		for (Iterator<StatisticalMeasure> k = statisticalMeasures.iterator() ; k.hasNext() ;) {
			StatisticalMeasure sm = k.next();
			termService.saveTerm(sm); 
		}

		IReferenceService referenceService = config.getCdmAppController().getReferenceService();
		referenceService.saveReference(sourceReference); 
		for (Iterator<ReferenceBase> k = publications.values().iterator() ; k.hasNext() ;){
			Article publication = (Article) k.next();
			referenceService.saveReference(publication); 
		}

		// Returns a CdmApplicationController created by the values of this configuration.
		IDescriptionService descriptionService = config.getCdmAppController().getDescriptionService();

		for (Iterator<TaxonDescription> k = taxonDescriptions.values().iterator() ; k.hasNext() ;){
			TaxonDescription taxonDescription = k.next();
			// Persists a Description
			descriptionService.saveDescription(taxonDescription); 
		}

		config.getCdmAppController().commitTransaction(ts);

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

	protected void addCdmBaseWithImage(String refMO, CdmBase cb){
		if ((refMO != null) && (cb!=null)) {
			if (!refMO.equals("")) {
				if (!mediaObject_ListCdmBase.containsKey(refMO)) {
					List<CdmBase> lcb = new ArrayList<CdmBase>();
					lcb.add(cb);
					mediaObject_ListCdmBase.put(refMO,lcb);
				} else {
					List<CdmBase> lcb = mediaObject_ListCdmBase.get(refMO);
					lcb.add(cb);
					mediaObject_ListCdmBase.put(refMO,lcb);
				}
			}
		}
	}

}
