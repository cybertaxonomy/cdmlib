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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;

/**
 * This class holds the complete configuration for the print publishing process.
 * All aspects of the process are defined here.
 * 
 * @author n.hoffmann
 * @created Aug 3, 2010
 * @version 1.0
 */
public class PublishConfigurator implements Serializable {

	private static final Logger logger = Logger
			.getLogger(PublishConfigurator.class);

	private static final long serialVersionUID = 4896190792717383839L;

	private ICdmRepository applicationConfiguration;

	private URL webserviceUrl;

	private List<Element> selectedTaxonNodeElements = new ArrayList<Element>();

	private boolean doSynonymy = true;

	private boolean doDescriptions = true;
	
	private boolean doPolytomousKey = true;

	private UUID featureTreeUuid;

	private boolean doImages = true;

	private boolean doPublishEntireBranches = true;

	private File exportFolder;

	private List<IPublishOutputModule> outputModules;

	private boolean remote;

	private IProgressMonitor progressMonitor;

	/**
	 * Hidden default constructor
	 */
	private PublishConfigurator() {
	}

	/**
	 * Creates a new instance connected to the given application controller.
	 * 
	 * @param applicationConfiguration
	 * @return
	 */
	public static PublishConfigurator NewLocalInstance(
			ICdmRepository applicationConfiguration) {
		PublishConfigurator configurator = new PublishConfigurator();
		configurator.setLocal();
		configurator.setApplicationConfiguration(applicationConfiguration);
		logger.trace("New local publish configurator instantiated.");
		return configurator;
	}

	/**
	 * Creates a new instance, ready to be connected to a CDM Community Stores
	 * access point.
	 * 
	 * @return
	 */
	public static PublishConfigurator NewRemoteInstance() {
		PublishConfigurator configurator = new PublishConfigurator();
		configurator.setRemote();
		logger.trace("New remote publish configurator instantiated.");
		return configurator;
	}

	/**
	 * Returns the CDM Community Stores access point connected to this
	 * configuration.
	 * 
	 * @return the access points url or null if this configurator is configured
	 *         to be local.
	 */
	public URL getWebserviceUrl() {
		return webserviceUrl;
	}

	/**
	 * @see {@link #getWebserviceUrl()}
	 * @param webserviceUrlString
	 * @throws MalformedURLException
	 */
	public void setWebserviceUrl(String webserviceUrlString)
			throws MalformedURLException {
		URL url = new URL(webserviceUrlString);
		setWebserviceUrl(url);
	}

	/**
	 * @see {@link #getWebserviceUrl()}
	 * @param webserviceUrl
	 */
	public void setWebserviceUrl(URL webserviceUrl) {
		this.webserviceUrl = webserviceUrl;
	}

	/**
	 * Returns a list of taxon node elements that should be processed by the
	 * print publisher.
	 * 
	 * @return a list of elements
	 */
	public List<Element> getSelectedTaxonNodeElements() {
		return selectedTaxonNodeElements;
	}

	/**
	 * Set the list of taxon node elements that should be processed by the print
	 * publisher.
	 * 
	 * @see {@link #getSelectedTaxonNodeElements()}
	 * @param selectedTaxonNodeElements
	 */
	public void setSelectedTaxonNodeElements(
			List<Element> selectedTaxonNodeElements) {
		this.selectedTaxonNodeElements = selectedTaxonNodeElements;
	}

	/**
	 * Add a taxon node element to list of taxon nodes that will be processed by
	 * the print publisher.
	 * 
	 * @see {@link #getSelectedTaxonNodeElements()}
	 * @param selectedTaxonNodeElement
	 */
	public void addSelectedTaxonNodeElements(Element selectedTaxonNodeElement) {
		this.selectedTaxonNodeElements.add(selectedTaxonNodeElement);
	}

	/**
	 * Whether to export descriptions.
	 * 
	 * @return
	 */
	public boolean isDoDescriptions() {
		return doDescriptions;
	}

	/**
	 * @see {@link #isDoDescriptions()}
	 * @param doDescriptions
	 */
	public void setDoDescriptions(boolean doDescriptions) {
		this.doDescriptions = doDescriptions;
	}

	/**
	 * Whether to export images
	 * 
	 * @return
	 */
	public boolean isDoImages() {
		return doImages;
	}

	/**
	 * @see {@link #isDoImages()}
	 * @param doImages
	 */
	public void setDoImages(boolean doImages) {
		this.doImages = doImages;
	}

	/**
	 * The folder, the produced output will be written to.
	 * 
	 * @return the exportFile
	 */
	public File getExportFolder() {
		return exportFolder;
	}

	/**
	 * @see {@link #getExportFolder()}
	 * @param exportFolder
	 *            the exportFile to set
	 */
	public void setExportFolder(File exportFolder) {
		if (!exportFolder.isDirectory()) {
			throw new IllegalArgumentException(
					"Given export folder is not a directory.");
		}
		if (!exportFolder.canWrite()) {
			throw new IllegalArgumentException(
					"Can not write to given export folder");
		}
		this.exportFolder = exportFolder;
	}

	/**
	 * Returns a list of output modules. The print publisher will export into
	 * the formats defined by these output modules
	 * 
	 * @see {@link IPublishOutputModule} and implementations thereof
	 * @return the outputModules
	 */
	public List<IPublishOutputModule> getOutputModules() {
		if (outputModules == null) {
			outputModules = new ArrayList<IPublishOutputModule>();
		}
		return outputModules;
	}

	/**
	 * @see {@link #getOutputModules()}
	 * @param outputModules
	 *            the outputModules to set
	 */
	public void setOutputModules(List<IPublishOutputModule> outputModules) {
		if (outputModules == null) {
			throw new IllegalArgumentException(
					"List of output modules may not be null.");
		}
		this.outputModules = outputModules;
	}

	/**
	 * Adds an output modules to this configurators list of output modules.
	 * 
	 * @see {@link IPublishOutputModule} and implementations thereof
	 * @param module
	 */
	public void addOutputModule(IPublishOutputModule module) {
		getOutputModules().add(module);
	}

	/**
	 * Whether this configurator is connected to a remote CDM Community Store
	 * 
	 * @return
	 */
	public boolean isRemote() {
		return remote;
	}

	/**
	 * @see {@link #isRemote()}
	 */
	private void setRemote() {
		this.remote = true;
	}

	/**
	 * Whether this configurator is connected to a local application controller.
	 * 
	 * @return
	 */
	public boolean isLocal() {
		return !remote;
	}

	/**
	 * @see {@link #isLocal()}
	 */
	private void setLocal() {
		this.remote = false;
	}

	/**
	 * Returns a {@link RemoteXMLEntityFactory} if <code>this</code> is a remote
	 * instance or a {@link LocalXMLEntityFactory} if <code>this</code> is a
	 * local instance
	 * 
	 * @return an {@link IXMLEntityFactory}
	 */
	public IXMLEntityFactory getFactory() {
		return isRemote() ? new RemoteXMLEntityFactory(getWebserviceUrl(),
				getProgressMonitor()) : new LocalXMLEntityFactory(
				getApplicationConfiguration(), getProgressMonitor());
	}

	/**
	 * FIXME this is a dummy implementation
	 * 
	 * @return
	 */
	public int calculateNumberOfNodes() {
		int count = 100;

		return count;
	}

	/**
	 * Whether taxonomically included taxa for the
	 * {@linkplain #getSelectedTaxonNodeElements() selected taxon nodes} should
	 * be exported recursively.
	 * 
	 * @return <code>true</code> if this is desired
	 */
	public boolean isDoPublishEntireBranches() {
		return doPublishEntireBranches;
	}

	/**
	 * @see {@link #isDoPublishEntireBranches()}
	 * @param doPublishEntireBranches
	 */
	public void setDoPublishEntireBranches(boolean doPublishEntireBranches) {
		this.doPublishEntireBranches = doPublishEntireBranches;
	}

	/**
	 * Whether the synonymy should be exported.
	 * 
	 * @return <code>true</code> if this is desired
	 */
	public boolean isDoSynonymy() {
		return doSynonymy;
	}

	/**
	 * @see {@link #isDoSynonymy()}
	 * @param selection
	 */
	public void setDoSynonymy(boolean selection) {
		this.doSynonymy = selection;
	}

	/**
	 * The {@linkplain CdmApplicationController application controller}
	 * associated with this instance
	 * 
	 * @return the {@link CdmApplicationController} or null if <code>this</code>
	 *         is a {@linkplain #isRemote() remote} instance
	 */
	public ICdmRepository getApplicationConfiguration() {
		return applicationConfiguration;
	}

	/**
	 * @see {@link #getApplicationController()}
	 * @param applicationConfiguration
	 */
	private void setApplicationConfiguration(
			ICdmRepository applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	/**
	 * The feature tree configures which features and in which order and nesting
	 * will be exported by the application
	 * 
	 * @return the featureTrees uuid
	 */
	public UUID getFeatureTreeUuid() {
		return featureTreeUuid;
	}

	/**
	 * @see {@link #getFeatureTreeUuid()}
	 * @param featureTreeUuid
	 *            the featureTree to set
	 */
	public void setFeatureTree(UUID featureTreeUuid) {
		this.featureTreeUuid = featureTreeUuid;
	}

	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor != null ? progressMonitor
				: new NullProgressMonitor();
	}

	/**
	 * @return the doPolytomousKey
	 */
	public boolean isDoPolytomousKey() {
		return doPolytomousKey;
	}

	/**
	 * @param doPolytomousKey the doPolytomousKey to set
	 */
	public void setDoPolytomousKey(boolean doPolytomousKey) {
		this.doPolytomousKey = doPolytomousKey;
	}

}
