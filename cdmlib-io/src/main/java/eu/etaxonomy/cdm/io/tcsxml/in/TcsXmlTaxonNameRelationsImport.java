/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.in;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsxml.TcsXmlTransformer;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

@Component
public class TcsXmlTaxonNameRelationsImport extends TcsXmlImportBase implements ICdmIO<TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlTaxonNameRelationsImport.class);

	private static int modCount = 5000;

	public TcsXmlTaxonNameRelationsImport(){
		super();
	}

	@Override
	public boolean doCheck(TcsXmlImportState state){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);

		return result;
	}

	@Override
	public void doInvoke(TcsXmlImportState state){


		logger.info("start make taxon name relations ...");
		MapWrapper<TaxonName> taxonNameMap = (MapWrapper<TaxonName>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<Reference> referenceMap = (MapWrapper<Reference>)state.getStore(ICdmIO.REFERENCE_STORE);

		Set<TaxonName> nameStore = new HashSet<TaxonName>();

		ResultWrapper<Boolean> success = ResultWrapper.NewInstance(true);
		String childName;
		boolean obligatory;
		String idNamespace = "TaxonName";

		TcsXmlImportConfigurator config = state.getConfig();
		Element elDataSet = super. getDataSetElement(config);
		Namespace tcsNamespace = config.getTcsXmlNamespace();

		DoubleResult<Element, Boolean> doubleResult;
		childName = "TaxonNames";
		obligatory = false;
		Element elTaxonNames = XmlHelp.getSingleChildElement(success, elDataSet, childName, tcsNamespace, obligatory);

		String tcsElementName = "TaxonName";
		List<Element> elTaxonNameList =  elTaxonNames == null ? new ArrayList<Element>() : elTaxonNames.getChildren(tcsElementName, tcsNamespace);

//		Element source = tcsConfig.getSourceRoot();

		int i = 0;
		int nameRelCount = 0;
		//for each taxonName
		for (Element elTaxonName : elTaxonNameList){

			if ((++i % modCount) == 0){ logger.info("Names handled: " + (i-1));}

			//Basionyms
			tcsElementName = "Basionym";
			List<Element> elBasionymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);

			for (Element elBasionym: elBasionymList){
				nameRelCount++;
				logger.debug("BASIONYM "+  nameRelCount);

				NameRelationshipType relType = NameRelationshipType.BASIONYM();
				boolean inverse = false;

				String id = elTaxonName.getAttributeValue("id");
//				TaxonName fromName = taxonNameMap.get(id);

				makeNomenclaturalNoteType(config, elBasionym, relType, taxonNameMap, nameStore, removeVersionOfRef(id), inverse);
			}// end Basionyms

			//SpellingCorrections
			tcsElementName = "SpellingCorrectionOf";
			List<Element> elSpellingCorrectionList = elTaxonName.getChildren(tcsElementName, tcsNamespace);

			for (Element elSpellingCorrection: elSpellingCorrectionList){
				nameRelCount++;
				logger.debug("SpellingCorrectionOf "+  nameRelCount);

				NameRelationshipType relType = NameRelationshipType.ORTHOGRAPHIC_VARIANT();
				boolean inverse = true;

				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(config, elSpellingCorrection, relType, taxonNameMap, nameStore, id, inverse);
			}// end SpellingCorrections

			//LaterHomonymOf
			tcsElementName = "LaterHomonymOf";
			List<Element> elLaterHomonymList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elLaterHomonym: elLaterHomonymList){
				nameRelCount++;
				logger.debug("LaterHomonymOf "+  nameRelCount);

				NameRelationshipType relType = NameRelationshipType.LATER_HOMONYM();
				boolean inverse = false;

				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(config, elLaterHomonym, relType, taxonNameMap, nameStore, id, inverse);
			}// end LaterHomonymOf

			//ReplacementNameFor
			tcsElementName = "ReplacementNameFor";
			List<Element> elReplacementNameForList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elReplacementNameFor: elReplacementNameForList){
				nameRelCount++;
				logger.debug("LaterHomonymOf "+  nameRelCount);

				NameRelationshipType relType = NameRelationshipType.REPLACED_SYNONYM();
				boolean inverse = false;

				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(config, elReplacementNameFor, relType, taxonNameMap, nameStore, id, inverse);
			}// end ReplacementNameFor

			//ConservedAgainst
			tcsElementName = "ConservedAgainst";
			List<Element> elConservedAgainstList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elConservedAgainst: elConservedAgainstList){
				nameRelCount++;
				logger.debug("ConservedAgainst "+  nameRelCount);

				NameRelationshipType relType = NameRelationshipType.CONSERVED_AGAINST();
				boolean inverse = false;

				String id = elTaxonName.getAttributeValue("id");
				makeNomenclaturalNoteType(config, elConservedAgainst, relType, taxonNameMap, nameStore, id, inverse);
			}// end ConservedAgainst



			//Sanctioned
			tcsElementName = "Sanctioned";
			List<Element> elSanctionedList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elSanctioned: elSanctionedList){

				//nameRelCount++;
				//TODO sanctioned
				logger.warn("Sanctioned not yet implemented " );

				///NameRelationshipType relType = NameRelationshipType.XXX
				//boolean inverse = false;
				//
				//String id = elTaxonName.getAttributeValue("id");
				//makeNomenclaturalNoteType(tcsConfig, elSanctioned, relType, taxonNameMap, nameStore, id, inverse);
			}// end Sanctioned

			//PublicationStatus
			tcsElementName = "PublicationStatus";
			List<Element> elPublicationStatusList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elPublicationStatus: elPublicationStatusList){

				nameRelCount++;
				//TODO PublicationStatus
				//logger.warn("PublicationStatus not yet implemented " );
				NomenclaturalStatusType statusType = null;
				NameRelationshipType nameRelType = null;
				List<Content> content = elPublicationStatus.getContent();
				Element el = elPublicationStatus.getChild("Note");
				Iterator<Content> iterator = content.iterator();
				Content next;
				String ruleConsidered = null;
				String relatedName = null;
				while (iterator.hasNext()){
					next = iterator.next();
					String test = next.getClass().getName();
					if (next.getClass().getName().equals("org.jdom.Element")){
						Element element = (Element)next;
						NomenclaturalStatus status;
						try {
							if (element.getName().equals("Note")){
								Iterator<Content> iteratorNote = element.getContent().iterator();
								Content nextNote;
								while (iteratorNote.hasNext()){
									nextNote = iteratorNote.next();
									test = nextNote.getClass().getName();
									if (nextNote.getValue().startsWith("nom. inval.")){
										statusType =TcsXmlTransformer.nomStatusString2NomStatus("nom. inval.");
									} else if (nextNote.getValue().startsWith("nom. illeg.")){
										statusType =TcsXmlTransformer.nomStatusString2NomStatus("nom. illeg.");
									} else if (nextNote.getValue().startsWith("nom. superfl.")){
										statusType =TcsXmlTransformer.nomStatusString2NomStatus("nom. superfl.");
									} else if (nextNote.getValue().startsWith("[isonym]")){
										//in cdm NameRelationship
										nameRelType = NameRelationshipType.LATER_ISONYM();
									}

								}
							}else if (element.getName().equals("RuleConsidered")){
								Iterator<Content> iteratorNote = element.getContent().iterator();
								Content nextNote;
								while (iteratorNote.hasNext()){
									nextNote = iteratorNote.next();
									ruleConsidered = nextNote.getValue();
								}
							}else if (element.getName().equals("RelatedName")){
								Iterator<Content> iteratorNote = element.getContent().iterator();
								Content nextNote;
								while (iteratorNote.hasNext()){
									nextNote = iteratorNote.next();
									relatedName = nextNote.getValue();
								}
							}
						} catch (UnknownCdmTypeException e) {
							// TODO Auto-generated catch block
							logger.warn(e.getMessage());
						}
					}

				}
				TaxonName taxonName = null;
				if (statusType != null || nameRelType != null){
					String id = elTaxonName.getAttributeValue("id");

					taxonName=  taxonNameMap.get(removeVersionOfRef(id));
				}
				if (taxonName != null){
					if (statusType != null){
						NomenclaturalStatus status = NomenclaturalStatus.NewInstance(statusType);
						if (ruleConsidered != null){
							status.setRuleConsidered(ruleConsidered);
						}
						taxonName.addStatus(status);
					}
					if (nameRelType != null){
						String id = elTaxonName.getAttributeValue("id");

						TaxonName relatedTaxonName =  taxonNameMap.get(removeVersionOfRef(id));
						taxonName.addRelationshipFromName(relatedTaxonName, nameRelType, ruleConsidered);
					}
				}




			}// end PublicationStatus

			//BasedOn
			tcsElementName = "BasedOn";
			List<Element> elBasedOnList = elTaxonName.getChildren(tcsElementName, tcsNamespace);
			for (Element elBasedOn: elBasedOnList){

				//nameRelCount++;
				//TODO BasedOn
				logger.debug("BasedOn not yet implemented " );
				/*
				 * <tcs:BasedOn>
						<tcs:RelatedName ref="urn:lsid:ipni.org:names:151372-1"></tcs:RelatedName>
					</tcs:BasedOn>
				 */
				///NameRelationshipType relType = NameRelationshipType.XXX
				//boolean inverse = false;
				//
				//String id = elTaxonName.getAttributeValue("id");
				//makeNomenclaturalNoteType(tcsConfig, elBasedOn, relType, taxonNameMap, nameStore, id, inverse);
			}// end BasedOn




		}
		//Other Relations
		//TODO

		logger.info(nameRelCount + " nameRelations handled");
		getNameService().save(nameStore);
		logger.info("end make taxon name relationships ...");
		if (!success.getValue()){
			state.setUnsuccessfull();
		}
		return;
	}


	private  boolean makeNomenclaturalNoteType(TcsXmlImportConfigurator tcsConfig, Element elRelation, NameRelationshipType relType, MapWrapper<TaxonName> taxonNameMap, Set<TaxonName> nameStore, String id, boolean inverse){
		if (elRelation == null){
			return false;
		}
		Namespace ns = elRelation.getNamespace();

		String ruleConsidered = elRelation.getChildText("RuleConsidered", ns);
		String note = elRelation.getChildText("Note", ns);
		String microReference = elRelation.getChildText("MicroReference", ns);
		Element elRelatedName = elRelation.getChild("RelatedName", ns);
		//TODO relType
		String relatedNameId = elRelatedName.getAttributeValue("ref");

		TaxonName fromName = taxonNameMap.get(removeVersionOfRef(id));

		TaxonName toName = taxonNameMap.get(removeVersionOfRef(relatedNameId));
		if (fromName == null){
			//logger.warn("fromName (" + id + ") not found in Map! Relationship not set!");
			return false;
		}
		if (toName == null){
			//logger.warn("toName (" + relatedNameId + ") not found in Map! Relationship not set!");
			return false;
		}


		//TODO note, microreference
		if (inverse == false){
			toName.addRelationshipToName(fromName, relType, ruleConsidered);
		}else{
			fromName.addRelationshipToName(toName, relType, ruleConsidered);
		}
		nameStore.add(fromName);
		return true;
	}

	@Override
	protected boolean isIgnore(TcsXmlImportState state){
		return ! state.getConfig().isDoRelNames();
	}

}
