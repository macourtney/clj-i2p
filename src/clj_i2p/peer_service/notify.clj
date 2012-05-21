(ns clj-i2p.peer-service.notify
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.client :as client]
            [clj-i2p.core :as clj-i2p-core]
            [clj-i2p.peer-service.peer :as peer-service]
            [clj-i2p.peer-service.peer-client :as peer-client]))

(defn call [destination]
  (peer-service/notify-call destination))

(defn action [request-map]
  (peer-service/update-peer-destination (:destination (:data request-map)))
  { :data "ok" })