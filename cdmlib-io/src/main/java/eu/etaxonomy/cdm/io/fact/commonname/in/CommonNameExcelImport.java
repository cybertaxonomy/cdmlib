/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.commonname.in;

import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Import for taxon based altitude data as quantitative data.
 *
 * @author a.mueller
 * @since 24.01.2023
 */
@Component
public class CommonNameExcelImport
        extends FactExcelImportBase<CommonNameExcelImportState, CommonNameExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = 8264900898340386516L;

    protected static final String COL_COMMON_NAME = "Common Name";
    protected static final String COL_LANGUAGE_LABEL = "Language Label";
    protected static final String COL_LANGUAGE_UUID = "Language UUID";
    protected static final String COL_AREA_LABEL = "Area Label";
    protected static final String COL_AREA_UUID = "Area UUID";


    @Override
    protected void doFirstPass(CommonNameExcelImportState state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
            return;
//            taxon = Taxon.NewInstance(null, null);
        }

        String commonNameText = getValue(state, COL_COMMON_NAME);
        if (isBlank(commonNameText)) {
            return;
        }

        UUID featureUuid = Feature.uuidCommonName;
        String featureLabel = "Common Name";
        Feature feature = getFeature(state, featureUuid);
        if (feature == null){
            feature = getFeature(state, featureUuid, featureLabel, featureLabel, null, null);
        }

        Language language = makeLanguage(state);

        NamedArea area = makeArea(state);

        CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameText, language, area);

        //source
        String id = null;
        String idNamespace = null;
        Reference reference = getSourceReference(state);

        //description
        TaxonDescription taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, true);
        taxonDescription.addElement(commonName);
        if (state.getConfig().getSourceType() == OriginalSourceType.PrimaryTaxonomicSource) {
            DoubleResult<TaxonName, String> originalName = getOriginalName(state, taxon,
                    state.getConfig().getColNameTitleCache(), state.getConfig().getColNameCache());
            DescriptionElementSource source = commonName.addSource(OriginalSourceType.PrimaryTaxonomicSource, reference, null, originalName.getSecondResult());
            source.setNameUsedInSource(originalName.getFirstResult());
        }else {
            //TODO other source types
            commonName.addImportSource(id, idNamespace, reference, linePure);
        }
    }

    private Language makeLanguage(CommonNameExcelImportState state) {
        UUID languageUuid = state.getConfig().getDefaultLanguageUuid();
        if (languageUuid == null) {
            String languageUuidStr = getValue(state, COL_LANGUAGE_UUID);
            if (languageUuidStr != null) {
                try {
                    languageUuid = UUID.fromString(languageUuidStr);
                } catch (Exception e) {
                    //TODO log message
                }
            }
        }
        Language language = getLanguage(state, languageUuid);
        return language;
    }

    private NamedArea makeArea(CommonNameExcelImportState state) {
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
    protected boolean isIgnore(CommonNameExcelImportState state) {
        return false;
    }

}