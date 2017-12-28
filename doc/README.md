# Running example DMMs

All DMMs in the "examples" directory can be run from a typical Clojure environment.

It is particularly convenient to start with any of the examples controlled by Quil, the Clojure wrapper around Processing:

https://github.com/jsa-aerial/DMM/tree/master/examples/dmm/quil-controlled

Clone this repository, load, for example, https://github.com/jsa-aerial/DMM/blob/master/examples/dmm/quil-controlled/jul_13_2017_experiment.clj
into your Clojure environment (e.g. CIDER for Emacs), start the REPL, and load the code buffer. You should have a small DMM running in a graphical
window and accumulating and rendering the list of mouse clicks within that window.
lein

## Using Lein

* make sure you have Java 8
* install Leiningen from https://leiningen.org/ ; 
* run `lein repl` from the root of your copy of DMM repository. It should install the necessary
dependencies (which might take a while the first time) and give you a REPL prompt.

```
git clone https://github.com/jsa-aerial/DMM/
cd DMM
lein repl
(load-file "examples/dmm/quil-controlled/jul_13_2017_experiment.clj")
```
