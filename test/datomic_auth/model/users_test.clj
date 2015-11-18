(ns datomic-auth.model.users-test
  (:require [clojure.test :refer :all]
            [datomic-auth.model.users :refer :all]
            [datomic-auth.utils :as utils]
            [datomic-auth.db :as db]
            [mount]))

(defn reset-state [f]
  (mount/start)
  (f)
  (mount/stop))

(use-fixtures :each reset-state)

(deftest create-user-test
  @(db/transact [(create "test-user" "test-password" (utils/uuid))])
  (is (username-exists? (db/db) "test-user"))
  (is (user-entity? (db/db) [:user/username "test-user"]))
  (is (not (username-exists? (db/db) "not-a-user")))
  (is (not (user-entity? (db/db) [:user/username "not-a-user"]))))

(deftest user-fns-tests
  @(db/transact [(create "test-user" "good-password" (utils/uuid))])
  (testing "username->uuid"
    (let [uuid (username->uuid (db/db) "test-user")]
      (is (= (type uuid) java.util.UUID))))

  (testing "login-valid?"
    (is (login-valid? (db/db) "test-user" "good-password"))
    (is (not (login-valid? (db/db) "test-user" "bad-password"))))

  (testing "change-password"
    @(db/transact [(change-password "test-user" "new-password")])
    (is (login-valid? (db/db) "test-user" "new-password"))
    (is (not (login-valid? (db/db) "test-user" "good-password")))))
