/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ericaceae;

import java.net.URI;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.eflora.EfloraImportConfigurator;

@Component
public class CentralAfricaEricaceaeImportConfigurator extends EfloraImportConfigurator  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CentralAfricaEricaceaeImportConfigurator.class);
	
	public static CentralAfricaEricaceaeImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new CentralAfricaEricaceaeImportConfigurator(uri, destination);
	}
	
	private static IInputTransformer defaultTransformer = new CentralAfricaEricaceaeTransformer();
	private String classificationTitle = "Flore d'Afrique Centrale - Ericaceae";
	private String sourceReferenceTitle = "Flore d'Afrique Centrale - Ericaceae";
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			CentralAfricaEricaceaeTaxonImport.class
		};
	};
	

	private CentralAfricaEricaceaeImportConfigurator() {
		super();
	}
	
	/**
	 * @param url
	 * @param destination
	 */
	private CentralAfricaEricaceaeImportConfigurator(URI uri, ICdmDataSource destination) {
		super(uri, destination, defaultTransformer);
		this.setClassificationTitle(classificationTitle);
		this.setSourceReferenceTitle(sourceReferenceTitle);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public CentralAfricaEricaceaeImportState getNewState() {
		return new CentralAfricaEricaceaeImportState(this);
	}

	
	
}
