;; This file is used to configure the database and connection.

(ns config.db-config
  (:require [clojure.tools.logging :as logging]
            [config.environment :as environment]
            [drift-db-h2.flavor :as h2]
            [drift-db.core :as drift-db]))

(defn dbname [environment]
  (cond
     ;; The name of the production database to use.
     (= environment :production) "clj_i2p_production"

     ;; The name of the development database to use.
     (= environment :development) "clj_i2p_development"

     ;; The name of the test database to use.
     (= environment :test) "clj_i2p_test"))

(defn
#^{:doc "Returns the database flavor which is used by Conjure to connect to the database."}
  create-flavor [environment]
  (logging/info (str "Environment: " environment))
  (h2/h2-flavor

    ;; Calculates the database to use.
    (dbname environment)

    "data/db/"))

(defn
  load-config []
  (when (not (drift-db/initialized?))
    (let [environment (environment/environment-name)
          flavor (create-flavor (keyword environment))]
      (if flavor
        (drift-db/init-flavor flavor)
        (throw (new RuntimeException (str "Unknown environment: " environment ". Please check your " environment/environment-property " system property.")))))))