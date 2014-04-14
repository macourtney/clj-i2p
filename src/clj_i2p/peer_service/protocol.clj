(ns clj-i2p.peer-service.protocol
  (:require [clj-i2p.peer-service.actions.list-peers :as list-peers-action]
            [clj-i2p.peer-service.actions.notify :as notify-action]
            [clj-i2p.service-protocol :as service-protocol]))

(def service-name :peer-service)

(deftype PeerService []
  service-protocol/Service
  (key [service] service-name)

  (name [service]
    "Peer Service")

  (version [service]
    "1.0.0")

  (description [service]
    "This is a service which handles peers. Keeping track of them and notifying them of events.")

  (handle [service request-map]
    (condp = (:action request-map)
      notify-action/action-key (notify-action/action request-map)
      list-peers-action/action-key (list-peers-action/action request-map)
      { :error (str "Unknown action: " (:action request-map)) })))

(def peer-service (new PeerService))