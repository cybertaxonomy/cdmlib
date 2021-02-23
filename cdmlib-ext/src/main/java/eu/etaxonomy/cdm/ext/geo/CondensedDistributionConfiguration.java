/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.ext.geo.CondensedDistributionComposer.StatusSymbolUsage;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * @author a.mueller
 * @since 17.02.2021
 */
public class CondensedDistributionConfiguration{

    //if true, all areas are shown in bold, no matter which status they have
    public boolean areasBold = false;  //true for Cuba

    //if true the area of scope (e.g. EM) area label is shown, otherwise only
    //the status symbol (usually an endemism information is shown, if at all)
    public boolean showAreaOfScopeLabel = false;   //true for Cuba

    //the separator before the list of outOfScope areas
    public String outOfScopeAreasSeperator = " " + UTF8.EN_DASH.toString() + " ";   //not needed for E+M

    public boolean splitNativeAndIntroduced = true;  //false for Cuba

    public String introducedBracketStart = "[";

    public String introducedBracketEnd = "]";

    //if true a subarea Az(F) will be shortened to F if it is shown in brackets after the parent area
    public boolean shortenSubAreaLabelsIfPossible = true;  //not relevant for Cuba

    public boolean showStatusOnParentAreaIfAllSame = true;  //false for Cuba

    public List<String> areaOfScopeSubAreaBracketStart = Arrays.asList(new String[]{" ","("});  // "(","(" for Cuba

    public List<String> areaOfScopeSubAreaBracketEnd = Arrays.asList(new String[]{"",")"});    // ")",")" for Cuba

    public List<UUID> statusForBoldAreas = Arrays.asList(new UUID[]{PresenceAbsenceTerm.uuidNative});  //empty for Cuba as for Cuba all areas are bold

    public StatusSymbolUsage statusSymbolField = StatusSymbolUsage.Symbol2;   //currently Symbol1, but may change in future

    //if true, any non-empty symbol is taken from symbol2, symbol1, idInVoc and abbrevLabel according to the given order
    public boolean showAnyStatusSmbol = false;   //usually does not make sense to mix symbol fields

    public UUID fallbackAreaMarker = MarkerType.uuidFallbackArea;

//************************** FACTORY ***************************************/

    public static CondensedDistributionConfiguration NewDefaultInstance() {
        CondensedDistributionConfiguration result = new CondensedDistributionConfiguration();
        return result;
    }

    public static CondensedDistributionConfiguration NewCubaInstance() {
        CondensedDistributionConfiguration result = new CondensedDistributionConfiguration();
        result.areasBold = true;
        result.showAreaOfScopeLabel = true;
        result.splitNativeAndIntroduced = false;
        result.shortenSubAreaLabelsIfPossible = false;
        result.showStatusOnParentAreaIfAllSame = false;
        result.areaOfScopeSubAreaBracketStart.set(0, "(");
        result.areaOfScopeSubAreaBracketEnd.set(0, ")");
        result.statusForBoldAreas = new ArrayList<>();
        result.statusSymbolField = StatusSymbolUsage.Symbol1;
        return result;
    }
}