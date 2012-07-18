(ns clj-i2p.test.core
  (:use [clj-i2p.core])
  (:use [clojure.test]))

(def test-destination "alskn09jh0pksd9-09j")
(def test-request-data { :test "foo" })
(def test-response-data { :test "bar" })

(deftest current-destination-test
  (is (nil? (current-destination))))

(defn mock-network-fn [destination data]
  (is (= destination test-destination))
  (is (= data test-request-data))
  test-response-data)

(deftest test-mock-network
  (is (nil? @mock-network))
  (set-mock-network mock-network-fn)
  (is (= @mock-network mock-network-fn))
  (is (= (send-message test-destination test-request-data) test-response-data))
  (clear-mock-network)
  (is (nil? @mock-network)))