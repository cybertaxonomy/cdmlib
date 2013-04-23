/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 29.05.2008
 * @version 1.0
 */
public class TcsRdfImportConfigurator extends ImportConfiguratorBase<TcsRdfImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(TcsRdfImportConfigurator.class);
	
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	//if false references in this rdf file are not published in the bibliography list
	private boolean isPublishReferences = true;
	
//	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
//	//names
	private boolean doTaxonNames = true;
	private boolean doRelNames = true;
//	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;
	private boolean doFacts = true;
	
	//rdfNamespace
	private Namespace rdfNamespace;
	//TaxonConcept namespace
	private Namespace tcNamespace;
	//TaxonName namespace
	private Namespace tnNamespace;
	//TDWG common namespace
	private Namespace commonNamespace;
	//TDWG geoNamespace
	private Namespace geoNamespace;
	//publicationNamespace
	private Namespace publicationNamespace;
	//palmNamespace
	private Namespace palmNamespace;
	
//TODO	
	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");
	protected static Namespace nsTn = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonName#");
	protected static Namespace nsTgeo = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/GeographicRegion#");
	protected static Namespace nsTc = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonConcept#");
	protected static Namespace nsTpub = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/PublicationCitation#");
	protected static Namespace nsTpalm = Namespace.getNamespace("http://wp5.e-taxonomy.eu/import/palmae/common");

	protected void makeIoClassList(){
		ioClassList = new Class[]{
			TcsRdfReferenceImport.class
			, TcsRdfTaxonNameImport.class
			, TcsRdfTaxonNameRelationsImport.class
			, TcsRdfTaxonImport.class
			, TcsRdfTaxonRelationsImport.class
		};
	};
	
	public static TcsRdfImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new TcsRdfImportConfigurator(uri, destination);
	}
	
	//TODO for spring use only 
	private TcsRdfImportConfigurator(){
		super(defaultTransformer);
		
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private TcsRdfImportConfigurator(URI url, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(url);
		setDestination(destination);
	}

	/**
	 * @return
	 */
	public Element getSourceRoot(){
		URI source = getSource();
		try {
			URL url;
			url = source.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Element root = XmlHelp.getRoot(is);
			makeNamespaces(root);
			return root;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean makeNamespaces(Element root){
		//String strTnNamespace = "http://rs.tdwg.org/ontology/voc/TaxonName#";
		//Namespace taxonNameNamespace = Namespace.getNamespace("tn", strTnNamespace);

		String prefix;
		rdfNamespace = root.getNamespace();
		prefix = "tc";
		tcNamespace = root.getNamespace(prefix);
		prefix = "tn";
		tnNamespace = root.getNamespace(prefix);
		prefix = "tcom";
		commonNamespace = root.getNamespace(prefix);
		prefix = "tgeo";
		geoNamespace = root.getNamespace(prefix);
		prefix = "tpub";
		publicationNamespace = root.getNamespace(prefix);
		
		prefix = "tpalm";
		palmNamespace = root.getNamespace(prefix);
		
		if (rdfNamespace == null || tcNamespace == null || tnNamespace == null ||
				commonNamespace == null ||	geoNamespace == null || publicationNamespace == null 
				|| palmNamespace == null){
			logger.warn("At least one Namespace is NULL");
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			sourceReference.setTitleCache("XXX", true);
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}
	
	public Namespace getRdfNamespace() {
		return rdfNamespace;
	}

	public void setRdfNamespace(Namespace rdfNamespace) {
		this.rdfNamespace = rdfNamespace;
	}

	public Namespace getTcNamespace() {
		return tcNamespace;
	}

	public void setTcNamespace(Namespace tcNamespace) {
		this.tcNamespace = tcNamespace;
	}

	public Namespace getTnNamespace() {
		return tnNamespace;
	}

	public void setTnNamespace(Namespace tnNamespace) {
		this.tnNamespace = tnNamespace;
	}

	public Namespace getCommonNamespace() {
		return commonNamespace;
	}

	public void setCommonNamespace(Namespace commonNamespace) {
		this.commonNamespace = commonNamespace;
	}

	public Namespace getGeoNamespace() {
		return geoNamespace;
	}

	public void setGeoNamespace(Namespace geoNamespace) {
		this.geoNamespace = geoNamespace;
	}

	public Namespace getPublicationNamespace() {
		return publicationNamespace;
	}

	public void setPublicationNamespace(Namespace publicationNamespace) {
		this.publicationNamespace = publicationNamespace;
	}
	/**
	 * @return the palmNamespace
	 */
	public Namespace getPalmNamespace() {
		return palmNamespace;
	}

	/**
	 * @param palmNamespace the palmNamespace to set
	 */
	public void setPalmNamespace(Namespace palmNamespace) {
		this.palmNamespace = palmNamespace;
	}

	/**
	 * if false references in this rdf file are not published in the bibliography list
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
	
	
	public boolean isDoFacts() {
		return doFacts;
	}
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}
	
	/**
	 * Import name relationships yes/no?.
	 * @return
	 */
	public boolean isDoRelNames() {
		return doRelNames;
	}
	public void setDoRelNames(boolean doRelNames) {
		this.doRelNames = doRelNames;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public TcsRdfImportState getNewState() {
		return new TcsRdfImportState(this);
	}
	
	
	
	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}
	
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}

	

	
	
}
