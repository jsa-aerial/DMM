(in-ns 'dmm.core)

(require '[com.rpl.specter :as s])

(let [ks [:a :b :c :d]]
  (loop [v [{:a 1 :b 2 :c 3 :d {:a 1 :b 2 :c [1 2 3]}}]
         cnt 0]
    (if (> cnt 3)
      v
      (recur
       (conj v (update-in
                (v cnt) [(ks cnt)]
                (fn[o] (cond
                        (number? o) (* 10 o)
                        (map? o) (assoc o :b 33)
                        (vector? o) [(o 0) 77 (o 2)]
                        ))))
       (inc cnt)))))


(let [ks [:a :b :c :d]
      v [{:a 1 :b 2 :c 3 :d {:a 1 :b 2 :c [1 2 3]}}]
      cnt 0]
  (update-in
   (v cnt) [(ks cnt)] (fn[o] (* 10 o))))


(let [ks [:a :b :c :d]
      v [{:a 1 :b 2 :c 3 :d {:a 1 :b 2 :c [1 2 3]}}]
      cnt 0]
  (s/transform [s/FIRST :a] #(* % 10) v)
  )


;; OK< what we want to do is use spectre to do a double update:
;; We want to ADD to the vector X, the transform of its previous last
;; element. NOT YET DONE
(let [ks [:a :b :c :d]
      v [{:a 1 :b 2 :c 3 :d {:a 1 :b 2 :c [1 2 3]}}]]
  (loop [x v, cnt 0]
    (if (> cnt 0)
      x
      (recur
       (s/setval s/END (s/transform
                        [(ks cnt)]
                        (fn[o] (cond
                                (number? o) (* 10 o)
                                (map? o) (assoc o :b 33)
                                (vector? o) [(o 0) 77 (o 2)]))
                        (last x))
                 x)
       (inc cnt)))))



(def data {:a [{:aa 1 :bb 2}
               {:cc 3}]
           :b [{:dd 4}]})

(s/transform [s/MAP-VALS s/ALL s/MAP-VALS even?] inc data)
