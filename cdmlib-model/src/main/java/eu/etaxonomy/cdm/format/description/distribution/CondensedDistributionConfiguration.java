/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description.distribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.compare.common.OrderType;
import eu.etaxonomy.cdm.format.description.distribution.CondensedDistributionComposer.SymbolUsage;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;

/**
 * @author a.mueller
 * @since 17.02.2021
 */
public class CondensedDistributionConfiguration implements Serializable {

    private static final long serialVersionUID = -4753899114349109805L;

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

    public SymbolUsage areaSymbolField = SymbolUsage.IdInVoc;   //currently IdInVoc for both, but may change in future

    public SymbolUsage statusSymbolField = SymbolUsage.Symbol2;   //currently Symbol1 for Cuba, but may change in future

    public OrderType orderType = OrderType.ALPHABETIC;

    //if true, any non-empty symbol is taken from symbol2, symbol1, idInVoc and abbrevLabel according to the given order
    public boolean showAnyStatusSmbol = false;   //usually does not make sense to mix symbol fields

    public Set<UUID> fallbackAreaMarkers = new HashSet<>(Arrays.asList(MarkerType.uuidFallbackArea)); //Note: we do not have hidden area markers anymore (use area tree instead)

    //should the status be put behind the area?
    public boolean statusTrailing = false;

    //seperator between area and status (currently used for trailing status only)
    public String statusSeparator = "";

//************************** FACTORY ***************************************/

    public static CondensedDistributionConfiguration NewDefaultInstance() {
        CondensedDistributionConfiguration result = new CondensedDistributionConfiguration();
        result.fallbackAreaMarkers.add(MarkerType.uuidFallbackArea);
        return result;
    }

    public static CondensedDistributionConfiguration NewIucnInstance() {
        CondensedDistributionConfiguration result = NewDefaultInstance();
        result.statusTrailing = true;
        result.statusSeparator = ":";
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
        result.statusSymbolField = SymbolUsage.Symbol1;
        result.orderType = OrderType.NATURAL;
        return result;
    }
}