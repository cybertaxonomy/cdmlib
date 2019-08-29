/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common.concurrent;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Concurrent queue. Example taken from
 * {@link
 * https://stackoverflow.com/questions/33195290/producer-consumer-model-using-javasynchronized-but-always-run-the-same-thread
 * } for Producer-Consumer pattern.
 *
 * @author a.mueller
 * @since 26.08.2019
 */
public class ConcurrentQueue<T> {

    private static final Logger logger = Logger.getLogger(ConcurrentQueue.class);

    private int capacity;
    private List<T> queue = new LinkedList<>();

    public ConcurrentQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void enqueue(T item) throws InterruptedException {
        while (queue.size() == this.capacity) {
            wait();
        }

//            System.out.println("Thread " + Thread.currentThread().getName() +
//                               " producing " + item);
        queue.add(item);
        printSize();

        if (queue.size() >= 1) {
            notifyAll();
        }
    }

    public synchronized T dequeue() throws InterruptedException {
        T item;

        while (queue.size() == 0) {
            wait();
        }

        item = queue.remove(0);
        printSize();
//            System.out.println("Thread " + Thread.currentThread().getName() +
//                               " consuming " + item);

        if (queue.size() == (capacity - 1)) {
            notifyAll();
        }

        return item;
    }

    /**
     *
     */
    private void printSize() {
        logger.warn("size:" + queue.size());

    }

}
