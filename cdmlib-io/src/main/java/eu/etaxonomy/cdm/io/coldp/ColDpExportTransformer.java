/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.common.mapping.out.ExportTransformerBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * @author a.mueller
 * @date 17.07.2023
 */
public class ColDpExportTransformer extends ExportTransformerBase {

    private static final long serialVersionUID = -527652844010832994L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //maps to http://api.checklistbank.org/vocab/distributionstatus
    @Override
    public String getCacheByPresenceAbsenceTerm(PresenceAbsenceTerm status) throws UndefinedTransformerMethodException {
        if (status == null) {
            return null;
        }
        if (status.isAnyNative()) {
            return "native";
        }
        if (status.isAnyIntroduced()) {
            return "alien";
        }
        return "uncertain";
    }

    @SuppressWarnings("incomplete-switch")  //we ignore this warning as the enumeration should always be completely covered, otherwise an error will be shown if we do not add a default state. This is wanted behavior here.
    @Override
    public String getCacheByReferenceType(Reference ref) throws UndefinedTransformerMethodException {
        if (ref == null || ref.getType() == null) {
            return null;
        }
        ReferenceType refType = ref.getType();
        switch (refType) {
            case Article:
            case Journal:  //should not happen
                return "article";
            case Book:
            case PrintSeries:  //should not happen
                return "book";
            case BookSection:
                return "chapter";
            case CdDvd:
                return "dataset";  //TODO
            case Database:
                return "dataset";  //TODO
            case InProceedings:
                return "paper conference";
            case Map:
                return "map";
            case Patent:
                return "patent";
            case PersonalCommunication:
                return "personal communication";
            case Proceedings:
                return "paper conference";
            case Report:
                return "report";
            case Thesis:
                return "thesis";
            case WebPage:
                return "webpage";
            case Generic:
            case Section:
                if (ref.getInReference() != null) {
                    return getCacheByReferenceType(ref.getInReference());
                }else {
                    return null;
                }
        }
        return null;
    }

    @Override
    public String getCacheByNomStatus(NomenclaturalStatusType nomStatusType) {
        if (nomStatusType == null) {
            return null;
        //specific status
        }else if (nomStatusType.equals(NomenclaturalStatusType.CONSERVED())) {
            return "conserved";
        }else if (nomStatusType.equals(NomenclaturalStatusType.REJECTED())) {
            return "rejected";
        }else if (nomStatusType.equals(NomenclaturalStatusType.DOUBTFUL())) {
            return "doubtful";
        }else if (nomStatusType.equals(NomenclaturalStatusType.INED())) {
            return "manuscript";
        //general status
        }else if (nomStatusType.isLegitimate()) {
            return "acceptable";
        }else if (nomStatusType.isIllegitimate()) {
            return "nomen illegitimum";
        }else if (nomStatusType.isInvalid()) {
            return "not established";
        }

        return null;
    }

    @SuppressWarnings("incomplete-switch") //we ignore this warning as the enumeration should always be completely covered, otherwise an error will be shown if we do not add a default state. This is wanted behavior here.
    @Override
    public String getCacheByNomenclaturalCode(NomenclaturalCode nomenclaturalCode) throws UndefinedTransformerMethodException {
        if (nomenclaturalCode == null) {
            return null;
        }
        switch (nomenclaturalCode) {
            case NonViral:
                return null; //TODO
            case ICNAFP:
            case Fungi:
                return "ICN"; //"botanical";
            case ICNCP:
                return "ICNCP"; //"cultivars";
            case ICNP:
                return "ICNP"; //"bacterial";
            case ICVCN:
                return "ICVCN"; //"virus";
            case ICZN:
                return "ICZN"; //"zoological";
        }
        return null;
    }

    @Override
    public String getCacheByRank(Rank rank) throws UndefinedTransformerMethodException {
        if (rank == null) {
            return null;
        }
        Representation preferredRep = rank.getPreferredRepresentation(Language.ENGLISH());
        if (preferredRep != null) {
            return preferredRep.getLabel() == null ? null :preferredRep.getLabel().toLowerCase();
        }else {
            return rank.getTitleCache().toLowerCase();
        }
        //TODO maybe we still need to adapt some ranks
    }

    public static enum ColDpNameRelType{
        BASIONYM("basionym", 0),
        SPELLING_CORRECTION("spelling correction", 0),
        EMENDATION("spelling correction", 1),
        BASED_ON("based on", 0),
        REPLACEMENT_NAME("replacement name", 0),
        CONSERVED("conserved", 0),
        LATER_HOMONYM("later homonym", 0),
        SUPERFLUOUS("superfluous", 1),
        //TODO others

        ;
        private String label;
        private int direction;
        ColDpNameRelType(String label, int direction){
            this.label = label;
            this.direction = direction;
        }
        public String getLabel() {
            return label;
        }
        public int getDirection() {
            return direction;
        }
    }

    public ColDpNameRelType getColDpNameRelTypeByNameRelationType(NameRelationshipType nameRelType) {
        if (nameRelType == null) {
            return null;
        }
        if (nameRelType.getUuid().equals(NameRelationshipType.uuidBasionym)){
            return ColDpNameRelType.BASIONYM;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidOrthographicVariant)) {
            return ColDpNameRelType.SPELLING_CORRECTION;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidEmendation)) {
            return ColDpNameRelType.EMENDATION;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidValidatedByName)
                || nameRelType.getUuid().equals(NameRelationshipType.uuidLaterValidatedByName)) {
            return ColDpNameRelType.BASED_ON;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidReplacedSynonym)) {
            return ColDpNameRelType.REPLACEMENT_NAME;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidConservedAgainst)) {
            return ColDpNameRelType.CONSERVED;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidLaterHomonym)
                || nameRelType.getUuid().equals(NameRelationshipType.uuidTreatedAsLaterHomonym)) {
            return ColDpNameRelType.LATER_HOMONYM;
        } else if (nameRelType.getUuid().equals(NameRelationshipType.uuidLaterIsonym)
                //only for future merged, currently it will never match as the type is different
                || nameRelType.getUuid().equals(NomenclaturalStatusType.uuidSuperfluous)) {
            return ColDpNameRelType.SUPERFLUOUS;
        } else {
            //TODO misspelling, alternative name, blocking name for, avoids homonym of, unspecific "non"
            String warning = "Name relationship type not yet handled by COL-DP: " + nameRelType.getTitleCache();
            //TODO handle warning
            Representation preferredRep = nameRelType.getPreferredRepresentation(Language.ENGLISH());
//            if (preferredRep != null) {
//               return preferredRep.getLabel() == null ? null :preferredRep.getLabel().toLowerCase();
//            }else {
//              return nameRelType.getTitleCache().toLowerCase();
//            }
            return null;
        }
    }
}