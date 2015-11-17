(ns datomic-auth.model.sessions
  (:require [datomic.api :as d]
            [datomic-auth.db :as db]))

(defn create [uuid user]
  [{:db/id         (d/tempid :sessions)
    :session/uuid  uuid
    :session/user  (db/ident user)}])
