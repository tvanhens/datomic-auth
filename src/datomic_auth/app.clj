(ns datomic-auth.app
  (:require [bidi.bidi :as bidi]
            [datomic-auth.auth :as auth]
            [datomic-auth.db :as db]
            [datomic-auth.model.users :refer [->User] :as users]
            [ring.util.response :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def routes ["" [["/login"    :login]
                 ["/logout"   :logout]
                 ["/register" :register]]])

(def username #(get-in % [:form-params :username]))

(def password #(get-in % [:form-params :password]))

(defmulti route-handler (juxt :handler :request-method))

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

(defn handler [{:keys [uri] :as request}]
  (let [request* (merge request (bidi/match-route routes uri))]
    (route-handler request*)))

(def app (-> handler
             auth/wrap-authentication
             (wrap-defaults api-defaults)
             db/wrap-conn))
