(ns datomic-auth.lib.auth.core-test
  (:require [stateful-check.core :refer [specification-correct?]]
            [clojure.test.check.generators :as gen]
            [datomic-auth.db :as db]
            [datomic-auth.schema :as schema]
            [datomic-auth.model.users :as users]
            [datomic-auth.lib.auth.core :refer :all]
            [datomic-auth.auth :as auth]
            [buddy.hashers :as hashers]
            [datomic.api :as d]
            [clojure.test :refer :all]))

(defn random-db-uri [] (str "datomic:mem://" (d/squuid)))

(defn setup []
  (let [uri (random-db-uri)]
    (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn schema/schema)
      conn)))

;;------------------------------------------------------------------------------
;; Generators

(def gen-valid-username gen/string-alphanumeric)

(defn random-username-password-args [state] [(:conn state) gen-valid-username gen/string])

(defn existing-username-password-args [state]
  (let [users (:users/by-username state)
        user  (gen/elements users)]
    [(:conn state) (gen/bind user (comp gen/return first)) (gen/bind user #(-> % second :password gen/return))]))

(defn users-present? [state] (:users/by-username state))

(defn username-doesnt-exist? [state [_ username]]
  (not (get-in state [:users/by-username username])))

(defn invalid-login? [state [_ username password]]
  (not= (get-in state [:users/by-username username :password]) password))

(defn valid-login? [state [_ username password]]
  (= (get-in state [:users/by-username username :password]) password))

(defn add-token [state username type token]
  (update-in state [:users/by-username username :tokens] conj [type token]))

(defn registered [state [_ username password] response]
  (-> state
      (update-in [:users/by-username username] assoc :password password)
      (add-token username :registered (:token response))))

(defn logged-in [state [_ username] response]
  (add-token state username :loggedIn (:token response)))

(defn status-pred [code] (fn [_ _ _ {:keys [status]}] (= code status)))

(defn init-state [conn] {:conn conn})

(def register-spec
  {:model/args         random-username-password-args
   :model/precondition username-doesnt-exist?
   :real/command       #'register
   :next-state         registered
   :real/postcondition (status-pred 200)})

(def register-with-existing-username-spec
  {:model/args         existing-username-password-args
   :model/requires     users-present?
   :real/command       #'register
   :real/postcondition (status-pred 400)})

(def invalid-login-spec
  {:model/args         random-username-password-args
   :model/precondition invalid-login?
   :real/command       #'login
   :real/postcondition (status-pred 400)})

(def login-spec
  {:model/args         existing-username-password-args
   :model/precondition valid-login?
   :model/requires     users-present?
   :real/command       #'login
   :next-state         logged-in
   :real/postcondition (status-pred 200)})

(def auth-spec
  {:commands      {:register          #'register-spec
                   :register-existing #'register-with-existing-username-spec
                   :invalid-login     #'invalid-login-spec
                   :login             #'login-spec}
   :real/setup    #'setup
   :initial-state init-state})

(deftest auth-test
  (with-redefs [auth/hash-password  identity
                auth/check-password (fn [attempt encrypted] (= attempt encrypted))]
    (is (specification-correct? auth-spec {:print-first-case? true}))))
