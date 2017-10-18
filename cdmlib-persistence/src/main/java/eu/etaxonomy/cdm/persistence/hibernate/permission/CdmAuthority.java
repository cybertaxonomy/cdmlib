/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.GrantedAuthority;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;

/**
 * A <code>CdmAuthority</code> consists basically of two parts which are separated
 * by a dot character '.'.
 *
 * <ul>
 * <li><code>permissionClass</code>: an {@link CdmPermissionClass} instance with represents a cdm
 * type or a part of the cdm type hierarchy. The className is always represented
 * as an upper case string.</li>
 * <li><code>property</code>: The <code>CdmAuthority</code> only applies to instances
 * which satisfy the specified property. Interpretation is up to type specific voters.</li>
 * <li><code>operation</code>: A string enclosed in brackets <code>[]</code>
 * which specifies one {@link Operation} or
 * multiple on that set of cdm types. Multiple {@link Operation} must be comma
 * separated.</li>
 * <li><code>targetUuid</code>: The <code>operation</code> may be restricted to a specific cdm entity by adding
 * the entity uuid to the <code>operation</code>. The uuid string is enclosed in curly brackets '<code>{</code>'
 * , '<code>}</code>' and appended to the end of the <code>operation</code>.</li>
 * </ul>
 *
 * <h3>Examples for permissionStrings</h3>
 *
 * <pre>
 * TAXONBASE.[CREATE]
 * TAXONBASE.[READ]
 * TAXONBASE.[UPDATE]
 * TAXONBASE.[DELETE]
 * DESCRIPTIONBASE.[UPDATE]
 * DESCRIPTIONBASE.[CREATE,UPDATE,DELETE,READ]
 * DESCRIPTIONELEMENTBASE(Ecology).[UPDATE]
 * TAXONNODE.[UPDATE]{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
 * </pre>
 *
 * The method {@link #getPermissionString(String)} parses a full authority and returns  permissionString and
 * the {@link CdmAuthority} from the <code>authority</code>.
 *
 *
 * @author k.luther
 * @author Andreas Kohlbecker
 */
public class CdmAuthority implements GrantedAuthority, ConfigAttribute, IGrantedAuthorityConverter {

    private static final long serialVersionUID = 1L;

    public static final Logger logger = Logger.getLogger(CdmAuthority.class);

    private static Map<String, CdmAuthority> grantedAuthorityCache = new HashMap<String, CdmAuthority>();

    CdmPermissionClass permissionClass;
    String property;
    // Making sure that operation is always initialized, for both
    // - the string representation to have a '[]'
    // - and the object representation to never be null (with check in constructors)
    EnumSet<CRUD> operation = EnumSet.noneOf(CRUD.class);
    UUID targetUuid;

    public CdmAuthority(CdmBase targetDomainObject, EnumSet<CRUD> operation){
        this.permissionClass = CdmPermissionClass.getValueOf(targetDomainObject);
        this.property = null;
        if(operation != null) {
        	this.operation = operation;
        }
        if(targetDomainObject.getUuid() == null){
            throw new NullPointerException("UUID of targetDomainObject is null. CDM entities need to be saved prior using this function");
        }
        this.targetUuid = targetDomainObject.getUuid();
    }

     public CdmAuthority(CdmBase targetDomainObject, String property, EnumSet<CRUD> operation, UUID uuid){
       this.permissionClass = CdmPermissionClass.getValueOf(targetDomainObject);
        this.property = property;
        if(operation != null) {
        	this.operation = operation;
        }
        this.targetUuid = uuid;
    }

     public CdmAuthority(Class<? extends CdmBase> targetDomainType, String property, EnumSet<CRUD> operation, UUID uuid){
         this.permissionClass = CdmPermissionClass.getValueOf(targetDomainType);
          this.property = property;
          if(operation != null) {
              this.operation = operation;
          }
          this.targetUuid = uuid;
      }


    public CdmAuthority(CdmPermissionClass permissionClass, String property, EnumSet<CRUD> operation, UUID uuid){
        this.permissionClass = permissionClass;
        this.property = property;
        if(operation != null) {
        	this.operation = operation;
        }
        this.targetUuid = uuid;
    }

    public CdmAuthority(CdmPermissionClass permissionClass, EnumSet<CRUD> operation){
        this.permissionClass = permissionClass;
        if(operation != null) {
            this.operation = operation;
        }
    }

    private CdmAuthority (String authority) throws CdmAuthorityParsingException{

        String[] tokens = parse(authority);
        // className must never be null

        try {
            permissionClass = CdmPermissionClass.valueOf(tokens[0]);
        } catch (IllegalArgumentException e) {
            throw new CdmAuthorityParsingException(authority);
        }
        property = tokens[1];

        if(tokens[2] != null){
            try {
                operation = Operation.fromString(tokens[2]);
            } catch (IllegalArgumentException e) {
                logger.warn("cannot parse Operation " + tokens[2]);
                throw new CdmAuthorityParsingException(authority);
            }
        }
        if(tokens[3] != null){
            targetUuid = UUID.fromString(tokens[3]);
        }
    }

    public CdmPermissionClass getPermissionClass(){
        return permissionClass;
    }

    public String getProperty(){
        return property;
    }

    public EnumSet<CRUD> getOperation(){
        return operation;
    }

    public void setOperation(EnumSet<CRUD> operation) {
        this.operation = operation;
    }

    public UUID getTargetUUID(){
        return targetUuid;
    }

    public boolean hasTargetUuid() {
        return targetUuid != null;
    }

    public boolean hasProperty() {
        return property != null;
    }

    /**
     * Parses the given <code>authority</code> and returns an array of tokens.
     * The array has a length of four elements whereas the elements can be null.
     * The elements in the array correspond to the fields of {@link CdmAuthority}:
     * <ol>
     * <li>{@link CdmAuthority#permissionClass}</li>
     * <li>{@link CdmAuthority#property}</li>
     * <li>{@link CdmAuthority#operation}</li>
     * <li>{@link CdmAuthority#targetUuid}</li>
     * </ol>
     * @param authority
     * @return an array of tokens
     * @throws CdmAuthorityParsingException
     */
    protected String[] parse(String authority) throws CdmAuthorityParsingException {
        //
        // regex pattern explained:
        //  (\\w*)             -> classname
        //  (?:\\((\\w*)\\))?  -> (property)
        //  \\.?               -> .
        //  (?:\\[(\\D*)\\])(?:\\{([\\da-z\\-]+)\\})? -> Permission and targetUuid
        //
        String regex = "(\\w*)(?:\\((\\w*)\\))?\\.?(?:\\[(\\D*)\\])?(?:\\{([\\da-z\\-]+)\\})?";
        Pattern pattern = Pattern.compile(regex);
        String[] tokens = new String[4];
        logger.debug("parsing '" + authority + "'");
        Matcher m = pattern.matcher(authority);

        if (m.find() && m.groupCount() == 4 ) {
            for (int i = 0; i < m.groupCount(); i++) {
                tokens[i] = m.group(i+1);
                // normalize empty strings to null
                if(tokens[i] != null && tokens[i].length() == 0){
                    tokens[i] = null;
                }
                logger.trace("[" + i + "]: " + tokens[i]+ "\n");
            }
        } else {
            logger.debug("no match");
            throw new CdmAuthorityParsingException("Unsupported authority string: '" + authority + "'");
        }

        return tokens;
    }

    /**
     * {@inheritDoc}
     *
     * same as {@link #toString()} and  {@link #getAttribute()}
     */
    @Override
    public String getAuthority() {
        return toString();
    }

    /**
     * {@inheritDoc}
     *
     * same as {@link #toString()} and  {@link #getAuthority()}
     */
    @Override
    public String getAttribute() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(permissionClass.toString());
        if(property != null){
            sb.append('(').append(property).append(')');
        }
        sb.append('.').append(operation.toString());
        if(targetUuid != null){
            sb.append('{').append(targetUuid.toString()).append('}');
        }
        return sb.toString() ;
    }

    /**
     * Constructs a new CdmAuthority by parsing the authority string.
     * For details on the syntax please refer to the class
     * documentation above.
     * <p>
     * This method is mainly used by the permission voters ({@link CdmPermissionVoter)}.
     * In order to improve the voting process this method is caching the <code>CdmAuthority</code>
     * instances per <code>GrantedAuthority</code> string in a map.
     *
     * @param authority
     * @throws CdmAuthorityParsingException
     */
    public static CdmAuthority fromGrantedAuthority(GrantedAuthority authority) throws CdmAuthorityParsingException {
        CdmAuthority cdmAuthority = grantedAuthorityCache.get(authority.getAuthority());
        if(cdmAuthority == null){
            cdmAuthority = new CdmAuthority(authority.getAuthority());
        }
        return cdmAuthority;
//        return  new CdmAuthority(authority.getAuthority());
    }


    @Override
    public GrantedAuthorityImpl asNewGrantedAuthority() throws CdmAuthorityParsingException {
        GrantedAuthorityImpl grantedAuthority = GrantedAuthorityImpl.NewInstance(getAuthority());
        return grantedAuthority;
    }


}
