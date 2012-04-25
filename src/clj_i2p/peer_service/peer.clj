(ns clj-i2p.peer-service.peer
  (:require [clj-i2p.peer-service.persister-protocol :as persister-protocol]))

(defn insert-peer
  "Saves the given peer."
  [peer]
  (persister-protocol/insert-peer (persister-protocol/registered-protocol) peer))

(defn update-peer
  "Updates a peer using the given peer map as a prototype."
  [peer]
  (persister-protocol/update-peer (persister-protocol/registered-protocol) peer))

(defn delete-peer
  "Deletes the given peer."
  [peer]
  (persister-protocol/delete-peer (persister-protocol/registered-protocol) peer))

(defn all-peers
  "Returns all known peers."
  []
  (persister-protocol/all-peers (persister-protocol/registered-protocol)))

(defn all-foreign-peers
  "Returns all foreign peers."
  []
  (persister-protocol/all-foreign-peers (persister-protocol/registered-protocol)))

(defn find-peer
  "Returns the first peer using the given peer as a prototype."
  [peer]
  (persister-protocol/find-peer (persister-protocol/registered-protocol) peer))

(defn find-all-peers
  "Finds all peers like the given peer."
  [peer]
  (persister-protocol/find-all-peers (persister-protocol/registered-protocol) peer))

(defn last-updated-peer
  "Returns the last updated peer."
  []
  (persister-protocol/last-updated-peer (persister-protocol/registered-protocol)))

(defn all-unnotified-peers
  "Returns all peers which have not been notified yet."
  []
  (persister-protocol/all-unnotified-peers (persister-protocol/registered-protocol)))

(defn all-notified-peers
  "Returns all peers which have been notified."
  []
  (persister-protocol/all-notified-peers (persister-protocol/registered-protocol)))

(defn add-peer-update-listener
  "Registers a listener which will be notified when a peer is inserted or updated in the system."
  [listener]
  (persister-protocol/add-peer-update-listener (persister-protocol/registered-protocol) listener))

(defn remove-peer-update-listener
  "Registers a listener which will be notified when a peer is inserted or updated in the system."
  [listener]
  (persister-protocol/remove-peer-update-listener (persister-protocol/registered-protocol) listener))

(defn add-peer-delete-listener
  "Registers a listener which will be notified when a peer is deleted from the system."
  [listener]
  (persister-protocol/add-peer-delete-listener (persister-protocol/registered-protocol) listener))

(defn remove-peer-delete-listener
  "Registers a listener which will be notified when a peer is deleted from the system."
  [listener]
  (persister-protocol/remove-peer-delete-listener (persister-protocol/registered-protocol) listener))