(ns datomic-auth.db
  (:require [datomic.api :as d]
            [datomic-auth.schema :refer [schema]]
            [mount :refer [defstate]]))

(def uri "datomic:mem://datomic-auth")

(defstate conn
  :start (do (d/create-database uri)
             (let [conn (d/connect uri)]
               @(d/transact conn schema)
               conn))
  :stop  (d/delete-database uri))

(defn db [] (d/db conn))

(defn transact [tx-data] (d/transact conn tx-data))

(defn wrap-conn [handler]
  (fn [request]
    (handler (assoc request :conn conn))))
