(ns tggreene.aero-extras
  (:require [aero.core :as aero]
            [cognitect.aws.client.api :as aws]))

(def secretsmanager-client
  (aws/client {:api :secretsmanager}))

(def json-reader
  (or (when-let [read-value (requiring-resolve 'jsonista.core/read-value)]
        (let [keyword-mapper @(requiring-resolve 'jsonista.core/keyword-keys-object-mapper)]
          (fn [s]
            (read-value s keyword-mapper))))
      (when-let [parse-string (requiring-resolve 'cheshire.core/parse-string)]
        (fn [s]
          (parse-string s true)))))

(defmethod aero/reader 'json
  [_ _ value]
  (if json-reader
    (json-reader value)
    (throw (ex-info "Couldn't find a valid json library on classpath" {}))))

(defn handle-aws-anomaly
  [{:cognitect.anomalies/keys [category message]
    :cognitect.aws.util/keys [throwable]
    :keys [Message __type]}]
  (ex-info
   "Couldn't retrieve aws secret"
   (cond-> {:category category
            :message (or message Message "Unknown error")}
     __type (assoc-in [:additional-info :type] __type))
   throwable))

(defmethod aero/reader 'aws-secret
  [_ _ value]
  (let [result
        (aws/invoke
         secretsmanager-client
         {:op :GetSecretValue
          :request {:SecretId value}})]
    (if-not (contains? result :cognitect.anomalies/category)
      (:SecretString result)
      (throw (handle-aws-anomaly result)))))
