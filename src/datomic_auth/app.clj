(ns datomic-auth.app
  (:require [bidi.bidi :as bidi]
            [datomic-auth.auth :as auth]
            [datomic-auth.db :as db]
            [datomic-auth.handlers]
            [datomic-auth.handlers.impl :refer :all]
            [datomic-auth.routes :refer [routes]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(defn handler [{:keys [uri] :as request}]
  (let [request* (merge request (bidi/match-route routes uri))]
    (route-handler request*)))

(def app (-> handler
             auth/wrap-authentication
             (wrap-defaults api-defaults)
             db/wrap-conn))
