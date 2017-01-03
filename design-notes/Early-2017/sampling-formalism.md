# Sampling formalism

This is a design sketch for future work.

Vector space of recurrent maps is sufficiently flexible to extend it
with various constructions. The reserved key `:number` allows to
explicitly embed a number into a level, when the level in question
has complex structure and therefore just having the number
implicitly as a leaf is insufficient, but instead this number must
be marked as a field of a more complex structure.

Similarly, other reserved keys can be added to allow having other
kinds of representation of various vectors. E.g. one could allow
to have functions transforming an arbitrary argument into a real,
e.g. one could allow mapping a reserved key `:function` into something
like `(fn [x] expression-returning-real-number)`.

In this text we focus on the ability to represent probability
distributions (or even more general structures, such as signed measures
or even complex-valued distributions), by samples. The distributions
in question are vectors (usually over reals, but we just mention
the complex-valued case, in case there is a desire to consider
complex-valued DMMs in the future). However, the samples can be any
values with arbitrarily complex structure expressible in Clojure.
Basically, the distributions can be over any space **X** whatsoever,
and it's fine to consider the space of all possible Clojure values
as **X** in this context.

**X** itself is not a vector space, so we will be defining linear combinations
in such a manner as to obtain samples representing linear combinations
of the underlying distributions.

We use the reserved key `:sample`, and allow to map it to the
elements of **X**.

## Linear combination via statistical sum

Normally, if one has a *0 < a < 1* coefficient, and one has a sample
*x* from the probability distribution *P* and a sample *y* from
the probability distribution *Q*, then to take a sample from
the linear combination of the distributions *aP+(1-a)Q*, one
draws a random number *r* uniformly distributed in *[0,1]*,
and if this number is less than *a*, one takes *x* as the sample
representing the linear combination *aP+(1-a)Q*, otherwise one takes *y*.

This is the way to compute *convex linear combination with
positive coefficients* for samples of probability distribition via
*statistical sum*.

## Linear combinations of samples with negative coefficients.

It is convenient to allow quasi-probability distributions
with negative values of probabilities being legal,
and to allow negative coeffients in the linear combinations.

After all, we would like to have genuine vector spaces in our formalism.

So we consider spaces of signed measures. We take pairs
*(x, flag)* as samples, where flag can be 1 or -1. Samples drawn
from the areas of negative probability are supposed to have the -1
value of the flag.

Multiplication by -1 flips the flag.

So, when one needs to perform the statistical sum with coefficients
which might be positive or negative, one uses absolute values of
those coefficients to decide which component the statistical sum must
select, but then if the corresponding coefficient is negative, the
flag of the selected sample must be flipped.

## Linear combinations of samples with complex coefficients.

Similarly to the previous section, for the complex case
samples are pairs *(x, phase)*, and samples drawn from the areas where
complex probability has a fixed phase *phi* are supposed to
look like *(x, phi)*.

Then similarly to the previous section, when a linear combination of
distributions with complex coefficients is considered, one uses
absolute values of those coefficients to decide which component
the statistical sum must select, but then if the corresponding
coefficient has phase *psi*, and the selected sample is
*(x, phi)*, then the resulting sample will be *(x, phi+psi* modulo 2 * *pi)*

The previous section is a partial case of this, with -1 flag corresponding
to *pi*.

## Missing samples and zero measures.

`:sample` leaves can be present on any level of the tree (the recurrent map)
and can coexist with `:number` leaves and other subtrees. When evaluating
linear combinations, one simply applies operations described in this text
instead of the usual numerical operations when `:sample` leaves occur.

A non-trivial consideraion is what to do, if a leaf is picked by a statistical
sum, but the `:sample` field is missing. It would be nice to have a reasonable
amd error-free interpretation of such situation.

First, we supply the answer to this question: if such a situation occurs,
the rule is that the `:sample` field will be missing in the result.

Theere are a few lines of thought, which can be used to justify this rule.
The lack of sample is the correct way to sample from zero measure.

Imagine a sub-probability measure with total probability *P<1*. Then it would
be nice if on *1-P* fraction of the occasions a sample would be missing
(informally speaking, one could think about this as sampling from the
missing "rest of the distribution").

Then one might think what should happen with probability measures with
total probability over 1. The whole topic provides quite a bit of material
for further meditation.

So this document is to serve only as an initial sketch.
