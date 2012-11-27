/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd.in;

import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.unitils.spring.annotation.SpringBeanByType;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author b.clark
 * @created 20.01.2009
 * @version 1.0
 */


public class SDDImportBenchmark extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    SDDImport sddImport;

    @SpringBeanByType
    INameService nameService;

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    private SDDImportConfigurator configurator;

    @Before
    public void setUp() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/Cichorieae-DA.sdd.xml");
        Assert.assertNotNull(url);
        configurator = SDDImportConfigurator.NewInstance(url.toURI(), null);
    }

    @BenchmarkOptions(benchmarkRounds = 1, warmupRounds = 0)
    @Test
    public void testInit() {
        assertNotNull("sddImport should not be null", sddImport);
        assertNotNull("nameService should not be null", nameService);
    }

    @BenchmarkOptions(benchmarkRounds = 2, warmupRounds = 0)
    @Test
    public void testDoInvoke() {
        sddImport.doInvoke(new SDDImportState(configurator));
        this.setComplete();
        this.endTransaction();
    }

}
