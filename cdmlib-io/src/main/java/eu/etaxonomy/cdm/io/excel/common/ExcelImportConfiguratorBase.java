/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.excel.common;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public abstract class ExcelImportConfiguratorBase
            extends ImportConfiguratorBase<ExcelImportState, URI>{

    private static final long serialVersionUID = 9031559773350645304L;
    private static final Logger logger = Logger.getLogger(ExcelImportConfiguratorBase.class);

	//TODO
	private static IInputTransformer defaultTransformer = null;
	private byte[] stream;
	private boolean deduplicateReferences = false;
	private boolean deduplicateAuthors = false;

	private String worksheetName = null;

	private Map<String,String> labelReplacements = new HashMap<>();

	protected ExcelImportConfiguratorBase(URI uri, ICdmDataSource destination) {
		this(uri, destination, defaultTransformer);
	}

	protected ExcelImportConfiguratorBase(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
		super(transformer);
		setSource(uri);
		setDestination(destination);
	}

	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			sourceReference.setTitleCache("Excel import " + getDateString(), true);
		}
		return sourceReference;
	}

	public boolean isReuseExistingTaxaWhenPossible() {
		return false;
	}
	 public byte[] getStream(){
	        return stream;
    }
    public void setStream(byte[] stream) {
        this.stream = stream;
    }

    public boolean isDeduplicateReferences() {
        return deduplicateReferences;
    }
    public void setDeduplicateReferences(boolean deduplicateReferences) {
        this.deduplicateReferences = deduplicateReferences;
    }

    public boolean isDeduplicateAuthors() {
        return deduplicateAuthors;
    }
    public void setDeduplicateAuthors(boolean deduplicateAuthors) {
        this.deduplicateAuthors = deduplicateAuthors;
    }

    public String getWorksheetName() {
        return worksheetName;
    }
    public void setWorksheetName(String worksheetName) {
        this.worksheetName = worksheetName;
    }

    public String replaceColumnLabel(String label) {
        String result = labelReplacements.get(label);
        if (result == null){
            return label;
        }else{
            return result;
        }
    }

    protected void putLabelReplacement(String defaultLabel, String newLabel) {
        labelReplacements.put(defaultLabel, newLabel);
    }
}
