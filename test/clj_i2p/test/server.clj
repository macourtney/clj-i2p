(ns clj-i2p.test.server
  (:use [clj-i2p.server])
  (:use [clojure.test])
  (:require [clj-i2p.resources.test-service :as test-service]
            [clj-i2p.service :as service]
            [clj-i2p.service-protocol :as service-protocol]))

(deftest service-test
  (let [test-service (test-service/create-test-service)
        test-service-key (keyword (service-protocol/key test-service))
        test-service-version (keyword (service-protocol/version test-service))]
    (service/add-service test-service)
  (is (service/find-service test-service-key test-service-version))
  (is (= (merge test-service/response-map { :service test-service-key :service-version test-service-version })
         (run-service { :service test-service-key :service-version test-service-version })))
  (service/reset-services!)))
