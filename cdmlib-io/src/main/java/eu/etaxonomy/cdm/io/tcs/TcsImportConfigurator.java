 package eu.etaxonomy.cdm.io.tcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class TcsImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {
	private static Logger logger = Logger.getLogger(TcsImportConfigurator.class);
	
	
	public static TcsImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new TcsImportConfigurator(url, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private TcsImportConfigurator(String url, ICdmDataSource destination) {
		super();
		setSource(url);
		setDestination(destination);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#getSource()
	 */
	public String getSource() {
		return (String)super.getSource();
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcs.IImportConfigurator#setSource(java.lang.String)
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
	
	
	
}
