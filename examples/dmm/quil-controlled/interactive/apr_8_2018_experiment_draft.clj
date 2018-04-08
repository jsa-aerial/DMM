(ns dmm.examples.quil-controlled.interactive.apr-8-2018-experiment
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [seesaw.core :as seesaw]
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

;;;;;;

(defn string-tail [s n]
  (subs s (max 0 (- (count s) n))))

(defn timestamp []
  (str (java.util.Date.)))

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
     :previous (get previous-state :previous {})}
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

(def dmm-setup-state (atom (init-state)))

(def edit-state (atom {}))

(def history (atom {})) ;; if we want to keep in-memory log

(def struct-of-test-image (atom {}))


(def stroke-color (atom [0]))

(defn set-stroke-color! [& c] (swap! stroke-color (fn[n] c)))

(defn use-stroke-color [] (apply q/stroke @stroke-color)) ;; the only reason we don't name it set-stroke!
                                                    ;; is that q/stroke does not have ! in its name
(def stroke-weight (atom 1))

(defn set-stroke-weight! [w] (swap! stroke-weight (fn[n] w)))

(defn use-stroke-weight [] (q/stroke-weight @stroke-weight))

(def fading (atom 0))

(defn set-fading! [f-factor] (swap! fading (fn[n] f-factor)))

(def activity-log (atom "data/activity-log.txt"))

(defn log-activity [s]
  (with-open [wrt (clojure.java.io/writer @activity-log :append true)]
    (.write wrt s)))


(def seesaw-window (atom {}))

(def unscreened-name-space *ns*) ;;;;; evil hack, because Quil event handler
                                 ;;;;; uses clojure.core for some reason
                                 ;;;;; quil-specific problem; unfortunarely
                                 ;;;;; we have to do the same
                                 ;;;;; with seesaw handler

(defn seesaw-setup []
  ;;; (seesaw/native!) ;;; that's only if you want to customize swing to your OS
  (let [dialog-window (seesaw/frame :title "Edit running DMM")
        input-area (seesaw/text :multi-line? true 
                                :text 
                                  (str "Enter commands                         "
                                       "                                       "
                                       "                                       "
                                       "       \nhere...\n\n\n\n\n\n\n\n\n\n\n")
                                :font (seesaw.font/font :name :monospaced :size 14))
        send-button (seesaw/button :text "Send")
        status-text (seesaw/text "")
        timer-text (seesaw/text "")
        ;;two-texts (seesaw/left-right-split status-text timer-text :divider-location 1/2)
        ;;split-line (seesaw/left-right-split send-button two-texts :divider-location 1/5)
        two-lefts (seesaw/left-right-split send-button status-text :divider-location 1/3)
        split-line (seesaw/left-right-split two-lefts timer-text :divider-location 3/5)
        split-vert (seesaw/top-bottom-split split-line (seesaw/scrollable input-area) :divider-location 1/8) 
       ]
    (seesaw/config! dialog-window :content split-vert)
    (-> dialog-window seesaw/pack! seesaw/show!)
    (swap! seesaw-window
           (fn[n] 
            {:dialog-window dialog-window
             :input-area input-area
             :send-button send-button
             :status-text status-text
             :timer-text timer-text}))
    (seesaw/listen (@seesaw-window :send-button) 
            :action (fn [e] 
                       (let [next-command (seesaw/text (@seesaw-window :input-area))
                             new-response (try (binding [*ns* unscreened-name-space] (eval (read-string next-command)))
                                             "ok"
                                             (catch Exception e "failed"))]
                          (seesaw/text! (@seesaw-window :status-text) new-response)
                          (log-activity (str (timestamp) "\n"
                                             "network timer: " (seesaw/text (@seesaw-window :timer-text)) "\n"
                                             "free memory:   " (.freeMemory (. Runtime getRuntime)) "\n"
                                             "total memory:  " (.totalMemory (. Runtime getRuntime)) "\n"
                                             "seesaw input:         " next-command "\n"
                                             "seesaw response:      " new-response "\n")))))
    (seesaw/text! (@seesaw-window :status-text) "STARTED")))

(defn map-of-image-points [image]
  (let [image-width (. image width)
        image-height (. image height)]
     {:width image-width
      :height image-height
      :points
        (into {} (for [y (range image-height)
                       x (range image-width)] 
                    {[x y] (q/get-pixel image x y)}))}))

(defn setup []
  (q/frame-rate 30)
  (q/background 127)
  (q/text-size 24)
  (q/fill 0)

  (q/tint 255 5)

  (seesaw-setup)
  (log-activity (str "NEW RUN: " (timestamp) "\n"))
  ;; setup function returns initial state. It contains
  ;; the initial output layer of the generalized neural network.
  (let [test-image (q/load-image "data/IMG_8924.JPG")]  ;;; was GenerativeBrush.PNG
    (q/resize test-image 0 400)
    (swap! struct-of-test-image (fn[n] (map-of-image-points test-image)))
    (log-activity (str "got image points " (count (:points @struct-of-test-image))  "\n")))
  {:output-layer (@dmm-setup-state :init-output)
   :timer 0
   :last-response "none"
   :current-text-input ""})

(defn update-state [quil-state]
  ;; Update sketch state by performing one cycle of the "two-stroke engine"
  ;; of the generalized neural network.
  (let [current-input (down-movement (:output-layer quil-state))
        current-output (up-movement current-input)
        timer (inc (:timer quil-state))]
    (swap! history (fn [h] (assoc h timer {:input current-input, :output current-output})))
    {:input-layer current-input
     :output-layer current-output
     :timer timer
     :last-response (:last-response quil-state)
     :current-text-input (:current-text-input quil-state)
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
  (get current-output v-mouse-pressed-monitor {}))

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

(defn draw-state [quil-state]

  (seesaw/text! (@seesaw-window :timer-text) (str (quil-state :timer)))

  (q/with-fill [127 @fading]
    (q/rect 0 0 (q/width) (q/height)))

  (let [test-image-struct @struct-of-test-image
        new-image  (q/create-image 
                     (:width test-image-struct)
                     (:height test-image-struct)
                     :argb)] ;;; Possible formats: :rgb, :argb, :alpha
    (doseq [[[x y] color] (:points test-image-struct)]
       (q/set-pixel new-image x y color))
    (seesaw/text! (@seesaw-window :status-text) 
                  (format "%X" (q/get-pixel new-image 0 0)))
    (q/image new-image 200 100))

  (use-stroke-color)
  (use-stroke-weight)
  (let [combined-mouse (->> quil-state :output-layer extract-mouse-position)
        current-mouse (get combined-mouse :current {})
        previous-mouse (get combined-mouse :previous {})]
    (q/line (get current-mouse :mouse-x 0) (get current-mouse :mouse-y 0)
            (get previous-mouse :mouse-x 0) (get previous-mouse :mouse-y 0)))

  (q/no-stroke)
  (q/with-fill [127]
    (q/rect 10 10 1000 25)
    (q/rect 10 40 1000 25))

  (q/text (str "input: " (string-tail (:current-text-input quil-state) 60)) 10 30)
  (q/text (str "status: " (:last-response quil-state)) 10 60))
          
(defn mouse-pressed [quil-state event]
  (async/go (async/>! mouse-pressed-channel event))
  quil-state)

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
  (async/go (async/>! network-update-channel (@edit-state field))))

;; set field to path
(defn sf [field x y value]
  (swap! edit-state (fn [s] (assoc s field (v-path-fn x (v-path-fn y value)))))
  nil)

;; set field to zero
(defn sfzero [field]
  (swap! edit-state (fn [s] (assoc s field {})))
  nil)

;; set field to sum of fields
(defn ssum [field field-1 field-2]
  (swap! edit-state (fn [s] (assoc s field (rec-map-sum (s field-1) (s field-2)))))
  nil)

;; multiply field by value
(defn smul [field value]
  (swap! edit-state (fn [s] (assoc s field (rec-map-mult value (s field)))))
  nil)

(defn key-typed [quil-state event]
  (let [next-key (:raw-key event)
        next-text-input (:current-text-input quil-state)]
    (if (= next-key \newline)
          (let [new-response
                   (try (binding [*ns* unscreened-name-space] ;;;;; using that evil hack
                                 (eval (read-string next-text-input)))
                        "ok"
                     (catch Exception e "failed"))]
            (log-activity (str (timestamp) "\n"
                               "network timer: " (:timer quil-state) "\n"
                               "free memory:   " (.freeMemory (. Runtime getRuntime)) "\n"
                               "total memory:  " (.totalMemory (. Runtime getRuntime)) "\n"
                               "input:         " next-text-input "\n"
                               "response:      " new-response "\n"))
            (assoc quil-state :last-response new-response :current-text-input ""))
          (let [plus-minus-one-char
                (if (= next-key \backspace)
                  (join "" (drop-last next-text-input))
                  (str next-text-input next-key))]
            (assoc quil-state :last-response "entering" :current-text-input plus-minus-one-char)))))
                       

(q/defsketch quil-try
  :title "A Quil-controlled DMM"
  :size [1100 700]
  ; setup function called only once, during sketch initialization.
  :setup setup
  ; update-state is called on each iteration before draw-state.
  :update update-state
  :draw draw-state
  :mouse-pressed mouse-pressed
  :key-typed key-typed
  ;; :features [:keep-on-top]
  ; This sketch uses functional-mode middleware.
  ; Check quil wiki for more info about middlewares and particularly
  ; fun-mode.
  :middleware [m/fun-mode])
