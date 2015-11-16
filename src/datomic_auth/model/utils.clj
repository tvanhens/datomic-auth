(ns datomic-auth.model.utils
  (:require [datomic.api :as d]))

(defn uuid [] (d/squuid))
