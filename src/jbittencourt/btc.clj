(ns jbittencourt.btc
  (:require [jbittencourt.utils :as util]
            [clojure.pprint :as pprint])
  (:import (org.bitcoinj.core Address Transaction SegwitAddress)
           (org.bitcoinj.crypto DeterministicKey HDKeyDerivation MnemonicCode)
           (org.bitcoinj.script Script$ScriptType ScriptPattern)))

(def NETWORK_PARAMS
  (org.bitcoinj.params.TestNet3Params.))

(defn generate-random-mnemonic []
  (.toMnemonic
   (new MnemonicCode)
   (util/generate-random-byte-array 16)))

(defn- from-base58 [xpriv]
  (DeterministicKey/deserializeB58 xpriv NETWORK_PARAMS))

(defn- to-address [key]
  (.toString (Address/fromKey NETWORK_PARAMS key Script$ScriptType/P2WPKH)))

(defn- derive-receive-key [xpriv-str index]
  (->  xpriv-str
       from-base58
       (.derive 84)
       (.derive 0)
       (.derive 0)
       (HDKeyDerivation/deriveChildKey 0)
       (HDKeyDerivation/deriveChildKey index)))

(defn create-master-key
  ([]
   (create-master-key (generate-random-mnemonic)))
  ([mnemonic]
   (create-master-key mnemonic ""))
  ([mnemonic passphrase]
   (.serializePrivB58
    (HDKeyDerivation/createMasterPrivateKey
     (MnemonicCode/toSeed mnemonic passphrase)) NETWORK_PARAMS)))

(defn generage-receive-address [xpriv index]
  (to-address (derive-receive-key xpriv index)))

(defn bytes->transaction [bytes]
  (Transaction. NETWORK_PARAMS bytes))

(defn output->address [output]
  (let [scriptPubKey (.getScriptPubKey output)]
    (cond
      (ScriptPattern/isP2WPKH scriptPubKey)
      (let [segwitHash (.getPubKeyHash scriptPubKey)
            segwitAddress (SegwitAddress/fromHash NETWORK_PARAMS segwitHash)]
        (.toString segwitAddress))

      :else nil)))

(defn parse-bytes-tx-addresses [bytes]
  (->> bytes
       bytes->transaction
       .getOutputs
       (map (fn [out]
              (let [value (.toBtc (.getValue out))
                    address (output->address out)]
                [address value])))))

(comment
  (def master (create-master-key))
  (generage-receive-address master 5))

(comment
  (def raw-tx [2, 0, 0, 0, 0, 1, 1, 50, 17, -68, 25, 103, 109, -86, 32, 10, -127, 8,
               -32, 82, -70, 95, 90, -83, 103, -23, -120, -45, 5, 51, -87, 49, -44,
               75, 68, -5, 91, -16, 100, 0, 0, 0, 0, 0, -1, -1, -1, -1, 2, -24, 3, 0,
               0, 0, 0, 0, 0, 22, 0, 20, 96, 66, 111, -86, 101, 60, 83, 101, 2, -32,
               -96, -45, 86, 88, -48, -70, 37, -102, 99, -74, 91, 3, 0, 0, 0, 0, 0,
               0, 22, 0, 20, 51, 62, 73, -49, 116, -102, -117, -73, -87, -65, 75, -5,
               -57, -1, -63, 82, 82, 114, 45, 64, 2, 71, 48, 68, 2, 32, 68, -64,
               -122, 91, 35, 80, 122, 56, 97, 44, 2, -114, 89, 58, 58, 124, 92, 97,
               -65, 51, -37, -49, 83, -102, -61, -5, -95, -25, -79, -48, 34, -78, 2,
               32, 4, 74, -90, 98, 58, 44, 82, -96, 105, -59, 99, 118, -109, 91, 67,
               -82, 121, 97, -1, 109, 74, 62, 112, 4, 38, -19, -9, -54, -5, 9, 58,
               -113, 1, 33, 2, -44, -65, -6, 3, 63, -27, 95, 125, 113, -27, 14, 57,
               -113, 55, 47, 103, 105, -84, -70, -120, 43, -39, 13, -72, -53, -1,
               -59, -10, -13, -111, 18, 113, 0, 0, 0, 0])

  (map #(.toString %) (parse-bytes-tx-addresses (byte-array raw-tx)))
  (parse-bytes-tx-addresses (byte-array raw-tx))
 	;;
  )
