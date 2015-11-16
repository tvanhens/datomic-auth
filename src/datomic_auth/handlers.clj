(ns datomic-auth.handlers
  (:require [datomic-auth.handlers.impl :refer :all]
            [datomic-auth.db :as db]
            [datomic-auth.model.users :refer [->User] :as users]
            [datomic-auth.auth :as auth]
            [ring.util.response :refer :all]))

(def username #(get-in % [:form-params :username]))

(def password #(get-in % [:form-params :password]))

(defmethod route-handler [:register :post]
  [request]
  @(db/transact [(->User (username request)
                         (password request))])
  (response "User Registered Successfully"))

(defmethod route-handler [:login :post]
  [request]
  (let [username (username request)]
    (if (users/login-valid? username (password request))
      (response {:token (auth/generate-token (users/username->uuid username))})
      (response "Login Failed"))))
