/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.reference.endnote.in;

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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

@Component
public class EndnoteImportConfigurator extends ImportConfiguratorBase<EndnoteImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(EndnoteImportConfigurator.class);
	
	public static EndnoteImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new EndnoteImportConfigurator(uri, destination);
	}
	
	private boolean doRecords = true;
//	private boolean doSpecimen = true;

	private Method functionRecordsDetailed = null; 
	private IEndnotePlaceholderClass placeholderClass;
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	//	rdfNamespace
	Namespace EndnoteNamespace;
/*TODO: wieder zurück!!!
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			EndnoteRecordsImport.class
		};
	};
*/
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private EndnoteImportConfigurator() {
		super(defaultTransformer);
//		setSource(url);
//		setDestination(destination);
	}
	
	/**
	 * @param url
	 * @param destination
	 */
	private EndnoteImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public EndnoteImportState getNewState() {
		return new EndnoteImportState(this);
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
		EndnoteNamespace = root.getNamespace();
		if (EndnoteNamespace == null){
			logger.warn("At least one Namespace is NULL");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			sourceReference = refFactory.newDatabase();
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
	
	public Namespace getEndnoteNamespace() {
		return EndnoteNamespace;
	}

	public void setEndnoteNamespace(Namespace EndnoteNamespace) {
		this.EndnoteNamespace = EndnoteNamespace;
	}
	

	/**
	 * @return the funMetaDataDetailed
	 */
	public Method getFunctionRecordsDetailed() {
		if (functionRecordsDetailed == null){
			//TODO!!!
		//	functionRecordsDetailed = getDefaultFunction(EndnoteRecordsImport.class, "defaultRecordsDetailedFunction");
		}
		return functionRecordsDetailed;
		
	}

	/**
	 * @param funMetaDataDetailed the funMetaDataDetailed to set
	 */
	public void setFunctionRecordsDetailed(Method functionRecordsDetailed) {
		this.functionRecordsDetailed = functionRecordsDetailed;
	}
	
	/**
	 * @return the doMetaData
	 */
	public boolean isDoRecords() {
		return doRecords;
	}

	/**
	 * @param doMetaData the doMetaData to set
	 */
	public void setDoRecords(boolean doRecords) {
		this.doRecords = doRecords;
	}

	/**
	 * @return the doSpecimen
	 */
//	public boolean isDoSpecimen() {
//		return doSpecimen;
//	}

	/**
	 * @param doSpecimen the doSpecimen to set
	 */
//	public void setDoSpecimen(boolean doSpecimen) {
//		this.doSpecimen = doSpecimen;
//	}

	/**
	 * @return the placeholderClass
	 */
	public IEndnotePlaceholderClass getPlaceholderClass() {
		if (placeholderClass == null){
			placeholderClass = new IEndnotePlaceholderClass();
		}
		return placeholderClass;
	}

	/**
	 * @param placeholderClass the placeholderClass to set
	 */
	public void setPlaceholderClass(IEndnotePlaceholderClass placeholderClass) {
		this.placeholderClass = placeholderClass;
	}

	@Override
	protected void makeIoClassList() {
		// TODO Auto-generated method stub
		
	}

	
}
