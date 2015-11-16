(ns datomic-auth.model.users
  (:require [datomic-auth.model.protocols :refer :all]
            [datomic-auth.model.utils :refer :all]
            [buddy.hashers :as hashers]))

(defrecord User [username password]
  Createable
  (create [_ tempid]
    [{:db/id         tempid
      :user/uuid     (uuid)
      :user/username username
      :user/password (hashers/encrypt password)}])

  (part [_] :users))
