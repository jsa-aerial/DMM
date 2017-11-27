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
use different wiring patterns.

*To be continued...*