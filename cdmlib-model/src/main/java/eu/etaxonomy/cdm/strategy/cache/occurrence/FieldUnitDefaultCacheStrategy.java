/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.occurrences.DistanceStringFormatter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * Formatting class for FieldUnits.
 *
 * Note: this class is mostly a copy from the orignal class DerivedUnitFacadeFieldUnitCacheStrategy
 *       in cdmlib-service. (#9678)
 *
 * @author a.mueller
 * @since 18.06.2021
 */
public class FieldUnitDefaultCacheStrategy
        extends OccurrenceCacheStrategyBase<FieldUnit>{

    private static final long serialVersionUID = -2313124329424995472L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FieldUnitDefaultCacheStrategy.class);

    private static final String METER = "m";

    private static final UUID uuid = UUID.fromString("6ada8184-6a2e-4f9a-b02a-49572f6d9570");
    @Override
    protected UUID getUuid() {return uuid;}

    private boolean includeEmptySeconds = false;
    private boolean includeReferenceSystem = true;

    private boolean includePlantDescription = true;
    private boolean addTrailingDot = true;

    public static FieldUnitDefaultCacheStrategy NewInstance(){
        return new FieldUnitDefaultCacheStrategy(null, null, null, null);
    }

    public static FieldUnitDefaultCacheStrategy NewInstance(boolean includePlantDescription, boolean addTrailingDot){
        return new FieldUnitDefaultCacheStrategy(null, null, includePlantDescription, addTrailingDot);
    }

    public static FieldUnitDefaultCacheStrategy NewInstance(boolean includeEmptySeconds,
            boolean includeReferenceSystem, boolean includePlantDescription, boolean addTrailingDot){
        return new FieldUnitDefaultCacheStrategy(includeEmptySeconds, includeReferenceSystem, includePlantDescription, addTrailingDot);
    }


    private FieldUnitDefaultCacheStrategy(Boolean includeEmptySeconds, Boolean includeReferenceSystem,
            Boolean includePlantDescription, Boolean addTrailingDot) {
        this.includeEmptySeconds = includeEmptySeconds == null ? this.includeEmptySeconds : includeEmptySeconds;
        this.includeReferenceSystem = includeReferenceSystem == null ? this.includeReferenceSystem : includeReferenceSystem;
        this.includePlantDescription = includePlantDescription == null ? this.includePlantDescription : includePlantDescription;
        this.addTrailingDot = addTrailingDot == null ? this.addTrailingDot : addTrailingDot;
    }

    @Override
    protected String doGetTitleCache(FieldUnit fieldUnit) {
        if (fieldUnit == null){
            return null;
        }
        String result = getFieldData(fieldUnit);
        if (includePlantDescription){
            result = addPlantDescription(result, fieldUnit);
        }
        if (addTrailingDot){
            result = CdmUtils.addTrailingDotIfNotExists(result);
        }
        return result;
    }

    private String getFieldData(FieldUnit fieldUnit) {

        if (fieldUnit.isProtectedTitleCache()){
            return fieldUnit.getTitleCache();
        }

        String ALTITUDE_PREFIX = "alt. ";
//      String ALTITUDE_POSTFIX = " m";

        String result = "";

        if (hasGatheringEvent(fieldUnit)){
            GatheringEvent gatheringEvent = CdmBase.deproxy(fieldUnit.getGatheringEvent());

            //country
            String strCountry = null;
            NamedArea country = gatheringEvent.getCountry();
            Representation repCountry = country == null ? null : country.getRepresentation(Language.DEFAULT());
            strCountry = repCountry == null ? null: repCountry.getLabel();
            result = CdmUtils.concat(", ", result, strCountry);

            //locality
            result = CdmUtils.concat(", ", result, getLocalityText(gatheringEvent));

            //elevation
            if (isNotBlank(absoluteElevationToString(gatheringEvent))){
                result = CdmUtils.concat(", " , result, ALTITUDE_PREFIX);
                result += absoluteElevationToString(gatheringEvent);
            }

            //exact locality
            if (gatheringEvent.getExactLocation() != null){
                String exactLocation = gatheringEvent.getExactLocation().toSexagesimalString(this.includeEmptySeconds, this.includeReferenceSystem);
                result = CdmUtils.concat(", ", result, exactLocation);
            }
        }

        //ecology
        result = CdmUtils.concat(", ", result, getEcology(fieldUnit));

        //gathering period
        //TODO period.toString ??
        TimePeriod gatheringPeriod = hasGatheringEvent(fieldUnit)? fieldUnit.getGatheringEvent().getTimeperiod(): null;
        result = CdmUtils.concat(", ", result, (gatheringPeriod == null? null : gatheringPeriod.toString()));

        //collector (team) and field number
        String collectorAndFieldNumber = getCollectorAndFieldNumber(fieldUnit);
        result = CdmUtils.concat(", ", result, collectorAndFieldNumber);

        return result;
    }

    private String getCollectorAndFieldNumber(FieldUnit fieldUnit) {
        String result = "";

        GatheringEvent gatheringEvent = fieldUnit.getGatheringEvent();
        AgentBase<?> collector = gatheringEvent == null? null : gatheringEvent.getCollector();
        String fieldNumber = fieldUnit.getFieldNumber();
        Person primaryCollector = fieldUnit.getPrimaryCollector();

        if (collector == null){
            return fieldNumber;
        }else if(collector.isProtectedTitleCache()){
            return  CdmUtils.concat(" ", collector.getTitleCache(), fieldNumber);
        }else{
            result = "";
            boolean hasMoreMembers = false;
            List<Person> teamMembers = new ArrayList<>();
            if (collector.isInstanceOf(Person.class)){
                if (primaryCollector == null){
                    primaryCollector = CdmBase.deproxy(collector, Person.class);
                }
                teamMembers.add(primaryCollector);
            } else if (collector.isInstanceOf(Team.class) && !collector.isProtectedTitleCache()){
                Team team = CdmBase.deproxy(collector, Team.class);
                teamMembers = team.getTeamMembers();
                hasMoreMembers = team.isHasMoreMembers();
            }else{ //protected titleCache
                return CdmUtils.concat(" ", collector.getTitleCache(), fieldNumber);
            }

            int counter = 0;
            boolean fieldNumberAdded = false;
            for (Person member : teamMembers){
                counter++;
                String sep = teamConcatSeparator(teamMembers, hasMoreMembers, counter);
                result = CdmUtils.concat(sep, result, getMemberString(member));
                if (member.equals(primaryCollector)){
                    result = addFieldNumber(result, fieldNumber);
                    fieldNumberAdded = true;
                }
            }
            if (hasMoreMembers){
                result = TeamDefaultCacheStrategy.addHasMoreMembers(result);
            }
            if (! fieldNumberAdded){
                result = addFieldNumber(result, fieldNumber);
            }
            return result;
        }
    }

    private String addFieldNumber(String str, String fieldNumber) {
        String result = CdmUtils.concat(" ", str, fieldNumber);
        return result;
    }

    //TODO
    private String getMemberString(Person member) {
        if (isNotBlank(member.getFamilyName()) && ! member.isProtectedTitleCache() ){
            String result = member.getFamilyName();
            if  (isNotBlank(member.getGivenName())){
                result = member.getGivenName().substring(0,1) + ". " + result;
            }
            return result;
        }else{
            return member.getTitleCache();
        }
    }

    public static String teamConcatSeparator(List<Person> teamMembers, boolean hasMoreMembers, int index) {
        if (index <= 1){
            return "";
        }else if (index < teamMembers.size() || (hasMoreMembers && index == teamMembers.size())){
            return TeamDefaultCacheStrategy.STD_TEAM_CONCATINATION;
        }else{
            return TeamDefaultCacheStrategy.FINAL_TEAM_CONCATINATION;
        }
    }


    private String getEcology(FieldUnit fieldUnit) {
        return getEcology(fieldUnit, Language.DEFAULT());
    }

    private String getEcology(FieldUnit fieldUnit, Language language) {
        Feature feature = Feature.ECOLOGY();
        LanguageString languageString = getTextDataAll(fieldUnit, feature).get(language);
        return (languageString == null ? null : languageString.getText());
    }

    private boolean hasGatheringEvent(FieldUnit fieldUnit) {
        return fieldUnit.getGatheringEvent() != null;
    }

    private String getLocalityText(GatheringEvent gatheringEvent) {
        LanguageString locality = gatheringEvent.getLocality();
        if (locality != null) {
            return locality.getText();
        }
        return null;
    }

    private String absoluteElevationToString(GatheringEvent gatheringEvent) {
        if (isNotBlank(gatheringEvent.getAbsoluteElevationText())){
            return gatheringEvent.getAbsoluteElevationText();
        }else{
            String text = gatheringEvent.getAbsoluteElevationText();
            Integer min = gatheringEvent.getAbsoluteElevation();
            Integer max = gatheringEvent.getAbsoluteElevationMax();
            return DistanceStringFormatter.distanceString(min, max, text, METER);
        }
    }
}
