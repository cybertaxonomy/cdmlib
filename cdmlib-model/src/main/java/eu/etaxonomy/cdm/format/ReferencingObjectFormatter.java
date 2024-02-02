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
import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.description.CategoricalDataFormatter;
import eu.etaxonomy.cdm.format.description.DescriptionElementFormatter;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.format.occurrences.DistanceStringFormatter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.IDescribable;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TemporalData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Registration;
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
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.SecundumSource;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author a.mueller
 * @date 19.03.2021
 */
public class ReferencingObjectFormatter {

    public static String format(CdmBase element, Language defaultLanguage) {
        return format(element, null, defaultLanguage);
    }

    public static String format(CdmBase element, String target, Language defaultLanguage) {

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
            }else if (originalSource instanceof IdentifiableSource && isNotBlank(target) ){
                sourceObjectTitle = "for " + target;
            }else if (originalSource instanceof NamedSource && isNotBlank(target) ){
                sourceObjectTitle = "for " + target;
            }
            resultString = CdmUtils.concat("; ", new String[]{originalSource.getIdNamespace(), originalSource.getIdInSource(), sourceObjectTitle, originalSource.getCitationMicroReference()});
        }else if (element instanceof Rights) {
            Rights rights = (Rights) element;
            resultString = getCache(rights, defaultLanguage);
        }else if (element instanceof LanguageStringBase) {
            resultString = ((LanguageStringBase) element).getText();
        }else if (element instanceof DescriptionElementBase) {
            resultString = getCache((DescriptionElementBase) element, defaultLanguage);
        }else if (element instanceof StateData) {
            resultString = getCache((StateData) element, defaultLanguage);
        }else if (element instanceof StatisticalMeasurementValue) {
            resultString = getCache((StatisticalMeasurementValue) element, defaultLanguage);
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
        }else if (element instanceof TermNode){
            resultString = getCache((TermNode<?>) element);
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
        }else if (element instanceof Registration) {
            Registration registration = (Registration) element;
            resultString = registration.getIdentifier();
        }else{
            // TODO write return texts for HomotypicalGroup, etc.
            resultString = element.toString();
        }

        if (StringUtils.isEmpty(resultString)){
            resultString = element.toString();
        }
        return resultString;
    }

    private static String getCache(Rights rights, Language defaultLanguage) {
        // TODO make it a rights formatter
        String result = "";
        if (rights.getType() != null) {
            RightsType type = rights.getType();
            String typeStr = getIdInVocOrSymbol(type);
            if (typeStr == null) {
                Representation rep = type.getPreferredRepresentation(defaultLanguage);
                if (rep != null) {
                    typeStr = rep.getAbbreviatedLabel();
                    if (isBlank(typeStr)) {
                        typeStr = rep.getLabel();
                    }
                }
            }
            if (isBlank(typeStr)) {
                typeStr = type.getTitleCache();
            }
            result = typeStr;
        }
        result = CdmUtils.concat(" ", result, rights.getAbbreviatedText());
        if (rights.getAgent() != null) {
            result = CdmUtils.concat(" ", result, rights.getAgent().getTitleCache());
        }
        //TODO always show but truncate at 10
        if (isBlank(result)) {
            result = rights.getText();
        }
        if (isBlank(result)) {
            result = rights.getUri() == null ? null : rights.getUri().toString();
        }
        if (isBlank(result)) {
            result = rights.toString();
        }

        return result;
    }

    private static String getIdInVocOrSymbol(DefinedTermBase<?> term) {
        if (isNotBlank(term.getIdInVocabulary())) {
            return term.getIdInVocabulary();
        }else if (isNotBlank(term.getSymbol())) {
            return term.getSymbol();
        }else if (isNotBlank(term.getSymbol2())) {
            return term.getSymbol2();
        }
        return null;
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
        Taxon taxon = taxonNode.getTaxon();
        if (taxon != null){
            result = taxon.getName() != null ? taxon.getName().getTitleCache() : taxon.getTitleCache();
        }

        final String invisible = "Invisible root of ";
        String parentStr;
        TaxonNode parentNode = taxonNode.getParent();
        if (parentNode == null){
            parentStr = invisible;
        }else{
            Taxon parentTaxon = parentNode.getTaxon();
            if (parentTaxon == null){
                parentStr = " (root of ";
            }else{
                TaxonName parentName = parentTaxon.getName();
                if (parentName == null){
                    parentStr = " (child of " + parentTaxon.getTitleCache();
                }else{
                    parentStr = " (child of " + parentName.getTitleCache();
                }
                parentStr += " in ";
            }
        }

        //classification
        Classification classification = taxonNode.getClassification();
        String classificationStr ;
        if (classification != null){
            classificationStr = classification.getName() == null ? "classification:"+classification.getId() : classification.getName().getText();
            if (isBlank(classificationStr)){
                classificationStr = classification.toString();
            }
        }else{
            classificationStr = "-no classification-"; //should not happen
        }

        result = CdmUtils.concat("", parentStr, classificationStr, parentStr == invisible? "" : ")");

        return result;
    }

    private static String getCache(TermNode<?> termNode) {
        String result = "";
        DefinedTermBase<?> term = termNode.getTerm();
        if (term != null){
            Representation prefRep = term.getPreferredRepresentation(Language.DEFAULT());
            if (prefRep != null) {
                result = CdmUtils.Nz(prefRep.getLabel());
            }
        }

        final String invisible = "Invisible root of ";
        String parentStr;
        TermNode<?> parentNode = termNode.getParent();
        if (parentNode == null){
            parentStr = invisible;
        }else{
            DefinedTermBase<?> parentTerm = parentNode.getTerm();
            if (parentTerm == null){
                parentStr = " (root of ";
            }else{
                Representation prefParentRep = term.getPreferredRepresentation(Language.DEFAULT());
                parentStr = " (child of " + (CdmUtils.Nz(prefParentRep.getLabel()));
                parentStr += " in ";
            }
        }

        //classification
        TermTree<?> tree = termNode.getGraph();
        String treeStr ;
        if (tree != null){
            Representation prefRep = tree.getPreferredRepresentation(Language.DEFAULT());
            String prefRepStr = prefRep == null? null : CdmUtils.Ne(prefRep.getLabel());
            treeStr = (prefRepStr == null) ? "tree:" + tree.getId() : prefRepStr;
            if (isBlank(treeStr)){
                treeStr = tree.toString();
            }
        }else{
            treeStr = "-no classification-"; //should not happen
        }

        result = CdmUtils.concat("", parentStr, treeStr, parentStr == invisible? "" : ")");

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

        DescriptionBase<?> descr = element.getInDescription();
        descr = CdmBase.deproxy(descr);

        String mainElementLabel = mainElementLabel(descr);

        String cache = null;
        //TextData
        if (element instanceof TextData) {
            LanguageString text = ((TextData) element).getPreferredLanguageString(defaultLanguage);
            if (text != null) {
                cache = text.getText();
            }
            cache = cache == null || isBlank(cache)? "empty" : StringUtils.truncate(cache, 20);
        //CommonTaxonName
        }else  if (element instanceof CommonTaxonName) {
            cache = ((CommonTaxonName) element).getName();
        //TaxonInteraction
        }else if (element instanceof TaxonInteraction) {
            Taxon taxon2 = ((TaxonInteraction) element).getTaxon2();
            if(taxon2 != null && taxon2.getName() != null){
                cache = taxon2.getName().getTitleCache();
            }else{
                cache = "No taxon chosen";
            }
         //IndividualsAssociation
        }else if (element instanceof IndividualsAssociation) {
            SpecimenOrObservationBase<?> unit = ((IndividualsAssociation) element).getAssociatedSpecimenOrObservation();
            if(unit != null){
                cache = unit.getIdentityCache();
                if (isBlank(cache)) {
                    cache = unit.getTitleCache();
                }
            }else{
                cache = "No unit chosen";
            }
        //Distribution
        }else if (element instanceof Distribution) {
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
        //CategoricalData
        }else if (element instanceof CategoricalData) {
            CategoricalData categoricalData = (CategoricalData) element;

            cache = CategoricalDataFormatter.NewInstance(new FormatKey[] {}).format(categoricalData);
        //QuantitativeData
        }else if (element instanceof QuantitativeData) {
            QuantitativeData quantitativeData = (QuantitativeData) element;

            cache = QuantitativeDataFormatter.NewInstance(new FormatKey[] {}).format(quantitativeData);
        //CategoricalData
        }else if (element instanceof TemporalData) {
            TemporalData temporalData = (TemporalData) element;
            cache = temporalData.toString();
        }

        String result = cache == null ? "" : cache;
        result = concatWithMainElement(mainElementLabel, result);
        return result;
    }

    private static String getCache(StateData stateData,
            Language defaultLanguage) {
        String cache = null;
        if (stateData.getState() != null) {
            Representation rep = stateData.getState().getPreferredRepresentation(defaultLanguage);
            if (rep != null) {
                cache = rep.getLabel();
            }
        }
        cache = isBlank(cache)? stateData.getUuid().toString() : cache;
        DescriptionBase<?> desc = stateData.getCategoricalData() == null? null : stateData.getCategoricalData().getInDescription();
        String mainElementLabel = mainElementLabel(desc);
        return concatWithMainElement(mainElementLabel, cache);
    }

    private static String getCache(StatisticalMeasurementValue smv, Language defaultLanguage) {
        String cache = null;
        if (smv.getType() != null) {
            Representation rep = smv.getType().getPreferredRepresentation(defaultLanguage);
            if (rep != null) {
                cache = rep.getLabel();
            }
        }
        if (smv.getValue() != null) {
            cache = CdmUtils.concat("=", cache, smv.getValue().toString());
        }
        cache = isBlank(cache)? smv.getUuid().toString() : cache;
        DescriptionBase<?> desc = smv.getQuantitativeData() == null? null : smv.getQuantitativeData().getInDescription();
        String mainElementLabel = mainElementLabel(desc);
        return concatWithMainElement(mainElementLabel, cache);
    }

    /**
     * Returns the label of the main element (taxon, specimen or name) for
     * the given description.
     */
    private static String mainElementLabel(DescriptionBase<?> descr) {
        String mainElementLabel = null;
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
        return mainElementLabel;
    }

    private static String concatWithMainElement(String mainElementLabel, String result) {
        if (isNotBlank(mainElementLabel)){
            result = CdmUtils.concat(" ", result, "(" + mainElementLabel + ")");
        }
        return result;
    }
}
