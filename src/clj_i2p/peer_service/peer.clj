(ns clj-i2p.peer-service.peer
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.peer-service.list-peers :as list-peers]
            [clj-i2p.peer-service.peer-client :as peer-client]
            [clj-i2p.peer-service.persister-protocol :as persister-protocol]
            [clj-i2p.client :as client]
            [clj-i2p.core :as clj-i2p-core])
  (:import [java.util Date]))

(def notify-action :notify)

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

(defn peers-downloaded?
  "Returns true if the peers downloaded property is set to true which should be true after all peers known on the network have been downloaded from a peer."
  []
  (persister-protocol/peers-downloaded? (persister-protocol/registered-protocol)))

(defn set-peers-downloaded?
  "Sets the value of the peers downloaded property which should only be set to true when all peers known on the network have been downloaded."
  [value]
  (persister-protocol/set-peers-downloaded? (persister-protocol/registered-protocol) value))

(defn find-by-peer-destination [peer-destination]
  (find-peer { :destination (clj-i2p-core/as-destination-str peer-destination) }))

(defn create-peer-from-destination [peer-destination]
  { :destination (clj-i2p-core/as-destination-str peer-destination)
    :created_at (new Date)
    :updated_at (new Date) })

(defn add-peer-destionation [peer-destination]
  (when peer-destination
    (insert-peer (create-peer-from-destination peer-destination))))

(defn add-peer-destination-if-missing [peer-destination]
  (when (and peer-destination (not (find-by-peer-destination peer-destination)))
    (add-peer-destionation peer-destination)))

(defn add-peer-destinations
  "Adds all of the given peer destinations to the persister if they have not already been added."
  [peer-destinations]
  (doseq [peer-destination peer-destinations]
    (add-peer-destination-if-missing peer-destination)))

(defn add-local-peer-destination [peer-destination]
  (when (and peer-destination (not (find-by-peer-destination peer-destination)))
    (insert-peer (merge (create-peer-from-destination peer-destination)
                   { :notified true :local true }))))

(defn local-destination? [peer-destination]
  (and peer-destination (clj-i2p-core/is-current-destination-base-64? peer-destination)))

(defn valid-non-local-destination? [peer-destination]
  (and peer-destination (not (local-destination? peer-destination))))

(defn destination-for [peer]
  (when peer
    (:destination (find-peer peer))))

(defn remove-peer-destination [peer-destination]
  (when-let [peer (find-by-peer-destination peer-destination)]
    (delete-peer peer)))

(defn update-peer-destination [peer-destination]
  (if-let [peer (find-by-peer-destination peer-destination)]
    (update-peer { :id (:id peer) :updated_at (new Date) :notified true })
    (when-let [peer-id (add-peer-destionation peer-destination)]
      (update-peer { :id peer-id :notified true }))))

(defn notify-call [peer-destination]
  (when peer-destination
    (let [response (peer-client/send-message peer-destination
                                             { :action notify-action :destination (client/base-64-destination) })]
      (= (:data response) "ok"))))

(defn notify-peer-destination [peer-destination]
  (if (notify-call peer-destination)
    (update-peer (merge (find-by-peer-destination peer-destination) { :notified true :updated_at (new Date) }))
    (logging/info (str "Destination " peer-destination " is not online."))))

(defn list-peers [peer-destination]
  (when peer-destination
    (list-peers/call peer-destination)))

(defn destination-online? [destination]
  (when (clj-i2p-core/destination-online? destination)
    destination))

(defn default-destinations
  "Returns a list of all destinations which have not been loaded into the persister yet. This function is called when no
destinations are in the persister which is likely the first time the application is run."
  []
  (let [destinations (shuffle (persister-protocol/default-destinations (persister-protocol/registered-protocol)))]
    (doseq [destination destinations]
      (add-peer-destination-if-missing destination))
    destinations))

(defn random-destinations
  "Returns a list of randomly ordered identities created by adding the last updated peer destination to the default
destinations."
  []
  (filter identity (cons (:destination (last-updated-peer)) (default-destinations))))

(defn find-online-destination
  ([] (find-online-destination 0))
  ([count]
    (if-let [online-destination (some destination-online? (random-destinations))]
      online-destination
      (when (< count 10)
        (logging/warn (str "Could not find a peer to download the latest peer list from. Attempt number: " (inc count)))
        (recur (inc count))))))

(defn notify-all-peers
  "Notifies all unnotified peers of this instance."
  []
  (doseq [peer (all-unnotified-peers)]
    (.start (Thread. #(notify-peer-destination (:destination peer))))))

(defn notified?
  "Returns true if the given peer has been notified."
  [peer]
  (:notified peer))

(defn notify-peer-destination-if-necessary
  "Notifies the peer associated with the given destination if and only if the peer has not already been notified."
  [peer-destination]
  (when-let [peer (find-by-peer-destination peer-destination)]
    (when-not (notified? peer)
      (notify-peer-destination peer-destination))))

(defn load-all-peers-from [peer-destination]
  (when (valid-non-local-destination? peer-destination)
    (when-let [peers (list-peers peer-destination)]
      (logging/info (str "Adding peers from destination: " peer-destination))
      (add-peer-destinations (filter valid-non-local-destination? peers))
      (when (not-empty peers)
        (set-peers-downloaded? true)))))

(defn load-all-peers-from-seq [peer-destinations]
  (doseq [peer-destination peer-destinations]
    (.start (Thread. #(load-all-peers-from peer-destination)))))

(defn load-all-peers
  "Attempts to load all peers on the network."
  []
  (logging/info "loading peers")
  (load-all-peers-from-seq (concat (map :destination (all-notified-peers)) (default-destinations))))

(defn is-online-peer?
  "Returns true if the given peer is not this peer and online."
  [peer]
  (and (not (clj-i2p-core/is-current-destination-base-64? (:destination peer)))
    (destination-online? (:destination peer))))

(defn find-online-notified-peer
  ([] (find-online-notified-peer 0))
  ([count]
    (if-let [online-peer (some #(when (is-online-peer? %1) %1) (shuffle (all-notified-peers)))]
      online-peer
      (when (< count 10)
        (logging/warn (str "Could not find a peer to download the latest peer list from. Attempt number: " (inc count)))
        (recur (inc count))))))

(defn reload-peers []
  (logging/info "Reloading peers")
  (load-all-peers-from-seq (map :destination (all-notified-peers))))

(defn download-peers-background []
  (logging/info "Downloading peers in background.")
  (if (peers-downloaded?)
    (reload-peers)
    (load-all-peers))
  (notify-all-peers))

(defn download-peers []
  (future (download-peers-background)))

(defn send-messages [service data call-back]
  (client/send-messages (map :destination (all-foreign-peers)) service data call-back))