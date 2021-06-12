/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.temporal.in;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportBase;
import eu.etaxonomy.cdm.model.common.ExtendedTimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Import for taxon based temporal data.
 *
 * @author a.mueller
 * @since 15.07.2020
 */
@Component
public class TemporalDataExcelImport<STATE extends TemporalDataExcelImportState<CONFIG>, CONFIG extends TemporalDataExcelImportConfigurator<?>>
        extends FactExcelImportBase<STATE, CONFIG, ExcelRowBase>{

    private static final long serialVersionUID = -2885338554616141176L;

    @Override
    protected String getWorksheetName(CONFIG config) {
        return "Data";
    }

    @Override
    protected void doFirstPass(STATE state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
//            return;
            taxon = Taxon.NewInstance(null, null);
        }

        Map<String, String> record = state.getOriginalRecord();

        Feature feature = null;
        UUID uuidFeature = state.getConfig().getFeatureUuid();
        if (uuidFeature != null){
            feature = (Feature)getTermService().find(uuidFeature);
        }
        if (feature == null){
            String message = "Feature could not be defined. Import not possible.";
            state.addError(message);
            return;
        }

        String colLabelStart = state.getConfig().getColumnLabelStart();
        String colLabelEnd = state.getConfig().getColumnLabelEnd();

        handleFeature(state, taxon, line, linePure, record, feature, colLabelStart, colLabelEnd);

    }

    protected void handleFeature(STATE state, Taxon taxon, String line, String linePure,
            Map<String, String> record, Feature feature, String startColLabel, String endColLabel) {

        TemporalData temporalData = TemporalData.NewInstance(feature);
        String startMonthStr = getValue(record, startColLabel);
        String endMonthStr = getValue(record, endColLabel);
        Integer startMonth = monthToInteger(state, startMonthStr, startColLabel, false);
        Integer startMonthExtreme = monthToInteger(state, startMonthStr, startColLabel, true);
        Integer endMonth = monthToInteger(state, endMonthStr, endColLabel, false);
        Integer endMonthExtreme = monthToInteger(state, endMonthStr, endColLabel, true);
        ExtendedTimePeriod period = ExtendedTimePeriod.NewExtendedMonthInstance(startMonth, endMonth, startMonthExtreme, endMonthExtreme);
        temporalData.setPeriod(period);

        //source
        String id = null;
        String idNamespace = getWorksheetName(state.getConfig());
        Reference reference = getSourceReference(state);

        //description
        if (!temporalData.getPeriod().isEmpty()){
            TaxonDescription taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, true);
            taxonDescription.addElement(temporalData);
            temporalData.addImportSource(id, idNamespace, reference, linePure);
        }
    }

    private Integer monthToInteger(STATE state, String monthStr, String colLabel, boolean isExtreme) {
        if(StringUtils.isBlank(monthStr)){
            return null;
        }else {
            int extremeIndex = 2;
            int normalIndex = 1;
            Matcher matcher = getPattern().matcher(monthStr);
            if (!matcher.matches()){
                matcher = getReversePattern().matcher(monthStr);
                extremeIndex = 1;
                normalIndex = 2;
            }
            if (matcher.matches()){
                if(isExtreme){
                    if (matcher.group(extremeIndex) != null){
                        String extreme = matcher.group(extremeIndex);
                        return Integer.valueOf(extreme);
                    }else{
                        return null; //extreme value does not exist
                    }
                }else{
                    String normal = matcher.group(normalIndex);
                    return Integer.valueOf(normal);
                }
            }else{
                if (!isExtreme){  //we have to record this only once
                    String message = "Value " + monthStr + " for " + colLabel + " could not be transformed to a valid month number. Value not imported." ;
                    state.addError(message);
                }
            }
        }
        return null;
    }

    private Pattern monthPattern;
    private String nr = "(0?[1-9]|1[0-2])";

    private Pattern getPattern() {
        if (monthPattern == null){
            monthPattern = Pattern.compile(nr + "\\s*(?:\\(" + nr + "\\))?");
        }
        return monthPattern;
    }

    private Pattern reverseMonthPattern;
    private Pattern getReversePattern(){
        if (reverseMonthPattern == null){
            reverseMonthPattern = Pattern.compile("(?:\\(" + nr + "\\))?\\s*" + nr);
        }
        return reverseMonthPattern;
    }

    @Override
    protected boolean requiresNomenclaturalCode() {
        return false;
    }

    @Override
    protected boolean isIgnore(STATE state) {
        return false;
    }
}