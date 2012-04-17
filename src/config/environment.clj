(ns config.environment
  (:require [clj-i2p.uncaught-exception-handler :as uncaught-exception-handler]
            [clojure.tools.logging :as logging]
            [config.environment :as config-env]
            ))

(def initialized? (atom false))

(def environment-property "clj_i2p.environment")
(def default-environment "production")

(defn
  set-evironment-property [environment]
  (System/setProperty environment-property environment)) 

(defn
  require-environment []
  (when (not (System/getProperty environment-property))
    (set-evironment-property default-environment))
  (let [mode (System/getProperty environment-property)]
    (require (symbol (str "config.environments." mode)))))

(defn
#^{ :doc "Returns the name of the environment." }
  environment-name []
  (System/getProperty environment-property))

(defn environment-init []
  (when (compare-and-set! initialized? false true)
    (uncaught-exception-handler/init)
    (require-environment)))