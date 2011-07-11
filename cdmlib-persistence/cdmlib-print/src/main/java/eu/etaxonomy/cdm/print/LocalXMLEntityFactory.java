// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.print;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.print.XMLHelper.EntityType;
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

/**
 * The local entity factory assumes that an application context is available and
 * may be accessed directly without the detour via http.
 * 
 * @author n.hoffmann
 * @created Jul 16, 2010
 * @version 1.0
 */
@Component
public class LocalXMLEntityFactory extends AbstractXmlEntityFactory {
	private static final Logger logger = Logger
			.getLogger(LocalXMLEntityFactory.class);

	private final JsonView xmlView;

	@Autowired
	private final ICdmApplicationConfiguration applicationConfiguration;

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

	private final IProgressMonitor monitor;

	/**
	 * 
	 * @param applicationConfiguration
	 * @param monitor
	 */
	protected LocalXMLEntityFactory(
			ICdmApplicationConfiguration applicationConfiguration,
			IProgressMonitor monitor) {
		this.applicationConfiguration = applicationConfiguration;
		this.monitor = monitor;
		this.xmlView = new JsonView();
		xmlView.setType(Type.XML);
		initControllers();
		initJsonConfigs();
	}

	/**
	 * 
	 */
	private void initControllers() {
		classificationListController = (ClassificationListController) applicationConfiguration
				.getBean("classificationListController");
		classificationController = (ClassificationController) applicationConfiguration
				.getBean("classificationController");
		taxonNodeListController = (TaxonNodeListController) applicationConfiguration
				.getBean("taxonNodeListController");
		taxonNodeController = (TaxonNodeController) applicationConfiguration
				.getBean("taxonNodeController");

		nameController = (NameController) applicationConfiguration
				.getBean("nameController");

		featureTreeListController = (FeatureTreeListController) applicationConfiguration
				.getBean("featureTreeListController");
		featureTreeController = (FeatureTreeController) applicationConfiguration
				.getBean("featureTreeController");
		featureNodeController = (FeatureNodeController) applicationConfiguration
				.getBean("featureNodeController");

		taxonPortalController = (TaxonPortalController) applicationConfiguration
				.getBean("taxonPortalController");
	}

	/**
	 * 
	 */
	private void initJsonConfigs() {
		jsonConfig = (JsonConfig) applicationConfiguration.getBean("jsonConfig");
		jsonConfigPortal = (JsonConfig) applicationConfiguration
				.getBean("jsonConfigPortal");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getClassifications()
	 */
	public List<Element> getClassifications() {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = classificationListController.doList(null, null,
				null);

		Element result = render(resultObject);

		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getChildNodes(org.jdom.
	 * Element)
	 */
	public List<Element> getChildNodes(Element treeNode) {
		xmlView.setJsonConfig(jsonConfig);
		EntityType entityType = XMLHelper.getEntityType(treeNode);

		UUID uuid = XMLHelper.getUuid(treeNode);

		Object resultObject = null;
		try {
			if (EntityType.CLASSIFICATION.equals(entityType)) {
				resultObject = classificationController.getChildNodes(uuid,
						null);
			} else if (EntityType.TAXON_NODE.equals(entityType)) {
				resultObject = taxonNodeListController
						.getChildNodes(uuid, null);
			}
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}

		Element result = render(resultObject);

		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonNodeByUuid(java
	 * .util.UUID)
	 */
	public Element getTaxonNode(UUID taxonNodeUuid) {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = null;
		try {
			resultObject = taxonNodeController.doGet(taxonNodeUuid, null, null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}
		Element result = render(resultObject);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTrees()
	 */
	public List<Element> getFeatureTrees() {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = featureTreeListController.doList(0, -1, null);

		Element result = render(resultObject);

		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureNode(java.util
	 * .UUID)
	 */
	public Element getFeatureNode(UUID uuid) {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = null;
		try {
			resultObject = featureNodeController.doGet(uuid, null, null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}
		Element result = render(resultObject);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureForFeatureNode
	 * (java.util.UUID)
	 */
	public Element getFeatureForFeatureNode(UUID uuid) {
		xmlView.setJsonConfig(jsonConfig);
		Object resultObject = null;
		try {
			resultObject = featureNodeController.getCdmBaseProperty(uuid,
					"feature", null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}
		Element result = render(resultObject);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTree()
	 */
	public Element getFeatureTree(UUID uuid) {
		xmlView.setJsonConfig(jsonConfig);

		Object resultObject = null;
		try {
			resultObject = featureTreeController.doGet(uuid, null, null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}
		Element result = render(resultObject);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonForTaxonNode(org
	 * .jdom.Element)
	 */
	public Element getTaxonForTaxonNode(Element taxonNodeElement) {
		xmlView.setJsonConfig(jsonConfig);
		UUID uuid = XMLHelper.getUuid(taxonNodeElement);

		Object resultObject = null;
		try {
			resultObject = taxonNodeController.getCdmBaseProperty(uuid,
					"taxon", null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}

		Element result = render(resultObject);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getAcceptedTaxonElement
	 * (org.jdom.Element)
	 */
	public Element getAcceptedTaxonElement(Element taxonElement) {
		xmlView.setJsonConfig(jsonConfigPortal);
		UUID uuid = XMLHelper.getUuid(taxonElement);

		Object resultObject = null;
		try {
			resultObject = HibernateProxyHelper.deproxy(taxonPortalController
					.doGet(uuid, null, null));
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}

		Element result = render(resultObject);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getSynonymy(org.jdom.Element
	 * )
	 */
	public List<Element> getSynonymy(Element taxonElement) {
		xmlView.setJsonConfig(jsonConfigPortal);
		UUID uuid = XMLHelper.getUuid(taxonElement);

		ModelAndView resultObject = null;
		try {
			resultObject = taxonPortalController
					.doGetSynonymy(uuid, null, null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}

		Element result = render(resultObject.getModel().values().iterator()
				.next());

		List<Element> elementList = new ArrayList<Element>();

		for (Object child : result.getChildren()) {
			if (child instanceof Element) {
				Element childElement = (Element) ((Element) child).clone();

				childElement.detach();

				elementList.add(childElement);
			}
		}

		return elementList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getTypeDesignations(org
	 * .jdom.Element)
	 */
	public List<Element> getTypeDesignations(Element nameElement) {
		xmlView.setJsonConfig(jsonConfig);

		UUID uuid = XMLHelper.getUuid(nameElement);

		Object resultObject = null;
		try {
			resultObject = nameController.getCdmBaseProperty(uuid,
					"typeDesignations", null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}
		Element result = render(resultObject);

		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getDescriptions(org.jdom
	 * .Element)
	 */
	public Element getDescriptions(Element taxonElement) {
		xmlView.setJsonConfig(jsonConfigPortal);
		UUID uuid = XMLHelper.getUuid(taxonElement);

		Object resultObject = null;
		try {
			resultObject = taxonPortalController.doGetDescriptions(uuid, null,
					null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
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
	private Element render(Object result) {
		File tmpFile = null;
		try {
			Document document = new Document();
			tmpFile = File.createTempFile("printpublisher", null);

			PrintWriter writer = new PrintWriter(tmpFile, "UTF-8");

			xmlView.render(HibernateProxyHelper.deproxy(result), writer);

			document = builder.build(tmpFile);

			Element root = document.getRootElement();

			return root;
			
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			monitor.warning(e.getLocalizedMessage(), e);
		} finally {
			if (tmpFile != null)
				tmpFile.delete();
		}

		return new Element("somethingWentWrong");
	}
}
