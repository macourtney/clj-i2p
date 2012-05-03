(ns clj-i2p.peer-service.notify
  (:require [clojure.tools.logging :as logging]
            [clj-i2p.client :as client]
            [clj-i2p.core :as clj-i2p-core]
            [clj-i2p.peer-service.peer-client :as peer-client]))

(def notify-action :notify)

(defn call [destination]
  (let [response (peer-client/send-message destination
                   { :action notify-action :destination (client/base-64-destination) })]
      (= (:data response) "ok")))