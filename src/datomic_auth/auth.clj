(ns datomic-auth.auth
  (:require [buddy.hashers :as hashers]
            [buddy.core.hash :as hash]
            [buddy.sign.jwe :as jwe]
            [buddy.auth.backends.token :as token]
            [buddy.auth.middleware :as middleware]
            [datomic-auth.utils :as utils]))

(defn now [] (java.util.Date.))

(def secret (hash/sha256 "notasecret"))

(defn hash-password [password] (hashers/encrypt password))

(defn generate-token [user-uuid session-uuid]
  (jwe/encode {:user-uuid     user-uuid
               :created       (now)
               :session-uuid  session-uuid}
              secret))

(defn check-password [attempt encrypted] (hashers/check attempt encrypted))

(def ^:private backend (token/jwe-backend {:secret secret}))

(defn- parse-identity [identity]
  (some-> identity
          (update :user-uuid utils/parse-uuid)
          (update :session-uuid utils/parse-uuid)
          (update :created utils/parse-date-int)))

(defn wrap-parse-identity [handler]
  (fn [{:keys [identity] :as request}]
    (handler (update request :identity parse-identity))))

(defn wrap-authentication-rules [handler]
  (fn [{:keys [identity] :as request}]
    (handler request)))

(defn wrap-authentication [handler]
  (-> handler
      wrap-authentication-rules
      wrap-parse-identity
      (middleware/wrap-authentication backend)))
