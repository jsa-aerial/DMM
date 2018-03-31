Notes for mar_14_2018 experiment
================================

We started with jan_25_2018_experiment, and proceeded to modify this
program towards making possible practical livecoding experiments with DMMs.

One can use a microeditor inside Quil window and a Seesaw-based editor.

Control rendering:

`(set-stroke-weight! 5)`   
`(set-stroke-color! 0 0 200)`  
`(set-fading! 5)`  

Adjust (or create) a weight in a running network:

`(nu [v-mouse-coords :mouse-position :previous] [v-mouse-coords :mouse-position :current] -0.1)`  

Store matrix fragments in a structure:

`(sf :a [v-mouse-coords :mouse-position :previous] [v-mouse-coords :mouse-position :current] -0.1)`  
`(sf :b [v-mouse-coords :mouse-position :previous] [v-mouse-coords :mouse-position :current] -0.1)`  
`(smul :b 2)`  
`(smul :a 3)`  
`(ssum :c :a :b)`  

And add an accumulated matrix fragment to the network matrix of a running network:

`(nuff :c)`  
