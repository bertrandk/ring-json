(ns ring.middleware.json
  "Augment Ring requests and responses with JSON support."
  (:use [ring.util.response :only [response content-type]])
  (:require [cheshire.core :as json]))

(defn- json-request?
  [req]
  (if-let [req-type (:content-type req)]
    (seq (re-seq #"^application/(vnd.+)?json" req-type))))

(defn- parse-json
  "Parse the body of JSON requests."
  [req]
  (if (json-request? req)
    (if-let [body (:body req)]
      (json/parse-string (slurp body)))))

(defn- assoc-json-params
  "Asocc JSON key-values from the request body with the request."
  [req]
  (if-let [json (parse-json req)]
    (merge-with merge req {:json-params json, :params json})))

(defn- receive
  "Return a JSON-parameterized request if appropriate, otherwise return
  original request."
  [req]
  (if-let [req* (assoc-json-params req)]
    req*
    req))

(defn- json-response
  "Convert the body of the response to a JSON string."
  [resp]
  (let [body (:body resp)]
    (if (or (string? body) (coll? body))
      (-> (response (json/generate-string body))
        (content-type "application/json")))))

(defn- respond
  "Return a JSON response if appropriate, otherwise return original response
  body."
  [resp]
  (if-let [resp* (json-response resp)]
    resp*
    resp))

(defn wrap-json
  "Middleware to convert JSON-formatted request bodies into a map of
  parameters and convert outgoing responses into JSON. Adds the following keys
  to the request map, if not already present:
    :json-params - a map of parameters from the body of the JSON request.
    :params      - a merged map of all the parameters."
  [handler]
  (fn [request]
    (let [req (receive request)
          res (handler req)]
      (respond res))))
