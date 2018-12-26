# io.wakamau/credit

# Custom Card Number Verification System Challenge
Custom Card Number Verification System Programming Challenge by Cellulant using Clojure


# caveat
This programming challenge is found at 
> [http://neo.cellulant.com:3000/sw_internship.html](http://neo.cellulant.com:3000/sw_internship.html)

or 

> [Google Web Cache](http://webcache.googleusercontent.com/search?q=cache:http://neo.cellulant.com:3000/sw_internship.html)

and it will be **presented as is**:


# the challenge

> Cellulant has implemented a custom, card number verification system to ensure detection and blocking of fake cards. A scratch card consists of **4** sets of **5** digits (20 digits in total) e.g. [**10006-12342-00081-99993**] which are printed separated by a space or a dash. 
>
> Each set of 5 digits consists of two parts, the first 4 digits (the number) and a 5th digit (the checksum) The formula dictates that each of the sets must be validated as follows: The first 4 digits, e.g 1234, as decimal numbers be converted into an octal number (base 8) i.e. 23228. This octal number is then processed to generate the checksum, as follows:
>
>
> 1. Add all digits to each other to get a new number **“X”**
> 2. If **“X”** is more than 1 digit, repeat step **(1)** until you have a single digit. **“Y”**
> 3. Append **“Y”** to the end of the original decimal number e.g. 1234 **2**
>
> #### For Example:
> **Given the set “10006”**
>
> Convert 1000 to octal =>1750
>
> 1+7+5+0=15
>
> 1+5=6
>
> Valid number is 10006 so;
>
> return **(TRUE)**
>
>
> **Given the set “99998”**
>
> Convert 9999 to octal => 23417
>
> 2+3+4+1+7=218
>
> 2+1=3
>
> Valid number is 99993 so;
>
> return **(FALSE)**
>
> Create a **Java**, **Java-Script** or **PHP** script with the **function** that will receive a **string** value **(scratch card number)** and return boolean TRUE or FALSE, on whether the card number is valid, as shown below
>
> You should use as **many other functions** as you deem necessary within your code.

# the solution

Instead of using any of the proposed languages above, I shall use **Clojure**.

`validate-scratch-card` is a function which takes a **string** which may look like:

1. "`10006-12342-00081-99993`"
2. "`10006 12342 00081 99993`"

It would be nice to split the argument based on the presence of a space or a hyphen so that I could work on each substring:

``` clojure
;; has hyphen
user> (clojure.string/split "10006-12342-00081-99993" #("-|\s)")
;; ["10006" "12342" "00081" "99993"]

;; has spaces
user> (clojure.string/split "10006 12342 00081 99993" #"(-|\s)")
;; ["10006" "12342" "00081" "99993"]
```

Next item on the agenda is to parse each individual substring to `Integer` via the Java method `Integer/parseInt`.

``` clojure
user> (Integer/parseInt "10006")
;; 10006
```

`map` can be used to parse each individual substring

``` clojure
user> (map #(Integer/parseInt %) "10006 12342 00081 99993")
;; 10006
```

Each Integer can be **checked for validity** based on the rules above.
Each Integer has 2 parts:

1. first four digits (the number)
2. the fifth digit (the checksum)

``` clojure
user> (quot 10006 10)
;; 1000   first four digits (the number)

user> (rem 10006 10)
;; 6   fifth digit (checksum)
```

Next is to convert the first four digits to octal.

This will be achieved by creating a base converter

``` clojure
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
```

``` clojure
user> (to-octal 1000)
;; (1 7 5 0)
```

Subsequent step is to sum up each of the generated numbers.

But before that, a function that takes two octal numbers and sums them up is needed.

``` clojure
(defn add-two-octal [x y]
  "add two octal numbers"
  (let [a (Integer/parseInt (str x) 8)
        b (Integer/parseInt (str y) 8)]
    (-> (+ a b)
        Integer/toOctalString
        Integer/parseInt)))
```

``` clojure
user> (add-two-octal 3 6)
;; 11
```

This `add-two-octal` function can then be used to sum up a list of octal numbers together as follows:

``` clojure
user> (reduce #(add-two-octal %1 %2)
              (to-octal 1000))
;; 15

user> (reduce #(add-two-octal %1 %2)
              '(1 7 5 0))
;; 15
```

Although the addition works fine it doesn't satisfy the requirement that the result of the sum should be a single digit.
If a single digit is the result of the sum then the single digit should be returned else the summation should be done again.
Also there is need for a function to separate the new summation if it happens to be two digits.

``` clojure
(defn separate [number]
  "a utility function to separate digits"
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
```

``` clojure 
user> (sum-octal 1000)
;; 6
```

Next a function that takes a number and checks for validity can be written as follows:

``` clojure
(defn is-valid? [number]
  (let [first-four (quot number 10)
        check-sum (rem number 10)
        verify (sum-octal first-four)]
    (= verify check-sum)))
```

``` clojure
user> (is-valid? 10006)
;; true

user> (is-valid? 99998)
;; false
```

The `is-valid?` function and the `#(Integer/parseInt %)` can be composed together

``` clojure
(comp is-valid? #(Integer/parseInt %))

user> ((comp is-valid? #(Integer/parseInt %)) "10006")
;; true
```

Finally, the `validate-scratch-card` function will shape up as follows

``` clojure
(defn validate-scratch-card [card-number]
  (let [f (comp is-valid? #(Integer/parseInt %))]
    (->> (string/split card-number #"(-|\s)")
         (map f)
         (every? true?))))
```


``` clojure
user> (validate-scratch-card "10006 12342 00081 99993")
;; true

user> (validate-scratch-card "10006 12342 00081 99998")
;; false
```
