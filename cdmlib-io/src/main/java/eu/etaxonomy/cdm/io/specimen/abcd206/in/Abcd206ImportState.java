/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

/**
 * @author a.mueller
 * @since 11.05.2009
 */
public class Abcd206ImportState
        extends SpecimenImportStateBase<Abcd206ImportConfigurator, Abcd206ImportState>{

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Abcd206ImportState.class);

	private String prefix;

	private List<String[]> associatedUnitIds = new ArrayList<>();

	private Set<URI> sequenceDataStableIdentifier = new HashSet<>();

	private List<UUID> fieldUnitUUIDsToDelete = new ArrayList<>();

	private FieldUnit lastFieldUnit = null;


//****************** CONSTRUCTOR ***************************************************/

    public Abcd206ImportState(Abcd206ImportConfigurator config) {
		super(config);
        setReport(new SpecimenImportReport());
        setTransformer(new AbcdTransformer());
	}

//************************ GETTER / SETTER *****************************************/

    @Override
    public Abcd206DataHolder getDataHolder() {
        return (Abcd206DataHolder)dataHolder;
    }

    public void setDataHolder(Abcd206DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    public List<UUID> getFieldUnitsToDelete() {
        return fieldUnitUUIDsToDelete;
    }
    public void setFieldUnitsToDelete(List<UUID> fieldUnitsToDelete) {
        this.fieldUnitUUIDsToDelete = fieldUnitsToDelete;
    }

    public void addFieldUnitsToDelete(FieldUnit fieldUnitToDelete) {
        if (fieldUnitToDelete != null){
            this.fieldUnitUUIDsToDelete.add(fieldUnitToDelete.getUuid());
        }
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }


    @Override
    public byte[] getReportAsByteArray() {
        ByteArrayOutputStream importStream = new ByteArrayOutputStream();
        getReport().printReport(new PrintStream(importStream));
        return importStream.toByteArray();
    }

    public void setAssociatedUnitIds(List<String[]> associatedUnitIds){
        this.associatedUnitIds = associatedUnitIds;
    }

    public List<String[]> getAssociatedUnitIds(){
        return this.associatedUnitIds;
    }

    public Set<URI> getSequenceDataStableIdentifier() {
        return sequenceDataStableIdentifier;
    }

    public void putSequenceDataStableIdentifier(URI sequenceDataStableIdentifier) {
        this.sequenceDataStableIdentifier.add(sequenceDataStableIdentifier);
    }

    public FieldUnit getLastFieldUnit() {
        return lastFieldUnit;
    }

    public void setLastFieldUnit(FieldUnit lastFieldUnit) {
        this.lastFieldUnit = lastFieldUnit;
        if (lastFieldUnit != null){
            this.setFieldUnit(lastFieldUnit);
        }
    }

//
//    public void reset() {
//        getDataHolder().reset();
//        setDerivedUnitBase(null);
//    }
}