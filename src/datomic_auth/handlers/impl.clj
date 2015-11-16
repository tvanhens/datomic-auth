(ns datomic-auth.handlers.impl)

(defmulti route-handler (juxt :handler :request-method))
