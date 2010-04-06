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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.tcsrdf.CdmSingleAttributeXmlMapperBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * @author a.mueller
 * @created 04.08.2008
 * @version 1.0
 */
public abstract class TcsXmlImportBase  extends CdmImportBase<TcsXmlImportConfigurator, TcsXmlImportState> {
	private static final Logger logger = Logger.getLogger(TcsXmlImportBase.class);

	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");
	protected static Namespace nsTn = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonName#");
	protected static Namespace nsTgeo = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/GeographicRegion#");
	protected static Namespace nsTc = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonConcept#");
	protected static Namespace nsTpub = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/PublicationCitation#");
	protected static Namespace nsTpalm = Namespace.getNamespace("http://wp5.e-taxonomy.eu/import/palmae/common");
	
	
	protected abstract boolean doInvoke(TcsXmlImportState state);
	ReferenceFactory refFactory = ReferenceFactory.newInstance();
	
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
		if(ref == null && linkType == null){
			result = getInstance(clazz);
			if (result != null){
				String title = element.getTextNormalize();
				result.setTitleCache(title);
			}
		}else if (linkType == null || linkType.equals("local")){
			//TODO
			result = objectMap.get(ref);
			if (result == null){
				logger.warn("Object (ref = " + ref + ")could not be found in WrapperMap");
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
	
	
	protected ReferenceBase makeAccordingTo(Element elAccordingTo, MapWrapper<ReferenceBase> referenceMap, ResultWrapper<Boolean> success){
		ReferenceBase result = null;
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
				result = refFactory.newGeneric();
				String title = elSimple.getTextNormalize();
				result.setTitleCache(title);
			}
		}
		return result;
	}
	
	
	private ReferenceBase makeAccordingToDetailed(Element elAccordingToDetailed, MapWrapper<ReferenceBase> referenceMap, ResultWrapper<Boolean> success){
		ReferenceBase result = null;
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
			result = makeReferenceType(elPublishedIn, ReferenceBase.class, referenceMap, success);
			
			//MicroReference
			childName = "MicroReference";
			obligatory = false;
			Element elMicroReference = XmlHelp.getSingleChildElement(success, elAccordingToDetailed, childName, tcsNamespace, obligatory);
			String microReference = elMicroReference.getTextNormalize();
			if (CdmUtils.Nz(microReference).equals("")){
				//TODO
				logger.warn("MicroReference not yet implemented for AccordingToDetailed");	
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



}
