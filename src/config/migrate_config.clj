(ns config.migrate-config
  (:require [darkexchange.core :as core]
            [drift.builder :as builder]
            [drift-db.migrate :as drift-db-migrate]))

(defn migrate-config []
   { :directory "/src/clj_i2p/database/migrations"
     :init core/init-args
     :ns-content "\n  (:use drift-db.core)"
     :current-version drift-db-migrate/current-version
     :update-version drift-db-migrate/update-version })