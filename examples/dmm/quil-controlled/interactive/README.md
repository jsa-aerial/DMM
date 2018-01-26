this is a subdirectory for interactive experiments, with the intention to implement along these lines:

https://github.com/jsa-aerial/DMM/blob/master/design-notes/Mid-2017/editing-the-network-on-the-fly.md

Jan 24, 2018 - it is a variant of Jul 13, 2017 experiment which, in addition, allows to
imitate a mouse click by typing something like `(mp 120 50)` in the graphics window;
so this does not edit the network yet, but just exercises the interaction pipeline.

Jan 25, 2018 - this version can actually do network updates on the fly, demonstrating
this capability. Tested lightly, by checking that typing
`(nu [v-dmm-cons :my-list :self] [v-dmm-cons :my-list :self] -0.1)` actually does what's
expected. Other that this, this implementation introduces `v-path` macro (to be moved
to `core` eventually) and the backspace capability.
