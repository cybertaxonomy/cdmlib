/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author h.fradin
 * @created 24.10.2008
 * @version 1.0
 */
public class SDDImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator, IMatchingImportConfigurator {
	private static final Logger logger = Logger.getLogger(SDDImportConfigurator.class);

	//TODO
	private static IInputTransformer defaultTransformer = null;
	
	private boolean doMatchTaxa = false;
	
	//xml xmlNamespace
	Namespace sddNamespace;

	protected void makeIoClassList(){
		ioClassList = new Class[]{
				SDDDescriptionIO.class
		};
	};
	
	public static SDDImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new SDDImportConfigurator(url, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private SDDImportConfigurator(String url, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(url);
		setDestination(destination);
	}
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public SDDImportState getNewState() {
		return new SDDImportState(this);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public String getSource() {
		return (String)super.getSource();
	}

	/**
	 * @param file
	 */
	public void setSource(String file) {
		super.setSource(file);
	}

	/**
	 * @return
	 */
	public Element getSourceRoot(){
		String source = getSource();
		try {
			URL url;
			url = new URL(source);
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
		sddNamespace = root.getNamespace();
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
			return this.getSource();
		}
	}

	public Namespace getSddNamespace() {
		return sddNamespace;
	}

	public void setSddNamespace(Namespace xmlNamespace) {
		this.sddNamespace = xmlNamespace;
	}

	/**
	 * @param doMatchTaxa the doMatchTaxa to set
	 */
	public void setDoMatchTaxa(boolean doMatchTaxa) {
		this.doMatchTaxa = doMatchTaxa;
	}

	/**
	 * @return the doMatchTaxa
	 */
	public boolean isDoMatchTaxa() {
		return doMatchTaxa;
	}
}
