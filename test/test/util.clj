(ns test.util
  (:require [clj-i2p.core :as core]
            [clj-i2p.service-protocol :as service-protocol]
            [clojure.tools.logging :as logging]))

(def test-destination (core/as-destination "gfgHVoVFMYpBJwuL04mRa-~vQBt-p5lYfyVW2JatrzuJBy3Z7DgkxF68zDQe4M9uD-zOoBCBWctioFoUjnzPbDbflacwPvLnNxN-2GB64b73vDNPKkffM1JXLn6cRLWurxTYVeXaZns7ZmVj969XM3tOwEny1JZbMm-24YIaUwb66vkLeM33uanMer~II--OuikXx654ZkMXAORoJSu3hb04Q2s8sMR6-dnABeijfKShzINDg-JZSCRxWIay~VidFF6nhpi-BO3HPHPfGYTPkN5-w08z0IEaTeoBLNBBfVrmXwy2xPQWK1px2IRMpf0J~EiOf300Gin9xEoAhjEeL0LtUshT4bX2J1c~WiMHNGRJfjw4YspNVr8sDLOcVziOshLORlYDwkV6~ZNmovRQdcnwQ9OZzfM16fYib7Xb2wVtk-5TGIJOJiaegazOIb7Ze71N~EGX6epvwU1m2eGZ2I6oe~i2MOekHmnqhvolC1MQTUNGi-temb17xaeomMAdAAAA"))
(def test-service-key :test-service)
(def test-service-version-key "1.0.0")

(deftype TestService []
  service-protocol/Service
  (key [service]
    test-service-key)

  (name [service]
    "Test Service")

  (version [service]
    test-service-version-key)

  (description [service]
    "This is a test service.")

  (handle [service request-map]
    { :test "test" }))

(def test-service (TestService.))

(defn destination-fixture
  "loads a test destination."
  [test]
  (let [old-destination (core/current-destination)]
    (try
      (core/set-destination test-destination)
      (core/notify-destination-listeners)
      (test)
      (finally
        (core/set-destination old-destination)
        (core/notify-destination-listeners)))))

(defn create-mock-network-fixture
  "Creates a fixture which loads the given mock network before running any tests
then unloads the mock network when finished."
  [mock-network]
  (fn [function]
    (try
      (core/set-mock-network mock-network)
      (function)
      (finally
        (core/clear-mock-network)))))