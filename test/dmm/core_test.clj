(ns dmm.core-test
  (:require ;;[clojure.test :refer :all]
            [dmm.core :refer :all])
  (:use expectations))

(def testmap
  {:a 1 :b 2 :c {:a 1 :b 2 :c {:x 1 :y 3}}})

(expect testmap testmap)

(expect (rec-map-mult-mask testmap testmap)
        {:b 4, :c {:b 4, :c {:x 1, :y 9}, :a 1}, :a 1})


(def testmask {:c 7})

(expect (rec-map-mult-mask testmask testmap)
        {:c {:b 14, :c {:x 7, :y 21}, :a 7}})

(expect (rec-map-lin-comb testmask testmask)
        {:number 49})

(expect (rec-map-lin-comb testmask testmap)
        {:a 7, :c {:x 7, :y 21}, :b 14})

(expect (rec-map-lin-comb testmap testmap)
        {:number 20})

(expect (rec-map-lin-comb testmap testmask)
        {})

(def arg-v {:a 3. :b 5}) ; these don't have to be numbers, can be any vectors

(def arg-m {:x {:a 8} :y {:b 10} :z {:a 2, :b 4}})

(expect (apply-matrix arg-m arg-v 1)
        {:x {:number 24.0}, :y {:number 50}, :z {:number 26.0}})

;;; the extra ":number" above are a bit annoying; we'll see if that's
;;; a problem, since for deeper vectors we have

(def arg-v1 {:a {:v 3}, :b {:v 5, :w {:v 8}}})

(expect (apply-matrix arg-m arg-v1 1)
        {:x {:v 24}, :y {:v 50, :w {:v 80}}, :z {:w {:v 32}, :v 26}})

(expect (rec-map-sum (rec-map-lin-comb testmask testmap)
                     (rec-map-lin-comb {:c 1} testmap))
        {:b 16, :c {:x 8, :y 24}, :a 8})

(def try-input
  {:accum (rec-map-lin-comb testmask testmap)
   :delta (rec-map-lin-comb {:c 1} testmap)})

(expect (accum try-input)
        {:single {:b 16, :c {:x 8, :y 24}, :a 8}})

(expect (max-norm {}) {:number 0})

(expect (max-norm {:a 1,:c 1}) {:number 1})

(expect (max-norm {:a 1, :c 2}) {:number 2})

(expect (max-norm {:c -2, :a 1}) {:number 2})

;;; Test iter-apply-fns
(defn g [m]
  (rec-map-mult 2 m))

(defn h [m]
  (rec-map-add 3 m))

(defn f [m]
  (rec-map-div 7 m))

(expect
 (take 3 (iter-apply-fns testmap g h))
 '({:a 1, :b 2,
    :c {:a 1, :b 2, :c {:x 1, :y 3}}}
   {:a 2, :b 4,
    :c {:a 2, :b 4, :c {:x 2, :y 6}}}
   {:a 5, :b 7,
    :c {:a 5, :b 7, :c {:x 5, :y 9}}}))

(expect
 (first (drop 9 (iter-apply-fns testmap g h)))
 {:a 122, :b 154, :c {:a 122, :b 154, :c {:x 122, :y 186}}})

(expect
 (first (drop 9 (iter-apply-fns testmap g h f)))
 {:a 209/343, :b 31/49,
  :c {:a 209/343, :b 31/49, :c {:x 209/343, :y 225/343}}})
