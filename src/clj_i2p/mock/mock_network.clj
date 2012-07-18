(ns clj-i2p.mock.mock-network
  (:require [clj-i2p.core :as clj-i2p-core]))

(def mock-destinations (atom {}))

(defn add-mock-destination [destination mock-destination]
  (reset! mock-destinations (assoc @mock-destinations (clj-i2p-core/as-destination-str destination) mock-destination)))

(defn reset-mock-destinations []
  (reset! mock-destinations {}))

(defn mock-network [destination data]
  (if-let [mock-destination (get @mock-destinations (clj-i2p-core/as-destination-str destination))]
    (mock-destination data)
    (throw (RuntimeException. (str "Could not find a mock destination for " destination)))))

(defn init []
  (clj-i2p-core/set-mock-network mock-network))