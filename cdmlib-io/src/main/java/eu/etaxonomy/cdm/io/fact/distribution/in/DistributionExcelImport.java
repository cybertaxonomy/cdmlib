/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.distribution.in;

import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportBase;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Import for taxon based distribution data.
 *
 * @author a.mueller
 * @since 08.10.2024
 */
@Component
public class DistributionExcelImport
        extends FactExcelImportBase<DistributionExcelImportState, DistributionExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = 605936178884105294L;

    protected static final String COL_AREA_LABEL = "Area Label";
    protected static final String COL_AREA_UUID = "Area UUID";
    protected static final String COL_STATUS_LABEL = "Status Label";
    protected static final String COL_STATUS_UUID = "Status UUID";


    @Override
    protected void doFirstPass(DistributionExcelImportState state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
            return;
//            taxon = Taxon.NewInstance(null, null);
        }

        PresenceAbsenceTerm status = makeStatus(state);

        NamedArea area = makeArea(state);

        Distribution distribution = Distribution.NewInstance(area, status);
        if (state.getConfig().getDistributionFeatureUuid() != null){
            Feature feature = this.getFeature(state, state.getConfig().getDistributionFeatureUuid(),
                    "Newly created distribution feature", "Newly created distribution feature", null, null);
            distribution.setFeature(feature);
        }

        //source
        String id = null;
        String idNamespace = null;
        Reference reference = getSourceReference(state);

        //description
        TaxonDescription taxonDescription;
        if (state.getConfig().getDescriptionMarkerTypeUuid() != null) {
            MarkerType markerType = this.getMarkerType(state, state.getConfig().getDescriptionMarkerTypeUuid(),
                    "Newly created marker type for distribution import", "Newly created marker type for distribution import", null);
            String title = "Factual data set for " + markerType.getLabel();
            taxonDescription = this.getMarkedTaxonDescription(taxon, markerType, !IMAGE_GALLERY, CREATE, null, title);
        }else {
            taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, CREATE);
        }
        taxonDescription.addElement(distribution);
        if (state.getConfig().getSourceType() == OriginalSourceType.PrimaryTaxonomicSource) {
            DoubleResult<TaxonName, String> originalName = getOriginalName(state, taxon,
                    state.getConfig().getColNameTitleCache(), state.getConfig().getColNameCache());
            DescriptionElementSource source = distribution.addSource(OriginalSourceType.PrimaryTaxonomicSource, reference, null, originalName.getSecondResult());
            source.setNameUsedInSource(originalName.getFirstResult());
        }else {
            //TODO other source types
            distribution.addImportSource(id, idNamespace, reference, linePure);
        }
    }

    private PresenceAbsenceTerm makeStatus(DistributionExcelImportState state) {

        UUID statusUuid = state.getConfig().getDefaultStatusUuid();
        if (statusUuid == null) {
            String statusUuidStr = getValue(state, COL_STATUS_UUID);
            if (statusUuidStr != null) {
                try {
                    statusUuid = UUID.fromString(statusUuidStr);
                } catch (Exception e) {
                    //TODO log message
                }
            }
        }
        PresenceAbsenceTerm status = getPresenceTerm(state, statusUuid);
        return status;
    }

    private NamedArea makeArea(DistributionExcelImportState state) {
        UUID areaUuid = state.getConfig().getDefaultAreaUuid();
        if (areaUuid == null) {
            String areaUuidStr = getValue(state, COL_AREA_UUID);
            if (areaUuidStr != null) {
                try {
                    areaUuid = UUID.fromString(areaUuidStr);
                } catch (Exception e) {
                    //TODO log message
                }
            }
        }
        NamedArea area = getNamedArea(state, areaUuid);
        return area;
    }


    @Override
    protected boolean requiresNomenclaturalCode() {
        return false;
    }

    @Override
    protected boolean isIgnore(DistributionExcelImportState state) {
        return false;
    }

}