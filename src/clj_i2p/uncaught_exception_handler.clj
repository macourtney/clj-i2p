(ns clj-i2p.uncaught-exception-handler
  (:require [clojure.tools.logging :as logging]))

(defn init []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException [this thread throwable]
        (logging/error "Uncaught Exception:" throwable)))))