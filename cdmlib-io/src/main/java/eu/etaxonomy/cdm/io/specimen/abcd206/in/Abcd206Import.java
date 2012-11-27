/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportBase;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringArea;
import eu.etaxonomy.cdm.io.specimen.UnitsGatheringEvent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
@Component
public class Abcd206Import extends
		SpecimenImportBase<Abcd206ImportConfigurator, Abcd206ImportState>
		implements ICdmIO<Abcd206ImportState> {
	private static final Logger logger = Logger.getLogger(Abcd206Import.class);
	private static String prefix = "";

	private Classification classification = null;
	private Reference<?> ref = null;

	private Abcd206ImportState abcdstate;
	private Abcd206DataHolder dataHolder;
	private DerivedUnitFacade derivedUnitFacade;

	private HashMap<Taxon, SpecimenTypeDesignationStatus> taxontypemap;

	private TransactionStatus tx;

	public Abcd206Import() {
		super();
	}

	@Override
	protected boolean doCheck(Abcd206ImportState state) {
		logger.warn("Checking not yet implemented for "
				+ this.getClass().getSimpleName());
		this.abcdstate = state;
		return true;
	}

	private Classification getClassification(Abcd206ImportState state) {
		if (this.classification == null) {
			String name = state.getConfig().getClassificationName();

			this.classification = Classification.NewInstance(name, ref,
					Language.DEFAULT());
			if (state.getConfig().getClassificationUuid() != null) {
				classification.setUuid(state.getConfig()
						.getClassificationUuid());
			}
			getClassificationService().save(classification);
		}
		return this.classification;

	}

	@Override
	public void doInvoke(Abcd206ImportState state) {
		abcdstate = state;
		tx = startTransaction();

		logger.info("INVOKE Specimen Import from ABCD2.06 XML ");
		// AbcdIO test = new AbcdIO();
		URI sourceName = this.abcdstate.getConfig().getSource();
		NodeList unitsList = getUnitsNodeList(sourceName);

		String treeName = this.abcdstate.getConfig().getClassificationName();
		UUID treeUuid = this.abcdstate.getConfig().getClassificationUuid();
		// classification = Classification.NewInstance(treeName);
		// classification.setUuid(treeUuid);
		classification = getClassification(abcdstate);

		taxontypemap = new HashMap<Taxon, SpecimenTypeDesignationStatus>();

		ref = this.abcdstate.getConfig().getSourceReference();

		if (unitsList != null) {
			String message = "nb units to insert: " + unitsList.getLength();
			// logger.info(message);
			updateProgress(this.abcdstate, message);

			dataHolder = new Abcd206DataHolder();
			for (int i = 0; i < unitsList.getLength(); i++) {
				this.setUnitPropertiesXML((Element) unitsList.item(i));
				this.handleSingleUnit();

				// compare the ABCD elements added in to the CDM and the
				// unhandled ABCD elements
				compareABCDtoCDM(sourceName, dataHolder.knownABCDelements);

				// reset the ABCD elements added in CDM
				// knownABCDelements = new ArrayList<String>();
				dataHolder.allABCDelements = new HashMap<String, String>();
			}
		}
		commitTransaction(tx);
		return;

	}

	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	private void handleSingleUnit() {
		// logger.info("handleSingleUnit");
		Abcd206ImportConfigurator config = this.abcdstate.getConfig();

		try {
			updateProgress(this.abcdstate, "Importing data for unit: "
					+ dataHolder.unitID);

			// Reference sec = Database.NewInstance();
			// sec.setTitleCache("XML DATA");
			ref = config.getTaxonReference();

			// create facade
			derivedUnitFacade = getFacade();

			// handle identifications
			handleIdentifications(config);

			// handle collection data
			setCollectionData(config);

			/**
			 * GATHERING EVENT
			 */

			// gathering event
			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(
					getTermService(), dataHolder.locality,
					dataHolder.languageIso, dataHolder.longitude,
					dataHolder.latitude, dataHolder.gatheringAgentList);

			// country
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(
					dataHolder.isocountry, dataHolder.country,
					getOccurrenceService());
			NamedArea areaCountry = unitsGatheringArea.getArea();

			// other areas
			unitsGatheringArea = new UnitsGatheringArea(
					dataHolder.namedAreaList);
			ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
			for (NamedArea namedArea : nas) {
				unitsGatheringEvent.addArea(namedArea);
			}

			// copy gathering event to facade
			GatheringEvent gatheringEvent = unitsGatheringEvent
					.getGatheringEvent();
			derivedUnitFacade.setLocality(gatheringEvent.getLocality());
			derivedUnitFacade.setExactLocation(gatheringEvent
					.getExactLocation());
			derivedUnitFacade.setCollector(gatheringEvent.getCollector());
			derivedUnitFacade.setCountry(areaCountry);
			derivedUnitFacade.addCollectingAreas(unitsGatheringArea.getAreas());

			// TODO exsiccatum

			// add fieldNumber
			derivedUnitFacade.setFieldNumber(dataHolder.fieldNumber);

			// join gatheringEvent to fieldObservation

			// //add Multimedia URLs
			if (dataHolder.multimediaObjects.size() != -1) {
				for (String multimediaObject : dataHolder.multimediaObjects) {
					Media media = getImageMedia(multimediaObject,
							READ_MEDIA_DATA, false);
					derivedUnitFacade.addDerivedUnitMedia(media);
				}
			}

			/**
			 * SAVE AND STORE DATA
			 */
			getTermService().save(areaCountry);// TODO save area sooner
			for (NamedArea area : nas) {
				getTermService().save(area);// save it sooner (foreach area)
			}
			getTermService()
					.saveLanguageData(unitsGatheringEvent.getLocality());

			getOccurrenceService().save(derivedUnitFacade.innerDerivedUnit());
			getOccurrenceService().save(
					derivedUnitFacade.innerFieldObservation());
			logger.info("saved ABCD specimen ...");

		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			this.abcdstate.setUnsuccessfull();
		}

		return;
	}

	private void setCollectionData(Abcd206ImportConfigurator config) {
		// set catalogue number (unitID)
		derivedUnitFacade.setCatalogNumber(dataHolder.unitID);
		derivedUnitFacade.setAccessionNumber(dataHolder.accessionNumber);
		// derivedUnitFacade.setCollectorsNumber(dataHolder.collectorsNumber);

		/**
		 * INSTITUTION & COLLECTION
		 */
		// manage institution
		Institution institution = this.getInstitution(
				dataHolder.institutionCode, config);
		// manage collection
		Collection collection = this.getCollection(dataHolder.collectionCode,
				institution, config);
		// link specimen & collection
		derivedUnitFacade.setCollection(collection);
	}

	private DerivedUnitFacade getFacade() {
		/**
		 * SPECIMEN OR OBSERVATION OR LIVING
		 */
		// DerivedUnitBase derivedThing = null;
		DerivedUnitType type = null;

		// create specimen
		if (dataHolder.recordBasis != null) {
			if (dataHolder.recordBasis.toLowerCase().startsWith("s")
					|| dataHolder.recordBasis.toLowerCase()
							.contains("specimen")) {// specimen
				type = DerivedUnitType.Specimen;
			}
			if (dataHolder.recordBasis.toLowerCase().startsWith("o")) {
				type = DerivedUnitType.Observation;
			}
			if (dataHolder.recordBasis.toLowerCase().contains("fossil"))
				type = DerivedUnitType.Fossil;

			if (dataHolder.recordBasis.toLowerCase().startsWith("l")) {
				type = DerivedUnitType.LivingBeing;
			}
			if (type == null) {
				logger.info("The basis of record does not seem to be known: "
						+ dataHolder.recordBasis);
				type = DerivedUnitType.DerivedUnit;
			}
			// TODO fossils?
		} else {
			logger.info("The basis of record is null");
			type = DerivedUnitType.DerivedUnit;
		}
		DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade
				.NewInstance(type);

		return derivedUnitFacade;
	}

	/*
	 * Return the list of root nodes for an ABCD 2.06 XML file
	 * 
	 * @param fileName: the file's location
	 * 
	 * @return the list of root nodes ("Unit")
	 */
	private static NodeList getUnitsNodeList(URI urlFileName) {
		NodeList unitList = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			URL url = urlFileName.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream) o;
			Document document = builder.parse(is);
			Element root = document.getDocumentElement();
			// logger.info("root nodename " + root.getNodeName());
			unitList = root.getElementsByTagName("Unit");
			if (unitList.getLength() == 0) {
				unitList = root.getElementsByTagName("abcd:Unit");
				prefix = "abcd:";
			}
		} catch (Exception e) {
			logger.warn(e);
		}
		return unitList;
	}

	/*
	 * Store the unit's properties into variables Look which unit is the
	 * preferred one Look what kind of name it is supposed to be, for the
	 * parsing (Botanical, Zoological)
	 * 
	 * @param racine: the root node for a single unit
	 */
	private void setUnitPropertiesXML(Element root) {
		try {
			NodeList group;

			// try{afficherInfos(racine, 0);}catch (Exception e)
			// {logger.info(e);}
			group = root.getChildNodes();
			// logger.info("prefix et ident : "+prefix+"Identifications");
			// logger.info("ABCD ELEMENT not stored: "+group.item(i).getNodeName().toString()+" - value: "+group.item(i).getTextContent());
			for (int i = 0; i < group.getLength(); i++) {
				if (group.item(i).getNodeName()
						.equals(prefix + "Identifications")) {
					group = group.item(i).getChildNodes();
					break;
				}
			}
			dataHolder.identificationList = new ArrayList<String>();
			dataHolder.statusList = new ArrayList<SpecimenTypeDesignationStatus>();
			dataHolder.atomisedIdentificationList = new ArrayList<HashMap<String, String>>();
			dataHolder.referenceList = new ArrayList<String>();
			dataHolder.multimediaObjects = new ArrayList<String>();

			this.getScientificNames(group);
			this.getType(root);

			// logger.info("this.identificationList "+this.identificationList.toString());
			this.getIDs(root);
			this.getRecordBasis(root);
			this.getMultimedia(root);
			this.getNumbers(root);
			this.getGeolocation(root);
			this.getGatheringPeople(root);
			boolean referencefound = this.getReferences(root);
			if (!referencefound) {
				dataHolder.referenceList.add(ref.getTitleCache());
			}

		} catch (Exception e) {
			logger.info("Error occured while parsing XML file" + e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#
	 * getSpecimenTypeDesignationStatusByKey(java.lang.String)
	 */
	public SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(
			String key) {
		if (key == null) {
			return null;
		} else if (key.matches("(?i)(T|Type)")) {
			return SpecimenTypeDesignationStatus.TYPE();
		} else if (key.matches("(?i)(HT|Holotype)")) {
			return SpecimenTypeDesignationStatus.HOLOTYPE();
		} else if (key.matches("(?i)(LT|Lectotype)")) {
			return SpecimenTypeDesignationStatus.LECTOTYPE();
		} else if (key.matches("(?i)(NT|Neotype)")) {
			return SpecimenTypeDesignationStatus.NEOTYPE();
		} else if (key.matches("(?i)(ST|Syntype)")) {
			return SpecimenTypeDesignationStatus.SYNTYPE();
		} else if (key.matches("(?i)(ET|Epitype)")) {
			return SpecimenTypeDesignationStatus.EPITYPE();
		} else if (key.matches("(?i)(IT|Isotype)")) {
			return SpecimenTypeDesignationStatus.ISOTYPE();
		} else if (key.matches("(?i)(ILT|Isolectotype)")) {
			return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
		} else if (key.matches("(?i)(INT|Isoneotype)")) {
			return SpecimenTypeDesignationStatus.ISONEOTYPE();
		} else if (key.matches("(?i)(IET|Isoepitype)")) {
			return SpecimenTypeDesignationStatus.ISOEPITYPE();
		} else if (key.matches("(?i)(PT|Paratype)")) {
			return SpecimenTypeDesignationStatus.PARATYPE();
		} else if (key.matches("(?i)(PLT|Paralectotype)")) {
			return SpecimenTypeDesignationStatus.PARALECTOTYPE();
		} else if (key.matches("(?i)(PNT|Paraneotype)")) {
			return SpecimenTypeDesignationStatus.PARANEOTYPE();
		} else if (key.matches("(?i)(unsp.|Unspecified)")) {
			return SpecimenTypeDesignationStatus.UNSPECIFIC();
		} else if (key.matches("(?i)(2LT|Second Step Lectotype)")) {
			return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
		} else if (key.matches("(?i)(2NT|Second Step Neotype)")) {
			return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
		} else if (key.matches("(?i)(OM|Original Material)")) {
			return SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL();
		} else if (key.matches("(?i)(IcT|Iconotype)")) {
			return SpecimenTypeDesignationStatus.ICONOTYPE();
		} else if (key.matches("(?i)(PT|Phototype)")) {
			return SpecimenTypeDesignationStatus.PHOTOTYPE();
		} else if (key.matches("(?i)(IST|Isosyntype)")) {
			return SpecimenTypeDesignationStatus.ISOSYNTYPE();
		} else {
			return null;
		}
	}

	private void getType(Node result) {
		// logger.info("GET REFERENCE");
		NodeList results, types, ntds, ntd;
		String type;
		results = result.getChildNodes();
		try {
			for (int k = 0; k < results.getLength(); k++) {
				if (results.item(k).getNodeName()
						.equals(prefix + "SpecimenUnit")) {
					types = results.item(k).getChildNodes();
					for (int l = 0; l < types.getLength(); l++) {
						if (types
								.item(l)
								.getNodeName()
								.equals(prefix
										+ "NomenclaturalTypeDesignations")) {
							ntds = types.item(l).getChildNodes();
							for (int m = 0; m < ntds.getLength(); m++) {
								if (ntds.item(m)
										.getNodeName()
										.equals(prefix
												+ "NomenclaturalTypeDesignation")) {
									ntd = ntds.item(m).getChildNodes();
									for (int n = 0; n < ntd.getLength(); n++)
										if (ntd.item(n).getNodeName()
												.equals(prefix + "TypeStatus")) {
											type = ntd.item(n).getTextContent();
											dataHolder.statusList
													.add(getSpecimenTypeDesignationStatusByKey(type));
											path = ntd.item(l).getNodeName();
											getHierarchie(ntd.item(l));
											dataHolder.knownABCDelements
													.add(path);
											path = "";
										}

								}
							}

						}
					}
				}
			}
		} catch (NullPointerException e) {
			dataHolder.statusList = new ArrayList<SpecimenTypeDesignationStatus>();
		}
	}

	String path = "";

	private void getHierarchie(Node node) {
		// logger.info("getHierarchie");
		while (node != null && node.getNodeName() != prefix + "DataSets"
				&& node.getParentNode() != null) {
			// logger.info("nodeparent "+node.getParentNode().getNodeName());
			path = node.getParentNode().getNodeName() + "/" + path;
			node = node.getParentNode();
		}
		// logger.info("path gethierarchie: "+path);
	}

	private void getScientificNames(NodeList group) {
		NodeList identifications, results;
		String tmpName = null;
		boolean nameFound = false;

		for (int j = 0; j < group.getLength(); j++) {
			if (group.item(j).getNodeName().equals(prefix + "Identification")) {
				identifications = group.item(j).getChildNodes();
				for (int m = 0; m < identifications.getLength(); m++) {
					if (identifications.item(m).getNodeName()
							.equals(prefix + "Result")) {
						results = identifications.item(m).getChildNodes();
						for (int k = 0; k < results.getLength(); k++) {

							if (results.item(k).getNodeName()
									.equals(prefix + "TaxonIdentified")) {
								tmpName = this.getScientificName(results
										.item(k));
								// logger.info("TMP NAME " + tmpName);
								dataHolder.identificationList.add(tmpName);
								nameFound = true;
							}

						}
					} else if (identifications.item(m).getNodeName()
							.equals(prefix + "PreferredFlag")) {
						if (dataHolder.nomenclatureCode != null
								&& dataHolder.nomenclatureCode != "") {
							// logger.info("TMP NAME P" + tmpName);

							dataHolder.identificationList.add(tmpName
									+ "_preferred_"
									+ identifications.item(m).getTextContent()
									+ "_code_" + dataHolder.nomenclatureCode);
						} else {
							dataHolder.identificationList.add(tmpName
									+ "_preferred_"
									+ identifications.item(m).getTextContent());
						}
						path = identifications.item(m).getNodeName();
						// getHierarchie(identifications.item(m));
						dataHolder.knownABCDelements.add(path);
						path = "";
						try {
							dataHolder.identificationList.remove(tmpName);
						} catch (Exception e) {
							logger.info("ohooooooooooo:" + e);
						}
					} else if (identifications.item(m).getNodeName()
							.equals(prefix + "References")) {
						this.getReferences(identifications.item(m));
					}
				}
			}
		}
		boolean hasPref = false;
		for (int j = 0; j < group.getLength(); j++) {
			if (group.item(j).getNodeName().equals(prefix + "Identification")) {
				dataHolder.nomenclatureCode = "";
				identifications = group.item(j).getChildNodes();
				for (int m = 0; m < identifications.getLength(); m++) {
					if (identifications.item(m).getNodeName()
							.equals(prefix + "Result")) {
						results = identifications.item(m).getChildNodes();
						for (int k = 0; k < results.getLength(); k++) {
							if (results.item(k).getNodeName()
									.equals(prefix + "TaxonIdentified")) {
								tmpName = this.getScientificName(results
										.item(k));
							}
						}
					}
					if (identifications.item(m).getNodeName()
							.equals(prefix + "PreferredFlag")) {
						hasPref = true;
					}
				}
				if (!hasPref && tmpName != null) {
					if (dataHolder.nomenclatureCode != null
							&& dataHolder.nomenclatureCode != "") {
						dataHolder.identificationList.add(tmpName
								+ "_preferred_" + "0" + "_code_"
								+ dataHolder.nomenclatureCode);
					} else {
						dataHolder.identificationList.add(tmpName
								+ "_preferred_" + "0");
					}
					try {
						dataHolder.identificationList.remove(tmpName);
					} catch (Exception e) {
						logger.info("ohooooooooooo:" + e);
					}
				}
			}
		}
	}

	private boolean getReferences(Node result) {
		// logger.info("GET REFERENCE");
		NodeList results, reference;
		results = result.getChildNodes();
		boolean referencefound = false;
		for (int k = 0; k < results.getLength(); k++) {
			if (results.item(k).getNodeName().equals(prefix + "Reference")) {
				reference = results.item(k).getChildNodes();
				for (int l = 0; l < reference.getLength(); l++) {
					if (reference.item(l).getNodeName()
							.equals(prefix + "TitleCitation")) {
						path = reference.item(l).getNodeName();
						dataHolder.referenceList.add(reference.item(l)
								.getTextContent());
						getHierarchie(reference.item(l));
						dataHolder.knownABCDelements.add(path);
						path = "";
						referencefound = true;
					}
				}
			}
		}
		return referencefound;
	}

	private String getScientificName(Node result) {
		// logger.info("IN getScientificName " + dataHolder.nomenclatureCode);
		NodeList taxonsIdentified, scnames, atomised;
		String tmpName = "";
		dataHolder.atomisedStr = "";
		taxonsIdentified = result.getChildNodes();
		for (int l = 0; l < taxonsIdentified.getLength(); l++) {

			if (taxonsIdentified.item(l).getNodeName()
					.equals(prefix + "ScientificName")) {
				scnames = taxonsIdentified.item(l).getChildNodes();
				for (int n = 0; n < scnames.getLength(); n++) {

					if (scnames.item(n).getNodeName()
							.equals(prefix + "FullScientificNameString")) {
						path = scnames.item(n).getNodeName();
						tmpName = scnames.item(n).getTextContent();
						getHierarchie(scnames.item(n));
						dataHolder.knownABCDelements.add(path);
						path = "";
					}
					if (scnames.item(n).getNodeName()
							.equals(prefix + "NameAtomised")) {
						try {
							if (scnames.item(n).hasChildNodes()) {
								String tmp = scnames.item(n).getChildNodes()
										.item(1).getNodeName();
								if (tmp.indexOf(prefix) != -1
										&& prefix.length() > 0)
									dataHolder.nomenclatureCode = tmp
											.split(prefix)[1];
								else {
									dataHolder.nomenclatureCode = scnames
											.item(n).getChildNodes().item(1)
											.getNodeName();
								}
							}
						} catch (Exception e) {
							logger.warn("PB nomenclaturecode");
							dataHolder.nomenclatureCode = "";
						}
						atomised = scnames.item(n).getChildNodes().item(1)
								.getChildNodes();
						dataHolder.atomisedIdentificationList.add(this
								.getAtomisedNames(dataHolder.nomenclatureCode,
										atomised));
					}
				}
			}
		}
		return tmpName;
	}

	private HashMap<String, String> getAtomisedNames(String code,
			NodeList atomised) {
		logger.info("code getatomised " + code);
		if (code.indexOf("Botanical") != -1) {
			return this.getAtomisedBotanical(atomised);
		}
		if (code.indexOf("Bacterial") != -1) {
			return this.getAtomisedBacterial(atomised);
		}
		if (code.indexOf("Viral") != -1) {
			return this.getAtomisedViral(atomised);
		}
		if (code.indexOf("Zoological") != -1) {
			return this.getAtomisedZoological(atomised);
		}
		return new HashMap<String, String>();
	}

	private HashMap<String, String> getAtomisedZoological(NodeList atomised) {
		logger.info("getAtomisedZoo");
		HashMap<String, String> atomisedMap = new HashMap<String, String>();

		for (int i = 0; i < atomised.getLength(); i++) {
			if (atomised.item(i).getNodeName()
					.equals(prefix + "GenusOrMonomial")) {
				atomisedMap.put("Genus", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "Subgenus")) {
				atomisedMap.put("Subgenus", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "SpeciesEpithet")) {
				atomisedMap.put("SpeciesEpithet", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "SubspeciesEpithet")) {
				atomisedMap.put("SubspeciesEpithet", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "AuthorTeamOriginalAndYear")) {
				atomisedMap.put("AuthorTeamOriginalAndYear", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "AuthorTeamParenthesisAndYear")) {
				atomisedMap.put("AuthorTeamParenthesisAndYear", atomised
						.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "CombinationAuthorTeamAndYear")) {
				atomisedMap.put("CombinationAuthorTeamAndYear", atomised
						.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "Breed")) {
				atomisedMap.put("Breed", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "NamedIndividual")) {
				atomisedMap.put("NamedIndividual", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
		}
		return atomisedMap;
	}

	private HashMap<String, String> getAtomisedViral(NodeList atomised) {
		HashMap<String, String> atomisedMap = new HashMap<String, String>();
		for (int i = 0; i < atomised.getLength(); i++) {
			if (atomised.item(i).getNodeName()
					.equals(prefix + "GenusOrMonomial")) {
				atomisedMap.put("Genus", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "ViralSpeciesDesignation")) {
				atomisedMap.put("ViralSpeciesDesignation", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "Acronym")) {
				atomisedMap.put("Acronym", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
		}
		return atomisedMap;
	}

	private HashMap<String, String> getAtomisedBotanical(NodeList atomised) {
		HashMap<String, String> atomisedMap = new HashMap<String, String>();
		for (int i = 0; i < atomised.getLength(); i++) {
			if (atomised.item(i).getNodeName()
					.equals(prefix + "GenusOrMonomial")) {
				atomisedMap.put("Genus", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "FirstEpithet")) {
				atomisedMap.put("FirstEpithet", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "InfraspecificEpithet")) {
				atomisedMap.put("InfraSpeEpithet", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "Rank")) {
				atomisedMap.put("Rank", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "HybridFlag")) {
				atomisedMap
						.put("HybridFlag", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "AuthorTeamParenthesis")) {
				atomisedMap.put("AuthorTeamParenthesis", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "AuthorTeam")) {
				atomisedMap
						.put("AuthorTeam", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "CultivarGroupName")) {
				atomisedMap.put("CultivarGroupName", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "CultivarName")) {
				atomisedMap.put("CultivarName", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "TradeDesignationNames")) {
				atomisedMap.put("Trade", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
		}
		return atomisedMap;
	}

	private HashMap<String, String> getAtomisedBacterial(NodeList atomised) {
		HashMap<String, String> atomisedMap = new HashMap<String, String>();
		for (int i = 0; i < atomised.getLength(); i++) {
			if (atomised.item(i).getNodeName()
					.equals(prefix + "GenusOrMonomial")) {
				atomisedMap.put("Genus", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName().equals(prefix + "Subgenus")) {
				atomisedMap.put("SubGenus", atomised.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "SubgenusAuthorAndYear")) {
				atomisedMap.put("SubgenusAuthorAndYear", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "SpeciesEpithet")) {
				atomisedMap.put("SpeciesEpithet", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "SubspeciesEpithet")) {
				atomisedMap.put("SubspeciesEpithet", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "ParentheticalAuthorTeamAndYear")) {
				atomisedMap.put("ParentheticalAuthorTeamAndYear", atomised
						.item(i).getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "AuthorTeamAndYear")) {
				atomisedMap.put("AuthorTeamAndYear", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
			if (atomised.item(i).getNodeName()
					.equals(prefix + "NameApprobation")) {
				atomisedMap.put("NameApprobation", atomised.item(i)
						.getTextContent());
				path = atomised.item(i).getNodeName();
				getHierarchie(atomised.item(i));
				dataHolder.knownABCDelements.add(path);
				path = "";
			}
		}
		return atomisedMap;
	}

	private void getIDs(Element root) {
		NodeList group;
		try {
			group = root.getElementsByTagName(prefix + "SourceInstitutionID");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.institutionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.institutionCode = "";
		}
		try {
			group = root.getElementsByTagName(prefix + "SourceID");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.collectionCode = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.collectionCode = "";
		}
		try {
			group = root.getElementsByTagName(prefix + "UnitID");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.unitID = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.unitID = "";
		}
	}

	private void getRecordBasis(Element root) {
		NodeList group;
		try {
			group = root.getElementsByTagName(prefix + "RecordBasis");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.recordBasis = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.recordBasis = "";
		}
	}

	private void getMultimedia(Element root) {
		NodeList group, multimedias, multimedia;
		try {
			group = root.getElementsByTagName(prefix + "MultiMediaObjects");
			for (int i = 0; i < group.getLength(); i++) {
				multimedias = group.item(i).getChildNodes();
				for (int j = 0; j < multimedias.getLength(); j++) {
					if (multimedias.item(j).getNodeName()
							.equals(prefix + "MultiMediaObject")) {
						multimedia = multimedias.item(j).getChildNodes();
						for (int k = 0; k < multimedia.getLength(); k++) {
							if (multimedia.item(k).getNodeName()
									.equals(prefix + "FileURI")) {
								dataHolder.multimediaObjects.add(multimedia
										.item(k).getTextContent());
								path = multimedia.item(k).getNodeName();
								getHierarchie(multimedia.item(k));
								dataHolder.knownABCDelements.add(path);
								path = "";
							}
						}
					}
				}
			}
		} catch (NullPointerException e) {
			logger.info(e);
		}
	}

	private void getNumbers(Element root) {
		NodeList group;
		try {
			group = root.getElementsByTagName(prefix + "AccessionNumber");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.accessionNumber = "";
		}
		try {
			group = root.getElementsByTagName(prefix + "CollectorsFieldNumber");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.fieldNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.fieldNumber = "";
		}

		try {
			group = root.getElementsByTagName(prefix + "AccessionNumber");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.accessionNumber = group.item(0).getTextContent();
		} catch (NullPointerException e) {
			dataHolder.accessionNumber = "";
		}
	}

	private void getGeolocation(Element root) {
		NodeList group, childs;
		try {
			group = root.getElementsByTagName(prefix + "LocalityText");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.locality = group.item(0).getTextContent();
			if (group.item(0).hasAttributes())
				if (group.item(0).getAttributes().getNamedItem("lang") != null)
					dataHolder.languageIso = group.item(0).getAttributes()
							.getNamedItem("lang").getTextContent();
		} catch (NullPointerException e) {
			dataHolder.locality = "";
		}
		try {
			group = root.getElementsByTagName(prefix + "LongitudeDecimal");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.longitude = Double.valueOf(group.item(0)
					.getTextContent());
		} catch (NullPointerException e) {
			dataHolder.longitude = null;
		}
		try {
			group = root.getElementsByTagName(prefix + "LatitudeDecimal");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.latitude = Double
					.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			dataHolder.latitude = null;
		}
		try {
			group = root.getElementsByTagName(prefix + "Country");
			childs = group.item(0).getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				if (childs.item(i).getNodeName() == "Name") {
					path = childs.item(i).getNodeName();
					getHierarchie(childs.item(i));
					dataHolder.knownABCDelements.add(path);
					path = "";
					dataHolder.country = childs.item(i).getTextContent();
				}
			}
		} catch (NullPointerException e) {
			dataHolder.country = "";
		}
		try {
			group = root.getElementsByTagName(prefix + "Country");
			childs = group.item(0).getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				if (childs.item(i).getNodeName() == "ISO3166Code") {
					path = childs.item(i).getNodeName();
					getHierarchie(childs.item(i));
					dataHolder.knownABCDelements.add(path);
					path = "";
					dataHolder.isocountry = childs.item(i).getTextContent();
				}
			}
		} catch (NullPointerException e) {
			dataHolder.isocountry = "";
		}
		try {
			group = root.getElementsByTagName(prefix + "Altitude");
			for (int i = 0; i < group.getLength(); i++) {
				childs = group.item(i).getChildNodes();
				for (int j = 0; j < childs.getLength(); j++) {
					if (childs.item(j).getNodeName()
							.equals(prefix + "MeasurementOrFactText")) {
						path = childs.item(j).getNodeName();
						getHierarchie(childs.item(j));
						dataHolder.knownABCDelements.add(path);
						path = "";
						dataHolder.altitude = Integer.valueOf(childs.item(j)
								.getTextContent());
					}
				}
			}
		} catch (NullPointerException e) {
			dataHolder.altitude = -9999;
		}

		try {
			group = root.getElementsByTagName(prefix + "Depth");
			path = group.item(0).getNodeName();
			getHierarchie(group.item(0));
			dataHolder.knownABCDelements.add(path);
			path = "";
			dataHolder.depth = Integer.valueOf(group.item(0).getTextContent());
		} catch (NullPointerException e) {
			dataHolder.depth = -9999;
		}

		try {
			group = root.getElementsByTagName(prefix + "NamedArea");
			dataHolder.namedAreaList = new ArrayList<String>();
			for (int i = 0; i < group.getLength(); i++) {
				childs = group.item(i).getChildNodes();
				for (int j = 0; j < childs.getLength(); j++) {
					if (childs.item(j).getNodeName()
							.equals(prefix + "AreaName")) {
						path = childs.item(j).getNodeName();
						getHierarchie(childs.item(j));
						dataHolder.knownABCDelements.add(path);
						path = "";
						dataHolder.namedAreaList.add(childs.item(j)
								.getTextContent());
					}
				}
			}
		} catch (NullPointerException e) {
			dataHolder.namedAreaList = new ArrayList<String>();
		}
	}

	private void getGatheringPeople(Element root) {
		NodeList group, childs, person;
		try {
			group = root.getElementsByTagName(prefix + "GatheringAgent");
			dataHolder.gatheringAgentList = new ArrayList<String>();
			for (int i = 0; i < group.getLength(); i++) {
				childs = group.item(i).getChildNodes();
				for (int j = 0; j < childs.getLength(); j++) {
					if (childs.item(j).getNodeName().equals(prefix + "Person")) {
						person = childs.item(j).getChildNodes();
						for (int k = 0; k < person.getLength(); k++) {
							if (person.item(k).getNodeName()
									.equals(prefix + "FullName")) {
								path = person.item(k).getNodeName();
								getHierarchie(person.item(k));
								dataHolder.knownABCDelements.add(path);
								path = "";
								dataHolder.gatheringAgentList.add(person
										.item(k).getTextContent());
							}
						}
					}

				}
			}
		} catch (NullPointerException e) {
			dataHolder.gatheringAgentList = new ArrayList<String>();
		}
	}

	private Institution getInstitution(String institutionCode,
			Abcd206ImportConfigurator config) {
		Institution institution;
		List<Institution> institutions;
		try {
			// logger.info(dataHolder.institutionCode);
			institutions = getAgentService().searchInstitutionByCode(
					dataHolder.institutionCode);
		} catch (Exception e) {
			institutions = new ArrayList<Institution>();
		}
		if (institutions.size() == 0 || !config.isReUseExistingMetadata()) {
			// logger.info("Institution (agent) unknown or not allowed to reuse existing metadata");
			// create institution
			institution = Institution.NewInstance();
			institution.setCode(dataHolder.institutionCode);
		} else {
			// logger.info("Institution (agent) already in the db");
			institution = institutions.get(0);
		}
		logger.info("getinstitution " + institution.toString());
		return institution;
	}

	/*
	 * Look if the Collection does already exists
	 * 
	 * @param collectionCode: a string
	 * 
	 * @param institution: the current Institution
	 * 
	 * @param app
	 * 
	 * @return the Collection (existing or new)
	 */
	private Collection getCollection(String collectionCode,
			Institution institution, Abcd206ImportConfigurator config) {
		Collection collection = Collection.NewInstance();
		List<Collection> collections;
		try {
			collections = getCollectionService().searchByCode(
					dataHolder.collectionCode);
		} catch (Exception e) {
			collections = new ArrayList<Collection>();
		}
		if (collections.size() == 0 || !config.isReUseExistingMetadata()) {
			logger.info("Collection not found or do not reuse existing metadata  "
					+ dataHolder.collectionCode);
			// create new collection
			collection.setCode(dataHolder.collectionCode);
			collection.setCodeStandard("GBIF");
			collection.setInstitute(institution);
		} else {
			boolean collectionFound = false;
			for (int i = 0; i < collections.size(); i++) {
				collection = collections.get(i);
				try {
					if (collection.getInstitute().getCode()
							.equalsIgnoreCase(institution.getCode())) {
						// found a collection with the same code and the same
						// institution
						collectionFound = true;
						break;
					}
				} catch (NullPointerException e) {
				}
			}
			if (!collectionFound) {
				collection.setCode(dataHolder.collectionCode);
				collection.setCodeStandard("GBIF");
				collection.setInstitute(institution);
			}

		}
		return collection;
	}

	private void linkDeterminationEvent(Taxon taxon, boolean preferredFlag,
			Abcd206ImportConfigurator config) {

		DeterminationEvent determinationEvent = DeterminationEvent
				.NewInstance();
		determinationEvent.setTaxon(taxon);
		determinationEvent.setPreferredFlag(preferredFlag);

		try {
			for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : dataHolder.statusList) {
				Specimen specimen = CdmBase.deproxy(
						derivedUnitFacade.innerDerivedUnit(), Specimen.class);
				TaxonNameBase name = taxon.getName();
				SpecimenTypeDesignation specimenTypeDesignation = name
						.addSpecimenTypeDesignation(specimen,
								specimenTypeDesignationstatus, ref, "",
								taxon.getName().getFullTitleCache(), false, false);
				derivedUnitFacade.innerDerivedUnit()
						.addSpecimenTypeDesignation(specimenTypeDesignation);
			}
		} catch (Exception e) {
			logger.warn("PB addding SpecimenType " + e);
		}

		logger.info("linkdetermination " + taxon.hashCode());
		for (String strReference : dataHolder.referenceList) {
			// logger.info("setReference "+strReference);
			Reference<?> reference = ReferenceFactory.newGeneric();
			reference.setTitleCache(strReference, true);
			determinationEvent.addReference(reference);
		}
		derivedUnitFacade.addDetermination(determinationEvent);

		if (config.isDoCreateIndividualsAssociations()) {
			logger.info("isDoCreateIndividualsAssociations");
			TaxonDescription taxonDescription = null;
			if (config.isDoMatchToExistingDescription()) {
				logger.warn("The import option 'DoMatchToExistingDescription' is not yet implemented.");
			} else {
				UUID taxonDescriptionUUID = config.getTaxonToDescriptionMap()
						.get(taxon.getUuid()); // rather
				if (taxonDescriptionUUID != null) {
					taxonDescription = (TaxonDescription) getDescriptionService()
							.load(taxonDescriptionUUID);
				}
				if (taxonDescription == null) {
					taxonDescription = TaxonDescription.NewInstance(taxon);
					config.getTaxonToDescriptionMap().put(taxon.getUuid(),
							taxonDescription.getUuid());

					taxonDescriptionUUID = config.getTaxonToDescriptionMap()
							.get(taxon.getUuid());
				}
			}
			IndividualsAssociation individualsAssociation = IndividualsAssociation
					.NewInstance();

			individualsAssociation
					.setAssociatedSpecimenOrObservation(derivedUnitFacade
							.innerDerivedUnit());
			individualsAssociation
					.setFeature(Feature.INDIVIDUALS_ASSOCIATION());
			for (Reference<?> citation : determinationEvent.getReferences()) {
				individualsAssociation.addSource(DescriptionElementSource
						.NewInstance(null, null, citation, null));
			}
			taxonDescription.addElement(individualsAssociation);
			getDescriptionService().saveOrUpdate(taxonDescription);
		}
	}

	private HashMap<Taxon, NonViralName<?>> getParentTaxon(Taxon taxon,
			NonViralName<?> taxonName, NonViralName<?> originalName,
			Abcd206ImportConfigurator config, boolean gethighernode) {

		Taxon parenttaxon = null;
		NonViralName<?> parentName = null;
		List<TaxonBase> c = null;

		List<String> highername = new ArrayList<String>();
		Rank higherrank = null;
		Rank taxonrank = taxonName.getRank();

		HashMap<Taxon, NonViralName<?>> map = new HashMap<Taxon, NonViralName<?>>();

		if (taxonrank == Rank.GENUS()) {
			//to change and add test DoReusetaxa
			for (TaxonNode p : classification.getAllNodes()) {
				logger.info("p UUID "+p.getUuid().toString());
				if (classification.getTopmostNode(p.getTaxon()) == null) {
					if (taxon.getTitleCache().contains(
							p.getTaxon().getTitleCache().split(" ")[0])) {
						classification.addParentChild(p.getTaxon(), taxon, ref,
								null);
						break;
					}
				}
			}
			//debug barbare - to be corrected
			TaxonNode p = classification.addChildTaxon(taxon, ref, "", null);
			return map;
		}

		if (taxonrank == Rank.INFRAGENUS()) {
			highername.add(originalName.getGenusOrUninomial());
			higherrank = Rank.GENUS();
		}

		if (taxonrank == Rank.SPECIES()) {
			if (originalName.getGenusOrUninomial() != null) {
				highername.add(originalName.getGenusOrUninomial());
				higherrank = Rank.GENUS();
			}
			if (originalName.getInfraGenericEpithet() != null) {
				highername.add(originalName.getInfraGenericEpithet());
				higherrank = Rank.INFRAGENUS();
			}

		}
		if (taxonrank == Rank.SUBSPECIES()) {
			if (originalName.getGenusOrUninomial() != null)
				highername.add(originalName.getGenusOrUninomial());
			if (originalName.getInfraGenericEpithet() != null) {
				highername.add(originalName.getInfraGenericEpithet());
			}
			if (originalName.getSpecificEpithet() != null)
				highername.add(originalName.getSpecificEpithet());
			higherrank = Rank.SPECIES();

		}

		String highernamestr = StringUtils.join(highername.iterator(), " ");
		if (config.isDoReUseTaxon() && highername != null
				&& highernamestr != "") {
			try {
				c = getTaxonService().searchTaxaByName(highernamestr, ref);
				for (TaxonBase b : c) {
					parenttaxon = (Taxon) b;
					Iterator it = parenttaxon.getTaxonNodes().iterator();
					logger.warn("ICI2");
					while (it.hasNext()) {
						TaxonNode tmpNode = (TaxonNode) it.next();
						logger.info("tmpNode UUID "+tmpNode.getUuid().toString());
						Taxon tmp = tmpNode.getTaxon();
						if (tmp.getTitleCache().equalsIgnoreCase(highernamestr)){
							parenttaxon = tmp;
						}
					}

				}
			} catch (Exception e) {
				logger.info("Problem while trying to reuse existing taxon" + e);
				parenttaxon = null;
			}
		}
		if ((parenttaxon == null && highername != null && highernamestr != "")
				|| !config.isDoReUseTaxon()) {

			parentName = NonViralName.NewInstance(null);
			parentName.setFullTitleCache(highernamestr);
			parentName.setNameCache(highernamestr);
			parentName.setRank(higherrank);
			parenttaxon = Taxon.NewInstance(parentName, ref);
		}

		map.put(parenttaxon, parentName);
		return map;
		// return parenttaxon;
	}

	private Taxon addParentTaxon(Taxon child, NonViralName<?> childName,
			Abcd206ImportConfigurator config, boolean gethighernode) {

		Taxon parenttaxon;
		NonViralName<?> originalName = childName;

		HashMap<Taxon, NonViralName<?>> tmpmaptn = getParentTaxon(child,
				childName, originalName, config, gethighernode);

		HashMap<Taxon, Taxon> map = new HashMap<Taxon, Taxon>();
		HashMap<Taxon, NonViralName<?>> maptn = new HashMap<Taxon, NonViralName<?>>();

		Taxon tmpparent = tmpmaptn.keySet().iterator().next();
		NonViralName<?> parentname = tmpmaptn.get(tmpparent);

		map.put(tmpparent, child);
		maptn.put(child, childName);
		maptn.put(tmpparent, parentname);

		while (!tmpparent.getTitleCache().trim()
				.equalsIgnoreCase(child.getTitleCache().trim())
				&& childName != null && tmpmaptn.keySet().size() > 0) {
			child = tmpparent;
			childName = maptn.get(child);
			if (childName == null) {
				logger.warn("OUPS " + tmpparent.getTitleCache());
			} else {
				tmpmaptn = getParentTaxon(child, childName, originalName,
						config, gethighernode);
				try {
					if (tmpmaptn.keySet().size() > 0) {

						tmpparent = tmpmaptn.keySet().iterator().next();
						parentname = tmpmaptn.get(tmpparent);
						map.put(tmpparent, child);
						maptn.put(tmpparent, parentname);

						for (Taxon elt : map.keySet()) {
							NonViralName<?> t = maptn.get(elt);
							if (parentname.getRank() == Rank.GENUS())

								t.setGenusOrUninomial(parentname
										.getTitleCache());
							if (parentname.getRank() == Rank.SPECIES())
								t.setSpecificEpithet(parentname.getTitleCache());
							maptn.remove(elt);
							maptn.put(elt, t);
						}

					}

				} catch (Exception e) {
					logger.warn("ohooooooooooooooooooooooooooooooooooooooo "
							+ e);
				}
			}

		}

		// last child == higher rank found
		tmpparent = child;

		for (TaxonNode p : classification.getAllNodes()) {
			logger.warn("ICI3");
			logger.warn("ptmpparent UUID "+p.getUuid().toString());
			if (classification.getTopmostNode(p.getTaxon()) == null) {
				if (tmpparent.getTitleCache().contains(
						p.getTaxon().getTitleCache().split(" ")[0])) {
					
					tmpparent = p.getTaxon();
					classification.addParentChild(tmpparent, null, ref, "");
					map.put(tmpparent, child);
					break;
				}
			}
		}
		ArrayList<Taxon> taxondone = new ArrayList<Taxon>();
		taxondone.add(tmpparent);

		while (taxondone.size() < map.size() + 1) {
			child = map.get(tmpparent);
			try {
				if (tmpparent != child) {
					classification.addParentChild(tmpparent, child, ref, null);
					tmpparent = child;
				}
				taxondone.add(child);
			} catch (Exception e) {
				logger.warn("PB :" + e);
			}
		}
		return child;

	}

	private Taxon getTaxon(Abcd206ImportConfigurator config,
			String scientificName, NonViralName taxonName) {
		Taxon taxon = null;
		List<TaxonBase> c = null;

		if (config.isDoMatchTaxa()) {
			taxon = getTaxonService().findBestMatchingTaxon(scientificName);
		}

		if (taxon == null && config.isDoReUseTaxon()) {
			try {
				c = getTaxonService().searchTaxaByName(scientificName, ref);
				for (TaxonBase b : c) {
					taxon = (Taxon) b;
				}
			} catch (Exception e) {
				logger.info("does not work here either " + e);
				taxon = null;
			}
		} else {
			logger.info("Matching to existing Taxon : " + taxon.getTitleCache());
		}

		if (!config.isDoReUseTaxon() && taxon == null) {
			getNameService().save(taxonName);
			taxon = Taxon.NewInstance(taxonName, ref);
		}
		if (taxon == null) {
			taxon = Taxon.NewInstance(taxonName, ref);
		}

		return taxon;
	}

	/*
	 * 
	 * @param app
	 * 
	 * @param derivedThing
	 * 
	 * @param sec
	 */
	private void handleIdentifications(Abcd206ImportConfigurator config) {

		String fullScientificNameString;
		Taxon taxon = null;
		Taxon parenttaxon = null;
		NonViralName<?> taxonName = null;
		NonViralName<?> parentName = null;
		List<TaxonBase> taxonList = null;

		Rank rankgenus = Rank.GENUS();
		Rank rankfamily = Rank.FAMILY();

		HashMap<String, Taxon> taxaDone = new HashMap<String, Taxon>();
		HashMap<Integer, Taxon> preferredtaxontoinsert = new HashMap<Integer, Taxon>();
		HashMap<Integer, NonViralName<?>> preferredtaxonnametoinsert = new HashMap<Integer, NonViralName<?>>();
		HashMap<Integer, Taxon> taxontoinsert = new HashMap<Integer, Taxon>();
		HashMap<Integer, NonViralName<?>> taxonnametoinsert = new HashMap<Integer, NonViralName<?>>();

		String scientificName = "";
		boolean preferredFlag = false;
		boolean onepreferred = false;

		if (dataHolder.nomenclatureCode == "")
			dataHolder.nomenclatureCode = config.getNomenclaturalCode()
					.toString();

		for (int i = 0; i < dataHolder.identificationList.size(); i++) {

			fullScientificNameString = dataHolder.identificationList.get(i);
			fullScientificNameString = fullScientificNameString.replaceAll(
					" et ", " & ");

			if (fullScientificNameString.indexOf("_preferred_") != -1) {
				scientificName = fullScientificNameString.split("_preferred_")[0];
				String pTmp = fullScientificNameString.split("_preferred_")[1]
						.split("_code_")[0];
				if (pTmp.equals("1")
						|| pTmp.toLowerCase().indexOf("true") != -1) {
					preferredFlag = true;
					onepreferred = true;
				} else {
					preferredFlag = false;
				}
			} else {
				scientificName = fullScientificNameString;
			}

			// logger.info("fullscientificname " + fullScientificNameString
			// + ", *" + dataHolder.nomenclatureCode + "*");

			if (fullScientificNameString.indexOf("_code_") != -1) {
				if (fullScientificNameString.indexOf(':') != -1) {
					dataHolder.nomenclatureCode = fullScientificNameString
							.split("_code_")[1].split(":")[1];
				} else
					dataHolder.nomenclatureCode = fullScientificNameString
							.split("_code_")[1];
			}

			if (config.isDoAutomaticParsing()
					|| dataHolder.atomisedIdentificationList == null
					|| dataHolder.atomisedIdentificationList.size() == 0) {
				taxonName = this.parseScientificName(scientificName);
				if (taxonName == null)
					taxonName = this.setTaxonNameByType(
							dataHolder.atomisedIdentificationList.get(i),
							scientificName);
			} else {
				if (dataHolder.atomisedIdentificationList != null
						|| dataHolder.atomisedIdentificationList.size() > 0) {
					taxonName = this.setTaxonNameByType(
							dataHolder.atomisedIdentificationList.get(i),
							scientificName);
				}
			}

			// logger.info("taxonName: " + taxonName);
			taxon = getTaxon(config, scientificName, taxonName);

			if (preferredFlag) {
				preferredtaxonnametoinsert.put(i, taxonName);
				preferredtaxontoinsert.put(i, taxon);
			} else {
				taxonnametoinsert.put(i, taxonName);
				taxontoinsert.put(i, taxon);
			}
		}

		Iterator entries = preferredtaxontoinsert.keySet().iterator();
		while (entries.hasNext()) {

			int a = (Integer) entries.next();
			logger.info("\n PREFERRED num." + a + " \n");
			taxonName = preferredtaxonnametoinsert.get(a);
			taxon = preferredtaxontoinsert.get(a);

			// logger.info("taxonrank " + taxonName + ", " + taxonrank);

			taxon = addParentTaxon(taxon, taxonName, config, true);
			linkDeterminationEvent(taxon, true, config);
		}

		if (onepreferred) {
			entries = taxontoinsert.keySet().iterator();

			while (entries.hasNext()) {
				int a = (Integer) entries.next();
				logger.info("\n NORMAL num." + a + " \n");
				taxonName = taxonnametoinsert.get(a);

				taxon = taxontoinsert.get(a);
				linkDeterminationEvent(taxon, false, config);
			}

		}

		else {// no preferred name, have to add everything to the classification
			logger.info("no preferred taxa");
			entries = taxontoinsert.keySet().iterator();

			while (entries.hasNext()) {
				int a = (Integer) entries.next();
				logger.info("\n NORMAL num." + a + " \n");
				taxonName = taxonnametoinsert.get(a);
				taxon = taxontoinsert.get(a);

				taxon = addParentTaxon(taxon, taxonName, config, true);
				if (taxontoinsert.keySet().size() == 1)
					linkDeterminationEvent(taxon, true, config);
				else
					linkDeterminationEvent(taxon, false, config);

			}

		}

	}

	private NonViralName<?> parseScientificName(String scientificName) {
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		NonViralName<?> taxonName = null;
		boolean problem = false;

		logger.info("parseScientificName "
				+ dataHolder.nomenclatureCode.toString());

		if (dataHolder.nomenclatureCode.toString().equals("Zoological")
				|| dataHolder.nomenclatureCode.toString().contains("ICZN")) {
			taxonName = (ZoologicalName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICZN, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Botanical")
				|| dataHolder.nomenclatureCode.toString().contains("ICBN")) {
			taxonName = (BotanicalName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICBN, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Bacterial")
				|| dataHolder.nomenclatureCode.toString().contains("ICBN")) {
			taxonName = (BacterialName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICNB, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (dataHolder.nomenclatureCode.toString().equals("Cultivar")
				|| dataHolder.nomenclatureCode.toString().contains("ICNCP")) {
			taxonName = (CultivarPlantName) nvnpi.parseFullName(scientificName,
					NomenclaturalCode.ICNCP, null);
			if (taxonName.hasProblem()) {
				problem = true;
			}
		}
		if (problem) {
			logger.info("Parsing with problem in parseScientificName");
			return null;
		}
		return taxonName;

	}

	private NonViralName<?> setTaxonNameByType(
			HashMap<String, String> atomisedMap, String fullName) {
		boolean problem = false;
		logger.info("settaxonnamebytype "
				+ dataHolder.nomenclatureCode.toString());

		if (dataHolder.nomenclatureCode.equals("Zoological")) {
			NonViralName<ZoologicalName> taxonName = ZoologicalName
					.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
			taxonName
					.setInfraGenericEpithet(getFromMap(atomisedMap, "SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap,
					"SpeciesEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,
					"SubspeciesEpithet"));

			if (taxonName.getGenusOrUninomial() != null)
				taxonName.setRank(Rank.GENUS());

			if (taxonName.getInfraGenericEpithet() != null)
				taxonName.setRank(Rank.SUBGENUS());

			if (taxonName.getSpecificEpithet() != null)
				taxonName.setRank(Rank.SPECIES());

			if (taxonName.getInfraSpecificEpithet() != null)
				taxonName.setRank(Rank.SUBSPECIES());

			Team team = null;
			if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
			} else {
				if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
					team = Team.NewInstance();
					team.setTitleCache(
							getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
				}
			}
			if (team != null) {
				taxonName.setBasionymAuthorTeam(team);
			} else {
				if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeamParenthesis"));
				} else if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeamAndYear"));
				}
			}
			if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"),
						true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem()) {
				logger.info("pb ICZN");
				problem = true;
			} else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Botanical")) {
			NonViralName<BotanicalName> taxonName = BotanicalName
					.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
			taxonName.setInfraGenericEpithet(getFromMap(atomisedMap,
					"FirstEpithet"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,
					"InfraSpeEpithet"));
			try {
				taxonName.setRank(Rank.getRankByName(getFromMap(atomisedMap,
						"Rank")));
			} catch (Exception e) {
				if (taxonName.getGenusOrUninomial() != null)
					taxonName.setRank(Rank.GENUS());

				if (taxonName.getInfraGenericEpithet() != null)
					taxonName.setRank(Rank.SUBGENUS());

				if (taxonName.getSpecificEpithet() != null)
					taxonName.setRank(Rank.SPECIES());

				if (taxonName.getInfraSpecificEpithet() != null)
					taxonName.setRank(Rank.SUBSPECIES());
			}
			Team team = null;
			if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
				if (team != null) {
					taxonName.setBasionymAuthorTeam(team);
				}
			}
			if (getFromMap(atomisedMap, "AuthorTeam") != null) {
				team = Team.NewInstance();
				team.setTitleCache(getFromMap(atomisedMap, "AuthorTeam"), true);
				if (team != null) {
					taxonName.setCombinationAuthorTeam(team);
				}
			}
			if (team == null) {
				if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeamParenthesis"));
				} else if (getFromMap(atomisedMap, "AuthorTeam") != null) {
					taxonName.setAuthorshipCache(getFromMap(atomisedMap,
							"AuthorTeam"));
				}
			}
			if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
				team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"),
						true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (taxonName.hasProblem()) {
				logger.info("pb ICBN");
				problem = true;
			} else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Bacterial")) {
			NonViralName<BacterialName> taxonName = BacterialName
					.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
			taxonName
					.setInfraGenericEpithet(getFromMap(atomisedMap, "SubGenus"));
			taxonName.setSpecificEpithet(getFromMap(atomisedMap, "Species"));
			taxonName.setInfraSpecificEpithet(getFromMap(atomisedMap,
					"SubspeciesEpithet"));

			if (taxonName.getGenusOrUninomial() != null)
				taxonName.setRank(Rank.GENUS());

			if (taxonName.getInfraGenericEpithet() != null)
				taxonName.setRank(Rank.SUBGENUS());

			if (taxonName.getSpecificEpithet() != null)
				taxonName.setRank(Rank.SPECIES());

			if (taxonName.getInfraSpecificEpithet() != null)
				taxonName.setRank(Rank.SUBSPECIES());

			if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
				Team team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
				taxonName.setCombinationAuthorTeam(team);
			}
			if (getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear") != null) {
				Team team = Team.NewInstance();
				team.setTitleCache(
						getFromMap(atomisedMap,
								"ParentheticalAuthorTeamAndYear"), true);
				taxonName.setBasionymAuthorTeam(team);
			}
			if (taxonName.hasProblem()) {
				logger.info("pb ICNB");
				problem = true;
			} else {
				return taxonName;
			}
		}
		if (dataHolder.nomenclatureCode.equals("Cultivar")) {
			CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

			if (taxonName.hasProblem()) {
				logger.info("pb ICNCP");
				problem = true;
			} else {
				return taxonName;
			}
			return taxonName;
		}

		if (problem) {
			logger.info("Problem im setTaxonNameByType ");
			NonViralName<?> taxonName = NonViralName.NewInstance(null);
			taxonName.setFullTitleCache(fullName, true);
			return taxonName;
		}
		NonViralName<?> tn = NonViralName.NewInstance(null);
		return tn;
	}

	private String getFromMap(HashMap<String, String> atomisedMap, String key) {
		String value = null;
		if (atomisedMap.containsKey(key)) {
			value = atomisedMap.get(key);
		}

		try {
			if (value != null && key.matches(".*Year.*")) {
				value = value.trim();
				if (value.matches("[a-z A-Z ]*[0-9]{4}$")) {
					String tmp = value.split("[0-9]{4}$")[0];
					int year = Integer.parseInt(value.split(tmp)[1]);
					if (year >= 1752) {
						value = tmp;
					} else {
						value = null;
					}
				} else {
					value = null;
				}
			}
		} catch (Exception e) {
			value = null;
		}

		return value;
	}

	private void compareABCDtoCDM(URI urlFileName, ArrayList<String> knownElts) {

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder constructeur = factory.newDocumentBuilder();
			URL url = urlFileName.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream) o;
			Document document = constructeur.parse(is);
			Element root = document.getDocumentElement();
			traverse(root);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> elts = dataHolder.allABCDelements.keySet();
		Iterator<String> it = elts.iterator();
		String elt;
		while (it.hasNext()) {
			elt = it.next();
			if (knownElts.indexOf(elt) == -1) {
				logger.info("Unsaved ABCD element: " + elt + " - "
						+ dataHolder.allABCDelements.get(elt));
			}
		}
	}

	/**
	 * Traverses the tree for compareABCDtoCDM
	 * 
	 * @param node
	 * @param dataHolder
	 */
	private void traverse(Node node) {
		// Extract node info:
		String test = node.getTextContent();

		// Print and continue traversing.
		if (test != null && test != "#text" && node.getNodeName() != "#text"
				&& test.split("\n").length == 1 && test.length() > 0) {
			path = node.getNodeName();
			getHierarchie(node);
			dataHolder.allABCDelements.put(path, test);
			path = "";
		}
		// Now traverse the rest of the tree in depth-first order.
		if (node.hasChildNodes()) {
			// Get the children in a list.
			NodeList nl = node.getChildNodes();
			// How many of them?
			int size = nl.getLength();
			for (int i = 0; i < size; i++) {
				// Recursively traverse each of the children.
				traverse(nl.item(i));
			}
		}
	}

	@Override
	protected boolean isIgnore(Abcd206ImportState state) {
		// return ! config.isDoNameFacts();
		return false;
	}

}
