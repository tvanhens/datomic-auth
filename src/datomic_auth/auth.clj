(ns datomic-auth.auth
  (:require [buddy.hashers :as hashers]
            [buddy.core.hash :as hash]
            [buddy.sign.jwe :as jwe]
            [buddy.auth.backends.token :as token]
            [buddy.auth.middleware :as middleware]))

(defn now [] (java.util.Date.))

(def secret (hash/sha256 "notasecret"))

(defn hash-password [password] (hashers/encrypt password))

(defn generate-token [uuid] (jwe/encode {:uuid uuid :created (now)} secret))

(defn check-password [attempt encrypted] (hashers/check attempt encrypted))

(def backend (token/jwe-backend {:secret secret}))

(defn wrap-authentication [handler] (middleware/wrap-authentication handler backend))
