(ns dmm.examples.quil-controlled.jul-6-2017-experiment
  (:require [quil.core :as q]
            [quil.middleware :as m] 
            [dmm.core :as dc
                      :refer [v-accum v-identity
                              down-movement up-movement
                              rec-map-sum]]))

(defn init-output []
  (def init-matrix
    {v-accum {:self {:accum {v-accum {:self {:single 1}}}}}})

  (def update-1-matrix-hook
    {v-identity {:update-1 {:single {v-identity {:update-1 {:single 1}}}}}})

  (def update-2-matrix-hook
    {v-identity {:update-2 {:single {v-identity {:update-2 {:single 1}}}}}})

  (def update-3-matrix-hook
    {v-identity {:update-3 {:single {v-identity {:update-3 {:single 1}}}}}})

  (def start-update-of-network-matrix
    {v-accum {:self {:delta {v-identity {:update-1 {:single 1}}}}}})

  (def start-matrix
    (rec-map-sum
      init-matrix update-1-matrix-hook update-2-matrix-hook
      update-3-matrix-hook start-update-of-network-matrix))


  (def update-1-matrix
    (rec-map-sum {v-accum {:self {:delta {v-identity {:update-1 {:single -1}}}}}}
                 {v-accum {:self {:delta {v-identity {:update-2 {:single 1}}}}}}))

  (def update-2-matrix
    (rec-map-sum {v-accum {:self {:delta {v-identity {:update-2 {:single -1}}}}}}
                 {v-accum {:self {:delta {v-identity {:update-3 {:single 1}}}}}}))

  (def update-3-matrix
    (rec-map-sum {v-accum {:self {:delta {v-identity {:update-3 {:single -1}}}}}}
                 {v-accum {:self {:delta {v-identity {:update-1 {:single 1}}}}}}))

  (def init-output
    (rec-map-sum {v-accum {:self {:single start-matrix}}}
                 {v-identity {:update-1 {:single update-1-matrix}}}
                 {v-identity {:update-2 {:single update-2-matrix}}}
                 {v-identity {:update-3 {:single update-3-matrix}}}))
  init-output
)

(defn setup []
  ; Set frame rate to 4 frames per second.
  (q/frame-rate 4)
  ; setup function returns initial state. It contains
  ; the initial output layer of the generalized neural network.
  {:output-layer (init-output)
   })

(defn update-state [state]
  ; Update sketch state by performing one cycle of the "two-stroke engine"
  ; of the generalized neural network.
  (let [current-input (down-movement (:output-layer state))]
    {:input-layer current-input
     :output-layer (up-movement current-input)
     }))


(defn extract-matrix [current-output]
  (((current-output v-accum) :self) :single))

(defn extract-delta [current-output]
  ((((extract-matrix current-output) v-accum) :self) :delta))


(defn draw-state [state]
  ; Clear the sketch by filling it with grey color.
  (q/background 127)

  (q/text-size 24)

  (let [sub-delta-row (->> state :output-layer extract-delta)]
    (q/text (str (q/frame-count) " " sub-delta-row)  50 (+ 50 (* 5 (q/frame-count))))       
              ))

(q/defsketch quil-try
  :title "A Quil-controlled DMM"
  :size [800 600]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
