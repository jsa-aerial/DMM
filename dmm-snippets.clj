;;;(take 7 (iterate rotate [:f1 :f2 :f3 :f4]))

(defn rotate
  ([coll]
   (rotate 1 coll))
  ([n coll]
   (concat (drop n coll) (take n coll))))

(defn mORn? [x] (or (map? x) (number? x)))
(defn mANDn? [x y] (and (map? x) (number? y)))
(defn =num0? [x] (and (number? x) (zero? x)))
;(defn one? [x] (or (= x 1) (= x 1.0)))
(defn one? [x] (and (number? x) (== x 1)))
(defn num-nonzero? [x] (and (number? x) (not (zero? x))))
(defn nullelt? [x] (or (= x {}) (=num0? x)))
(defn numelt [x] {:number x})

(defn maps? [x & xs]
  (reduce (fn[tv b] (and tv b)) (map map? (conj xs x))))


(def testmap
  {:a 1 :b 2 :c {:a 1 :b 2 :c {:x 1 :y 3}}})

(defn rec-map-op [op n M]
  (reduce (fn [M [k v]]
            (let [new-v
                  (cond
                    (map? v) (rec-map-op op n v)
                    (number? v) (op v n)
                    :else 0)]
              (if (nullelt? new-v) M (assoc M k new-v))))
          {} M))

(defn rec-map-mult [n M]
  (rec-map-op * n M))

(defn rec-map-div [n M]
  (rec-map-op / n M))

(defn rec-map-add [n M]
  (rec-map-op + n M))

(defn rec-map-sub [n M]
  (rec-map-op - n M))

; the idea of rec-map-mult-share is to avoid copying;
; we might do that for other operations if necessary

(defn rec-map-mult-share [n M]
  (cond
    (one? n) M
    (zero? n) {}
    :else (rec-map-mult n M)))

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
              (if (nullelt? new-v) M (assoc M k new-v))))
          large-M small-M))


(defn g [m]
  (rec-map-mult 2 m))

(defn h [m]
  (rec-map-add 3 m))

(defn f [m]
  (rec-map-div 7 m))

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


(take 3 (iter-apply-fns testmap g h))
(first (drop 9 (iter-apply-fns testmap g h)))
(first (drop 9 (iter-apply-fns testmap g h f)))

; generalized multiplicative masks and linear combinations

; a recurrent map Mask (mult-mask) and a recurrent map Structured Vector (M)
; traverse the Mask; for each numerical leaf in the Mask, 
; if the path corresponding to the leaf exists in the Structured Vector,
; take the result of multiplication of that leaf by the
; rec-map or number corresponding to that path in the Structured Vector.
; Otherwise just drop the path from the result.

; note that in the current version if (:number x) is present
; in the mult-mask instead of x, it would not work correctly.

; further note: meditate whether equality of a multiplier to 1 
; requires a special consideration

(defn rec-map-mult-mask [mult-mask M]
  (reduce (fn [new-M [k mask]]
            (let [m (get M k)
                  actual-M (if (not (mORn? m)) 0 m)
                  actual-mask (if (not (mORn? mask)) 0 mask)
                  new-v
                    (cond
                      (maps? actual-mask actual-M) (rec-map-mult-mask actual-mask actual-M)
                      (mANDn? actual-M actual-mask) (rec-map-mult-share actual-mask actual-M) ; leaf works!
                      (mANDn? actual-mask actual-M) 0
                      :else (* actual-M actual-mask))]
              (if (nullelt? new-v) new-M (assoc new-M k new-v)))) 
          {} mult-mask))

; this test just takes squares of all leaves

(rec-map-mult-mask testmap testmap)
; andswer {:b 4, :c {:b 4, :c {:x 1, :y 9}, :a 1}, :a 1}

; this works too, it multiplies the :c subtree of testmap by 7

(def testmask {:c 7})

(rec-map-mult-mask testmask testmap)
; answer {:c {:b 14, :c {:x 7, :y 21}, :a 7}}

; generalized linear combination - same as above, but
; compute the sum of the resulting vectors corresponding
; to the leaves in the Mask.


; right now the way it uses rec-map-sum somewhat ignores large-M vs small-M distinction
; it should not matter correctness-wise, but should eventually prompt a meditation
; efficiency-wise; we did just ended the practice of adding a non-trivial map
; to {} interpreted as a large map, so some progress here.

; this function is very similar to rec-map-mult-mask, the only difference
; except for its name (and hence recursive call) should have been
; that rec-map-sum is used instead of assoc

; but rec-map-sum is current only works for maps, which is why
; part of its functionality is duplicated here - something to meditate upon
; during a code review

(defn rec-map-lin-comb [mult-mask M]
  (reduce (fn [new-M [k mask]]
            (let [m (get M k)
                  actual-M (if (not (mORn? m)) 0 m)
                  actual-mask (if (not (mORn? mask)) 0 mask)
                  v-to-add
                    (cond
                      (maps? actual-mask actual-M) (rec-map-lin-comb actual-mask actual-M)
                      (mANDn? actual-M actual-mask) (rec-map-mult-share actual-mask actual-M) ; leaf works!
                      (mANDn? actual-mask actual-M) 0
                      :else (* actual-M actual-mask))
                  new-sum
                    (cond
                       (map? v-to-add) 
                          (if (= new-M {}) v-to-add (rec-map-sum new-M v-to-add))
                       (num-nonzero? v-to-add) (rec-map-sum new-M (numelt v-to-add)) 
                       :else new-M)]
              new-sum))
          {} mult-mask))

; the following tests work

(rec-map-lin-comb testmask testmask)
; answer {:number 49}

(rec-map-lin-comb testmask testmap)
; answer {:a 7, :c {:x 7, :y 21}, :b 14}

(rec-map-lin-comb testmap testmap)
; answer {:number 20}

(rec-map-lin-comb testmap testmask)
; answer {}

; the addendum to caveat 1 we've just committed ("caveat 1 continued")
; means that the "down movement" is just plain matrix multiplication
; without any extra frills. The application of the matrix row to
; the vector of neuron outputs is simply "rec-map-lin-comb".

; let's say, applying a row to the vector of outputs is level 0,
; and applying a map of rows obtaining the map of answer is level 1, etc.
; Our current design spec says that the "down movement" is applying this
; funcion at level 3.

(defn apply-matrix [arg-matrix arg-vector level]
  (if (= level 0) 
    (rec-map-lin-comb arg-matrix arg-vector)
    (reduce (fn [new-map [k v]]
              (assoc new-map k (apply-matrix v arg-vector (- level 1))))
      {} arg-matrix)))

(def arg-v {:a 3. :b 5}) ; these don't have to be numbers, can be any vectors

(def arg-m {:x {:a 8} :y {:b 10} :z {:a 2, :b 4}})

(apply-matrix arg-m arg-v 1)
; answer {:x {:number 24.0}, :y {:number 50}, :z {:number 26.0}}

; the extra ":number" above are a bit annoying; we'll see if that's a problem,
; since for deeper vectors we have

(def arg-v1 {:a {:v 3}, :b {:v 5, :w {:v 8}}})

(apply-matrix arg-m arg-v1 1)
; answer {:x {:v 24}, :y {:v 50, :w {:v 80}}, :z {:w {:v 32}, :v 26}}

; accumulator (in the first version it is just the sum, the user is expected
; to keep the matrix row corresponding to :accum field at 1 at the appropriate place
; and zero at all others)

(defn accum [input]
  (rec-map-sum (input :accum) (input :delta)))


; provisional version of down-movement (UNTESTED, need to define "accum" before it might work)

(defn down-movement [function-named-instance-map-of-outputs]
  (apply-matrix
    ((function-named-instance-map-of-outputs accum) :self) ; current matrix
    function-named-instance-map-of-outputs ; arg-vector
    3))

