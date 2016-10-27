(ns dmm.core
  "The core part of Dataflow Matrix Machine engine")

;;; auxiliary primitives

(defn mORn? [x] (or (map? x) (number? x)))
(defn mANDn? [x y] (and (map? x) (number? y)))
(defn =num0? [x] (and (number? x) (zero? x)))
;;(defn one? [x] (or (= x 1) (= x 1.0)))
(defn one? [x] (and (number? x) (== x 1)))
(defn num-nonzero? [x] (and (number? x) (not (zero? x))))
(defn nullelt? [x] (or (= x {}) (=num0? x)))
(defn numelt [x] {:number x})

(defn maps? [x & xs]
  (reduce (fn[tv b] (and tv b)) (map map? (conj xs x))))

;;; control functions (to organize composition of half-steps
;;; in the two-stroke engine, to apply functions only to
;;; odd or even members of the resulting sequences, and similar)

(defn rotate
  ([coll]
   (rotate 1 coll))
  ([n coll]
   (concat (drop n coll) (take n coll))))

(defn iter-apply-fns
  ([m f]
   (iterate f m))
  ([m f1 f2]
   (map first
        (iterate (fn[[m [f1 f2]]]
                   [(f1 m) [f2 f1]])
                 [m [f1 f2]])))
  ([m f1 f2 & fs]
   (map first
        (iterate (fn[[m fs]]
                   [((first fs) m) (rotate fs)])
                 [m (cons f1 (cons f2 fs))]))))

(defn map-every-nth [n f coll]
  (map-indexed #(if (zero? (mod (inc %1) n)) (f %2) nil) coll))

(defn map-every-other [f coll]
  ((partial map-every-nth 2) f coll))

;;; ***********************************************************************
;;; recurrent maps section - see design notes

;;; rec-map-mult and its relatives (an operation
;;; involving a scalar and a recurrent map;
;;; the most important case is multiplication
;;; of the recurrent map by the scalar

(defn rec-map-op
  ([op unit? n M]
   (if (unit? n)
     M
     (rec-map-op op n M)))
  ([op n M]
   (reduce (fn [M [k v]]
             (let [new-v
                   (cond
                     (map? v) (rec-map-op op n v)
                     (number? v) (op v n)
                     :else 0)]
               (if (nullelt? new-v) M (assoc M k new-v))))
           {} M)))


(defn rec-map-mult [n M]
  (if (zero? n)
    {}
    (rec-map-op * one? n M)))

(defn rec-map-div [n M]
  (rec-map-op / one? n M))

(defn rec-map-add [n M]
  (rec-map-op + zero? n M))

(defn rec-map-sub [n M]
  (rec-map-op - zero? n M))

;;; add recurrent maps together

(defn rec-map-sum [large-M small-M] ; "large" and "small" express intent
  (reduce (fn [M [k small-v]]
            (let [large-v (get large-M k)
                  l-v (if (not (mORn? large-v)) 0 large-v)
                  s-v (if (not (mORn? small-v)) 0 small-v)
                  new-v
                  (cond
                    (maps? l-v s-v)  (rec-map-sum l-v s-v)
                    (mANDn? l-v s-v) (rec-map-sum l-v (numelt s-v))
                    (mANDn? s-v l-v) (rec-map-sum s-v (numelt l-v))
                    :else (+ l-v s-v))]
              (if (nullelt? new-v) (dissoc M k) (assoc M k new-v))))
          large-M small-M))

(defn rec-map-sum-variadic [x & xs]
  (reduce (fn [new-sum y]
            (rec-map-sum new-sum y))
          x xs))

;;; generalized multiplicative masks and linear combinations

;;; a recurrent map Mask (mult-mask) and a recurrent map Structured
;;; Vector (M) traverse the Mask; for each numerical leaf in the Mask,
;;; if the path corresponding to the leaf exists in the Structured Vector,
;;; take the result of multiplication of that leaf by the rec-map or
;;; number corresponding to that path in the Structured Vector.
;;; Otherwise just drop the path from the result.

;;; note that in the current version if (:number x) is present
;;; in the mult-mask instead of x, it would not work correctly.

;;; further note: meditate whether equality of a multiplier to 1
;;; requires a special consideration

(defn rec-map-mult-mask [mult-mask M]
  (reduce (fn [new-M [k mask]]
            (let [m (get M k)
                  actual-M (if (not (mORn? m)) 0 m)
                  actual-mask (if (not (mORn? mask)) 0 mask)
                  new-v
                    (cond
                      (maps? actual-mask actual-M)
                      (rec-map-mult-mask actual-mask actual-M)

                      (mANDn? actual-M actual-mask)
                      (rec-map-mult actual-mask actual-M) ; leaf works!

                      (mANDn? actual-mask actual-M) 0
                      :else (* actual-M actual-mask))]

              (if (nullelt? new-v) new-M (assoc new-M k new-v))))
          {} mult-mask))

;;; generalized linear combination - same as above, but compute the
;;; sum of the resulting vectors corresponding to the leaves in the
;;; Mask.

;;; right now the way it uses rec-map-sum somewhat ignores large-M vs
;;; small-M distinction it should not matter correctness-wise, but
;;; should eventually prompt a meditation efficiency-wise; we did just
;;; ended the practice of adding a non-trivial map to {} interpreted
;;; as a large map, so some progress here.

;;; this function is very similar to rec-map-mult-mask, the only
;;; difference except for its name (and hence recursive call) should
;;; have been that rec-map-sum is used instead of assoc

;;; but rec-map-sum is current only works for maps, which is why part
;;; of its functionality is duplicated here - something to meditate
;;; upon during a code review

(defn rec-map-lin-comb [mult-mask M]
  (reduce (fn [new-M [k mask]]
            (let [m (get M k)
                  actual-M (if (not (mORn? m)) 0 m)
                  actual-mask (if (not (mORn? mask)) 0 mask)
                  v-to-add (cond
                             (maps? actual-mask actual-M)
                             (rec-map-lin-comb actual-mask actual-M)

                             (mANDn? actual-M actual-mask)
                             (rec-map-mult actual-mask actual-M) ; leaf works!

                             (mANDn? actual-mask actual-M) 0
                             :else (* actual-M actual-mask))
                  new-sum (cond
                            (map? v-to-add)
                            (if (= new-M {})
                              v-to-add
                              (rec-map-sum new-M v-to-add))

                            (num-nonzero? v-to-add)
                            (rec-map-sum new-M (numelt v-to-add))

                            :else new-M)]
              new-sum))
          {} mult-mask))


;;; apply matrix to vector (mutrix multiplication)

;;; we expect that at level 0, the structures of a matrix row
;;; and argument vector are such that the use of
;;; rec-map-lin-comb makes sense, and that the indexes
;;; of rows have plain mutlilevel structure.

(defn apply-matrix [arg-matrix arg-vector level]
  (if (= level 0)
    (rec-map-lin-comb arg-matrix arg-vector)
    (reduce (fn [new-map [k v]]
              (assoc new-map k (apply-matrix v arg-vector (- level 1))))
      {} arg-matrix)))

;;; end of the recurrent maps section
;;; ***********************************************************************

;;; two indispensible neuron types: accum (sum) and identity

;;; accumulator (in the first version it is just the sum, the user is
;;; expected to keep the matrix row corresponding to :accum field at 1
;;; at the appropriate place and zero at all others)

(defn accum [input]
  {:single (rec-map-sum (input :accum) (input :delta))})

;;; Remark on identity: note that "identity" function (fn [x] x) is
;;; already defined and can also be used as a simple accumulator, as
;;; long as we are content with input and output being named the
;;; same (e.g. :single); otherwise one would need a definition
;;; like (defn id [x] {:output (x :input)})

(def v-accum (var accum))

(def v-identity (var identity))

;;; current version of down-movement of the two-stroke engine:
;;; computing neuron inputs as linear combinations of neuron outputs.

;;; the addendum to caveat 1 in the deisgn-notes ("caveat 1
;;; continued") means that the "down movement" is just plain matrix
;;; multiplication without any extra frills. The application of the
;;; matrix row to the vector of neuron outputs is
;;; simply "rec-map-lin-comb".

;;; let's say, applying a row to the vector of outputs is level 0, and
;;; applying a map of rows obtaining the map of answer is level 1,
;;; etc. Our current design spec says that the "down movement" is
;;; applying this funcion at level 3.


(defn down-movement [function-named-instance-map-of-outputs]
  (apply-matrix
   (((function-named-instance-map-of-outputs v-accum) :self) :single); current matrix
   function-named-instance-map-of-outputs ; arg-vector
   3))

;;; a sketch of the up-movement pattern for the two-stroke engine.
;;;
;;; (normally a neuron takes a map and produces a map according to
;;; design notes, but the up-movement pattern should work for any set
;;; of functions taking one argument (an empty hash-map is recommended
;;; if one needs to signify absense of meaningful arguments) and
;;; producing one argument)
;;;
;;; there is a map from such functions to (maps from names of their
;;; instances to the arguments the function is to be applied to)
;;;
;;; the result should be a map from such functions to (maps from names
;;; of their instances to the results obtained by the corresponding
;;; function application)


;;; auxiliary pass applying a function to a map from names of
;;; instances to arguments and producing a map from names of instances
;;; to results

(defn apply-to-map-of-named-instances [f named-instance-to-arg-map]
  (reduce (fn [new-ninstance-arg-map [name arg]]
            (assoc new-ninstance-arg-map name (f arg)))
          {} named-instance-to-arg-map))



(defn up-movement [function-named-instance-map]
  (reduce (fn [new-fnamed-imap [f names-to-args-map]]
            (assoc new-fnamed-imap f
                   (apply-to-map-of-named-instances f names-to-args-map)))
          {} function-named-instance-map))
