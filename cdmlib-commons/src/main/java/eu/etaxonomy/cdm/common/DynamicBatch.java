// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * DynamicBatch: a JVM resources aware batch manager.
 *
 * @author a.kohlbecker
 * @date Jul 4, 2016
 *
 */
public class DynamicBatch {

    public static final Logger logger = Logger.getLogger(DynamicBatch.class);

    int batchSize;
    int batchItemCount = -1;
    Long batchMinFreeHeap = null;
    List<Integer> items = null;

    int gcTimeIncreaseCount = 0;

    private int allowedGcIncreases = -1;

    private int itemWhereLimitsTouched = 0;


    List<Integer> unprocessedIds = new ArrayList<Integer>(batchSize);

    private final JvmMonitor jvmMonitor = new JvmMonitor();

    private final long intitialFreeHeap;

    public DynamicBatch(int initialSize) {

        this.batchSize = initialSize;
        this.intitialFreeHeap = jvmMonitor.getFreeHeap(true);
    }

    public DynamicBatch(int initialSize, long minInitialHeap) throws JvmLimitsException {

        this.batchSize = initialSize;
        this.intitialFreeHeap = jvmMonitor.getFreeHeap(true);
        if(this.intitialFreeHeap < minInitialHeap) {
            throw new JvmLimitsException("At least " + minInitialHeap + " byte of free Heap space required but only " + intitialFreeHeap + " byte available.");
        }
    }

    /**
     * @param requiredFreeHeap
     * @throws JvmLimitsException
     */
    public void setRequiredFreeHeap(double requiredFreeHeap) throws JvmLimitsException {

        this.batchMinFreeHeap = (long) (intitialFreeHeap * requiredFreeHeap);
        if(memoryLimitsExceeded()) {
            throw new JvmLimitsException("Not enough free heap for batch");
        }
    }

    /**
     * @param requiredFreeHeap
     * @throws JvmLimitsException
     */
    public void setRequiredFreeHeap(long requiredFreeHeap) throws JvmLimitsException {
        this.batchMinFreeHeap = requiredFreeHeap;
        if(memoryLimitsExceeded()) {
            throw new JvmLimitsException("Not enough free heap for batch");
        }
    }

    /**
     *
     * @param allowedGcIncreases the amount of continiously observed increases of the gc time
     */
    public void setMaxAllowedGcIncreases(int allowedGcIncreases) {
        this.allowedGcIncreases = allowedGcIncreases;
        // reset GCtime
        jvmMonitor.getGCtimeSiceLastCheck();
    }

    public int size() {
        return batchSize;
    }

    public  List<Integer> items(){
        return items;
    }

    public boolean hasUnprocessedItems() {
        return unprocessedIds.size() > 0;
    }

    /**
     * 1. Fills all remaining items into the new batch and pads with next items from the iterator.
     *
     * 2. Resets the internal batchItemCount!!
     *
     * @param itemIterator
     * @return
     */
    public List<Integer> nextItems(Iterator<Integer> itemIterator){

        logger.debug("new batch of items with size of " + batchSize);
        items = new ArrayList<Integer>(batchSize);
        if(unprocessedIds.size() > 0) {
            List<Integer> remainingUnprocessed = null;
            Iterator<Integer> unprocessedIt = unprocessedIds.iterator();
            int i = 0;
            while(unprocessedIt.hasNext()) {
                Integer nextUnprocessed = unprocessedIt.next();
                if(i < batchSize) {
                   items.add(nextUnprocessed);
                } else {
                    if(remainingUnprocessed == null) {
                        remainingUnprocessed = new ArrayList<Integer>(unprocessedIds.size() - i + 1);
                    }
                    remainingUnprocessed.add(nextUnprocessed);
                }
                i++;
            }
            unprocessedIds.clear();
            if(remainingUnprocessed != null) {
                unprocessedIds = remainingUnprocessed;
            }
        }

        while(itemIterator.hasNext() && items.size() < batchSize ) {
            items.add(itemIterator.next());
        }

        itemWhereLimitsTouched =  0;
        batchItemCount = 0;

        return items;
    }

    public void incementCounter() {
        batchItemCount++;
    }

    /**
     *
     */
    private void reduceSize() {
        manageUnprocessedItems();
        batchSize = itemWhereLimitsTouched;
        if(batchSize < 1) {
            batchSize = 1;
        }
    }

    public void reduceSize(double by) {
        manageUnprocessedItems();
        batchSize = (int) (batchSize * by);
        if(batchSize < 1) {
            batchSize = 1;
        }
    }

    /**
     *
     */
    protected void manageUnprocessedItems() {

        if(itemWhereLimitsTouched > 0) {
            int batchItemsUnprocessed = items.size() - itemWhereLimitsTouched;
            logger.info("batchSize reduced to " + itemWhereLimitsTouched);
            if(batchItemsUnprocessed > 0) {
                unprocessedIds.addAll(items.subList(items.size() - batchItemsUnprocessed, items.size()));
            }
        }
    }


    public boolean isWithinJvmLimits() {
        if(memoryLimitsExceeded()) {
            logger.info("memoryLimitsExceeded ==> reducing batchSize");
            reduceSize();
            return false;
        }
        if(allowedGcIncreases > 0 && gcLimitsExceeded()) {
            logger.info("gcIncreaseLimitExceeded ==> reducing batchSize");
            reduceSize();
            return false;
        }

        return true;
    }

    public boolean gcLimitsExceeded() {

        long gctimeSiceLastTime = jvmMonitor.getGCtimeSiceLastCheck();
        if(gctimeSiceLastTime > 0) {
            if(gcTimeIncreaseCount == 0) {
                itemWhereLimitsTouched  = batchItemCount;
            }
            gcTimeIncreaseCount++;
            logger.debug("gctimeSiceLastTime: " + gctimeSiceLastTime + ", gcTimeIncreaseCount: " + gcTimeIncreaseCount);
        } else {
            gcTimeIncreaseCount = 0;
            itemWhereLimitsTouched = 0;
        }
        return gcTimeIncreaseCount > allowedGcIncreases;

    }


    public boolean memoryLimitsExceeded() {

        if(!jvmMonitor.hasFreeHeap(batchMinFreeHeap)) {
            if(batchItemCount > -1) { // not in initial state, that it before first batch
                itemWhereLimitsTouched  = batchItemCount;
            }
            logger.debug("min free heap limit (" + batchMinFreeHeap + ") exceeded ");
            return true;
        } else {
            return false;
        }

    }

    public JvmMonitor getJvmMonitor() {
        return jvmMonitor;
    }

}
