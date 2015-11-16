(ns datomic-auth.model.users
  (:require [datomic-auth.model.protocols :refer :all]
            [datomic-auth.model.utils :refer :all]
            [datomic-auth.db :as db]
            [datomic-auth.auth :as auth]
            [datomic.api :as d]))

(defrecord User [username password]
  Createable
  (create [_ tempid]
    [{:db/id         tempid
      :user/uuid     (uuid)
      :user/username username
      :user/password (auth/hash-password password)}])

  (part [_] :users))

(defrecord ChangePassword [username password]
  Transactable
  (tx-data [_]
    [{:db/id         [:user/username username]
      :user/password (auth/hash-password)}]))

(defn username->uuid [db username]
  {:post [%]}
  (d/q '{:find  [?uuid .]
         :in    [$ ?username]
         :where [[?e :user/username ?username]
                 [?e :user/uuid ?uuid]]}
       db
       username))

#_(defn request->username [{:keys [identity] :as request}]
    (db/request-q '{:find  [?username .]
                    :in    [$ ?uuid]
                    :where [[?e :user/uuid ?uuid]
                            [?e :user/username ?username]]}
                  request
                  (:uuid identity)))

(defn login-valid? [db username attempt]
  (d/q '{:find  [?valid .]
         :in    [$ ?username ?attempt]
         :where [[?e :user/username ?username]
                 [?e :user/password ?encrypted]
                 [(datomic-auth.auth/check-password ?attempt ?encrypted) ?valid]]}
       db
       username
       attempt))

(defn user? [db username]
  (d/q '{:find  [?username .]
         :in    [$ ?username]
         :where [[_ :user/username ?username]]}
       db
       username))

(defn user-entity? [db e]
  (d/q '{:find  [?e]
         :in    [$ ?e]
         :where [[?e :user/username]]}
       db
       e))
