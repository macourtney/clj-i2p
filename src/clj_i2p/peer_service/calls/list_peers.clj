(ns clj-i2p.peer-service.calls.list-peers
  (:require [clj-i2p.peer-service.actions.list-peers :as list-peers-action]
            [clj-i2p.peer-service.peer-client :as peer-client]))

(defn call [destination]
  (when destination
    (when-let [response (peer-client/send-message destination
                          { :action list-peers-action/action-key :type :all })]
      (:data response))))