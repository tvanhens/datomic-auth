(ns datomic-auth.model.users-test
  (:require [clojure.test :refer :all]
            [datomic-auth.model.users :refer :all]
            [datomic-auth.db :as db]
            [mount]))

(defn reset-state [f]
  (mount/start)
  (f)
  (mount/stop))

(use-fixtures :each reset-state)

(deftest create-user-test
  (db/transact [(->User "test-user" "test-password")])
  (is (user? "test-user"))
  (is (not (user? "not-a-user"))))

(deftest user-fns-tests
  (db/transact [(->User "test-user" "good-password")])
  (testing "username->uuid"
    (let [uuid (username->uuid "test-user")]
      (is (= (type uuid)
             java.util.UUID))))

  (testing "login-valid?"
    (is (login-valid? "test-user" "good-password"))
    (is (not (login-valid? "test-user" "bad-password")))))
