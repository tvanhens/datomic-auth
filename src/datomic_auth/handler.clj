(ns datomic-auth.handler
  (:require [bidi.bidi :as bidi]
            [datomic-auth.routes :refer [routes]]))

(defn handler [{:keys [uri] :as request}]
  request)
