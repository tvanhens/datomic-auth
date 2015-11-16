(ns datomic-auth.handler
  (:require [bidi.bidi :as bidi]
            [datomic-auth.handler.impl :refer :all]
            [datomic-auth.routes :refer [routes]]))

(defn handler [{:keys [uri] :as request}]
  (let [request* (merge request (bidi/match-route routes uri))]
    (route-handler request*)))
