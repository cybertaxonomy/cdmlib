/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.images;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author n.hoffmann
 * @created 11.11.2008
 * @version 1.0
 */
public class ImageImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(ImageImportConfigurator.class);


	public static ImageImportConfigurator NewInstance(File source, ICdmDataSource destination, String mediaUrlString, Class<? extends AbstractImageImporter> importerClass){
		return new ImageImportConfigurator(source, destination, mediaUrlString, importerClass);		
	}
	
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	/**
	 * @param source
	 * @param destination
	 * @param importerClass
	 * @return
	 */
	 
	public static ImageImportConfigurator NewInstance(File source, ICdmDataSource destination, Class<? extends AbstractImageImporter> importerClass){
		return new ImageImportConfigurator(source, destination, null, importerClass);		
	}
	
	private ImageImportConfigurator(File source, ICdmDataSource destination, String mediaUrlString, Class<? extends AbstractImageImporter> importerClass){
		super(defaultTransformer);
		FileNotFoundException e;
		setSource(source);
		setDestination(destination);
		setMediaUrlString(mediaUrlString);
		ioClassList = new Class[] {importerClass};
	}
	
	private String mediaUrlString = null;
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
//	@Override
	public ReferenceBase getSourceReference() {
	//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			sourceReference = refFactory.newDatabase();
			sourceReference.setTitleCache("XXX");
		}
		return sourceReference;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	@Override
	//NOT used, component class is injected via constructor
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				AbstractImageImporter.class
		};
	}
	
	
	/**
	 * @return the mediaUrlString
	 */
	public String getMediaUrlString() {
		if(mediaUrlString == null){
			throw new NullPointerException("mediaUrlString has not been set");
		}
		return mediaUrlString;
	}

	/**
	 * @param mediaUrlString the mediaUrlString to set
	 */
	public void setMediaUrlString(String mediaUrlString) {
		this.mediaUrlString = mediaUrlString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	@Override
	public Object getSource() {
		return super.getSource();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		return "Image file " + getSource();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public ImageImportState getNewState() {
		return new ImageImportState(this);
	}
}
