/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.taxonx;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @since 29.07.2008
 * @version 1.0
 */
public class TaxonXImportConfigurator extends ImportConfiguratorBase<TaxonXImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(TaxonXImportConfigurator.class);

	//if true the information in the mods part (taxonxHeader)
	private boolean doMods = true;
	private boolean doFacts = true;
	private boolean doTypes = true;


	//TODO
	private static IInputTransformer defaultTransformer = null;


	//if false references in this rdf file are not published in the bibliography list
	private boolean isPublishReferences = true;


	private String originalSourceTaxonNamespace = "TaxonConcept";
	private String originalSourceId;

	@Override
    protected void makeIoClassList(){
		ioClassList = new Class[]{
				TaxonXModsImport.class,
				TaxonXDescriptionImport.class
				, TaxonXNomenclatureImport.class
//				, new TaxonXDescriptionImport(config.isDoFacts())

		};
	}

	/**
	 * @param uri
	 * @param destination
	 * @return
	 */
	public static TaxonXImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new TaxonXImportConfigurator(uri, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private TaxonXImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	@Override
    public TaxonXImportState getNewState() {
		return new TaxonXImportState(this);
	}

	public Element getSourceRoot(){
		URI source = getSource();
		try {
			URL url;
			url = source.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Element root = XmlHelp.getRoot(is);
			return root;
		} catch (Exception e) {
			logger.error("The source '" + source + "' has errors.", e);
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		logger.warn("getSource Reference not yet implemented");
		Reference result = ReferenceFactory.newDatabase();
		result.setTitleCache("XXX", true);
		return result;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}

	public String getOriginalSourceTaxonNamespace() {
		return originalSourceTaxonNamespace;
	}

	public void setOriginalSourceTaxonNamespace(String originalSourceTaxonNamespace) {
		this.originalSourceTaxonNamespace = originalSourceTaxonNamespace;
	}

	public String getOriginalSourceId() {
		return originalSourceId;
	}

	public void setOriginalSourceId(String originalSourceId) {
		this.originalSourceId = originalSourceId;
	}


	/**
	 * @return the doMods
	 */
	public boolean isDoMods() {
		return doMods;
	}

	/**
	 * @param doMods the doMods to set
	 */
	public void setDoMods(boolean doMods) {
		this.doMods = doMods;
	}


	public boolean isDoFacts() {
		return doFacts;
	}
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}



	public boolean isDoTypes() {
		return doTypes;
	}
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
	}


	/**
	 * @return the isPublishReferences
	 */
	public boolean isPublishReferences() {
		return isPublishReferences;
	}

	/**
	 * @param isPublishReferences the isPublishReferences to set
	 */
	public void setPublishReferences(boolean isPublishReferences) {
		this.isPublishReferences = isPublishReferences;
	}



}
