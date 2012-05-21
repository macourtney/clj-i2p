(ns clj-i2p.server
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.core :as core]
            [clj-i2p.list-service.protocol :as list-service-protocol]
            [clj-i2p.peer-service.protocol :as peer-service-protocol]
            [clj-i2p.server-interceptors :as server-interceptors]
            [clj-i2p.service :as service]
            [clj-i2p.service-protocol :as service-protocol]))

(defn service-key
  "Returns the service key from the given request-map."
  [request-map]
  (when-let [service-key (:service request-map)]
    (keyword service-key)))

(defn service-version
  "Returns the service version from the given request-map."
  [request-map]
  (when-let [service-version (:service-version request-map)]
    (keyword service-version)))

(defn find-service
  "Gets the service for the request-map. The service is fetched using service-key and service-version."
  [request-map]
  (when-let [service-key (service-key request-map)]
    (when-let [service-version (service-version request-map)]
      (service/find-service service-key service-version))))

(defn service-not-found
  "Calls service/service-not-found for the service referenced in request-map."
  [request-map]
  (service/service-not-found (service-key request-map) (service-version request-map)))

(defn update-response-map
  "Adds the service and service version from the given service to the response-map."
  [service response-map]
  (merge (service-protocol/handle service response-map)
    { :service (keyword (service-protocol/key service))
      :service-version (keyword (service-protocol/version service)) }))

(defn run-service
  "Runs the service requested by the request-map, and passes the request-map to the service."
  [request-map]
  (if-let [service (find-service request-map)]
    (update-response-map service (service-protocol/handle service request-map))
    (service-not-found request-map )))

(defn build-response
  "Reads a request map from the given socket and runs the appropriate service."
  [socket]
  (server-interceptors/run-interceptors run-service (core/read-json socket)))

(defn perform-service
  "Reads a request map from the given socket, runs the appropriate service and writes the response back to the socket."
  [socket]
  (core/write-json socket (build-response socket)))

(defn client-handler
  "Handles a server socket connection. After accepting a connection, this function passes the socket to perform-service."
  [server-socket]
  (while true
    (try
      (when-let [socket (.accept server-socket)]
        (perform-service socket))
      (catch Throwable t
        (logging/error "An error occured while handling a connection." t)))))

(defn init
  "Initializes the server by loading the client-handler into the i2p server handler."
  []
  (logging/info "Initializing server.")
  (service/add-service (list-service-protocol/create-list-service))
  (service/add-service (peer-service-protocol/create-list-service))
  (core/init client-handler))