(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [mount]))

(defn go []
  (mount/stop)
  (mount/start))

(defn reset []
  (refresh :after 'user/go))
