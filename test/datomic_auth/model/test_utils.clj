(ns datomic-auth.model.test-utils
  (:require [mount]))

(defn reset-state [f]
  (mount/start)
  (f)
  (mount/stop))
