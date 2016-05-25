
package eu.etaxonomy.cdm.persistence.dao;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class MethodCacheImplTest {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MethodCacheImplTest.class);
    private Taxon taxon;
    private BotanicalName botanicalName;

    IMethodCache methodCache;
    private Reference nomenclaturalReference;

    @Before
    public void setUp() throws Exception {
        methodCache = new MethodCacheImpl();
        taxon = Taxon.NewInstance(null, null);
        botanicalName = BotanicalName.NewInstance(null);
        nomenclaturalReference = ReferenceFactory.newBook();
    }

    /**
     * Test for a method that is not declared by the class, but by the superclass
     * also the parameter of the matching method is more generic than the given parameter
     */
    @Test
    public void testGetMethod() {
        Method method = methodCache.getMethod(taxon.getClass(), "setName", botanicalName.getClass());
        assertNotNull("Method should exist", method);
    }

    @Test
    public void testGetMethodWhereMethodParameterIsInterface(){
        Method method = methodCache.getMethod(botanicalName.getClass(), "setNomenclaturalReference", nomenclaturalReference.getClass());
        assertNotNull("Method should exist", method);
    }

}
