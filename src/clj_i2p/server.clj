(ns clj-i2p.server
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.core :as core]
            [clj-i2p.server-interceptors :as server-interceptors]))

(def actions (atom {}))

(defn add-action [action-key action-fn]
  (swap! actions assoc action-key action-fn))

(defn action-map []
  @actions)

(defn reset-actions! []
  (reset! actions {}))

(defn find-action [action-key]
  (get (action-map) action-key))

(defn get-action-key [request-map]
  (when-let [action-key (:action request-map)]
    (keyword action-key)))

(defn action-not-found [action-key]
  (logging/error (str "Action not found: " action-key))
  { :data nil :type :action-not-found :action action-key })

(defn run-action [request-map]
  (let [action-key (get-action-key request-map)]
    (if-let [action-fn (find-action action-key)]
      (assoc (action-fn request-map) :action action-key)
      (action-not-found action-key))))

(defn build-response [socket]
  (server-interceptors/run-interceptors run-action (core/read-json socket)))

(defn perform-action [socket]
  (core/write-json socket (build-response socket)))

(defn client-handler [server-socket]
  (while true
    (try
      (when-let [socket (.accept server-socket)]
        (perform-action socket))
      (catch Throwable t
        (logging/error "An error occured while handling a connection." t)))))

(defn init []
  (logging/info "Initializing server.")
  (core/init client-handler))