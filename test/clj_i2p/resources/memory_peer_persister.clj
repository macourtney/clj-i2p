(ns clj-i2p.resources.memory-peer-persister
  (:require [clj-i2p.peer-service.persister-protocol :as persister-protocol]))

(def peer-map-instance (atom {}))

(def peer-update-listeners (atom #{}))
(def peer-delete-listeners (atom #{}))

(defn peer-map []
  @peer-map-instance)

(defn update-peer-map [update-fn]
  (swap! peer-map-instance update-fn))

(defn reset-peer-map [new-peer-map]
  (update-peer-map (fn [_] new-peer-map)))

(defn notify-update-listeners [peer]
  (doseq [listener @peer-update-listeners]
    (listener peer)))

(defn notify-delete-listeners [peer]
  (doseq [listener @peer-delete-listeners]
    (listener peer)))

(defn next-id []
  (inc (or (apply max (keys (peer-map))) 0)))

(deftype MemoryPeerPersister []
  persister-protocol/PeerPersister
  (insert-peer [persister peer]
    (if-let [id (:id peer)]
      (do
        (update-peer-map #(assoc % id peer))
        (notify-update-listeners (get (peer-map) id))
        id)
      (let [id (next-id)
            new-peer (assoc peer :id id)]
        (update-peer-map #(assoc % id new-peer))
        (notify-update-listeners (get (peer-map) id))
        id)))

  (update-peer [persister peer]
    (if-let [id (:id peer)]
      (do
        (update-peer-map #(assoc %1 id (merge (get %1 id) peer)))
        (notify-update-listeners (get (peer-map) id)))
      (throw (new RuntimeException (str "Cannot update a peer without an id: " peer)))))

  (delete-peer [persister peer]
    (if-let [id (:id peer)]
      (let [persisted-peer (get (peer-map) id)]
        (update-peer-map #(dissoc %1 id))
        (notify-delete-listeners persisted-peer))
      (throw (new RuntimeException (str "Cannot delete a peer without an id: " peer)))))

  (all-peers [persister]
    (vals (peer-map)))

  (all-foreign-peers [persister]
    (filter #(not (:local %)) (persister-protocol/all-peers persister)))

  (find-peer [persister peer]
    (first (persister-protocol/find-all-peers persister peer)))

  (find-all-peers [persister peer]
    (filter
      (fn [persisted-peer]
        (let [cleaned-peer (select-keys peer persister-protocol/peer-keys)]
          (reduce #(and %1 %2) (map #(= (get cleaned-peer %1) (get persisted-peer %1)) (keys cleaned-peer)))))
      (persister-protocol/all-peers persister)))

  (last-updated-peer [persister]
    (reduce
      (fn [last-peer test-peer]
        (if (> 0 (compare (:updated_at last-peer) (:updated_at test-peer)))
          test-peer
          last-peer))
      (persister-protocol/all-peers persister)))

  (all-unnotified-peers [persister]
    (filter #(not (:notified %)) (persister-protocol/all-peers persister)))

  (all-notified-peers [persister]
    (filter :notified (persister-protocol/all-peers persister)))

  (add-peer-update-listener [persister listener]
    (swap! peer-update-listeners conj listener))

  (remove-peer-update-listener [persister listener]
    (swap! peer-update-listeners disj listener))

  (add-peer-delete-listener [persister listener]
    (swap! peer-delete-listeners conj listener))

  (remove-peer-delete-listener [persister listener]
    (swap! peer-delete-listeners disj listener))
  
  (default-destinations [persister]
    ["default-destination"]))

(defn create-memory-peer-persister []
  (MemoryPeerPersister.))

(defn register-memory-peer-persister []
  (persister-protocol/register (create-memory-peer-persister)))