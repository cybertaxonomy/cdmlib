/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.ExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;

/**
 * @author pplitzner
 * @since May 2, 2019
 */
public class StructureTreeOwlExportConfigurator extends ExportConfiguratorBase<StructureTreeOwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = -4360021755279592592L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StructureTreeOwlExportConfigurator.class);

    private List<UUID> featureTreeUuids = new ArrayList<>();
    private List<UUID> vocabularyUuids = new ArrayList<>();

    private StructureTreeOwlExportConfigurator() {
        super(null);
    }

    public static StructureTreeOwlExportConfigurator NewInstance() {
        return new StructureTreeOwlExportConfigurator();
    }

    @Override
    public StructureTreeOwlExportState getNewState() {
        return new StructureTreeOwlExportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[] {
                // order is important: the last export has to write the model
                TermVocabularyOwlExport.class,
                StructureTreeOwlExport.class
        };
    }

    public List<UUID> getFeatureTreeUuids() {
        return featureTreeUuids;
    }

    public List<UUID> getVocabularyUuids() {
        return vocabularyUuids;
    }

    public void setFeatureTreeUuids(List<UUID> featureTreeUuids) {
        this.featureTreeUuids = featureTreeUuids;
    }

    public void setVocabularyUuids(List<UUID> vocabularyUuids) {
        this.vocabularyUuids = vocabularyUuids;
    }

    @Override
    public String getDestinationNameString() {
        return null;
    }
}