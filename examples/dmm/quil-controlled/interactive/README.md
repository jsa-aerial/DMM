this is a subdirectory for interactive experiments, with the intention to implement along these lines:

https://github.com/jsa-aerial/DMM/blob/master/design-notes/Mid-2017/editing-the-network-on-the-fly.md

Jan 24, 2018 - it is a variant of Jul 13, 2017 experiment which, in addition, allows to
imitate a mouse click by typing something like `(mp 120 50)` in the graphics window;
so this does not edit the network yet, but just exercises the interaction pipeline.

Jan 25, 2018 - this version can actually do network updates on the fly, demonstrating
this capability. Tested lightly, by checking that typing
`(nu [v-dmm-cons :my-list :self] [v-dmm-cons :my-list :self] -0.1)` actually does what's
expected. Other than that, this implementation introduces `v-path` macro (to be moved
to `core` eventually) and the backspace capability.

May 14, 2018 - prototype of the system for practical livecoding experiments 
(see [comment_to_mar_14_2018_experiment.md](comment_to_mar_14_2018_experiment.md)
for details).

Apr 8, 2018; Apr 15, 2018 - tentative experiments with Quil images
(see [comment_to_april_2018_experiments.md](comment_to_april_2018_experiments.md)
for lessons).
