; a sketch of the up-movement pattern per design notes
;
; (normally a neuron takes a map and produces a map according to design notes,
; but the up-movement pattern should work for any set of functions
; taking one argument (an empty hash-map is recommended if one needs
; to signify absense of meaningful arguments) and producing one argument)
;
; there is a map from such functions to (maps from names of their instances
; to the arguments the function is to be applied to)
;
; the result should be a map from such functions to
; (maps from names of their instances to the results obtained
; by the corresponding function application)


; auxiliary pass applying a function to a map from names of instances
; to arguments and producing a map from names of instances to results

(defn apply-to-map-of-named-instances [f named-instance-to-arg-map]
  (reduce (fn [new-ninstance-arg-map [name arg]]
            (assoc new-ninstance-arg-map name (f arg)))
          {} named-instance-to-arg-map))



(defn up-movement [function-named-instance-map]
  (reduce (fn [new-fnamed-imap [f names-to-args-map]]
            (assoc new-fnamed-imap f (apply-to-map-of-named-instances f names-to-args-map)))
          {} function-named-instance-map))  


; auxiliary functions to render funcnames better

(require '[clojure.string :as str])

(require '[clojure.test :as test])


(defn funcname [f] ((str/split ((str/split (str f) #"\$" 2) 1) #"\@") 0))

(defn render-funcmap [funcmap]
  (reduce (fn [new-map [f v]]
            (assoc new-map (funcname f) v))
          {} funcmap))

; a non-string-based alternative to render-funcmap

(defn render-funcmap-2 [funcmap]
  (reduce (fn [new-map [f v]]
            (assoc new-map (type f) v))
          {} funcmap))

; let's render functions in an arbitrary map in a smart was

(defn render-smart [map-with-funcs]
  (reduce (fn [new-map [k v]]
            (let [new-k (if (test/function? k) (funcname k) k)
                  new-v (if (map? v) (render-smart v) v)]
              (assoc new-map new-k new-v)))
          {} map-with-funcs))


