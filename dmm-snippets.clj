;;;(take 7 (iterate rotate [:f1 :f2 :f3 :f4]))

(defn rotate
  ([coll]
   (rotate 1 coll))
  ([n coll]
   (concat (drop n coll) (take n coll))))

(defn mORn? [x] (or (map? x) (number? x)))
(defn mANDn? [x y] (and (map? x) (number? y)))
(defn =num0? [x] (and (number? x) (zero? x)))
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
                      (mANDn? actual-M actual-mask) (rec-map-mult actual-mask actual-M) ; leaf works!
                      (mANDn? actual-mask actual-M) 0
                      :else (* actual-M actual-mask))]
              (if (nullelt? new-v) new-M (assoc new-M k new-v)))) 
          {} mult-mask))

; this test just takes squares of all leaves

(rec-map-mult-mask testmap testmap)

; this works too, it multiplies the :c subtree of testmap by 7

(def testmask {:c 7})

(rec-map-mult-mask testmask testmap)

; generalized linear combination - same as above, but
; compute the sum of the resulting vectors corresponding
; to the leaves in the Mask.


; right now the way it uses rec-map-sum completely ignores large-M vs small-M distinction
; it should not matter correctness-wise, but should eventually prompt a meditation
; efficiency-wise

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
                      (mANDn? actual-M actual-mask) (rec-map-mult actual-mask actual-M) ; leaf works!
                      (mANDn? actual-mask actual-M) 0
                      :else (* actual-M actual-mask))
                  new-sum
                    (cond
                       (map? v-to-add) (rec-map-sum new-M v-to-add)
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
