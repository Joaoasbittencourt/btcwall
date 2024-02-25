(ns jbittencourt.db
  (:require [clojure.java.jdbc :as jdbc]
            [honey.sql :as sql]))

(def db
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "btcwall.db"})

(defn get-db [] db)

(def schema
  [{:create-table [:wallets :if-not-exists]
    :with-columns [[:id :integer :primary-key :autoincrement]
                   [:name :text :not nil]
                   [:xpriv :text :not nil]
                   [:created-at :datetime :not nil :default :CURRENT_TIMESTAMP]]}
   {:create-table [:addresses :if-not-exists]
    :with-columns [[:id :integer :primary-key :autoincrement]
                   [:address :text :not nil]
                   [:derivation-index :integer :not nil]
                   [:created-at :datetime :not nil :default :CURRENT_TIMESTAMP]
                   [:wallet-id :integer :not nil]
                   [[:foreign-key :wallet-id] [:references [:wallets :id]]]]}])

(defn create-tables! [schema]
  (jdbc/with-db-transaction [conn db]
    (doseq [table schema]
      (jdbc/db-do-commands conn (sql/format table)))))


(comment
  (create-tables! schema))
