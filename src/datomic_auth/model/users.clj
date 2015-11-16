(ns datomic-auth.model.users
  (:require [datomic-auth.model.protocols :refer :all]
            [datomic-auth.model.utils :refer :all]
            [datomic-auth.auth :as auth]))

(defrecord User [username password]
  Createable
  (create [_ tempid]
    [{:db/id         tempid
      :user/uuid     (uuid)
      :user/username username
      :user/password (auth/hash-password password)}])

  (part [_] :users))
