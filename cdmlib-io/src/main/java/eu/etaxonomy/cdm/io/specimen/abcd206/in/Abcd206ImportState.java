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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class Abcd206ImportState extends SpecimenImportStateBase<Abcd206ImportConfigurator<Abcd206ImportState, InputStream>, Abcd206ImportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Abcd206ImportState.class);


	private String prefix;





	private Abcd206DataHolder dataHolder;


	private List<OriginalSourceBase<?>> associationRefs = new ArrayList<OriginalSourceBase<?>>();
	private boolean associationSourcesSet=false;
	private List<OriginalSourceBase<?>> descriptionRefs = new ArrayList<OriginalSourceBase<?>>();
	private boolean descriptionSourcesSet=false;
	private List<OriginalSourceBase<?>> derivedUnitSources = new ArrayList<OriginalSourceBase<?>>();
	private boolean derivedUnitSourcesSet=false;
	private boolean descriptionGroupSet = false;


//****************** CONSTRUCTOR ***************************************************/

	public Abcd206ImportState(Abcd206ImportConfigurator config) {
		super(config);
        setReport(new SpecimenImportReport());
        setTransformer(new AbcdTransformer());
	}

//************************ GETTER / SETTER *****************************************/



    @Override
    public Abcd206DataHolder getDataHolder() {
        return dataHolder;
    }

    public void setDataHolder(Abcd206DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }



    public List<OriginalSourceBase<?>> getAssociationRefs() {
        return associationRefs;
    }

    public void setAssociationRefs(List<OriginalSourceBase<?>> associationRefs) {
        this.associationRefs = associationRefs;
    }

    public boolean isAssociationSourcesSet() {
        return associationSourcesSet;
    }

    public void setAssociationSourcesSet(boolean associationSourcesSet) {
        this.associationSourcesSet = associationSourcesSet;
    }

    public List<OriginalSourceBase<?>> getDescriptionRefs() {
        return descriptionRefs;
    }

    public void setDescriptionRefs(List<OriginalSourceBase<?>> descriptionRefs) {
        this.descriptionRefs = descriptionRefs;
    }

    public boolean isDescriptionSourcesSet() {
        return descriptionSourcesSet;
    }

    public void setDescriptionSourcesSet(boolean descriptionSourcesSet) {
        this.descriptionSourcesSet = descriptionSourcesSet;
    }

    public List<OriginalSourceBase<?>> getDerivedUnitSources() {
        return derivedUnitSources;
    }

    public void setDerivedUnitSources(List<OriginalSourceBase<?>> derivedUnitSources) {
        this.derivedUnitSources = derivedUnitSources;
    }

    public boolean isDerivedUnitSourcesSet() {
        return derivedUnitSourcesSet;
    }

    public void setDerivedUnitSourcesSet(boolean derivedUnitSourcesSet) {
        this.derivedUnitSourcesSet = derivedUnitSourcesSet;
    }

    public boolean isDescriptionGroupSet() {
        return descriptionGroupSet;
    }

    public void setDescriptionGroupSet(boolean descriptionGroupSet) {
        this.descriptionGroupSet = descriptionGroupSet;
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

    @Override
    public void reset() {
        getDataHolder().reset();
        setDerivedUnitBase(null);
    }
}
