# Running example DMMs

All DMMs in the "examples" directory can be run from a typical Clojure environment.

It is particularly convenient to start with any of the examples controlled by Quil, the Clojure wrapper around Processing:

https://github.com/jsa-aerial/DMM/tree/master/examples/dmm/quil-controlled

Clone this repository, load, for example, https://github.com/jsa-aerial/DMM/blob/master/examples/dmm/quil-controlled/jul_13_2017_experiment.clj
into your Clojure environment (e.g. CIDER for Emacs), start the REPL, and load the code buffer. You should have a small DMM running in a graphical
window and accumulating and rendering the list of mouse clicks within that window.
