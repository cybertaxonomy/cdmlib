
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermission;


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

    public EvaluationFailedException(Authentication autherntication, CdmBase entity, CdmPermission permission) {
        super(permission.name() + " not permitted for '" + autherntication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public EvaluationFailedException(Authentication autherntication, CdmBase entity, String permission) {
        super(permission + " not permitted for '" + autherntication.getName()
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
