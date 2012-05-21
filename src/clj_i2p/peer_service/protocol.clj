(ns clj-i2p.peer-service.protocol
  (:require [clj-i2p.peer-service.list-peers :as list-peers]
            [clj-i2p.peer-service.notify :as notify]
            [clj-i2p.peer-service.peer :as peer-service]
            [clj-i2p.peer-service.peer-client :as peer-client]
            [clj-i2p.service-protocol :as service-protocol]))

(deftype ListService []
  service-protocol/Service
  (key [service]
    peer-client/service-name)

  (name [service]
    "Peer Service")

  (version [service]
    "1.0.0")

  (description [service]
    "This is a service which handles peers. Keeping track of them and notifying them of events.")

  (handle [service request-map]
    (condp = (:action request-map)
      peer-service/notify-action (notify/action request-map)
      list-peers/list-peers-action (list-peers/action request-map)
      { :error (str "Unknown action: " (:action request-map)) })))

(defn create-list-service []
  (ListService.))