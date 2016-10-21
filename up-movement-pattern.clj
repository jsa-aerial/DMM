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


