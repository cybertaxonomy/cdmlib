/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.occurrences.DistanceStringFormatter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SecundumSource;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * @author a.mueller
 * @date 19.03.2021
 */
public class ReferencingObjectFormatter {

    public static String format(CdmBase element, Language defaultLanguage) {

        String resultString = null;
        if (element == null){
            return null;
        }else if (element instanceof IdentifiableEntity) {
            resultString = ((IdentifiableEntity<?>) element).getTitleCache();
        }else if (element instanceof OriginalSourceBase) {
            OriginalSourceBase originalSource = (OriginalSourceBase) element;
//              ISourceable sourcedObject = originalSource.getSourcedObj();
            //due to #5743 the bidirectionality for sourced object had to be removed

            String sourceObjectTitle = "sourced object data not available (#5743)";

            //it is now possible for NomenclaturalSource as they link to the sourced name
            if (originalSource instanceof NomenclaturalSource){
                TaxonName sourcedName = ((NomenclaturalSource)originalSource).getSourcedName();
                sourceObjectTitle = sourcedName == null ? "Source orphaned, not attached to a name" :
                    "for " + sourcedName.getTitleCache();
            }else if (originalSource instanceof SecundumSource){
                TaxonBase<?> sourcedTaxon = ((SecundumSource)originalSource).getSourcedTaxon();
                sourceObjectTitle = sourcedTaxon == null ? "Source orphaned, not attached to a taxon" :
                    "for " + sourcedTaxon.getTitleCache();
            }else if (originalSource instanceof DescriptionElementSource){
                sourceObjectTitle = getCache((DescriptionElementSource)originalSource, defaultLanguage);
            }
            resultString = CdmUtils.concat("; ", new String[]{originalSource.getIdNamespace(), originalSource.getIdInSource(), sourceObjectTitle});
        }else if (element instanceof LanguageStringBase) {
            resultString = ((LanguageStringBase) element).getText();
        }else if (element instanceof DescriptionElementBase) {
            resultString = getCache((DescriptionElementBase) element, defaultLanguage);
        }else if (element instanceof RelationshipBase<?, ?, ?>) {
            resultString = getCache((RelationshipBase<?, ?, ?>) element, defaultLanguage);
        }else if (element instanceof TypeDesignationBase<?>) {
            resultString = getCache((TypeDesignationBase<?>) element, defaultLanguage);
        }else if (element instanceof HomotypicalGroup) {
            resultString = getCache((HomotypicalGroup) element);
        }else if (element instanceof TaxonNode) {
            resultString = getCache((TaxonNode) element);
        }else if (element instanceof DeterminationEvent) {
            resultString = getCache((DeterminationEvent) element);
        }else if (element instanceof NomenclaturalStatus) {
            resultString = getCache((NomenclaturalStatus) element);
        }else if (element instanceof GatheringEvent){
            resultString = getCache((GatheringEvent) element);
        }else if (element instanceof Marker) {
            Marker marker = (Marker) element;
            MarkerType type = marker.getMarkerType();
            resultString = (type == null ? "- no marker type -" : marker.getMarkerType().getLabel()) + " (" + marker.getFlag() + ")";
        }else if (element instanceof User) {
            User user = (User) element;
            resultString = user.getUsername();
        }else if (element instanceof Group) {
            Group group = (Group) element;
            resultString = group.getName();
        }else if (element instanceof KeyStatement) {
            KeyStatement keyStatement = (KeyStatement) element;
            resultString = getCache(keyStatement);
        }else{
            // TODO write return texts for HomotypicalGroup, etc.
            resultString = element.toString();
        }

        if (resultString == null){
            resultString = element.toString();
        }
        return resultString;
    }

    private static String getCache(DescriptionElementSource source, Language defaultLanguage) {
        DescriptionElementBase sourcedElement = source.getSourcedElement();

        if (sourcedElement == null){
            return "Source orphaned, not attached to a fact";
        }
        String superLabel = getDescribedObjectLabel(sourcedElement.getInDescription());
        String result = CdmUtils.concat(": ", DescriptionElementFormatter.format(sourcedElement, defaultLanguage), superLabel);
        return result;
    }

    private static String getDescribedObjectLabel(DescriptionBase<?> inDescription) {
        IDescribable<?> entity = inDescription.describedEntity();
        if (entity != null){
            return entity.getTitleCache();
        }else{
            return inDescription.getTitleCache();
        }
    }

    private static String getCache(RelationshipBase<?, ?, ?> rel, Language defaultLanguage) {
        rel = CdmBase.deproxy(rel);
        RelationshipTermBase<?> type = rel.getType();
        IdentifiableEntity<?> from;
        IdentifiableEntity<?> to;
        if (rel.isInstanceOf(NameRelationship.class)){
            from = ((NameRelationship)rel).getFromName();
            to = ((NameRelationship)rel).getToName();
        }else if (rel.isInstanceOf(HybridRelationship.class)){
            from = ((HybridRelationship)rel).getParentName();
            to = ((HybridRelationship)rel).getHybridName();
        }else if (rel.isInstanceOf(TaxonRelationship.class)){
            from = ((TaxonRelationship)rel).getFromTaxon();
            to = ((TaxonRelationship)rel).getToTaxon();
        }else{
            try {
                Method fromMethod = rel.getClass().getMethod("getRelatedFrom");
                Method toMethod = rel.getClass().getMethod("getRelatedFrom");
                fromMethod.setAccessible(true);
                toMethod.setAccessible(true);
                from = (IdentifiableEntity<?>)fromMethod.invoke(rel);
                to = (IdentifiableEntity<?>)toMethod.invoke(rel);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        String typeLabel = null;
        if (type != null){
            Representation typeRepr = type.getPreferredRepresentation(defaultLanguage);
            if (typeRepr != null){
                typeLabel = typeRepr.getAbbreviatedLabel();
            }
            if (isBlank(typeLabel) && typeRepr != null){
                typeLabel = typeRepr.getLabel();
            }
            if (isBlank(typeLabel) ){
                typeLabel = type.getSymbol();
            }
            if (isBlank(typeLabel)){
                typeLabel = type.getTitleCache();
            }
        }
        if (isBlank(typeLabel)){
            typeLabel = "->";
        }
        String result = CdmUtils.concat(" ", new String[]{from == null ? null : from.getTitleCache(),
                typeLabel, to == null? null : to.getTitleCache()});
        return result;
    }

    private static String getCache(GatheringEvent gatheringEvent){
        String ALTITUDE_PREFIX = "alt. ";
        final String METER = "m";

        String result = "";

        //collector
        AgentBase<?> collector = CdmBase.deproxy(gatheringEvent.getCollector());
        String collectorStr = collector == null? null : collector.getTitleCache();
        result = CdmUtils.concat(", ", result, collectorStr);

        //gathering period
        TimePeriod gatheringPeriod = gatheringEvent.getTimeperiod();
        result = CdmUtils.concat(", ", result, (gatheringPeriod == null? null : gatheringPeriod.toString()));

        //country
        String strCountry = null;
        NamedArea country = gatheringEvent.getCountry();
        Representation repCountry = country == null ? null : country.getRepresentation(Language.DEFAULT());
        strCountry = repCountry == null ? null: repCountry.getLabel();
        result = CdmUtils.concat(", ", result, strCountry);

        //locality
        LanguageString locality = gatheringEvent.getLocality();
        if (locality != null) {
            result = CdmUtils.concat(", ", result, locality.getText());
        }

        //elevation
        String elevationStr;
        if (isNotBlank(gatheringEvent.getAbsoluteElevationText())){
            elevationStr = gatheringEvent.getAbsoluteElevationText();
        }else{
            String text = gatheringEvent.getAbsoluteElevationText();
            Integer min = gatheringEvent.getAbsoluteElevation();
            Integer max = gatheringEvent.getAbsoluteElevationMax();
            elevationStr = DistanceStringFormatter.distanceString(min, max, text, METER);
        }
        if (isNotBlank(elevationStr)){
            result = CdmUtils.concat(", " , result, ALTITUDE_PREFIX);
            result += elevationStr;
        }

        //exact locality
        if (gatheringEvent.getExactLocation() != null){
            String exactLocation = gatheringEvent.getExactLocation().toSexagesimalString(false, false);
            result = CdmUtils.concat(", ", result, exactLocation);
        }

        return result;
    }

    private static String getCache(DeterminationEvent detEvent) {
        //taxon
        String taxonStr = null;
        TaxonName taxonName = detEvent.getTaxonName();
        TaxonBase<?> taxon = detEvent.getTaxon();
        if (taxonName != null){
            taxonStr = taxonName.getTitleCache();
        }
        if (isBlank(taxonStr) && taxon != null){
            taxonStr = taxon.getTitleCache();
        }
        if (isBlank(taxonStr)){
            taxonStr = "no or unlabled taxon";
        }

        //unit
        SpecimenOrObservationBase<?> unit = detEvent.getIdentifiedUnit();
        String unitStr;
        if (unit != null){
            unitStr = unit.getTitleCache();
            if (isBlank(unitStr)){
                unitStr = "Unlabled unit";
            }
        }else{
            unitStr = "no unit";
        }

        String result = CdmUtils.concat(" determined as ", unitStr, taxonStr);

        return result;
    }

    private static String getCache(TaxonNode taxonNode) {
        String result = "";
        Classification classification = taxonNode.getClassification();
        if (classification != null){
            String classificationStr = classification.getName() == null ? "" : classification.getName().getText();
            result = CdmUtils.concat("" , result, classificationStr);
            if (isBlank(result)){
                result = classification.toString();
            }
        }
        String parentStr;
        TaxonNode parentNode = taxonNode.getParent();
        if (parentNode == null){
            parentStr = "no parent";
        }else{
            Taxon parentTaxon = parentNode.getTaxon();
            if (parentTaxon == null){
                parentStr = "no parent taxon";
            }else{
                TaxonName parentName = parentTaxon.getName();
                if (parentName == null){
                    parentStr = "child of " + parentTaxon.getTitleCache();
                }else{
                    parentStr = "child of " + parentName.getTitleCache();
                }
            }
        }
        result = CdmUtils.concat(": ", result, parentStr);

        return result;
    }

    private static String getCache(TypeDesignationBase<?> designation, Language defaultLanguage) {
        designation = CdmBase.deproxy(designation);
        //from
        String fromString = null;
        Set<TaxonName> from = designation.getTypifiedNames();
        if(from != null){
            for (TaxonName name : from){
                fromString = CdmUtils.concat(",", fromString, name.getTitleCache());
            }
        }
        //to
        IdentifiableEntity<?> to = null;
        String toStr = "";
        if (designation.isInstanceOf(SpecimenTypeDesignation.class)){
            to = ((SpecimenTypeDesignation)designation).getTypeSpecimen();
        }else if (designation.isInstanceOf(NameTypeDesignation.class)){
            to = ((NameTypeDesignation)designation).getTypeName();
        }else if (designation.isInstanceOf(TextualTypeDesignation.class)){
            toStr = ((TextualTypeDesignation)designation).getPreferredText(defaultLanguage);
        }else{
            throw new RuntimeException("Type Designation class not supported: " +  designation.getClass().getName());
        }
        toStr = to == null? toStr : to.getTitleCache();
        //status
        String typeLabel = null;
        TypeDesignationStatusBase<?> status = designation.getTypeStatus();
        if (status != null){
            Representation typeRepr = status.getPreferredRepresentation(defaultLanguage);
            if (typeRepr != null){
                typeLabel = typeRepr.getAbbreviatedLabel();
            }
            if (isBlank(typeLabel) && typeRepr != null){
                typeLabel = typeRepr.getLabel();
            }
            if (isBlank(typeLabel) ){
                typeLabel = status.getSymbol();
            }
            if (isBlank(typeLabel)){
                typeLabel = status.getTitleCache();
            }
        }
        if (isBlank(typeLabel)){
            typeLabel = "->";
        }
        //concat
        String result = CdmUtils.concat(" ", new String[]{fromString, typeLabel, toStr});
        return result;
    }

    private static String getCache(HomotypicalGroup hg) {
        String result = "";
        for (TaxonName tnb : hg.getTypifiedNames()){
            result = CdmUtils.concat(", ", result, tnb.getTitleCache());
        }
        if (isBlank(result)){
            result = "No typified names";
        }
        return result;
    }

    private static String getCache(KeyStatement ks) {
        String result = "";
        LanguageString ls = ks.getPreferredLanguageString(Language.DEFAULT());
        if (ls != null && CdmUtils.isNotBlank(ls.getText())){
            result = ls.getText();
        }else{
            result = ks.toString();
        }
        return result;
    }

    private static String getCache(NomenclaturalStatus nomStatus) {
        String result = nomStatus.getName().getTitleCache();
        if (nomStatus.getType()!= null){
            Representation rep = nomStatus.getType().getPreferredRepresentation(Language.DEFAULT());
            if (rep != null){
                result = CdmUtils.concat(": ", rep.getAbbreviatedLabel(), result);
            }
        }
        return result;
    }


    private static boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    private static boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }

    //from taxeditor DescriptionHelper
    private static String getCache(DescriptionElementBase element,
            Language defaultLanguage) {

        String mainElementLabel= null;
        DescriptionBase<?> descr = element.getInDescription();
        descr = CdmBase.deproxy(descr);

        if (descr != null){
            if (descr.isInstanceOf(TaxonDescription.class)){
                Taxon taxon = CdmBase.deproxy(descr, TaxonDescription.class).getTaxon();
                if (taxon != null){
                    mainElementLabel = taxon.getTitleCache();
                }
            }else if (descr.isInstanceOf(SpecimenDescription.class)){
                SpecimenOrObservationBase<?> specimen = CdmBase.deproxy(descr, SpecimenDescription.class).getDescribedSpecimenOrObservation();
                if (specimen != null){
                    mainElementLabel = specimen.getTitleCache();
                }
            }else if (descr.isInstanceOf(TaxonNameDescription.class)){
                TaxonName name = CdmBase.deproxy(descr, TaxonNameDescription.class).getTaxonName();
                if (name != null){
                    mainElementLabel = name.getTitleCache();
                }
            }
        }

        String cache = null;
        if (element instanceof TextData) {
            //cache = ((TextData) element).getText(language);
            cache = "Text Data";
        }
        if (element instanceof CommonTaxonName) {
            cache = ((CommonTaxonName) element).getName();
        }
        if (element instanceof TaxonInteraction) {
            Taxon taxon2 = ((TaxonInteraction) element).getTaxon2();
            if(taxon2 != null && taxon2.getName() != null){
                cache = taxon2.getName().getTitleCache();
            }else{
                cache = "No taxon chosen";
            }
        }
        if (element instanceof Distribution) {
            Distribution distribution = (Distribution) element;

            NamedArea area = distribution.getArea();
            if(area != null){
                cache =  area.getLabel();

                PresenceAbsenceTerm status = distribution.getStatus();
                if (status == null){
                    cache += ", no status";
                }else {
                    cache += ", " + status.getLabel();
                }
            }
        }
        String result = cache == null ? "" : cache;
        if (isNotBlank(mainElementLabel)){
            result = CdmUtils.concat(" ", result, "(" + mainElementLabel + ")");
        }
        return result;
    }

}
