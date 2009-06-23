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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
public class TaxonXImportConfigurator extends ImportConfiguratorBase<TaxonXImportState> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(TaxonXImportConfigurator.class);
	
	private boolean findTaxonByName = false;
	private String originalSourceTaxonNamespace = "TaxonConcept";
	private String originalSourceId;
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				TaxonXDescriptionImport.class
				, TaxonXNomenclatureImport.class
//				, new TaxonXDescriptionImport(config.isDoFacts())

		};
	}
	
	/**
	 * @param url
	 * @param destination
	 * @return
	 */
	public static TaxonXImportConfigurator NewInstance(String url, ICdmDataSource destination){
		return new TaxonXImportConfigurator(url, destination);
	}


	/**
	 * @param url
	 * @param destination
	 */
	private TaxonXImportConfigurator(String url, ICdmDataSource destination) {
		super();
		setSource(url);
		setDestination(destination);
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public TaxonXImportState getNewState() {
		return new TaxonXImportState(this);
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
	
	public Element getSourceRoot(){
		String source = getSource();
		try {
			URL url;
			url = new URL(source);
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Element root = XmlHelp.getRoot(is);
			return root;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		logger.warn("getSource Reference not yet implemented");
		ReferenceBase result = Database.NewInstance();
		result.setTitleCache("XXX");
		return result;
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
	
}
