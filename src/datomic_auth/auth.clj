(ns datomic-auth.auth
  (:require [buddy.hashers :as hashers]
            [datomic-auth.db :as db]))

(defn hash-password [password]
  (hashers/encrypt password))

(defn login-valid? [username attempt]
  (db/q* '{:find  [?valid .]
           :in    [$ ?username ?attempt]
           :where [[?e :user/username ?username]
                   [?e :user/password ?encrypted]
                   [(buddy.hashers/check ?attempt ?encrypted) ?valid]]}
         username
         attempt))
