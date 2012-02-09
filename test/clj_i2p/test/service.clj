(ns clj-i2p.test.service
  (:use [clj-i2p.service])
  (:use [clojure.test])
  (:require [clj-i2p.resources.test-service :as test-service]
            [clj-i2p.service-protocol :as service-protocol]))

(deftest service-test
  (let [service (test-service/create-test-service)]
    (add-service service)
    (is (find-service (service-protocol/key service) (service-protocol/version service)))
    (reset-services!)
    (is (not (find-service (service-protocol/key service) (service-protocol/version service))))))
