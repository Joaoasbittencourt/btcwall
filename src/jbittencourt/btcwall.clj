(ns jbittencourt.btcwall
  (:gen-class)
  (:require [clojure.java.jdbc :as jdbc]
            [honey.sql :as sql]
            [jbittencourt.btc :as btc]
            [jbittencourt.db :as db]))


(defn get-wallet [conn wallet-id]
  (first (jdbc/query
          conn (sql/format {:select [:id :name :xpriv :created_at]
                            :from :wallets
                            :where [:= :id wallet-id]
                            :limit 1}))))

(defn get-wallet-addresses [conn wallet-id]
  (into []
        (jdbc/query
         conn (sql/format {:select [:id :address :derivation_index :created_at]
                           :from :addresses
                           :where [:= :wallet_id wallet-id]
                           :order-by [:derivation_index]}))))

(defn insert-address! [conn {:keys [wallet-id derivation-index address]}]
  (jdbc/query
   conn (sql/format {:insert-into :addresses
                     :values [{:wallet-id wallet-id
                               :derivation-index derivation-index
                               :address address}]
                     :returning [:id :address :derivation-index]})))


(defn generate-next-wallet-address-command [wallet-id]
  (jdbc/with-db-transaction [conn (db/get-db)]
    (let [wallet (get-wallet conn wallet-id)
          addresses (get-wallet-addresses conn (wallet :id))
          new-address (btc/generage-receive-address (:xpriv wallet) (count addresses))
          address (insert-address! conn {:wallet-id wallet-id
                                         :derivation-index (count addresses)
                                         :address new-address})]
      (assoc wallet :addresses (conj addresses address)))))

(defn create-wallet-command [name mnemonic passphrase]
  (let [conn (db/get-db)
        xpriv (btc/create-master-key mnemonic passphrase)
        wallet-values {:name name :xpriv xpriv}
        insert-query (sql/format {:insert-into :wallets :values [wallet-values]
                                  :returning [:id :name :xpriv]})
        db-result (jdbc/query conn insert-query)]
    (-> db-result first)))


(comment
  (create-wallet-command "joaowallet" (btc/generate-random-mnemonic) "test123")
  (get-wallet (db/get-db) 1)
  (get-wallet-addresses (db/get-db) 1)
  (generate-next-wallet-address-command 1)
  (get-wallet-addresses (db/get-db) 1))

(defn -main [& args])
