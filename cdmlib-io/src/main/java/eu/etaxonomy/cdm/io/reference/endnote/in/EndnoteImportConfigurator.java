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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

@Component
public class EndnoteImportConfigurator
            extends ImportConfiguratorBase<EndnoteImportState, URI> {

    private static final long serialVersionUID = 2763770696094215281L;
    private static final Logger logger = Logger.getLogger(EndnoteImportConfigurator.class);

    //TODO
    private static IInputTransformer defaultTransformer = null;


    //  rdfNamespace
    private Namespace endnoteNamespace;

// *********************** FACTORY ****************************/

	public static EndnoteImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new EndnoteImportConfigurator(uri, destination);
	}

// ******************** CONSTRUCTOR ************************************/

    private EndnoteImportConfigurator() {
        super(defaultTransformer);
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



	@Override
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			EndnoteRecordsImport.class
		};
	}

	@Override
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
		endnoteNamespace = root.getNamespace();
		if (endnoteNamespace == null){
			logger.warn("At least one Namespace is NULL");
		}
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

// ********************* GETTER / SETTER *****************************/

	public Namespace getEndnoteNamespace() {
		return endnoteNamespace;
	}

	public void setEndnoteNamespace(Namespace EndnoteNamespace) {
		this.endnoteNamespace = EndnoteNamespace;
	}

}
