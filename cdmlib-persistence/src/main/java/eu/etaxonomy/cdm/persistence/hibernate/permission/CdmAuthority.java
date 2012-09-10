
package eu.etaxonomy.cdm.persistence.hibernate.permission;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.security.access.ConfigAttribute;

import eu.etaxonomy.cdm.model.common.CdmBase;

import sun.security.provider.PolicyParser.ParsingException;

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
 * <li><code>operation</code>: a string which specifies a {@link Operation} on that set of cdm
 * types</li>
 * <li><code>targetUuid</code>: The <code>operation</code> may be restricted to a specific cdm entity by adding
 * the entity uuid to the <code>operation</code>. The uuid string is enclosed in curly brackets '<code>{</code>'
 * , '<code>}</code>' and appended to the end of the <code>operation</code>.</li>
 * </ul>
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
 * the {@link CdmAuthority} from the <code>authority</code>.
 *
 *
 * @author k.luther
 */
public class CdmAuthority implements ConfigAttribute {

    private static final long serialVersionUID = 1L;

    public static final Logger logger = Logger.getLogger(CdmAuthority.class);

    CdmPermissionClass permissionClass;
    String property;
    Operation operation;
     UUID targetUuid;

    public CdmAuthority(CdmBase targetDomainObject, Operation operation, UUID uuid){
        this.permissionClass = CdmPermissionClass.getValueOf(targetDomainObject);
        this.property = null;
        this.operation = operation;
        this.targetUuid = uuid;
    }

     public CdmAuthority(CdmBase targetDomainObject, String property, Operation operation, UUID uuid){
       this.permissionClass = CdmPermissionClass.getValueOf(targetDomainObject);
        this.property = property;
        this.operation = operation;
        this.targetUuid = uuid;
    }


    public CdmAuthority(CdmPermissionClass permissionClass, String property, Operation operation, UUID uuid){
        this.permissionClass = permissionClass;
        this.property = property;
        this.operation = operation;
        this.targetUuid = uuid;
    }

    /**
     * Constructs a new CdmAuthority by parsing the contents of an
     * authority string. For details on the syntax please refer to the class
     * documentation above.
     *
     * TODO usually one would not use a constructor but a valueOf(String) or fromSting() method for this
     *
     * @param authority
     * @throws ParsingException
     */
    public CdmAuthority (String authority) throws ParsingException{

        String[] tokens = parse(authority);
        // className must never be null
        permissionClass = CdmPermissionClass.valueOf(tokens[0]);
        property = tokens[1];
        if(tokens[2] != null){
            operation = Operation.valueOf(tokens[2]);
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

    public Operation getOperation(){
        return operation;
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
     * @throws ParsingException
     */
    protected String[] parse(String authority) throws ParsingException {
        //
        // regex pattern explained:
        //  (\\w*)             -> classname
        //  (?:\\((\\w*)\\))?  -> (property)
        //  \\.?               -> .
        //  (?:(\\w*))(?:\\{([\\da-z\\-]+)\\})? -> Permmission and targetUuid
        //
        String regex = "(\\w*)(?:\\((\\w*)\\))?\\.?(?:(\\w*))(?:\\{([\\da-z\\-]+)\\})?";
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
                logger.debug("[" + i + "]: " + tokens[i]+ "\n");
            }
        } else {
            logger.debug("no match");
            throw new ParsingException("Unsupported authority string: '" + authority + "'");
        }

        return tokens;
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

    @Override
    public String getAttribute() {
        return toString();
    }

}