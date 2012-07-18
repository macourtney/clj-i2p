(ns clj-i2p.mock.test.mock-network
  (:use [clj-i2p.mock.mock-network])
  (:use [clojure.test])
  (:require [clj-i2p.core :as clj-i2p-core]))

(def test-destination1 "alskn09jh0pksd9-09jdklf")
(def test-request-data1 { :test "foo" })
(def test-response-data1 { :test "bar" })

(def test-destination2 "alskn09jh0pksd9-09jdghwer")
(def test-request-data2 { :test "foo2" })
(def test-response-data2 { :test "bar2" })

(def test-destination-error "alskn09jh0pksd9-09error")

(defn mock-destination-fn1 [data]
  (is (= data test-request-data1))
  test-response-data1)

(defn mock-destination-fn2 [data]
  (is (= data test-request-data2))
  test-response-data2)

(deftest test-mock-destinations
  (is (= @mock-destinations {}))
  (add-mock-destination test-destination1 mock-destination-fn1)
  (add-mock-destination test-destination2 mock-destination-fn2)
  (is (= @mock-destinations { test-destination1 mock-destination-fn1 test-destination2 mock-destination-fn2 }))
  (is (= (mock-network test-destination1 test-request-data1) test-response-data1))
  (is (= (mock-network test-destination2 test-request-data2) test-response-data2))
  (try
    (mock-network test-destination-error "nil")
    (is false "Expected unregistered destination to throw an exception.")

    (catch RuntimeException e
      ; Expected a runtime exception.
      nil))
  (reset-mock-destinations)
  (is (= @mock-destinations {})))

(deftest test-init
  (is (nil? (clj-i2p-core/get-mock-network)))
  (init)
  (is (= (clj-i2p-core/get-mock-network) mock-network))
  (clj-i2p-core/clear-mock-network))