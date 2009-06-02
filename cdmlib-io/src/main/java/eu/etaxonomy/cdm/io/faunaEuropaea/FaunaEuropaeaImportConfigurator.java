/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.faunaEuropaea;

import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 08.05.2009
 * @version 1.0
 */
public class FaunaEuropaeaImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(FaunaEuropaeaImportConfigurator.class);
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
//				FaunaEuropaeaAuthorImport.class,
//				FaunaEuropaeaReferenceImport.class,
//				FaunaEuropaeaTaxonNameImport.class
				FaunaEuropaeaTaxonImport.class,
//				FaunaEuropaeaDistributionImport.class
		};
	};
	
	public static FaunaEuropaeaImportConfigurator NewInstance(Source source, ICdmDataSource destination){
		return new FaunaEuropaeaImportConfigurator(source, destination);
}
	
	private FaunaEuropaeaImportConfigurator(Source source, ICdmDataSource destination) {
		setSource(source);
		setDestination(destination);
		setNomenclaturalCode(NomenclaturalCode.ICBN);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public Source getSource() {
		return (Source)super.getSource();
	}
	
	/**
	 * @param dbSource
	 */
	public void setSource(Source dbSource) {
		super.setSource(dbSource);
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
			sourceReference.setTitleCache("Fauna Europaea data import");
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null) {
			return null;
		}else{
			return this.getSource().toString();
		}
	}
	
}
