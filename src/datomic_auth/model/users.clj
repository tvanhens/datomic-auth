(ns datomic-auth.model.users
  (:require [datomic-auth.model.protocols :refer :all]
            [buddy.hashers :as hashers]))

(defrecord User [username password]
  Createable
  (create [_ temp-id]
    [{:db/id         temp-id
      :user/username username
      :user/password (hashers/encrypt password)}])
  (part [_] :part/users))
