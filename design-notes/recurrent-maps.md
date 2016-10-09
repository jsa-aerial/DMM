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
would name (and map to) the respective input and output
streams.

