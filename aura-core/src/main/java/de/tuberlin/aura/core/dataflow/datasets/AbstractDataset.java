package de.tuberlin.aura.core.dataflow.datasets;

import de.tuberlin.aura.core.dataflow.operators.base.IExecutionContext;

import java.util.Collection;

public abstract class AbstractDataset<E> {

    public enum DatasetType {

        UNKNOWN,

        DATASET_INTERMEDIATE_RESULT,

        DATASET_ITERATION_HEAD_STATE,

        DATASET_ITERATION_TAIL_STATE
    }

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    final protected IExecutionContext context;

    // ---------------------------------------------------
    // Constructor.
    // ---------------------------------------------------

    public AbstractDataset(final IExecutionContext context) {
        // sanity check.
        if (context == null)
            throw new IllegalArgumentException("context == null");

        this.context = context;
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public abstract void add(final E element);

    public abstract Collection<E> getData();

    public abstract void clear();

    public abstract void setData(final Collection<E> data);
}
