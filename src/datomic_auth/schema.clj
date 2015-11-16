(ns datomic-auth.schema
  (:require [clojure.java.io :as io]))

(def schema (concat (-> "schema.edn" io/resource slurp read-string)))
