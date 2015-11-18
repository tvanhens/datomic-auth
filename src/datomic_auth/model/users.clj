(ns datomic-auth.model.users
  (:require [datomic-auth.utils :as utils]
            [datomic-auth.db :as db]
            [datomic-auth.auth :as auth]
            [datomic.api :as d]))

(defrecord User [tempid username password uuid]
  db/IIdent
  (ident [_]
    (cond
      tempid   tempid
      username [:user/username username]))

  db/ITxData
  (tx-data [_]
    [{:db/id         tempid
      :user/uuid     uuid
      :user/username username
      :user/password (auth/hash-password password)}]))

(defn create [username password uuid]
  (->User (d/tempid :users) username password uuid))

(defn change-password [username password]
  [{:db/id         [:user/username username]
    :user/password (auth/hash-password password)}])

(defn username->uuid [db username]
  (:user/uuid (d/entity db [:user/username username])))

(defn login-valid? [db username attempt]
  (->> (d/entity db [:user/username username])
       :user/password
       (auth/check-password attempt)))

(defn user-entity? [db e] (:user/username (d/entity db e)))

(defn username-exists? [db username]
  (:user/username (d/entity db [:user/username username])))
