/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @date 2013-10-25
 */
public class BeanInitNode implements Comparable<BeanInitNode>{

	private BeanInitNode parent;
	
	private Map<String, BeanInitNode> children = new HashMap<String, BeanInitNode>();
	
	private String path;
	
	private boolean isToManyInitialized = false;
	
	private boolean isToOneInitialized = false;
	
	private  Map<Class<?>, Set<Object>> beans = new HashMap<Class<?>, Set<Object>>();

	
	
	public BeanInitNode(BeanInitNode parent, String part) {
		this.path = CdmUtils.Nz(part);
		this.parent = parent;
		if (parent != null){
			parent.addChild(part, this);
		}
	}

	private void addChild(String part, BeanInitNode child) {
		children.put(part, child);
		if (child.isWildcard()){
			if (part.equals(AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD)){
				this.isToManyInitialized = true;
			}
			this.isToOneInitialized = true;
		}
	}


	public static BeanInitNode createInitTree(List<String> propertyPaths) {
		
		//sort paths  //TODO needed?
		Collections.sort(propertyPaths);

	    BeanInitNode root = new BeanInitNode(null, "");
	    
	    for (String fullPath : propertyPaths){
	    	String[] parts = fullPath.split("\\.");
	    	BeanInitNode lastNode = root;
	    	for (String part : parts){
	    		BeanInitNode child = lastNode.children.get(part);
	    		if (child == null){
	    			child = new BeanInitNode(lastNode, part);
	    		}
    			lastNode = child;
	    	}
	    }
		 
		return root;
	}


	public List<BeanInitNode> getChildrenList() {
		List<BeanInitNode> result = new ArrayList<BeanInitNode>(children.values());
		Collections.sort(result);
		return result;
	}


	public void addBean(Object bean) {
		if (bean != null){
			Class<?> key = bean.getClass();
			Set<Object> classedBeans = beans.get(key);
			if (classedBeans == null){
				classedBeans = new HashSet<Object>();
				beans.put(key, classedBeans);
			}
			classedBeans.add(bean);
		}
	}
	public void addBeans(Collection<?> beans) {
		for (Object bean : beans){
			addBean(bean);
		}
	}
	
	private Map<Class<?>, Set<Object>> getClassedBeans(){
		return this.beans;
	}
	
	public void resetBeans(){
		beans.clear();
	}

	public String getPath() {
		return path;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}


	public boolean isWildcard() {
		return path.equals(AbstractBeanInitializer.LOAD_2ONE_WILDCARD) || 
				path.equals(AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD);
	}
	
	public boolean isToManyWildcard() {
		return path.equals(AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD);
	}


	public boolean isInitializedIfSingle(){
		return parent.isToOneInitialized;
	}
	
	public boolean isInitializedAny(){
		return parent.isToManyInitialized;
	}
	
	@Override
	public int compareTo(BeanInitNode o) {
		String toMany = AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD;
		String toOne = AbstractBeanInitializer.LOAD_2ONE_WILDCARD;
		if (this.path.equals(toMany)){
			return -1;
		}else if (o.path.equals(toMany)){
			return 1;
		}else if (this.path.equals(toOne)){
			return -1;
		}else if (o.path.equals(toOne)){
			return 1;
		}else{
			return path.compareTo(o.path);
		}
	}
	
	
	@Override
	public String toString() {
		
		if(parent == null){
			return "/";
		}else{
			return parent.toString() +  ((parent.parent == null) ? "" : ".") + path;
		}
	}
	
	public String toStringTree() {
		
		String result = (path.equals("")? "/": path) + "\n";
		for (BeanInitNode child : getChildrenList()){
			result += toString() + "." + child.toStringTree();
		}
		
		return result;
	}

	public Map<Class<?>, Set<Object>> getParentBeans() {
		if (parent == null){
			return new HashMap<Class<?>, Set<Object>>();
		}else{
			return parent.getClassedBeans();
		}
	}


}
