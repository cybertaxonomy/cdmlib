/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Rules to define tagging for HTML tagged string output.
 * 
 * @author a.mueller
 * @created 14/02/2012
 *
 */
public class HTMLTagRules {

	private List<TagRule> rules = new ArrayList<HTMLTagRules.TagRule>();
	
	private class TagRule{
		private TagRule(TagEnum type, String htmlTag){this.type = type; this.htmlTag = htmlTag;}
		private TagEnum type;
		private String htmlTag;
//		public TagEnum getType(){return this.type;}
//		public String getString(){return this.htmlTag;}
		@Override public String toString(){return type.name() + "-><" + htmlTag + ">";}
	}
	
	
	public HTMLTagRules addRule(TagEnum type, String htmlTag){
		if (type == null || htmlTag == null){
			throw new NullPointerException("Null tpye or htmlTag not allowed for HTMLTagRule");
		}
		rules.add(new TagRule(type, htmlTag));
		return this;
	}
	
	public SortedSet<String> getRule(TagEnum type){
		SortedSet<String> result = new TreeSet<String>();
		for (TagRule rule : rules){
			if (rule.type.equals(type)){
				result.add(rule.htmlTag);
			}
		}
		return result;
	}
	
	public boolean hasRule(TagEnum type, String htmlTag){
		for (TagRule rule : rules){
			if (rule.type.equals(type) && htmlTag.equals(htmlTag)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		String result = "HTMLTagRules[";
		for (TagRule rule : rules){
			result += rule.toString() + ";";
		}
		result = result.substring(0, result.length() -1) + "]";
		return result;
	}
	
	
}
