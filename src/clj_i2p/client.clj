(ns clj-i2p.client
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.core :as core]
            [clj-i2p.client-interceptors :as client-interceptors]))

(defn current-destination []
  (core/current-destination))

(defn base-64-destination []
  (core/base-64-destination))

(defn request-map-from []
  { :destination (base-64-destination) })

(defn create-request-map [destination service data]
  { :destination destination :service service :data data :from (request-map-from) })

(defn- send-request [request-map]
  (try
    (core/send-message (:destination request-map) (dissoc request-map :destination))
    (catch java.net.NoRouteToHostException e
      (logging/warn (str "Could not connect to destination: " (:destination request-map)))
      nil)))

(defn send-message [destination service data]
  (client-interceptors/run-interceptors send-request (create-request-map destination service data)))

(defn send-messages [destinations service data call-back]
  (doall (map #(future (call-back (send-message % service data))) destinations)))