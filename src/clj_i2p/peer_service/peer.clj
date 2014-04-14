(ns clj-i2p.peer-service.peer
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.peer-service.calls.list-peers :as list-peers-call]
            [clj-i2p.peer-service.calls.notify :as notify-call]
            [clj-i2p.peer-service.persister-protocol :as persister-protocol]
            [clj-i2p.client :as client]
            [clj-i2p.core :as clj-i2p-core])
  (:import [java.util Date]))

(defn local-destination? [peer-destination]
  (and peer-destination (clj-i2p-core/is-current-destination-base-64? peer-destination)))

(defn valid-non-local-destination? [peer-destination]
  (and peer-destination (not (local-destination? peer-destination))))

(defn notify-peer-destination
  "Notifies the peer at the given destination that this peer exists."
  [peer-destination]
  (if (notify-call/call peer-destination)
    (persister-protocol/update-registered-peer
      (merge (persister-protocol/find-by-peer-destination peer-destination)
             { :notified true :updated-at (new Date) }))
    (logging/info (str "Destination " peer-destination " is not online."))))

(defn notify-all-peers
  "Notifies all unnotified peers of this instance."
  []
  (doseq [peer (persister-protocol/all-unnotified-registered-peers)]
    (.start (Thread. #(notify-peer-destination (:destination peer))))))

(defn notified?
  "Returns true if the given peer has been notified."
  [peer]
  (:notified peer))

(defn notify-peer-destination-if-necessary
  "Notifies the peer associated with the given destination if and only if the
peer has not already been notified."
  [peer-destination]
  (when-let [peer (persister-protocol/find-by-peer-destination
                    peer-destination)]
    (when-not (notified? peer)
      (notify-peer-destination peer-destination))))

(defn load-all-peers-from [peer-destination]
  (when (valid-non-local-destination? peer-destination)
    (when-let [peers (list-peers-call/call peer-destination)]
      (logging/info (str "Adding peers from destination: " peer-destination))
      (persister-protocol/add-peer-destinations (filter valid-non-local-destination? peers))
      (when (not-empty peers)
        (persister-protocol/set-registered-peers-downloaded? true)))))

(defn load-all-peers-from-seq [peer-destinations]
  (doseq [peer-destination peer-destinations]
    (.start (Thread. #(load-all-peers-from peer-destination)))))

(defn load-all-peers
  "Attempts to load all peers on the network."
  []
  (logging/info "loading peers")
  (load-all-peers-from-seq
    (concat
      (map :destination (persister-protocol/all-notified-registered-peers))
      (persister-protocol/default-registered-destinations))))

(defn reload-peers []
  (logging/info "Reloading peers")
  (load-all-peers-from-seq
    (map :destination (persister-protocol/all-notified-registered-peers))))

(defn download-peers-background []
  (logging/info "Downloading peers in background.")
  (if (persister-protocol/registered-peers-downloaded?)
    (reload-peers)
    (load-all-peers))
  (notify-all-peers))

(defn download-peers []
  (future (download-peers-background)))

(defn send-messages [service data call-back]
  (client/send-messages
    (map :destination (persister-protocol/all-foreign-registered-peers)) service data call-back))