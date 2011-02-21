/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 04.08.2008
 * @version 1.0
 */
public abstract class EfloraImportBase  extends CdmImportBase<EfloraImportConfigurator, EfloraImportState> {
	private static final Logger logger = Logger.getLogger(EfloraImportBase.class);

	
	protected abstract boolean doInvoke(EfloraImportState state);

	
	
	protected Element getBodyElement(EfloraImportConfigurator config){
		Element root = config.getSourceRoot();
		
		if (! "body".equalsIgnoreCase(root.getName())){
			logger.error("Root element is not 'body'");
			return null;
		}
		//TODO prevent multiple elements
		
		return root;
	}
	
	
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
				result.setTitleCache(title, true);
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
