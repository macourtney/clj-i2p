(ns clj-i2p.peer-service.destination-listener
  (:require [clj-i2p.peer-service.peer :as peer-service]
            [clj-i2p.peer-service.persister-protocol :as persister-protocol]))

(defn destination-listener [destination]
  (persister-protocol/add-local-peer-destination destination)
  (peer-service/download-peers))