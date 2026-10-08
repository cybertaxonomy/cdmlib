/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibenate.permission;

import java.util.EnumSet;

import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.TargetEntityStates;
import eu.etaxonomy.cdm.persistence.permission.voter.CdmVote;
import eu.etaxonomy.cdm.persistence.permission.voter.SpecimenOrObservationBaseVoter;

/**
 * @author a.kohlbecker
 * @since 16.10.2017
 */
public class SpecimenOrObservationBaseVoterTest extends AbstractCdmPermissionVoterTest {

    private static final EnumSet<CRUD> UPDATE = EnumSet.of(CRUD.UPDATE);

    private SpecimenOrObservationBaseVoter voter = new SpecimenOrObservationBaseVoter();

    private FieldUnit fuA;

    private FieldUnit fuB;

    private DerivedUnit duA;

    private DerivedUnit duB;

    private DerivedUnit duAB;

    private DerivedUnit du2;

    @Test
    public void testSimplePerEntityPermission(){

        DerivedUnit du = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        CdmVote vote = voter.vote(authentication(
                new CdmAuthority(du, UPDATE)
                ),
                new TargetEntityStates(du),
                new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE)
             );
        assertEquals(CdmVote.GRANTED, vote);
    }

    @Test
    public void testSimplePerOriginalPermission(){

        DerivedUnit du1 = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        DerivedUnit du2 = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        FieldUnit fuA = FieldUnit.NewInstance();

        DerivationEvent.NewSimpleInstance(fuA, du1, null);
        DerivationEvent.NewSimpleInstance(du1, du2, null);

        CdmVote vote = voter.vote(authentication(
                    new CdmAuthority(fuA, UPDATE)
                ),
                new TargetEntityStates(du1),
                new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE)
             );
        assertEquals(CdmVote.GRANTED, vote);
    }

    public void testMultipleOriginalsGrantForCommonOriginal(){

        buildDerivationGraph();


        CdmVote vote = voter.vote(authentication(
                new CdmAuthority(duAB, UPDATE)
                ),
                new TargetEntityStates(du2),
                new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE)
             );
        assertEquals(CdmVote.GRANTED, vote);

    }

    @Test
    public void testMultipleOriginalsGrantForOneRootOnly(){

        buildDerivationGraph();

        CdmVote vote = voter.vote(authentication(
                new CdmAuthority(fuA, UPDATE)
                ),
                new TargetEntityStates(du2),
                new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE)
             );
        assertEquals(CdmVote.DENIED, vote);

    }

    @Test
    @Ignore // see https://dev.e-taxonomy.eu/redmine/issues/7020
    public void testMultipleOriginalsGrantForAllRoots(){

        buildDerivationGraph();

        CdmVote vote = voter.vote(authentication(
                new CdmAuthority(fuA, UPDATE),
                new CdmAuthority(fuB, UPDATE)
                ),
                new TargetEntityStates(du2),
                new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE)
             );
        assertEquals(CdmVote.GRANTED, vote);
    }


    /**
     *  Builds a derivation graph having two roots.
     *
     <pre>
        fuA -- duA
                   \
                    duAB -- du2
                   /
        fuB -- duB
     </pre>
     *
     */
    protected void buildDerivationGraph() {

        fuA = FieldUnit.NewInstance();
        fuB = FieldUnit.NewInstance();

        duA = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
        duB = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        duAB = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        du2 = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        DerivationEvent.NewSimpleInstance(fuA, duA, null);
        DerivationEvent.NewSimpleInstance(fuB, duB, null);

        DerivationEvent groupingEvent = DerivationEvent.NewInstance(DerivationEventType.GROUPING());
        groupingEvent.addOriginal(duA);
        groupingEvent.addOriginal(duB);
        groupingEvent.addDerivative(duAB);

        DerivationEvent.NewSimpleInstance(duAB, du2, null);
    }

}
