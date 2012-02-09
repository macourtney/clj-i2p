(ns clj-i2p.test.server
  (:use [clj-i2p.server])
  (:use [clojure.test])
  (:require [clj-i2p.service :as service]
            [clj-i2p.service-protocol :as service-protocol]))

(def response-map { :data "blah" })

(deftype TestService []
  service-protocol/Service
  (key [service]
     :test-service)

  (name [service]
    "Test Service")
  
  (version [service]
    "1.0.0")

  (description [service]
    "This is a test service.")

  (handle [service request-map]
     response-map))

(deftest service-test
  (let [test-service (TestService.)
        test-service-key (service-protocol/key test-service)]
    (service/add-service test-service)
  (is (service/find-service test-service-key))
  (is (= (assoc response-map :service test-service-key) (run-service { :service test-service-key })))
  (service/reset-services!)))
