/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import jakarta.validation.ConstraintViolation;

/**
 * @author a.mueller
 */
public class ValidPointTest extends ValidationTestBase{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	private GatheringEvent gatheringEvent;
	private Point point = new Point();

	@BeforeEach
	public void setUp() {
		gatheringEvent = GatheringEvent.NewInstance();

		Set<ConstraintViolation<GatheringEvent>> constraintViolations  = validator.validate(gatheringEvent, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as gathering with NO exact location is allowed");
	}

/****************** TESTS *****************************/

	@Test
	public final void testNoPoint() {

        Set<ConstraintViolation<GatheringEvent>> constraintViolations  = validator.validate(gatheringEvent, Level2.class);
        assertTrue(constraintViolations.isEmpty(), "There should not be a constraint violation as gathering with NO exact location is allowed");
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
