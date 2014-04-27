(ns clj-i2p.core
  (:require [clojure.data.json :as json]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as logging]
            [clojure.java.io :as java-io]
            [clojure.java.javadoc :as javadoc]
            [clojure.string :as clj-string])
  (:import [java.io BufferedInputStream BufferedReader BufferedWriter ByteArrayInputStream File FileInputStream InputStreamReader OutputStreamWriter PrintWriter]
           [net.i2p.client.streaming I2PSocketManagerFactory]
           [net.i2p.data Destination PrivateKey PrivateKeyFile]
           [net.i2p.util I2PThread]))

(def data-key :data)
(def destination-key :destination)
(def from-key :from)
(def service-key :service)
(def service-version-key :service-version)

(def manager (atom nil))

(def mock-network (atom nil))

(def destination (atom nil))

(def destination-listeners (atom []))

(def send-message-fail-listeners (atom []))

(def private-key-file-name (atom "private_key.dat"))

(def private-key-directory (atom (File. "data")))

(def timeout 600000)

(defn add-destination-listener [listener]
  (swap! destination-listeners conj listener))

(defn add-send-message-fail-listener [listener]
  (swap! send-message-fail-listeners conj listener))

(defn set-destination [destination-obj]
  (swap! destination (fn [_] destination-obj)))

(defn current-destination []
  @destination)

(defn base-64-destination []
  (when-let [destination (current-destination)]
    (.toBase64 destination)))

(defn is-current-destination? [destination]
  (= destination (current-destination)))

(defn is-current-destination-base-64? [destination]
  (= destination (base-64-destination)))

(defn set-private-key-file-name [new-private-key-file-name]
  (reset! private-key-file-name new-private-key-file-name))

(defn set-private-key-file-directory [new-private-key-directory]
  (reset! private-key-directory new-private-key-directory))

(defn private-key-file []
  (File. @private-key-directory @private-key-file-name))
  
(defn private-key-file-exists? []
  (.exists (private-key-file)))

(defn load-private-key []
  (let [private-key (new PrivateKey)]
    (with-open [key-file-input-stream (FileInputStream. (private-key-file))
                key-buffer-input-stream (BufferedInputStream. key-file-input-stream)]
      (.readBytes private-key key-buffer-input-stream))
    private-key))

(defn save-private-key []
  (when (not (private-key-file-exists?))
    (logging/debug "Creating and saving the private key.")
    (PrivateKeyFile/main (into-array String (list (.getPath (private-key-file)))))))

(defn create-new-manager []
  (when (private-key-file-exists?)
    (I2PSocketManagerFactory/createManager (java-io/input-stream (private-key-file)))))

(defn create-manager []
  (save-private-key)
  (create-new-manager))

(defn load-manager []
  (if-let [old-manager @manager]
    old-manager
    (let [new-manager (create-manager)]
      (reset! manager new-manager)
      new-manager)))

(defn get-server-socket [manager]
  (try
    (.getServerSocket manager)
    (catch NullPointerException e
      (throw (RuntimeException. "Could not connect to i2p router. Please make sure the i2p router is running." e)))))

(defn read-socket [socket]
  (let [reader (java-io/reader (.getInputStream socket))]
    (clj-string/join "\n" (take-while identity (repeatedly #(.readLine reader))))))

(defn write-socket [socket data]
  (let [writer (java-io/writer (.getOutputStream socket))]
    (.write writer data)
    (.flush writer)))

(defn read-json [socket]
  (try
    (json/read-json (java-io/reader (.getInputStream socket)))
    (catch Exception e
      ; Ignore errors
      nil)))

(defn write-json [socket json-data]
  (let [socket-writer (PrintWriter. (.getOutputStream socket))]
    (json/write-json json-data socket-writer true)
    (.flush socket-writer)))

(defn start-client-handler [client-handler]
  (let [server-socket (get-server-socket @manager)
        i2p-thread (I2PThread. #(client-handler server-socket))]
    (.setName i2p-thread "clienthandler1")
    (.setDaemon i2p-thread false)
    (.start i2p-thread)))

(defn notify-destination-listeners []
  (doseq [destination-listener @destination-listeners]
    (destination-listener (current-destination))))

(defn load-manager-and-destination []
  (let [new-manager (load-manager)
        session (.getSession new-manager)]
    (set-destination (.getMyDestination session))
    (notify-destination-listeners)))

(defn start-server [client-handler]
  (load-manager-and-destination)
  (start-client-handler client-handler))

(defn init [client-handler]
  (.start (Thread. #(start-server client-handler))))

(defn as-destination
  "Tries to convert the given destination to a i2p Destination object."
  [destination]
  (if (or (nil? destination) (instance? Destination destination))
    destination
    (Destination. (str destination))))

(defn as-destination-str
  "Converts the given i2p destination object into a string."
  [destination]
  (if (instance? Destination destination)
    (.toBase64 destination)
    destination))

(defn notify-send-message-fail
  "Called when send-message fails. Notifies any send-message-fail-listeners that
a message has failed."
  [destination data]
  (logging/debug "Failed to send message to destination: "
                 (as-destination-str destination))
  (let [destination-obj (as-destination destination)]
    (doseq [send-message-fail-listener @send-message-fail-listeners]
      (send-message-fail-listener destination data))))

(defn destination-online?
  "Returns true if a manager is installed and the given destination is online
and pingable."
  [destination]
  (and @manager destination
       (.ping @manager (as-destination destination) timeout)))

(defn set-mock-network
  "Sets a mock network. A mock network is a function which takes a destination
and data and returns a response. A mock network is useful when testing and you
don't want to connect to the real i2p network."
  [mock-network-function]
  (reset! mock-network mock-network-function))

(defn clear-mock-network
  "Removes the mock network."
  []
  (reset! mock-network nil))

(defn get-mock-network
  "Returns the mock network currently installed."
  []
  @mock-network)

(defn send-mock-network-message
  "Sends the given data to the given destination on the mock network."
  [destination data]
  (@mock-network destination data))

(defn send-message
  "Sends the given data to the given destination. If a mock network is
installed, then the data is sent to the mock network instead. Before the data is
sent, the destination is pinged. If the ping fails, then the
notify-send-message-fail function is called."
  [destination data]
  (if (and @mock-network destination)
    (send-mock-network-message destination data)
    (let [destination-obj (as-destination destination)]
      (when @manager
        (logging/debug "Sending message to destination: "
                       (as-destination-str destination))
        (if (.ping @manager destination-obj timeout)
          (let [socket (.connect @manager destination-obj)]
            (logging/debug "Ping succeeded. Writing to socket.")
            (write-json socket data)
            (let [response (read-json socket)]
              (.close socket)
              response))
          (notify-send-message-fail destination-obj data))))))