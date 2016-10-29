(ns dmm.examples.oct-28-2016-experiment
  (:require [dmm.core :as dc
             :refer [v-accum v-identity
                     down-movement up-movement
                     rec-map-sum]]
            [clojure.core.async :as async]))


;;; trying to build a very small network to explore asyncronous feeding of
;;; updates into a network

;;; we would like to have a neuron whose associated function has a built-in
;;; channel, and this channel very occasionally get a recurrent map.

;;; when that happens the recurrent map in question get passed to the
;;; :signal output of that neuron, otherwise {} get passes to the
;;; :signal output of that neuron.

;;; other than that we might make sure that this neuron has a :dummy
;;; non-trivial input to make sure it is activate

;;; we might do various things with the :signal output, e.g. pass it
;;; an accumulator, which would accumulate the sum of the signals.

;;; this mechanism can be used to update the network matrix itself as well

;;; until now we were running a few iterations of our networks;
;;; with this new mechanism we want to run the network continuously
;;; for some time, and it is probably too fast for our purposes,
;;; so we might want to insert a small pause into each network cycle
;;; for the time being.

;;; other things I'd like to consider:
;;;   -- it would be nice if the network can take a function definition
;;;   via a channel, and to add a corresponding var to be used
;;;   as keys in the neural net in question (basically, I am talking
;;;   about dynamic expansion of the selection of available neural types);
;;;   -- consider a mechanism to do other control actions, perhaps
;;;   via a different channel: halt the network, change the delay
;;;   (the engine speed), etc.

(def interface-channel (async/chan))

(async/alts!! [interface-channel] :default {})

;;; reader is a neuron type, but we are only going to have one neuron
;;; of this type, because it uses this particular channel

(defn reader [dummy]
  (let [reader-output {:signal ((async/alts!! [interface-channel] :default {:step 1}) 0)}]
   (clojure.pprint/pprint reader-output)
   reader-output))

(def v-reader (var reader))

;;; writer is a gadget to send smth to that neuron

(defn writer [input-map]
  (async/>!! interface-channel input-map))

;;; printer is a neuron type, an active neuron of this type
;;; prints its input om each iteration; otehrwise it is an identity neuron

(defn printer [input]
  (clojure.pprint/pprint input)
  input)

(def v-printer (var printer))

;;; the network 4 neurons

(def the-network-matrix-hook
  {v-accum {:self {:accum {v-accum {:self {:single 1}}}}}})

(def the-reader-hook ; let's hook it from the network matrix itself
  {v-reader {:the-reader {:dummy {v-accum {:self {:single 1}}}}}})

(def the-reader-accum
  {v-accum {:the-reader-accum {:accum {v-accum {:the-reader-accum {:single 1}}}}}})

(def the-printer ; let's hook it from the reader accum (it will be silent
                 ; until it becomes non-zero, which is OK
  {v-printer {:the-printer {:to-print {v-accum {:the-reader-accum {:single 1}}}}}})

;;; connect the-reader to an input of the-reader-accum

(def input-to-accum-link
  {v-accum {:the-reader-accum {:delta {v-reader {:the-reader {:signal 1}}}}}})

;;; initial network

(def start-matrix
  (rec-map-sum the-network-matrix-hook the-reader-hook the-reader-accum
                        the-printer input-to-accum-link))

; (def init-output {v-accum {:self {:single init-matrix}}})

(def init-output
  {v-accum {:self {:single start-matrix}}})

;;; a network cycle with delay

(defn network-cycle [initial-output delay-ms]
   (Thread/sleep delay-ms)
   (up-movement (down-movement initial-output)))

(defn network-run [initial-output delay-ms n-iter]
  (loop [n 1
         current-output initial-output]
    (if (< n n-iter)
      (recur (inc n) (network-cycle current-output delay-ms)))))

(network-run init-output 1000 10)

;;; to run an experiment

;;; (future (network-run init-output 1000 300))  ; run for 5 map-indexed

;;; (writer {:a 1}) ; only do this when a network or other consumer
                    ; for this channel is running to avoid blocking



;;; obsolete code ****************

(defn extract-matrix [current-output]
  (((current-output v-accum) :self) :single))

(defn extract-delta [current-output]
  ((((extract-matrix current-output) v-accum) :self) :delta))


;;; recording the experiment here on Oct 20 after the switch from
;;; accum to (var accum), and from identity to (var identity)
;;;
;;; also the difference here is that we are using iter-apply-fns to
;;; run network steps and that we actually re-run the network from the
;;; start each time (just because it's less typing and because we can)

(comment
  (->> (dc/iter-apply-fns init-output down-movement up-movement)
       rest (dc/map-every-other extract-delta)
       (take 20)
       (filter v-identity)
       clojure.pprint/pprint)
  user>
  ({#'clojure.core/identity {:update-2 {:single 1}}}
   {#'clojure.core/identity {:update-3 {:single 1}}}
   {#'clojure.core/identity {:update-1 {:single 1}}}
   {#'clojure.core/identity {:update-2 {:single 1}}}
   {#'clojure.core/identity {:update-3 {:single 1}}}
   {#'clojure.core/identity {:update-1 {:single 1}}}
   {#'clojure.core/identity {:update-2 {:single 1}}}
   {#'clojure.core/identity {:update-3 {:single 1}}}
   {#'clojure.core/identity {:update-1 {:single 1}}}
   {#'clojure.core/identity {:update-2 {:single 1}}}))




; user=> (extract-delta init-output)
; {#'clojure.core/identity {:update-1 {:single 1}}}
; user=> (extract-delta (first (drop 2 (iter-apply-fns init-output down-movement up-movement))))
; {#'clojure.core/identity {:update-2 {:single 1}}}
; user=> (extract-delta (first (drop 4 (iter-apply-fns init-output down-movement up-movement))))
; {#'clojure.core/identity {:update-3 {:single 1}}}
; user=> (extract-delta (first (drop 6 (iter-apply-fns init-output down-movement up-movement))))
; {#'clojure.core/identity {:update-1 {:single 1}}}
; user=> (extract-delta (first (drop 8 (iter-apply-fns init-output down-movement up-movement))))
; {#'clojure.core/identity {:update-2 {:single 1}}}
; user=> (extract-delta (first (drop 10 (iter-apply-fns init-output down-movement up-movement))))
; {#'clojure.core/identity {:update-3 {:single 1}}}
; user=> (extract-delta (first (drop 12 (iter-apply-fns init-output down-movement up-movement))))
; {#'clojure.core/identity {:update-1 {:single 1}}}
