package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

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
public class Role implements GrantedAuthority {

    private static final long serialVersionUID = -2244354513663448504L;

    /**
     * The role prefix 'ROLE_' is defined in the spring security
     * <code>RoleVoter</code>
     */
    private static final String ROLE_PREFIX = "ROLE_";

    public final static Role ROLE_ADMIN = new Role(UUID.fromString("56eac992-67ba-40be-896c-4e992ca2afc0"), "ROLE_ADMIN");
    public final static Role ROLE_USER_MANAGER = new Role(UUID.fromString("9eabd2c6-0590-4a1e-95f5-99cc58b63aa7"), "ROLE_USER_MANAGER");

    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    private String authority;

    public Role(UUID uuid, String authority) {
        this.uuid = uuid;
        this.authority = authority;
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
            if(!authority.equals(grantedAuthority.getAuthority())){
                throw new RuntimeException("the persisted Authority with uuid " + uuid + " is not '" + authority + "");
            }
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
        grantedAuthority.setAuthority(authority);
        return grantedAuthority;
    }

    public static Role fromGrantedAuthority(GrantedAuthorityImpl grantedAuthority){
        if(!grantedAuthority.getAuthority().matches("^" + ROLE_PREFIX +"\\w*$")){
            throw new RuntimeException("invalid role prefix of authority " + grantedAuthority.getAuthority() + "[" + grantedAuthority.getUuid() + "]");
        }
        return new Role(grantedAuthority.getUuid(), grantedAuthority.getAuthority());
    }

    @Override
    public String getAuthority() {
        return authority;
    }

}