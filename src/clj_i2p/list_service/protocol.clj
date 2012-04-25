(ns clj-i2p.list-service.protocol
  (:require [clj-i2p.list-service.core :as list-service]
            [clj-i2p.service-protocol :as service-protocol]))

(deftype ListService []
  service-protocol/Service
  (key [service]
     :list-service)

  (name [service]
    "List Service")

  (version [service]
    "1.0.0")

  (description [service]
    "This is a service which lists and handles info about peers.")

  (handle [service request-map]
    (condp = (:action request-map)
      :list (list-service/list-services)
      { :error (str "Unknown action: " (:action request-map)) })))

(defn create-list-service []
  (ListService.))