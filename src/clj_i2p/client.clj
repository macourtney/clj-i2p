(ns clj-i2p.client
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.core :as core]
            [clj-i2p.client-interceptors :as client-interceptors]
            [clj-i2p.service-protocol :as service-protocol]))

(defn current-destination []
  (core/current-destination))

(defn base-64-destination []
  (core/base-64-destination))

(defn request-map-from []
  { core/destination-key (base-64-destination) })

(defn update-request-map
  "Creates a request map for the given destination, calling the given service
with the given data."
  [destination service request-map]
  (merge request-map
    { core/destination-key (core/as-destination destination)
      core/service-key (service-protocol/key service)
      core/service-version-key (service-protocol/version service)
      core/from-key (request-map-from) }))

(defn- send-request [request-map]
  (try
    (core/send-message (core/destination-key request-map)
                       (dissoc request-map core/destination-key))
    (catch java.net.NoRouteToHostException e
      (logging/warn (str "Could not connect to destination: "
                         (core/destination-key request-map)))
      nil)))

(defn send-message [destination service data]
  (client-interceptors/run-interceptors send-request
    (update-request-map destination service data)))

(defn send-messages [destinations service data call-back]
  (doseq [destination destinations]
    (future (call-back (send-message destination service data)))))