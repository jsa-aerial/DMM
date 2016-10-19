# Recurrent maps and related topics

Recurrent maps are maps which map arbitrary legal hash-map keys
into numbers or other recurrent maps.

Recurrent maps are a nice candidate for the role of a "universal vector space".

The initial version of

https://github.com/jsa-aerial/DMM/blob/master/dmm-snippets.clj

implements linear algebra on recurrent maps.

## Recurrent maps for dataflow matrix machines

At the moment we can just consider dataflow matrix machines
with one kind of linear streams - recurrent maps. The recurrent
maps seems to be rich enough to subsume the variery of
linear streams we need for the initial experimental release.

The remaining variability between different types of neurons
(besides their built-in stream transformations) is the number
of input and output streams and their names.

Fortunately, recurrent maps are rich enough to accommodate
this diversity. Let's allow only one argument and one
return result for any built-in stream transformation,
and let's stipulate that those must be recurrent maps, 
and let's agree that ihe first level of hash-map keys
would name the respective input and output
streams and map to their latest values.

## Non-flat structure of DMM indexes.

It seems convenient to keep the natural multilevel structure
while coding the network, and not to try to flatten the indexes
into one level.

Therefore the root keys should be types of neurons.

The only thing that distinguishes the neuron types at this
point are the names of the functions implementing the built-in
stream transformations. Symbols serving as names of these functions
might be a good choice for the top-level keys in the network,
given that, for example, the following works nicely

`(map (fn [x] (eval (list (x 0) (x 1)))) m)`

when `m` is the dictionary mapping the function
names to the arguments the functions are to be applied to.

### The natural structure of the row of the network matrix.

A map from names of the built-in transformers to
(the map from names of the individual neurons to
(the map from names of their output streams to the
respective non-zero matrix coefficients)).

Approximately similar structure should be of the matrix
itself (with the caveat that we might blend the matrix into
the overall network).

But without this caveat: a map from names of the built-in transformers to
(the map from names of the individual neurons to
(the map from names of their output streams to the
respective non-zero matrix rows)).

Caveat 1: If a neuron is mentioned with a row, we want it to be present
in the top-level map, even if there are no non-empty rows associated with
it (in this case, an empty map `{}` is associated with this neuron).

Caveat 1 continued: however, for the time being we require
a non-trivial input to a neuron, which it is free to ignore.
E.g. one can input some fake thing, or even the network matrix itself,
or even the output of the neuron itself with coefficient one,
and that should activate it today.

Caveat 2: we have not yet decided whether to find a place for the
current values of the input and output streams within the matrix,
or on the side (something to ponder).

