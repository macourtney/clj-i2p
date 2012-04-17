(ns clj-i2p.database.util
  (:require [config.db-config :as db-config]
            [drift-db.core :as drift-db]
            [drift-db.protocol :as flavor-protocol]))

(def db (atom {}))

(defn init-database []
  (db-config/load-config)
  (swap! db (flavor-protocol/db-map (deref drift-db/drift-db-flavor))))