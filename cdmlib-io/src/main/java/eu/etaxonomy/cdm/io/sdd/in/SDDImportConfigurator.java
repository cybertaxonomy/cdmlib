/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.in;

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
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.common.XmlImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.sdd.SDDTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author h.fradin
 * @created 24.10.2008
 */
public class SDDImportConfigurator extends XmlImportConfiguratorBase<SDDImportState> implements IImportConfigurator, IMatchingImportConfigurator {
    private static final long serialVersionUID = -960998183005112130L;

    private static final Logger logger = Logger.getLogger(SDDImportConfigurator.class);

	//TODO
	private static IInputTransformer defaultTransformer = new SDDTransformer();

	private boolean doMatchTaxa = true;

	//xml xmlNamespace
	Namespace sddNamespace;

	@Override
    protected void makeIoClassList(){
		ioClassList = new Class[]{
				SDDImport.class
		};
	};

	public static SDDImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new SDDImportConfigurator(uri, destination);
	}


	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private SDDImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}


	@Override
    public SDDImportState getNewState() {
		return new SDDImportState(this);
	}


	/**
	 * @return
	 */
	public Element getSourceRoot(){
		try {
			URL url;
			url = getSource().toURL();
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

	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
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
	@Override
    public void setReuseExistingTaxaWhenPossible(boolean doMatchTaxa) {
		this.doMatchTaxa = doMatchTaxa;
	}

	/**
	 * @return the doMatchTaxa
	 */
	@Override
    public boolean isReuseExistingTaxaWhenPossible() {
		return doMatchTaxa;
	}
}
