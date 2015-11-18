(ns datomic-auth.model.sessions
  (:require [datomic.api :as d]
            [datomic-auth.db :as db]))

(def ^:private users-tokens
  '[(users-tokens ?user-token ?token ?tokens)
    [?token :session/token ?user-token]
    [?token :session/user ?user]
    [?tokens :session/user ?user]
    [(not= ?tokens ?token)]])

(def ^:private tx
  '[(tx ?e ?tx)
    [?e _ _ ?tx]])

(def ^:private by-event
  '[(by-event ?event ?tokens)
    [(datomic-auth.model.sessions/event-ident ?event) ?event-ident]
    [?tokens :session/event ?event-ident]])

(defn event-ident [kw]
  {:pre [#{:passwordChanged :registered :loggedIn}]}
  (->> kw name (keyword "session.event")))

(defn create [token user event]
  [{:db/id         (d/tempid :sessions)
    :session/token token
    :session/user  (db/ident user)
    :session/event (event-ident event)}])

(defn token? [db token] (:session/token (d/entity db [:session/token token])))

(defn valid-token? [db token]
  (let [valid (d/q '{:find  [[?valid ...]]
                     :in    [$ % ?str-token]
                     :where [(users-tokens ?str-token ?this-token ?users-tokens)
                             (by-event :passwordChanged ?users-tokens)
                             (tx ?this-token ?tx1)
                             (tx ?users-tokens ?tx2)
                             [(>= ?tx1 ?tx2) ?valid]]}
                   db [users-tokens by-event tx] token)]
    (and (token? db token) (every? identity valid))))
