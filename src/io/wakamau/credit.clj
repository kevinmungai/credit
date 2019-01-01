(ns io.wakamau.credit
  (:require [clojure.string :as string]))

(defn- to-digits [number base]
  (loop [n number
         digits '()]
    (if (pos? n)
      (recur (quot n base)
             (conj digits (mod n base)))
      digits)))

(defn to-octal [number]
  "returns a list of octal numbers"
  (to-digits number 8))

(defn mod-add-two-octal [x y]
  "add two octal numbers using modulo arithmetic

   I would have to submit both the quotient and the modulus"
  (let [sum (+ x y)
        q (quot sum 8)
        m (mod sum 8)]
    {:q q :m m}))

(defn add-two-octal [x y]
  "add two octal numbers"
  (let [a (Integer/parseInt (str x) 8)
        b (Integer/parseInt (str y) 8)]
    (-> (+ a b)
        Integer/toOctalString
        Integer/parseInt)))

(defn separate [number]
  (loop [n number
         digits '()]
    (if (zero? n)
      digits
      (recur (quot n 10)
             (conj digits (rem n 10))))))

(defn sum-octal [number]
  (let [sum (reduce #(add-two-octal %1 %2)
                    (to-octal number))]
    (if (zero? (quot sum 10))
      sum
      (apply add-two-octal (separate sum)))))

(defn is-valid? [number]
  (let [first-four (quot number 10)
        check-sum (rem number 10)
        verify (sum-octal first-four)]
    (= verify check-sum)))

(defn validate-scratch-card [card-number]
  (let [f (comp is-valid? #(Integer/parseInt %))]
    (->> (string/split card-number #"(-|\s)")
         (map f)
         (every? true?))))
