/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.compare.common.TreeIndexComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.TermNode;

/**
 * A class to handle tree indexes as used in {@link TaxonNode}, {@link TermNode}
 * etc.<BR>
 * Might be come a hibernate user type in future.
 *
 * @author a.mueller
 * @since 02.12.2016
 */
public class TreeIndex {

    public static String sep = "#";

    //regEx, we also allow the tree itself to have a tree index (e.g. #t1#)
    //this may change in future as not necessarily needed
    private static String regEx = sep+"[a-z](\\d+"+sep+")+";
    private static Pattern pattern = Pattern.compile(regEx);

    private static TreeIndexComparator comparator = new TreeIndexComparator();

    private String treeIndex;

//***************** FACTORY ********************************/

    public static TreeIndex NewInstance(String treeIndex){
        return new TreeIndex(treeIndex);
    }

    public static TreeIndex NewInstance(TaxonNode node) {
        if (node == null){
            return null;
        }else{
            return new TreeIndex(node.treeIndex());
        }
    }

    public static List<TreeIndex> NewListInstance(List<String> stringList) {
        List<TreeIndex> result = new ArrayList<>();
        for (String string: stringList){
            result.add(new TreeIndex(string));
        }
        return result;
    }

// ******************* CONSTRUCTOR ********************/

    private TreeIndex(String treeIndex){
        if (! pattern.matcher(treeIndex).matches()){
            throw new IllegalArgumentException("Given string is not a valid tree index");
        }
        this.treeIndex = treeIndex;
    }

// ************** METHODS ***************************************/

    public boolean hasChild(TreeIndex childCandidateTreeIndex) {
        return childCandidateTreeIndex.treeIndex.startsWith(treeIndex);
    }

    /**
     * Returns a new TreeIndex instance which represents the parent of this tree index.
     * Returns null if this tree index already represents the root node of the tree.
     */
    public TreeIndex parent(){
        int index = treeIndex.substring(0, treeIndex.length()-1).lastIndexOf(ITreeNode.separator);
        try {
            TreeIndex result = index < 0 ? null : NewInstance(treeIndex.substring(0, index+1));
            return result;
        } catch (Exception e) {
            //it is not a valid treeindex anymore
            return null;
        }
    }

    public boolean isTreeRoot(){
        int count = 0;
        for (char c : this.treeIndex.toCharArray()){
            if (c == '#') {
                count++;
            }
        }
        return count == 3;
    }

    public boolean isTree(){
        int count = 0;
        for (char c : this.treeIndex.toCharArray()){
            if (c == '#') {
                count++;
            }
        }
        return count == 2;
    }

// ********************** STATIC METHODS  *****************************/

    /**
     * Returns a list of string based tree node ids of all ancestors. Starts with the highest ancestor.
     *
     * @param includeRoot if the root node which has no data attached should be included
     * @param includeTree if the tree node which precedes the treeindex representing the tree should be included
     */
    public List<String> parentNodeIds(boolean includeRoot, boolean includeTree){
        String[] splits = treeIndex.split(sep);
        List<String> result = new ArrayList<>();
        for (int i = 1; i<splits.length; i++){  //the first split is empty
            if (i > 2 || i == 1 && includeTree || i == 2 && includeRoot){
                result.add(splits[i]);
            }
        }
        return result;
    }

    /**
     * Returns a list of integer based tree node ids of all ancestors. Starts with the highest ancestor.
     * @param treeIndex the tree index
     * @param includeRoot if the root node which has no data attached should be included
     */
    public List<Integer> parentNodeIds(boolean includeRoot){
        List<String> split = parentNodeIds(includeRoot, false);
        List<Integer> result = new ArrayList<>();
        for (String str:split){
            result.add(Integer.valueOf(str));
        }
        return result;
    }

    /**
     * Creates a list for the given tree indexes and sorts them in ascending
     * order.
     * @param treeIndexSet
     * @return
     */
    public static List<TreeIndex> sort(Collection<TreeIndex> treeIndexSet) {
        List<TreeIndex> result = new ArrayList<>(treeIndexSet);
        Collections.sort(result, comparator);
        return result;
    }

    /**
     * Creates a list for the given tree indexes and sorts them in descending
     * order.
     */
    public static List<TreeIndex> sortDesc(Collection<TreeIndex> treeIndexSet) {
        List<TreeIndex> result = sort(treeIndexSet);
        Collections.reverse(result);
        return result;
    }

    public static Map<TreeIndex, TreeIndex> group(Collection<TreeIndex> groupingIndexes, Collection<TreeIndex> toBeGroupedIndexes){

        //for larger groupingIndexes we could optimize this by sorting both collections
        //prior to loop. This way we do traverse both lists once
        Map<TreeIndex, TreeIndex> result = new HashMap<>();
        List<TreeIndex> descSortedGroupingIndexes = sortDesc(groupingIndexes);

        for (TreeIndex toBeGrouped : toBeGroupedIndexes) {
            boolean groupFound = false;
            for (TreeIndex groupingIndex : descSortedGroupingIndexes){
                if (groupingIndex.hasChild(toBeGrouped)){
                    result.put(toBeGrouped, groupingIndex);
                    groupFound = true;
                    break;
                }
            }
            if (!groupFound){
                result.put(toBeGrouped, null);
            }
        }
        return result;
    }

// **************************** EQUALS *****************************/

    @Override
    public int hashCode() {
        return treeIndex.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeIndex){
            return treeIndex.equals(((TreeIndex)obj).treeIndex);
        }else{
            return false;
        }
    }

// *************************** toString() ***********************

    @Override
    public String toString(){
        return treeIndex;
    }

    /**
     * Null save toString method.
     */
    public static String toString(TreeIndex treeIndex) {
        return treeIndex == null? null: treeIndex.toString();
    }

    public static List<String> toString(Collection<TreeIndex> treeIndexes) {
        List<String> result = new ArrayList<>();
        for (TreeIndex treeIndex : treeIndexes){
            result.add(treeIndex.toString());
        }
        return result;
    }
}
