package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.UUID;

import junit.framework.Assert;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;

/**
 * The role prefix 'ROLE_' is defined in the spring security
 * <code>RoleVoter</code>
 *
 * @author a.kohlbecker
 * @date Oct 5, 2012
 *
 *
 */
public enum Role {

    ROLE_ADMIN(UUID.fromString("56eac992-67ba-40be-896c-4e992ca2afc0")),
    ROLE_USER_MANAGER(UUID.fromString("9eabd2c6-0590-4a1e-95f5-99cc58b63aa7"));

    private UUID uuid;

    Role(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * <b>WARNING:</b> This method must only be used when there is nothing in
     * hibernate to be flushed to the database. Otherwise you risk of getting
     * into an endless loop. Alternatively you can use
     * {@link #asNewGrantedAuthority()}
     *
     *
     * @return either an instance which already is persited to the database or a
     *         fresh not persisted instance of {@link GrantedAuthorityImpl} for
     *         the Role.
     */
    public GrantedAuthorityImpl asGrantedAuthority(IGrantedAuthorityDao grantedAuthorityDao) {
        GrantedAuthorityImpl grantedAuthority = grantedAuthorityDao.findByUuid(uuid);
        if (grantedAuthority == null) {
            grantedAuthority = asNewGrantedAuthority();
        } else {
            // perform consistency check
            Assert.assertEquals(name(), grantedAuthority.getAuthority());
        }
        return grantedAuthority;
    }

    /**
     * @return a fresh <b>not persisted instance</b> of {@link GrantedAuthorityImpl}
     *         for the Role.
     */
    public GrantedAuthorityImpl asNewGrantedAuthority() {
        GrantedAuthorityImpl grantedAuthority;
        grantedAuthority = GrantedAuthorityImpl.NewInstance();
        grantedAuthority.setUuid(uuid);
        grantedAuthority.setAuthority(name());
        return grantedAuthority;
    }

}