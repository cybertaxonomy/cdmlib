/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author a.mueller
 */
public class ValidPointTest extends ValidationTestBase{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ValidPointTest.class);

	private GatheringEvent gatheringEvent;
	private Point point = new Point();

	@Before
	public void setUp() {
		gatheringEvent = GatheringEvent.NewInstance();

		Set<ConstraintViolation<GatheringEvent>> constraintViolations  = validator.validate(gatheringEvent, Level2.class);
        assertTrue("There should not be a constraint violation as gathering with NO exact location is allowed",constraintViolations.isEmpty());
	}

/****************** TESTS *****************************/

	@Test
	public final void testNoPoint() {

        Set<ConstraintViolation<GatheringEvent>> constraintViolations  = validator.validate(gatheringEvent, Level2.class);
        assertTrue("There should not be a constraint violation as gathering with NO exact location is allowed",constraintViolations.isEmpty());
 	}

	@Test
    public final void testNullLongitude() {
	    point.setLatitude(33.3d);
	    point.setLongitude(null);
	    validateHasConstraint(point, NotNullValidator.class, Level2.class);
	    gatheringEvent.setExactLocation(point);
	    validateHasConstraint(gatheringEvent, NotNullValidator.class, Level2.class);

	    point.setLongitude(22.2d);
	    validateHasNoConstraint(point, NotNullValidator.class, Level2.class);
	    validateHasNoConstraint(gatheringEvent, NotNullValidator.class, Level2.class);
   }
}