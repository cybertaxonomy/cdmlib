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
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 29.05.2008
 */
public class TcsRdfImportConfigurator extends ImportConfiguratorBase<TcsRdfImportState, URI> implements IImportConfigurator {

    private static final long serialVersionUID = -8987364078779275820L;


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
	Map<String, String> nsPrefixMap;
	/*//rdfNamespace
	private Namespace rdfNamespace;
	//Team namespace
	private Namespace tmNamespace;
	//Person namespace
	private Namespace personNamespace;
	//TaxonName namespace
	private Namespace tnNamespace;
	//TaxonConcept namespace
	private Namespace tcNamespace;
	//TDWG common namespace
	private Namespace commonNamespace;
	//TDWG geoNamespace
	private Namespace geoNamespace;
	//publicationNamespace
	private Namespace publicationNamespace;
	//owlNamespace
	private Namespace owlNamespace;
	//dcNamespace
	private Namespace dcNamespace;
	//dcTermsNamespace
	private Namespace dcTermsNamespace;
	//palmNamespace
	private Namespace palmNamespace;

//TODO
	protected static Namespace nsTcom = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/Common#");
	protected static Namespace nsTn = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonName#");
	protected static Namespace nsTgeo = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/GeographicRegion#");
	protected static Namespace nsTc = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/TaxonConcept#");
	protected static Namespace nsTpub = Namespace.getNamespace("http://rs.tdwg.org/ontology/voc/PublicationCitation#");
	protected static Namespace nsTpalm = Namespace.getNamespace("http://wp5.e-taxonomy.eu/import/palmae/common");

	String[] prefixArray = {"rdf", "tm", "p","tc","tcom", "tgeo","owl","dc","dcterms","tn"};
*/
	@Override
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
	public Model getSourceRoot(){
		URI source = getSource();
		try {
			URL url;
			url = source.toURL();
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Model model = ModelFactory.createDefaultModel();
			model.read(is, null);
			makeNamespaces(model);
			return model;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
public Element getSourceRoot(InputStream is){

		try {
			Element root = XmlHelp.getRoot(is);
			makeNamespaces(root);
			return root;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			logger.warn("The InputStream does not contain an rdf file.");
			logger.warn(e.getMessage());
		}
		return null;
	}


	private void makeNamespaces(Element root){

		String prefix = "rdf";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
		prefix = "tc";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
		prefix = "tn";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
		prefix = "tcom";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
		prefix = "tgeo";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
		prefix = "tpub";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());

		prefix = "tpalm";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
		prefix = "tm";
		nsPrefixMap.put(prefix, root.getNamespace().getURI().toString());
	}
	public void makeNamespaces(Model model){
		nsPrefixMap = model.getNsPrefixMap();

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
	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}

	public String getRdfNamespaceURIString() {
		return nsPrefixMap.get("rdf");
	}

	public String getTeamNamespaceURIString() {
		return nsPrefixMap.get("tm");
	}

	public void setNamespace(Namespace namespace) {
		this.nsPrefixMap.put(namespace.getPrefix(), namespace.getURI());
	}

	public String getTcNamespaceURIString() {
		return nsPrefixMap.get("tc");
	}



	public String getTnNamespaceURIString() {
		return nsPrefixMap.get("tn");
	}



	public String getCommonNamespaceURIString() {
		return nsPrefixMap.get("tcom");
	}



	public String getGeoNamespaceURIString() {
		return nsPrefixMap.get("tgeo");
	}



	public String getPublicationNamespaceURI() {
		return nsPrefixMap.get("tpub");
	}


	/**
	 * @return the palmNamespace
	 */
	public String getPalmNamespaceURIString() {
		return nsPrefixMap.get("tpalm");
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
	@Override
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
