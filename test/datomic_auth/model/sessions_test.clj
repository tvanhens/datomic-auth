(ns datomic-auth.model.sessions-test
  (:require [datomic-auth.model.sessions :refer :all]
            [datomic-auth.model.users :as users]
            [clojure.test :refer :all]
            [datomic-auth.utils :as utils]
            [datomic-auth.db :as db]
            [datomic-auth.model.test-utils :as test-utils]))

(use-fixtures :each test-utils/reset-state)

(deftest session-test
  (let [user (users/create "test-user" "pass" (utils/uuid))]
    @(db/transact [user (create "token-1" user :registered)])
    (is (token? (db/db) "token-1"))
    (is (not (token? (db/db) "token-2")))

    (is (valid-token? (db/db) "token-1"))
    (is (not (valid-token? (db/db) "token-2")))

    @(db/transact [(create "token-2" [:user/username "test-user"] :loggedIn)])
    (is (valid-token? (db/db) "token-1"))
    (is (valid-token? (db/db) "token-2"))

    @(db/transact [(create "token-3" [:user/username "test-user"] :passwordChanged)])
    (is (not (valid-token? (db/db) "token-1")))
    (is (not (valid-token? (db/db) "token-2")))
    (is (valid-token? (db/db) "token-3"))
    (is (not (valid-token? (db/db) "token-4")))

    @(db/transact [(create "token-4" [:user/username "test-user"] :loggedIn)])
    (is (not (valid-token? (db/db) "token-1")))
    (is (not (valid-token? (db/db) "token-2")))
    (is (valid-token? (db/db) "token-3"))
    (is (valid-token? (db/db) "token-4"))

    @(db/transact [(create "token-5" [:user/username "test-user"] :passwordChanged)])
    (is (not (valid-token? (db/db) "token-1")))
    (is (not (valid-token? (db/db) "token-2")))
    (is (not (valid-token? (db/db) "token-3")))
    (is (not (valid-token? (db/db) "token-4")))
    (is (valid-token? (db/db) "token-5"))
    (is (not (valid-token? (db/db) "token-6")))))
