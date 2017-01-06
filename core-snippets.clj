(in-ns 'dmm.core)

(let [ks [:a :b :c :d]]
  (loop [v [{:a 1 :b 2 :c 3 :d {:a 1 :b 2 :c [1 2 3]}}]
         cnt 0]
    (if (> cnt 2)
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
