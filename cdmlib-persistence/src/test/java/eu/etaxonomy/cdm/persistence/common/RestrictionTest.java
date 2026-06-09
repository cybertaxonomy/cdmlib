/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.common;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.filter.Restriction;
import eu.etaxonomy.cdm.api.filter.Restriction.Operator;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author muellera
 * @since 18.05.2026
 */
public class RestrictionTest extends CdmTransactionalIntegrationTest {


    @SpringBeanByType
    private IReferenceDao referenceDao;

    @Test
    public void testOrTitleAndEnumSet() {

        Reference book = ReferenceFactory.newBook();
        book.setTitle("My Reference");
        referenceDao.save(book);
        Reference journal = ReferenceFactory.newJournal();
        journal.setTitle("My Journal");
        referenceDao.save(journal);
        commitAndStartNewTransaction();

        List<Restriction<?>> restrictions = new ArrayList<>();
        restrictions.add(new Restriction<>("title", Operator.OR, MatchMode.ANYWHERE, "Journal"));
        List<Reference> references = referenceDao.findByTitleWithRestrictions(
                Reference.class, "My Reference", MatchMode.BEGINNING,
                restrictions,
                null, null,
                null, null);
        assertEquals("countMembers should return 2", 2, references.size());

        restrictions.add(new Restriction<>("type", Operator.AND, null, EnumSet.of(ReferenceType.Section, ReferenceType.Journal)));
        references = referenceDao.findByTitleWithRestrictions(
                Reference.class, "My Reference", MatchMode.BEGINNING,
                restrictions,
                null, null,
                null, null);
        assertEquals("Filtered references size should be 1", 1, references.size());

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
