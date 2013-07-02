/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.wp6;

import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public class CommonNameImportConfigurator extends ExcelImportConfiguratorBase implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CommonNameImportConfigurator.class);

	private String referenceTitle = "Common Name Excel Import";
	
	
	private static IInputTransformer defaultTransformer = new CommonNamesTransformer();
	
	public static CommonNameImportConfigurator NewInstance(URI source, ICdmDataSource destination){
		return new CommonNameImportConfigurator(source, destination);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#makeIoClassList()
	 */
	@Override
	protected void makeIoClassList(){
		ioClassList = new Class[]{
				CommonNameExcelImport.class ,
		};	
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	@Override
	public ImportStateBase getNewState() {
		return new CichorieaeCommonNameImportState(this);
	}



	private CommonNameImportConfigurator(URI source, ICdmDataSource destination) {
	   super(source, destination, defaultTransformer);
	   setNomenclaturalCode(NomenclaturalCode.ICBN); 
	   setSource(source);
	   setDestination(destination);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	@Override
	public URI getSource() {
		return (URI)super.getSource();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#setSource(java.lang.Object)
	 */
	@Override
	public void setSource(URI source) {
		super.setSource(source);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		if (sourceReference == null){
			sourceReference =  ReferenceFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(referenceTitle, true);
			}
		}
		return sourceReference;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceNameString()
	 */
	@Override
	public String getSourceNameString() {
		return getSource().toString();
	}
}
