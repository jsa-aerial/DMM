# lessons from april 2018 experiments in image processing and rendering in DMMs

## More linear operation types are desirable

Using numbers in the image structure to express the containing rectangle shows that
one would like to also have access to linear operations where + stands for `max`
("tropical arithmetic"), or, if one wants to also express the variable upper left
corner, where + dually stands for `min`.

## It is not clear if performance of hash maps with array keys (e.g. `[num1 num2]`)
is good enough

In general, slowdown from transforming such maps was higher than I expected, and
this requires further thought.

## A natural programming style with memory based on accumulator neurons seems
to be emerging. 

The `nu-general` function which sends data to update the network using 
arbitrary output of the update neuron is useful in this context.
Different outputs can be connected to different accumulator neurons, e.g.

`(nu [v-accum :test-image :accum] [v-accum :test-image :single] 1)`

`(nu [v-accum :test-image :delta] [v-network-update-monitor :network-interactive-updater :to-test-image] 1)`

`(nu-general :test-image :to-test-image)`

Here we use `:to-test-image` output of the update neuron, whereas the `nu` function
uses `:direct` output of the update neuron.

This pattern is consistent with the observations of an ACL 2018 paper by a U. of Washington team,
"Long Short-Term Memory as a Dynamically Computed Element-wise Weighted Sum"

https://arxiv.org/abs/1805.03716

So this is the pattern we might want to pursue more, since it is already occurring
naturally in our context. Here we have neurons connected via the accumulator connection pattern
and acting as active memory units, and the idea to build a machine learning model solely based 
on such memory units does feel more and more attractive recently.

This is also consistent with an observation stemming
from a recent paper introducing Recurrent Identity Networks, which notes that the
matrix responsible for a recurrent connection should ideally be `1 + epsilon` for a 
recurrent setup to work well:

http://www.cs.brandeis.edu/~bukatin/recurrent-identity-networks.html
