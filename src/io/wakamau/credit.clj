(ns io.wakamau.credit
  (:require [clojure.string :as string]))

;; https://dev.to/kevinmungai/custom-card-number-verification-system-challenge-13o0

(defn is-valid?
  [number]
  (let [first-four (quot number 10)
        check-sum (rem number 10)]
    (-> first-four
        (mod 7)
        (- check-sum)
        (mod 7)
        (= 0))))

(defn validate-scratch-card [card-number]
  (let [f (comp is-valid? #(Integer/parseInt %))]
    (->> (string/split card-number #"(-|\s)")
         (map f)
         (every? true?))))
