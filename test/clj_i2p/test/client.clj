(ns clj-i2p.test.client
  (:use [clj-i2p.client])
  (:use [clojure.test]))

(deftest current-destination-test
  (is (nil? (current-destination))))
