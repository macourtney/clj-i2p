(ns clj-i2p.peer-service.calls.notify
  (:require [clj-i2p.client :as client]
            [clj-i2p.peer-service.actions.notify :as notify-action]
            [clj-i2p.peer-service.peer-client :as peer-client]))

(defn call [destination]
  (when destination
    (let [response (peer-client/send-message destination
                     { :action notify-action/action-key
                       :destination (client/base-64-destination) })]
      (= (:data response) "ok"))))