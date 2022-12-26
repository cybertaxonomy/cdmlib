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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.application.ICdmApplication;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.print.XMLHelper.EntityType;
import eu.etaxonomy.cdm.remote.controller.ClassificationController;
import eu.etaxonomy.cdm.remote.controller.ClassificationListController;
import eu.etaxonomy.cdm.remote.controller.NameController;
import eu.etaxonomy.cdm.remote.controller.TaxonNodePrintAppController;
import eu.etaxonomy.cdm.remote.controller.TaxonPortalController;
import eu.etaxonomy.cdm.remote.controller.TermNodeController;
import eu.etaxonomy.cdm.remote.controller.TermTreeController;
import eu.etaxonomy.cdm.remote.controller.TermTreeListController;
import eu.etaxonomy.cdm.remote.controller.dto.PolytomousKeyNodeDtoController;
import eu.etaxonomy.cdm.remote.view.JsonView;
import eu.etaxonomy.cdm.remote.view.JsonView.Type;
import net.sf.json.JsonConfig;

/**
 * The local entity factory assumes that an application context is available and
 * may be accessed directly without the detour via http.
 *
 * @author n.hoffmann
 * @since Jul 16, 2010
 */
@Component
public class LocalXMLEntityFactory extends XmlEntityFactoryBase {

    private static final Logger logger = LogManager.getLogger();

    private final JsonView xmlView;

    @Autowired
    private final ICdmApplication repository;

    @Autowired
    private JsonConfig jsonConfig;
    @Autowired
    private JsonConfig jsonConfigPortal;

    @Autowired
    private ClassificationListController classificationListController;
    @Autowired
    private ClassificationController classificationController;
    @Autowired
    private TaxonNodePrintAppController taxonNodePrintAppController;
    @Autowired
    private NameController nameController;
    @Autowired
    private TermTreeListController featureTreeListController;

    @Autowired
    private TaxonPortalController taxonPortalController;

    @Autowired
    private TermTreeController featureTreeController;

    @Autowired
    private TermNodeController termNodeController;

    @Autowired
    private PolytomousKeyNodeDtoController polytomousKeyNodeDtoController;

    private final IProgressMonitor monitor;

    protected LocalXMLEntityFactory(
            ICdmApplication applicationConfiguration,
            IProgressMonitor monitor) {
        this.repository = applicationConfiguration;
        this.monitor = monitor;
        this.xmlView = new JsonView();
        xmlView.setType(Type.XML);
        initControllers();
        initJsonConfigs();
    }

    private void initControllers() {
    	classificationListController = (ClassificationListController) repository
    			.getBean("classificationListController");
    	classificationController = (ClassificationController) repository
    			.getBean("classificationController");
    	taxonNodePrintAppController = (TaxonNodePrintAppController) repository
    			.getBean("taxonNodePrintAppController");

    	nameController = (NameController) repository
    			.getBean("nameController");

    	featureTreeListController = (TermTreeListController) repository
    			.getBean("featureTreeListController");
    	featureTreeController = (TermTreeController) repository
    			.getBean("featureTreeController");
    	termNodeController = (TermNodeController) repository
    			.getBean("featureNodeController");

    	taxonPortalController = (TaxonPortalController) repository
    			.getBean("taxonPortalController");

    	polytomousKeyNodeDtoController = (PolytomousKeyNodeDtoController) repository.getBean("polytomousKeyNodeDtoController");
    }

    private void initJsonConfigs() {
        jsonConfig = (JsonConfig) repository.getBean("jsonConfig");
        jsonConfigPortal = (JsonConfig) repository
                .getBean("jsonConfigPortal");
    }

    @Override
    public List<Element> getClassifications() {
        xmlView.setJsonConfig(jsonConfig);
        Object resultObject = classificationListController.doList(null, null,
                null, null, null);

        Element result = render(resultObject);

        return processElementList(result);
    }

    @Override
    public List<Element> getChildNodes(Element treeNode) {
        xmlView.setJsonConfig(jsonConfig);
        EntityType entityType = XMLHelper.getEntityType(treeNode);

        UUID uuid = XMLHelper.getUuid(treeNode);

        boolean includeUnpublished = DaoBase.NO_UNPUBLISHED;  //for now we do not allow any remote service to publish unpublished data

        Object resultObject = null;
        try {
            if (EntityType.CLASSIFICATION.equals(entityType)) {
                resultObject = classificationController.getChildNodes(uuid, null,
                        null, null, null);
            } else if (EntityType.TAXON_NODE.equals(entityType)) {
                resultObject = taxonNodePrintAppController
                        .getChildNodes(uuid, includeUnpublished, null);
            }
        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            logger.error(e);
        }

        Element result = render(resultObject);

        return processElementList(result);
    }

    @Override
    public Element getTaxonNode(UUID taxonNodeUuid) {
        xmlView.setJsonConfig(jsonConfig);
        Object resultObject = null;
        try {
            resultObject = taxonNodePrintAppController.doGet(taxonNodeUuid, null, null);
        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            logger.error(e);
        }
        Element result = render(resultObject);

        return result;
    }

    @Override
    public List<Element> getFeatureTrees() {
        xmlView.setJsonConfig(jsonConfig);
        Object resultObject = featureTreeListController.doList(0, -1, null, null, null);

        Element result = render(resultObject);

        return processElementList(result);
    }

    @Override
    public Element getTermNode(UUID uuid) {
        xmlView.setJsonConfig(jsonConfig);
        Object resultObject = null;
        try {
            resultObject = termNodeController.doGet(uuid, null, null);
        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            logger.error(e);
        }
        Element result = render(resultObject);

        return result;
    }

    @Override
    public Element getFeatureForTermNode(UUID uuid) {
        xmlView.setJsonConfig(jsonConfig);
        Object resultObject = null;
        try {
            resultObject = termNodeController.getCdmBaseProperty(uuid,
                    "feature", null);
        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            logger.error(e);
        }
        Element result = render(resultObject);

        return result;
    }

    @Override
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

    @Override
    public Element getTaxonForTaxonNode(Element taxonNodeElement) {
        xmlView.setJsonConfig(jsonConfig);
        UUID uuid = XMLHelper.getUuid(taxonNodeElement);

        Object resultObject = null;
        try {
            resultObject = taxonNodePrintAppController.doGetTaxon(uuid);
        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            logger.error(e);
        }

        Element result = render(resultObject);

        return result;
    }

    @Override
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


    @Override
    public List<Element> getSynonymy(Element taxonElement) {
        xmlView.setJsonConfig(jsonConfigPortal);
        UUID uuid = XMLHelper.getUuid(taxonElement);

        ModelAndView resultObject = null;
        try {
            resultObject = taxonPortalController
                    .doGetSynonymy(uuid, null, null, null);
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

    @Override
    public List<Element> getTypeDesignations(Element nameElement) {
        xmlView.setJsonConfig(jsonConfig);

        UUID uuid = XMLHelper.getUuid(nameElement);

        Object resultObject = null;

        try {
            resultObject = nameController.getCdmBaseProperty(uuid,"typeDesignations", null);

    		//LORNA: could use service here directly instead of controller with request set to null
            //resultObject = nameController.doListNameTypeDesignations(uuid, null, null);

        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            logger.error(e);
        }
        Element result = render(resultObject);

        return processElementList(result);
    }

    @Override
    public Element getDescriptions(Element taxonElement) {
        xmlView.setJsonConfig(jsonConfigPortal);
        UUID uuid = XMLHelper.getUuid(taxonElement);

		Object resultObject = null;

		try {
			resultObject = taxonPortalController.doGetDescriptions(uuid, null, null,
					null, null);
		} catch (IOException e) {
			monitor.warning(e.getLocalizedMessage(), e);
			logger.error(e);
		}

		Element result = render(resultObject);
        //Element result = render(resultObject);

        return result;
    }


    @Override
    public Element getPolytomousKey(Element taxonElement) {
    	xmlView.setJsonConfig(jsonConfigPortal);
    	UUID uuid = XMLHelper.getUuid(taxonElement);

    	ModelAndView resultObject = null;
    	try {
    		//e.g. uuid 02b6579c-2f6d-4df0-b77c-e5d259ddb307 must be the uuid of the polytomous key. Where do we get that
    		//check web service calls in portal.......
    		/////resultObject = polytomousKeyNodeDtoController.doLinkedStyle(uuid, null, null);
    		resultObject = polytomousKeyNodeDtoController.doLinkedStyleByTaxonomicScope(uuid, null, null, null, null);

    	} catch (IOException e) {
    		monitor.warning(e.getLocalizedMessage(), e);
    		logger.error(e);
    	}

    	Element result = null;

    	if (resultObject.getModel().values().iterator().hasNext()) {

    		result = render(resultObject.getModel().values().iterator().next());
    	}

    	return result;

    	//List<Element> elementList = new ArrayList<Element>();

    	/*if (result != null){

    		for (Object child : result.getChildren()) {
    			if (child instanceof Element) {
    				Element childElement = (Element) ((Element) child).clone();
    				childElement.detach();

    				elementList.add(childElement);
    			} else {
    				logger.error("child is not an instance of element");
    			}
    		}
    	}
    	return elementList;*/
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

            xmlView.render(HibernateProxyHelper.deproxy(result), writer, null, null, null);

            document = builder.build(tmpFile);

            Element root = document.getRootElement();

            return root;

        } catch (IOException e) {
            monitor.warning(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            monitor.warning(e.getLocalizedMessage(), e);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }

        return new Element("somethingWentWrong");
    }

    @Override
    public List<Element> getReferences(Element referenceElement) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public Element getTaxonNodesByName(String taxonName, String classification) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Element> getMedia(Element taxonElement) {
		// TODO Auto-generated method stub
		return null;
	}
}
