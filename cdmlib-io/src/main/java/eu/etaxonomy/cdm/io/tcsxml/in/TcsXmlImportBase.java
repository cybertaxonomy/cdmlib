/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsxml.in;

import static eu.etaxonomy.cdm.io.common.ImportHelper.OBLIGATORY;
import static eu.etaxonomy.cdm.io.common.ImportHelper.OVERWRITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.ext.ipni.IpniService;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsxml.CdmSingleAttributeXmlMapperBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 * @since 04.08.2008
 */
public abstract class TcsXmlImportBase  extends CdmImportBase<TcsXmlImportConfigurator, TcsXmlImportState> {
    private static final long serialVersionUID = -2169244092211698392L;

    private static final Logger logger = Logger.getLogger(TcsXmlImportBase.class);

	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");
	protected static Namespace nsTn = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonName#");
	protected static Namespace nsTgeo = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/GeographicRegion#");
	protected static Namespace nsTc = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonConcept#");
	protected static Namespace nsTpub = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/PublicationCitation#");
	protected static Namespace nsTpalm = Namespace.getNamespace("http://wp5.e-taxonomy.eu/import/palmae/common");

	@Autowired
	IpniService service;

	@Override
    protected abstract void doInvoke(TcsXmlImportState state);

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
//	 */
//	@Override
//	protected boolean doInvoke(IImportConfigurator config,
//			Map<String, MapWrapper<? extends CdmBase>> stores){
//		TcsXmlImportState state = ((TcsXmlImportConfigurator)config).getState();
//		state.setConfig((TcsXmlImportConfigurator)config);
//		return doInvoke(state);
//	}


	protected boolean makeStandardMapper(Element parentElement, CdmBase ref, Set<String> omitAttributes, CdmSingleAttributeXmlMapperBase[] classMappers){
		if (omitAttributes == null){
			omitAttributes = new HashSet<String>();
		}
		boolean result = true;
		for (CdmSingleAttributeXmlMapperBase mapper : classMappers){
			Object value = getValue(mapper, parentElement);
			//write to destination
			if (value != null){
				String destinationAttribute = mapper.getDestinationAttribute();
				if (! omitAttributes.contains(destinationAttribute)){
					result &= ImportHelper.addValue(value, ref, destinationAttribute, mapper.getTypeClass(), OVERWRITE, OBLIGATORY);
				}
			}
		}
		return true;
	}

	private Object getValue(CdmSingleAttributeXmlMapperBase mapper, Element parentElement){
		String sourceAttribute = mapper.getSourceAttribute();
		Namespace sourceNamespace = mapper.getSourceNamespace(parentElement);
		Element child = parentElement.getChild(sourceAttribute, sourceNamespace);
		if (child == null){
			return null;
		}
		if (child.getContentSize() > 1){
			logger.warn("Element is not String");
		}
		Object value = child.getTextTrim();
		return value;
	}

	protected boolean checkAdditionalContents(Element parentElement, CdmSingleAttributeXmlMapperBase[] classMappers, CdmSingleAttributeXmlMapperBase[] operationalMappers, CdmSingleAttributeXmlMapperBase[] unclearMappers){
		List<Content> additionalContentList = new ArrayList<Content>();
		List<Content> contentList = parentElement.getContent();
		List<CdmSingleAttributeXmlMapperBase> mapperList = new ArrayList<CdmSingleAttributeXmlMapperBase>();

		mapperList.addAll(Arrays.asList(classMappers));
		mapperList.addAll(Arrays.asList(operationalMappers));
		mapperList.addAll(Arrays.asList(unclearMappers));

		for(Content content: contentList){
			boolean contentExists = false;
			if (content instanceof Element){
				for (CdmSingleAttributeXmlMapperBase mapper : mapperList){
					if (mapper.mapsSource(content, parentElement)){
						contentExists = true;
						break;
					}
				}

			}else if (content instanceof Text){
				//empty Text
				if (((Text)content).getTextNormalize().equals("")){
					contentExists = true;
				}else{
					//
				}
			}

			if (contentExists == false){
				additionalContentList.add(content);
			}
		}
		for (Content additionalContent : additionalContentList){
			logger.warn("Additional content: " +  additionalContent);
		}
		return (additionalContentList.size() == 0);
	}

	protected Element getDataSetElement(TcsXmlImportConfigurator tcsConfig){
		Element root = tcsConfig.getSourceRoot();

		if (! "DataSet".equals(root.getName())){
			logger.error("Root element is not 'DataSet'");
			return null;
		}
		if (tcsConfig.getTcsXmlNamespace() == null){
			logger.error("No namespace defined for tcs");
			return null;
		}
		if (! tcsConfig.getTcsXmlNamespace().equals(root.getNamespace())){
			logger.error("Wrong namespace for element 'DataSet'");
			return null;
		}
		//TODO prevent multiple elements

		return root;
	}

//	static public boolean checkFirstTwoFunctionElements(List<Object> objList){
//		if (! (objList.get(0) instanceof TcsXmlImportConfigurator)){
//			logger.error("first method object has wrong type. Must be " + TcsXmlImportConfigurator.class.getSimpleName() + " but is " + (objList.get(0) == null ? "null": objList.get(0).getClass().getSimpleName()));
//			return false;
//		}
//		if (! (objList.get(1) == null) && ! (objList.get(1) instanceof Element)){
//			logger.error("first method object has wrong type. Must be " + Element.class.getSimpleName() + " but is " + (objList.get(1) == null ? "null": objList.get(1).getClass().getSimpleName()));
//			return false;
//		}
//		return true;
//	}


	protected boolean testAdditionalElements(Element parentElement, List<String> excludeList){
		boolean result = true;
		List<Element> list = parentElement.getChildren();
		for (Element element : list){
			if (! excludeList.contains(element.getName())){
				logger.warn("Unknown element (" + element.getName() + ") in parent element (" + parentElement.getName() + ")");
				result = false;
			}
		}
		return result;
	}


	protected <T extends IdentifiableEntity> T makeReferenceType(Element element, Class<? extends T> clazz, MapWrapper<? extends T> objectMap, ResultWrapper<Boolean> success){
		T result = null;
		String linkType = element.getAttributeValue("linkType");
		String ref = element.getAttributeValue("ref");
		if (ref != null){
			if (ref.matches("urn:lsid:ipni.org:.*:*")){
				ref = ref.substring(0,ref.lastIndexOf(":"));
			}
		}
		if(ref == null && linkType == null){
			result = getInstance(clazz);
			if (result != null){
				String title = element.getTextNormalize();
				result.setTitleCache(title, true);
			}
		}else if (linkType == null || linkType.equals("local")){
			//TODO
			result = objectMap.get(ref);
			if (result == null){
				result = getInstance(clazz);
				if (result != null){
					String title = element.getTextNormalize();
					result.setTitleCache(title, true);
				}
			}
		}else if(linkType.equals("external")){
			logger.warn("External link types not yet implemented");
		}else if(linkType.equals("other")){
			logger.warn("Other link types not yet implemented");
		}else{
			logger.warn("Unknown link type or missing ref");
		}
		if (result == null){
			success.setValue(false);
		}
		return result;
	}


	protected Reference makeAccordingTo(Element elAccordingTo, MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){
		Reference result = null;
		if (elAccordingTo != null){
			String childName = "AccordingToDetailed";
			boolean obligatory = false;
			Element elAccordingToDetailed = XmlHelp.getSingleChildElement(success, elAccordingTo, childName, elAccordingTo.getNamespace(), obligatory);

			childName = "Simple";
			obligatory = true;
			Element elSimple = XmlHelp.getSingleChildElement(success, elAccordingTo, childName, elAccordingTo.getNamespace(), obligatory);

			if (elAccordingToDetailed != null){
				result = makeAccordingToDetailed(elAccordingToDetailed, referenceMap, success);
			}else{
				result = ReferenceFactory.newGeneric();
				String title = elSimple.getTextNormalize();
				result.setTitleCache(title, true);
			}
		}
		return result;
	}


	private Reference makeAccordingToDetailed(Element elAccordingToDetailed, MapWrapper<Reference> referenceMap, ResultWrapper<Boolean> success){
		Reference result = null;
		Namespace tcsNamespace = elAccordingToDetailed.getNamespace();
		if (elAccordingToDetailed != null){
			//AuthorTeam
			String childName = "AuthorTeam";
			boolean obligatory = false;
			Element elAuthorTeam = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			makeAccordingToAuthorTeam(elAuthorTeam, success);

			//PublishedIn
			childName = "PublishedIn";
			obligatory = false;
			Element elPublishedIn = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			result = makeReferenceType(elPublishedIn, Reference.class, referenceMap, success);

			//MicroReference
			childName = "MicroReference";
			obligatory = false;
			Element elMicroReference = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			if (elMicroReference != null){
				String microReference = elMicroReference.getTextNormalize();
				if (CdmUtils.Nz(microReference).equals("")){
					//TODO
					logger.warn("MicroReference not yet implemented for AccordingToDetailed");
				}
			}
		}
		return result;
	}

	private Team makeAccordingToAuthorTeam(Element elAuthorTeam, ResultWrapper<Boolean> succes){
		Team result = null;
		if (elAuthorTeam != null){
			//TODO
			logger.warn("AuthorTeam not yet implemented for AccordingToDetailed");
		}
		return result;
	}





	protected void testNoMoreElements(){
		//TODO
		//logger.info("testNoMoreElements Not yet implemented");
	}





	@SuppressWarnings("unchecked")
	protected TeamOrPersonBase<?> makeNameCitation(Element elNameCitation, MapWrapper<Person> authorMap, ResultWrapper<Boolean> success){
		TeamOrPersonBase<?> result = null;
		String childName;
		boolean obligatory;
		if (elNameCitation != null){
			Namespace ns = elNameCitation.getNamespace();

			childName = "Authors";
			obligatory = false;
			Element elAuthors = XmlHelp.getSingleChildElement(success, elNameCitation, childName, ns, obligatory);
			testNoMoreElements();

			if (elAuthors != null){
				childName = "AgentName";
				List<Element> elAgentList = elAuthors.getChildren(childName, ns);
				Team team = Team.NewInstance();
				result = team;
				if (elAgentList.size() > 1){
					for(Element elAgent : elAgentList){
						Person teamMember = makeAgent(elAgent, ns, authorMap, success);
						team.addTeamMember(teamMember);
					}
				}else if(elAgentList.size() == 1){
					result = makeAgent(elAgentList.get(0), ns, authorMap, success);
				}
			}else{
				childName = "Simple";
				obligatory = true;
				Element elSimple = XmlHelp.getSingleChildElement(success, elNameCitation, childName, ns, obligatory);
				String simple = (elSimple == null)? "" : elSimple.getTextNormalize();
				result = Team.NewInstance();
				result.setNomenclaturalTitle(simple);
			}
		}
		return result;
	}

	private Person makeAgent(Element elAgentName, Namespace ns, MapWrapper<Person> agentMap, ResultWrapper<Boolean> success){
		Person result = null;
		if (elAgentName != null){
			String authorTitle = elAgentName.getTextNormalize();
			result = Person.NewTitledInstance(authorTitle);
			Class<? extends Person> clazz = Person.class;
			result = makeReferenceType(elAgentName, clazz, agentMap, success);
			return result;
		}else{
			return null;
		}
	}









	protected Integer getIntegerYear(String year){
		try {
			Integer result = Integer.valueOf(year);
			return result;
		} catch (NumberFormatException e) {
			logger.warn("Year string could not be parsed. Set = 9999 instead");
			return 9999;
		}
	}

	protected String removeVersionOfRef(String ref){
		if (ref != null && ref.matches("urn:lsid:ipni.org:.*:.*:.*")){
			return ref = ref.substring(0,ref.lastIndexOf(":"));
		} else {
			return ref;
		}

	}



	protected void makeTypification(TaxonName name, Element elTypifiacation, ResultWrapper<Boolean> success){
		if (elTypifiacation != null){
			//logger.warn("makeTypification not yet implemented");
			//success.setValue(false);
		}
	}


	protected void makePublicationStatus(TaxonName name, Element elPublicationStatus, ResultWrapper<Boolean> success){
		//Status

		if (elPublicationStatus != null){
			String pubStat = elPublicationStatus.getAttributeValue("Note");

		}
	}

	protected void makeProviderLink(TaxonName name, Element elProviderLink, ResultWrapper<Boolean> success){
		if (elProviderLink != null){
			//logger.warn("makeProviderLink not yet implemented");
			//success.setValue(false);
		}
	}


	protected void makeProviderSpecificData(TaxonName name, Element elProviderSpecificData, ResultWrapper<Boolean> success, TcsXmlImportState state){
		if (elProviderSpecificData != null){

			Namespace ns = elProviderSpecificData.getNamespace();

			String childName = "ipniData";
			boolean obligatory = true;
			List<Element> elIpniData = elProviderSpecificData.getChildren();
			Element el =  elIpniData.get(0);


			childName = "citationType";
			ns = el.getNamespace();

			Element elCitationType = XmlHelp.getSingleChildElement(success, el, childName, ns, obligatory);

			childName = "referenceRemarks";
			Element elReferenceRemarks = XmlHelp.getSingleChildElement(success, el, childName, ns, obligatory);

			childName = "suppressed";
			Element elSuppressed = XmlHelp.getSingleChildElement(success, el, childName, ns, obligatory);

			childName = "score";
			Element elScore = XmlHelp.getSingleChildElement(success, el, childName, ns, obligatory);

			childName = "nomenclaturalSynonym";
			Element elNomenclaturalSynonym = XmlHelp.getSingleChildElement(success, el, childName, ns, obligatory);
			 ns = elProviderSpecificData.getNamespace();
			childName = "RelatedName";
			Element elRelatedName = XmlHelp.getSingleChildElement(success, elNomenclaturalSynonym, childName, ns, obligatory);

			//create homotypic synonym
			if (elRelatedName != null){
    			String id =elRelatedName.getAttributeValue("ref");
    			System.out.println(removeVersionOfRef(id));
    			if (name.getTaxa().iterator().hasNext()){
    			    Taxon taxon = (Taxon) name.getTaxa().iterator().next();
    			    //if taxon already exist
    			    taxon.addHomotypicSynonym((Synonym)state.getStore(TAXON_STORE).get(removeVersionOfRef(id)));
    			    //otherwise add to a map for homotypic synonyms
    			}
			}

		}
	}

	@Override
    protected boolean isIgnore(TcsXmlImportState state){
		return ! state.getConfig().isDoTaxonNames();
	}


	protected static final Reference unknownSec(){
		Reference result = ReferenceFactory.newGeneric();
		result.setTitleCache("UNKNOWN", true);
		return result;
	}



}
