/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxonGraph;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.persistence.hibernate.CdmPostDataChangeObservableListener;
import eu.etaxonomy.cdm.persistence.hibernate.TaxonGraphHibernateListener;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.kohlbecker
 * @since Oct 1, 2018
 *
 */
public class TaxonGraphHibernateListenerTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonGraphDao taxonGraphDao;

    @SpringBeanByType
    private TaxonGraphHibernateListener taxonGraphObserver;

    @SpringBeanByType
    private IReferenceDao referenceService;

    @SpringBeanByType
    private IDefinedTermDao termService;

    @SpringBeanByType
    private ITaxonNameDao nameService;

    @SpringBeanByType
    private ITaxonDao taxonService;


  static boolean isListener = false;

  @Before
  public void registerObserver() {
      if(!isListener){
          TaxonGraphHibernateListener listenerInstance = TaxonGraphHibernateListener.instance();
          listenerInstance.setTaxonGraphDao(taxonGraphDao);
          isListener = true;
      }
  }

  @Before
  public void setSecRef(){
      taxonGraphDao.setSecReferenceUUID(TaxonGraphTest.uuid_secRef);
  }

  // @Test
  // @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonGraphTest.xml")
  public void testnewTaxonName() throws TaxonGraphException{

      Reference refX = ReferenceFactory.newBook();
      refX.setTitleCache("Ref-X", true);

      TaxonName n_t_argentinensis = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(), "Trachelomonas", null, "argentinensis", null, null, refX, null, null);
      n_t_argentinensis = nameService.save(n_t_argentinensis);
      CdmPostDataChangeObservableListener.getDefault().delayedNotify();
      commitAndStartNewTransaction();

      Assert.assertTrue("a taxon should have been created", n_t_argentinensis.getTaxa().size() > 0);

      List<TaxonGraphEdgeDTO> edges = taxonGraphDao.edges(n_t_argentinensis, nameService.load(TaxonGraphTest.uuid_n_trachelomonas), true);
      Assert.assertEquals(1, edges.size());
      Assert.assertEquals(refX.getUuid(), edges.get(0).getCitationUuid());
  }

/**
 * {@inheritDoc}
 */
@Override
public void createTestDataSet() throws FileNotFoundException {
    // dataset created by TaxonGraphTest.createTestDataSet();

}


}
