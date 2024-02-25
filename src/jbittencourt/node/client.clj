(ns jbittencourt.node.client
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [jbittencourt.btcwall :refer [get-wallet-addresses]]
            [jbittencourt.db :as db]
            [jbittencourt.utils :refer [date-time-str->timestamp]])
  (:import java.util.Base64))

(defn encode [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(def config
  {:username "btcwall"
   :password "btcwall"
   :uri "http://127.0.0.1:18332/"})

(defn uuid []
  (str (java.util.UUID/randomUUID)))

(defn basic-token [username password]
  (str "Basic " (encode (str username ":" password))))

(defn url
  ([] (:uri config))
  ([path] (str (:uri config) path)))

(defn make-request [url id method params]
  (let [user (:username config)
        password (:password config)]
    (client/post
     url
     {:headers {:Authorization (basic-token user password)}
      :content-type :json
      :form-params {:jsonrpc "1.0"
                    :id  id
                    :method method
                    :params params}})))

(defn create-wallet [wallet-name]
  ;; creates a wallet on the node
  (-> (make-request
       (url)
       (uuid)
       :createwallet
       [wallet-name
        true  ;; disable private keys
        true  ;; blank wallets
        ""    ;; passphrase
        false ;; avoid reusing addresses
        false ;; descriptor wallet
        ])
      :body
      json/parse-string))

(defn list-wallet-dir []
  ;; list the created wallets on the node
  (-> (make-request (url) (uuid) :listwalletdir [])
      :body
      json/parse-string
      (get-in ["result" "wallets"])))


(defn list-wallets []
  ;; list the current loaded wallets on the node
  (-> (make-request (url) (uuid) :listwallets [])
      :body
      json/parse-string
      (get-in ["result"])))

(defn load-wallet [wallet-name]
  ;; list the current loaded wallets on the node
  (-> (make-request (url) (uuid) :loadwallet [wallet-name])
      :body
      json/parse-string))

(defn get-new-address [wallet-name]
  (-> (make-request (url) (uuid) :getnewaddress [wallet-name])
      :body
      json/parse-string
      (get-in ["result"])))

(defn get-balance [wallet-name]
  (-> (make-request
       (url (str "wallet/" wallet-name))
       (uuid)
       :getbalance [])
      :body
      json/parse-string
      (get-in ["result"])))

(defn address->import-multi [address]
  {:scriptPubKey {:address (address :address)}
   :timestamp (date-time-str->timestamp (address :created_at))
   :watchonly true})

(defn multi-import [wallet-name addresses-import]
  (try
    (make-request
     (url (str "wallet/" wallet-name))
     (uuid)
     :importmulti [addresses-import])
    (catch Exception e (pprint/pprint e))))

(defn list-unspent [wallet-name addresses]
  (-> (make-request
       (url (str "wallet/" wallet-name))
       (uuid)
       :listunspent [0 ;; minimum number of confirmations
                     2147483647 ;; maximum number of confirmations
                     addresses ;; addresses
                     ])
      :body
      json/parse-string
      (get-in ["result"])))

(comment
  (create-wallet "joao-wallet")
  (list-wallet-dir)
  (list-wallets)
  (load-wallet "joao-wallet")
  (list-wallets)
  (get-new-address "joao-wallet")
  (get-balance "joao-wallet")
  (multi-import "joao-wallet" (map address->import-multi (get-wallet-addresses (db/get-db) 1)))

  (list-unspent "joao-wallet" (map :address (get-wallet-addresses (db/get-db) 1)))
 	;;
  )
