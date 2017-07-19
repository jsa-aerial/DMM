(ns dmm.examples.quil-controlled.jul-13-2017-experiment
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [dmm.core :as dc
                      :refer [v-accum v-identity
                              down-movement up-movement
                              rec-map-sum]]
            [clojure.core.async :as async]))

(def mouse-pressed-channel (async/chan))

(defn mouse-pressed-monitor [dummy]
  (let [signal ((async/alts!! [mouse-pressed-channel] :default {}) 0)]
    {:signal
     (if (= signal {})
       {}
       {:x (signal :x) :y (signal :y) :flag 1})}))
  ; might want to add a transformation of :button field too

(def v-mouse-pressed-monitor (var mouse-pressed-monitor))

(defn dmm-cons [accum-style-input]
  (let [old-self (accum-style-input :self)]
    (if (get-in accum-style-input [:signal :flag])
      {:self {:this (accum-style-input :signal) :rest old-self}}
      {:self old-self})))

(def v-dmm-cons (var dmm-cons))

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
              {:mouse-position {:single {v-mouse-coords {:mouse-y 1}}}}}

             :mouse-pressed-monitor-hook
             {v-mouse-pressed-monitor {:mouse-pressed-monitor {:single
              {v-mouse-pressed-monitor {:mouse-pressed-monitor {:single 1}}}}}}

             :dmm-cons-accum-connection
             {v-dmm-cons {:my-list {:self {v-dmm-cons {:my-list {:self 0.9}}}}}}

             :dmm-cons-signal-connection
             {v-dmm-cons {:my-list {:signal
              {v-mouse-pressed-monitor {:mouse-pressed-monitor {:signal 1}}}}}})))

   ((fn[m]
      (assoc m :start-matrix
             (rec-map-sum
              (m :init-matrix)
              (m :mouse-tracking-neuron-hook)
              (m :mouse-pressed-monitor-hook)
              (m :dmm-cons-accum-connection)
              (m :dmm-cons-signal-connection)))))

   ((fn[m]
      (assoc m :init-output
             (rec-map-sum
              {v-accum {:self {:single (m :start-matrix)}}}
              {v-dmm-cons {:my-list {:self {:end-list 1}}}}))))))

(def state (atom (init-state)))



(defn setup []
  ;; Set frame rate to 1 frame per second, so that one has time to
  ;; ponder things.
  (q/frame-rate 1)
  ;; setup function returns initial state. It contains
  ;; the initial output layer of the generalized neural network.
  {:output-layer (@state :init-output)})

(defn update-state [state]
  ;; Update sketch state by performing one cycle of the "two-stroke engine"
  ;; of the generalized neural network.
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
  ;; "quil quirk" - the first iteration "draw-state" happens
  ;;                without "update-state"

(defn extract-list [current-output]
   (((current-output v-dmm-cons) :my-list) :self))

(defn extract-mouse-pressed-monitor [current-output]
  (current-output v-mouse-pressed-monitor))

(defn render-list [recorded-list horizontal-shift]
  ;;(q/text (str recorded-list) 25 25)
  (when (not (recorded-list :end-list))
    (q/no-fill)
    (q/rect horizontal-shift (mod (+ 30 (* 50 (q/frame-count))) (q/height)) 20 20)
    (q/fill 0)
    (q/ellipse
     (+ (int (/ (* 20 ((recorded-list :this) :x)) (q/width))) horizontal-shift)
     (+ (int (/ (* 20 ((recorded-list :this) :y)) (q/height))) (mod (+ 30 (* 50 (q/frame-count))) (q/height)))
     3 3)
    (render-list (recorded-list :rest) (+ horizontal-shift 30))
    ))


(defn draw-state [state]
  ;; Clear the sketch by filling it with grey color.
  ;;(q/background 127)

  (q/fill 127 50)
  (q/rect 0 0 (q/width) (q/height))

  (q/fill 0)

  (q/text-size 24)

  (render-list (extract-list (state :output-layer)) 50)

  (let [sub-delta-row (->> state :output-layer extract-mouse-pressed-monitor)]
    (q/text (str (q/frame-count) " " sub-delta-row)
            50 (mod (+ 25 (* 50 (q/frame-count))) (q/height)))))

(defn mouse-pressed [state event]
  #_(async/>!! mouse-pressed-channel event)
  (async/go (async/>! mouse-pressed-channel event))
  state)

(q/defsketch quil-try
  :title "A Quil-controlled DMM"
  :size [800 600]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :mouse-pressed mouse-pressed
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
