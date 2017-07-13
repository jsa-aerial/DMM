(ns dmm.examples.quil-controlled.jul-6-2017-experiment
  (:require [quil.core :as q]
            [quil.middleware :as m] 
            [dmm.core :as dc
                      :refer [v-accum v-identity
                              down-movement up-movement
                              rec-map-sum]]))

;;; the neurons of this type track mouse position
;;; we treat X and Y mouse coordinates as separate outputs
(defn mouse-coords [dummy]
    {:mouse-x (q/mouse-x) :mouse-y (q/mouse-y)}
  )

(def v-mouse-coords (var mouse-coords))

(defn init-state []
  (->
   {}

   ((fn[m]
      (assoc m 

             :init-matrix
             {v-accum {:self {:accum {v-accum {:self {:single 1}}}}}}

             :mouse-tracking-neuron-hook
             {v-mouse-coords
              {:mouse-position {:single {v-mouse-coords {:mouse-y 1}}}}})))

   ((fn[m]
      (assoc m :start-matrix
             (rec-map-sum
              (m :init-matrix)
              (m :mouse-tracking-neuron-hook)))))
   
   ((fn[m]
      (assoc m :init-output
             {v-accum {:self {:single (m :start-matrix)}}})))))

(def state (atom (init-state)))



(defn setup []
  ; Set frame rate to 4 frames per second.
  (q/frame-rate 4)
  ; setup function returns initial state. It contains
  ; the initial output layer of the generalized neural network.
  {:output-layer (@state :init-output)
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

(defn extract-mouse-position [current-output]
  (get (current-output v-mouse-coords) :mouse-position "quil quirk"))
  ; "quil quirk" - the first iteration "draw-state" happens
  ;                without "update-state" 


(defn draw-state [state]
  ; Clear the sketch by filling it with grey color.
  ;(q/background 127)

  (q/fill 127 50)
  (q/rect 0 0 (q/width) (q/height))
  (q/fill 0)

  (q/text-size 24)

  (let [sub-delta-row (->> state :output-layer extract-mouse-position)]
    (q/text (str (q/frame-count) " " sub-delta-row)  50  (mod (+ 50 (* 25 (q/frame-count))) (q/height)))       
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
