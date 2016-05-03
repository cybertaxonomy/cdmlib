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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.print.XMLHelper.EntityType;

/**
 * Implementation of an IXMLEntityFactory that is connected to a CDM Community
 * Server on a remote machine. API call will be executed by accessing the
 * servers REST API.
 * 
 * @author n.hoffmann
 * @author l.morris
 * @created Apr 6, 2010
 * @version 1.0
 */
public class RemoteXMLEntityFactory extends XmlEntityFactoryBase {
	private static final Logger logger = Logger
			.getLogger(RemoteXMLEntityFactory.class);

	private URL serviceUrl;

	private IProgressMonitor monitor;

	private static final List<NameValuePair> UNLIMITED_RESULTS = Arrays
			.asList(new NameValuePair[] { new BasicNameValuePair("start", "0"),
					new BasicNameValuePair("limit", "-1") });

	private static final String UUID = "{uuid}";

	private static final String CLASSIFICATIONS = "classification";
	private static final String CLASSIFICATION_CHILD_NODES = "classification/"
			+ UUID + "/childNodes/";
	private static final String TAXONNODE_CHILD_NODES = "taxonNode/" + UUID
			+ "/childNodes/";
	private static final String TAXONNODE = "taxonNode/" + UUID;
	private static final String TAXONNODE_TAXON = TAXONNODE + "/taxon";
	// private static final String TAXONNODES = TAXONNODE + "/taxon";
	private static final String TAXA_AND_NAMES = "taxon/findTaxaAndNames";
	private static final String TAXONNODES = "taxon/" + UUID + "/taxonNodes";
	// http://dev.e-taxonomy.eu/cdmserver/palmae/portal/taxon/d58c0b44-29f8-4071-aa49-32baa185296f/taxonNodes

	private static final String FEATURETREES = "featureTree";
	private static final String FEATURETREE = "featureTree/" + UUID;
	private static final String FEATURENODE = "featurenode/" + UUID;
	// private static final String FEATURENODE = "featureNode/" + UUID +
	// "/childNodes";
	private static final String FEATURENODE_FEATURE = FEATURENODE + "/feature";

	private static final String NAME_TYPE_DESIGNATIONS = "name/" + UUID
			+ "/typeDesignations";

	// TAXON_ACCEPTED should populate references but authorship is not always
	// populated so call the reference controller directly
	private static final String REFERENCES = "portal/reference/" + UUID;

	private static final String TAXON_ACCEPTED = "portal/taxon/" + UUID;
	private static final String TAXON_SYNONYMY = "portal/taxon/" + UUID
			+ "/synonymy";
	private static final String TAXON_DESCRIPTIONS = "portal/taxon/" + UUID
			+ "/descriptions";

	private static final String POLYTOMOUS_KEY = "dto/polytomousKey/linkedStyle?findByTaxonomicScope="
			+ UUID;
	// dto/polytomousKey/linkedStyle.json?findByTaxonomicScope=f820f533-06f2-4116-87e9-c9319c0c1cbf

	// images /portal/taxon/{uuid}
	private static final String TAXON_MEDIA = "portal/taxon/" + UUID + "/media";

	// http://test.e-taxonomy.eu/cdmserver/palmae/portal/media/bb49e8f0-49bc-41f7-9674-1cb36eb716fb

	/**
	 * Creates new instance of this factory and connects it to the given CDM
	 * Community Stores access point.
	 * 
	 * Typically, there is no need to instantiate this class.
	 * 
	 * @param monitor
	 */
	protected RemoteXMLEntityFactory(URL webserviceUrl, IProgressMonitor monitor) {
		this.serviceUrl = webserviceUrl;
		this.monitor = monitor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getClassifications()
	 */
	public List<Element> getClassifications() {
		Element result = queryServiceWithParameters(CLASSIFICATIONS,
				UNLIMITED_RESULTS);
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
		EntityType entityType = XMLHelper.getEntityType(treeNode);

		Element result = null;

		if (EntityType.CLASSIFICATION.equals(entityType)) {
			result = queryService(treeNode, CLASSIFICATION_CHILD_NODES);
		} else if (EntityType.TAXON_NODE.equals(entityType)) {
			result = queryService(treeNode, TAXONNODE_CHILD_NODES);
		}

		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonNode(java.util.
	 * UUID)
	 */
	public Element getTaxonNode(UUID taxonNodeUuid) {
		return queryService(taxonNodeUuid, TAXONNODE);
	}

	/*
	 * Returns the taxonNode for a specific taxon name string.
	 */
	public Element getTaxonNodesByName(String taxonName, String classification) {

		// 1-To find the uuid of a Taxon name:
		// http://dev.e-taxonomy.eu/cdmserver/palmae/taxon/findTaxaAndNames.json?doTaxa=1&matchMode=EXACT&query=Acrocomia
		// 2-Then get the taxonNodes for this name:
		// http://dev.e-taxonomy.eu/cdmserver/palmae/portal/taxon/d58c0b44-29f8-4071-aa49-32baa185296f/taxonNodes
		Element taxonNodes = null;

		List <NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("doTaxa", "1"));
		params.add(new BasicNameValuePair("matchMode", "EXACT"));
		params.add(new BasicNameValuePair("query", taxonName));

		if (classification != null) {
			List<Element> classifications = getClassifications();
			String classificationUuid = "";

			// String xPathString="//titleCache[.='" +
			// classification+"']/../uuid";

			for (Element element : classifications) {
				if (element.getChild("titleCache").getValue()
						.equals(classification)) {
					System.out.println(element.getChild("uuid").getValue());
					classificationUuid = element.getChild("uuid").getValue();
				}
			}
			params.add(new BasicNameValuePair("tree", classificationUuid));
		}

		Element element = queryServiceWithParameters(TAXA_AND_NAMES, params);

		Element taxonNodeString = null;
		UUID taxonNodeUuid = null;

		try {

			// TODO: we only get the first Taxon to match this name - we should
			// get all Taxons to match the name
			Element taxonUuid = (Element) XPath.selectSingleNode(element,
					"//records/e[1]");

			taxonNodes = queryService(taxonUuid, TAXONNODES);

			taxonNodeString = (Element) XPath.selectSingleNode(taxonNodes,
					"//PersistentSet/e[1]/uuid");
			String val = taxonNodeString.getValue();
			taxonNodeUuid = java.util.UUID.fromString(taxonNodeString
					.getValue());

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getTaxonNode(taxonNodeUuid);
		// TODO: Should be return a set of taxonNodes if there is more than one
		// for the name
		// public List<Element> getTaxonNodesByName(String taxonName) {
		// return taxonNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTree(java.util
	 * .UUID)
	 */
	public Element getFeatureTree(UUID featureTreeUuid) {
		return queryService(featureTreeUuid, FEATURETREE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTrees()
	 */
	public List<Element> getFeatureTrees() {
		Element result = queryServiceWithParameters(FEATURETREES,
				UNLIMITED_RESULTS);
		return processElementList(result);
	}

	public Element getFeatureNode(UUID featureNodeUuid) {
		Element result = queryService(featureNodeUuid, FEATURENODE);
		return result;
	}

	public Element getFeatureForFeatureNode(UUID featureNodeUuid) {
		Element result = queryService(featureNodeUuid, FEATURENODE_FEATURE);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonFromTaxonNode(org
	 * .jdom.Element)
	 */
	public Element getTaxonForTaxonNode(Element taxonNodeElement) {
		return queryService(taxonNodeElement, TAXONNODE_TAXON);
	}

	/*
	 * 
	 */
	/*
	 * public List<Element> getReferences(Element taxonElement) throws
	 * JDOMException {
	 * 
	 * //get the references from the taxonElement String referencePattern =
	 * "//name/nomenclaturalReference";
	 * 
	 * //but there could be many references Element referenceElement = (Element)
	 * XPath.selectSingleNode(taxonElement, referencePattern); //List<Element>
	 * descriptionElementElements = XPath.selectNodes(context, featurePattern +
	 * "/..");
	 * 
	 * List<Element> elementList = null;
	 * 
	 * if(referenceElement != null){ //the referencePattern was found in the
	 * taxonElement
	 * 
	 * Element result = queryService(referenceElement, REFERENCES);
	 * 
	 * elementList = new ArrayList<Element>();
	 * 
	 * for(Object child : result.getChildren()){ if(child instanceof Element){
	 * Element childElement = (Element) ((Element)child).clone();
	 * 
	 * childElement.detach();
	 * 
	 * elementList.add(childElement); } } }
	 * 
	 * return elementList; }
	 */

	public List<Element> getReferences(Element referenceElement) {

		Element result = queryService(referenceElement, REFERENCES);

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
	 * eu.etaxonomy.printpublisher.IXMLEntityFactory#getAcceptedTaxonElement
	 * (org.jdom.Element)
	 */
	public Element getAcceptedTaxonElement(Element taxonElement) {
		Element result = queryService(taxonElement, TAXON_ACCEPTED);
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
		Element result = queryService(taxonElement, TAXON_SYNONYMY);

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
	 * 
	 */
	public List<Element> getMedia(Element taxonElement) {

		List<NameValuePair> params = Arrays
				.asList(new NameValuePair[] {
						new BasicNameValuePair("includeTaxonDescriptions",
								"true"),
						new BasicNameValuePair("includeOccurrences", "false"),
						new BasicNameValuePair("includeTaxonNameDescriptions",
								"false") });

		UUID uuid = XMLHelper.getUuid(taxonElement);

		Element result = queryServiceWithParameters(
				TAXON_MEDIA.replace(UUID, uuid.toString()), params);

		// Element result = queryService(taxonElement, TAXON_MEDIA);

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
		Element result = queryService(nameElement, NAME_TYPE_DESIGNATIONS);
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
		Element result = queryService(taxonElement, TAXON_DESCRIPTIONS);
		return result;
	}

	/**
	 * Queries the service with the uuid of the given element
	 * 
	 * @param element
	 * @param restRequest
	 * @return
	 */
	private Element queryService(Element element, String restRequest) {
		UUID uuid = XMLHelper.getUuid(element);
		return queryService(uuid, restRequest);
	}

	/**
	 * Queries the service with the given uuid
	 * 
	 * @param uuid
	 * @param restRequest
	 * @return
	 */
	private Element queryService(UUID uuid, String restRequest) {
		String request = restRequest.replace(UUID, uuid.toString());
		return queryServiceWithParameters(request, null);
	}

	/**
	 * 
	 * @param restRequest
	 * @return
	 * @throws URISyntaxException
	 */
	private Element queryServiceWithParameters(String restRequest,
			List<NameValuePair> queryParameters) {

		try {
			URI newUri = UriUtils.createUri(serviceUrl, restRequest,
					queryParameters, null);

			Map<String, String> requestHeaders = new HashMap<String, String>();
			requestHeaders.put("Accept", "application/xml");
			// requestHeaders.put("Accept", "application/json");
			requestHeaders.put("Accept-Charset", "UTF-8");

			HttpResponse response = UriUtils
					.getResponse(newUri, requestHeaders);

			logger.info("Firing request for URI: " + newUri);

			if (UriUtils.isOk(response)) {

				// get the content at the resource
				InputStream content = UriUtils.getContent(response);

				// specify encoding in the reader
				BufferedReader in = new BufferedReader(new InputStreamReader(
						content, "UTF-8"));

				// build the jdom document
				Document document = builder.build(in);
				// Document document = builder.build(content);

				return document.getRootElement();
			} else {
				monitor.warning(UriUtils.getStatus(response));
				logger.error(UriUtils.getStatus(response));
			}

		} catch (IOException e) {
			monitor.warning("No content for request: " + restRequest, e);
			logger.error("No content for request: " + restRequest);
		} catch (JDOMException e) {
			monitor.warning("Error building the document.", e);
			logger.error("Error building the document.", e);
		} catch (URISyntaxException e) {
			monitor.warning("Error building URI", e);
			logger.error("Error building URI", e);
		}

		// error
		return null;
	}

	/**
	 * The access point of a CDM Community Server
	 * 
	 * @return the serviceUrl
	 */
	public URL getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * The CDM Community Servers access point
	 * 
	 * @param serviceUrl
	 */
	public void setServiceUrl(URL serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	// dto/polytomousKey/linkedStyle.json?findByTaxonomicScope=f820f533-06f2-4116-87e9-c9319c0c1cbf
	/*
	 * (non-Javadoc) TODO: Implement this method.
	 * 
	 * @see
	 * eu.etaxonomy.cdm.print.IXMLEntityFactory#getPolytomousKey(org.jdom.Element
	 * )
	 */
	@Override
	// public List<Element> getPolytomousKey(Element taxonElement) {
	public Element getPolytomousKey(Element taxonElement) {

		Element result = queryService(taxonElement, POLYTOMOUS_KEY);
		return result;
		// TODO Auto-generated method stub
		// return null;
	}
}
