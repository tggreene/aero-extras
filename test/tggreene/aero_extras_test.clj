(ns tggreene.aero-extras-test
  (:require [clojure.test :refer [deftest testing is]]
            [aero.core :as aero]
            [tggreene.aero-extras :as aero-extras]
            [clojure.java.io :as io]
            [cognitect.aws.client.api :as aws]))

(deftest can-parse-json-in-aero-config
  (let [config (.getBytes "{:config #json \"{\\\"a\\\":{\\\"b\\\":\\\"c\\\"}}\"}")]
    (is (= {:config {:a {:b "c"}}} (aero/read-config config)))))

(deftest tries-to-resolve-aws-secret-in-aero-config
  (let [config (.getBytes "{:secret #aws-secret \"secret-key\"}")]
    (testing "successful result"
      (with-redefs [aws/invoke (fn [_ _]
                                 {:ARN "arn:aws:secretsmanager:us-east-2:000000000000:secret:secret-key"
                                  :CreatedDate #inst "2021-11-11T00:00:00.000-00:00"
                                  :Name "secret-key"
                                  :SecretString "secret-value"
                                  :VersionId "b7a3319f-b624-48be-bd46-361353261214"
                                  :VersionStages ["AWSCURRENT"]})]
        (is (= {:secret "secret-value"}
               (aero/read-config config)))))

    (testing "unable to fetch region"
      (with-redefs [aws/invoke (fn [_ _]
                                 {:cognitect.anomalies/category :cognitect.anomalies/fault,
                                  :cognitect.anomalies/message "Unable to fetch region.",
                                  :cognitect.aws.util/throwable
                                  (ex-info "No region found by any region provider." {:providers []})})]
        (try
          (aero/read-config config)
          (catch Exception e
            (is (= {:message "Unable to fetch region."
                    :category :cognitect.anomalies/fault}
                   (ex-data e)))))))

    (testing "unable to fetch credentials"
      (with-redefs [aws/invoke (fn [_ _]
                                 #:cognitect.anomalies
                                 {:category :cognitect.anomalies/fault,
                                  :message "Unable to fetch credentials. See log for more details."})]
        (try
          (aero/read-config config)
          (catch Exception e
            (is (= {:message "Unable to fetch credentials. See log for more details."
                    :category :cognitect.anomalies/fault}
                   (ex-data e)))))))

    (testing "secret not found"
      (with-redefs [aws/invoke (fn [_ _]
                                 {:Message "Secrets Manager can't find the specified secret.",
                                  :__type "ResourceNotFoundException",
                                  :cognitect.anomalies/category :cognitect.anomalies/incorrect})]
        (try
          (aero/read-config config)
          (catch Exception e
            (is (= {:category :cognitect.anomalies/incorrect
                    :message "Secrets Manager can't find the specified secret."
                    :additional-info {:type "ResourceNotFoundException"}}
                   (ex-data e)))))))))
