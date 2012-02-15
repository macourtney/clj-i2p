(ns clj-i2p.list-service.core
  (:require [clj-i2p.service :as service]
            [clj-i2p.service-protocol :as service-protocol]))

(defn service-description-map [service]
  { :name (service-protocol/name service)
    :description (service-protocol/description service) })

(defn list-service-versions [service-version-map]
  (reduce
    (fn [output [service-version service]]
      (assoc output service-version (service-description-map service)))
    {}
    service-version-map))

(defn list-services []
  (reduce
    (fn [output [service-key service-version-map]]
      (assoc output service-key (list-service-versions service-version-map)))
    {}
    (service/service-map)))