(ns jbittencourt.zeromq
  (:require [jbittencourt.btc :refer [parse-bytes-tx-addresses]]
            [clojure.pprint :as pprint])
  (:import (org.zeromq ZContext ZMQ)))

(def ^:const SOCKET_ADDRESS "tcp://127.0.0.1:29000")
(def ^:const RAW_TX_TOPIC "rawtx")

(defn on-tx-received [bytes]
  (let [addresses (parse-bytes-tx-addresses (byte-array bytes))]
    (println "[tx-listener-task]: Received transaction with addresses:")
    (pprint/pprint addresses)))

(defn create-subscriber [ctx]
  (let [socket (.createSocket ctx ZMQ/SUB)]
    (.setReceiveTimeOut socket 1000)
    (.subscribe socket (.getBytes RAW_TX_TOPIC "UTF-8"))
    (.connect socket SOCKET_ADDRESS)
    socket))

(def *active (atom true))

(defn stop-tx-listener-task! []
  (reset! *active false))

(defn create-tx-listener-task! []
  (reset! *active true)
  (future
    (println "[tx-listener-task]: started")
    (with-open [context (ZContext.)
                socket (create-subscriber context)]
      (loop []
        (let [message (.recvStr socket)]
          (when (= message "rawtx")
            (on-tx-received (.recv socket))))
        (when @*active (recur)))
      (println "[tx-listener-task]: stopped"))))



(def job (create-tx-listener-task!))

(comment
  (create-tx-listener-task!)
  (stop-tx-listener-task!)
;;
  )
