(ns datomic-auth.lib.auth.core
  (:require [datomic-auth.model.users :as users]
            [datomic-auth.model.sessions :as sessions]
            [datomic-auth.auth :as auth]
            [datomic-auth.db :as db]
            [datomic-auth.utils :as utils]
            [datomic.api :as d]
            [ring.util.response :refer :all]))

(defn- token-response [token] (response {:token token}))

(defn- username-exists-response [username]
  (-> {:msg (format "Username [%s] already exists." username)} response (status 400)))

(defn- invalid-username-password-response []
  (-> {:msg "Invalid username/password combination."} response (status 400)))

(defn- new-confirm-mismatch-response []
  (-> {:msg "New password does not match confirmation."} response (status 400)))

(defn- generate-token [db username]
  (->> username (users/username->uuid db) auth/generate-token))

(defn register [conn username password]
  (let [db (d/db conn)]
    (if (users/username-exists? db username)
      (username-exists-response username)
      (let [uuid               (utils/uuid)
            user               (users/create username password uuid)
            token              (auth/generate-token uuid)
            {:keys [db-after]} @(db/transact conn [user (sessions/create token user :registered)])]
        (token-response token)))))

(defn login [conn username password]
  (let [db (d/db conn)]
    (if (users/login-valid? db username password)
      (let [token (generate-token db username)]
        @(db/transact conn [(sessions/create token (users/map->User {:username username}) :loggedIn)])
        (token-response token))
      (invalid-username-password-response))))

(defn change-password [conn username old-password new-password confirm]
  (let [db (d/db conn)]
    (if (users/login-valid? db username old-password)
      (if (= new-password confirm)
        (let [token (generate-token db username)
              {:keys [db-after]}
              @(db/transact conn
                            [(users/change-password username new-password)
                             (sessions/create token (users/map->User {:username username}) :passwordChanged)])]
          (token-response token))
        (new-confirm-mismatch-response))
      (invalid-username-password-response))))
