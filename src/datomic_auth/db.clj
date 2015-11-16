(ns datomic-auth.db
  (:require [datomic.api :as d]
            [datomic-auth.model.protocols :as protos]
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

(defn- instance->tx-data [instance]
  (condp #(satisfies? % instance)
      protos/Createable (protos/create instance (d/tempid (protos/part instance)))))

(defn- concat-tx-data [tx-data instance]
  (concat tx-data (instance->tx-data instance)))

(defn- instances->tx-data [instances] (reduce concat-tx-data [] instances))

#_(defn filter-passwords [user-uuid db [e a v t]]
  (not (= (d/ident db a) :user/password)))

#_(defn filter-users [user-uuid db [e a v t]]
  (if (users/user-entity?  e)
    (=  (:user/uuid (d/entity db e)) user-uuid)
    true))

#_(def ^:private +filters+ [filter-passwords
                          filter-users])

#_(defn- request->db [{:keys [identity] :as request}]
  (as-> request <>
    (:conn <>)
    (d/db <>)
    (reduce d/filter <> (map #(partial % (:uuid identity)) +filters+))))

(defn transact [instances] (d/transact conn (instances->tx-data instances)))

(defn wrap-conn [handler]
  (fn [request]
    (handler (assoc request :conn conn))))
