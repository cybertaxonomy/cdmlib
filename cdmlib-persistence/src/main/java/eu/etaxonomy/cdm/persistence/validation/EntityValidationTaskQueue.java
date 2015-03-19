/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A job queue catering to the needs of the entity validation process. If an
 * entity validation task is submitted to the queue, and the queue already
 * contains tasks validating the exact same entity, those tasks should be
 * removed first, because they are validating a state of the entity that is no
 * longer actual. Note that it is not fatal to validate the entity in those
 * intermediary states, because in the end the final state of the entity does
 * get validated as well. It's just useless and may lead to unnecessary
 * contention of the queue.
 *
 * @author ayco_holleman
 *
 */
@SuppressWarnings("serial")
class EntityValidationTaskQueue extends ArrayBlockingQueue<Runnable> {

    public EntityValidationTaskQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean add(Runnable r) {
        cleanup(r);
        return super.add(r);
    }

    @Override
    public boolean offer(Runnable r, long timeout, TimeUnit unit) throws InterruptedException {
        cleanup(r);
        return super.offer(r, timeout, unit);
    }

    @Override
    public boolean offer(Runnable r) {
        cleanup(r);
        return super.offer(r);
    }

    @Override
    public void put(Runnable r) throws InterruptedException {
        cleanup(r);
        super.put(r);
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> c) {
        throw new RuntimeException("Submitting multiple validation tasks at once not supported");
    }

    private void cleanup(Runnable runnable) {
        EntityValidationTaskBase newTask = (EntityValidationTaskBase) runnable;
        Iterator<Runnable> iterator = this.iterator();
        while (iterator.hasNext()) {
            EntityValidationTaskBase oldTask = (EntityValidationTaskBase) iterator.next();
            if (oldTask.equals(newTask)) {
                iterator.remove();
            }
        }
    }

}
