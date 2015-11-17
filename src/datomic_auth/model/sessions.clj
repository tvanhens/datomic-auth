(ns datomic-auth.model.sessions
  (:require [datomic.api :as d]
            [datomic-auth.db :as db]))

(defn create [token user]
  [{:db/id         (d/tempid :sessions)
    :session/token token
    :session/user  (db/ident user)}])
