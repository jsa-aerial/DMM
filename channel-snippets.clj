
(def ch1 (chan 3))

(defn feed-ch1 [ms]
  (loop [n 1]
    (let [v (keyword (str "a" n))]
      (>!! ch1 v)
      (Thread/sleep ms))
    (if (< n 23)
      (recur (inc n))
      (>!! ch1 :done))))

(defn eat-ch1 [& args]
  (let [v (<!! ch1)]
    (if (= v :done)
      :done
      (println (str v " has " (count (str v)) " characters")))))

(def test-ch1-fut (future (feed-ch1 2000)))

(def eat-fut
  (future (loop [v (eat-ch1)]
            (if (= v :done)
              :finished
              (recur (eat-ch1))))))


(defn feed-ch1-fns
  [fs ms]
  (loop [n 1
         fs fs]
    (>!! ch1 (first fs))
    (Thread/sleep ms)
    (if (< n 11)
      (recur (inc n)
             (rotate fs))
      (>!! ch1 :done))))

(defn apply-ch1-fs
  [m]
  (loop [f (<!! ch1)
         res m]
    (if (= f :done)
      res
      (let [v (f res)]
        (println v)
        (recur (<!! ch1), v)))))

(def  test-ch1-fut (future (feed-ch1-fns [g h f] 1000)))
(def  eat-fut (future (apply-ch1-fs testmap)))
