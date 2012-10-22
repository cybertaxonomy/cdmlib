
package eu.etaxonomy.cdm.database;

import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;


/**
 * FIXME Rename to PermissionDeniedException ???
 *
 * @author andreas
 * @date Sep 4, 2012
 *
 */
public class EvaluationFailedException extends HibernateException {
    private static final Logger logger = Logger
            .getLogger(EvaluationFailedException.class);

    /**
     * @param message
     */
    public EvaluationFailedException(String message) {
        super(message);
    }

    public EvaluationFailedException(Authentication authentication, CdmBase entity, Operation requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public EvaluationFailedException(Authentication authentication, CdmBase entity, EnumSet<CRUD> requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public EvaluationFailedException(Authentication authentication, CdmBase entity, String requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    /**
     * @param cause
     */
    public EvaluationFailedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public EvaluationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
