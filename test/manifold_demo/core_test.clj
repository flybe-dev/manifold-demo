(ns manifold-demo.core-test
  (:require [clojure.test :refer :all]
            [manifold-demo.core :refer :all]
            [manifold.deferred :as d]
            [clojure.core.async :as a]))

(defn delayed-inc
  [x]
  (d/future
    (Thread/sleep 1000)
    (inc x)))

(defn bomb
  [time]
  (d/future
    (Thread/sleep time)
    (throw (RuntimeException.))))

(defn f-bomb
  [time]
  (let [fail (d/deferred)]
    (d/future
      (Thread/sleep time)
      (d/error! fail (Exception. "BOooomoOOOOMMMMMMMM!")))
    fail))

(defn get-eggs
  []
  (d/future (Thread/sleep 1000) "eggs"))

(defn get-bacon
  []
  (let [bacon (d/deferred)]
    (d/future
      (Thread/sleep 1000)
      (d/success! bacon "bacon"))
    bacon))

(defn cloud-concatenate
  [& args]
  (future
    (Thread/sleep 1000)
    (apply str (interpose " and " args))))

(comment
  @(d/chain 1 delayed-inc delayed-inc inc))

(comment
  (d/let-flow
    [eggs (get-eggs)
     bacon (get-bacon)]
    (cloud-concatenate eggs bacon))

  (let [a (a)
        b (b)]
    (d/chain
      (d/zip a b)                                           ;yields a deferred list that will contain [@a @b]
      (fn [[a b]]
        (+ a b)))))

(comment
  (let [eggs (get-eggs)
        bacon (get-bacon)]
    (cloud-concatenate @eggs @bacon)))

(comment
  (time @(let [eggs @(get-eggs)
               bacon @(get-bacon)]
           (cloud-concatenate eggs bacon))))

(defn put-on-chan-with-delay
  [item delay]
  (let [c (a/chan)]
    (future
      (Thread/sleep delay)
      (a/put! c item))
    c))

(defn get-eggs-async
  []
  (put-on-chan-with-delay "eggs" 1000))

(defn get-bacon-async
  []
  (put-on-chan-with-delay "bacon" 2000))


(comment
  (a/<!! (a/map list [(get-eggs-async) (get-bacon-async)])))

(comment
  core.async is also a great solution but the catch is that you
  do have to use it everywhere

  manifold can quite happily coexist with ordinary clojure futures and promises)
