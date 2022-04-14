(ns tggreene.aero-extras
  (:require [aero.core :as aero]
            [amazonica.aws.secretsmanager :as secretsmanager]))

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

(defmethod aero/reader 'concat
  [_ _ value]
  (apply concat value))

(defmethod aero/reader 'format
  [_ _ value]
  (apply format value))

(def ^:private get-env #'aero/get-env)

;; Replicate shell var expansion behaviour ${X:-value}
;; Useful in the case where a value can optionally be set to false
;; in the environment
;; Can be used in place of #env for plain env values #env- SOMETHING

(defmethod aero/reader 'env-
  [_ _ value]
  (let [[env-var fallback] (if (sequential? value)
                             value
                             [(first value) nil])
        env-val (get-env env-var)]
    (if (some? env-val)
      env-val
      fallback)))

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
  (:secret-string (secretsmanager/get-secret-value {:secret-id value})))
