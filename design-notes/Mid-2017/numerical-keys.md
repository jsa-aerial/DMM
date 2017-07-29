# Numerical keys and flattening.

We have more than one situation when numerical hash keys
and/or flattened indices are desirable. At the same time,
we would like to preserve abritrary hash-keys and
hierarchical indexes as the main way to look at the
network matrices.

## Situations.

The use of `core.matrix` requires numerical hash keys.
Flatting is optional, but might be rather useful.
The ability to keep numerical hash keys small would
make the matrices less sparse (and penalties for
using dense versions less severe).

Visualization of the changing network matrices
similar to 
https://github.com/anhinga/fluid/tree/master/Lightweight_Pure_DMMs/aug_27_16_experiment
can really benefit from the ability to flatten
indexes and moreover, from the ability to use
numerical hash keys which are flattened, small,
and *manually controlled*.

In any case, we'll be talking about a one-to-one
correspondence (a bijection) between a finite set of
general hash key triplets (which should include
all triplets present in the network matrix at the
moment) and a finite set of numbers; and we'd like
an option to manually control this correspondence
while enforcing its correctness (such as the same
number does not correspond to different triplets,
etc.).

## Starting to think about the mechanisms.

First of all, note that these reshapings between
three-dimensional and one-dimensional arrays are
very simple linear operators, and they can be
supported inside the network, or they can supported
as "extra" support mechanisms which are not
implemented as parts of the network.

Eventually, it would be desirable to have these mechanisms
inside the network, but whether to do so from the
start remains to be seen.
