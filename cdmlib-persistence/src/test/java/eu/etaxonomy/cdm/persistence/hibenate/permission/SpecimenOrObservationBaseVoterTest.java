/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibenate.permission;

import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.access.AccessDecisionVoter;

import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.voter.SpecimenOrObservationBaseVoter;

/**
 * @author a.kohlbecker
 * @since 16.10.2017
 *
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

        int vote = voter.vote(authentication(
                new CdmAuthority(du, UPDATE)
                ),
                du,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE))
             );
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    @Test
    public void testSimplePerOriginalPermission(){

        DerivedUnit du1 = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        DerivedUnit du2 = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);

        FieldUnit fuA = FieldUnit.NewInstance();

        DerivationEvent.NewSimpleInstance(fuA, du1, null);
        DerivationEvent.NewSimpleInstance(du1, du2, null);

        int vote = voter.vote(authentication(
                    new CdmAuthority(fuA, UPDATE)
                ),
                du1,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE))
             );
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
    }

    public void testMultipleOriginalsGrantForCommonOriginal(){

        buildDerivationGraph();


        int vote = voter.vote(authentication(
                new CdmAuthority(duAB, UPDATE)
                ),
                du2,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE))
             );
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);

    }

    @Test
    public void testMultipleOriginalsGrantForOneRootOnly(){

        buildDerivationGraph();

        int vote = voter.vote(authentication(
                new CdmAuthority(fuA, UPDATE)
                ),
                du2,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE))
             );
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, vote);

    }

    @Test
    @Ignore // see https://dev.e-taxonomy.eu/redmine/issues/7020
    public void testMultipleOriginalsGrantForAllRoots(){

        buildDerivationGraph();

        int vote = voter.vote(authentication(
                new CdmAuthority(fuA, UPDATE),
                new CdmAuthority(fuB, UPDATE)
                ),
                du2,
                Arrays.asList(new CdmAuthority(CdmPermissionClass.SPECIMENOROBSERVATIONBASE, UPDATE))
             );
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, vote);
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
