/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 20.03.2008
 */
public abstract class DbImportConfiguratorBase<STATE extends DbImportStateBase> extends ImportConfiguratorBase<STATE, Source> implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DbImportConfiguratorBase.class);

	private Method userTransformationMethod;

	/* Max number of records to be saved with one service call */
	private int recordsPerTransaction = 1000;

	/**
	 * @param source
	 * @param destination
	 * @param code
	 */
	protected DbImportConfiguratorBase(Source source, ICdmDataSource destination, NomenclaturalCode code, IInputTransformer defaultTransformer) {
	   super(defaultTransformer);
	   setNomenclaturalCode(code);
	   setSource(source);
	   setDestination(destination);
	}


	@Override
    public Source getSource() {
		return super.getSource();
	}
	@Override
    public void setSource(Source berlinModelSource) {
		super.setSource(berlinModelSource);
	}


	@Override
    public Reference getSourceReference() {
		if (sourceReference == null){
			sourceReference =  ReferenceFactory.newDatabase();
			if (getSource() != null){
				sourceReference.setTitleCache(getSource().getDatabase(), true);
			}
			if (getSourceRefUuid() != null){
				sourceReference.setUuid(getSourceRefUuid());
			}
		}
		return sourceReference;
	}

	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().getDatabase();
		}
	}

	/**
	 * @return the limitSave
	 */
	public int getRecordsPerTransaction() {
		return recordsPerTransaction;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setRecordsPerTransaction(int recordsPerTransaction) {
		this.recordsPerTransaction = recordsPerTransaction;
	}


	/**
	 * @return the userTransformationMethod
	 */
	public Method getUserTransformationMethod() {
		return userTransformationMethod;
	}

	/**
	 * @param userTransformationMethod the userTransformationMethod to set
	 */
	public void setUserTransformationMethod(Method userTransformationMethod) {
		this.userTransformationMethod = userTransformationMethod;
	}


}
