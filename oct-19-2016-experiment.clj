; trying to build a very small network to reproduce the essence of
; Project Fluid Aug 27, 2016 experiment

(def init-matrix {accum {:self {:accum {accum {:self {:single 1}}}}}})


(def update-1-matrix-hook {identity {:update-1 {:single {identity {:update-1 {:single 1}}}}}})

(def update-2-matrix-hook {identity {:update-2 {:single {identity {:update-2 {:single 1}}}}}})

(def update-3-matrix-hook {identity {:update-3 {:single {identity {:update-3 {:single 1}}}}}})

(def start-update-of-network-matrix {accum {:self {:delta {identity {:update-1 {:single 1}}}}}})

(def start-matrix (rec-map-sum-variadic init-matrix update-1-matrix-hook update-2-matrix-hook 
                                        update-3-matrix-hook start-update-of-network-matrix))

; note that (render-smart start-matrix)
; now yields
; {"accum" {:self {:accum {"accum" {:self {:single 1}}}, :delta {"identity" {:update-1 {:single 1}}}}}, "identity" {:update-1 {:single {"identity" {:update-1 {:single 1}}}}, :update-2 {:single {"identity" {:update-2 {:single 1}}}}, :update-3 {:single {"identity" {:update-3 {:single 1}}}}}}

(def update-1-matrix 
  (rec-map-sum {accum {:self {:delta {identity {:update-1 {:single -1}}}}}}
               {accum {:self {:delta {identity {:update-2 {:single 1}}}}}}))

(def update-2-matrix 
  (rec-map-sum {accum {:self {:delta {identity {:update-2 {:single -1}}}}}}
               {accum {:self {:delta {identity {:update-3 {:single 1}}}}}}))

(def update-3-matrix 
  (rec-map-sum {accum {:self {:delta {identity {:update-3 {:single -1}}}}}}
               {accum {:self {:delta {identity {:update-1 {:single 1}}}}}}))


; (def init-output {accum {:self {:single init-matrix}}})

(def init-output
  (rec-map-sum-variadic {accum {:self {:single start-matrix}}}
                        {identity {:update-1 {:single update-1-matrix}}}
                        {identity {:update-2 {:single update-2-matrix}}}
                        {identity {:update-3 {:single update-3-matrix}}}))

(defn extract-matrix [current-output]
  (((current-output accum) :self) :single))

(defn extract-delta [current-output]
  ((((extract-matrix current-output) accum) :self) :delta))

; recording the experiment in the comments here

; user=> (load-file "dmm-snippets.clj")
; #'user/down-movement
; user=> (load-file "up-movement-pattern.clj")
; #'user/render-smart
; user=> (load-file "oct-19-2016-experiment.clj")
; #'user/extract-delta
; user=> (extract-delta init-output)
; {#<core$identity clojure.core$identity@56a75737> {:update-1 {:single 1}}}
; user=> (render-smart (extract-delta init-output))
; {"identity" {:update-1 {:single 1}}}
; user=> (def init-input (down-movement init-output))
; #'user/init-input
; user=> (def second-output (up-movement init-input))
; #'user/second-output
; user=> (render-smart (extract-delta second-output))
; {"identity" {:update-2 {:single 1}}}
; user=> (def another-output (up-movement (down-movement second-output)))
; #'user/another-output
; user=> (render-smart (extract-delta another-output))
; {"identity" {:update-3 {:single 1}}}
; user=> (def another-output (up-movement (down-movement another-output)))
; #'user/another-output
; user=> (render-smart (extract-delta another-output))
; {"identity" {:update-1 {:single 1}}}
; user=> (def another-output (up-movement (down-movement another-output)))
; #'user/another-output
; user=> (render-smart (extract-delta another-output))
; {"identity" {:update-2 {:single 1}}}
; user=> (def another-output (up-movement (down-movement another-output)))
; #'user/another-output
; user=> (render-smart (extract-delta another-output))
; {"identity" {:update-3 {:single 1}}}
; user=> (def another-output (up-movement (down-movement another-output)))
; #'user/another-output
; user=> (render-smart (extract-delta another-output))
; {"identity" {:update-1 {:single 1}}}

