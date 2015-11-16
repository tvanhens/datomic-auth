(ns datomic-auth.model.protocols)

(defprotocol Createable
  (create [this temp-id])
  (part   [this]))
