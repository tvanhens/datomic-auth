(ns datomic-auth.app
  (:require [bidi.bidi :as bidi]
            [datomic-auth.auth :as auth]
            [datomic-auth.db :as db]
            [datomic-auth.handler :refer [handler]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def app (-> handler
             auth/wrap-authentication
             (wrap-defaults api-defaults)
             db/wrap-conn))
