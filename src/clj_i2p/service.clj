(ns clj-i2p.service
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.service-protocol :as service-protocol]))

(def services (atom {}))

(defn add-service [service]
  (swap! services assoc (service-protocol/key service) service))

(defn service-map []
  @services)

(defn reset-services! []
  (reset! services {}))

(defn find-service [service-key]
  (get (service-map) service-key))

(defn service-not-found [service-key]
  (logging/error (str "Service not found: " service-key))
  { :data nil :type :service-not-found :service service-key })