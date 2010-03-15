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
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @created 22.09.2009
 * @version 1.0
 */
public class CdmImportConfigurator extends ImportConfiguratorBase<CdmImportState> implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(CdmImportConfigurator.class);
	
	private boolean doHeterotypicSynonymsForBasionyms = true;
	private int limitSave = 1000;
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				FaunaEuropaeaHeterotypicSynonymImport.class
		};
	};
	
	//TODO
	private static IInputTransformer defaultTransformer = null;

	
	public static CdmImportConfigurator NewInstance(Source source, ICdmDataSource destination){
		return new CdmImportConfigurator(source, destination);
}
	
	private CdmImportConfigurator(Source source, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(source);
		setDestination(destination);
		setNomenclaturalCode(NomenclaturalCode.ICBN);
	}
	
	public static CdmImportConfigurator NewInstance(ICdmDataSource source, ICdmDataSource destination){
		return new CdmImportConfigurator(source, destination);
}
	
	private CdmImportConfigurator(ICdmDataSource source, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(source);
		setDestination(destination);
		setNomenclaturalCode(NomenclaturalCode.ICBN);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public ICdmDataSource getSource() {
		return (ICdmDataSource)super.getSource();
	}
	
	/**
	 * @param dbSource
	 */
	public void setSource(ICdmDataSource source) {
		super.setSource(source);
	}

//	public ICdmDataSource getSource() {
//		return source;
//	}
//	
//	public void setSource(ICdmDataSource source) {
//		this.source = source;
//	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase<?> getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			ReferenceFactory refFactory = ReferenceFactory.newInstance();
			sourceReference = refFactory.newDatabase();
			sourceReference.setTitleCache("Fauna Europaea database");
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public CdmImportState getNewState() {
		return new CdmImportState(this);
	}

	/**
	 * @return the limitSave
	 */
	public int getLimitSave() {
		return limitSave;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setLimitSave(int limitSave) {
		this.limitSave = limitSave;
	}

	/**
	 * @return the doHeterotypicSynonymsForBasionyms
	 */
	public boolean isDoHeterotypicSynonymsForBasionyms() {
		return doHeterotypicSynonymsForBasionyms;
	}

	/**
	 * @param doHeterotypicSynonymsForBasionyms the doHeterotypicSynonymsForBasionyms to set
	 */
	public void setDoHeterotypicSynonymsForBasionyms(
			boolean doHeterotypicSynonymsForBasionyms) {
		this.doHeterotypicSynonymsForBasionyms = doHeterotypicSynonymsForBasionyms;
	}

}
