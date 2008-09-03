 package eu.etaxonomy.cdm.io.tcs;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class TcsImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(TcsImportConfigurator.class);
	
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			TcsReferenceIO.class
			, TcsTaxonNameIO.class
			, TcsTaxonNameRelationsIO.class
			, TcsTaxonIO.class
			, TcsTaxonRelationsIO.class
		};
	};
	
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
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = Database.NewInstance();
			sourceReference.setTitleCache("XXX");
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
	
	
	
}
