/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.redlist.bfnXml;

/**
 * @author a.oppermann
 * @date 03.07.2013
 *
 */
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.tcsxml.DefaultTcsXmlPlaceholders;
import eu.etaxonomy.cdm.io.tcsxml.ITcsXmlPlaceholderClass;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

@Component
public class BfnXmlImportConfigurator extends ImportConfiguratorBase<BfnXmlImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(BfnXmlImportConfigurator.class);
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	private boolean doMetaData = true;
	private boolean doSpecimen = true;
	
//	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
//	//names
	private boolean doTaxonNames = true;
	private boolean doRelNames = true;
//	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;

	
	
	private Method functionMetaDataDetailed = null; 
	private ITcsXmlPlaceholderClass placeholderClass;
	
	//	rdfNamespace
	Namespace bfnXmlNamespace;

	protected static Namespace nsTcsXml = Namespace.getNamespace("http://www.tdwg.org/schemas/tcs/1.01");
	
//	@Autowired
//	TcsXmlMetaDataImport tcsXmlMetaDataImport;
//	@Autowired
//	TcsXmlSpecimensImport tcsXmlSpecimensIO;
//	@Autowired
//	TcsXmlPublicationsImport tcsXmlPublicationsIO;
//	@Autowired
//	BfnXmlTaxonNameImport tcsXmlTaxonNameIO;
//	@Autowired
//	TcsXmlTaxonNameRelationsImport tcsXmlTaxonNameRelationsIO;
//	@Autowired
//	BfnXmlTaxonImport tcsXmlTaxonIO;
//	@Autowired
//	TcsXmlTaxonRelationsImport tcsXmlTaxonRelationsIO;
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList(){
//		ioBeans = new String[]{
//				"tcsXmlMetaDataImport"
//				, "tcsXmlSpecimensIO"
//				, "tcsXmlPublicationsIO"
//				, "tcsXmlTaxonNameIO"
//				, "tcsXmlTaxonNameRelationsIO"
//				, "tcsXmlTaxonIO"
//				, "tcsXmlTaxonRelationsIO"	
//		};
		
		ioClassList = new Class[]{
				BfnXmlImportFeature.class,
				BfnXmlImportTaxonName.class
		};
	};
	
	public static BfnXmlImportConfigurator NewInstance(URI uri,
			ICdmDataSource destination){
		return new BfnXmlImportConfigurator(uri, destination);
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BfnXmlImportConfigurator() {
		super(defaultTransformer);
//		setSource(url);
//		setDestination(destination);
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BfnXmlImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public BfnXmlImportState getNewState() {
		return new BfnXmlImportState(this);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public URI getSource() {
		return (URI)super.getSource();
	}
	
	/**
	 * @param file
	 */
	public void setSource(URI uri) {
		super.setSource(uri);
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
		bfnXmlNamespace = root.getNamespace();
		if (bfnXmlNamespace == null 
				/**|| tcNamespace == null 
				 * || tnNamespace == null 
				 * || commonNamespace == null 
				 * ||	geoNamespace == null 
				 * || publicationNamespace == null*/){
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
			sourceReference.setTitleCache("Rote Liste Band 6 (2011), Pilze (Teil2) - Flechten und Myxomyzeten", true);
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
	
	public Namespace getBfnXmlNamespace() {
		return bfnXmlNamespace;
	}

	public void setBfnXmlNamespace(Namespace bfnXmlNamespace) {
		this.bfnXmlNamespace = bfnXmlNamespace;
	}

	/**
	 * @param funMetaDataDetailed the funMetaDataDetailed to set
	 */
	public void setFunctionMetaDataDetailed(Method functionMetaDataDetailed) {
		this.functionMetaDataDetailed = functionMetaDataDetailed;
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
	
	/**
	 * @return the doMetaData
	 */
	public boolean isDoMetaData() {
		return doMetaData;
	}

	/**
	 * @param doMetaData the doMetaData to set
	 */
	public void setDoMetaData(boolean doMetaData) {
		this.doMetaData = doMetaData;
	}


	/**
	 * @return the doSpecimen
	 */
	public boolean isDoSpecimen() {
		return doSpecimen;
	}

	/**
	 * @param doSpecimen the doSpecimen to set
	 */
	public void setDoSpecimen(boolean doSpecimen) {
		this.doSpecimen = doSpecimen;
	}

	/**
	 * @return the placeholderClass
	 */
	public ITcsXmlPlaceholderClass getPlaceholderClass() {
		if (placeholderClass == null){
			placeholderClass = new DefaultTcsXmlPlaceholders();
		}
		return placeholderClass;
	}

	/**
	 * @param placeholderClass the placeholderClass to set
	 */
	public void setPlaceholderClass(ITcsXmlPlaceholderClass placeholderClass) {
		this.placeholderClass = placeholderClass;
	}

	
}
