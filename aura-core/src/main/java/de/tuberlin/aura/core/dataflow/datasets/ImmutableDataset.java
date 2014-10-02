package de.tuberlin.aura.core.dataflow.datasets;

import de.tuberlin.aura.core.dataflow.operators.base.IOperatorEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImmutableDataset<E> extends AbstractDataset<E> {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private List<E> data;

    // ---------------------------------------------------
    // Constructor.
    // ---------------------------------------------------

    public ImmutableDataset(final IOperatorEnvironment environment) {
        super(environment);

        this.data = new ArrayList<>();
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    @Override
    public void add(E element) {
        // sanity check.
        if (element == null)
            throw new IllegalArgumentException("element == null");

        data.add(element);
    }

    @Override
    public Collection<E> getData() {
        return data;
    }

    public void setData(final Collection<E> data) {
        // sanity check.
        if (data == null)
            throw new IllegalArgumentException("data == null");

        this.data = new ArrayList<>(data);
    }
}