// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;


/**
 * @author pplitzner
 * @date Sep 8, 2014
 *
 */
public class SaveCdmEntitiesTest {

    private void handleNonCascadedElements(SpecimenOrObservationBase<?> specimen, ConversationHolder conversation, CdmApplicationController applicationController){
        //services
        ITermService termService = applicationController.getTermService();
        IReferenceService referenceService = applicationController.getReferenceService();
        INameService nameService = applicationController.getNameService();
        
        //scan SpecimenOrObservationBase
        for(DeterminationEvent determinationEvent:specimen.getDeterminations()){
            DefinedTerm modifier = determinationEvent.getModifier();
            if(termService.find(modifier.getUuid())==null){
                
            }
        }
    }
    /*
     * SOOB
    -DescriptionBase?? Wie instantiieren?
    -determinations
    --modifier TERM
    --setOfReferences CDM
    -kindOfUnit TERM
    -lifeStage TERM
    - sex TERM

    FieldUnit
    -Country TERM
    -CollectingAreas TERM

    DerivedUnit
    -collection
    --institute
    ---types TERM
    -preservationMethod
    --medium TERM
    -storedUnder CDM TaxonNameBase (überhaupt neu anlegen?)
    */

    @Test
    public void testSaveSpecimen(){
        DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
        ICdmDataSource datasource = CdmDataSource.NewMySqlInstance("localhost", "test_db", "root", "root");
        CdmApplicationController applicationController = CdmApplicationController.NewInstance(datasource, dbSchemaValidation);

        ConversationHolder conversation = applicationController.NewConversation();
        conversation.startTransaction();
        conversation.bind();

        IOccurrenceService occurrenceService = applicationController.getOccurrenceService();
        for(int i=0;i<10;i++){
            DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
            derivedUnitFacade.setAccessionNumber(Double.toString(Math.random()));
            derivedUnitFacade.setCountry(NamedArea.AFRICA());
            derivedUnitFacade.addSource(OriginalSourceType.Unknown, ReferenceFactory.newArticle(), "microReference", "originalNameString");

            //save specimen
            occurrenceService.saveOrUpdate(derivedUnitFacade.innerDerivedUnit());
            conversation.commit();

          //Collection
            Collection collection = Collection.NewInstance();
            Collection subCollection = Collection.NewInstance();
            subCollection.setSuperCollection(collection);

            collection.setCode("coll code");
            collection.setCodeStandard("codeStandard");
            collection.setName("coll name");
            collection.setTownOrLocation("townOrLocation");
            Institution institution = Institution.NewInstance();
            collection.setInstitute(institution);

            //FieldUnit
            FieldUnit fieldUnit = FieldUnit.NewInstance();
            fieldUnit.setFieldNumber("fieldNumber");
            fieldUnit.setFieldNotes("fieldNotes");
            Person primaryCollector = Person.NewInstance();
            primaryCollector.setLifespan(TimePeriod.NewInstance(2014));
            fieldUnit.setPrimaryCollector(primaryCollector);

            GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
            fieldUnit.setGatheringEvent(gatheringEvent);
            gatheringEvent.putLocality(Language.ENGLISH(), "locality");
            gatheringEvent.setExactLocation(Point.NewInstance(22.4, -34.2,
                    ReferenceSystem.WGS84(), 33));
            gatheringEvent.setCountry(Country.GERMANY());
            gatheringEvent.addCollectingArea(NamedArea.EUROPE());
            gatheringEvent.setCollectingMethod("collectingMethod");
            gatheringEvent.setAbsoluteElevation(10);
            gatheringEvent.setAbsoluteElevationMax(100);
            gatheringEvent.setAbsoluteElevationText("elevation text");

            gatheringEvent.setDistanceToGround(10.4);
            gatheringEvent.setDistanceToGroundMax(100.3);
            gatheringEvent.setDistanceToGroundText("distance to ground text");

            gatheringEvent.setDistanceToWaterSurface(10.4);
            gatheringEvent.setDistanceToWaterSurfaceMax(100.3);
            gatheringEvent.setDistanceToWaterSurfaceText("distance to water text");


            //Derived Unit
            MediaSpecimen mediaSpecimen = MediaSpecimen.NewInstance(SpecimenOrObservationType.StillImage);
            mediaSpecimen.setCollection(collection);
            mediaSpecimen.setCatalogNumber("catalogNumber");
            mediaSpecimen.setAccessionNumber("accessionNumber");
//          mediaSpecimen.setCollectorsNumber("collectorsNumber");
            mediaSpecimen.setBarcode("barcode");
            BotanicalName storedUnder = BotanicalName.NewInstance(Rank.SPECIES());
            storedUnder.setTitleCache("Stored under", true);
            mediaSpecimen.setStoredUnder(storedUnder);
            mediaSpecimen.setExsiccatum("exsiccatum");
            PreservationMethod preservation = PreservationMethod.NewInstance(null, "My preservation");
            preservation.setTemperature(22.4);
            mediaSpecimen.setPreservation(preservation);

            //DerivationEvent
            DerivationEvent event = DerivationEvent.NewInstance(DerivationEventType.ACCESSIONING());
            event.addOriginal(fieldUnit);
            event.addDerivative(mediaSpecimen);


            //SpecOrObservationBase
            fieldUnit.setSex(DefinedTerm.SEX_FEMALE());
//            fieldUnit.setLifeStage(DefinedTerm.NewStageInstance("Live stage", "stage", null));
//            fieldUnit.setKindOfUnit(DefinedTerm.NewKindOfUnitInstance("Kind of unit", "Kind of unit", null));
            fieldUnit.setIndividualCount(3);
            fieldUnit.putDefinition(Language.ENGLISH(), "definition");
            fieldUnit.setPublish(true);

            //Determination
            DeterminationEvent determinationEvent = DeterminationEvent.NewInstance(getTaxon(), mediaSpecimen);
            determinationEvent.setModifier(DefinedTerm.DETERMINATION_MODIFIER_AFFINIS());
            determinationEvent.setPreferredFlag(true);
            Reference<?> reference = getReference();
            applicationController.getReferenceService().saveOrUpdate(reference);
            determinationEvent.addReference(reference);


            //save specimen
            occurrenceService.saveOrUpdate(fieldUnit);
            try{
                conversation.commit();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        assertEquals("Incorrect number of DerivedUnits in DB", 20, occurrenceService.getDerivedUnitUuidAndTitleCache().size());

    }
    private Reference<?> getReference() {
        Reference<?> result = ReferenceFactory.newGeneric();
        result.setTitle("some generic reference");
        return result;
   }

   private Taxon getTaxon() {
       Reference<?> sec = getReference();
       TaxonNameBase<?,?> name = BotanicalName.NewInstance(Rank.GENUS());
       Taxon taxon = Taxon.NewInstance(name, sec);
       return taxon;

   }

}
