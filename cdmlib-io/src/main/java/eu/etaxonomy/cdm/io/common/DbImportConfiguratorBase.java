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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 20.03.2008
 */
public abstract class DbImportConfiguratorBase<STATE extends DbImportStateBase>
            extends ImportConfiguratorBase<STATE, Source> {

    private static final long serialVersionUID = 3474072167155099394L;
	@SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private Method userTransformationMethod;

	/* Max number of records to be saved with one service call */
	private int recordsPerTransaction = 1000;

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
			if (StringUtils.isNotBlank(getSourceReferenceTitle())){
			    sourceReference.setTitleCache(getSourceReferenceTitle(), true);
			}else if (getSource() != null){
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

	public int getRecordsPerTransaction() {
		return recordsPerTransaction;
	}
	public void setRecordsPerTransaction(int recordsPerTransaction) {
		this.recordsPerTransaction = recordsPerTransaction;
	}

	public Method getUserTransformationMethod() {
		return userTransformationMethod;
	}
	public void setUserTransformationMethod(Method userTransformationMethod) {
		this.userTransformationMethod = userTransformationMethod;
	}
}
