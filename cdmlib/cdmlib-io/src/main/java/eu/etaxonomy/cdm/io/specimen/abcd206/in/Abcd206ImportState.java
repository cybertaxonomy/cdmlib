// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class Abcd206ImportState extends ImportStateBase<Abcd206ImportConfigurator, CdmImportBase<Abcd206ImportConfigurator,Abcd206ImportState>>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Abcd206ImportState.class);

	private TransactionStatus tx;
	
	private ICdmApplicationConfiguration cdmRepository; 
	
//****************** CONSTRUCTOR ***************************************************/	
	
	public Abcd206ImportState(Abcd206ImportConfigurator config) {
		super(config);
	}

//************************ GETTER / SETTER *****************************************/
	
	public TransactionStatus getTx() {
		return tx;
	}

	public void setTx(TransactionStatus tx) {
		this.tx = tx;
	}

	public ICdmApplicationConfiguration getCdmRepository() {
		return cdmRepository;
	}

	public void setCdmRepository(ICdmApplicationConfiguration cdmRepository) {
		this.cdmRepository = cdmRepository;
	}

}
