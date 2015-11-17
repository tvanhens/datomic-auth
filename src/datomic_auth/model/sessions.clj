(ns datomic-auth.model.sessions
  (:require [datomic.api :as d]
            [datomic-auth.db :as db]))

(defn- event-ident [kw]
  (->> kw name (keyword "session.type")))

(defn create [token user event]
  [{:db/id         (d/tempid :sessions)
    :session/token token
    :session/user  (db/ident user)
    :session/type  (event-ident event)}])
