(ns clj-i2p.server
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.core :as core]
            [clj-i2p.server-interceptors :as server-interceptors]
            [clj-i2p.service :as service]
            [clj-i2p.service-protocol :as service-protocol]))

(defn get-service-key [request-map]
  (when-let [service-key (:service request-map)]
    (keyword service-key)))

(defn run-service [request-map]
  (let [service-key (get-service-key request-map)]
    (if-let [service (service/find-service service-key)]
      (assoc (service-protocol/handle service request-map) :service service-key)
      (service/service-not-found service-key))))

(defn build-response [socket]
  (server-interceptors/run-interceptors run-service (core/read-json socket)))

(defn perform-service [socket]
  (core/write-json socket (build-response socket)))

(defn client-handler [server-socket]
  (while true
    (try
      (when-let [socket (.accept server-socket)]
        (perform-service socket))
      (catch Throwable t
        (logging/error "An error occured while handling a connection." t)))))

(defn init []
  (logging/info "Initializing server.")
  (core/init client-handler))