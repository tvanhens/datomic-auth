(ns datomic-auth.lib.auth.core
  (:require [datomic-auth.model.users :as users]
            [datomic-auth.auth :as auth]
            [datomic-auth.db :as db]
            [ring.util.response :refer :all]))

(defn- token-response [uuid] (response {:token (auth/generate-token uuid)}))

(defn- username-exists-response [username]
  (-> {:msg (format "Username [%s] already exists." username)} response (status 400)))

(defn- invalid-username-password-response []
  (-> {:msg "Invalid username/password combination."} response (status 400)))

(defn- new-confirm-mismatch-response []
  (-> {:msg "New password does not match confirmation."} response (status 400)))

(defn register-user [db username password]
  (if (users/username-exists? db username)
    (username-exists-response username)
    (let [{:keys [db-after]} @(db/transact (users/create username password))]
      (token-response (users/username->uuid db-after username)))))

(defn login [db username password]
  (if (users/login-valid? db username password)
    (token-response (users/username->uuid db username))
    (invalid-username-password-response)))

(defn change-password [db username old-password new-password confirm]
  (if (users/login-valid? db username old-password)
    (if (= new-password confirm)
      (let [{:keys [db-after]} @(db/transact (users/change-password username new-password))]
        (token-response (users/username->uuid db username)))
      (new-confirm-mismatch-response))
    (invalid-username-password-response)))
