/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.collection.internal.AbstractPersistentCollection;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @since 2013-10-25
 */
public class BeanInitNode implements Comparable<BeanInitNode>{

	// ************************ ATTRIBUTES *******************************/

	private final BeanInitNode parent;

	private final Map<String, BeanInitNode> children = new HashMap<String, BeanInitNode>();

	private final String path;

	private boolean isToManyInitialized = false;

	private boolean isToOneInitialized = false;

	private BeanInitNode wildcardChildCache;


	private final  Map<Class<?>, Set<Object>> beans = new HashMap<Class<?>, Set<Object>>();

	private final  Map<Class<?>, Set<Serializable>> lazyBeans = new HashMap<Class<?>, Set<Serializable>>();

	private final  Map<Class<?>, Map<String, Set<Serializable>>> lazyCollections = new HashMap<Class<?>, Map<String,Set<Serializable>>>();

	//java.util.Set is  not possible here to avoid initializing of collection during .add()
	private final List<AbstractPersistentCollection> uninitializedCollections = new ArrayList<AbstractPersistentCollection>();

// *************************** STATIC METHODS *****************************************/

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

	private static boolean isWildcard(String pathPart) {
		return pathPart.equals(AbstractBeanInitializer.LOAD_2ONE_WILDCARD) ||
				pathPart.equals(AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD);
	}


//***************************** CONSTRUCTOR RELATED ****************************************/

	public BeanInitNode(BeanInitNode parent, String part) {
		this.path = CdmUtils.Nz(part);
		this.parent = parent;
		this.isToManyInitialized = this.path.equals(AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD);
		this.isToOneInitialized = this.path.equals(AbstractBeanInitializer.LOAD_2ONE_WILDCARD) || this.isToManyInitialized;
		if (parent != null){
			parent.addChild(part, this);
		}
	}

	private void addChild(String part, BeanInitNode child) {
		children.put(part, child);
		if (child.isWildcard()){
			//set wildcard child if not exists or if child is stronger then existing wildcard child
			if (this.wildcardChildCache == null || (! this.wildcardChildCache.isToManyInitialized) && child.isToManyInitialized ){
				this.wildcardChildCache = child;
			}
		}
	}

// ************************** 	***********************************************************/

	public BeanInitNode getChild(String param){
		return children.get(param);
	}



	public List<BeanInitNode> getChildrenList() {
		List<BeanInitNode> result = new ArrayList<BeanInitNode>(children.values());
		Collections.sort(result);
		return result;
	}

	public BeanInitNode getSibling(String param) {
		if (getPath().equals(param) || parent == null){
			return null;
		}else{
			return parent.getChild(param);
		}
	}

	public String getPath() {
		return path;
	}

	public boolean isRoot() {
		return StringUtils.isBlank(path);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public BeanInitNode getWildcardChild(){
		return this.wildcardChildCache;
	}
	public boolean isWildcard() {
		return this.isToManyInitialized || this.isToOneInitialized;
	}
	public boolean hasWildcardChild() {
		return this.wildcardChildCache != null;
	}
	public boolean hasToManyWildcardChild() {
		return this.wildcardChildCache != null;
	}

	public boolean hasWildcardToManySibling() {
		BeanInitNode sibl = getWildcardSibling();
		return (sibl != null && sibl.isToManyInitialized);
	}

	private BeanInitNode getWildcardSibling() {
		if (!isWildcard() && parent == null){
			return parent.getWildcardChild();
		}else{
			return null;
		}
	}


	public boolean isToManyWildcard() {
		return path.equals(AbstractBeanInitializer.LOAD_2ONE_2MANY_WILDCARD);
	}


// ******************* LAZY COLLECTION *****************************************/

	public void putLazyCollection(AbstractPersistentCollection collection) {
		String role = collection.getRole();
		String parameter = role.substring(role.lastIndexOf(".") + 1);
		//for embeddable classes we need also the parent parameter
		//TODO also allow recursive embedding
		String parentRole = role.substring(0, role.lastIndexOf("."));
		String parentParameter = parentRole.substring(parentRole.lastIndexOf(".") + 1);
		Class<?> ownerClass = collection.getOwner().getClass();
		if (Character.isLowerCase(parentParameter.charAt(0))){
		    parameter = parentParameter + "." + parameter;
		}
		putLazyCollection(ownerClass, parameter, collection.getKey());
		this.uninitializedCollections.add(collection);
	}

    private void putLazyCollection(Class<?> ownerClazz, String parameter, Serializable id) {
		if (ownerClazz != null && parameter != null && id != null){
			Map<String, Set<Serializable>> lazyParams = lazyCollections.get(ownerClazz);
			if (lazyParams == null){
				lazyParams = new HashMap<String, Set<Serializable>>();
				lazyCollections.put(ownerClazz, lazyParams);
			}
			Set<Serializable> layzIds = lazyParams.get(parameter);
			if (layzIds == null){
				layzIds = new HashSet<Serializable>();
				lazyParams.put(parameter, layzIds);
			}
			layzIds.add(id);
		}else{
			throw new IllegalArgumentException("Class, parameter and id should not be null");
		}
	}
	public Map<Class<?>, Map<String, Set<Serializable>>> getLazyCollections(){
		return this.lazyCollections;
	}
	public List<AbstractPersistentCollection> getUninitializedCollections(){
		return this.uninitializedCollections;
	}
	public void resetLazyCollections(){
		this.lazyCollections.clear();
		this.uninitializedCollections.clear();
	}


// ******************* LAZY BEAN *****************************************/

	public void putLazyBean(Class<?> clazz, Serializable id) {
		if (clazz != null && id != null){
			Set<Serializable> classedLazies= lazyBeans.get(clazz);
			if (classedLazies == null){
				classedLazies = new HashSet<Serializable>();
				lazyBeans.put(clazz, classedLazies);
			}
			classedLazies.add(id);
		}else{
			throw new IllegalArgumentException("Class and id should not be null");
		}
	}
	public Map<Class<?>, Set<Serializable>> getLazyBeans(){
		return this.lazyBeans;
	}
	public void resetLazyBeans(){
		this.lazyBeans.clear();
	}

// ********************* BEANS ******************************/

	private Map<Class<?>, Set<Object>> getClassedBeans(){
		return this.beans;
	}


	/**
	 * Returns beans represented by this node, split into implementing
	 * classes.
	 * @return
	 */
	public Map<Class<?>, Set<Object>> getBeans() {
		return this.beans;
	}

	public Map<Class<?>, Set<Object>> getParentBeans() {
		if (parent == null){
			return new HashMap<Class<?>, Set<Object>>();
		}else{
			return parent.getClassedBeans();
		}
	}

	/**
	 * Add a bean represented by this node
	 * @param bean
	 */
	public void addBean(Object bean) {
		if (! isWildcard()){
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
	}
	/**
     * Adds beans represented by this node
     * @param beans a collection of beans to be added
     */
	public void addBeans(Collection<?> beans) {
		for (Object bean : beans){
			addBean(bean);
		}
	}

	/**
	 * Removes all beans representing this node.
	 */
	public void resetBeans(){
		beans.clear();
	}


// ************************* OVERRIDES **********************************/

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
			return "/";    //for root node
		}else{
			String result = parent.toString() + ((parent.parent == null) ? "" : ".") + path;
			return result;
		}
	}

	public String toStringNoWildcard(){
		return toString().replace(".$", "").replace(".*", "");
	}

	public String toStringTree() {

		String result = (path.equals("") ? "/" : path) + "\n";

		for (BeanInitNode child : getChildrenList()){
			result += toString() + (result.endsWith("/\n") ? "" : ".") + child.toStringTree();
		}

		return result;
	}

	public boolean hasWildcardToOneSibling() {
		BeanInitNode sibl = getWildcardSibling();
		return (sibl != null && sibl.isToOneInitialized);
	}


}
