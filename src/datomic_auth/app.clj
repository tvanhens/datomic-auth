(ns datomic-auth.app
  (:require [bidi.bidi :as bidi]
            [datomic-auth.db :as db]
            [datomic-auth.model.users :refer [->User]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def routes ["" [["/login"    :login]
                 ["/logout"   :logout]
                 ["/register" :register]]])

(def username #(get-in % [:form-params :username]))

(def password #(get-in % [:form-params :password]))

(defmulti route-handler (juxt :handler :request-method))

(defmethod route-handler [:register :post]
  [request]
  {:status 200
   :body   @(db/transact request [(->User (username request)
                                          (password request))])})

(defn handler [{:keys [uri] :as request}]
  (let [request* (merge request (bidi/match-route routes uri))]
    (route-handler request*)))

(def app (-> handler
             (wrap-defaults api-defaults)
             db/wrap-conn))

(comment

  (mount/start)

  (app {:uri            "/register"
        :request-method :post
        :form-params    {:username "tvanhens"
                         :password "1234"}})

  )
