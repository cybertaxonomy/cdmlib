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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author n.hoffmann
 * @created 11.11.2008
 * @version 1.0
 */
public class ImageImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {
	private static Logger logger = Logger.getLogger(ImageImportConfigurator.class);

	public static ImageImportConfigurator NewInstance(File source, ICdmDataSource destination){
		return new ImageImportConfigurator(source, destination);		
	}
	
	private ImageImportConfigurator(File source, ICdmDataSource destination){
		super();
		setSource(source);
		setDestination(destination);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
//	@Override
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
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	@Override
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				AbstractImageImporter.class
		};
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public String getSource() {
		return super.getSource().toString();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		return "Image file " + getSource();
	}
}
