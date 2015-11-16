(ns datomic-auth.handler.auth
  (:require [datomic-auth.auth :as auth]
            [datomic-auth.db :as db]
            [datomic-auth.handler.impl :as impl]
            [datomic-auth.model.users :as users]
            [ring.util.response :refer :all]))

(def username #(get-in % [:form-params :username]))

(def password #(get-in % [:form-params :password]))

(defmethod impl/route-handler [:register :post]
  [request]
  @(db/transact [(users/->User (username request)
                               (password request))])
  (response "User Registered Successfully"))

(defmethod impl/route-handler [:login :post]
  [request]
  (let [username (username request)]
    (if (users/login-valid? username (password request))
      (response {:token (auth/generate-token (users/username->uuid username))})
      (response "Login Failed"))))
