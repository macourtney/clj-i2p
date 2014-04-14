(ns clj-i2p.peer-service.test.peer
  (:use [clj-i2p.peer-service.peer])
  (:use [clojure.test])
  (:require [clj-i2p.peer-service.persister-protocol :as persister-protocol]
            [clj-i2p.resources.memory-peer-persister :as memory-peer-persister]
            [clj-i2p.service-protocol :as service-protocol])
  (:import [java.util Date]))

(def test-peer-1 { :id 1 
                   :destination "test-destination"
                   :created-at (Date. 1335383546623)
                   :updated-at (Date. 1335383546623)
                   :notified true
                   :local false })

(def test-peer-2 { :id 2 
                   :destination "test-destination-2"
                   :created-at (Date. 1335383556623)
                   :updated-at (Date. 1335383556623)
                   :notified false
                   :local false })

(def test-peer-3 { :id 3
                   :destination "test-destination-3"
                   :created-at (Date. 1335383566623)
                   :updated-at (Date. 1335383566623)
                   :notified true
                   :local true })

(defn collection-contains? [collection & values]
  (is (= (count collection) (count values)))
  (let [collection-set (set collection)]
    (doseq [value values]
      (is (contains? collection-set value)))))

(deftest persister-protocol
  (memory-peer-persister/register-memory-peer-persister)
  (persister-protocol/insert-registered-peer test-peer-1)
  (persister-protocol/insert-registered-peer test-peer-2)
  (persister-protocol/insert-registered-peer test-peer-3)
  (collection-contains? (persister-protocol/all-registered-peers)
                        test-peer-1 test-peer-2 test-peer-3)
  (collection-contains? (persister-protocol/all-foreign-registered-peers)
                        test-peer-1 test-peer-2)
  (is (= (persister-protocol/find-registered-peer { :id (:id test-peer-1) })
         test-peer-1))
  (is (= (persister-protocol/find-registered-peer
           { :destination (:destination test-peer-2) })
         test-peer-2))
  (collection-contains?
    (persister-protocol/find-all-registered-peers { :notified true })
    test-peer-1 test-peer-3)
  (is (= (persister-protocol/last-updated-registered-peer) test-peer-3))
  (collection-contains? (persister-protocol/all-unnotified-registered-peers)
                        test-peer-2)
  (collection-contains? (persister-protocol/all-notified-registered-peers)
                        test-peer-1 test-peer-3)
  (persister-protocol/delete-registered-peer test-peer-1)
  (collection-contains? (persister-protocol/all-registered-peers) test-peer-2 test-peer-3))
