
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;

/**
 * A <code>AuthorityPermission</code> consists of two parts which are separated
 * by a dot character '.' in the permissionString which can retrieved by
 * {@link #getPermissionString(String)}:
 *
 * <ul>
 * <li><code>className</code>: an {@link CdmPermissionClass} instance with represents a cdm
 * type or a part of the cdm type hierarchy. The className is always represented
 * as an upper case string.</li>
 * <li><code>permission</code>: a string which specifies a {@link CdmPermission} on that set of cdm
 * types</li>
 * <li><code>targetUuid</code>: The permission may be restricted to a specific cdm entity by adding
 * the entity uuid to the permission. The uuid string is enclosed in curly brackets '<code>{</code>'
 * , '<code>}</code>' and appended to the end of the permission.</li>
 * </ul>
 * The authority string syntax looks like:<br>
 * <pre>CLASSNAME.PERMISSION[{UUID}]</pre>
 * Whereas the square brackets are indicating an optional element.
 *
 * <h3>Examples for permissionStrings</h3>
 *
 * <pre>
 * TAXONBASE.CREATE
 * TAXONBASE.READ
 * TAXONBASE.UPDATE
 * TAXONBASE.DELETE
 * DESCRIPTIONBASE.UPDATE
 * TAXONNODE.UPDATE{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
 * </pre>
 *
 * The method {@link #getPermissionString(String)} parses a full authority and returns  permissionString and
 * the {@link AuthorityPermission} from the <code>authority</code>.
 *
 *
 *
 * @author k.luther
 */
public class AuthorityPermission{
    CdmPermissionClass className;
    CdmPermission permission;
    UUID targetUuid;

    public AuthorityPermission(Object targetDomainObject, CdmPermission permission, UUID uuid){
        this.className = CdmPermissionClass.getValueOf(targetDomainObject);
        this.permission = permission;
        targetUuid = uuid;
    }

    public CdmPermissionClass getClassName(){
        return className;
    }

    public CdmPermission getPermission(){
        return permission;
    }

    public UUID getTargetUUID(){
        return targetUuid;
    }

    /**
     * Constructs a new AuthorityPermission by parsing the contents of an
     * authority string. For details on the syntax please refer to the class
     * documentation above.
     *
     * @param authority
     */
    public AuthorityPermission (String authority){
        String permissionString;
        int firstPoint = authority.indexOf(".");
        if (firstPoint == -1){
            // no dot: the authorityString only holds a CdmPermissionClass
            className = CdmPermissionClass.valueOf(authority);
        }else{
            // has a dot: the authorityString only holds a CdmPermissionClass and a permissionString
            className = CdmPermissionClass.valueOf((authority.substring(0, firstPoint)));
            int bracket = authority.indexOf("{");
            permissionString = getPermissionString(authority);
            if (bracket != -1){
                // having a bracket means the permissionString contains a uuid !!!
                int secondBracket = authority.indexOf("}");
                String uuid = authority.substring(bracket+1, secondBracket);
                targetUuid = UUID.fromString(uuid);
            }
            permission = CdmPermission.valueOf(permissionString.toUpperCase());
        }
    }

    /**
     * The method {@link #getPermissionString(String)} parses a full authority
     * string like
     * "<code>TAXONNODE.READ{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}</code>"and
     * returns the string representation of the CdmPermission "<code>READ</code>"
     * contained in the authority string
     *
     * @param authority
     * @return
     */
    private static String getPermissionString(String authority){
        int lastPoint = authority.lastIndexOf(".");
        int bracket = authority.indexOf("{");
        if (bracket == -1){
            return authority.substring(lastPoint+1);
        }else{
            return authority.substring(lastPoint+1, bracket);
        }
    }

}