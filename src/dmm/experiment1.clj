(ns dmm.experiment1
  [:require
   [clojure.core.matrix :as m]
   [clojure.core.matrix.operators :as mo]
   [clojure.core.matrix.dataset :as ds]])


(m/set-current-implementation :vectorz)



(def M (m/matrix [[1 2]
                  [3 4]]))
(def M2 (m/matrix [[3 2]
                   [1 5]]))

(def v (m/matrix [1 2]))

(m/mul M v)
(m/square M)


(def a1 (m/array :vectorz [1 2 3]))
(def b1 (m/array [1 2 3]))
(= a1 b1)


(def M3 (m/outer-product a1 a1))



(def A (m/new-sparse-array [1000000 1000000]))

(dotimes [i 1000]
  (m/mset! A (rand-int 1000000) (rand-int 1000000) (rand-int 100)))


(def B (m/new-sparse-array [100]))

(dotimes [i 10]
  (m/mset! B (rand-int 100) (rand-int 100)))

(m/non-zero-count B)
#_(m/outer-product B B) ; <-- returns full matrix!! Kills JVM

(m/esum A)

(time (m/add A A))

(time (m/mmul A (m/transpose A)))

(m/non-zero-count A)
(m/zero-count A)
(m/zero-matrix? A)
(m/submatrix A 0 10 0 10)
(m/submatrix A 0 [0 10])


(m/non-zero-count (m/mmul A (m/transpose A)))

(m/non-zero-indices (m/matrix [[3 0]
                               [0 5]]))

(def indices (m/non-zero-indices (m/matrix [[3 0 1]
                                            [0 0 0]
                                            [1 1 0]])))

(map #(map identity %) indices)

(defn nonzero [indices]
  (->> indices (map #(mapv identity %))
       (interleave (range))
       (partition-all 2)
       (mapv vec)
       (keep (fn[[i p :as a]] (when (seq p) a)))))

(nonzero indices)
(take 10 (nonzero (m/non-zero-indices A)))
(take 10 (nonzero (m/non-zero-indices (m/mmul A (m/transpose A)))))

(ds/dataset [{"a" 1 "b" 3 "c" 4}
             {"a" 3.14 "b" 2.718 "c" 0.0}
             {"a" 17.1 "b" 7.0 "c" 33.3}])
