(ns datomic-auth.handler.impl)

(defmulti route-handler (juxt :handler :request-method))
