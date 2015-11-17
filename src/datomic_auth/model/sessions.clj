(ns datomic-auth.model.sessions
  (:require [datomic.api :as d]
            [datomic-auth.db :as db]))

(defn- event-ident [kw]
  (->> kw name (keyword "session.event")))

(defn create [token user event]
  [{:db/id         (d/tempid :sessions)
    :session/token token
    :session/user  (db/ident user)
    :session/event (event-ident event)}])
