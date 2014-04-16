(ns clj-i2p.test.client
  (:require [clj-i2p.core :as core]
            [test.util :as test-util])
  (:use [clj-i2p.client])
  (:use [clojure.test]))

(def test-to-destination (core/as-destination "~hheKu6kY-vcWTjDvOToD8qUoCbrZ9zb6Zu6qSsm4OD9Ea6DIgW047svze-lRc0PR~5NuYb61DvU7fQodTeMSEXtC~5PxY2QjFmSouRrzim6f7qhr-NLva4k22UGurgU9gotrPWwlb6r6k4CRu7INyTV2LSTc7E9RIZCjUqq~1B4odnhmLSgF4I34zCSzOx6GtTAAiancIl04yAV8ee-wdzTFOmhQpjWclOXxardMQ4EXlkqpXtLi6b5pPtTtNsT75UJVfjpDE9cI63EvXDdyRBcsDiMu-aWj21EhoZzEnCRRHt71REzQrp2pPnoLYPBK0bKN2guJ4K7ABAl7CElU0~3~YQSnDlTJ~kSnsMix6NUrP37D2dZTr5e6JhhWZPiu3Qmin6l91geGbVP~DQkljcoAECkC43TTuTF84GGa~3SVyA5B9wmC4zgvNQ0HuEEC5ijR7bk2Iyp7tz60pSJHMsBTnpnNqfzoGzZjSOVaTJRsK76BR5eiPnnTGorOpocAAAA"))
(def test-data { :foo "bar" })
(def test-response { :biz "baz" })

(defn test-request-map []
  { core/service-key test-util/test-service-key
    core/service-version-key test-util/test-service-version-key
    core/data-key test-data
    core/from-key (request-map-from) })

(defn mock-network
  "A mock network for testing the send-message function."
  [destination request-map]
  (is (= destination test-to-destination))
  (is (= request-map (test-request-map)))
  test-response)

(use-fixtures
  :once (join-fixtures [test-util/destination-fixture
                        (test-util/create-mock-network-fixture mock-network)]))

(deftest current-destination-test
  (is (= (current-destination) test-util/test-destination)))

(deftest test-request-map-from
  (is (= (request-map-from) { :destination (base-64-destination) })))

(deftest test-create-request-map
  (is (= (update-request-map
           test-to-destination test-util/test-service
           { core/data-key test-data })
         (assoc (test-request-map) core/destination-key test-to-destination))))

(deftest test-send-message
  (is (= (send-message test-to-destination test-util/test-service
                       { core/data-key test-data })
         test-response)))