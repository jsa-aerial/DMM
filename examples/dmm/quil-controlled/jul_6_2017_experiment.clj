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
  ; Set color mode to HSB (HSV) instead of default RGB.
  ;(q/color-mode :hsb)
  ; setup function returns initial state. It contains
  ; circle color and position.
  {:output-layer (init-output)
   ;:color 0
   ;:rect-color 0
   ;:angle 0
   })

(defn update-state [state]
  ; Update sketch state by changing circle color and position.
  (let [current-input (down-movement (:output-layer state))]
    {:input-layer current-input
     :output-layer (up-movement current-input)
     ;:color (mod (+ (:color state) 0.7) 255)
     ;:rect-color (mod (+ (:rect-color state) 0.07) 255)
     ;:angle (+ (:angle state) 0.1)
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
  :title "You spin my circle right round"
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
