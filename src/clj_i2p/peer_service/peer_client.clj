(ns clj-i2p.peer-service.peer-client
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.client :as client]
            [clj-i2p.peer-service.protocol :as peer-service-protocol]))

(defn send-message [destination data]
  (try
    (client/send-message destination peer-service-protocol/peer-service data)
    (catch Exception error
      (logging/error error
                     "An error occured while sending a peer service message.")
      nil)))