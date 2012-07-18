(ns clj-i2p.peer-service.persister-protocol)

(def instance (atom nil))

(def peer-keys [:id :destination :created_at :updated_at :notified :local])

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