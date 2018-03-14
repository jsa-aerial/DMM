(ns dmm.examples.quil-controlled.interactive.jan-25-2018-experiment
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [dmm.core :as dc
                      :refer [v-accum v-identity
                              down-movement up-movement
                              rec-map-sum rec-map-mult]]
            [clojure.core.async :as async]
            [clojure.string :as string :refer [join]]))

;;;;;;;;;;  the v-path macro might need to move to the core

(defn v-path-fn
  ([x] (v-path-fn x 1))
  ([x y]
   (if (empty? x)
     y
     {(first x) (v-path-fn (rest x) y)})))

(defmacro v-path
  ([x] (v-path-fn x 1))
  ([x y] (v-path-fn x y)))

(v-path [:a :b :c] (v-path [:d :e]))
;; {:a {:b {:c {:d {:e 1}}}}}

(v-path [:a :b :c] (v-path [:d :e] 0.9))
;; {:a {:b {:c {:d {:e 0.9}}}}}

(v-path-fn [:a :b :c] (v-path-fn [:d :e] 0.9))
;; {:a {:b {:c {:d {:e 0.9}}}}}

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def mouse-pressed-channel (async/chan))

(def network-update-channel (async/chan))

(defn mouse-pressed-monitor [dummy]
  (let [signal ((async/alts!! [mouse-pressed-channel] :default {}) 0)]
    {:signal
     (if (= signal {})
       {}
       {:x (signal :x) :y (signal :y) :flag 1})}))
  ; might want to add a transformation of :button field too

(defn network-update-monitor [dummy]
  (let [signal ((async/alts!! [network-update-channel] :default {}) 0)]
    {:signal signal}))

(def v-mouse-pressed-monitor (var mouse-pressed-monitor))

(def v-network-update-monitor (var network-update-monitor))

(defn dmm-cons [accum-style-input]
  (let [old-self (accum-style-input :self)]
    (if (get-in accum-style-input [:signal :flag])
      {:self {:this (accum-style-input :signal) :rest old-self}}
      {:self old-self})))

(def v-dmm-cons (var dmm-cons))

;;; the neurons of this type track mouse position
;;; we treat X and Y mouse coordinates as separate outputs
(defn mouse-coords [previous-state]
    {:current {:mouse-x (q/mouse-x) :mouse-y (q/mouse-y)}
     :previous (previous-state :previous)}
  )

(def v-mouse-coords (var mouse-coords))

(defn init-state []
  (->
   {}

   ((fn[m]
      (assoc m

             :init-matrix
             (v-path [v-accum :self :accum]
                     (v-path [v-accum :self :single]))

             :mouse-tracking-neuron-hook
             (v-path [v-mouse-coords :mouse-position :previous]
                     (v-path [v-mouse-coords :mouse-position :current]))

             :mouse-pressed-monitor-hook
             (v-path [v-mouse-pressed-monitor :mouse-pressed-monitor :single]
                     (v-path [v-mouse-pressed-monitor :mouse-pressed-monitor :single]))

             :network-interactive-updater-hook
             (v-path [v-network-update-monitor :network-interactive-updater :signal]
                     (v-path [v-network-update-monitor :network-interactive-updater :signal]))

             :network-update-connection
             (v-path [v-accum :self :delta]
                     (v-path [v-network-update-monitor :network-interactive-updater :signal]))
             
             :dmm-cons-accum-connection
             (v-path [v-dmm-cons :my-list :self]
                     (v-path [v-dmm-cons :my-list :self]))

             :dmm-cons-signal-connection
             (v-path [v-dmm-cons :my-list :signal]
                     (v-path [v-mouse-pressed-monitor :mouse-pressed-monitor :signal])))))

   ((fn[m]
      (assoc m :start-matrix
             (rec-map-sum
              (m :init-matrix)
              (m :mouse-tracking-neuron-hook)
              (m :mouse-pressed-monitor-hook)
              (m :network-interactive-updater-hook)
              (m :network-update-connection)
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
  {:output-layer (@state :init-output)
   :last-response "none"
   :current-text-input ""})

(defn update-state [state]
  ;; Update sketch state by performing one cycle of the "two-stroke engine"
  ;; of the generalized neural network.
  (let [current-input (down-movement (:output-layer state))]
    {:input-layer current-input
     :output-layer (up-movement current-input)
     :last-response (:last-response state)
     :current-text-input (:current-text-input state)
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
    (q/rect horizontal-shift (mod (+ 30 (* 75 (q/frame-count))) (q/height)) 20 20)
    (q/fill 0)
    (q/ellipse
     (+ (int (/ (* 20 (get-in recorded-list [:this :x] 0)) (q/width))) horizontal-shift)
     (+ (int (/ (* 20 (get-in recorded-list [:this :y] 0)) (q/height))) (mod (+ 30 (* 75 (q/frame-count))) (q/height)))
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

  (let [sub-delta-row-1 (->> state :output-layer extract-mouse-pressed-monitor)
        sub-delta-row-2 (->> state :output-layer extract-mouse-position)]
    (q/text (str (q/frame-count) " " sub-delta-row-1 " " sub-delta-row-2)
            50 (mod (+ 25 (* 75 (q/frame-count))) (q/height))))

  (q/text (str "last response: " (:last-response state))
          50 (mod (+ 75 (* 75 (q/frame-count))) (q/height)))

  (q/fill 0 0 127)
  
  (q/text (str "current text input: " (:current-text-input state))
          400 (mod (+ 75 (* 75 (q/frame-count))) (q/height))))
          
(defn mouse-pressed [state event]
  (async/go (async/>! mouse-pressed-channel event))
  state)

;; imitation of mouse pressed event for (eval (read-string current-text-input))
;; type something like (mp 120 50) in the graphics window to imitate a click
;; note that you are allowed to go beyond your window size and even to go
;; negative here, for example (mp -100 -100), in which case the dot will be
;; shown outside the "mini-window" representing the mouse click in question
(defn mp [x y]
  (async/go (async/>! mouse-pressed-channel {:x x :y y})))

;; send "nu [input-path] [output-path] weight" from the quil window
;; to update the network
(defn nu [x y value]
  (async/go (async/>! network-update-channel (v-path-fn x (v-path-fn y value)))))

;; send field to update the network (network update from field)
(defn nuff [field]
  (async/go (async/>! network-update-channel (@state field))))

;; set field to path
(defn sf [field x y value]
  (swap! state (fn [s] (assoc s field (v-path-fn x (v-path-fn y value)))))
  nil)

;; set field to zero
(defn sfzero [field]
  (swap! state (fn [s] (assoc s field {})))
  nil)

;; set field to sum of fields
(defn ssum [field field-1 field-2]
  (swap! state (fn [s] (assoc s field (rec-map-sum (s field-1) (s field-2)))))
  nil)

;; multiply field by value
(defn smul [field value]
  (swap! state (fn [s] (assoc s field (rec-map-mult value (s field)))))
  nil)




(def unscreened-name-space *ns*) ;;;;; evil hack, because Quil event handler
                                 ;;;;; uses clojure.core for some reason
                                 ;;;;; quil-specific problem

(defn key-typed [state event]
  (let [next-key (:raw-key event)
        next-text-input (:current-text-input state)]
    (if (= next-key \newline)
          (let [new-response
                   (try (binding [*ns* unscreened-name-space] ;;;;; using that evil hack
                                 (eval (read-string next-text-input)))
                        "ok"
                     (catch Exception e "failed"))]
            (assoc state :last-response new-response :current-text-input ""))
          (let [plus-minus-one-char
                (if (= next-key \backspace)
                  (join "" (drop-last next-text-input))
                  (str next-text-input next-key))]
            (assoc state :last-response "entering" :current-text-input plus-minus-one-char)))))
                       

(q/defsketch quil-try
  :title "A Quil-controlled DMM"
  :size [2000 800]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :mouse-pressed mouse-pressed
  :key-typed key-typed
  :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
