(ns datomic-auth.model.users
  (:require [datomic-auth.model.protocols :refer :all]
            [datomic-auth.model.utils :refer :all]
            [datomic-auth.db :as db]
            [datomic-auth.auth :as auth]))

(defrecord User [username password]
  Createable
  (create [_ tempid]
    [{:db/id         tempid
      :user/uuid     (uuid)
      :user/username username
      :user/password (auth/hash-password password)}])

  (part [_] :users))

(defn username->uuid [username]
  {:post [%]}
  (db/q* '{:find  [?uuid .]
           :in    [$ ?username]
           :where [[?e :user/username ?username]
                   [?e :user/uuid ?uuid]]}
         username))

(defn login-valid? [username attempt]
  (db/q* '{:find  [?valid .]
           :in    [$ ?username ?attempt]
           :where [[?e :user/username ?username]
                   [?e :user/password ?encrypted]
                   [(datomic-auth.auth/check-password ?attempt ?encrypted) ?valid]]}
         username
         attempt))
