(ns datomic-auth.db
  (:require [datomic.api :as d]
            [datomic-auth.model.protocols :as protos]
            [mount :refer [defstate]]))

(def uri "datomic:mem://datomic-auth")

(defstate conn
  :start (do (d/create-database uri)
             (d/connect uri))
  :stop  (d/delete-database uri))

(def get-conn :conn)

(defn- instance->tx-data [instance]
  (condp #(satisfies? % instance)
      protos/Createable (protos/create instance (d/tempid (protos/part instance)))))

(defn- concat-tx-data [tx-data instance]
  (concat tx-data (instance->tx-data instance)))

(defn- instances->tx-data [instances]
  (reduce concat-tx-data [] instances))

(defn transact [request instances]
  (println (instances->tx-data instances))
  (d/transact (get-conn request) (instances->tx-data instances)))

(defn wrap-conn [handler]
  (fn [request]
    (handler (assoc request :conn conn))))

(comment
  (mount/start)
  )
