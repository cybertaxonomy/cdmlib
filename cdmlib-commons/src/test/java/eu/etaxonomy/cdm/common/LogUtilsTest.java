// $Id$
/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author a.mueller
 * @date 22.07.2022
 *
 */
public class LogUtilsTest {

    private static final Logger logger = LogManager.getLogger();

    private static final String LOGGER = "MyTestLogger";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        Logger logger = LogManager.getLogger(LOGGER);
        Assert.assertEquals(Level.ERROR, logger.getLevel());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetLevel() {
        Logger logger = LogManager.getLogger(LOGGER);
        Level levelBefore = LogManager.getLogger(LOGGER).getLevel();
        Assert.assertEquals("Logger does not have the expected default level", Level.ERROR, levelBefore);

        LogUtils.setLevel(logger, Level.DEBUG);
        Assert.assertEquals(Level.DEBUG, logger.getLevel());
        Assert.assertSame(logger, LogManager.getLogger(LOGGER));

        LogUtils.setLevel(LOGGER, Level.TRACE);
        Assert.assertEquals(Level.TRACE, logger.getLevel());
        Assert.assertSame(logger, LogManager.getLogger(LOGGER));

    }

    @Test
    public void testLogAsTrace() {
        Logger traceLogger = LogManager.getLogger(LogUtils.TRACE);
        if (traceLogger.getLevel().equals(Level.TRACE)) {
            logger.error("Level is already trace. LogAsTrace can not be tested!!");
        }else {
            LogUtils.logAsTrace(getClass(), "TRACE test");
            Assert.assertEquals(Level.TRACE, traceLogger.getLevel());
        }
    }

    @Test
    public void testLogAsDebug() {
        Logger debugLogger = LogManager.getLogger(LogUtils.DEBUG);
        if (debugLogger.getLevel().equals(Level.DEBUG)) {
            logger.error("Level is already trace. LogAsDebug can not be tested!!");
        }else {
            LogUtils.logAsDebug(getClass(), "DEBUG test");
            Assert.assertEquals(Level.DEBUG, debugLogger.getLevel());
        }
    }

    @Test
    public void testLogAsInfo() {
        Logger infoLogger = LogManager.getLogger(LogUtils.INFO);
        if (infoLogger.getLevel().equals(Level.INFO)) {
            logger.error("Level is already trace. LogAsInfo can not be tested!!");
        }else {
            LogUtils.logAsInfo(getClass(), "INFO test");
            Assert.assertEquals(Level.INFO, infoLogger.getLevel());
        }
    }

    @Test
    public void testLogAsWarn() {
        Logger warnLogger = LogManager.getLogger(LogUtils.WARN);
        if (warnLogger.getLevel().equals(Level.WARN)) {
            logger.error("Level is already trace. LogAsWarn can not be tested!!");
        }else {
            LogUtils.logAsWarn(getClass(), "WARN test");
            Assert.assertEquals(Level.WARN, warnLogger.getLevel());
        }
    }

    @Test
    public void testLogWithTrace() {
        Logger loggerWithTrace = LogManager.getLogger(LOGGER + "."+ LogUtils.TRACE);
        LogUtils.setLevel(loggerWithTrace, Level.TRACE);
        LogUtils.logWithTrace(LogManager.getLogger(LOGGER), "with TRACE test");
        //only manually tested in Console
    }

}
