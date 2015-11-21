(ns datomic-auth.accessrules)

(defn unauthorized-handler [request]
  (println request)
  {:status 403
   :body   "Unauthorized Request"})

(def rules [])
