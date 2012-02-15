(ns clj-i2p.list-service.test.core
  (:use [clj-i2p.list-service.core])
  (:use [clojure.test])
  (:require [clj-i2p.resources.test-service :as test-service]
            [clj-i2p.service :as i2p-service]
            [clj-i2p.service-protocol :as service-protocol]))

(deftest service-description-map-test
  (let [service (test-service/create-test-service)]
    (is (= { :name (service-protocol/name service) :description (service-protocol/description service) }
           (service-description-map service)))))

(deftest list-service-versions-test
  (let [service (test-service/create-test-service)
        version-key (keyword (service-protocol/version service))]
    (is (= { version-key (service-description-map service) }
           (list-service-versions { version-key service })))))

(deftest list-services-test
  (let [service (test-service/create-test-service)]
    (i2p-service/add-service service)
    (is (= { (keyword (service-protocol/key service)) 
             { (keyword (service-protocol/version service)) (service-description-map service) } }
           (list-services)))
    (i2p-service/reset-services!)))
