# Comment to jul_13_2017_experiment

This is a small DMM which should accumulate the list of mouse clicks and render them in an associated graphics window.

It is mostly meant to illustrate that if one uses a neural network based on flows of vectors (in this case, flows of V-values which are vectors based on nested dictionaries) instead of flows of numbers, then it is easy to implement usual programming operations such as consing the list, which are pretty difficult to implement within the conventional neural nets.

The network itself is defined in lines 38-78 of the `jul_13_2017_experiment.clj` file. It consists of 3 neurons:

  *  the Self neuron (with `v-accum` activation function and name `:self`) which holds the network matrix (which does not change in this particular example, but in general the network is capable of modifying itself (see `jul_6_2017_experiment` example for that));

  *  the neuron tracking the mouse clicks and emitting the mouse position on mouse clicks (with activation function `v-mouse-pressed-monitor` and name `:mouse-pressed-monitor`)

  *  the neuron accumulating the list of meaningful inputs (with activation function `v-dmm-cons` and name `:my-list`)

(There is also an active neuron with activation function `v-mouse-coords` and name `:mouse-position` left from `jul_12_2017_experiment`, but in this network it is not connected to anything besides itself. It emits the current mouse coords regardless of clicks. We decided to keep it here for convenience.)

(With this formalism, one can have rather powerful neurons, if one wants to, so the programs/networks often have a small number of them.)
