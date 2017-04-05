# A schema for population coordinate descent

This has two sources of motivation. First, we would like to have
an adaptive optimization schema which would avoid relatively complicated
analytics, such as computing the gradients or the uncorrelated
orthogonal bases.

Another source of motivation is that in our framework a network
(a DMM, a program) is represented by a group of weights 
(that is, by a linear combination of coordinates, i.e.
a generalized coordinate). It is natural to be able to take
such a semantically meaning unit, and add it to or subtract it
from the network being optimized. This ability to add or subtract
(perhaps with a weight) the semantically meaningful units is
one of the original motivating features for DMMs as a software
framework.

## Population of coordinates

The idea for this scheme is to repeat the following step:
pick a group of weights (a generalized coordinate) and perform
a step of coordinate descent along that coordinate.

The non-trivial part is how to pick a group of weights
(a generalized coordinate) for the next step.

In this scheme, we do not try to maintain a linearly
independent system of coordinates. Instead, we maintain
an overdefined coordinate system (a *population of coordinates*)
and we maintain an adaptive schema of sampling from
this population.

One can either think about a finite population of
generalized coordinates with an adaptive probability
dsitribution/adaptive sampling schema over that population,
or one can actually think that all possible
finite groups of weights (generalized coordinates)
belong to the population, but it's OK to have zero
probability/zero chance of being sampled from
associated with any subset of that.


