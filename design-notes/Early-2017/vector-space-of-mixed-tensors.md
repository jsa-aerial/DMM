# Vector space of mixed tensors (recurrent maps)

Consider a countable set **L** which is a subset of legal keys for
Clojure hash-maps. Consider finite ordered sequences of non-negative length
of the elements of **L** and calls those sequences *paths*, and denote the
set of those paths as **P**. Use **P** as a basis to generate vector space,
and consider the space **V** of finite linear combinations of the elements
of **P**.

That is, **V** consists of the sets of reals indexed by **P**
(functions from **P** to reals), such that only finite number of
those reals are non-zero in every such indexed set. If one considers

https://en.wikipedia.org/wiki/Examples_of_vector_spaces#Infinite_coordinate_space

we are talking about a co-product (direct sum) of one-dimenstional spaces.

The reason why we call elements of **P** paths is that we tend to think
about elements of **V** as *finite trees*. For any given vector **v**,
only the paths **label-1, ..., label-n** which index non-zero
coordinates of **v** are present, and those non-zero numbers are
the *leaves* at the end of those paths.

## Mixed tensors

We use the notion of *tensor of rank N* in the fashion customary in
machine learning, namely to denote an N-dimensional matrix.

If the only path in the tree is of the length 0, we think about the vector in
question as a scalar (tensor of rank 0).

If all paths are of the length 1, we think about the vector in question
as a one-dimensional vector (tensor of rank 1).

If all paths are of the length 2, we think about the vector in question
as a matrix (tensor of rank 2).

Et cetera...

Now consider an arbitrary finite tree from **V**. It contains paths of
various lengths. Paths of the length 1 correponds to coordinates of
a one-dimensional vector, paths of the legnth 2 correspond to the
elements of a matrix, etc.

Therefore, we call an arbitrary **v** from **V** a *mixed tensor*.
It can contain coordinates for tensors of rank 1, 2, 3, etc, together
with a scalar (the coordinate for the tensor of rank 0) at the same time.

(The alternative terminology might be a *mixed rank tensor*.)

## Recurrent maps

We implement the space **V** via recurrent maps. Recurrent maps are allowed
to map any legal key for Clojure hash-maps to either a real number or
another recurrent map.

The main design decision in this representation is how to represent
**v** which has a non-zero scalar coordinate and some non-zero coordinates
corresponding to higher tensors.

In our 2016 implementation we decided to use a reserved key `:number`
which we don't include in our set **L**. We allow the use of
this key in our paths, and we say that **label-1, ..., label-n, x** is
equivalent to **label-1, ..., label-n, :number, x**.

So, when one needs to add a non-zero scalar *x* to a non-zero tensor of
higher rank than zero, one starts with replacing this scalar with
an equivalent one-dimensional vector `:number x`, and then one performs
the addition.

In our 2016 implementation we also consider the possibility that
the key is mapped not to a number or a recurrent map, but to something
else, and in our 2016 implementation we interpret this situation as
an equivalent of having a zero leaf at this position (which in turn
is equivalent to not having this path in the tree at all).

In our 2016 implementation we tend to represent zero vector by
the empty hash-map, `{}`.

The current implementation of this vector space is by
functions `rec-map-mult` (multiplication of vector by scalar)
and `rec-map-sum` (sum of vectors, the case of two arguments
implements the canonical operation for the definition of vector space).

https://github.com/jsa-aerial/DMM/blob/master/src/dmm/core.clj

## Final remark

The description in terms of space of finite trees with intermediate
nodes labeled by legal hash keys from **L** and with real numbers
as their leaves is a mathematically clean way to think about this space,
and therefore this description should be considered fundamental.

Implementation-wise, the space of recurrent maps seems to be the most
convenient at the moment. However, mathematically speaking one needs
to pay attention to various delicate issues such as the equivalence
between `x` and `:number x` in the paths. So at the moment we are
treating the space of recurrent maps as implementation convenience
for the space described in the previous paragraph.
