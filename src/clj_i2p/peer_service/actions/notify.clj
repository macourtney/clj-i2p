(ns clj-i2p.peer-service.actions.notify
  (:require [clj-i2p.peer-service.persister-protocol :as persister-protocol]))

(def action-key :notify)

(defn action [request-map]
  (persister-protocol/update-peer-destination
    (:destination (:data request-map)))
  { :data "ok" })