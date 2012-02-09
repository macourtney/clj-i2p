(ns clj-i2p.test.service
  (:use [clj-i2p.service])
  (:use [clojure.test]))

(def test-service-key :test-service)
(def response-map { :data "blah" })

(defn test-service [request-map]
  response-map)

(deftest service-test
  (add-service test-service-key test-service)
  (is (find-service test-service-key))
  (reset-services!)
  (is (not (find-service test-service-key))))
