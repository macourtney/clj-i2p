(ns clj-i2p.peer-service.peer-client
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.client :as client]))

(def service-name :peer-service)

(defn send-message [destination data]
  (try
    (client/send-message destination service-name data)
    (catch Exception e
      (logging/error (str "e: " e) e)
      nil)))