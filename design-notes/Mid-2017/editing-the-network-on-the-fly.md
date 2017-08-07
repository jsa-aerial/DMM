# Editing the generalized neural network on the fly.

The Oct 28, 2016 and Jul 6, 2017 experiments demonstrate
the ability of DMM to edit itself on the fly using
connections from update neurons to the `:self` neuron.

The Jul 13, 2017 demonstrate the ability of DMMs to
build and change data structures and to react interactively
to the inputs such as mouse clicks.

There are a number of ways to edit a running neural network
on the fly in this context. On one end of the spectrum, a traditional
engineering way is available under Quil: as long as one edits
the part of the program callable from `:update` handler of
the sketch, the behavior will change on the fly.

On the other end of the spectrum we are inspired by high-end multimedia
capabilities: the ability of modern video editing software and
VJ performance software to apply effects and otherwise
edit videos and animations on the fly, while those videos
and animations are running. We are also inspired by the
modern abilities to interactively sculpt 3-D objects in
virtual reality.

While we are eventually aiming towards that other, high end of the spectrum,
here are some modest intermediate steps we can take now.

Being inspired by our Jul 6 and Jul 13 experiments, we
can start to introduce modest mechanisms allowing to create
and edit V-values including the network matrices themselves,
by the network itself via update neurons.

The main ingredients are: V-values to be edited, paths in
V-values (basis vectors of the space V), numerical sliders,
text buffers (e.g. when a new hash-key not previously used
needs to be added), accumulator metaphor, handlers of
mouse clicks and other relevant mouse movements, handlers
of keyboard events.
