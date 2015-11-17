(ns datomic-auth.model.users
  (:require [datomic-auth.model.utils :refer :all]
            [datomic-auth.db :as db]
            [datomic-auth.auth :as auth]
            [datomic.api :as d]))

(defn create [username password]
  [{:db/id         (d/tempid :users)
    :user/uuid     (uuid)
    :user/username username
    :user/password (auth/hash-password password)}])

(defn change-password [username password]
  [{:db/id         [:user/username username]
    :user/password (auth/hash-password password)}])

(defn username->uuid [db username]
 (:user/uuid (d/entity db [:user/username username])))

(defn request->username [db {:keys [identity] :as request}]
  (:user/username (d/entity db [:user/uuid (:uuid identity)])))

(defn login-valid? [db username attempt]
  (->> (d/entity db [:user/username username])
       :user/password
       (auth/check-password attempt)))

(defn user-entity? [db e] (:user/username (d/entity db e)))

(defn username-exists? [db username]
  (:user/username (d/entity db [:user/username username])))
