(ns clj-i2p.peer-service.list-peers
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.client :as client]
            [clj-i2p.core :as clj-i2p-core]
            [clj-i2p.peer-service.peer-client :as peer-client]
            [clj-i2p.peer-service.persister-protocol :as persister-protocol]))

(def list-peers-action :list-peers)

(defn call [destination]
  (when destination
    (when-let [response (peer-client/send-message destination { :action list-peers-action :type :all })]
      (:data response))))

(defn action [request-map]
  { :data (map :destination (persister-protocol/all-peers (persister-protocol/registered-protocol))) })