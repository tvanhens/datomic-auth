(ns datomic-auth.db
  (:require [datomic.api :as d]
            [datomic-auth.schema :refer [schema]]
            [mount :refer [defstate]]))

(defprotocol IIdent
  (ident [_]))

(defprotocol ITxData
  (tx-data [_]))

(extend-protocol ITxData
  clojure.lang.PersistentVector
  (tx-data [this] this))

(extend-protocol IIdent
  clojure.lang.PersistentVector
  (ident [this] this))

(def uri "datomic:mem://datomic-auth")

(defstate conn
  :start (do (d/create-database uri)
             (let [conn (d/connect uri)]
               @(d/transact conn schema)
               conn))
  :stop  (d/delete-database uri))

(defn db [] (d/db conn))

(defn concat-tx-data [acc coll] (concat acc (tx-data coll)))

(defn transact [coll]
  (let [tx-data (reduce concat-tx-data [] coll)]
    (d/transact conn tx-data)))

(defn wrap-conn [handler]
  (fn [request]
    (handler (assoc request :conn conn))))
