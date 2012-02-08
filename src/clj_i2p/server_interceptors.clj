(ns clj-i2p.server-interceptors
  (:require [clj-i2p.interceptor-util :as interceptor-util]))

(def interceptors (atom []))

(defn add-interceptor [interceptor]
  (swap! interceptors #(cons interceptor %)))

(defn run-interceptors [function request-map]
  (interceptor-util/run-interceptors @interceptors function request-map))