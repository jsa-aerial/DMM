# Time slicing

The layer structure of a network can be specified by sparsity structure of the network matrix.
However, all layers would still function simultaneously.

If it is desirable for the layers to function in turn, one can orchestrate
their action by using multiplicative masks. However, this does create
the need for optimization in order to avoid wasting computational cycles
on layers silenced for the moment by multiplicative masks.

The other option is **time slicing**. Then the network matrix at any given
moment contains the connectivity required to create the inputs for the
layer to be evaluated during the next *up movement*.

This would be quite wasteful in an architecture requiring rewrite of the
matrix elements in place, but should be quite efficient in our architecture
of immutable data and shared common substructures.

This does require that we stop wiring `Self` neuron as an accumulator and
use different wiring patterns instead.

## Matrix registers

We can think about the particular output of `Self` containing the
network matrix to be used during the next down movement as an *active matrix register*.

Currently, this output is named `Single`, but this should change for time slicing schemas;
the relevant output might be called `Active`, or something like that.

We can think about neuron outputs containing network matrices for various steps
(or templates from which those matrices are formed on the fly) as *matrix registers*.

They can all belong to `Self`, or they can be spread between multiple neurons.

It is likely that the convenient activation function for `Self` would be identity.

If one wants to conduct a single static loop of several matrices, it is easy to accomplish:
each matrix should make sure to place its successor to the `Active` *input* of `Self`.

However, one can make the control of the sequences of matrices very flexible, if desired.


*To be continued...*