(ns clj-i2p.peer-service.destination-listener
  (:require [clj-i2p.peer-service.peer :as peer-service]))

(defn destination-listener [destination]
  (peer-service/add-local-peer-destination destination)
  (peer-service/download-peers))