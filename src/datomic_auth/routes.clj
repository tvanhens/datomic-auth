(ns datomic-auth.routes)

(def routes ["" [["/login"    :login]
                 ["/logout"   :logout]
                 ["/register" :register]]])