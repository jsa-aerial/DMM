# Lessons from July 13, 2017 experiment

A Quil-controlled network, with a neuron tracking "mouse-pressed" events,
and a neuron accumulating a list of non-trivial inputs.


## Lessons from the list-accumulating neuron.

  1. The accumulator metaphor can be used not only to accumulate sums,
  but to accumulate complex data structures, e.g. lists.
  
    * One still connects an input and an output of the neuron in question
    with weight one. A fruitful idiom is to usually have the identity transform
    between this input and this output built into the neuron, except if
    something interesting came on some other inputs.

  2. In particular, a neuron can keep a log of its activity via this
  mechanism, and to have this log handy as one of the inputs.
  
  3. One can avoid the need for precise coordination between the
  neurons, if one accumulates the inputs values which need to be in sync within the
  "identity cycles", so that they persist for some time.
  
## Lessons from the click-tracking neuron.
