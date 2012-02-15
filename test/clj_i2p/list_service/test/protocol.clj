(ns clj-i2p.list-service.test.protocol
  (:use [clj-i2p.list-service.protocol])
  (:use [clojure.test])
  (:require [clj-i2p.service-protocol :as service-protocol]))

(deftest list-service-test
  (let [list-service (create-list-service)]
    (is (= :list-service (service-protocol/key list-service)))
    (is (= "List Service" (service-protocol/name list-service)))
    (is (= "1.0.0" (service-protocol/version list-service)))
    (is (= "This is a service which lists and handles info about peers." (service-protocol/description list-service)))))
