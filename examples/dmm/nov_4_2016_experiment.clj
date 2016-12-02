(ns dmm.examples.nov-4-2016-experiment
  (:require [dmm.core :as dc
             :refer [v-accum v-identity v-max-norm v-less-or-equal
                     down-movement up-movement
                     rec-map-sum]]
            [clojure.core.async :as async]))

;;; building on top of oct_28_2016_experiment experience we are now
;;; trying to implement something similar to the duplicate
;;; characters detector from the "Programming Patterns" paper

(def interface-channel (async/chan))

;;; reader is a neuron type, but we are only going to have one neuron
;;; of this type, because it uses this particular channel

(defn reader [dummy]
  (let [reader-output {:signal ((async/alts!! [interface-channel] :default {}) 0)}]
   (clojure.pprint/pprint reader-output)
   reader-output))

(def v-reader (var reader))

;;; writer is a gadget to send smth to that neuron

(defn writer [input-string delay-ms]
  (loop [input-chars (seq input-string)]
    (if (seq input-chars)
      (do
        (Thread/sleep delay-ms)
        (async/>!! interface-channel {(first input-chars) 1}) ; (clojure.pprint/pprint {(first input-chars) 1})
        (recur (rest input-chars))))))

;;; printer is a neuron type, an active neuron of this type
;;; prints its input om each iteration; otherwise it is an identity neuron

(defn printer [input]
  (clojure.pprint/pprint input)
  input)

(def v-printer (var printer))

;;; the network 5 neurons

(def the-network-matrix-hook
  {v-accum {:self {:accum {v-accum {:self {:single 1}}}}}})

(def the-reader-hook ; let's hook it from the network matrix itself
  {v-reader {:the-reader {:dummy {v-accum {:self {:single 1}}}}}})

(def the-reader-accum
  {v-accum {:the-reader-accum {:accum {v-accum {:the-reader-accum {:single 1}}}}}})

(def the-max-norm ; let's hook it from the reader accum
  {v-max-norm {:the-max-norm {:input {v-accum {:the-reader-accum {:single 1}}}}}})

(def the-printer ; let's hook it from the max norm
  {v-printer {:the-printer {:to-print {v-max-norm {:the-max-norm {:number 1}}}}}})

;;; connect the-reader to an input of the-reader-accum

(def input-to-accum-link
  {v-accum {:the-reader-accum {:delta {v-reader {:the-reader {:signal 1}}}}}})

;;; initial network

(def start-matrix
  (rec-map-sum the-network-matrix-hook the-reader-hook the-reader-accum
                        the-max-norm the-printer input-to-accum-link))

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

;;; (network-run init-output 1000 10)

;;; to run an experiment

;;; (future (network-run init-output 1000 300))  ; run for 5 minutes

;;; ;;; (writer "hey 235eee" 4321) ; only do this when a network or other consumer
                                ; for this channel is running to avoid blocking;
                                ; make sure that the delay is larger than
                                ; the network delay
