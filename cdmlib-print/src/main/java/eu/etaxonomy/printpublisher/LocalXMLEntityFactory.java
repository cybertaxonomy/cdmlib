// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.printpublisher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.remote.controller.ClassificationController;
import eu.etaxonomy.cdm.remote.controller.ClassificationListController;
import eu.etaxonomy.cdm.remote.controller.FeatureNodeController;
import eu.etaxonomy.cdm.remote.controller.FeatureTreeController;
import eu.etaxonomy.cdm.remote.controller.FeatureTreeListController;
import eu.etaxonomy.cdm.remote.controller.NameController;
import eu.etaxonomy.cdm.remote.controller.TaxonNodeController;
import eu.etaxonomy.cdm.remote.controller.TaxonNodeListController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController;
import eu.etaxonomy.cdm.remote.view.JsonView;
import eu.etaxonomy.cdm.remote.view.JsonView.Type;
import eu.etaxonomy.printpublisher.XMLHelper.EntityType;

/**
 * The local entity factory assumes that an application context is available and may be accessed
 * directly without the detour via http.
 * 
 * @author n.hoffmann
 * @created Jul 16, 2010
 * @version 1.0
 */
@Component
public class LocalXMLEntityFactory extends AbstractXmlEntityFactory {
	private static final Logger logger = Logger
			.getLogger(LocalXMLEntityFactory.class);
		
	private JsonView xmlView;

	@Autowired
	private CdmApplicationController applicationController;
	
	@Autowired
	private JsonConfig jsonConfig;
	@Autowired
	private JsonConfig jsonConfigPortal;
	
	@Autowired
	private ClassificationListController classificationListController;
	@Autowired
	private ClassificationController classificationController;
	@Autowired
	private TaxonNodeListController taxonNodeListController;
	@Autowired
	private TaxonNodeController taxonNodeController;
	@Autowired
	private NameController nameController;
	@Autowired
	private FeatureTreeListController featureTreeListController;
	
	@Autowired
	private TaxonPortalController taxonPortalController;

	@Autowired
	private FeatureTreeController featureTreeController;

	@Autowired
	private FeatureNodeController featureNodeController;

	/**
	 * 
	 * @param applicationController
	 */
	protected LocalXMLEntityFactory(CdmApplicationController applicationController){
		this.applicationController = applicationController;
		this.xmlView = new JsonView();
		xmlView.setType(Type.XML);
		initControllers();
		initJsonConfigs();
	}
	
	/**
	 * 
	 */
	private void initControllers(){
		classificationListController = (ClassificationListController) applicationController.getBean("classificationListController");
		classificationController = (ClassificationController) applicationController.getBean("classificationController");
		taxonNodeListController = (TaxonNodeListController) applicationController.getBean("taxonNodeListController");
		taxonNodeController = (TaxonNodeController) applicationController.getBean("taxonNodeController");
		
		nameController = (NameController) applicationController.getBean("nameController");
		
		featureTreeListController = (FeatureTreeListController) applicationController.getBean("featureTreeListController");
		featureTreeController = (FeatureTreeController) applicationController.getBean("featureTreeController");
		featureNodeController = (FeatureNodeController) applicationController.getBean("featureNodeController");
		
		taxonPortalController = (TaxonPortalController) applicationController.getBean("taxonPortalController");
	}
	
	/**
	 * 
	 */
	private void initJsonConfigs(){
		jsonConfig = (JsonConfig) applicationController.getBean("jsonConfig");
		jsonConfigPortal = (JsonConfig) applicationController.getBean("jsonConfigPortal");
	}
		
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getClassifications()
	 */
	public List<Element> getClassifications() {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = classificationListController.doList(null, null, null);
		
		Element result = render(resultObject);
		
		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getChildNodes(org.jdom.Element)
	 */
	public List<Element> getChildNodes(Element treeNode) {
		xmlView.setJsonConfig(jsonConfig);
		EntityType entityType = XMLHelper.getEntityType(treeNode);
		
		UUID uuid = XMLHelper.getUuid(treeNode);
		
		Object resultObject = null;
		try {
			if(EntityType.CLASSIFICATION.equals(entityType)){
				resultObject = classificationController.getChildNodes(uuid, null);
			}
			else if(EntityType.TAXON_NODE.equals(entityType)){
				resultObject = taxonNodeListController.getChildNodes(uuid, null);
			}
		} catch (IOException e) {
			logger.error(e);
		}
		
		Element result = render(resultObject); 
		
		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonNodeByUuid(java.util.UUID)
	 */
	public Element getTaxonNode(UUID taxonNodeUuid) {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = null;
		try {
			resultObject = taxonNodeController.doGet(taxonNodeUuid, null, null);
		} catch (IOException e) {
			logger.error(e);
		}
		Element result = render(resultObject); 
		
		return result;
	}
	

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTrees()
	 */
	@Override
	public List<Element> getFeatureTrees() {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = featureTreeListController.doList(0, -1, null);
		
		Element result = render(resultObject);
		
		return processElementList(result);
	}
	
	@Override
	public Element getFeatureNode(UUID uuid) {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = null;
		try {
			resultObject = featureNodeController.doGet(uuid, null, null);
		} catch (IOException e) {
			logger.error(e);
		}
		Element result = render(resultObject); 
		
		return result;
	}

	@Override
	public Element getFeatureForFeatureNode(UUID uuid) {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = null;
		try {
			resultObject = featureNodeController.getCdmBaseProperty(uuid, "feature", null);
		} catch (IOException e) {
			logger.error(e);
		}
		Element result = render(resultObject); 
		
		return result;
	}
	

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTree()
	 */
	@Override
	public Element getFeatureTree(UUID uuid) {
		xmlView.setJsonConfig(jsonConfig);
		
		Object resultObject = null;
		try {
			resultObject = featureTreeController.doGet(uuid, null, null);
		} catch (IOException e) {
			logger.error(e);
		}
		Element result = render(resultObject);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonForTaxonNode(org.jdom.Element)
	 */
	public Element getTaxonForTaxonNode(Element taxonNodeElement) {
		xmlView.setJsonConfig(jsonConfig);
		UUID uuid = XMLHelper.getUuid(taxonNodeElement);
		
		Object resultObject = null;
		try {
			resultObject = taxonNodeController.getCdmBaseProperty(uuid, "taxon", null);
		} catch (IOException e) {
			logger.error(e);
		}
		
		Element result = render(resultObject); 
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getAcceptedTaxonElement(org.jdom.Element)
	 */
	public Element getAcceptedTaxonElement(Element taxonElement) {
		xmlView.setJsonConfig(jsonConfigPortal);
		UUID uuid = XMLHelper.getUuid(taxonElement);
		
		Object resultObject = null;
		try {
			resultObject = HibernateProxyHelper.deproxy(taxonPortalController.doGet(uuid, null, null));
		} catch (IOException e) {
			logger.error(e);
		}
		
		Element result = render(resultObject); 
		
		return result; 
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getSynonymy(org.jdom.Element)
	 */
	public List<Element> getSynonymy(Element taxonElement) {
		xmlView.setJsonConfig(jsonConfigPortal);
		UUID uuid = XMLHelper.getUuid(taxonElement);
		
		ModelAndView resultObject = null;
		try {
			resultObject = taxonPortalController.doGetSynonymy(uuid, null, null);
		} catch (IOException e) {
			logger.error(e);
		}
		
		Element result = render(resultObject.getModel().values().iterator().next()); 
		
		List<Element> elementList = new ArrayList<Element>();
		
		for(Object child : result.getChildren()){
			if(child instanceof Element){
				Element childElement = (Element) ((Element)child).clone();
				
				childElement.detach();
				
				elementList.add(childElement);
			}
		}
		
		return elementList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getTypeDesignations(org.jdom.Element)
	 */
	@Override
	public List<Element> getTypeDesignations(Element nameElement) {
		xmlView.setJsonConfig(jsonConfig);
		
		UUID uuid = XMLHelper.getUuid(nameElement);
		
		Object resultObject = null;
		try {
			resultObject = nameController.getCdmBaseProperty(uuid, "typeDesignations", null);
		} catch (IOException e) {
			logger.error(e);
		}
		Element result = render(resultObject); 
		
		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getDescriptions(org.jdom.Element)
	 */
	public Element getDescriptions(Element taxonElement) {
		xmlView.setJsonConfig(jsonConfigPortal);
		UUID uuid = XMLHelper.getUuid(taxonElement);
		
		Object resultObject = null;
		try {
			resultObject = taxonPortalController.doGetDescriptions(uuid, null, null);
		} catch (IOException e) {
			logger.error(e);
		}
		
		Element result = render(resultObject); 
		
		return result;
	}

	/**
	 * 
	 * @param result
	 * @return
	 */
	private Element render(Object result){
		Document document = new Document();
		File tmpFile = null;
		
		try{
			tmpFile = File.createTempFile("printpublisher", null);
			
			PrintWriter writer = new PrintWriter(tmpFile, "UTF-8");
			
			
			xmlView.render(HibernateProxyHelper.deproxy(result), writer);
			
			document = builder.build(tmpFile); 
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			if(tmpFile != null)
				tmpFile.delete();
		}
		
		Element root = document.getRootElement();
		
		return root;
	}
}
