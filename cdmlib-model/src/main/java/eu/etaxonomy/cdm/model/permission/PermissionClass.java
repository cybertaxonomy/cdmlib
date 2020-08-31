/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.permission;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.CdmBaseType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.EnumeratedTermVoc;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * see also {@link CdmBaseType}
 *
 * @author k.luther
 * @author a.kohlbecker
 * @author a.mueller
 * @since 06.07.2011
 */
public enum PermissionClass implements IEnumTerm<PermissionClass>{
    ALL("All"),
    AGENTBASE("Agent"),
    ANNOTATION("Annotation"),
    ANNOTATIONTYPE("AnnotationType"),
    CDMMETADATA("CdmMetaData"),
    CLASSIFICATION("Classification"),
    COLLECTION("Collectioni"),
    DEFINEDTERM("DefinedTerm"),
    DESCRIPTIONBASE("Description"),
    DESCRIPTIONELEMENTBASE("DescriptionElement"),
    DESCRIPTIONELEMENTSOURCE("DescriptionElementSource"),
    DESCRIPTIVEDATASET("DescriptiveDataSet"),
    EXTENSION("Extension"),
    EXTENSIONTYPE("ExtensionType"),
    FEATURE("Feature"),
    TERMNODE("TermNode"),
    TERMTREE("TermTree"),
    GATHERINGEVENT("GatheringEvent"),
    GRANTEDAUTHORITYIMPL("GrantedAuthorityImpl"),
    GROUP("Group"),
    HOMOTYPICALGROUP("HomotypicalGroup"),
    HYBRIDRELATIONSHIP("HybridRelationship"),
    IDENTIFIABLESOURCE("IdentifiableSource"),
    IDENTIFIER("Identifier"),
    KEYSTATEMENT("KeyStatement"),
    LANGUAGE("Language"),
    LANGUAGESTRING("LanguageString"),
    MARKER("Marker"),
    MARKERTYPE("MarkerType"),
    MEDIA("Media"),
    MEDIAREPRESENTATION("MediaRepresentation"),
    MEDIAREPRESENTATIONPART("MediaRepresentationPart"),
    NAMEDAREA("NamedArea"),
    NAMEDAREALEVEL("NamedAreaLevel"),
    NAMERELATIONSHIP("NameRelationship"),
    NAMETYPEDESIGNATION("NameTypeDesignation"),
    NOMENCLATURALSTATUS("NomenclaturalStatus"),
    NOMENCLATURALSTATUSTYPE("NomenclaturalStatusType"),
    PRESENCEABSENCETERM("PresenceAbsenceTerm"),
    POLYTOMOUSKEY("PolytomousKey"),
    POLYTOMOUSKEYNODE("PolytomousKeyNode"),
    RANK("Rank"),
    REFERENCE("Reference"),
    REGISTRATION("Registration"),
    REPRESENTATION("Representation"),
    SPECIMENOROBSERVATIONBASE("SpecimenOrObservation"),
    SPECIMENTYPEDESIGNATION("SpecimenTypeDesignation"),
    STATEDATA("StateData"),
    STATISTICALMEASUREMENTVALUE("StatisticalMeasurementValue"),
    TAXONBASE("TaxonBase"),
    TAXONNAME("TaxonName"),
    TAXONNODE("TaxonNode"),
    TAXONRELATIONSHIP("TaxonRelationship"),
    TEAMORPERSONBASE("TeamOrPerson"),
    TERMVOCABULARY("TermVocabulary"),
    USER("User"),
    ;

    public static final Logger logger = Logger.getLogger(PermissionClass.class);

    private PermissionClass(String key){
        //we have no UUIDs defined yet, but needed for tests
        this(UUID.randomUUID(), key, key, null);
    }


    private PermissionClass(UUID uuid, String defaultString, String key){
        this(uuid, defaultString, key, null);
    }

    private PermissionClass(UUID uuid, String defaultString, String key, PermissionClass parent){
        delegateVocTerm = EnumeratedTermVoc.addTerm(getClass(), this, uuid, defaultString, key, parent);
    }

 // *************************** DELEGATE **************************************/

    private static EnumeratedTermVoc<PermissionClass> delegateVoc;
    private IEnumTerm<PermissionClass> delegateVocTerm;

    static {
        delegateVoc = EnumeratedTermVoc.getVoc(PermissionClass.class);
    }

    @Override
    public String getKey(){return delegateVocTerm.getKey();}

    @Override
    public String getMessage(){return delegateVocTerm.getMessage();}

    @Override
    public String getMessage(eu.etaxonomy.cdm.model.common.Language language){return delegateVocTerm.getMessage(language);}

    @Override
    public UUID getUuid() {return delegateVocTerm.getUuid();}

    @Override
    public PermissionClass getKindOf() {return delegateVocTerm.getKindOf();}

    @Override
    public Set<PermissionClass> getGeneralizationOf() {return delegateVocTerm.getGeneralizationOf();}

    @Override
    public boolean isKindOf(PermissionClass ancestor) {return delegateVocTerm.isKindOf(ancestor);  }

    @Override
    public Set<PermissionClass> getGeneralizationOf(boolean recursive) {return delegateVocTerm.getGeneralizationOf(recursive);}

    public static PermissionClass getByKey(String key){return delegateVoc.getByKey(key);}
    public static PermissionClass getByUuid(UUID uuid) {return delegateVoc.getByUuid(uuid);}

// ************************ METHODS *******************************/

    /**
     * return the appropriate CdmPermissionClass for the given Object
     *
     * @param cdmBase
     * @return the CdmPermissionClass or null
     */
    public static PermissionClass getValueOf(CdmBase cdmBase){
        return PermissionClass.getValueOf(cdmBase.getClass());
    }

    /**
     * Return the appropriate CdmPermissionClass for the given Object
     *
     * @param clazz
     * @return the CdmPermissionClass or null
     */
    public static PermissionClass getValueOf(Class clazz){

        PermissionClass permissionClass = doValueOf(clazz);
        if(permissionClass == null) {
            logger.error("Permission class support for " + clazz + " not implemented");
        }
        return permissionClass;
    }

    /**
     * @param o
     * @return
     */
    private static PermissionClass doValueOf(Class<?> clazz) {
        try{
            String normalizedName = clazz.getSimpleName().toUpperCase();
            return PermissionClass.valueOf(normalizedName);
        } catch(IllegalArgumentException e){
            if (CdmBase.class.isAssignableFrom(clazz)){
                return doValueOf(clazz.getSuperclass());
            }

        }
        return null;
    }

}
