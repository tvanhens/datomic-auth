(ns datomic-auth.utils
  (:require [clj-time.coerce :as t-coerce]))

(defn parse-uuid [s]
  {:pre [(string? s)]}
  (java.util.UUID/fromString s))

(defn parse-date-int [i] (-> i long t-coerce/from-long))
