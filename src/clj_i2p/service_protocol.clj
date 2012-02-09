(ns clj-i2p.service-protocol)

(defprotocol Service
  "This protocol includes all functions necessary to add a service to the clj-i2p server. To add a service, implement
  this protocol and add your implementation to the service list using clj-i2p.service/add-service."

  (key [service] "Returns a key used by clj-i2p.service to add and find this service.")

  (name [service] "Returns a human readable name for this service.")

  (version [service] "Returns the version of the service. This function should return a string.")

  (description [service] "Returns a human readable description for this service.")

  (handle [service request-map] "This function handles a request. The given request map is a map of all parameters passed
  during the request. This function should return a response-map. The response map my include any information you want,
  but must not be nil."))