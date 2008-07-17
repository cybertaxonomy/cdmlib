 package eu.etaxonomy.cdm.io.taxonx;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class TaxonXImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(TaxonXImportConfigurator.class);
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				TaxonXDescriptionIO.class
//				, new TaxonXDescriptionIO(config.isDoFacts())

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


	
}
