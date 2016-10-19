; trying to build a very small network to reproduce the essence of
; Project Fluid Aug 27, 2016 experiment

(def init-matrix {accum {:self {:accum {accum {:self {:single 1}}}}}})

(def init-output {accum {:self {:single init-matrix}}})


