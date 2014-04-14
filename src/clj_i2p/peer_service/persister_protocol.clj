(ns clj-i2p.peer-service.persister-protocol
  (:require [clj-i2p.core :as clj-i2p-core]
            [clojure.tools.logging :as logging])
  (:import [java.util Date]))

(def instance (atom nil))

(def peer-keys [:id :destination :created-at :updated-at :notified :local])

(defprotocol PeerPersister
  "This protocol describes an interface for all list service functions which must persist data."

  (insert-peer [persister peer] "Inserts the given peer into the persistence layer.")

  (update-peer [persister peer] "Updates the peer with the same id as the given peer with the data from the given peer.")

  (delete-peer [persister peer] "Deletes the given peer from the system.")

  (all-peers [persister] "Returns all peers in the system.")

  (all-foreign-peers [persister] "Returns all peers in the system which are created by this instance of clj-i2p.")

  (find-peer [persister peer] "Finds a peer record using the given peer as a prototype.")

  (find-all-peers [persister peer] "Finds all peers like the given peer.")

  (last-updated-peer [persister] "Returns the last updated peer.")

  (all-unnotified-peers [persister] "Returns all peers which have not been notified yet.")

  (all-notified-peers [persister] "Returns all peers which have been notified.")

  (add-peer-update-listener [persister listener] "Registers a listener which will be notified when a peer is inserted or updated in the system.")

  (remove-peer-update-listener [persister listener] "Unregisters an update listener.")

  (add-peer-delete-listener [persister listener] "Registers a listener which will be notified when a peer is deleted from the system.")

  (remove-peer-delete-listener [persister listener] "Unregisters a delete listener.")

  (default-destinations [persister] "Returns a list of all destinations which have not been loaded into the persister
yet. This function is called when no destinations are in the persister which is likely the first time the application is
run.")

  (peers-downloaded? [persister] "Returns true if the peers downloaded property is set to true which should be true after all peers known on the network have been downloaded from a peer.")

  (set-peers-downloaded? [persister value] "Sets the value of the peers downloaded property which should only be set to true when all peers known on the network have been downloaded."))

(defn register [peer-persister]
  (swap! instance (fn [_] peer-persister)))

(defn protocol-registered? []
  (if @instance
    true
    false))

(defn registered-protocol []
  (if-let [peer-persister @instance]
    peer-persister
    (throw (new RuntimeException "No protocol registered."))))

(defn find-registered-peer
  "Returns the first peer using the given peer as a prototype."
  [peer]
  (find-peer (registered-protocol) peer))

(defn insert-registered-peer
  "Saves the given peer."
  [peer]
  (insert-peer (registered-protocol) peer))

(defn update-registered-peer
  "Updates a peer using the given peer map as a prototype."
  [peer]
  (update-peer (registered-protocol) peer))

(defn delete-registered-peer
  "Deletes the given peer."
  [peer]
  (delete-peer (registered-protocol) peer))

(defn find-all-registered-peers
  "Finds all peers like the given peer."
  [peer]
  (find-all-peers (registered-protocol) peer))

(defn registered-peers-downloaded?
  "Returns true if the peers downloaded property is set to true which should be
true after all peers known on the network have been downloaded from a peer."
  []
  (peers-downloaded? (registered-protocol)))

(defn all-registered-peers
  "Returns all known peers."
  []
  (all-peers (registered-protocol)))

(defn all-foreign-registered-peers
  "Returns all foreign peers."
  []
  (all-foreign-peers (registered-protocol)))

(defn all-notified-registered-peers
  "Returns all peers which have been notified."
  []
  (all-notified-peers (registered-protocol)))

(defn all-unnotified-registered-peers
  "Returns all peers which have not been notified yet."
  []
  (all-unnotified-peers (registered-protocol)))

(defn set-registered-peers-downloaded?
  "Sets the value of the peers downloaded property which should only be set to
true when all peers known on the network have been downloaded."
  [value]
  (set-peers-downloaded? (registered-protocol) value))

(defn last-updated-registered-peer
  "Returns the last updated peer."
  []
  (last-updated-peer (registered-protocol)))

(defn add-registered-peer-update-listener
  "Registers a listener which will be notified when a peer is inserted or
updated in the system."
  [listener]
  (add-peer-update-listener (registered-protocol) listener))

(defn remove-registered-peer-update-listener
  "Registers a listener which will be notified when a peer is inserted or
updated in the system."
  [listener]
  (remove-peer-update-listener (registered-protocol) listener))

(defn add-registered-peer-delete-listener
  "Registers a listener which will be notified when a peer is deleted from the
system."
  [listener]
  (add-peer-delete-listener (registered-protocol) listener))

(defn remove-registered-peer-delete-listener
  "Registers a listener which will be notified when a peer is deleted from the
system."
  [listener]
  (remove-peer-delete-listener (registered-protocol) listener))

(defn destination-for
  "Returns the destination for the given peer."
  [peer]
  (when peer
    (:destination (find-registered-peer peer))))

(defn find-by-peer-destination
  "Given the peer's destination, attempts to find the peer."
  [peer-destination]
  (find-registered-peer
    { :destination (clj-i2p-core/as-destination-str peer-destination) }))

(defn create-peer-from-destination [peer-destination]
  { :destination (clj-i2p-core/as-destination-str peer-destination)
    :created-at (new Date)
    :updated-at (new Date) })

(defn add-peer-destination
  "Adds a new peer using the given destination."
  [peer-destination]
  (when peer-destination
    (insert-registered-peer (create-peer-from-destination peer-destination))))

(defn add-peer-destination-if-missing
  "If the given peer destination is missing, then it is added to the persister."
  [peer-destination]
  (when (and peer-destination (not (find-by-peer-destination peer-destination)))
    (add-peer-destination peer-destination)))

(defn add-peer-destinations
  "Adds all of the given peer destinations to the persister if they have not already been added."
  [peer-destinations]
  (doseq [peer-destination peer-destinations]
    (add-peer-destination-if-missing peer-destination)))

(defn add-local-peer-destination
  "Adds the given destination as a local destination."
  [peer-destination]
  (when (and peer-destination (not (find-by-peer-destination peer-destination)))
    (insert-registered-peer
      (merge (create-peer-from-destination peer-destination)
             { :notified true :local true }))))

(defn update-peer-destination
  "Updates the given peer destination, setting notified to true and updated-at
to the current time. If the peer does not exist, then it is created."
  [peer-destination]
  (if-let [peer (find-by-peer-destination peer-destination)]
    (update-peer (registered-protocol)
      { :id (:id peer) :updated-at (new Date) :notified true })
    (when-let [peer-id (add-peer-destination peer-destination)]
      (update-peer (registered-protocol) { :id peer-id :notified true }))))

(defn remove-peer-destination
  "Removes the peer with the given destination."
  [peer-destination]
  (when-let [peer (find-by-peer-destination peer-destination)]
    (delete-peer (registered-protocol) peer)))

(defn default-registered-destinations
  "Returns a list of all destinations which have not been loaded into the
persister yet. This function is called when no destinations are in the persister
which is likely the first time the application is run."
  []
  (let [destinations (shuffle (default-destinations (registered-protocol)))]
    (doseq [destination destinations]
      (add-peer-destination-if-missing destination))
    destinations))

(defn random-destinations
  "Returns a list of randomly ordered identities created by adding the last
updated peer destination to the default destinations."
  []
  (filter identity
          (cons (:destination (last-updated-peer (registered-protocol)))
                (default-registered-destinations))))

(defn destination-online?
  "Returns the given destination if it is online."
  [destination]
  (when (clj-i2p-core/destination-online? destination)
    destination))

(defn find-online-destination
  "Finds a random online destination."
  ([] (find-online-destination 0))
  ([count]
    (if-let [online-destination (some destination-online? (random-destinations))]
      online-destination
      (when (< count 10)
        (logging/warn (str "Could not find a peer to download the latest peer list from. Attempt number: " (inc count)))
        (recur (inc count))))))

(defn is-online-peer?
  "Returns true if the given peer is not this peer and online."
  [peer]
  (when (and (not (clj-i2p-core/is-current-destination-base-64? (:destination peer)))
             (destination-online? (:destination peer)))
    peer))

(defn find-online-notified-peer
  "Returns a random online notified peer."
  ([] (find-online-notified-peer 0))
  ([count]
    (if-let [online-peer (some is-online-peer?
                               (shuffle (all-notified-registered-peers)))]
      online-peer
      (when (< count 10)
        (logging/warn (str "Could not find a peer to download the latest peer list from. Attempt number: " (inc count)))
        (recur (inc count))))))