(ns clj-i2p.peer-service.actions.list-peers
  (:require [clj-i2p.peer-service.persister-protocol :as persister-protocol]))

(def action-key :list-peers)

(defn action [request-map]
  { :data (map :destination (persister-protocol/all-peers (persister-protocol/registered-protocol))) })