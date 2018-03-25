Notes for mar_14_2018 experiment
================================

We started with jan_25_2018_experiment, and proceeded to modify this
program towards making possible practical livecoding experiments with DMMs.

As of March 25, we have some progress towards such system, but are not
there yet. The most acute pain point is that we have a microeditor
inside Quil window, and it does not support copying and pasting
(and is not currently multiline). So the next step will be to move
the editor outside Quil window. Nevertheless, some useful functionality
to enable livecoding is supported already.

Here is some of what we tested:

`(q/stroke-weight 5)
(set-stroke-color! 0 0 200)
(set-fading! 5)
(nu [v-mouse-coords :mouse-position :previous] [v-mouse-coords :mouse-position :current] -0.1)`
