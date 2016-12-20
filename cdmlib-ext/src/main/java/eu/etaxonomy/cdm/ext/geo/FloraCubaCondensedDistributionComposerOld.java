/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.dto.CondensedDistribution;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @date Apr 05, 2016
 *
 */
public class FloraCubaCondensedDistributionComposerOld extends CondensedDistributionComposerBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FloraCubaCondensedDistributionComposerOld.class);

//    private static Set<UUID> foreignStatusUuids;

    private UUID uuidInternalArea = UUID.fromString("d0144a6e-0e17-4a1d-bce5-d464a2aa7229");  //Cuba

    private String internalAreaSeparator = UTF8.EN_DASH.toString() + " ";


//    // these status uuids are special for EuroPlusMed and might also be used
//    private final static UUID REPORTED_IN_ERROR_UUID =  UUID.fromString("38604788-cf05-4607-b155-86db456f7680");

    static {

        // ==================================================
        // Mapping as defined in ticket http://dev.e-taxonomy.eu/trac/ticket/5682
        // ==================================================

       statusSymbols = new HashMap<UUID, String> ();
       //no entries as we handle symbols now on model level

    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public CondensedDistribution createCondensedDistribution(Collection<Distribution> filteredDistributions,
            List<Language> languages) {

        CondensedDistribution condensedDistribution = new CondensedDistribution();

        //empty
        if (filteredDistributions == null || filteredDistributions.isEmpty()){
            return condensedDistribution;
        }

        OrderedTermVocabulary<NamedArea> areaVocabulary = CdmBase.deproxy(filteredDistributions.iterator().next().getArea().getVocabulary(), OrderedTermVocabulary.class);

        //deproxy and reverse order
        List<NamedArea> areaList = new ArrayList<NamedArea>();
        for (DefinedTermBase<NamedArea> dtb : areaVocabulary.getOrderedTerms()){
            areaList.add(0, (NamedArea)CdmBase.deproxy(dtb));
        }


        boolean isFirstAfterInternalArea = false;
        for (NamedArea area : areaList){

            if (area.getPartOf() != null){
                continue;  //subarea are handled later
            }

            StringBuilder areaStatusString = new StringBuilder();



            Distribution distribution = getDistribution(area, filteredDistributions);
            if (distribution == null){
                continue;
            }

            if (area.getUuid().equals(uuidInternalArea)){
                isFirstAfterInternalArea = true;
            }else if(isFirstAfterInternalArea && !area.getUuid().equals(uuidInternalArea)){
                areaStatusString.append(internalAreaSeparator);
                isFirstAfterInternalArea = false;
            }

            PresenceAbsenceTerm status = distribution.getStatus();

            String statusSymbol = statusSymbol(status);
            areaStatusString.append(statusSymbol);

            String areaLabel = makeAreaLabel(languages, area);
            areaStatusString.append(areaLabel);

            if(!area.getIncludes().isEmpty()) {
//                areaStatusString.append('(');
                subAreaLabels(languages, area.getIncludes(), areaStatusString, statusSymbol, areaLabel, filteredDistributions);
//                areaStatusString.append(')');
            }


//            if(isForeignStatus(status)) {
//                condensedDistribution.addForeignDistributionItem(status, areaStatusString.toString(), areaLabel);
//            } else {
                condensedDistribution.addIndigenousDistributionItem(status, areaStatusString.toString(), areaLabel);
//            }

        }

//        }
//        //5. order the condensedDistributions alphabetically
//        // FIXME
//        condensedDistribution.sortForeign();
//        condensedDistribution.sortIndigenous();

        return condensedDistribution;
    }

    /**
     * @param area
     * @param filteredDistributions
     * @return
     */
    private Distribution getDistribution(NamedArea area, Collection<Distribution> filteredDistributions) {
        for (Distribution dist : filteredDistributions){
            if (dist.getArea() != null && dist.getArea().equals(area)){
                return dist;
            }
        }
        return null;
    }


//    private boolean isForeignStatus(PresenceAbsenceTerm status) {
//        return foreignStatusUuids.contains(status.getUuid());
//    }

    /**
     * @param langs
     * @param node
     * @param areaString
     * @param statusSymbol
     */
    private void subAreaLabels(List<Language> langs, Collection<NamedArea> subAreas, StringBuilder areaString,
            String statusSymbol, String parentLabel,
            Collection<Distribution> filteredDistributions) {
        //TODO very redundant with main method
        List<String> subAreaLabels = new ArrayList<String>();

        //deproxy and reverse order
        List<NamedArea> areaList = new ArrayList<NamedArea>();
        for (DefinedTermBase<NamedArea> dtb : subAreas){
            areaList.add(0, (NamedArea)CdmBase.deproxy(dtb));
        }
//        Collections.sort(areaList);

        for(NamedArea area : areaList) {

            StringBuilder subAreaString = new StringBuilder();
            Distribution distribution = getDistribution(area, filteredDistributions);
            if (distribution == null){
                continue;
            }


            PresenceAbsenceTerm status = distribution.getStatus();
            String subAreaStatusSymbol = statusSymbol(status);
            if (subAreaStatusSymbol != null && !subAreaStatusSymbol.equals(statusSymbol)){
                subAreaString.append(subAreaStatusSymbol);
            }

            String areaLabel = makeAreaLabel(langs, area);
//            String cleanSubAreaLabel = StringUtils.replaceEach(areaLabel, new String[] {parentLabel, "(", ")"}, new String[] {"", "", ""});
            String cleanSubAreaLabel = areaLabel;
            subAreaString.append(cleanSubAreaLabel);

            if(!area.getIncludes().isEmpty()) {
//                subAreaString.append('(');
                subAreaLabels(langs, area.getIncludes(), subAreaString, subAreaStatusSymbol, areaLabel, filteredDistributions);
//                subAreaString.append(')');
            }

            subAreaLabels.add(subAreaString.toString());
        }

//      Collections.sort(subAreaLabels);
        if (!subAreaLabels.isEmpty()){
            areaString.append("(" + StringUtils.join(subAreaLabels, " ") + ")");
        }

    }


    public String getInternalAreaSeparator() {
        return internalAreaSeparator;
    }

    public void setInternalAreaSeparator(String internalAreaSeparator) {
        this.internalAreaSeparator = internalAreaSeparator;
    }






}
