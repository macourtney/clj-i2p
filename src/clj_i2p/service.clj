(ns clj-i2p.service
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.service-protocol :as service-protocol]))

(def services (atom {}))

(defn service-map []
  @services)

(defn service-version-map [service]
  (when service
    (get (service-map) (service-protocol/key service) {})))

(defn update-service-version [service]
  (when service
    (assoc (service-version-map service) (keyword (service-protocol/version service)) service)))

(defn add-service [service]
  (swap! services assoc (keyword (service-protocol/key service)) (update-service-version service)))

(defn reset-services! []
  (reset! services {}))

(defn find-service [service-key service-version]
  (get (get (service-map) (keyword service-key)) (keyword service-version)))

(defn service-not-found [service-key service-version]
  (logging/error (str "Service not found: " service-key ", version " service-version))
  { :data nil :type :service-not-found :service service-key :service-version service-version })