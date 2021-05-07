package eu.etaxonomy.cdm.persistence.permission;

import java.util.UUID;

import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.dao.permission.IGrantedAuthorityDao;

/**
 * The role prefix 'ROLE_' is defined in the spring security
 * <code>RoleVoter</code>
 *
 * @author a.kohlbecker
 * @since Oct 5, 2012
 */
public class Role implements GrantedAuthority, IGrantedAuthorityConverter {

    private static final long serialVersionUID = -2244354513663448504L;

    /**
     * The role prefix 'ROLE_' is defined in the spring security
     * {@link RoleVoter}
     */
    private static final String ROLE_PREFIX = "ROLE_";

    private static UUID uuidAdmin = UUID.fromString("56eac992-67ba-40be-896c-4e992ca2afc0");
    private static UUID uuidProjectManager = UUID.fromString("9eabd2c6-0590-4a1e-95f5-99cc58b63aa7");
    private static UUID uuidUserManager = UUID.fromString("74d340a9-b472-4b97-b52a-c140e27a5c76");
    private static UUID uuidPublish = UUID.fromString("9ffa7879-cc67-4592-a14a-b251cccde1a7");
    private static UUID uuidRemoting = UUID.fromString("be004bf6-0498-48e3-9f06-ff93fc9cdc9a");

    public final static Role ROLE_ADMIN = new Role(uuidAdmin, "ROLE_ADMIN");
    public final static Role ROLE_PROJECT_MANAGER = new Role(uuidProjectManager, "ROLE_PROJECT_MANAGER");
    public final static Role ROLE_USER_MANAGER = new Role(uuidUserManager, "ROLE_USER_MANAGER");
    public final static Role ROLE_PUBLISH = new Role(uuidPublish, "ROLE_PUBLISH");
    public final static Role ROLE_REMOTING = new Role(uuidRemoting, "ROLE_REMOTING");

    private final UUID uuid;
    private final String authority;

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
            Assert.isTrue(authority.equals(grantedAuthority.getAuthority()), "the persisted Authority with uuid " + uuid + " is not '" + authority + "'" );
        }
        return grantedAuthority;
    }

    /**
     * @return a fresh <b>not persisted instance</b> of {@link GrantedAuthorityImpl}
     *         for the Role.
     */
    @Override
    public GrantedAuthorityImpl asNewGrantedAuthority() {
        GrantedAuthorityImpl grantedAuthority;
        grantedAuthority = GrantedAuthorityImpl.NewInstance(null);
        grantedAuthority.setUuid(uuid);
        grantedAuthority.setAuthority(authority);
        return grantedAuthority;
    }

    public static Role fromGrantedAuthority(GrantedAuthorityImpl grantedAuthority){
        String authorityString = grantedAuthority.getAuthority();
		Assert.isTrue(authorityString.matches("^" + ROLE_PREFIX +"\\w*$"), "invalid role prefix of authority " + authorityString + "[" + grantedAuthority.getUuid() + "]");
        return new Role(grantedAuthority.getUuid(), authorityString);
    }

    public static Role fromString(String authorityString){
		Assert.isTrue(authorityString.matches("^" + ROLE_PREFIX +"\\w*$"), "invalid role prefix of authority " + authorityString);
		Role role = null;
		if(authorityString.equals(ROLE_ADMIN.authority)){
			return ROLE_ADMIN;
		} else
		if(authorityString.equals(ROLE_PROJECT_MANAGER.authority)){
			return ROLE_PROJECT_MANAGER;
		} else
		if(authorityString.equals(ROLE_PUBLISH.authority)){
			return ROLE_PUBLISH;
		} else
		if(authorityString.equals(ROLE_USER_MANAGER.authority)){
			return ROLE_USER_MANAGER;
		}
		Assert.notNull(role, "The given auhtority #" + authorityString + "' does not match any known role");
		return role;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String toString(){
        return getAuthority();
    }
}