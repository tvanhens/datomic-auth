(ns datomic-auth.auth
  (:require [buddy.hashers :as hashers]
            [buddy.core.hash :as hash]
            [buddy.sign.jwe :as jwe]
            [buddy.auth.backends.token :as token]
            [buddy.auth.middleware :as middleware]
            [buddy.auth.accessrules :as accessrules]
            [datomic-auth.utils :as utils]
            [datomic-auth.accessrules :refer [rules unauthorized-handler]]))

(def secret (hash/sha256 "notasecret"))

(defn hash-password [password] (hashers/encrypt password))

(defn generate-token [uuid] (jwe/encode {:uuid uuid} secret))

(defn check-password [attempt encrypted] (hashers/check attempt encrypted))

(def ^:private backend (token/jwe-backend {:secret secret}))

(defn- parse-identity [identity] (some-> identity (update :uuid utils/parse-uuid)))

(defn- wrap-parse-identity [handler]
  (fn [{:keys [identity] :as request}]
    (handler (update request :identity parse-identity))))

(defn wrap-authentication [handler]
  (-> handler
      wrap-parse-identity
      (accessrules/wrap-access-rules {:rules    rules
                                      :policy   :reject
                                      :on-error unauthorized-handler})
      (middleware/wrap-authentication backend)))

(comment

  ((wrap-authentication identity)
   {:uri "/hello"
    :request-method :get})

  )
