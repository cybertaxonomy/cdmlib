
package eu.etaxonomy.cdm.database;

import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.security.core.Authentication;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;

/**
 * @author andreas
 \* @since Sep 4, 2012
 */
public class PermissionDeniedException extends HibernateException {
    private static final long serialVersionUID = 6993452039967589921L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PermissionDeniedException.class);

    /**
     * @param message
     */
    public PermissionDeniedException(String message) {
        super(message);
    }

    public PermissionDeniedException(Authentication authentication, CdmBase entity, Operation requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public PermissionDeniedException(Authentication authentication, CdmBase entity, EnumSet<CRUD> requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    public PermissionDeniedException(Authentication authentication, CdmBase entity, String requiredOperation) {
        super(requiredOperation + " not permitted for '" + authentication.getName()
                + "' on " + entity.getClass().getSimpleName() + "[uuid:" + entity.getUuid() + "', toString:'" + entity.toString() + "']");
    }

    /**
     * @param authentication
     * @param roles
     */
    public PermissionDeniedException(Authentication authentication, Role[] roles) {

        super("Permission denied for '" + authentication.getName()
                + "' none of the roles '" + roles + "' found in authentication.");
    }

    /**
     * @param cause
     */
    public PermissionDeniedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

}
