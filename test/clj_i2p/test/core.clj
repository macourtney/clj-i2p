(ns clj-i2p.test.core
  (:use [clj-i2p.core])
  (:use [clojure.test]))

(deftest current-destination-test
  (is (nil? (current-destination))))
