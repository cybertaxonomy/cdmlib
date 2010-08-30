/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.floraMalesiana;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.eflora.EfloraImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.EfloraTaxonImport;

@Component
public class FloraMalesianaImportConfigurator extends EfloraImportConfigurator  {
	private static final Logger logger = Logger.getLogger(FloraMalesianaImportConfigurator.class);
	
	public static FloraMalesianaImportConfigurator NewInstance(String url, ICdmDataSource destination){
		return new FloraMalesianaImportConfigurator(url, destination);
	}
	
	//TODO
	private static IInputTransformer defaultTransformer = null;
	private String classificationTitle = "Flora Malesiana";
	private String sourceReferenceTitle = "Flora Malesiana";
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			EfloraTaxonImport.class
		};
	};
	

	private FloraMalesianaImportConfigurator() {
		super();
	}	
	
	/**
	 * @param url
	 * @param destination
	 */
	private FloraMalesianaImportConfigurator(String url, ICdmDataSource destination) {
		super(url, destination, defaultTransformer);
		this.setClassificationTitle(classificationTitle);
		this.setSourceReferenceTitle(sourceReferenceTitle);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public FloraMalesianaImportState getNewState() {
		return new FloraMalesianaImportState(this);
	}


	
	
}
