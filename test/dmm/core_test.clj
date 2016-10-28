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
