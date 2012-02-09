(ns clj-i2p.resources.test-service
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

(defn create-test-service []
  (TestService.))