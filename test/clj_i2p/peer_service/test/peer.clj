(ns clj-i2p.peer-service.test.peer
  (:use [clj-i2p.peer-service.peer])
  (:use [clojure.test])
  (:require [clj-i2p.resources.memory-peer-persister :as memory-peer-persister]
            [clj-i2p.service-protocol :as service-protocol])
  (:import [java.util Date]))

(def test-peer-1 { :id 1 
                   :destination "test-destination"
                   :created_at (Date. 1335383546623)
                   :updated_at (Date. 1335383546623)
                   :notified true
                   :local false })

(def test-peer-2 { :id 2 
                   :destination "test-destination-2"
                   :created_at (Date. 1335383556623)
                   :updated_at (Date. 1335383556623)
                   :notified false
                   :local false })

(def test-peer-3 { :id 3
                   :destination "test-destination-3"
                   :created_at (Date. 1335383566623)
                   :updated_at (Date. 1335383566623)
                   :notified true
                   :local true })

(defn collection-contains? [collection & values]
  (is (= (count collection) (count values)))
  (let [collection-set (set collection)]
    (doseq [value values]
      (is (contains? collection-set value)))))

(deftest persister-protocol
  (memory-peer-persister/register-memory-peer-persister)
  (insert-peer test-peer-1)
  (insert-peer test-peer-2)
  (insert-peer test-peer-3)
  (collection-contains? (all-peers) test-peer-1 test-peer-2 test-peer-3)
  (collection-contains? (all-foreign-peers) test-peer-1 test-peer-2)
  (is (= (find-peer { :id (:id test-peer-1) }) test-peer-1))
  (is (= (find-peer { :destination (:destination test-peer-2) }) test-peer-2))
  (collection-contains? (find-all-peers { :notified true }) test-peer-1 test-peer-3)
  (is (= (last-updated-peer) test-peer-3))
  (collection-contains? (all-unnotified-peers) test-peer-2)
  (collection-contains? (all-notified-peers) test-peer-1 test-peer-3)
  (delete-peer test-peer-1)
  (collection-contains? (all-peers) test-peer-2 test-peer-3))
