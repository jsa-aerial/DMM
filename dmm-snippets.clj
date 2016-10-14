
(defn mORn? [x] (or (map? x) (number? x)))
(defn mANDn? [x y] (and (map? x) (number? y)))
(defn =num0? [x] (and (number? x) (zero? x)))
(defn nullelt? [x] (or (= x {}) (=num0? x)))
(defn numelt [x] {:number x})

(defn maps? [x & xs]
  (reduce (fn[tv b] (and tv b)) (map map? (conj xs x))))

(defn rec-map-mult [n M]
  (reduce (fn [M [k v]]
            (let [new-v
                   (cond
                     (map? v) (rec-map-mult n v)
                     (number? v) (* n v)
                     :else 0)]
               (if (nullelt? new-v) M (assoc M k new-v))))
          {} M))

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
