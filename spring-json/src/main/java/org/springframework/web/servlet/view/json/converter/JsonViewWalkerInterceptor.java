package org.springframework.web.servlet.view.json.converter;

import java.beans.PropertyEditor;
import java.util.Date;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import net.sf.sojo.common.WalkerInterceptor;
import net.sf.sojo.core.Constants;
import net.sf.sojo.core.UniqueIdGenerator;
import net.sf.sojo.interchange.SerializerException;
import net.sf.sojo.interchange.json.JsonWalkerInterceptor;
import net.sf.sojo.util.Util;

public class JsonViewWalkerInterceptor implements WalkerInterceptor {
	private StringBuffer jsonString = new StringBuffer();
	private boolean withNullValuesInMap = false;
	private PropertyEditorRegistry propertyEditorRegistry;
	private String objectName;
	private String iterabelPath;
	private boolean convertAllMapValues;

	
	public String getJsonString() { return jsonString.toString(); }
	
	public boolean getWithNullValuesInMap() { return withNullValuesInMap; }
	public void setWithNullValuesInMap(boolean pvWithNullValuesInMap) { withNullValuesInMap = pvWithNullValuesInMap; }

	public void startWalk(Object pvStartObject) {
		jsonString = new StringBuffer();
	}

	public void endWalk() {
		Util.delLastComma(jsonString);
	}

	/**
	 * Convert escape (control) character from a JSON representation in a Java-String.
	 * This means: <code>\\b</code> to <code>\b</code>
	 * @param pvValue
	 * @return converted String
	 */
	public static String handleControlCharacterBack(final String pvValue) {

		if (pvValue == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int l = pvValue.length();
		char c;
		for (int i=0; i<l; i++) {
			c = pvValue.charAt(i);
            switch (c) {
	            case 0:
	            	break;
	            case '\n':
	            case '\r':
	            case '\t':
	            	sb.append(c);
	            	break;
	            case '\\':
	                i++;
					c = pvValue.charAt(i);
	                switch (c) {
		                case 'b':
		                    sb.append('\b');
		                    break;
		                case 't':
		                    sb.append('\t');
		                    break;
		                case 'n':
		                    sb.append('\n');
		                    break;
		                case 'f':
		                    sb.append('\f');
		                    break;
		                case 'r':
		                    sb.append('\r');
		                    break;
//		                case 'u':
//		                    sb.append((char)Integer.parseInt(next(4), 16));
//		                    break;
//		                case 'x' :
//		                    sb.append((char) Integer.parseInt(next(2), 16));
//		                    break;
		                default:
		                    sb.append(c);
		                }
	                break;
	            default:
	                sb.append(c);
	            }
        }
		return sb.toString();
	}

	/**
	 * Convert escape (control) character to a JSON representation.
	 * This means: <code>\b</code> to <code>\\b</code>
	 * @param pvValue
	 * @return converted String
	 */
	public static Object handleControlCharacter(final Object pvValue) {
		Object lvReturn = pvValue;
		if (lvReturn != null && lvReturn.getClass().equals(String.class)) {
			String lvString = lvReturn.toString();

	        int len = lvString.length();
			/*if (len == 0) {
				return "\"\"";
			}*/
	
	        char vorgChar;
	        char c = 0;
	        StringBuffer sb = new StringBuffer(len + 4);
	
	        for (int i = 0; i < len; i++) {
	            vorgChar = c;
	            c = lvString.charAt(i);
	            switch (c) {
		            case '\\':
		                if (vorgChar == '\\') {
		                    sb.append('\\').append(c);
		                }
		                break;
		            case '"':
		                sb.append('\\').append(c);
		                break;
		            case '/':
		                sb.append('\\').append(c);
		                break;
		            case '\b':
		                sb.append("\\b");
		                break;
		            case '\t':
		                sb.append("\\t");
		                break;
		            case '\n':
		                sb.append("\\n");
		                break;
		            case '\f':
		                sb.append("\\f");
		                break;
		            case '\r':
		                sb.append("\\r");
		                break;
		            default:
//		                if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
//		                    hex = "000" + Integer.toHexString(c);
//		                    sb.append("\\u" + hex.substring(hex.length() - 4));
//		                } else {
		                    sb.append(c);
//		                }
		            }
	        }
	        return sb.toString();
		} else {
			return lvReturn;
		}
	}
	
    public String object2StringWithDoubleQuote(Object pvObject) {
    	StringBuffer s = new StringBuffer("");
    	if (pvObject.getClass().equals(String.class) || pvObject.getClass().equals(Character.class) || Date.class.isAssignableFrom(pvObject.getClass())) {
    		s.append("\"").append(pvObject).append("\"");
    	}
    	else  {
    		s.append(pvObject);
    	}
    	return s.toString();
    }
    
    public String handleSimpleJsonValue(Object pvValue, String path){
    	Object convertedValue = convertJsonValue(pvValue, path);
    	return handleJsonValue (convertedValue );
    }

    private Object convertJsonValue(Object pvValue, String path) {
    	if(path != null && propertyEditorRegistry != null && (convertAllMapValues || path.startsWith("("+ objectName + ")."))){
	    	String p = path.replaceAll("\\("+ objectName + "\\).","");
	    	PropertyEditor ce = null;
			if(iterabelPath != null)
				 ce = propertyEditorRegistry.findCustomEditor(pvValue.getClass(), iterabelPath); 
	    	if(ce == null)
	    		ce = propertyEditorRegistry.findCustomEditor(pvValue.getClass(), p);
			if(ce == null)
				return pvValue.toString();
			else{
				ce.setValue(pvValue);
				return ce.getAsText();
			}
				
    	}else
    		return pvValue;
	}

	public  String handleJsonValue (Object pvValue) {
    	String s = "";
    	if (pvValue != null) {
	    	Object o = handleControlCharacter(pvValue);
	    	s = object2StringWithDoubleQuote(o);
    	}
    	return s;
    }
    
	public boolean visitElement(Object pvKey, int pvIndex, Object pvValue, int pvType, String pvPath, int pvNumberOfRecursion) {
		// --- SIMPLE ---
		if ( pvPath.indexOf("class") >= 0 || pvPath.indexOf(UniqueIdGenerator.UNIQUE_ID_PROPERTY) >= 0) {
			return false;
		} 
		if (pvType == Constants.TYPE_SIMPLE) {
			if (pvKey != null && pvKey.getClass().equals(String.class)) {
				jsonString.append(handleJsonValue(pvKey)).append(":");
			}  else if (pvKey != null) {
				throw new SerializerException("JSON support only properties/keys from type String and not: '" + pvKey.getClass().getName() + "' (" + pvKey + ")");
			}
			jsonString.append(handleSimpleJsonValue(pvValue, pvPath)).append(",");
		} 
		
		// --- NULL ---
		else if (pvType == Constants.TYPE_NULL) {
			if (pvPath.endsWith(")")) {
				if (getWithNullValuesInMap()) {
					jsonString.append(handleJsonValue(pvKey)).append(":null,");
				}
			} 
			else {
				jsonString.append("null,");
			}
		}
		
		// -- KEY and not SIMPLE ---
		else if (pvKey != null && pvValue != null) {
			if (pvKey != null && pvKey.getClass().equals(String.class)) {
				jsonString.append(handleJsonValue(pvKey)).append(":");
			} else {
				throw new SerializerException("JSON support only properties/keys from type String and not: '" + pvKey.getClass().getName() + "' (" + pvKey + ")");
			}
		}
		
		return false;
	}
	

	public void visitIterateableElement(Object pvValue, int pvType, String pvPath, int pvBeginEnd) {
		
		if (pvBeginEnd == Constants.ITERATOR_BEGIN) {
			if(!pvPath.endsWith("."))
				iterabelPath = pvPath;
			if (pvType == Constants.TYPE_ITERATEABLE) {
					if(!pvPath.endsWith("."))
					jsonString.append("[");
				} else if (pvType == Constants.TYPE_MAP) {
					jsonString.append("{");
				}
		} else if (pvBeginEnd == Constants.ITERATOR_END) {
				iterabelPath = null;
				Util.delLastComma(jsonString);
				if (pvType == Constants.TYPE_ITERATEABLE) {
					jsonString.append("],");
				} else if (pvType == Constants.TYPE_MAP) {					
					jsonString.append("},");
				}
		}
	}
	
	

	public PropertyEditorRegistry getPropertyEditorRegistry() {
		return propertyEditorRegistry;
	}

	public void setPropertyEditorRegistry(
			PropertyEditorRegistry propertyEditorRegistry) {
		this.propertyEditorRegistry = propertyEditorRegistry;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public boolean getConvertAllMapValues() {
		return convertAllMapValues;
	}

	public void setConvertAllMapValues(boolean convertAllMapValues) {
		this.convertAllMapValues = convertAllMapValues;
	}

	
    
	
	
	
	
	
}
