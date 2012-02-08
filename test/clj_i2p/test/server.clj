(ns clj-i2p.test.server
  (:use [clj-i2p.server])
  (:use [clojure.test]))

(def test-action-key :test-action)
(def response-map { :data "blah" })

(defn test-action [request-map]
  response-map)

(deftest action-test
  (add-action test-action-key test-action)
  (is (find-action test-action-key))
  (is (= (assoc response-map :action test-action-key) (run-action { :action test-action-key }))))
