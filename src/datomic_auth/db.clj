(ns datomic-auth.db
  (:require [clojure.java.io :as io]
            [datomic.api :as d]
            [datomic-auth.model.protocols :as protos]
            [mount :refer [defstate]]))

(def uri "datomic:mem://datomic-auth")

(def ^:private schema (-> "schema.edn" io/resource slurp read-string))

(defstate conn
  :start (do (d/create-database uri)
             (let [conn (d/connect uri)]
               @(d/transact conn schema)
               conn))
  :stop  (d/delete-database uri))

(defn- instance->tx-data [instance]
  (condp #(satisfies? % instance)
      protos/Createable (protos/create instance (d/tempid (protos/part instance)))))

(defn- concat-tx-data [tx-data instance]
  (concat tx-data (instance->tx-data instance)))

(defn- instances->tx-data [instances]
  (reduce concat-tx-data [] instances))

(defn q* [query & args]
  (apply d/q query (d/db conn) args))

(defn transact [instances]
  (d/transact conn (instances->tx-data instances)))

(defn wrap-conn [handler]
  (fn [request]
    (handler (assoc request :conn conn))))

(comment
  (mount/start)
  )
